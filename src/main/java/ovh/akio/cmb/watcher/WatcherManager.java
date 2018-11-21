package ovh.akio.cmb.watcher;

import fr.alexpado.database.Database;
import fr.alexpado.database.observable.ArrayListListener;
import fr.alexpado.database.observable.ListenableArrayList;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.User;
import ovh.akio.cmb.data.CrossoutItem;
import ovh.akio.cmb.data.discord.DiscordUser;
import ovh.akio.cmb.data.discord.Watcher;
import ovh.akio.cmb.logging.Logger;
import ovh.akio.cmb.throwables.WatcherNotFoundException;
import ovh.akio.cmb.utils.BotUtils;
import ovh.akio.cmb.utils.WebAPI;

import java.sql.ResultSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class WatcherManager implements ArrayListListener<Watcher> {

    private class WatcherThread extends TimerTask {

        private Consumer<Void> onInterval;

        public WatcherThread(Consumer<Void> onInterval) {
            this.onInterval = onInterval;
        }

        @Override
        public void run() {
            onInterval.accept(null);
        }

    }


    private Database database;
    private Timer timer = new Timer();
    private WatcherThread thread;
    private ListenableArrayList<Watcher> watchers = new ListenableArrayList<>();

    @Override
    public void onItemAdded(Watcher item) {
        item.getSqlObject().insert();
    }

    @Override
    public void onItemDeleted(Watcher item) {
        item.getSqlObject().delete();
    }

    public WatcherManager(JDA jda, Database database) {
        this.database = database;
        this.thread = new WatcherThread(onInterval -> onTick(jda));

        HashMap<Integer, CrossoutItem> items = new HashMap<>();

        try (ResultSet rs = this.database.query("SELECT * FROM Watchers")) {
            while (rs.next()) {
                Watcher watcher = new Watcher(database, rs.getLong("discordUser"), rs.getInt("itemID"));
                watcher.setLastInterval(rs.getLong("lastInterval"));
                watcher.setPriceLimit(rs.getDouble("priceLimit"));
                watcher.setWatcherType(rs.getInt("watcherType"));
                watcher.setWatchInterval(rs.getLong("watchInterval"));

                if(items.keySet().contains(watcher.getItemID())) {
                    watcher.setItem(items.get(watcher.getItemID()));
                }else{
                    new WebAPI().getItem(watcher.getItemID(), item -> items.put(watcher.getItemID(), item), Throwable::printStackTrace);
                    watcher.setItem(items.get(watcher.getItemID()));
                }

                this.watchers.add(watcher);
            }
        }catch (Exception e) {
            Logger.error(e.getMessage());
            BotUtils.reportException(e);
        }

        this.watchers.setListener(this);
        this.timer.scheduleAtFixedRate(this.thread, 0, 300000);
    }

    private void onTick(JDA jda) {
        for (Watcher watcher : this.watchers) {
            if(System.currentTimeMillis() - watcher.getLastInterval() > watcher.getWatchInterval()) {
                watcher.refresh((oldItem, newItem) -> {
                    User user = jda.getUserById(watcher.getDiscordUser());
                    if(user != null) {
                        boolean a = watcher.getWatcherType() == Watcher.WatcherType.CLASSIC;
                        boolean b = watcher.getWatcherType() == Watcher.WatcherType.WATCH_SELL && (newItem.getMarketBuy()/100.0) > watcher.getPriceLimit();
                        boolean c = watcher.getWatcherType() == Watcher.WatcherType.WATCH_BUY && (newItem.getMarketSell()/100.0) < watcher.getPriceLimit();
                        try {
                            DiscordUser discordUser = new DiscordUser(this.database, watcher.getDiscordUser());
                            if(!discordUser.isWatcherPaused() && (a || b || c)) {
                                user.openPrivateChannel().queue(privateChannel -> {
                                    privateChannel.sendMessage(this.getClassicWatcherEmbed(jda, oldItem, newItem).build()).queue();
                                });
                            }
                        } catch (Exception e) {
                            BotUtils.reportException(e);
                        }
                    }
                }, BotUtils::reportException);
                watcher.setLastInterval(System.currentTimeMillis());
                watcher.getSqlObject().update();
            }
        }
    }

    public Watcher find(Long userID, Integer itemID) {
        return this.watchers.stream().filter(watcher -> watcher.getDiscordUser().equals(userID) && watcher.getItemID().equals(itemID)).findAny().orElse(null);
    }

    public void removeWatcher(Long userID, Integer itemID) throws WatcherNotFoundException {
        Watcher watcher = this.find(userID, itemID);
        if(watcher != null) {
            this.watchers.remove(watcher);
        }else{
            throw new WatcherNotFoundException();
        }
    }

    public void addWatcher(Watcher watcher) {
        this.watchers.add(watcher);
    }

    public Collection<Watcher> getUserWatchers(User user) {
        return this.watchers.stream().filter(watcher -> watcher.getDiscordUser().equals(user.getIdLong())).collect(Collectors.toList());
    }

    public int getWatcherTotalCount() {
        return this.watchers.size();
    }

    public int getUserWatcherCount(User user) {
        return this.getUserWatchers(user).size();
    }


    private EmbedBuilder getClassicWatcherEmbed(JDA jda, CrossoutItem oldItem, CrossoutItem newItem) {

        double marketSellDiff = (newItem.getMarketSell() - oldItem.getMarketSell())/100;
        double marketBuyDiff = (newItem.getMarketBuy() - oldItem.getMarketBuy())/100;
        double craftSellDiff = (newItem.getCraftSell() - oldItem.getCraftSell())/100;
        double craftBuyDiff = (newItem.getCraftBuy() - oldItem.getCraftBuy())/100;
        String marketSellDiffStr = BotUtils.numberDifferenceFormat(marketSellDiff);
        String marketBuyDiffStr = BotUtils.numberDifferenceFormat(marketBuyDiff);
        String craftSellDiffStr = BotUtils.numberDifferenceFormat(craftSellDiff);
        String craftBuyDiffStr = BotUtils.numberDifferenceFormat(craftBuyDiff);


        EmbedBuilder builder = new EmbedBuilder();
        String currency = "%s Coins %s";

        builder.setAuthor("Price changes for " + newItem.getName(), "https://crossoutdb.com/item/" + newItem.getId(), jda.getSelfUser().getAvatarUrl());
        builder.setFooter("Run `cm:unwatch " + newItem.getName() + "` in a server where I am to stop this.", null);

        builder.addField("Buy it for :", String.format(currency, BotUtils.numberFormat(newItem.getMarketSell()/100), marketSellDiffStr), true);
        builder.addField("Sell it for :", String.format(currency, BotUtils.numberFormat(newItem.getMarketBuy()/100), marketBuyDiffStr), true);

        if(newItem.isCraftable()) {
            builder.addField("Buy Craft Items for :", String.format(currency, BotUtils.numberFormat(newItem.getCraftSell()/100), craftSellDiffStr), true);
            builder.addField("Sell Craft Items for :", String.format(currency, BotUtils.numberFormat(newItem.getCraftBuy()/100), craftBuyDiffStr), true);
        }

        builder.setThumbnail(newItem.getThumbnailImage());

        return builder;
    }
}

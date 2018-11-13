package ovh.akio.cmb.utils;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.User;
import ovh.akio.cmb.CrossoutMarketBot;
import ovh.akio.cmb.data.CrossoutItem;
import ovh.akio.cmb.data.WatchMemory;
import ovh.akio.cmb.throwables.WatcherNotFoundException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TimerTask;
import java.util.function.Consumer;

public class TimerWatch extends TimerTask {

    private CrossoutMarketBot bot;
    private ArrayList<WatchMemory> watchMemories = new ArrayList<>();
    private boolean firstLoad = true;

    public TimerWatch(CrossoutMarketBot bot, JDA jda) {
        this.bot = bot;
        HashMap<Integer, CrossoutItem> itemIds = new HashMap<>();
        ResultSet rs = this.bot.getDatabase().query("SELECT * FROM Watchers");
        try {
            while(rs.next()) {
                Long userID = rs.getLong("discordUser");
                User user = jda.getUserById(userID);
                int itemID = rs.getInt("itemID");
                if(user != null) {
                    if(itemIds.containsKey(itemID)) {
                        WatchMemory memory = new WatchMemory(itemIds.get(itemID), user);
                        this.watchMemories.add(memory);
                    }else{
                        new WebAPI().getItem(itemID, crossoutItem -> {
                            itemIds.put(itemID, crossoutItem);
                            WatchMemory memory = new WatchMemory(crossoutItem, user);
                            this.watchMemories.add(memory);
                        }, error -> {});
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        this.bot.getDatabase().close(rs);
    }

    @Override
    public void run() {
        if(firstLoad) {
            firstLoad = false;
            return;
        }

        HashMap<Long, Boolean> notifier = new HashMap<>();

        ResultSet rs = this.bot.getDatabase().query("SELECT discordID, watcherPaused FROM DiscordUser");

        try {
            while (rs.next()) {
                notifier.put(rs.getLong("discordID"), rs.getBoolean("watcherPaused"));
            }
        } catch (SQLException e) {
            BotUtils.reportException(e);
        }

        this.bot.getDatabase().close(rs);

        for (WatchMemory watchMemory : watchMemories) {
            watchMemory.refresh((oldItem, newItem) -> {
                        if(!notifier.get(watchMemory.getUser().getIdLong())) {
                            watchMemory.getUser().openPrivateChannel().queue(privateChannel ->
                                    privateChannel.sendMessage(getDiffEmbed(oldItem, newItem, privateChannel.getJDA()).build()).queue()
                            );
                        }
                    }
                    , (e -> {
                        BotUtils.reportException(e);
                        watchMemory.getUser().openPrivateChannel().queue(privateChannel ->
                                privateChannel.sendMessage("Sorry, but something went wrong while getting information about the item " + watchMemory.getItem().getName()).queue()
                        );
                    }));
        }
    }

    private EmbedBuilder getDiffEmbed(CrossoutItem oldItem, CrossoutItem newItem, JDA jda) {
        double marketSellDiff = (newItem.getMarketSell() - oldItem.getMarketSell())/100;
        double marketBuyDiff = (newItem.getMarketBuy() - oldItem.getMarketBuy())/100;
        double craftSellDiff = (newItem.getCraftSell() - oldItem.getCraftSell())/100;
        double craftBuyDiff = (newItem.getCraftBuy() - oldItem.getCraftBuy())/100;
        String marketSellDiffStr = BotUtils.numberDifferenceFormat(marketSellDiff);
        String marketBuyDiffStr = BotUtils.numberDifferenceFormat(marketBuyDiff);
        String craftSellDiffStr = BotUtils.numberDifferenceFormat(craftSellDiff);
        String craftBuyDiffStr = BotUtils.numberDifferenceFormat(craftBuyDiff);


        EmbedBuilder builder = new EmbedBuilder();
        String currency = "%s Gold %s";

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

    private WatchMemory findWatch(User user, CrossoutItem item) {
        for (WatchMemory watchMemory : this.watchMemories) {
            if(watchMemory.equals(user, item)) {
                return watchMemory;
            }
        }
        return null;
    }

    public void addWatch(WatchMemory memory, Consumer<Void> onSuccess) {
        this.watchMemories.add(memory);
        this.bot.getDatabase().execute("INSERT INTO Watchers VALUE (?, ?)", memory.getUser().getIdLong(), memory.getItem().getId());
        onSuccess.accept(null);
    }

    public void removeWatch(User user, CrossoutItem item, Consumer<Void> onSuccess, Consumer<Exception> onFailure) {
        WatchMemory memory = this.findWatch(user, item);
        if(memory == null) {
            onFailure.accept(new WatcherNotFoundException(item));
        }else{
            this.watchMemories.remove(memory);
            this.bot.getDatabase().execute("DELETE FROM Watchers WHERE discordUser = ? AND itemID = ?", memory.getUser().getIdLong(), memory.getItem().getId());
            onSuccess.accept(null);
        }
    }

    public ArrayList<WatchMemory> getWatchMemories(User user) {
        ArrayList<WatchMemory> watchMemories = new ArrayList<>();
        for (WatchMemory watchMemory : this.watchMemories) {
            if(watchMemory.getUser().equals(user)) watchMemories.add(watchMemory);
        }
        return watchMemories;
    }

    public int getWatcherCount() {
        return this.watchMemories.size();
    }

}

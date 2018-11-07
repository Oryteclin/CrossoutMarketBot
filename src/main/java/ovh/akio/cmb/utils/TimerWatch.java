package ovh.akio.cmb.utils;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.User;
import org.json.JSONArray;
import org.json.JSONObject;
import ovh.akio.cmb.data.CrossoutItem;
import ovh.akio.cmb.data.WatchMemory;
import ovh.akio.cmb.logging.Logger;
import ovh.akio.cmb.throwables.WatcherNotFoundException;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TimerTask;
import java.util.function.Consumer;

public class TimerWatch extends TimerTask {

    private ArrayList<WatchMemory> watchMemories = new ArrayList<>();
    private JSONObject watchers;
    private boolean firstLoad = true;

    public TimerWatch(JDA jda) {
        BotUtils.getFileContent(new File("data/watchers.json"), (watchers) -> {
            this.watchers = new JSONObject(watchers);

            HashMap<Integer, CrossoutItem> itemIds = new HashMap<>();

            for (String key : this.watchers.keySet()) {

                User user = jda.getUserById(key);
                if(user != null) {
                    JSONArray array = this.watchers.getJSONArray(key);
                    for (int i = 0; i < array.length(); i++) {
                        final int index = i;
                        if(!itemIds.containsKey(array.getInt(i))) {
                            new WebAPI().getItem(array.getInt(i), item -> {
                                itemIds.put(array.getInt(index), item);
                                this.watchMemories.add(new WatchMemory(item, user));
                            }, exception -> {});
                        }else{
                            this.watchMemories.add(new WatchMemory(itemIds.get(array.getInt(i)), user));
                        }
                    }

                }
            }

        }, (exception) -> {
            BotUtils.reportException(exception);
            Logger.fatal("Can't continue : unable to load watchers.");
            System.exit(-1);
        });
    }

    private void saveWatchers() {
        try (FileWriter file = new FileWriter("data/watchers.json")) {
            file.write(this.watchers.toString(2));
        } catch (Exception e) {
            BotUtils.reportException(e);
            Logger.error("Can't save watchers.json : " + e.getMessage());
        }
    }

    @Override
    public void run() {
        if(firstLoad) {
            firstLoad = false;
            return;
        }
        for (WatchMemory watchMemory : watchMemories) {
            watchMemory.refresh((oldItem, newItem) ->
                            watchMemory.getUser().openPrivateChannel().queue(privateChannel ->
                                    privateChannel.sendMessage(getDiffEmbed(oldItem, newItem, privateChannel.getJDA()).build()).queue()
                            )
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

        if (this.watchers.has(memory.getUser().getId())) {
            this.watchers.getJSONArray(memory.getUser().getId()).put(memory.getItem().getId());
        }else{
            this.watchers.put(memory.getUser().getId(), new JSONArray().put(memory.getItem().getId()));
        }

        onSuccess.accept(null);

        saveWatchers();
    }

    public void removeWatch(User user, CrossoutItem item, Consumer<Void> onSuccess, Consumer<Exception> onFailure) {
        WatchMemory memory = this.findWatch(user, item);

        if(memory == null) {
            onFailure.accept(new WatcherNotFoundException(item));
        }else{
            this.watchMemories.remove(memory);

            if(this.watchers.has(memory.getUser().getId())) {
                JSONArray array = this.watchers.getJSONArray(memory.getUser().getId());
                int indexRemove = -1;
                for (int i = 0; i < array.length(); i++) {
                    if(array.getInt(i) == memory.getItem().getId()) {
                        indexRemove = i;
                        break;
                    }
                }
                this.watchers.getJSONArray(memory.getUser().getId()).remove(indexRemove);
            }
            saveWatchers();
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

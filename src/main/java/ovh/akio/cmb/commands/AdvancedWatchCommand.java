package ovh.akio.cmb.commands;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import ovh.akio.cmb.CrossoutMarketBot;
import ovh.akio.cmb.data.CrossoutItem;
import ovh.akio.cmb.data.discord.Watcher;
import ovh.akio.cmb.impl.command.Command;
import ovh.akio.cmb.logging.Logger;
import ovh.akio.cmb.utils.BotUtils;
import ovh.akio.cmb.utils.WebAPI;

import java.awt.*;
import java.util.ArrayList;

public class AdvancedWatchCommand extends Command {

    public AdvancedWatchCommand(CrossoutMarketBot bot) {
        super(bot);
    }

    @Override
    public String getLabel() {
        return "advancedwatch";
    }

    @Override
    public String getDescription(GuildMessageReceivedEvent event) {
        return this.getTranslation(event, "Command.AdvancedWatch.Description");
    }

    @Override
    public ArrayList<String> getAliases() {
        ArrayList<String> aliases =  new ArrayList<>();
        aliases.add("aw");
        return aliases;
    }

    @Override
    public String getUsage() {
        return getLabel() + " <sell/buy> <price> <item name>";
    }

    @Override
    public void execute(GuildMessageReceivedEvent event) {
        String[] command = event.getMessage().getContentRaw().split(" ");

        if(command.length > 3) {
            event.getChannel().sendMessage(new EmbedBuilder().setDescription("Running in the 90's...").build()).queue(message -> {
                String watchType = command[1];
                Watcher.WatcherType watcherType;
                if (watchType.equalsIgnoreCase("sell")) {
                    watcherType = Watcher.WatcherType.WATCH_SELL;
                } else if (watchType.equalsIgnoreCase("buy")) {
                    watcherType = Watcher.WatcherType.WATCH_BUY;
                }else {
                    message.editMessage(
                            new EmbedBuilder()
                                    .setAuthor(this.getTranslation(event, "Command.Invite"), "https://discordapp.com/api/oauth2/authorize?client_id=500032551977746453&permissions=59456&scope=bot", event.getJDA().getSelfUser().getAvatarUrl())
                                    .setDescription(this.getTranslation(event, "Command.AdvancedWatch.WatcherType"))
                                    .setColor(Color.RED)
                                    .build()
                    ).queue();
                    return;
                }

                String priceStr = command[2];
                double price = 0.0;
                try {
                    price = Double.parseDouble(priceStr);
                } catch (NumberFormatException e) {
                    message.editMessage(
                            new EmbedBuilder()
                                    .setAuthor(this.getTranslation(event, "Command.Invite"), "https://discordapp.com/api/oauth2/authorize?client_id=500032551977746453&permissions=59456&scope=bot", event.getJDA().getSelfUser().getAvatarUrl())
                                    .setDescription(this.getTranslation(event, "Command.AdvancedWatch.NumberFormat"))
                                    .setColor(Color.RED)
                                    .build()
                    ).queue();
                    return;
                }
                final double finalPrice = price;
                String item = event.getMessage().getContentRaw().replace(command[0] + " " + watchType + " " + priceStr + "", "").trim();

                new WebAPI().search(item, crossoutItems -> {
                    CrossoutItem crossoutItem = null;
                    if(crossoutItems.size() == 1) {
                        crossoutItem = crossoutItems.get(0);
                    }else if(crossoutItems.size() > 1) {
                        for (CrossoutItem crossoutItem1 : crossoutItems) {
                            if(crossoutItem1.getName().equalsIgnoreCase(item)) {
                                crossoutItem = crossoutItem1;
                            }
                        }
                        if(crossoutItem == null) {
                            message.editMessage(
                                    new EmbedBuilder()
                                            .setAuthor(this.getTranslation(event, "Command.Invite"), "https://discordapp.com/api/oauth2/authorize?client_id=500032551977746453&permissions=59456&scope=bot", event.getJDA().getSelfUser().getAvatarUrl())
                                            .setDescription(this.getTranslation(event, "Command.AdvancedWatch.MultipleFound"))
                                            .setColor(Color.RED)
                                            .build()
                            ).queue();
                            return;
                        }
                    }else{
                        message.editMessage(
                                new EmbedBuilder()
                                        .setAuthor(this.getTranslation(event, "Command.Invite"), "https://discordapp.com/api/oauth2/authorize?client_id=500032551977746453&permissions=59456&scope=bot", event.getJDA().getSelfUser().getAvatarUrl())
                                        .setDescription(this.getTranslation(event, "Command.AdvanceWatch.NoItemFound"))
                                        .setColor(Color.RED)
                                        .build()
                        ).queue();
                        return;
                    }

                    Watcher watcher = this.getBot().getWatcherManager().find(event.getAuthor().getIdLong(), crossoutItem.getId());
                    if(watcher != null) {
                        watcher.setWatcherType(watcherType);
                        watcher.setPriceLimit(finalPrice);
                        watcher.getSqlObject().update();

                        if(watcherType == Watcher.WatcherType.WATCH_SELL) {
                            message.editMessage(
                                    new EmbedBuilder()
                                            .setAuthor(this.getTranslation(event, "Command.Invite"), "https://discordapp.com/api/oauth2/authorize?client_id=500032551977746453&permissions=59456&scope=bot", event.getJDA().getSelfUser().getAvatarUrl())
                                            .setDescription(String.format(this.getTranslation(event, "Command.AdvancedWatch.Sell"), crossoutItem.getName(), BotUtils.numberFormat(finalPrice)))
                                            .setColor(Color.GREEN)
                                            .build()
                            ).queue();
                        }else{
                            message.editMessage(
                                    new EmbedBuilder()
                                            .setAuthor(this.getTranslation(event, "Command.Invite"), "https://discordapp.com/api/oauth2/authorize?client_id=500032551977746453&permissions=59456&scope=bot", event.getJDA().getSelfUser().getAvatarUrl())
                                            .setDescription(String.format(this.getTranslation(event, "Command.AdvancedWatch.Buy"), crossoutItem.getName(), BotUtils.numberFormat(finalPrice)))
                                            .setColor(Color.GREEN)
                                            .build()
                            ).queue();
                        }
                    }else{
                        message.editMessage(
                                new EmbedBuilder()
                                        .setAuthor(this.getTranslation(event, "Command.Invite"), "https://discordapp.com/api/oauth2/authorize?client_id=500032551977746453&permissions=59456&scope=bot", event.getJDA().getSelfUser().getAvatarUrl())
                                        .setDescription(this.getTranslation(event, "Command.AdvancedWatch.NotFound"))
                                        .setColor(Color.RED)
                                        .build()
                        ).queue();
                    }
                }, (error) -> {
                    BotUtils.reportException(error);
                    message.editMessage(
                            new EmbedBuilder()
                                    .setAuthor(this.getTranslation(event, "Command.Invite"), "https://discordapp.com/api/oauth2/authorize?client_id=500032551977746453&permissions=59456&scope=bot", event.getJDA().getSelfUser().getAvatarUrl())
                                    .setDescription(String.format(this.getTranslation(event, "Command.AdvancedWatch.CantProcess"), error.getMessage()))
                                    .setColor(Color.RED)
                                    .build()
                    ).queue();
                });
            });
        }
    }
}

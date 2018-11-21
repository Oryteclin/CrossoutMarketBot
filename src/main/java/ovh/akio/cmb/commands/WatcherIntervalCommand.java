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
import java.util.Arrays;

public class WatcherIntervalCommand extends Command {

    public WatcherIntervalCommand(CrossoutMarketBot bot) {
        super(bot);
    }

    @Override
    public String getLabel() {
        return "watcherinterval";
    }

    @Override
    public String getDescription(GuildMessageReceivedEvent event) {
        return this.getTranslation(event, "Command.WatcherIntervalCommand.Description");
    }

    @Override
    public ArrayList<String> getAliases() {
        ArrayList<String> aliases =  new ArrayList<>();
        aliases.add("wi");
        return aliases;
    }

    @Override
    public String getUsage() {
        return getLabel() + " <interval in seconds> <item name>";
    }

    @Override
    public void execute(GuildMessageReceivedEvent event) {

        String[] command = event.getMessage().getContentRaw().split(" ");

        if(command.length > 2) {
            event.getChannel().sendMessage(new EmbedBuilder().setDescription("Running in the 90's...").build()).queue(message -> {
                String time = command[1];
                int timeInt = 0;
                try {
                    timeInt = Integer.parseInt(time);
                } catch (NumberFormatException e) {
                    message.editMessage(
                            new EmbedBuilder()
                                    .setAuthor(this.getTranslation(event, "Command.Invite"), "https://discordapp.com/api/oauth2/authorize?client_id=500032551977746453&permissions=59456&scope=bot", event.getJDA().getSelfUser().getAvatarUrl())
                                    .setDescription(this.getTranslation(event, "Command.WatcherInterval.NumberFormat"))
                                    .setColor(Color.RED)
                                    .build()
                    ).queue();
                    return;
                }
                final int finalTime = timeInt;
                String item = event.getMessage().getContentRaw().replace(command[0] + " " + time + "", "").trim();

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
                                            .setDescription(this.getTranslation(event, "Command.WatcherInterval.MultipleFound"))
                                            .setColor(Color.RED)
                                            .build()
                            ).queue();
                            return;
                        }
                    }else{
                        message.editMessage(
                                new EmbedBuilder()
                                        .setAuthor(this.getTranslation(event, "Command.Invite"), "https://discordapp.com/api/oauth2/authorize?client_id=500032551977746453&permissions=59456&scope=bot", event.getJDA().getSelfUser().getAvatarUrl())
                                        .setDescription(this.getTranslation(event, "Command.WatcherInterval.NoItemFound"))
                                        .setColor(Color.RED)
                                        .build()
                        ).queue();
                        return;
                    }

                    Logger.debug("Manage");
                    Watcher watcher = this.getBot().getWatcherManager().find(event.getAuthor().getIdLong(), crossoutItem.getId());
                    if(watcher != null) {
                        watcher.setWatchInterval(finalTime * 1000L);
                        watcher.getSqlObject().update();
                        message.editMessage(
                                new EmbedBuilder()
                                        .setAuthor(this.getTranslation(event, "Command.Invite"), "https://discordapp.com/api/oauth2/authorize?client_id=500032551977746453&permissions=59456&scope=bot", event.getJDA().getSelfUser().getAvatarUrl())
                                        .setDescription(String.format(this.getTranslation(event, "Command.WatcherInterval.Edited"), crossoutItem.getName(), finalTime))
                                        .setColor(Color.GREEN)
                                        .build()
                        ).queue();
                    }else{
                        message.editMessage(
                                new EmbedBuilder()
                                        .setAuthor(this.getTranslation(event, "Command.Invite"), "https://discordapp.com/api/oauth2/authorize?client_id=500032551977746453&permissions=59456&scope=bot", event.getJDA().getSelfUser().getAvatarUrl())
                                        .setDescription(this.getTranslation(event, "Command.WatcherInterval.NotFound"))
                                        .setColor(Color.RED)
                                        .build()
                        ).queue();
                    }
                }, (error) -> {
                    BotUtils.reportException(error);
                    message.editMessage(
                            new EmbedBuilder()
                                    .setAuthor(this.getTranslation(event, "Command.Invite"), "https://discordapp.com/api/oauth2/authorize?client_id=500032551977746453&permissions=59456&scope=bot", event.getJDA().getSelfUser().getAvatarUrl())
                                    .setDescription(String.format(this.getTranslation(event, "Command.WatcherInterval.CantProcess"), error.getMessage()))
                                    .setColor(Color.RED)
                                    .build()
                    ).queue();
                });
            });
        }
    }
}

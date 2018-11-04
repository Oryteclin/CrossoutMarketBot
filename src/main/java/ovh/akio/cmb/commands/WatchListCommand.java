package ovh.akio.cmb.commands;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import ovh.akio.cmb.CrossoutMarketBot;
import ovh.akio.cmb.data.WatchMemory;
import ovh.akio.cmb.impl.command.Command;
import ovh.akio.cmb.impl.embed.EmbedItem;
import ovh.akio.cmb.impl.embed.EmbedPage;
import ovh.akio.cmb.utils.TimerWatch;

import java.awt.*;
import java.util.ArrayList;

public class WatchListCommand extends Command {

    public WatchListCommand(CrossoutMarketBot bot) {
        super(bot);
    }

    @Override
    public String getLabel() {
        return "watchlist";
    }

    @Override
    public String getDescription() {
        return "Get the list of your watchers.";
    }

    @Override
    public ArrayList<String> getAliases() {
        return new ArrayList<>();
    }

    @Override
    public String getUsage() {
        return getLabel();
    }

    @Override
    public void execute(GuildMessageReceivedEvent event) {

        event.getChannel().sendMessage(new EmbedBuilder().setDescription("Running in the 90's...").build()).queue(message -> {
            TimerWatch timerWatch = this.getBot().getTimerWatch();
            ArrayList<EmbedItem> items = new ArrayList<>();

            for (WatchMemory watchMemory : timerWatch.getWatchMemories(event.getAuthor())) {
                items.add(watchMemory.getItem());
            }

            if(items.size() == 0) {
                message.editMessage(new EmbedBuilder().setAuthor("Click here to invite the bot.", "https://discordapp.com/api/oauth2/authorize?client_id=500032551977746453&permissions=59456&scope=bot", event.getJDA().getSelfUser().getAvatarUrl()).setDescription("You haven't any watcher.").setColor(Color.ORANGE).build()).queue();
            }else{
                new EmbedPage(message, items, 10) {
                    @Override
                    public EmbedBuilder getEmbed() {
                        EmbedBuilder builder = new EmbedBuilder();
                        builder.setTitle("Your watchers");
                        builder.setAuthor("Click here to invite the bot.", "https://discordapp.com/api/oauth2/authorize?client_id=500032551977746453&permissions=59456&scope=bot", event.getJDA().getSelfUser().getAvatarUrl());
                        builder.setColor(Color.white);
                        return builder;
                    }
                };
            }

        });

    }
}

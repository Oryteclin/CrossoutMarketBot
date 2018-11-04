package ovh.akio.cmb.commands;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import ovh.akio.cmb.CrossoutMarketBot;
import ovh.akio.cmb.impl.command.Command;

import java.awt.*;
import java.util.ArrayList;

public class StatsCommand extends Command {

    public StatsCommand(CrossoutMarketBot bot) {
        super(bot);
    }

    @Override
    public String getLabel() {
        return "stats";
    }

    @Override
    public String getDescription() {
        return "Get the bot stats";
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

        EmbedBuilder builder = new EmbedBuilder();

        builder.setAuthor("Click here to invite the bot.", "https://discordapp.com/api/oauth2/authorize?client_id=500032551977746453&permissions=59456&scope=bot", event.getJDA().getSelfUser().getAvatarUrl());
        builder.setDescription("Here is some cool stats !");
        builder.setColor(Color.CYAN);

        builder.addField("Uptime", this.getBot().getUptime(), false);
        builder.addField("Watchers", this.getBot().getTimerWatch().getWatcherCount() + "", false);
        builder.addField("Discord Servers", event.getJDA().getGuilds().size() + "", false);
        builder.addField("Commands executed here", this.getBot().getGuildCommandCount(event.getGuild()) + "", false);
        builder.addField("Commands executed", this.getBot().getGlobalCommandCount() + "", false);

        event.getChannel().sendMessage(builder.build()).queue();

    }
}

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
    public String getDescription(GuildMessageReceivedEvent event) {
        return this.getTranslation(event, "Command.Stats.Description");
    }

    @Override
    public ArrayList<String> getAliases() {
        ArrayList<String> aliases =  new ArrayList<>();
        aliases.add("s");
        return aliases;
    }

    @Override
    public String getUsage() {
        return getLabel();
    }

    @Override
    public void execute(GuildMessageReceivedEvent event) {

        EmbedBuilder builder = new EmbedBuilder();

        builder.setAuthor(this.getTranslation(event, "Command.Invite"), "https://discordapp.com/api/oauth2/authorize?client_id=500032551977746453&permissions=59456&scope=bot", event.getJDA().getSelfUser().getAvatarUrl());
        builder.setDescription(this.getTranslation(event, "Command.Stats.Description"));
        builder.setColor(Color.CYAN);

        builder.addField(this.getTranslation(event, "Command.Stats.Uptime"), this.getBot().getUptime(), false);
        builder.addField(this.getTranslation(event, "Command.Stats.Watchers"), this.getBot().getWatcherManager().getWatcherTotalCount() + "", false);
        builder.addField(this.getTranslation(event, "Command.Stats.Guilds"), event.getJDA().getGuilds().size() + "", false);
        builder.addField(this.getTranslation(event, "Command.Stats.CommandLocalExecute"), this.getBot().getGuildCommandCount(event.getGuild()) + "", false);
        builder.addField(this.getTranslation(event, "Command.Stats.CommandGlobalExecute"), this.getBot().getGlobalCommandCount() + "", false);

        event.getChannel().sendMessage(builder.build()).queue();

    }
}

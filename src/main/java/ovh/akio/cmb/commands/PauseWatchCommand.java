package ovh.akio.cmb.commands;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import ovh.akio.cmb.CrossoutMarketBot;
import ovh.akio.cmb.impl.command.Command;
import ovh.akio.cmb.utils.BotUtils;

import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class PauseWatchCommand extends Command {

    public PauseWatchCommand(CrossoutMarketBot bot) {
        super(bot);
    }

    @Override
    public String getLabel() {
        return "pausewatch";
    }

    @Override
    public String getDescription(GuildMessageReceivedEvent event) {
        return this.getTranslation(event, "Command.PauseWatch.Description");
    }

    @Override
    public ArrayList<String> getAliases() {
        ArrayList<String> aliases =  new ArrayList<>();
        aliases.add("pw");
        return aliases;
    }

    @Override
    public String getUsage() {
        return getLabel();
    }

    @Override
    public void execute(GuildMessageReceivedEvent event) {
        this.getBot().getDatabase().execute("UPDATE DiscordUser SET watcherPaused = !watcherPaused WHERE discordID = ?", event.getAuthor().getIdLong());

        ResultSet rs = this.getBot().getDatabase().query("SELECT watcherPaused FROM DiscordUser WHERE discordID = ?", event.getAuthor().getIdLong());

        try {
            if (rs.next()) {
                boolean paused = rs.getBoolean("watcherPaused");
                if (paused) {
                    event.getChannel().sendMessage(new EmbedBuilder()
                            .setColor(Color.ORANGE)
                            .setAuthor(this.getTranslation(event, "Command.Invite"), "https://discordapp.com/api/oauth2/authorize?client_id=500032551977746453&permissions=59456&scope=bot", event.getJDA().getSelfUser().getAvatarUrl())
                            .setDescription(this.getTranslation(event, "Command.PauseWatch.Paused")).build()).queue();
                }else {
                    event.getChannel().sendMessage(new EmbedBuilder()
                            .setColor(Color.GREEN)
                            .setAuthor(this.getTranslation(event, "Command.Invite"), "https://discordapp.com/api/oauth2/authorize?client_id=500032551977746453&permissions=59456&scope=bot", event.getJDA().getSelfUser().getAvatarUrl())
                            .setDescription(this.getTranslation(event, "Command.PauseWatch.Resumed")).build()).queue();
                }
            }

        } catch (SQLException e) {
            event.getChannel().sendMessage("Something went wrong and the dev has been notified.").queue();
            BotUtils.reportException(e);
        }

        this.getBot().getDatabase().close(rs);
    }
}

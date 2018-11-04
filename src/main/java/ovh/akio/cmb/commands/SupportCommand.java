package ovh.akio.cmb.commands;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import ovh.akio.cmb.CrossoutMarketBot;
import ovh.akio.cmb.impl.command.Command;

import java.util.ArrayList;

public class SupportCommand extends Command {

    public SupportCommand(CrossoutMarketBot bot) {
        super(bot);
    }

    @Override
    public String getLabel() {
        return "support";
    }

    @Override
    public String getDescription(GuildMessageReceivedEvent event) {
        return this.getTranslation(event, "Command.Support.Description");
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
        String officialSupportDiscord = "https://discord.gg/hxAx8yP";
        String officialForumTopic = "https://forum.crossout.net/index.php?/topic/295123-crossout-market-discord-bot/";
        String officialCrossoutDiscord = "https://discord.gg/RE6ar6y";
        String officialGitHub = "https://github.com/alexpado/CrossoutMarketBot";

        EmbedBuilder builder = new EmbedBuilder();

        builder.setAuthor(this.getTranslation(event, "Command.Invite"), "https://discordapp.com/api/oauth2/authorize?client_id=500032551977746453&permissions=59456&scope=bot", event.getJDA().getSelfUser().getAvatarUrl());
        builder.setDescription(this.getTranslation(event, "Command.Support.Embed.Description"));

        builder.addField(this.getTranslation(event, "Command.Support.Support"), officialSupportDiscord, true);
        builder.addField(this.getTranslation(event, "Command.Support.Forum"), officialForumTopic, true);
        builder.addField(this.getTranslation(event, "Command.Support.Discord"), officialCrossoutDiscord, true);
        builder.addField(this.getTranslation(event, "Command.Support.GitHub"), officialGitHub, true);

        event.getChannel().sendMessage(builder.build()).queue();

    }
}

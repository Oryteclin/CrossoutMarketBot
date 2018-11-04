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
    public String getDescription() {
        return "Want to get some help or ask something ?";
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

        builder.setAuthor("Click here to invite the bot.", "https://discordapp.com/api/oauth2/authorize?client_id=500032551977746453&permissions=59456&scope=bot", event.getJDA().getSelfUser().getAvatarUrl());
        builder.setDescription("Hey ! Need help, having a suggestion or want to report a bug ? Here is some useful links.");

        builder.addField("Crossout Market Support", officialSupportDiscord, true);
        builder.addField("Crossout Forum Topic", officialForumTopic, true);
        builder.addField("Crossout Official Discord", officialCrossoutDiscord, true);
        builder.addField("Bot GitHub", officialGitHub, true);

        event.getChannel().sendMessage(builder.build()).queue();

    }
}

package ovh.akio.cmb.commands;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import ovh.akio.cmb.CrossoutMarketBot;
import ovh.akio.cmb.impl.command.Command;
import ovh.akio.cmb.utils.BotUtils;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;

public class ChangelogCommand extends Command {

    public ChangelogCommand(CrossoutMarketBot bot) {
        super(bot);
    }

    @Override
    public String getLabel() {
        return "changelog";
    }

    @Override
    public String getDescription(GuildMessageReceivedEvent event) {
        return this.getTranslation(event, "Command.Changelog.Description");
    }

    @Override
    public ArrayList<String> getAliases() {
        ArrayList<String> aliases =  new ArrayList<>();
        aliases.add("cl");
        return aliases;
    }

    @Override
    public String getUsage() {
        return getLabel();
    }

    @Override
    public void execute(GuildMessageReceivedEvent event) {
        BotUtils.getFileContent(new File("data/changelog.txt"), (changelog) -> {

            EmbedBuilder builder = new EmbedBuilder();
            builder.setAuthor(this.getTranslation(event, "Command.Invite"), "https://discordapp.com/api/oauth2/authorize?client_id=500032551977746453&permissions=59456&scope=bot", event.getJDA().getSelfUser().getAvatarUrl());
            builder.setDescription(String.format("```%s```", changelog));
            builder.setColor(Color.white);
            event.getChannel().sendMessage(builder.build()).queue();

        }, BotUtils::reportException);
    }
}

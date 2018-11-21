package ovh.akio.cmb.commands;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import ovh.akio.cmb.CrossoutMarketBot;
import ovh.akio.cmb.impl.command.Command;

import java.awt.*;
import java.util.ArrayList;

public class HelpCommand extends Command {

    public HelpCommand(CrossoutMarketBot bot) {
        super(bot);
    }

    @Override
    public String getLabel() {
        return "help";
    }

    @Override
    public String getDescription(GuildMessageReceivedEvent event) {
        return this.getTranslation(event, "Command.Help.Description");
    }

    @Override
    public ArrayList<String> getAliases() {
        ArrayList<String> aliases =  new ArrayList<>();
        aliases.add("h");
        aliases.add("?");
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
        builder.setDescription(this.getTranslation(event, "Command.Help.Embed.Description"));
        builder.setColor(Color.white);

        for (Command command : this.getBot().getCommandManager().getCommands()) {
            builder.addField(command.getUsage(), command.getDescription(event), false);
        }

        event.getChannel().sendMessage(builder.build()).queue();

    }
}

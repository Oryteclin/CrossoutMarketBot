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
    public String getDescription() {
        return "Some helpful help :eyes:";
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
        builder.setDescription("Oh ? You need some help with the commands ? Got it !");
        builder.setColor(Color.white);

        for (Command command : this.getBot().getCommandManager().getCommands()) {
            builder.addField(command.getUsage(), command.getDescription(), false);
        }

        event.getChannel().sendMessage(builder.build()).queue();

    }
}

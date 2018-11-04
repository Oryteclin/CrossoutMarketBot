package ovh.akio.cmb.impl.command;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import ovh.akio.cmb.CrossoutMarketBot;

import java.util.ArrayList;

public abstract class Command {

    private CrossoutMarketBot bot;

    public Command(CrossoutMarketBot bot) {
        this.bot = bot;
    }

    public CrossoutMarketBot getBot() {
        return bot;
    }

    public abstract String getLabel();
    public abstract String getDescription();
    public abstract ArrayList<String> getAliases();
    public abstract String getUsage();

    public abstract void execute(GuildMessageReceivedEvent event);

}

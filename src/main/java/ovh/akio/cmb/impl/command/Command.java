package ovh.akio.cmb.impl.command;

import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import ovh.akio.cmb.CrossoutMarketBot;
import ovh.akio.cmb.utils.language.Translation;

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
    public abstract String getDescription(GuildMessageReceivedEvent event);
    public abstract ArrayList<String> getAliases();
    public abstract String getUsage();

    public String getTranslation(GuildMessageReceivedEvent event, String identifier) {
        return this.getBot().getLanguageManager().getTranslationForGuild(event.getGuild()).getString(identifier);
    }

    public Translation getTranslation(GuildMessageReceivedEvent event) {
        return this.getBot().getLanguageManager().getTranslationForGuild(event.getGuild());
    }

    public abstract void execute(GuildMessageReceivedEvent event);

}

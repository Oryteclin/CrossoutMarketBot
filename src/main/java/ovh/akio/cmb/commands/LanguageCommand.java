package ovh.akio.cmb.commands;

import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import ovh.akio.cmb.CrossoutMarketBot;
import ovh.akio.cmb.impl.command.Command;
import ovh.akio.cmb.utils.language.Language;

import java.util.ArrayList;

public class LanguageCommand extends Command {

    public LanguageCommand(CrossoutMarketBot bot) {
        super(bot);
    }

    @Override
    public String getLabel() {
        return "lang";
    }

    @Override
    public String getDescription(GuildMessageReceivedEvent event) {
        return this.getTranslation(event, "Command.Language.Description");
    }

    @Override
    public ArrayList<String> getAliases() {
        return new ArrayList<>();
    }

    @Override
    public String getUsage() {
        return getLabel() + " <language name>";
    }

    @Override
    public void execute(GuildMessageReceivedEvent event) {

        String label = event.getMessage().getContentRaw().split(" ")[0];
        String lang = event.getMessage().getContentRaw().replace(label, "").trim();

        try {
            Language language = Language.valueOf(lang);
            this.getBot().getLanguageManager().setTranslationForGuild(event.getGuild(), language);
            event.getChannel().sendMessage(this.getTranslation(event, "Command.Language.Switch")).queue();
        } catch (IllegalArgumentException e) {
            event.getChannel().sendMessage(this.getTranslation(event, "Command.Language.Unknown")).queue();
            e.printStackTrace();
        }

    }
}

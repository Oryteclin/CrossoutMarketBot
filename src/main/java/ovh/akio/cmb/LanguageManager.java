package ovh.akio.cmb;

import net.dv8tion.jda.core.entities.Guild;
import ovh.akio.cmb.data.discord.DiscordGuild;
import ovh.akio.cmb.logging.Logger;
import ovh.akio.cmb.utils.BotUtils;
import ovh.akio.cmb.utils.language.Translation;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class LanguageManager {

    private CrossoutMarketBot bot;
    private HashMap<String, Translation> translations = new HashMap<>();

    LanguageManager(CrossoutMarketBot bot) {
        this.bot = bot;
        this.loadTranslations();
    }

    void loadTranslations() {
        this.translations.clear();
        File languageFolder = new File("languages");
        File[] languages = languageFolder.listFiles();
        if(!languageFolder.exists() || languages == null) {
            Logger.error("Can't load any languages. The bot can't send message without English translation file.");
            System.exit(-1);
        }
        ArrayList<String> languagesName = new ArrayList<>();
        for (File language : languages) {
            String languageName = language.getName().replace(".lang", "");
            languagesName.add(languageName);
            this.translations.put(languageName, new Translation(this, languageName));
        }
        Logger.debug("Languages : " + Arrays.toString(languagesName.toArray()));
    }

    public Translation getTranslation(String language) {
        Translation translation = this.translations.get(language);
        if(translation == null) {
            File langFile = new File(String.format("languages/%s.lang", language));
            if(langFile.exists()) {
                Logger.debug("Lazy load language : " + language);
                // Load language at runtime. Allow to load new language without updating the bot.
                this.translations.put(language, new Translation(this, language));
            }
        }
        return this.translations.get(language);
    }

    public boolean translationExists(String language) {
        Translation translation = this.translations.get(language);
        if(translation == null) {
            File langFile = new File(String.format("languages/%s.lang", language));
            if(langFile.exists()) {
                Logger.debug("Lazy load language : " + language);
                // Load language at runtime. Allow to load new language without updating the bot.
                this.translations.put(language, new Translation(this, language));
                return true;
            }
        }else{
            return true;
        }
        return false;
    }

    public void setTranslationForGuild(Guild guild, String language) {
        try {
            DiscordGuild discordGuild = new DiscordGuild(this.bot.getDatabase(), guild.getIdLong());
            discordGuild.getSqlObject().select(null);
            discordGuild.setLanguage(language);
            discordGuild.getSqlObject().update();
        } catch (Exception e) {
            BotUtils.reportException(e);
        }
    }

    public Translation getTranslationForGuild(Guild guild) {
        try {
            DiscordGuild discordGuild = new DiscordGuild(this.bot.getDatabase(), guild.getIdLong());
            discordGuild.getSqlObject().select(null);
            return this.getTranslation(discordGuild.getLanguage());
        } catch (Exception e) {
            Logger.warn("Unable to load guild language setting. Using English by default.");
            return this.getTranslation("English");
        }
    }

}

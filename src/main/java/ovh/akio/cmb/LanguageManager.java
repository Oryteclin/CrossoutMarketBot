package ovh.akio.cmb;

import net.dv8tion.jda.core.entities.Guild;
import ovh.akio.cmb.logging.Logger;
import ovh.akio.cmb.utils.language.Translation;
import sun.rmi.runtime.Log;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
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
        for (File language : languages) {
            String languageName = language.getName().replace(".lang", "");
            this.translations.put(languageName, new Translation(this, languageName));
        }
    }

    public Translation getTranslation(String language) {
        Translation translation = this.translations.get(language);
        if(translation == null) {
            File langFile = new File(String.format("languages/%s.lang", language));
            if(langFile.exists()) {
                // Load language at runtime. Allow to load new language without updating the bot.
                this.translations.put(language, new Translation(this, language));
            }
        }
        return this.translations.get(language);
    }

    public boolean translationExists(String language) {
        return this.translations.keySet().contains(language);
    }

    public void setTranslationForGuild(Guild guild, String language) {
        this.bot.getDatabase().execute("UPDATE DiscordGuild SET language = ? WHERE discordID = ?", language, guild.getIdLong());
    }

    public Translation getTranslationForGuild(Guild guild) {
        ResultSet rs = this.bot.getDatabase().query("SELECT language FROM DiscordGuild WHERE discordID = ?", guild.getIdLong());
        try {
            String language;
            if(rs.next()) {
                language = rs.getString("language");
            }else{
                language = "English";
            }
            bot.getDatabase().close(rs);
            return this.getTranslation(language);
        } catch (SQLException e) {
            Logger.warn("Unable to load guild language setting. Using English by default.");
            this.bot.getDatabase().close(rs);
            return this.getTranslation("English");
        }
    }

}

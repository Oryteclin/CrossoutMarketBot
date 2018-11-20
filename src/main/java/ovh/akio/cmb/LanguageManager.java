package ovh.akio.cmb;

import net.dv8tion.jda.core.entities.Guild;
import ovh.akio.cmb.logging.Logger;
import ovh.akio.cmb.utils.language.Language;
import ovh.akio.cmb.utils.language.Translation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class LanguageManager {

    private CrossoutMarketBot bot;
    private HashMap<Language, Translation> translations = new HashMap<>();

    LanguageManager(CrossoutMarketBot bot) {
        this.bot = bot;
        this.translations.put(Language.English, new Translation(this, Language.English));
        this.translations.put(Language.French, new Translation(this, Language.French));
    }

    public Translation getTranslation(Language language) {
        return this.translations.get(language);
    }

    public void setTranslationForGuild(Guild guild, Language language) {
        this.bot.getDatabase().execute("UPDATE DiscordGuild SET language = ? WHERE discordID = ?", language.name(), guild.getIdLong());
    }

    public Translation getTranslationForGuild(Guild guild) {
        ResultSet rs = this.bot.getDatabase().query("SELECT language FROM DiscordGuild WHERE discordID = ?", guild.getIdLong());
        try {
            Language language;
            if(rs.next()) {
                language = Language.valueOf(rs.getString("language"));
            }else{
                language = Language.English;
            }
            bot.getDatabase().close(rs);
            return this.getTranslation(language);
        } catch (SQLException e) {
            Logger.warn("Unable to load guild language setting. Using English by default.");
            this.bot.getDatabase().close(rs);
            return this.getTranslation(Language.English);
        }
    }

}

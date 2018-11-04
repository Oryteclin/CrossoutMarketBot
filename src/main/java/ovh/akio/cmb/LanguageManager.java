package ovh.akio.cmb;

import net.dv8tion.jda.core.entities.Guild;
import org.json.JSONObject;
import ovh.akio.cmb.logging.Logger;
import ovh.akio.cmb.utils.BotUtils;
import ovh.akio.cmb.utils.language.Language;
import ovh.akio.cmb.utils.language.Translation;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;

public class LanguageManager {

    private JSONObject languageSettings;
    private HashMap<Language, Translation> translations = new HashMap<>();

    public LanguageManager() {

        BotUtils.getFileContent(new File("data/languages.json"), setting ->
                        this.languageSettings = new JSONObject(setting)
                , e -> {
                    BotUtils.reportException(e);
                    Logger.warn("Can't load language settings file.");
                });


        this.translations.put(Language.English, new Translation(Language.English));
        this.translations.put(Language.French, new Translation(Language.French));
    }

    public Translation getTranslation(Language language) {
        return this.translations.get(language);
    }

    public void setTranslationForGuild(Guild guild, Language language) {
        this.languageSettings.put(guild.getId(), language.name());
        saveSettings();
    }

    public Translation getTranslationForGuild(Guild guild) {
        if(this.languageSettings.has(guild.getId())) {
            return this.translations.get(Language.valueOf(this.languageSettings.getString(guild.getId())));
        }else{
            this.setTranslationForGuild(guild, Language.English);
            return this.translations.get(Language.English);
        }
    }

    private void saveSettings() {
        try (FileWriter file = new FileWriter("data/languages.json")) {
            file.write(this.languageSettings.toString(2));
        } catch (Exception e) {
            BotUtils.reportException(e);
            Logger.error("Can't save languages.json : " + e.getMessage());
        }
    }

}

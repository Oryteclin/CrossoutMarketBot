package ovh.akio.cmb.utils.language;

import ovh.akio.cmb.LanguageManager;
import ovh.akio.cmb.logging.Logger;
import ovh.akio.cmb.utils.BotUtils;

import java.io.File;
import java.util.HashMap;

public class Translation {

    private LanguageManager manager;
    private String language;
    private HashMap<String, String> texts = new HashMap<>();

    public Translation(LanguageManager languageManager, String language) {
        this.manager = languageManager;
        this.language = language;
        this.loadTexts();
    }

    private void loadTexts() {
        this.texts.clear();
        BotUtils.getFileContent(new File(String.format("languages/%s.lang", language)), (langText) -> {
            for (String line : langText.split("\n")) {
                if(line.length()>1) {
                    String[] splitLine = line.split("=");
                    this.texts.put(splitLine[0].trim(), splitLine[1].trim());
                }
            }
        }, e -> {
            Logger.error(String.format("Can't load %s translation file.", language));
            e.printStackTrace();
        });
    }

    public String getString(String identifier) {
        String translatedText = this.texts.getOrDefault(identifier, "");
        if(translatedText.equals("") && !this.language.equals("English")) {
            translatedText = this.manager.getTranslation("English").getString(identifier);
        }
        return translatedText;
    }


}

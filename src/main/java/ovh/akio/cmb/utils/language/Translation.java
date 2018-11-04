package ovh.akio.cmb.utils.language;

import ovh.akio.cmb.logging.Logger;
import ovh.akio.cmb.utils.BotUtils;

import java.io.File;
import java.util.HashMap;

public class Translation {

    private Language language;
    private HashMap<String, String> texts = new HashMap<>();

    public Translation(Language language) {
        this.language = language;
        this.loadTexts();
    }

    public void loadTexts() {
        this.texts.clear();
        BotUtils.getFileContent(new File(String.format("languages/%s.lang", language.name())), (langText) -> {
            for (String line : langText.split("\n")) {
                if(line.length()>1) {
                    String[] splitLine = line.split("=");
                    this.texts.put(splitLine[0].trim(), splitLine[1].trim());
                }
            }
        }, e -> {
            Logger.error(String.format("Can't load %s translation file.", language.name()));
            e.printStackTrace();
        });
    }

    public String getString(String identifier) {
        return this.texts.getOrDefault(identifier, "");
    }


}

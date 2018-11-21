package ovh.akio.cmb.logging;

public class Logger {

    public enum Level {
        Debug, Info, Warning, Error, Fatal
    }

    private static boolean showInfo = true;
    private static boolean showDebug = true;
    private static boolean showWarning = true;
    private static boolean showError = true;
    private static boolean showFatal = true;

    private static void prettyPrint(String str, Console.ForegroundColor foregroundColor, Console.BackgroundColor backgroundColor) {

        int charLimit = 140;
        int charLabel = 13;

        Console.resetColors();
        if(foregroundColor!=null) Console.setForegroundColor(foregroundColor);
        if(backgroundColor!=null) Console.setBackgroundColor(backgroundColor);
        int charPosition = 1;
        Console.write(" ");
        String[] words = str.split(" ");
        for (String word : words) {
            if(charPosition + word.length() > charLimit){

                while(charPosition < charLimit) {
                    Console.write(" ");
                    charPosition++;
                }

                Console.resetColors();
                Console.writeWithNewLine("");
                for(int i = 0 ; i <= charLabel-1 ; i++) {
                    Console.write(".");
                }
                if(foregroundColor!=null) Console.setForegroundColor(foregroundColor);
                if(backgroundColor!=null) Console.setBackgroundColor(backgroundColor);
                Console.write(" " + word + " ");
                charPosition = word.length()+2;
            }else if(charPosition + word.length() == charLimit) {
                Console.write(word);
                charPosition += word.length();
            }else {
                Console.write(word + " ");
                charPosition += word.length() + 1;
            }
        }
        while(charPosition < charLimit) {
            Console.write(" ");
            charPosition++;
        }
        Console.resetColors();
        Console.writeWithNewLine("");
    }

    public static void info(String str) {
        if(!showInfo) return;
        Console.setBackgroundColor(Console.BackgroundColor.CYAN);
        Console.setForegroundColor(Console.ForegroundColor.WHITE);
        Console.write(" INFORMATION ");
        prettyPrint(str, Console.ForegroundColor.CYAN, null);
    }

    public static void debug(String str) {
        if(!showDebug) return;
        Console.setBackgroundColor(Console.BackgroundColor.BLUE);
        Console.setForegroundColor(Console.ForegroundColor.WHITE);
        Console.write("    DEBUG    ");
        prettyPrint(str, Console.ForegroundColor.BLUE, null);
    }

    public static void warn(String str) {
        if(!showWarning) return;
        Console.setBackgroundColor(Console.BackgroundColor.YELLOW);
        Console.setForegroundColor(Console.ForegroundColor.WHITE);
        Console.write("   WARNING   ");
        prettyPrint(str, Console.ForegroundColor.YELLOW, null);
    }

    public static void error(String str) {
        if(!showError) return;
        Console.setBackgroundColor(Console.BackgroundColor.RED);
        Console.setForegroundColor(Console.ForegroundColor.WHITE);
        Console.write("    ERROR    ");
        prettyPrint(str, Console.ForegroundColor.RED, null);
    }

    public static void fatal(String str) {
        if(!showFatal) return;
        Console.setBackgroundColor(Console.BackgroundColor.RED);
        Console.setForegroundColor(Console.ForegroundColor.WHITE);
        Console.write("    FATAL    ");
        prettyPrint(str, Console.ForegroundColor.RED, Console.BackgroundColor.WHITE);
    }

    public static void setShowInfo(boolean showInfo) {
        Logger.showInfo = showInfo;
    }

    public static void setShowDebug(boolean showDebug) {
        Logger.showDebug = showDebug;
    }

    public static void setShowWarning(boolean showWarning) {
        Logger.showWarning = showWarning;
    }

    public static void setShowError(boolean showError) {
        Logger.showError = showError;
    }

    public static void setShowFatal(boolean showFatal) {
        Logger.showFatal = showFatal;
    }

}

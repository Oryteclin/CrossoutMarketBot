package ovh.akio.cmb.logging;

/**
 * Only For GNUU/Linux OS
 */
public class Console {

    private static ForegroundColor foregroundColor;
    private static BackgroundColor backgroundColor;
    private static String ANSI_RESET = "\u001B[0m";

    public enum ForegroundColor {

        WHITE("\u001B[30m"),
        RED("\u001B[31m"),
        GREEN("\u001B[32m"),
        YELLOW("\u001B[33m"),
        BLUE("\u001B[34m"),
        PURPLE("\u001B[35m"),
        CYAN("\u001B[36m"),
        GREY("\u001B[37m");

        private String ansi;
        ForegroundColor(String ansi) {
            this.ansi = ansi;
        }

        public String getAnsi() {
            return ansi;
        }
    }

    public enum BackgroundColor {

        WHITE("\u001B[40m"),
        RED("\u001B[41m"),
        GREEN("\u001B[42m"),
        YELLOW("\u001B[43m"),
        BLUE("\u001B[44m"),
        PURPLE("\u001B[45m"),
        CYAN("\u001B[46m"),
        GREY("\u001B[47m");

        private String ansi;
        BackgroundColor(String ansi) {
            this.ansi = ansi;
        }

        public String getAnsi() {
            return ansi;
        }
    }


    public static void setForegroundColor(ForegroundColor foregroundColor) {
        Console.foregroundColor = foregroundColor;
    }

    public static void setBackgroundColor(BackgroundColor backgroundColor) {
        Console.backgroundColor = backgroundColor;
    }

    public static void write(Object out) {
        if(foregroundColor != null) System.out.print(foregroundColor.getAnsi());
        if(backgroundColor != null) System.out.print(backgroundColor.getAnsi());
        System.out.print(out);
    }

    public static void write(Object out, boolean newline) {
        write(out);
        if(newline) System.out.println();
    }

    public static void writeLine(Object out){
        write(out, true);
    }

    public static void resetColors() {
        foregroundColor = null;
        backgroundColor = null;
        write(ANSI_RESET);
    }
}

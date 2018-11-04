package ovh.akio.cmb.logging;

/**
 * Only For GNUU/Linux OS
 */
class Console {

    private static ForegroundColor foregroundColor;
    private static BackgroundColor backgroundColor;

    public enum ForegroundColor {

        WHITE("\u001B[30m"),
        RED("\u001B[31m"),
        YELLOW("\u001B[33m"),
        BLUE("\u001B[34m"),
        CYAN("\u001B[36m");

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
        YELLOW("\u001B[43m"),
        BLUE("\u001B[44m"),
        CYAN("\u001B[46m");

        private String ansi;
        BackgroundColor(String ansi) {
            this.ansi = ansi;
        }

        public String getAnsi() {
            return ansi;
        }
    }


    static void setForegroundColor(ForegroundColor foregroundColor) {
        Console.foregroundColor = foregroundColor;
    }

    static void setBackgroundColor(BackgroundColor backgroundColor) {
        Console.backgroundColor = backgroundColor;
    }

    static void write(Object out) {
        if(foregroundColor != null) System.out.print(foregroundColor.getAnsi());
        if(backgroundColor != null) System.out.print(backgroundColor.getAnsi());
        System.out.print(out);
    }

    static void writeWithNewLine(Object out) {
        write(out);
        System.out.println();
    }

    static void resetColors() {
        foregroundColor = null;
        backgroundColor = null;
        write("\u001B[0m");
    }
}

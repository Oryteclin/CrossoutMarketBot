package ovh.akio.cmb.utils;

public class Duration {

    private long duration;
    private long day;
    private long hour;
    private long minutes;
    private long seconds;

    public Duration(Long duration) {

        this.duration = duration;

        day = duration / 86400000;
        long _d = day * 86400000;

        hour = (duration-_d) / 3600000;
        long _h = hour * 3600000;

        minutes = (duration-_h) / 60000;
        long _m = minutes * 60000;

        seconds = (duration-_h-_m) / 1000;
    }

    public long getDuration() {
        return duration;
    }

    public String toString() {

        StringBuilder builder = new StringBuilder();

        if(day > 0) {
            builder.append(day).append("d");
        }

        if(hour > 0 || builder.toString().contains("d")) {
            if(hour < 10) {
                builder.append("0").append(hour).append("h");
            }else{
                builder.append(hour).append("h");
            }
        }

        if(minutes > 0 || builder.toString().contains("h")) {
            if(minutes < 10) {
                builder.append("0").append(minutes).append("m");
            }else{
                builder.append(minutes).append("m");
            }
        }

        if(seconds > 0 || builder.toString().contains("m")) {
            if(seconds < 10) {
                builder.append("0").append(seconds).append("s");
            }else{
                builder.append(seconds).append("s");
            }
        }

        return builder.toString();
    }

}

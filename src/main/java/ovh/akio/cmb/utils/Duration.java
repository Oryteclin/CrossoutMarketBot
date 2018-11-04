package ovh.akio.cmb.utils;

public class Duration {

    private long duration;
    private long hour;
    private long minutes;
    private long seconds;

    public Duration(Long duration) {

        this.duration = duration;

        hour = duration / 3600000;
        long _h = hour * 3600000;

        minutes = (duration-_h) / (60000);
        long _m = minutes * 60000;

        seconds = (duration-_h-_m) / 1000;
    }

    public long getDuration() {
        return duration;
    }

    public String toString() {

        String h, m, s;

        if(hour < 10) {
            h = "0" + hour;
        }else{
            h = "" + hour;
        }

        if(minutes < 10) {
            m = "0" + minutes;
        }else{
            m = "" + minutes;
        }

        if(seconds < 10) {
            s = "0" + seconds;
        }else{
            s = "" + seconds;
        }

        return h + "h" + m + "m" + s + "s";
    }

}

package ovh.akio.cmb.throwables;

import ovh.akio.cmb.data.CrossoutItem;

public class WatcherNotFoundException extends Exception {

    private CrossoutItem item;

    public WatcherNotFoundException(CrossoutItem item) {
        this.item = item;
    }

    public CrossoutItem getItem() {
        return item;
    }
}

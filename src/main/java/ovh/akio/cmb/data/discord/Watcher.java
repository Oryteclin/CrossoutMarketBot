package ovh.akio.cmb.data.discord;

import fr.alexpado.database.Database;
import fr.alexpado.database.annotations.Column;
import fr.alexpado.database.annotations.Table;
import fr.alexpado.database.utils.SQLColumnType;
import fr.alexpado.database.utils.SQLObject;
import ovh.akio.cmb.data.CrossoutItem;
import ovh.akio.cmb.utils.WebAPI;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Table(name = "Watchers")
public class Watcher {

    public enum WatcherType {
        CLASSIC(0), WATCH_SELL(1), WATCH_BUY(2);

        public int type;
        WatcherType(int type) {
            this.type = type;
        }

        public static WatcherType fromType(int type) {
            switch (type) {
                case 0:
                    return CLASSIC;
                case 1:
                    return WATCH_SELL;
                case 2:
                    return WATCH_BUY;
            }
            return null;
        }
    }

    private SQLObject sqlObject;

    private CrossoutItem item;

    @Column(name = "discordUser", columnType = SQLColumnType.BIGINT, primaryKey = true)
    private Long discordUser;

    @Column(name = "itemID", columnType = SQLColumnType.INT, primaryKey = true)
    private Integer itemID;

    @Column(name = "watcherType", columnType = SQLColumnType.INT)
    private Integer watcherType = WatcherType.CLASSIC.type;

    @Column(name = "watchInterval", columnType = SQLColumnType.BIGINT)
    private Long watchInterval = 1800000L;

    @Column(name = "lastInterval", columnType = SQLColumnType.BIGINT)
    private Long lastInterval = System.currentTimeMillis() - 10;

    @Column(name = "priceLimit", columnType = SQLColumnType.DOUBLE)
    private Double priceLimit = 0.0;

    public Watcher(Database database) throws Exception {
        this.sqlObject = new SQLObject(database, this);
    }

    public Watcher(Database database, Long discordUser, Integer itemID) throws Exception {
        this.sqlObject = new SQLObject(database, this);
        this.discordUser = discordUser;
        this.itemID = itemID;
    }

    public SQLObject getSqlObject() {
        return sqlObject;
    }

    public Long getDiscordUser() {
        return discordUser;
    }

    public Integer getItemID() {
        return itemID;
    }

    public WatcherType getWatcherType() {
        return WatcherType.fromType(watcherType);
    }

    public Long getWatchInterval() {
        return watchInterval;
    }

    public Long getLastInterval() {
        return lastInterval;
    }

    public Double getPriceLimit() {
        return priceLimit;
    }

    public void setWatcherType(WatcherType watcherType) {
        this.watcherType = watcherType.type;
    }

    public void setWatcherType(Integer watcherType) {
        this.watcherType = watcherType;
    }

    public void setWatchInterval(Long watchInterval) {
        this.watchInterval = watchInterval;
    }

    public void setLastInterval(Long lastInterval) {
        this.lastInterval = lastInterval;
    }

    public void setPriceLimit(Double priceLimit) {
        this.priceLimit = priceLimit;
    }

    public CrossoutItem getItem() {
        return item;
    }

    public void setItem(CrossoutItem item) {
        this.item = item;
    }

    public void refresh(BiConsumer<CrossoutItem, CrossoutItem> onSuccess, Consumer<Exception> onFailure) {
        new WebAPI().getItem(this.getItemID(), item -> {
            onSuccess.accept(this.getItem(), item);
            this.setItem(item);
        }, onFailure);
    }

}

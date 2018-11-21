package ovh.akio.cmb.data.discord;

import fr.alexpado.database.Database;
import fr.alexpado.database.annotations.Column;
import fr.alexpado.database.annotations.Table;
import fr.alexpado.database.utils.SQLColumnType;
import fr.alexpado.database.utils.SQLObject;
import net.dv8tion.jda.core.entities.User;


@Table(name = "Users")
public class DiscordUser {

    private SQLObject sqlObject;

    @Column(name = "discordID", columnType = SQLColumnType.BIGINT, primaryKey = true)
    private Long discordID;

    @Column(name = "name", columnType = SQLColumnType.VARCHAR, additionnalData = "(191)")
    private String name;

    @Column(name = "avatar", columnType = SQLColumnType.VARCHAR, additionnalData = "(255)")
    private String avatar;

    @Column(name = "watcherPaused", columnType = SQLColumnType.BOOLEAN)
    private boolean watcherPaused;

    public DiscordUser(Database database) throws Exception {
        this.sqlObject = new SQLObject(database, this);
    }

    public DiscordUser(Database database, Long discordID) throws Exception {
        this.discordID = discordID;
        this.sqlObject = new SQLObject(database, this);
    }

    public DiscordUser(Database database, User user) throws Exception {
        this(database, user.getIdLong());
        this.setName(user.getName());
        this.setAvatar(user.getAvatarUrl());
    }

    public SQLObject getSqlObject() {
        return sqlObject;
    }

    public Long getDiscordID() {
        return discordID;
    }

    public String getName() {
        return name;
    }

    public String getAvatar() {
        return avatar;
    }

    public boolean isWatcherPaused() {
        return watcherPaused;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar == null ? "" : avatar;
    }

    public void setWatcherPaused(boolean watcherPaused) {
        this.watcherPaused = watcherPaused;
    }
}

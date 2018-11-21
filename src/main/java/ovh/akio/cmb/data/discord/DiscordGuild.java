package ovh.akio.cmb.data.discord;

import fr.alexpado.database.Database;
import fr.alexpado.database.annotations.Column;
import fr.alexpado.database.annotations.Table;
import fr.alexpado.database.utils.SQLColumnType;
import fr.alexpado.database.utils.SQLObject;
import net.dv8tion.jda.core.entities.Guild;

@Table(name = "Guilds")
public class DiscordGuild {

    private SQLObject sqlObject;

    @Column(name = "discordID", columnType = SQLColumnType.BIGINT, primaryKey = true)
    private Long discordID;

    @Column(name = "name", columnType = SQLColumnType.VARCHAR, additionnalData = "(191)")
    private String name;

    @Column(name = "icon", columnType = SQLColumnType.VARCHAR, additionnalData = "(255)")
    private String icon;

    @Column(name = "joinedDate", columnType = SQLColumnType.BIGINT)
    private Long joinedDate;

    @Column(name = "owner", columnType = SQLColumnType.BIGINT)
    private Long owner;

    @Column(name = "language", columnType = SQLColumnType.VARCHAR, additionnalData = "(20)")
    private String language;

    public DiscordGuild(Database database) throws Exception {
        this.sqlObject = new SQLObject(database, this);
    }

    public DiscordGuild(Database database, Long discordID) throws Exception {
        this.discordID = discordID;
        this.sqlObject = new SQLObject(database, this);
    }

    public DiscordGuild(Database database, Guild guild) throws Exception {
        this(database, guild.getIdLong());
        this.setName(guild.getName());
        this.setIcon(guild.getIconUrl());
        this.setJoinedDate(guild.getSelfMember().getJoinDate().toEpochSecond());
        this.setLanguage("English");
        this.setOwner(guild.getOwnerIdLong());
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

    public String getIcon() {
        return icon;
    }

    public Long getJoinedDate() {
        return joinedDate;
    }

    public Long getOwner() {
        return owner;
    }

    public String getLanguage() {
        return language;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setIcon(String icon) {
        this.icon = icon == null ? "" : icon;
    }

    public void setJoinedDate(Long joinedDate) {
        this.joinedDate = joinedDate;
    }

    public void setOwner(Long owner) {
        this.owner = owner;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}

package ovh.akio.cmb;

import fr.alexpado.database.Database;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.json.JSONObject;
import ovh.akio.cmb.data.discord.DiscordGuild;
import ovh.akio.cmb.data.discord.DiscordUser;
import ovh.akio.cmb.data.discord.Watcher;
import ovh.akio.cmb.logging.Logger;
import ovh.akio.cmb.utils.BotUtils;
import ovh.akio.cmb.utils.DatabaseUpdater;
import ovh.akio.cmb.utils.Duration;
import ovh.akio.cmb.watcher.WatcherManager;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.util.HashMap;
import java.util.Timer;

public class CrossoutMarketBot extends ListenerAdapter {

    private CrossoutMarketCommand commandManager;
    private boolean beta;
    private WatcherManager watcherManager;
    private Long startTime = System.currentTimeMillis();
    private LanguageManager languageManager;
    private Database database;

    public CrossoutMarketBot(boolean beta) {
        this.beta = beta;
        if(beta) Logger.info("Running on Beta Version");
        BotUtils.getFileContent(new File("data/configuration.json"),
                (content) -> startBot(new JSONObject(content)),
                (exception) -> Logger.error("Can't load configuration file. Exiting bot.")
        );
    }

    private void startBot(JSONObject object) {

        this.database = new Database("data/");

        JDABuilder builder = new JDABuilder(AccountType.BOT);

        String token;
        if(this.beta) {
            token = object.getJSONObject("token").getString("indev");
            this.commandManager = new CrossoutMarketCommand(this, "bcm:");
        }else{
            token = object.getJSONObject("token").getString("release");
            this.commandManager = new CrossoutMarketCommand(this, "cm:");
        }
        builder.setToken(token);

        builder.addEventListener(this);

        try {
            builder.build();
        } catch (LoginException e) {
            e.printStackTrace();
            System.exit(-1);
        }

    }

    public CrossoutMarketCommand getCommandManager() {
        return commandManager;
    }

    public String getUptime() {
        return new Duration(System.currentTimeMillis() - startTime).toString();
    }

    private void convertConfigurationFiles() {

        File watchers = new File("data/watchers.json");
        File language = new File("data/languages.json");

        if(watchers.exists()) {
            Logger.info("watchers.json found : Converting into MySQL Data...");
            BotUtils.getFileContent(watchers, content -> {
                JSONObject jsonContent = new JSONObject(content);
                for (String userID : jsonContent.keySet()) {
                    Long id = Long.parseLong(userID);
                    for (int i = 0; i < jsonContent.getJSONArray(userID).length(); i++) {
                        try {
                            Watcher watcher = new Watcher(this.database, id, jsonContent.getJSONArray(userID).getInt(i));
                            watcher.getSqlObject().insert();
                        } catch (Exception e) {
                            Logger.warn(e.getMessage());
                        }
                    }
                }
                boolean ignoreResult = watchers.delete();
            }, error -> {
                Logger.warn("Can't start convert. Keeping file for converting later.");
                Logger.error(error.getMessage());
            });
        }

        if(language.exists()) {
            Logger.info("languages.json found : Converting into MySQL Data...");
            BotUtils.getFileContent(language, content -> {
                JSONObject jsonContent = new JSONObject(content);
                for (String guildID : jsonContent.keySet()) {
                    try {
                        DiscordGuild guild = new DiscordGuild(this.database, Long.parseLong(guildID));
                        guild.getSqlObject().select(null);
                        guild.setLanguage(jsonContent.getString(guildID));
                        guild.getSqlObject().update();
                    } catch (Exception e) {
                        Logger.warn(e.getMessage());
                    }
                }
                boolean ignoreResult = language.delete();
            }, error -> {
                Logger.warn("Can't start convert. Keeping file for converting later.");
                Logger.error(error.getMessage());
            });
        }

    }

    private void createDatabaseStructure() {
        try {
            new DiscordGuild(this.database).getSqlObject().createTable();
            new DiscordUser(this.database).getSqlObject().createTable();
            new Watcher(this.database).getSqlObject().createTable();
        } catch (Exception e) {
            Logger.fatal(e.getMessage());
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public WatcherManager getWatcherManager() {
        return watcherManager;
    }

    @Override
    public void onReady(ReadyEvent event) {
        Guild myGuild = event.getJDA().getGuildById(508012982287073280L);
        TextChannel logs = myGuild.getTextChannelById(508020752994271242L);
        BotUtils.setReportChannel(logs);

        Logger.info("Loading languages...");
        languageManager = new LanguageManager(this);

        Logger.info("Registering listeners...");
        event.getJDA().addEventListener(new DatabaseUpdater(this));

        Logger.info("Updating database...");
        this.createDatabaseStructure();
        event.getJDA().getGuilds().forEach(guild ->
                this.getDatabase().execute("INSERT INTO Guilds (discordID, name, icon, joinedDate, owner, language) VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE name = VALUES(name), icon = VALUES(icon), owner = VALUES(owner)",
                        guild.getIdLong(), guild.getName(), guild.getIconUrl() == null ? "" : guild.getIconUrl(),
                        guild.getSelfMember().getJoinDate().toEpochSecond(), guild.getOwner().getUser().getIdLong(), "English")
        );
        this.convertConfigurationFiles();

        Logger.info("Loading Watch Service...");
        this.watcherManager = new WatcherManager(event.getJDA(), this.database);

        Logger.info("Bot ready !");
        event.getJDA().getPresence().setGame(Game.playing("marketbot in " + event.getJDA().getGuilds().size() + " servers."));
        super.onReady(event);
    }



    private HashMap<Long, Integer> commandsTrack = new HashMap<>();

    public int getGuildCommandCount(Guild guild) {
        return this.commandsTrack.getOrDefault(guild.getIdLong(), 0);
    }

    public int getGlobalCommandCount() {
        int cmdCount = 0;
        for (Integer value : this.commandsTrack.values()) {
            cmdCount += value;
        }
        return cmdCount;
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        if(!event.getAuthor().isBot() && event.getMessage().getContentRaw().startsWith(this.commandManager.getPrefix())) {

            User user = event.getAuthor();

            this.getDatabase().execute("INSERT INTO Users (discordID, name, avatar, watcherPaused) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE name = VALUES(name), avatar = VALUES(avatar)",
                    user.getIdLong(), user.getName(), user.getAvatarUrl() == null ? "" : user.getAvatarUrl(), false);

            if(event.getGuild() != null) {
                if (!BotUtils.checkPermission(event.getGuild(), event.getChannel(), Permission.MESSAGE_EMBED_LINKS)) {
                    event.getChannel().sendMessage(this.getLanguageManager().getTranslationForGuild(event.getGuild()).getString("Permission.MESSAGE_EMBED_LINK")).queue();
                    return;
                }
            }
            switch (this.commandManager.execute(event)) {
                case EXECUTED:
                    this.commandsTrack.put(event.getGuild().getIdLong(), this.getGuildCommandCount(event.getGuild())+1);
                    break;
                case NOTFOUND:
                    event.getChannel().sendMessage(this.getLanguageManager().getTranslationForGuild(event.getGuild()).getString("Command.NotFound")).queue();
                    break;
            }
        }
    }

    @Override
    public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
        if(event.getAuthor().getIdLong() == 149279150648066048L) {
            if(event.getMessage().getContentRaw().startsWith("setLogLevel:")) {
                String levelStr = event.getMessage().getContentRaw().replace("setLogLevel:", "");
                try {
                    Logger.Level level = Logger.Level.valueOf(levelStr);
                    BotUtils.setLogOutputLevel(level);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }else if(event.getMessage().getContentRaw().startsWith("reload:")) {
                String target = event.getMessage().getContentRaw().replace("reload:", "");
                if(target.equals("languages")) {
                    this.getLanguageManager().loadTranslations();
                }
            }
        }
    }

    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    public Database getDatabase() {
        return database;
    }

}

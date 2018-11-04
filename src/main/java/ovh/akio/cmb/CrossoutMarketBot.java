package ovh.akio.cmb;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.EventListener;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.json.JSONObject;
import ovh.akio.cmb.logging.Logger;
import ovh.akio.cmb.utils.BotUtils;
import ovh.akio.cmb.utils.Duration;
import ovh.akio.cmb.utils.TimerWatch;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.nio.channels.Channel;
import java.util.HashMap;
import java.util.Timer;

public class CrossoutMarketBot extends ListenerAdapter {

    private CrossoutMarketCommand commandManager;
    private boolean beta;
    private TimerWatch timerWatch;
    private Timer timer = new Timer();
    private Long startTime = System.currentTimeMillis();

    public CrossoutMarketBot(boolean beta) {
        this.beta = beta;
        if(beta) Logger.info("Running on Beta Version");
        BotUtils.getFileContent(new File("data/configuration.json"),
                (content) -> startBot(new JSONObject(content)),
                (exception) -> Logger.error("Can't load configuration file. Exiting bot.")
        );
    }

    public void startBot(JSONObject object) {

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

    public TimerWatch getTimerWatch() {
        return timerWatch;
    }

    public String getUptime() {
        return new Duration(System.currentTimeMillis() - startTime).toString();
    }

    @Override
    public void onReady(ReadyEvent event) {
        Guild myGuild = event.getJDA().getGuildById(508012982287073280L);
        TextChannel logs = myGuild.getTextChannelById(508020752994271242L);
        BotUtils.setReportChannel(logs);
        Logger.info("Loading Watch Service");
        this.timerWatch = new TimerWatch(event.getJDA());
        this.timer.scheduleAtFixedRate(this.timerWatch, 0L, 30 * 60 * 1000);
        Logger.info("Bot ready !");
        super.onReady(event);
    }

    public static boolean checkPermission(Guild guild, TextChannel channel, Permission permission) {
        boolean permissionAllowed;
        permissionAllowed = guild.getSelfMember().hasPermission(permission);

        int rolePower = 0;

        for (Role role : guild.getSelfMember().getRoles()) {
            if(role.getPosition() > rolePower) {
                rolePower = role.getPosition();
                for (Permission rolePermission : role.getPermissions()) {
                    if(rolePermission == permission) permissionAllowed = true;
                }
                PermissionOverride permissionOverride = channel.getPermissionOverride(role);

                if(permissionOverride != null) {
                    for (Permission rolePermission : channel.getPermissionOverride(role).getAllowed()) {
                        if(rolePermission == permission) permissionAllowed = true;
                    }

                    for (Permission rolePermission : channel.getPermissionOverride(role).getDenied()) {
                        if(rolePermission == permission) permissionAllowed = false;
                    }
                }
            }
        }
        PermissionOverride permissionOverride = channel.getPermissionOverride(guild.getSelfMember());
        if(permissionOverride != null) {
            for (Permission permission1 : permissionOverride.getAllowed()) {
                if (permission1 == permission) permissionAllowed = true;
            }

            for (Permission permission1 : permissionOverride.getDenied()) {
                if (permission1 == permission) permissionAllowed = false;
            }
        }
        return permissionAllowed;
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
            if(event.getGuild() != null) {
                if (!CrossoutMarketBot.checkPermission(event.getGuild(), event.getChannel(), Permission.MESSAGE_EMBED_LINKS)) {
                    event.getChannel().sendMessage("Whoops, I can't execute any command here. I need the permission `MESSAGE_EMBED_LINKS`.").queue();
                    return;
                }
            }
            switch (this.commandManager.execute(event)) {
                case EXECUTED:
                    this.commandsTrack.put(event.getGuild().getIdLong(), this.getGuildCommandCount(event.getGuild())+1);
                    break;
                case NOTFOUND:
                    event.getChannel().sendMessage("Huh, this command does not exists :cry: :cry:").queue();
                    break;
            }
        }
    }


    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        BotUtils.sendToLog("Hey ! I've joined the server `" + event.getGuild().getName() + "`");
    }

    @Override
    public void onGuildLeave(GuildLeaveEvent event) {
        BotUtils.sendToLog("Hey ! I've joined the server `" + event.getGuild().getName() + "`");
    }
}

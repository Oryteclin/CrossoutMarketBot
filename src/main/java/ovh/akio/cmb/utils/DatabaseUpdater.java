package ovh.akio.cmb.utils;

import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.guild.update.GuildUpdateIconEvent;
import net.dv8tion.jda.core.events.guild.update.GuildUpdateNameEvent;
import net.dv8tion.jda.core.events.guild.update.GuildUpdateOwnerEvent;
import net.dv8tion.jda.core.events.user.update.UserUpdateAvatarEvent;
import net.dv8tion.jda.core.events.user.update.UserUpdateNameEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import ovh.akio.cmb.CrossoutMarketBot;

public class DatabaseUpdater extends ListenerAdapter {

    private CrossoutMarketBot bot;

    public DatabaseUpdater(CrossoutMarketBot bot) {
        this.bot = bot;
    }

    @Override
    public void onUserUpdateName(UserUpdateNameEvent event) {
        this.bot.getDatabase().execute("UPDATE DiscordUser SET name = ? WHERE discordID = ?", event.getNewName(), event.getEntity().getIdLong());
    }

    @Override
    public void onUserUpdateAvatar(UserUpdateAvatarEvent event) {
        this.bot.getDatabase().execute("UPDATE DiscordUser SET avatar = ? WHERE discordID = ?", event.getNewAvatarUrl(), event.getEntity().getIdLong());
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {

        event.getGuild().getMembers().forEach(member -> {
            User user = member.getUser();
            this.bot.getDatabase().execute("INSERT IGNORE INTO DiscordUser VALUE (?, ?, ?, ?)",
                    user.getIdLong(), user.getName(),
                    user.getAvatarUrl() == null ? "" : user.getAvatarUrl(), false);
        });


        this.bot.getDatabase().execute("INSERT IGNORE INTO DiscordGuild VALUE (?, ?, ?, ?, ?, ?)",
                event.getGuild().getIdLong(), event.getGuild().getName(),
                event.getGuild().getIconUrl() == null ? "" : event.getGuild().getIconUrl(),
                event.getGuild().getSelfMember().getJoinDate().toEpochSecond(),
                event.getGuild().getOwner().getUser().getIdLong(), "English");

        BotUtils.sendToLog("Hey ! I've joined the server `" + event.getGuild().getName() + "`");
        event.getJDA().getPresence().setGame(Game.playing("marketbot in " + event.getJDA().getGuilds().size() + " servers."));
    }

    @Override
    public void onGuildLeave(GuildLeaveEvent event) {
        BotUtils.sendToLog("Hey ! I've left the server `" + event.getGuild().getName() + "`");
        event.getJDA().getPresence().setGame(Game.playing("marketbot in " + event.getJDA().getGuilds().size() + " servers."));
    }

    @Override
    public void onGuildUpdateIcon(GuildUpdateIconEvent event) {
        this.bot.getDatabase().execute("UPDATE DiscordGuild SET icon = ? WHERE discordID = ?", event.getNewIconUrl(), event.getEntity().getIdLong());
    }

    @Override
    public void onGuildUpdateName(GuildUpdateNameEvent event) {
        this.bot.getDatabase().execute("UPDATE DiscordGuild SET name = ? WHERE discordID = ?", event.getNewName(), event.getEntity().getIdLong());
    }

    @Override
    public void onGuildUpdateOwner(GuildUpdateOwnerEvent event) {
        this.bot.getDatabase().execute("UPDATE DiscordGuild SET oowner = ? WHERE discordID = ?", event.getNewOwner().getUser().getIdLong(), event.getEntity().getIdLong());
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        User user = event.getUser();
        this.bot.getDatabase().execute("INSERT IGNORE INTO DiscordUser VALUE (?, ?, ?, ?)",
                user.getIdLong(), user.getName(),
                user.getAvatarUrl() == null ? "" : user.getAvatarUrl(), false);
    }

}

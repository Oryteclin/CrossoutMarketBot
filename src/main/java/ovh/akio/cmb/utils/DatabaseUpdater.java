package ovh.akio.cmb.utils;

import net.dv8tion.jda.core.entities.Game;
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
import ovh.akio.cmb.data.discord.DiscordGuild;
import ovh.akio.cmb.data.discord.DiscordUser;

public class DatabaseUpdater extends ListenerAdapter {

    private CrossoutMarketBot bot;

    public DatabaseUpdater(CrossoutMarketBot bot) {
        this.bot = bot;
    }

    private void selectUserOrInsert(DiscordUser user) {
        user.getSqlObject().select((aVoid -> {
            user.setWatcherPaused(false);
            user.setAvatar(user.getAvatar());
            user.setName(user.getName());
            user.getSqlObject().insert();
        }));
    }

    @Override
    public void onUserUpdateName(UserUpdateNameEvent event) {
        try {
            DiscordUser user = new DiscordUser(this.bot.getDatabase(), event.getUser().getIdLong());
            this.selectUserOrInsert(user);
            user.setName(event.getNewName());
            user.getSqlObject().update();
        } catch (Exception e) {
            BotUtils.reportException(e);
        }
    }

    @Override
    public void onUserUpdateAvatar(UserUpdateAvatarEvent event) {
        try {
            DiscordUser user = new DiscordUser(this.bot.getDatabase(), event.getUser().getIdLong());
            this.selectUserOrInsert(user);
            user.setAvatar(event.getNewAvatarUrl());
            user.getSqlObject().update();
        } catch (Exception e) {
            BotUtils.reportException(e);
        }
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {

        try {
            DiscordGuild guild = new DiscordGuild(this.bot.getDatabase(), event.getGuild());
            guild.getSqlObject().insert();
        } catch (Exception e) {
            BotUtils.reportException(e);
        }

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
        try {
            DiscordGuild guild = new DiscordGuild(this.bot.getDatabase(), event.getGuild().getIdLong());
            guild.getSqlObject().select(null);
            guild.setIcon(event.getNewIconUrl());
            guild.getSqlObject().update();
        } catch (Exception e) {
            BotUtils.reportException(e);
        }
    }

    @Override
    public void onGuildUpdateName(GuildUpdateNameEvent event) {
        try {
            DiscordGuild guild = new DiscordGuild(this.bot.getDatabase(), event.getGuild().getIdLong());
            guild.getSqlObject().select(null);
            guild.setName(event.getNewName());
            guild.getSqlObject().update();
        } catch (Exception e) {
            BotUtils.reportException(e);
        }
    }

    @Override
    public void onGuildUpdateOwner(GuildUpdateOwnerEvent event) {
        try {
            DiscordGuild guild = new DiscordGuild(this.bot.getDatabase(), event.getGuild().getIdLong());
            guild.getSqlObject().select(null);
            guild.setOwner(event.getEntity().getOwnerIdLong());
            guild.getSqlObject().update();
        } catch (Exception e) {
            BotUtils.reportException(e);
        }
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        try {
            DiscordUser user = new DiscordUser(this.bot.getDatabase(), event.getUser());
            user.getSqlObject().insert();
        } catch (Exception e) {
            BotUtils.reportException(e);
        }
    }

}

package ovh.akio.cmb.utils;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.PermissionOverride;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.htmlparser.jericho.Renderer;
import net.htmlparser.jericho.Source;
import ovh.akio.cmb.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Consumer;

public class BotUtils {

    private static TextChannel textChannel;

    public static void setReportChannel(TextChannel channel) {
        textChannel = channel;
    }

    public static void reportException(Throwable throwable) {
        if(textChannel != null) {
            String[] stackTrace = getCustomStackTrace(throwable);
            for (String stack : stackTrace) {
                textChannel.sendMessage("```" + stack + "```").queue(message -> {
                    Logger.warn(String.format("An error has occurred and has been sent to the discord logs channel. MessageID : %s", message.getId()));
                }, e -> {
                    Logger.error("An error has occurred, but couldn't send it to the discord text channel. Printing stacktrace here.");
                    throwable.printStackTrace();
                });
            }
        }else{
            Logger.error("An error has occurred, but couldn't send it to the discord text channel. Printing stacktrace here.");
            throwable.printStackTrace();
        }
    }

    public static void sendToLog(String str) {
        if(textChannel != null) {
            textChannel.sendMessage(str).queue();
        }
    }

    private static String[] getCustomStackTrace(Throwable aThrowable) {
        final StringBuilder result = new StringBuilder();
        result.append(aThrowable.toString());
        final String NEW_LINE = System.getProperty("line.separator");
        result.append(NEW_LINE);

        for (StackTraceElement element : aThrowable.getStackTrace() ){
            result.append( element );
            result.append( NEW_LINE );
        }
        return result.toString().split("(?<=\\G.{1994})");
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

    public static int[] getPageBound(int size, int page, int count) {
        int startAt = (page-1) * count;
        if(startAt > size) return null;

        int endAt = startAt + count;
        if(endAt > size) endAt = size;
        return new int[]{startAt, endAt};
    }

    public static String removeHTML(String text) {
        Source htmlSource = new Source(text);
        Renderer htmlRend = new Renderer(htmlSource);
        return htmlRend.toString();
    }

    public static String numberFormat(double number) {
        return String.format("%,.2f", number);
    }

    public static String numberDifferenceFormat(double number) {
        if(number == 0) {
            return "(=)";
        }else if(number > 0) {
            return "(+" + numberFormat(number) + ")";
        }else if(number < 0) {
            return "(" + numberFormat(number) + ")";
        }
        return "";
    }

    public static String formatJSON(String json) {
        return removeHTML(json.replace("<br>", "\n"));
    }

    public static void getFileContent(File file, Consumer<String> onSuccess, Consumer<Exception> onFailure) {
        try {
            byte[] readAllBytes = Files.readAllBytes(Paths.get( file.getAbsolutePath() ));
            onSuccess.accept(new String(readAllBytes));
        } catch (IOException e) {
            onFailure.accept(e);
        }
    }

    public static void setLogOutputLevel(Logger.Level level) {
        switch (level) {
            case Debug:
                Logger.setShowDebug(true);
                Logger.setShowInfo(true);
                Logger.setShowWarning(true);
                Logger.setShowError(true);
                Logger.setShowFatal(true);
                break;
            case Info:
                Logger.setShowDebug(false);
                Logger.setShowInfo(true);
                Logger.setShowWarning(true);
                Logger.setShowError(true);
                Logger.setShowFatal(true);
                break;
            case Warning:
                Logger.setShowDebug(false);
                Logger.setShowInfo(false);
                Logger.setShowWarning(true);
                Logger.setShowError(true);
                Logger.setShowFatal(true);
                break;
            case Error:
                Logger.setShowDebug(false);
                Logger.setShowInfo(false);
                Logger.setShowWarning(false);
                Logger.setShowError(true);
                Logger.setShowFatal(true);
                break;
            case Fatal:
                Logger.setShowDebug(false);
                Logger.setShowInfo(false);
                Logger.setShowWarning(false);
                Logger.setShowError(false);
                Logger.setShowFatal(true);
                break;
        }
    }

}

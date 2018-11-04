package ovh.akio.cmb.impl.command;

import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import ovh.akio.cmb.CrossoutMarketBot;
import ovh.akio.cmb.logging.Logger;

import java.util.ArrayList;

public abstract class CommandManager {

    public enum ExecuteResponse {
        IGNORED, EXECUTED, NOTFOUND
    }


    private CrossoutMarketBot bot;
    private String prefix;

    private ArrayList<Command> commands = new ArrayList<>();

    public CommandManager(CrossoutMarketBot bot, String prefix) {
        this.bot = bot;
        this.prefix = prefix;
    }

    private boolean isCommandRegistered(String label) {
        return this.commands.stream().anyMatch(command -> command.getLabel().equals(label) || command.getAliases().contains(label));
    }

    private Command getCommand(String label) {
        return this.commands.stream().filter(command -> command.getLabel().equals(label) || command.getAliases().contains(label)).findFirst().get();
    }

    public void registerCommand(Command command) {
        if(!this.isCommandRegistered(command.getLabel())) {
            this.commands.add(command);
        }
    }

    public ExecuteResponse execute(GuildMessageReceivedEvent event) {
        String messageLabel = event.getMessage().getContentRaw().split(" ")[0].replace(this.prefix, "");

        if(event.getMessage().getContentRaw().startsWith(this.prefix)) {
            if(this.isCommandRegistered(messageLabel)) {
                if(event.getGuild() != null) {
                    Logger.debug("Command " + messageLabel + " executed on " + event.getGuild().getName() + " by " + event.getAuthor().getName());
                }else{
                    Logger.debug("Command " + messageLabel + " executed in DM by " + event.getAuthor().getName());
                }

                this.getCommand(messageLabel).execute(event);
                return ExecuteResponse.EXECUTED;
            }
            return ExecuteResponse.NOTFOUND;
        }
        return ExecuteResponse.IGNORED;
    }

    public ArrayList<Command> getCommands() {
        return commands;
    }

    public String getPrefix() {
        return prefix;
    }
}

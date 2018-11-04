package ovh.akio.cmb.impl.command;

import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import ovh.akio.cmb.CrossoutMarketBot;
import ovh.akio.cmb.logging.Logger;

import java.util.ArrayList;
import java.util.Optional;

public abstract class CommandManager {

    public enum ExecuteResponse {
        IGNORED, EXECUTED, NOTFOUND
    }

    private String prefix;

    private ArrayList<Command> commands = new ArrayList<>();

    public CommandManager(String prefix) {
        this.prefix = prefix;
    }

    private Command getCommand(String label) {
        Optional<Command> commandOpt = this.commands.stream().filter(command -> command.getLabel().equals(label) || command.getAliases().contains(label)).findFirst();
        return commandOpt.orElse(null);
    }

    protected void registerCommand(Command command) {
        if(this.getCommand(command.getLabel()) == null) {
            this.commands.add(command);
        }
    }

    public ExecuteResponse execute(GuildMessageReceivedEvent event) {
        String messageLabel = event.getMessage().getContentRaw().split(" ")[0].replace(this.prefix, "");

        if(event.getMessage().getContentRaw().startsWith(this.prefix)) {
            Command command = this.getCommand(messageLabel);
            if(command != null) {
                Logger.debug("Command " + messageLabel + " executed on " + event.getGuild().getName() + " by " + event.getAuthor().getName());
                command.execute(event);
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

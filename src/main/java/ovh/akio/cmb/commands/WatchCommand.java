package ovh.akio.cmb.commands;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import ovh.akio.cmb.CrossoutMarketBot;
import ovh.akio.cmb.data.CrossoutItem;
import ovh.akio.cmb.data.WatchMemory;
import ovh.akio.cmb.data.discord.Watcher;
import ovh.akio.cmb.impl.command.Command;
import ovh.akio.cmb.utils.BotUtils;
import ovh.akio.cmb.utils.WebAPI;

import java.awt.*;
import java.util.ArrayList;

public class WatchCommand extends Command {

    public WatchCommand(CrossoutMarketBot bot) {
        super(bot);
    }

    @Override
    public String getLabel() {
        return "watch";
    }

    @Override
    public String getDescription(GuildMessageReceivedEvent event) {
        return this.getTranslation(event, "Command.Watch.Description");
    }

    @Override
    public ArrayList<String> getAliases() {
        ArrayList<String> aliases =  new ArrayList<>();
        aliases.add("w");
        return aliases;
    }

    @Override
    public String getUsage() {
        return getLabel() + " <item name>";
    }

    @Override
    public void execute(GuildMessageReceivedEvent event) {

        String prefix = this.getBot().getCommandManager().getPrefix();
        String label = event.getMessage().getContentRaw().split(" ")[0].replace(prefix, "");
        String query = event.getMessage().getContentRaw().replace(prefix + label, "").trim();

        WebAPI webAPI = new WebAPI();

        event.getChannel().sendMessage(new EmbedBuilder().setDescription("Running in the 90's...").build()).queue(message ->
                webAPI.search(query, (result) -> {
                    if(result.size() == 0) {
                        message.editMessage(
                                new EmbedBuilder()
                                        .setAuthor(this.getTranslation(event, "Command.Invite"), "https://discordapp.com/api/oauth2/authorize?client_id=500032551977746453&permissions=59456&scope=bot", event.getJDA().getSelfUser().getAvatarUrl())
                                        .setDescription(this.getTranslation(event, "Command.Watch.NoItemFound"))
                                        .setColor(Color.RED)
                                        .build()
                        ).queue();
                    }else if(result.size() == 1) {
                        this.applyItem(message, event, result.get(0));
                    }else{
                        for (CrossoutItem item : result) {
                            if(item.getName().equalsIgnoreCase(query)) {
                                this.applyItem(message, event, item);
                                return;
                            }
                        }

                        message.editMessage(
                                new EmbedBuilder()
                                        .setAuthor(this.getTranslation(event, "Command.Invite"), "https://discordapp.com/api/oauth2/authorize?client_id=500032551977746453&permissions=59456&scope=bot", event.getJDA().getSelfUser().getAvatarUrl())
                                        .setDescription(this.getTranslation(event, "Command.Watch.MultipleFound"))
                                        .setColor(Color.RED)
                                        .build()
                        ).queue();
                    }
                }, (error) -> {
                    BotUtils.reportException(error);
                    message.editMessage(
                            new EmbedBuilder()
                                    .setAuthor(this.getTranslation(event, "Command.Invite"), "https://discordapp.com/api/oauth2/authorize?client_id=500032551977746453&permissions=59456&scope=bot", event.getJDA().getSelfUser().getAvatarUrl())
                                    .setDescription(String.format(this.getTranslation(event, "Command.Watch.CantProcess"), error.getMessage()))
                                    .setColor(Color.RED)
                                    .build()
                    ).queue();
                })
        );

    }

    private void applyItem(Message message, GuildMessageReceivedEvent event, CrossoutItem item) {
        try {
            Watcher watcher = new Watcher(this.getBot().getDatabase(), event.getAuthor().getIdLong(), item.getId());
            watcher.setItem(item);
            watcher.setLastInterval(System.currentTimeMillis());

            this.getBot().getWatcherManager().addWatcher(watcher);

            message.editMessage(
                    new EmbedBuilder()
                            .setAuthor(this.getTranslation(event, "Command.Invite"), "https://discordapp.com/api/oauth2/authorize?client_id=500032551977746453&permissions=59456&scope=bot", event.getJDA().getSelfUser().getAvatarUrl())
                            .setDescription(String.format(this.getTranslation(event, "Command.Watch.Added"), item.getName()))
                            .setColor(Color.GREEN)
                            .build()
            ).queue();
        } catch (Exception e) {
            BotUtils.reportException(e);
        }
    }

}

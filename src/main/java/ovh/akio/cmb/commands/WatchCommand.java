package ovh.akio.cmb.commands;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import ovh.akio.cmb.CrossoutMarketBot;
import ovh.akio.cmb.data.CrossoutItem;
import ovh.akio.cmb.data.WatchMemory;
import ovh.akio.cmb.impl.Command;
import ovh.akio.cmb.impl.EmbedPage;
import ovh.akio.cmb.utils.BotUtils;
import ovh.akio.cmb.utils.WebAPI;

import java.awt.*;
import java.util.ArrayList;
import java.util.Optional;

public class WatchCommand extends Command {

    public WatchCommand(CrossoutMarketBot bot) {
        super(bot);
    }

    @Override
    public String getLabel() {
        return "watch";
    }

    @Override
    public String getDescription() {
        return "Watch price changes for an item.";
    }

    @Override
    public ArrayList<String> getAliases() {
        return new ArrayList<>();
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

        event.getChannel().sendMessage(new EmbedBuilder().setDescription("Running in the 90's...").build()).queue(message -> {
            webAPI.search(query, (result) -> {
                if(result.size() == 0) {
                    message.editMessage(
                            new EmbedBuilder()
                                    .setAuthor("Click here to invite the bot.", "https://discordapp.com/api/oauth2/authorize?client_id=500032551977746453&permissions=59456&scope=bot", event.getJDA().getSelfUser().getAvatarUrl())
                                    .setDescription("No item found. Please check the item name with `cm:item <item name>`")
                                    .setColor(Color.RED)
                                    .build()
                    ).queue();
                }else if(result.size() == 1) {
                    WatchMemory memory = new WatchMemory(result.get(0), event.getAuthor());
                    this.getBot().getTimerWatch().addWatch(memory, aVoid -> {
                        message.editMessage(
                                new EmbedBuilder()
                                        .setAuthor("Click here to invite the bot.", "https://discordapp.com/api/oauth2/authorize?client_id=500032551977746453&permissions=59456&scope=bot", event.getJDA().getSelfUser().getAvatarUrl())
                                        .setDescription(result.get(0).getName() + " has been added to your watchers list.")
                                        .setColor(Color.GREEN)
                                        .build()
                        ).queue();
                    });

                }else{
                    message.editMessage(
                            new EmbedBuilder()
                                    .setAuthor("Click here to invite the bot.", "https://discordapp.com/api/oauth2/authorize?client_id=500032551977746453&permissions=59456&scope=bot", event.getJDA().getSelfUser().getAvatarUrl())
                                    .setDescription("Multiple items found. Please check the item name with `cm:item <item name>`")
                                    .setColor(Color.RED)
                                    .build()
                    ).queue();
                }
            }, (error) -> {
                BotUtils.reportException(error);
                message.editMessage(
                        new EmbedBuilder()
                                .setAuthor("Click here to invite the bot.", "https://discordapp.com/api/oauth2/authorize?client_id=500032551977746453&permissions=59456&scope=bot", event.getJDA().getSelfUser().getAvatarUrl())
                                .setDescription(String.format("Couldn't process your request : %s", error.getMessage()))
                                .setColor(Color.RED)
                                .build()
                ).queue();
            });
        });

    }
}

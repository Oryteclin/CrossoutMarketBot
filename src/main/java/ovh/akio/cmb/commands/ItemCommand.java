package ovh.akio.cmb.commands;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import ovh.akio.cmb.CrossoutMarketBot;
import ovh.akio.cmb.data.CrossoutItem;
import ovh.akio.cmb.impl.Command;
import ovh.akio.cmb.impl.EmbedPage;
import ovh.akio.cmb.utils.BotUtils;
import ovh.akio.cmb.utils.WebAPI;

import java.awt.*;
import java.util.ArrayList;
import java.util.Optional;

public class ItemCommand extends Command {

    public ItemCommand(CrossoutMarketBot bot) {
        super(bot);
    }

    @Override
    public String getLabel() {
        return "item";
    }

    @Override
    public String getDescription() {
        return "Search for an item in the CrossoutDB Item Database";
    }

    @Override
    public ArrayList<String> getAliases() {
        return new ArrayList<>();
    }

    @Override
    public String getUsage() {
        return getLabel() + " [item name]";
    }

    @Override
    public void execute(GuildMessageReceivedEvent event) {

        String prefix = this.getBot().getCommandManager().getPrefix();
        String label = event.getMessage().getContentRaw().split(" ")[0].replace(prefix, "");
        String query = event.getMessage().getContentRaw().replace(prefix + label, "").trim();

        final long startQuery = System.currentTimeMillis();

        WebAPI webAPI = new WebAPI();

        event.getChannel().sendMessage(new EmbedBuilder().setDescription("Running in the 90's...").build()).queue(message -> {
            webAPI.search(query, (result) -> {
                long queryTime = System.currentTimeMillis() - startQuery;
                if(result.size() == 0) {
                    message.editMessage(
                            new EmbedBuilder()
                                    .setDescription(String.format("No item found for query `%s`", query))
                                    .setColor(Color.RED)
                                    .build()
                    ).queue();
                }else if(result.size() == 1) {
                    message.editMessage(result.get(0).toEmbed(queryTime, event.getJDA().getSelfUser().getAvatarUrl()).build()).queue();
                }else{
                    Optional<CrossoutItem> stream = result.stream().filter(crossoutItem -> crossoutItem.getName().equalsIgnoreCase(query)).findFirst();

                    if(stream.isPresent()) {
                        CrossoutItem item = stream.get();
                        message.editMessage(item.toEmbed(queryTime, event.getJDA().getSelfUser().getAvatarUrl()).build()).queue();
                        return;
                    }

                    new EmbedPage(message, new ArrayList<>(result), 10) {
                        @Override
                        public EmbedBuilder getEmbed() {
                            EmbedBuilder builder = new EmbedBuilder();
                            builder.setTitle("Item list");
                            return builder;
                        }
                    };
                }
            }, (error) -> {
                BotUtils.reportException(error);
                message.editMessage(
                        new EmbedBuilder()
                                .setDescription(String.format("Couldn't process your request : %s", error.getMessage()))
                                .setColor(Color.RED)
                                .build()
                ).queue();
            });
        });
    }

}

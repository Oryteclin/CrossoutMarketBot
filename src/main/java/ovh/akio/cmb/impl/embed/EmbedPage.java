package ovh.akio.cmb.impl.embed;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import ovh.akio.cmb.CrossoutMarketBot;
import ovh.akio.cmb.utils.BotUtils;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public abstract class EmbedPage extends ListenerAdapter {

    private class EditorTimer extends TimerTask {

        private int timeout;
        private int timeLeft;

        EditorTimer(int timeout) {
            this.timeout = timeout;
            this.timeLeft = timeout;
        }


        @Override
        public void run() {
            if(resetTimer) {
                this.timeLeft = timeout;
                resetTimer = false;
            }else if(this.timeLeft == 0) {
                timeout(message);
            }else{
                this.timeLeft--;
            }

        }

    }

    private EditorTimer editorTimer;
    private Timer timer = new Timer();


    private Message message;
    private ArrayList<String> items = new ArrayList<>();

    private boolean resetTimer = false;
    private int currentPage;


    private int totalPage;

    protected EmbedPage(Message message, ArrayList<EmbedItem> items, int timeout) {
        this.message = message;

        items.forEach(o -> this.items.add(o.toString()));
        this.currentPage = 1;

        this.editorTimer = new EditorTimer(timeout);

        this.message.getJDA().addEventListener(this);


        float a = (float)items.size()/10f;
        int b = items.size()/10;

        if(a > b) {
            totalPage = b+1;
        }else{
            totalPage = b;
        }

        if(items.size() > 10) {
            if(CrossoutMarketBot.checkPermission(message.getGuild(), message.getTextChannel(), Permission.MESSAGE_MANAGE)) {
                this.message.addReaction("◀").queue(aVoid ->
                        this.message.addReaction("▶").queue(aVoid1 ->
                                this.message.addReaction("❌").queue(aVoid2 -> {
                                    refreshEmbed();
                                    this.timer.scheduleAtFixedRate(this.editorTimer, 0, 1000);
                                })

                        )
                );
            }else{
                refreshEmbed();
                message.getChannel().sendMessage("You can't view all items because you can't switch page with reactions. For this feature to work, I need the permission Manage Message.").queue();
            }
        }else{
            refreshEmbed();
        }


    }

    public abstract EmbedBuilder getEmbed();

    private void timeout(Message message) {
        this.timer.cancel();
        message.clearReactions().queue();
        message.getJDA().removeEventListener(this);
    }

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {

        if(event.getMessageIdLong() != message.getIdLong()) return;

        String emote = event.getReactionEmote().getName();
        if(event.getUser().equals(event.getJDA().getSelfUser())) return;

        if(emote.equals("▶")) nextPage();
        if(emote.equals("◀")) previousPage();
        if(emote.equals("❌")) timeout(message);

        event.getReaction().removeReaction(event.getUser()).queue();

    }

    private void refreshEmbed() {
        this.resetTimer = true;
        this.message.editMessage(this.getEmbed().setDescription(getPageText()).setFooter("Page " + currentPage + "/" + totalPage, null).build()).queue();
    }

    private boolean isPageValid() {
        int[] pageBound = BotUtils.getPageBound(this.items.size(), this.currentPage, 10);
        return pageBound != null;
    }

    private String getPageText(){

        int[] pageBound = BotUtils.getPageBound(this.items.size(), this.currentPage, 10);
        if(pageBound == null) return "";
        StringBuilder builder = new StringBuilder();
        for(int i = pageBound[0] ; i < pageBound[1] ; i++) {
            builder.append(this.items.get(i).trim()).append("\n");
        }

        return builder.toString();
    }

    private void nextPage() {
        this.currentPage++;

        if(this.isPageValid()) {
            refreshEmbed();
        }else{
            this.currentPage--;
        }
    }

    private void previousPage() {
        this.currentPage--;

        if(this.isPageValid()) {
            refreshEmbed();
        }else{
            this.currentPage++;
        }
    }



}

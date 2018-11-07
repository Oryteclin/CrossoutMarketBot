package ovh.akio.cmb.data;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.json.JSONObject;
import ovh.akio.cmb.impl.embed.EmbedItem;
import ovh.akio.cmb.utils.BotUtils;
import ovh.akio.cmb.utils.language.Translation;

import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class CrossoutItem implements EmbedItem {

    private int id;
    private String name;
    private String description;
    private String rarity;

    private double marketSell;
    private double marketBuy;
    private double craftSell;
    private double craftBuy;

    private boolean craftable;
    private String lastUpdate;
    private Date lastUpdateDate;


    public CrossoutItem(JSONObject jsonObject) {

        this.id = jsonObject.getInt("id");
        this.name = jsonObject.getString("name");
        this.description = jsonObject.get("description") != JSONObject.NULL ? BotUtils.formatJSON(jsonObject.getString("description")) : "*No description for this item.*";
        this.rarity = jsonObject.get("rarityName") != JSONObject.NULL ? jsonObject.getString("rarityName") : "";

        this.marketSell = jsonObject.getLong("sellPrice");
        this.marketBuy = jsonObject.getLong("buyPrice");
        this.craftSell = jsonObject.getLong("craftingSellSum");
        this.craftBuy = jsonObject.getLong("craftingBuySum");

        this.craftable = !(craftSell == 0 || craftBuy == 0);

        this.lastUpdate = jsonObject.getString("timestamp"); refreshDate();

    }

    private void refreshDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        try {
            lastUpdateDate = formatter.parse(this.lastUpdate);
            lastUpdateDate.setTime(lastUpdateDate.getTime()+3600);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getMarketSell() {
        return marketSell;
    }

    public double getMarketBuy() {
        return marketBuy;
    }

    public double getCraftSell() {
        return craftSell;
    }

    public double getCraftBuy() {
        return craftBuy;
    }

    public boolean isCraftable() {
        return craftable;
    }

    @Override
    public String toString() {
        return this.getName();
    }

    public EmbedBuilder toEmbed(Translation tr, Long queryTime, String avatarUrl) {

        EmbedBuilder builder = new EmbedBuilder();

        builder.setAuthor("Click here to invite the bot.", "https://discordapp.com/api/oauth2/authorize?client_id=500032551977746453&permissions=59456&scope=bot", avatarUrl);

        builder.setTitle(this.name, "https://crossoutdb.com/item/" + this.id);
        builder.setDescription(this.description);

        String currencyValue = "%,.2f %s";

        builder.addField(tr.getString("Item.Buy"), String.format(currencyValue, this.marketSell/100, tr.getString("Item.Currency")), true);
        builder.addField(tr.getString("Item.Sell"), String.format(currencyValue, this.marketBuy/100, tr.getString("Item.Currency")), true);
        if(craftable) {
            builder.addField(tr.getString("Item.CraftBuy"), String.format(currencyValue, this.craftSell/100, tr.getString("Item.Currency")), true);
            builder.addField(tr.getString("Item.CraftSell"), String.format(currencyValue, this.craftBuy/100, tr.getString("Item.Currency")), true);
        }
        builder.setTimestamp(this.lastUpdateDate.toInstant());
        builder.setFooter(String.format(tr.getString("Item.QueryTime"), queryTime), null);
        builder.setThumbnail(this.getThumbnailImage());

        switch (this.rarity) {
            case "Common":
                builder.setColor(new Color(216, 216, 216));
                break;
            case "Rare":
                builder.setColor(new Color(0, 99, 200));
                break;
            case "Epic":
                builder.setColor(new Color(174, 0, 241));
                break;
            case "Legendary":
                builder.setColor(new Color(236, 147, 58));
                break;
            case "Relic":
                builder.setColor(new Color(219, 87,0));
                break;

        }
        return builder;
    }

    public String getThumbnailImage() {
        Date today = Calendar.getInstance().getTime();
        return String.format("https://crossoutdb.com/img/items/%o.png?d=%tY%tm%td", this.getId(), today, today, today);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof CrossoutItem) {
            CrossoutItem item = ((CrossoutItem) obj);
            return item.getId() == this.getId();
        }
        return super.equals(obj);
    }
}

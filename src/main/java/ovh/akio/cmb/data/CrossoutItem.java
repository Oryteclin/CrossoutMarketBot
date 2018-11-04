package ovh.akio.cmb.data;

import net.dv8tion.jda.core.EmbedBuilder;
import org.json.JSONObject;
import ovh.akio.cmb.impl.embed.EmbedItem;
import ovh.akio.cmb.utils.BotUtils;

import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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

    public EmbedBuilder toEmbed(Long queryTime, String avatarUrl) {

        EmbedBuilder builder = new EmbedBuilder();

        builder.setAuthor("Click here to invite the bot.", "https://discordapp.com/api/oauth2/authorize?client_id=500032551977746453&permissions=59456&scope=bot", avatarUrl);

        builder.setTitle(this.name, "https://crossoutdb.com/item/" + this.id);
        builder.setDescription(this.description);

        builder.addField("Buy it for", String.format("%,.2f", this.marketSell/100) + " Gold", true);
        builder.addField("Sell it for", String.format("%,.2f", this.marketBuy/100) + " Gold", true);
        if(craftable) {
            builder.addField("Buy Craft Item for", String.format("%,.2f", this.craftSell/100) + " Gold", true);
            builder.addField("Sell Craft Item for", String.format("%,.2f", this.craftBuy/100) + " Gold", true);
        }
        builder.setTimestamp(this.lastUpdateDate.toInstant());
        builder.setFooter(String.format("CrossoutDB API Response time : %oms • Updated ", queryTime), null);
        builder.setThumbnail(String.format("https://crossoutdb.com/img/items/%d.png", this.id));

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

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof CrossoutItem) {
            CrossoutItem item = ((CrossoutItem) obj);
            return item.getId() == this.getId();
        }
        return super.equals(obj);
    }
}

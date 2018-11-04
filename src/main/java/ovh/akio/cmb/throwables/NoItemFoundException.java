package ovh.akio.cmb.throwables;

public class NoItemFoundException extends Exception {

    private int itemID = -1;
    private String itemName = "";

    public NoItemFoundException(int id) {
        this.itemID = id;
    }

    public NoItemFoundException(String itemName) {
        this.itemName = itemName;
    }

    public int getItemID() {
        return itemID;
    }

    public String getItemName() {
        return itemName;
    }
}

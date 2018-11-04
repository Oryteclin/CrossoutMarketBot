package ovh.akio.cmb.data;

import net.dv8tion.jda.core.entities.User;
import ovh.akio.cmb.utils.WebAPI;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class WatchMemory {

    private CrossoutItem item;
    private User user;

    public WatchMemory(CrossoutItem item, User user) {
        this.item = item;
        this.user = user;
    }

    public CrossoutItem getItem() {
        return item;
    }

    public User getUser() {
        return user;
    }

    public void refresh(BiConsumer<CrossoutItem, CrossoutItem> onSuccess, Consumer<Exception> onFailure) {
        new WebAPI().getItem(this.item.getId(), (item) -> {
            onSuccess.accept(this.item, item);
            this.item = item;
        }, onFailure);
    }

    public boolean equals(User user, CrossoutItem crossoutItem) {
        return user.equals(this.user) && crossoutItem.equals(this.item);
    }

}

package ovh.akio.cmb.impl.watch;

import ovh.akio.cmb.data.CrossoutItem;

public interface WatchResponseHandler {

    void onRefreshFailure(Exception e);
    void onRefreshSuccess(CrossoutItem before, CrossoutItem after);

}

package ovh.akio.cmb.utils;

import org.json.JSONArray;
import ovh.akio.cmb.data.CrossoutItem;
import ovh.akio.cmb.throwables.NoItemFoundException;
import ovh.akio.cmb.throwables.WebRequestFailed;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.function.Consumer;

public class WebAPI {

    private static String ITEMS = "https://crossoutdb.com/api/v1/items";
    private static String ITEMQUERY = "https://crossoutdb.com/api/v1/items?query=%s";
    private static String ITEM = "https://crossoutdb.com/api/v1/item/%d";

    private void httpRequest(String urlStr, Consumer<String> onSuccess, Consumer<Exception> onFailure) {
        try {
            StringBuilder result = new StringBuilder();
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            rd.close();
            onSuccess.accept(result.toString());
        } catch (IOException e) {
            BotUtils.reportException(e);
            onFailure.accept(new WebRequestFailed(e));
        }
    }

    public void getItem(int id, Consumer<CrossoutItem> onSuccess, Consumer<Exception> onFailure) {
        String finalURL = String.format(WebAPI.ITEM, id);
        this.httpRequest(finalURL, (response) -> {
            JSONArray resultArray = new JSONArray(response);
            if(resultArray.length() == 0) {
                onFailure.accept(new NoItemFoundException(id));
            }
            onSuccess.accept(new CrossoutItem(resultArray.getJSONObject(0)));
        }, onFailure);
    }

    public void search(String query, Consumer<ArrayList<CrossoutItem>> onSuccess, Consumer<Exception> onFailure) {
        String finalURL = null;
        try {
            finalURL = String.format(WebAPI.ITEMQUERY, URLEncoder.encode(query, "UTF-8"));
        } catch(Exception e) {
            // Empty Catch Block
        }
        this.httpRequest(finalURL, (response) -> {
            JSONArray resultArray = new JSONArray(response);
            ArrayList<CrossoutItem> items = new ArrayList<>();
            for (int i = 0; i < resultArray.length(); i++) {
                items.add(new CrossoutItem(resultArray.getJSONObject(i)));
            }
            onSuccess.accept(items);
        }, onFailure);
    }

}

package com.example.assessment;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.AsyncTask;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private ListView itemListView;
    private List<Item> itemList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        itemListView = findViewById(R.id.itemListView);
        itemList = new ArrayList<>();

        new FetchItemsTask().execute();
    }

    private class FetchItemsTask extends AsyncTask<Void, Void, List<Item>> {

        @Override
        protected List<Item> doInBackground(Void... voids) {
            List<Item> items = new ArrayList<>();
            try {
                URL url = new URL("https://fetch-hiring.s3.amazonaws.com/hiring.json");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream());

                int data = inputStreamReader.read();
                StringBuilder response = new StringBuilder();
                while (data != -1) {
                    char current = (char) data;
                    response.append(current);
                    data = inputStreamReader.read();
                }

                JSONArray jsonArray = new JSONArray(response.toString());
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject itemJson = jsonArray.getJSONObject(i);
                    int id = itemJson.getInt("id");
                    int listId = itemJson.getInt("listId");
                    String name = itemJson.optString("name");

                    if (name != null && !name.isEmpty()) {
                        Item item = new Item();
                        item.setId(id);
                        item.setListId(listId);
                        item.setName(name);
                        items.add(item);
                    }
                }

                Collections.sort(items, new Comparator<Item>() {
                    @Override
                    public int compare(Item item1, Item item2) {
                        if (item1.getListId() == item2.getListId()) {
                            return item1.getName().compareTo(item2.getName());
                        } else {
                            return Integer.compare(item1.getListId(), item2.getListId());
                        }
                    }
                });

                inputStreamReader.close();
                connection.disconnect();
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return items;
        }

        @Override
        protected void onPostExecute(List<Item> items) {
            super.onPostExecute(items);
            itemList = items;
            displayItems();
        }
    }

    private void displayItems() {
        Map<Integer, List<Item>> groupedItems = new HashMap<>();
        for (Item item : itemList) {
            int listId = item.getListId();
            if (!groupedItems.containsKey(listId)) {
                groupedItems.put(listId, new ArrayList<>());
            }
            groupedItems.get(listId).add(item);
        }

        List<String> displayList = new ArrayList<>();
        for (Map.Entry<Integer, List<Item>> entry : groupedItems.entrySet()) {
            displayList.add("List ID: " + entry.getKey());
            for (Item item : entry.getValue()) {
                displayList.add("  - " + item.getName());
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, displayList);
        itemListView.setAdapter(adapter);
    }
}

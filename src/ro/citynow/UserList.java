package ro.citynow;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.ProgressBar;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class UserList extends Activity {
    private static final int USER_ENTRY = 3;

    private Integer categorieId;
    private Integer subcategorieId;
    private EndlessScrollListener scrollListener;
    private UserListAdapter adapter = new UserListAdapter();

    private ProgressBar progressBar;

    private int page = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_list);

        Intent intent = getIntent();
        this.categorieId = intent.getIntExtra("categorie_id", 0);
        this.subcategorieId = intent.getIntExtra("subcategorie_id", 0);
        this.progressBar = (ProgressBar) findViewById(R.id.progressBar);

        initList();
        loadMoneItems(0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.adapter.clearList();
    }

    private void initList() {
        this.scrollListener = new EndlessScrollListener();

        ListView listView = (ListView) findViewById(R.id.usersList);
        listView.setAdapter(adapter);
        listView.setOnScrollListener(scrollListener);
    }

    private void loadNextPage() {
        Log.d("usr", String.format("next page triggered = %d", page + 1));
        this.scrollListener.setHasMoreItems(false);
        this.loadMoneItems(++page);
    }

    private void loadMoneItems(Integer page) {
        Log.d("usr", String.format("page = %d", page));
        final String url = "http://www.citynow.ro/m/gl.json";

        Poster poster = new Poster() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
                progressBar.setProgress(values[0]);
            }

            @Override
            protected void onPostExecute(ServerResponse serverResponse) {
                super.onPostExecute(serverResponse);
                progressBar.setProgress(0);
                updateUseri(serverResponse);
            }
        };

        poster.execute(url, String.format("c=%d", this.categorieId), String.format("s=%d", this.subcategorieId),
                       String.format("p=%d", page));
    }

    private void updateUseri(ServerResponse response) {
        Log.d("usr", String.valueOf(response.getResponseCode()));
        boolean hasMore;

        try {
            final JSONObject jsonObject = response.parseAsJson();
            final Integer lenght = jsonObject.getInt("l");
            final Integer items = jsonObject.getInt("i");

            hasMore = (lenght >= items);

            final JSONArray users = jsonObject.getJSONArray("u");
            for (int i=0; i<users.length(); ++i) {
                JSONObject user = users.getJSONObject(i);
                adapter.addUser(new User(user.getInt("i"), user.getString("d"), user.getString("z"),
                        user.getString("a"), user.getString("l")));
            }
            adapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
            hasMore = false;
        }

        this.scrollListener.setHasMoreItems(hasMore);
    }

    //-------------------
    //      CLASSES
    //-------------------

    public class EndlessScrollListener implements AbsListView.OnScrollListener {
        private int visibleThreshold = 5;
        private boolean hasMoreItems = false;

        public void setHasMoreItems(boolean hasMoreItems) {
            this.hasMoreItems = hasMoreItems;
        }

        public EndlessScrollListener() {}

        public EndlessScrollListener(int visibleThreshold) {
            this.visibleThreshold = visibleThreshold;
        }

        @Override
        public void onScrollStateChanged(AbsListView absListView, int i) {}

        @Override
        public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if (hasMoreItems && (totalItemCount - visibleItemCount) < (firstVisibleItem + visibleThreshold)) {
                Log.d("usr", String.format(" %d - %d - %d", totalItemCount, visibleItemCount, firstVisibleItem));
                loadNextPage();
            }
        }
    }

}
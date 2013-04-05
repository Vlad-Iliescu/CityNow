package ro.citynow;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ListView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class UserList extends Activity {
    Integer categorieId;
    Integer subcategorieId;
    EndlessScrollListener scrollListener;
    UserListAdapter adapter = new UserListAdapter();

    private ProgressDialog mProgressDialog;
    private Handler handler;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_list);

        Intent intent = getIntent();
        this.categorieId = intent.getIntExtra("categorie_id", 0);
        this.subcategorieId = intent.getIntExtra("subcategorie_id", 0);

        mProgressDialog = new ProgressDialog(UserList.this);
        mProgressDialog.setMessage("A message");
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setMax(100);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

        handler = new Handler();
        this.scrollListener = new EndlessScrollListener() {
            @Override
            public void loadMore(int currentPage) {
                this.setMoreItems(loadMoneItems(currentPage, false));
            }
        };

        ListView listView = (ListView) findViewById(R.id.usersList);
        listView.setAdapter(adapter);


        loadMoneItems(0, true);
    }

    private void updateUseri(ServerResponse response, boolean setUpScroll) {
        Log.d("usr", String.valueOf(response.getResponseCode()));
        boolean attach = true;

        try {
            JSONObject jsonObject = response.parseAsJson();
            Integer lenght = jsonObject.getInt("l");
            Integer items = jsonObject.getInt("i");

            attach = (lenght >= items);

            this.scrollListener.setMoreItems(lenght < items);
            JSONArray users = jsonObject.getJSONArray("u");
            for (int i=0; i<users.length(); ++i) {
                JSONObject user = users.getJSONObject(i);
                adapter.addUser(new User(user.getInt("i"), user.getString("d"), user.getString("z"),
                        user.getString("a"), user.getString("l")));
            }
            adapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
            attach = false;
        }



        if (setUpScroll && attach) {
            ListView listView = (ListView) findViewById(R.id.usersList);
            listView.setOnScrollListener(scrollListener);
        }
    }

    private boolean loadMoneItems(Integer page, boolean setUpScroll) {
        final String url = "http://www.citynow.ro/m/gl.json";
        final boolean setUp = setUpScroll;

        Poster poster = new Poster() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mProgressDialog.show();
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
                mProgressDialog.setProgress(values[0]);
            }

            @Override
            protected void onPostExecute(ServerResponse serverResponse) {
                super.onPostExecute(serverResponse);
                updateUseri(serverResponse, setUp);
                mProgressDialog.hide();
            }
        };

        poster.execute(url, String.format("c=%d", this.categorieId), String.format("s=%d", this.subcategorieId),
                String.format("p=%d", page));
        return true;
    }
}
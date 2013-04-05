package ro.citynow;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SubcatList extends Activity {
    private static final int LIST_ENTRY = 2;

    private ProgressDialog mProgressDialog;
    private Handler handler;
    private DBHelper dbHelper;
    private CategorieAdapter adapter;
    Integer categorieId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subcat_list);

        mProgressDialog = new ProgressDialog(SubcatList.this);
        mProgressDialog.setMessage("A message");
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setMax(100);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

        handler = new Handler();

        Intent intent = getIntent();
        this.categorieId = intent.getIntExtra("categorie_id", 0);

        Log.d("cat", String.valueOf(this.categorieId));

        this.dbHelper = new DBHelper(this);
        adapter = new CategorieAdapter(this, this.dbHelper.getSubcategorii(this.categorieId));

        ListView listView = (ListView) findViewById(R.id.subcatList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
                itemClicked(id);
            }
        });

        setCategorie(this.categorieId);
    }

    private void itemClicked(long id) {
        Cursor subcategorie = dbHelper.getSubcategorie(id);
        if (subcategorie != null) {
            if (subcategorie.moveToFirst()) {
                Integer cat_id = subcategorie.getInt(subcategorie.getColumnIndex(DB.SUBCATEGORIE.CATEGORIE_ID));
                Integer subcat_id = subcategorie.getInt(subcategorie.getColumnIndex(DB.SUBCATEGORIE.SUBCATEGORIE_ID));

                Log.d("resp", String.format("{cat=%d, subcat=%d}", cat_id, subcat_id));
            }
        }
    }

    private void setCategorie(Integer categorie_id) {
        final String url = "https://www.citynow.ro/m/us.json";

        Log.d("resp", "START");
        Poster poster = new Poster() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mProgressDialog.show();
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
                Log.d("resp", String.valueOf(values[0]));
                mProgressDialog.setProgress(values[0]);
            }

            @Override
            protected void onPostExecute(ServerResponse serverResponse) {
                super.onPostExecute(serverResponse);
                updateCategorii(serverResponse);
                Log.d("resp", "STOP");
                mProgressDialog.hide();
            }
        };

        poster.execute(url, "d=010120130101", String.format("c=%d", categorie_id));
    }

    private void updateCategorii(ServerResponse response) {
        if (response.getResponseCode() == 200) {
            final JSONObject jsonObject;
            try {
                jsonObject = response.parseAsJson();
                if ((jsonObject != null) && (jsonObject.getInt("l") != 0)) {
                    Thread thread = new Thread() {
                        @Override
                        public void run() {
                            try {
                                JSONArray subcat = jsonObject.getJSONArray("s");
                                for (int i=0; i<subcat.length(); ++i) {
                                    JSONObject scat = subcat.getJSONObject(i);
                                    Integer subcat_id = dbHelper.getSubcategorieId(scat.getInt("i"));

                                    if (subcat_id != null) {
                                        dbHelper.updateSubcategorieById(subcat_id, scat.getInt("i"), scat.getInt("c"),
                                                scat.getString("n"), scat.getString("b"), scat.getString("t"),
                                                scat.getInt("a"), scat.getInt("s"));
                                    } else {
                                        dbHelper.saveSubcategorie(scat.getInt("i"), scat.getInt("c"),
                                                scat.getString("n"), scat.getString("b"), scat.getString("t"),
                                                scat.getInt("a"), scat.getInt("s"));
                                    }
                                }

                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        refreshSubcategorii();
                                    }
                                });
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    thread.run();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void refreshSubcategorii() {
        this.adapter.changeCursor(dbHelper.getSubcategorii(this.categorieId));
        this.adapter.notifyDataSetChanged();
    }
}
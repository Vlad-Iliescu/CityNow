package ro.citynow;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.util.LruCache;
import android.text.format.Formatter;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class SubcatList extends Activity {
    private static final int LIST_ENTRY = 2;

    private ProgressDialog mProgressDialog;
    private Handler handler;
    private DBHelper dbHelper;
    private SubcategorieAdapter adapter;

    private Integer categorieId;
    private ThumbnailCache thumbnailCache;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subcat_list);

        Intent intent = getIntent();
        this.categorieId = intent.getIntExtra("categorie_id", 0);
        Log.d("cat", String.valueOf(this.categorieId));

        handler = new Handler();
        this.dbHelper = new DBHelper(this);

        initDialog();
        initList();
        initCache();

        setCategorie(this.categorieId);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        adapter.changeCursor(null);
    }

    private void initDialog() {
        mProgressDialog = new ProgressDialog(SubcatList.this);
        mProgressDialog.setMessage("loading data...");
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setMax(100);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    }

    private void initList() {
        this.adapter = new SubcategorieAdapter(this, this.dbHelper.getSubcategorii(this.categorieId));

        ListView listView = (ListView) findViewById(R.id.subcatList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
                itemClicked(id);
            }
        });
        listView.setRecyclerListener( new AbsListView.RecyclerListener() {
            @Override
            public void onMovedToScrapHeap(View view) {
                final ImageView imageView = (ImageView) view.findViewById(R.id.imageView);
                imageView.setImageBitmap(null);
            }
        });
    }

    private void initCache() {
        final ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final int memoryClassBytes = am.getMemoryClass() * 1024 * 1024;
        this.thumbnailCache = new ThumbnailCache(memoryClassBytes / 2);
    }


    private void itemClicked(long id) {
        Cursor subcategorie = dbHelper.getSubcategorie(id);
        if (subcategorie != null) {
            if (subcategorie.moveToFirst()) {
                Integer cat_id = subcategorie.getInt(subcategorie.getColumnIndex(DB.SUBCATEGORIE.CATEGORIE_ID));
                Integer subcat_id = subcategorie.getInt(subcategorie.getColumnIndex(DB.SUBCATEGORIE.SUBCATEGORIE_ID));

                Intent intent = new Intent(this, UserList.class);
                intent.putExtra("categorie_id", cat_id);
                intent.putExtra("subcategorie_id", subcat_id);
                startActivityForResult(intent, LIST_ENTRY);

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
                mProgressDialog.dismiss();
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

    private void cacheStats() {
        Log.d("cache", String.format("{size=%s, hits=%d, miss=%d, evictions=%d}",
                Formatter.formatFileSize(this, thumbnailCache.size()),
                thumbnailCache.hitCount(), thumbnailCache.missCount(),
                thumbnailCache.evictionCount() ));
    }

    //----------------------------------
    //          CLASSES
    //----------------------------------

    public static class ThumbnailCache extends LruCache<Long, Bitmap> {
        public ThumbnailCache(int maxSizeBytes) {
            super(maxSizeBytes);
        }

        @Override
        protected int sizeOf(Long key, Bitmap value) {
            return value.getRowBytes() * value.getHeight();
        }
    }

    public class SubcategorieAdapter extends CursorAdapter {

        public SubcategorieAdapter(Context context, Cursor c) {
            super(context, c, false);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
            LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
            return inflater.inflate(R.layout.categorie_item, viewGroup, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView denumire = (TextView) view.findViewById(R.id.denumire);
            denumire.setTextColor(Color.parseColor("#" + cursor.getString(
                    cursor.getColumnIndex(DB.SUBCATEGORIE.TEXT_COLOR))));
            denumire.setText(cursor.getString(cursor.getColumnIndex(DB.SUBCATEGORIE.NAME)));

            // IMAGINE
            final long pozaId = cursor.getInt(cursor.getColumnIndex(DB.SUBCATEGORIE.ID));
            final ImageView imageView = (ImageView) view.findViewById(R.id.imageView);
            final ThumbnailAsyncTask oldCreator = (ThumbnailAsyncTask) imageView.getTag();

            if (oldCreator != null) {
                oldCreator.cancel(false);
            }

            final Bitmap cachedResult = thumbnailCache.get(pozaId);
            if (cachedResult != null) {
                imageView.setImageBitmap(cachedResult);
                cacheStats();
                return;
            }

            final ThumbnailAsyncTask creator = new ThumbnailAsyncTask(imageView);
            imageView.setImageBitmap(null);
            imageView.setTag(creator);
            creator.execute(pozaId, (long) cursor.getInt(cursor.getColumnIndex(DB.SUBCATEGORIE.SUBCATEGORIE_ID)));
        }
    }

    public class ThumbnailAsyncTask extends AsyncTask<Long, Void, Bitmap> {
        private final ImageView imageView;

        public ThumbnailAsyncTask(ImageView imageView) {
            this.imageView = imageView;
        }

        @Override
        protected void onPreExecute() {
            imageView.setTag(this);
        }

        @Override
        protected Bitmap doInBackground(Long... longs) {
            final long id = longs[0];
            final long subcat_id = longs[1];

            final Cursor query = dbHelper.getPozaSubcategorie(id);
            final String blob;
            if (query != null) {
                if (query.moveToFirst()) {
                    blob = query.getString(query.getColumnIndex(DB.SUBCATEGORIE.POZA));
                } else {
                    blob = null;
                }
            } else {
                blob = null;
            }

            if (blob == null) {
                Log.d("bitmap", String.format("{cat=%d, bm=null, m='no blob'}", subcat_id));
                Log.d("blob", String.valueOf(subcat_id));
                return null;
            }

            byte[] decodedByte = new byte[0];
            try {
                decodedByte = Base64.decode(blob.getBytes(), Base64.NO_WRAP);
            } catch (IllegalArgumentException ex) {
                Log.d("blob", String.valueOf(subcat_id));
                Log.d("bitmap", String.format("{cat=%d, bm=null, m='no decode', dec=%s}", subcat_id,
                                              Arrays.toString(decodedByte)));
                return null;
            }

            Bitmap bitmap = null;

            if (decodedByte.length > 0) {
                bitmap = BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
            }

            if (bitmap != null) {
                thumbnailCache.put(id, bitmap);
                cacheStats();
            } else {
                Log.d("bitmap", String.format("{cat=%d, bm=%s, m='no bitmap' dec=%s}", subcat_id, bitmap,
                                              Arrays.toString(decodedByte)));
                return null;
            }

            Log.d("bitmap", String.format("{cat=%d, bm=%s}", subcat_id, bitmap.toString()));
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (imageView.getTag() == this) {
                imageView.setImageBitmap(bitmap);
                imageView.setTag(null);
            }
        }

    }
}
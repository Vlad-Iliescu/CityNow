package ro.citynow;

import android.app.Activity;
import android.app.ActivityManager;
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
import android.widget.AbsListView.RecyclerListener;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class CityNow extends Activity {
    public static final int SUBCAT_ENTRY = 1;
    public static final int LIST_ENTRY = 2;

    private Handler handler;
    private DBHelper dbHelper;
    private CategorieAdapter adapter;

    private ThumbnailCache thumbnailCache;
    private ProgressBar progressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        handler = new Handler();
        this.dbHelper = new DBHelper(this);
        this.progressBar = (ProgressBar) findViewById(R.id.progressBar);

        initCache();
        initList();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        adapter.changeCursor(null);
    }

    public void itemClicked(long id) {
        Cursor categorie = dbHelper.getCategorie(id);
        if (categorie != null) {
            if (categorie.moveToFirst()) {
                Integer cat_id = categorie.getInt(categorie.getColumnIndex(DB.CATEGORIE.CATEGORIE_ID));
                Integer subcat = categorie.getInt(categorie.getColumnIndex(DB.CATEGORIE.SUBCAT));

                if (subcat > 1) {
                    Intent intent = new Intent(this, SubcatList.class);
                    intent.putExtra("categorie_id", cat_id);
                    startActivityForResult(intent, SUBCAT_ENTRY);
                } else {
                    Intent intent = new Intent(this, UserList.class);
                    intent.putExtra("categorie_id", cat_id);
                    startActivityForResult(intent, LIST_ENTRY);
                }
            }
        }
    }

    public void onLogoButtonClick(View view) {
        final String url = "https://www.citynow.ro/m/uc.json";

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
                updateCategorii(serverResponse);
            }
        };

        poster.execute(url, "d=010120130101");
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
                                JSONArray categorii = jsonObject.getJSONArray("c");
                                for (int i = 0; i<categorii.length(); ++i) {
                                    JSONObject cat = categorii.getJSONObject(i);
                                    Integer cat_id = dbHelper.getCategorieId(cat.getInt("i"));

                                    if (cat_id != null) {
                                        dbHelper.updateCategorieById(cat_id, cat.getInt("i"), cat.getString("n"),
                                                cat.getString("b"), cat.getString("c"), cat.getInt("a"), cat.getInt("s"));
                                    } else {
                                        dbHelper.saveCategorie(cat.getInt("i"), cat.getString("n"),
                                                cat.getString("b"), cat.getString("c"), cat.getInt("a"), cat.getInt("s"));
                                    }
                                }

                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        refreshCategorii();
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

    private void refreshCategorii() {
        this.adapter.changeCursor(dbHelper.getCategorii());
        this.adapter.notifyDataSetChanged();
    }

    private void initCache() {
        final ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final int memoryClassBytes = am.getMemoryClass() * 1024 * 1024;
        this.thumbnailCache = new ThumbnailCache(memoryClassBytes / 2);
    }

    private void initList() {
        this.adapter = new CategorieAdapter(this, this.dbHelper.getCategorii());

        ListView listView = (ListView) findViewById(R.id.categoriiList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
                itemClicked(id);
            }
        });
        listView.setRecyclerListener( new RecyclerListener() {
            @Override
            public void onMovedToScrapHeap(View view) {
                final ImageView imageView = (ImageView) view.findViewById(R.id.imageView);
                imageView.setImageBitmap(null);
            }
        });
    }

    private void cacheStats() {
        Log.d("cache", String.format("{size=%s, hits=%d, miss=%d, evictions=%d}",
                Formatter.formatFileSize(this, thumbnailCache.size()),
                thumbnailCache.hitCount(), thumbnailCache.missCount(),
                thumbnailCache.evictionCount() ));
    }

    //------------------------------------
    //         OTHER CLASSES
    // ------------------------------------

    public static class ThumbnailCache extends LruCache<Long, Bitmap> {
        public ThumbnailCache(int maxSizeBytes) {
            super(maxSizeBytes);
        }

        @Override
        protected int sizeOf(Long key, Bitmap value) {
            return value.getRowBytes() * value.getHeight();
        }
    }

    public class CategorieAdapter extends CursorAdapter {

        public CategorieAdapter(Context context, Cursor c) {
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
            denumire.setTextColor(Color.parseColor("#" + cursor.getString(cursor.getColumnIndex(DB.CATEGORIE.TEXT_COLOR))));
            denumire.setText(cursor.getString(cursor.getColumnIndex(DB.CATEGORIE.NAME)));

            // IMAGINE
            final long pozaId = cursor.getInt(cursor.getColumnIndex(DB.CATEGORIE.ID));
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
            creator.execute(pozaId, (long) cursor.getInt(cursor.getColumnIndex(DB.CATEGORIE.CATEGORIE_ID)));
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
            final long cat_id = longs[1];

            final Cursor query = dbHelper.getPozaCategorie(id);
            final String blob;
            if (query != null) {
                if (query.moveToFirst()) {
                    blob = query.getString(query.getColumnIndex(DB.CATEGORIE.POZA));
                } else {
                    blob = null;
                }
            } else {
                blob = null;
            }

            if (blob == null) {
                Log.d("bitmap", String.format("{cat=%d, bm=null, m='no blob'}", cat_id));
                Log.d("blob", String.valueOf(cat_id));
                return null;
            }

            byte[] decodedByte = new byte[0];
            try {
                decodedByte = Base64.decode(blob.getBytes(), Base64.NO_WRAP);
            } catch (IllegalArgumentException ex) {
                Log.d("blob", String.valueOf(cat_id));
                Log.d("bitmap", String.format("{cat=%d, bm=null, m='no decode', dec=%s}", cat_id, Arrays.toString(decodedByte)));
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
                Log.d("bitmap", String.format("{cat=%d, bm=%s, m='no bitmap' dec=%s}", cat_id, bitmap, Arrays.toString(decodedByte)));
                return null;
            }

            Log.d("bitmap", String.format("{cat=%d, bm=%s}", cat_id, bitmap.toString()));
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


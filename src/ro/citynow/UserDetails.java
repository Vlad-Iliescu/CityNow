package ro.citynow;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class UserDetails extends Activity {
    private Long userId;
    private ProgressBar progressBar;
    private Handler handler;

    private int imageIndex = 0;
    private Timer timer;
    private TimerTask timerTask;
    private ImageView slideImageView;
    private boolean isPaused = false;

    private Details details = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_details);

        Intent intent = getIntent();
        this.userId = intent.getLongExtra("user_id", 0);
        this.progressBar = (ProgressBar) findViewById(R.id.progressBar);
        this.handler = new Handler();

        getDetalii(userId);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.incoming, R.anim.outgoing);
    }

    private void getDetalii(Long user_id) {
        Log.d("usr", String.format("user_id = %d", user_id));
        final String url = "http://www.citynow.ro/m/gu.json";

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
                updateDetalii(serverResponse);
            }
        };

        poster.execute(url, String.format("i=%d", user_id));
    }

    private void updateDetalii(ServerResponse response) {
        Log.d("usr", String.valueOf(response.getResponseCode()));
        if (response.getResponseCode() == 200) {
            final JSONObject jsonObject;
            try {
                jsonObject = response.parseAsJson();
                if (jsonObject != null) {
                    this.details = new Details(this.userId);
                    details.setDenumire(jsonObject.getString("d"));
                    details.setTelefon(jsonObject.getString("t"));
                    details.setWebsite(jsonObject.getString("w"));

                    // adresa
                    JSONObject adresaJson = jsonObject.getJSONObject("a");
                    Address adresa = details.getAddress();
                    adresa.setAdresa(adresaJson.getString("a"));
                    adresa.setZona(adresaJson.getString("z"));
                    adresa.setLatitudine(adresaJson.getString("la"));
                    adresa.setLongitudine(adresaJson.getString("lo"));

                    // galerie
                    JSONArray poze = jsonObject.getJSONArray("g");
                    if (poze.length() > 0) {
                        ArrayList<Picture> pictures = details.getPictures();
                        for (int i=0; i<poze.length(); ++i) {
                            JSONObject poza = poze.getJSONObject(i);
                            pictures.add(new Picture(poza.getInt("i"), poza.getString("d"), poza.getString("p"),
                                    poza.getString("t")));
                        }
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            setUpTimer();
        }
    }

    private void setUpTimer() {
        this.slideImageView = this.details.createLayout(this);
        if (this.slideImageView == null) {
            return;
        }
        LinearLayout layout = (LinearLayout) findViewById(R.id.userLayout);
        layout.addView(this.slideImageView);

        final Runnable updateImage = new Runnable() {
            @Override
            public void run() {
                animateSlideShow();
            }
        };

        final int delay = 1000; // delay for 1 sec.
        final int period = 8000; // repeat every 4 sec.

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                handler.post(updateImage);
            }
        }, delay, period);
    }

    private void animateSlideShow() {
        if (isPaused) {
            return;
        }

        if (details == null) {
            return;
        }
        Bitmap image = details.getImage(imageIndex);
        if (image == null) {
            return;
        }

        this.imageIndex = (imageIndex + 1) % details.getImageCount();

        this.slideImageView.setImageBitmap(image);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.incoming);
        this.slideImageView.startAnimation(animation);
    }

    private void onImageClick(ArrayList<Picture> pictures) {
        Log.d("pictures", String.valueOf(imageIndex));

    }

    //------------------------------------
    //         OTHER CLASSES
    // ------------------------------------

    class Picture {
        private Integer id;
        private String denumire;
        private String urlPic;
        private String urlThumb;
        private Bitmap pic;
        private Bitmap thumb;

        Picture(Integer id, String denumire, String urlPic, String urlThumb) {
            this.id = id;
            this.denumire = denumire;
            this.urlPic = urlPic;
            this.urlThumb = urlThumb;

            new ThumbnailAsyncTask().execute(urlThumb);
        }

        public Drawable getThumbFromUrl() {
            try {
                InputStream is = (InputStream) new URL(urlThumb).getContent();
                return Drawable.createFromStream(is, "CityNow");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        Bitmap getThumb() {
            return thumb;
        }

        public class ThumbnailAsyncTask extends AsyncTask<String, Void, Bitmap> {

            @Override
            protected Bitmap doInBackground(String... strings) {
                final String url = strings[0];
                try {
                    return BitmapFactory.decodeStream((InputStream)new URL(url).getContent());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                thumb = bitmap;
            }
        }
    }

    class Address {
        private String adresa;
        private String latitudine;
        private String longitudine;
        private String zona;

        Address(String adresa, String zona, String latitudine, String longitudine) {
            this.adresa = adresa;
            this.latitudine = latitudine;
            this.longitudine = longitudine;
            this.zona = zona;
        }

        Address() {}

        String getAdresa() {
            return adresa;
        }

        void setAdresa(String adresa) {
            this.adresa = adresa;
        }

        String getLatitudine() {
            return latitudine;
        }

        void setLatitudine(String latitudine) {
            this.latitudine = latitudine;
        }

        String getLongitudine() {
            return longitudine;
        }

        void setLongitudine(String longitudine) {
            this.longitudine = longitudine;
        }

        String getZona() {
            return zona;
        }

        void setZona(String zona) {
            this.zona = zona;
        }
    }

    class Details {
        private Long id;
        private String denumire;
        private ArrayList<Picture> pictures = new ArrayList<Picture>();
        private Address address = new Address();
        private String telefon;
        private String website;

        Details(Long id) {
            this.id = id;
        }

        String getDenumire() {
            return denumire;
        }

        void setDenumire(String denumire) {
            this.denumire = denumire;
        }

        String getTelefon() {
            return telefon;
        }

        void setTelefon(String telefon) {
            this.telefon = telefon;
        }

        String getWebsite() {
            return website;
        }

        void setWebsite(String website) {
            this.website = website;
        }

        Address getAddress() {
            return address;
        }

        ArrayList<Picture> getPictures() {
            return pictures;
        }

        public ImageView createLayout(Context context) {
            if (pictures.size() == 0) {
                return null;
            }
            ImageView imageView = new ImageView(context);
            imageView.setLayoutParams(
                    new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, 200));
            imageView.setBackgroundResource(R.drawable.loading);
            imageView.setAdjustViewBounds(true);

//            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onImageClick(pictures);
                }
            });

            return imageView;
        }

        public Bitmap getImage(int i) {
            if (i > pictures.size()) {
                return null;
            }
            return pictures.get(i).getThumb();
        }

        public int getImageCount() {
            return this.pictures.size();
        }
    }
}
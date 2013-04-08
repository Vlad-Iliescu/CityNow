package ro.citynow;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;

import java.util.ArrayList;

public class UserDetails extends Activity {
    private Long userId;
    private ProgressBar progressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_details);

        Intent intent = getIntent();
        this.userId = intent.getLongExtra("user_id", 0);
        this.progressBar = (ProgressBar) findViewById(R.id.progressBar);

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
    }

    //------------------------------------
    //         OTHER CLASSES
    // ------------------------------------

    class Picture {
        private Integer id;
        private String denumire;
        private String urlPic;
        private String ulrThumb;
        private Bitmap pic;
        private Bitmap thumb;

        Picture(Integer id, String denumire, String urlPic, String ulrThumb) {
            this.id = id;
            this.denumire = denumire;
            this.urlPic = urlPic;
            this.ulrThumb = ulrThumb;
        }
    }

    class Address {
        private String adresa;
        private String latitudine;
        private String longitudine;

        Address(String adresa, String latitudine, String longitudine) {
            this.adresa = adresa;
            this.latitudine = latitudine;
            this.longitudine = longitudine;
        }
    }

    class Details {
        private Integer id;
        private String denumire;
        private ArrayList<Picture> userList = new ArrayList<Picture>();
        private Address address;
        private String telefon;
        private String website;


    }

}
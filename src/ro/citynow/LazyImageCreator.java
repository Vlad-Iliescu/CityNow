package ro.citynow;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;

public class LazyImageCreator extends AsyncTask<Long, Void, Bitmap> {
    private ImageView imageView;


    public LazyImageCreator(ImageView target) {
        this.imageView = target;
    }

    @Override
    protected void onPreExecute() {
        imageView.setTag(this);
    }

    @Override
    protected Bitmap doInBackground(Long... longs) {
        long id = longs[0];
        final DBHelper dbHelper = new DBHelper(null);

        Log.d("decode", String.valueOf(id));

//        final Cursor query = dbHelper.getPozaCategorie(id);
//
//        final String blob;
//        if (query != null) {
//            if (query.moveToFirst()) {
//                blob = query.getString(query.getColumnIndex(DB.CATEGORIE.POZA));
//            } else {
//                blob = null;
//            }
//        } else {
//            blob = null;
//        }
//
//        Log.d("decode", blob);
//        String blob = strings[0];
//
//        if (blob == null) {
//            return null;
//        }

//        byte[] decodedByte = Base64.decode(blob, 0);
//        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
        return null;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        Log.d("decode", String.valueOf(bitmap));
        Log.d("decode", String.valueOf(this.imageView));
        if (bitmap != null && this.imageView != null) {
            this.imageView.setImageBitmap(bitmap);
        }
    }
}

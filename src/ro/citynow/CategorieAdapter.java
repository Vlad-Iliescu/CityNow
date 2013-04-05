package ro.citynow;

import android.content.Context;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CategorieAdapter extends CursorAdapter {

    public CategorieAdapter(Context context, Cursor c) {
        super(context, c);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.categorie_item, viewGroup, false);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        final ImageView imageView = (ImageView) view.findViewById(R.id.imageView);
        final long pozaId = cursor.getInt(0);

        final LazyImageCreator oldCreator = (LazyImageCreator) imageView.getTag();
        if (oldCreator != null) {
            oldCreator.cancel(false);
        }

        final LazyImageCreator creator = new LazyImageCreator(imageView);
        imageView.setImageBitmap(null);
        imageView.setTag(creator);
//        creator.execute(pozaId);

//        imageView.setImageBitmap(makeBitmap(cursor.getString(3)));

        Log.d("decode", String.valueOf(pozaId));

        TextView denumire = (TextView) view.findViewById(R.id.denumire);
        denumire.setTextColor(Color.parseColor("#"+cursor.getString(2)));
        denumire.setText(cursor.getString(1));
    }

    private Bitmap makeBitmap(String blob) {
        if (blob == null) {
            return null;
        }

        byte[] decodedByte = Base64.decode(blob, 0);
        Bitmap poza = BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
        return poza;
    }
}


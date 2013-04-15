package ro.citynow;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.util.LruCache;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

public class PicturesGallery extends FragmentActivity {
    private int imageIndex = 0;

    private ArrayList<String> urls = new ArrayList<String>();

    private ViewPager mPager;
    private PageIndicator mIndicator;
    private PictureFragmentAdapter mAdapter;

    private PictureCache pictureCache;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.picture_gallery);

        initCache();

        Intent intent = getIntent();
        imageIndex = intent.getIntExtra("index", 0);
        urls = intent.getStringArrayListExtra("urls");

        mAdapter = new PictureFragmentAdapter(getSupportFragmentManager());

        mPager = (ViewPager)findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);

        mIndicator = (LinePageIndicator)findViewById(R.id.indicator);
        mIndicator.setViewPager(mPager);
//        mIndicator.setCurrentItem(imageIndex);

    }

    private void initCache() {
        final ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final int memoryClassBytes = am.getMemoryClass() * 1024 * 1024;
        this.pictureCache = new PictureCache(memoryClassBytes / 2);
    }

    private void initPicturesDownload() {

    }

    public class PictureFragmentAdapter extends FragmentPagerAdapter {

        public PictureFragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Log.d("index", String.format("{i: %d, imageIndex: %d}", i, i % urls.size()));
            imageIndex = i % urls.size();
            Fragment fragment = new PictureFragment();
            fragment.setRetainInstance(true);
            return fragment;
        }

        @Override
        public int getCount() {
            return urls.size();
        }
    }

    public class PictureFragment extends Fragment {
        private String url;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            url = urls.get(imageIndex);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            ImageView imageView = new ImageView(getActivity());
//            imageView.setBackgroundColor(Color.BLACK);

            imageView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

            final Bitmap cachedResult = pictureCache.get(url);
            if (cachedResult != null) {
                imageView.setImageBitmap(cachedResult);
            } else {
                final PictureAsyncTask creator = new PictureAsyncTask(imageView);
                imageView.setImageBitmap(null);
                creator.execute(url);
            }

            LinearLayout linearLayout = new LinearLayout(getActivity());
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            linearLayout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

//            linearLayout.setBackgroundColor(Color.BLUE);
//            linearLayout.setGravity(Gravity.CENTER);

            linearLayout.addView(imageView);

            return linearLayout;
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
        }
    }

    public static class PictureCache extends LruCache<String, Bitmap> {
        public PictureCache(int maxSizeBytes) {
            super(maxSizeBytes);
        }

        @Override
        protected int sizeOf(String key, Bitmap value) {
            return value.getRowBytes() * value.getHeight();
        }
    }

    public class PictureAsyncTask extends AsyncTask<String, Void, Bitmap> {
        private ImageView imageView;

        public PictureAsyncTask(ImageView imageView) {
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(String... strings) {
            final String url = strings[0];
            Bitmap bitmap = null;

            try {
                bitmap =  BitmapFactory.decodeStream((InputStream) new URL(url).getContent());
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (bitmap != null) {
                pictureCache.put(url, bitmap);
            }

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            imageView.setImageBitmap(bitmap);
        }
    }
}
package ro.citynow;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.*;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.util.LruCache;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.Log;
import android.view.*;
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
    private LinePageIndicator mIndicator;
    private PictureFragmentAdapter mAdapter;

    private static PictureCache pictureCache;

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
        mPager.setCurrentItem(imageIndex - 1);

        mIndicator = (LinePageIndicator)findViewById(R.id.indicator);
        mIndicator.setViewPager(mPager);
        mIndicator.setCurrentItem(imageIndex - 1);
        mIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {}

            @Override
            public void onPageSelected(int i) {
//                if (i >= 0) {
//                    View view = mPager.getChildAt(mPager.getCurrentItem());
//                    PictureFragment.ZoomImageView zoomImageView = (PictureFragment.ZoomImageView) view.findViewById(1124);
//                    zoomImageView.resetView();
//                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {}
        });
    }

    private void initCache() {
        final ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final int memoryClassBytes = am.getMemoryClass() * 1024 * 1024;
        pictureCache = new PictureCache(memoryClassBytes / 2);
    }

    public class PictureFragmentAdapter extends FragmentPagerAdapter {
        private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

        public PictureFragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Log.d("index", String.format("{i: %d, imageIndex: %d}", i, mPager.getCurrentItem()));

            Fragment fragment = PictureFragment.newInstance(urls.get(i % urls.size()));
            fragment.setRetainInstance(true);
            return fragment;
        }

        @Override
        public int getCount() {
            return urls.size();
        }
    }

    public static class PictureFragment extends Fragment {
        private String url;

        public static PictureFragment newInstance(String url) {
            PictureFragment fragment = new PictureFragment();
            fragment.url = url;
            return fragment;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            final ZoomImageView imageView = new ZoomImageView(getActivity());
            imageView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
            imageView.setId(1124);

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

            linearLayout.addView(imageView);
            return linearLayout;
        }

        public class ZoomImageView extends ImageView implements View.OnTouchListener {
            static final int MAX_SCALE_FACTOR = 2;
            static final int NONE = 0;
            static final int DRAG = 1;
            static final int ZOOM = 2;
            int mCurrentMode = NONE;

            private Matrix mViewMatrix = new Matrix();
            private Matrix mCurSavedMatrix = new Matrix();

            private PointF start = new PointF();
            private PointF mCurMidPoint = new PointF();
            private float mOldDist = 1f;

            private Matrix mMinScaleMatrix;
            private float mMinScale;
            private float mMaxScale;
            float[] mTmpValues = new float[9];
            private boolean mWasScaleTypeSet;

            public ZoomImageView(Context context) {
                super(context);
                this.setOnTouchListener(this);
            }

            @SuppressWarnings("unused")
            public ZoomImageView(Context context, AttributeSet attrs) {
                super(context, attrs);
                this.setOnTouchListener(this);
            }

            @SuppressWarnings("unused")
            public ZoomImageView(Context context, AttributeSet attrs, int defStyle) {
                super(context, attrs, defStyle);
                this.setOnTouchListener(this);
            }

            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                mViewMatrix = new Matrix(this.getImageMatrix());
                mMinScaleMatrix = new Matrix(mViewMatrix);

                float initialScale = getMatrixScale(mViewMatrix);

                if (initialScale < 1.0f) // Image is bigger than screen
                    mMaxScale = MAX_SCALE_FACTOR;
                else
                    mMaxScale = MAX_SCALE_FACTOR * initialScale;

                mMinScale = getMatrixScale(mMinScaleMatrix);
            }

            private float getMatrixScale(Matrix matrix) {
                matrix.getValues(mTmpValues);
                return mTmpValues[Matrix.MSCALE_X];
            }

            private void downscaleMatrix(float scale, Matrix dist) {
                float resScale = mMaxScale / scale;
                dist.postScale(resScale, resScale, mCurMidPoint.x, mCurMidPoint.y);
            }

            private float spacing(MotionEvent event) {
                float x = event.getX(0) - event.getX(1);
                float y = event.getY(0) - event.getY(1);
                return FloatMath.sqrt(x * x + y * y);
            }

            private void midPoint(PointF point, MotionEvent event) {
                float x = event.getX(0) + event.getX(1);
                float y = event.getY(0) + event.getY(1);
                point.set(x / 2, y / 2);
            }

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ImageView view = (ImageView) v;

                if(!mWasScaleTypeSet) {
                    view.setScaleType(ImageView.ScaleType.MATRIX);
                    mWasScaleTypeSet = true;
                }

                float scale;
//                dumpEvent(event);

                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN: // first finger down only
                        mCurSavedMatrix.set(mViewMatrix);
                        start.set(event.getX(), event.getY());
                        mCurrentMode = DRAG;
                        break;

                    case MotionEvent.ACTION_UP: // first finger lifted
                    case MotionEvent.ACTION_POINTER_UP: // second finger lifted
                        mCurrentMode = NONE;

                        float resScale = getMatrixScale(mViewMatrix);

                        if (resScale > mMaxScale) {
                            downscaleMatrix(resScale, mViewMatrix);
                        } else if (resScale < mMinScale) {
                            mViewMatrix = new Matrix(mMinScaleMatrix);
                        } else if ((resScale - mMinScale) < 0.1f) {
                        // Don't allow user to drag picture outside in case of FIT TO WINDOW zoom
                            mViewMatrix = new Matrix(mMinScaleMatrix);
                        } else {
                            break;
                        }
                        break;

                    case MotionEvent.ACTION_POINTER_DOWN: // first and second finger down
                        mOldDist = spacing(event);
                        if (mOldDist > 5f) {
                            mCurSavedMatrix.set(mViewMatrix);
                            midPoint(mCurMidPoint, event);
                            mCurrentMode = ZOOM;
                        }
                        break;

                    case MotionEvent.ACTION_MOVE:
                        if (mCurrentMode == DRAG) {
                            mViewMatrix.set(mCurSavedMatrix);
                            mViewMatrix.postTranslate(event.getX() - start.x, event.getY() - start.y);
                            // create the transformation in the matrix  of points
                        } else if (mCurrentMode == ZOOM) {
                            // pinch zooming
                            float newDist = spacing(event);
                            if (newDist > 1.f) {
                                mViewMatrix.set(mCurSavedMatrix);
                                scale = newDist / mOldDist; // setting the scaling of the
                                // matrix...if scale > 1 means
                                // zoom in...if scale < 1 means
                                // zoom out
                                mViewMatrix.postScale(scale, scale, mCurMidPoint.x, mCurMidPoint.y);
                            }
                        }
                        break;
                }

                view.setImageMatrix(mViewMatrix); // display the transformation on screen
                return true; // indicate event was handled
            }

            public void resetView() {
                mViewMatrix.reset();
                this.setImageMatrix(mViewMatrix);
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

    public static class PictureCache extends LruCache<String, Bitmap> {
        public PictureCache(int maxSizeBytes) {
            super(maxSizeBytes);
        }

        @Override
        protected int sizeOf(String key, Bitmap value) {
            return value.getRowBytes() * value.getHeight();
        }
    }

}
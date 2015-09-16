package hanks.com.mylibrary.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.LruCache;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * Created by Hanks on 2015/9/16.
 */
public class ImageLoader {

    private static ImageLoader mInstance;

    //lru
    private LruCache<String, Bitmap> mLruCache;

    //Thread pool
    private ExecutorService mThreadPool;
    private static final int DEFAULT_THREAD_COUNT = 1;

    //Task queue type
    private static final int TYPE_FIFO = 0;
    private static final int TYPE_LIFO = 1;
    private static int mType = TYPE_FIFO;

    private LinkedList<Runnable> mTaskQueue;

    private Thread mPoolThread;
    private Handler mPoolThreadHandler;

    private Handler mUIHandler;

    private Semaphore mSemaphorePoolThreadHandler = new Semaphore(0);

    private ImageLoader(int mThreadCount, int type) {
        init(mThreadCount, type);
    }

    private void init(int threadCount, int type) {

        //background task
        mPoolThread = new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                mPoolThreadHandler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        mThreadPool.execute(getTask());
                    }
                };
                mSemaphorePoolThreadHandler.release();
                Looper.loop();
            }
        };
        mPoolThread.start();

        //LRU
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheMemory = maxMemory / 8;
        mLruCache = new LruCache<String, Bitmap>(cacheMemory) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight();
            }
        };


        //ThreadPool
        mThreadPool = Executors.newFixedThreadPool(threadCount);
        mTaskQueue = new LinkedList<>();
        mType = type;
    }

    private Runnable getTask() {
        if (mType == TYPE_FIFO) {
            return mTaskQueue.removeFirst();
        } else {
            return mTaskQueue.removeLast();
        }
    }

    public static ImageLoader getInstance() {
        if (mInstance == null) {
            synchronized (ImageLoader.class) {
                if (mInstance == null) {
                    mInstance = new ImageLoader(DEFAULT_THREAD_COUNT, mType);
                }
            }
        }
        return mInstance;
    }


    public void loadImage(final String path, final ImageView imageView) {
        imageView.setTag(path);
        if (mUIHandler == null) {
            mUIHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    //set bitmap
                    ImgHolder imgHolder = (ImgHolder) msg.obj;
                    if (imgHolder.imageView.getTag().toString().equals(imgHolder.path)) {
                        imgHolder.imageView.setImageBitmap(imgHolder.bitmap);
                    }
                }
            };
        }

        Bitmap bm = getBitmapFromLruCache(path);
        if (bm != null) {
            refreshImageView(path, imageView, bm);
        } else {
            addTask(new Runnable() {
                @Override
                public void run() {
                    //resize image and load image
                    int[] imageSize = new int[2];
                    getImageViewSize(imageView, imageSize);

                    //resize bitmap
                    Bitmap bitmap = getResizeBitmap(path, imageSize);

                    //cacheBitmap
                    cacheBitmap(path,bitmap);

                    //show bitmap
                    refreshImageView(path, imageView, bitmap);
                }
            });
        }
    }

    private void cacheBitmap(String path, Bitmap bitmap) {
        if(mLruCache.get(path) ==null){
            mLruCache.put(path,bitmap);
        }
    }

    private void refreshImageView(String path, ImageView imageView, Bitmap bm) {
        Message message = Message.obtain();
        ImgHolder imgHolder = new ImgHolder();
        imgHolder.bitmap = bm;
        imgHolder.imageView = imageView;
        imgHolder.path = path;
        message.obj = imgHolder;
        mUIHandler.sendMessage(message);
    }

    private Bitmap getResizeBitmap(String path, int[] imageSize) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true; // only get bitmap infomation
        options.inSampleSize = calcSampleSize(options, imageSize);
        BitmapFactory.decodeFile(path, options);

        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    private int calcSampleSize(BitmapFactory.Options options, int[] imageSize) {
        int inSampleSize = 1;
        if (imageSize[0] < options.outWidth || imageSize[1] < options.outHeight) {
            int widthRadio = Math.round(options.outWidth * 1.0f / imageSize[0]);
            int heightRadio = Math.round(options.outWidth * 1.0f / imageSize[0]);
            inSampleSize = Math.min(widthRadio, heightRadio);
        }
        return inSampleSize;
    }

    private void getImageViewSize(ImageView imageView, int[] imageSize) {
        ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
        imageSize[0] = imageView.getWidth();
        if (imageSize[0] <= 0) {
            imageSize[0] = layoutParams.width;
        }

        if (imageSize[0] <= 0) {
            imageSize[0] = imageView.getMaxWidth();
        }

        if (imageSize[0] <= 0) {
            DisplayMetrics displayMetrics = imageView.getContext().getResources().getDisplayMetrics();
            imageSize[0] = displayMetrics.widthPixels;
        }

        imageSize[1] = imageView.getHeight();
        if (imageSize[1] <= 0) {
            imageSize[1] = layoutParams.height;
        }

        if (imageSize[1] <= 0) {
            imageSize[1] = imageView.getMaxHeight();
        }

        if (imageSize[1] <= 0) {
            DisplayMetrics displayMetrics = imageView.getContext().getResources().getDisplayMetrics();
            imageSize[1] = displayMetrics.heightPixels;
        }

    }

    private synchronized void addTask(Runnable runnable) {
        mTaskQueue.add(runnable);
        try {
            if (mPoolThreadHandler == null) {
                mSemaphorePoolThreadHandler.acquire();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mPoolThreadHandler.sendEmptyMessage(0x400);
    }

    private Bitmap getBitmapFromLruCache(String path) {
        return mLruCache.get(path);
    }

    private class ImgHolder {
        Bitmap bitmap;
        ImageView imageView;
        String path;
    }
}

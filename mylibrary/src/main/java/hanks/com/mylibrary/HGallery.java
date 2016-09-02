package hanks.com.mylibrary;

import android.content.Context;
import android.content.Intent;

import hanks.com.mylibrary.base.DefalutImageLoader;
import hanks.com.mylibrary.base.HImageLoader;

/**
 * weibo gallery
 * Created by hanks on 16/9/2.
 */
public class HGallery {

    private static HImageLoader sImageLoader;

    public static HImageLoader getImageLoader(Context context) {
        if (sImageLoader == null) {
            synchronized (HGallery.class) {
                if (sImageLoader == null) {
                    sImageLoader = new DefalutImageLoader();
                }
            }
        }
        return sImageLoader;
    }

    public static void init(Context context, HImageLoader imageLoader) {
        sImageLoader = imageLoader;
        init(context);
    }

    public static void init(Context context) {
    }

    public static void start(Context context){
        Intent intent = new Intent(context, GridImageActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}

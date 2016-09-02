package hanks.com.mylibrary.base;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.DrawableRes;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by hanks on 16/9/2.
 */
public interface HImageLoader {

    void displayImage(ImageView imageView, String path, @DrawableRes int loadingResId, @DrawableRes int failResId, int width, int height, DisplayImageListener displayImageListener);

    void displayImage(ImageView imageView, String path);

    void downloadImage(Context context, String path, DownloadImageListener downloadImageListener);

    interface DisplayImageListener {
        void onSuccess(View view, String path);
    }

    interface DownloadImageListener {
        void onSuccess(String path, Bitmap bitmap);

        void onFailed(String path);
    }
}

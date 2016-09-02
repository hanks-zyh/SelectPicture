package hanks.com.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;

import hanks.com.mylibrary.base.HImageLoader;

/**
 * Created by hanks on 16/9/2.
 */
public class GlideImageLoader implements HImageLoader {

    @Override
    public void displayImage(final ImageView imageView, String path, @DrawableRes int loadingResId, @DrawableRes int failResId, int width, int height, final DisplayImageListener displayImageListener) {
        if (path == null) {
            path = "";
        }

        if (!path.startsWith("http://") && !path.startsWith("https://") && !path.startsWith("file://")) {
            path = "file://" + path;
        }

        final String finalPath = path;
        Glide.with(imageView.getContext()).load(finalPath).asBitmap().placeholder(loadingResId).error(failResId).override(width, height).listener(new RequestListener<String, Bitmap>() {
            @Override
            public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                if (displayImageListener != null) {
                    displayImageListener.onSuccess(imageView, finalPath);
                }
                return false;
            }
        }).into(imageView);
    }

    @Override
    public void displayImage(ImageView imageView, String path) {
        if (path == null) {
            path = "";
        }
        if (!path.startsWith("http://") && !path.startsWith("https://") && !path.startsWith("file://")) {
            path = "file://" + path;
        }
        final String finalPath = path;
        Glide.with(imageView.getContext()).load(finalPath).into(imageView);
    }

    @Override
    public void downloadImage(Context context, String path, final DownloadImageListener downloadImageListener) {
        if (path == null) {
            path = "";
        }
        if (!path.startsWith("http://") && !path.startsWith("https://") && !path.startsWith("file://")) {
            path = "file://" + path;
        }

        final String finalPath = path;
        Glide.with(context).load(finalPath).asBitmap().into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                if (downloadImageListener != null) {
                    downloadImageListener.onSuccess(finalPath, resource);
                }
            }

            @Override
            public void onLoadFailed(Exception e, Drawable errorDrawable) {
                if (downloadImageListener != null) {
                    downloadImageListener.onFailed(finalPath);
                }
            }
        });
    }
}
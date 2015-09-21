package hanks.com.mylibrary.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.lang.reflect.Field;

/**
 * Created by Hanks on 2015/9/17.
 */
public class ImageUtils {
    public static Bitmap getResizeBitmap(String path, int[] imageSize) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true; // only get bitmap infomation
        BitmapFactory.decodeFile(path, options);
        options.inSampleSize = calcSampleSize(options, imageSize);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    private static int calcSampleSize(BitmapFactory.Options options, int[] imageSize) {
        Log.i("", options.outWidth + "," + imageSize[0] + "\n" + options.outHeight + "," + imageSize[1] + "\n------------\n");
        int inSampleSize = 1;
        if (imageSize[0] < options.outWidth || imageSize[1] < options.outHeight) {
            int widthRadio = Math.round(options.outWidth * 1.0f / imageSize[0]);
            int heightRadio = Math.round(options.outHeight * 1.0f / imageSize[1]);
            inSampleSize = Math.min(widthRadio, heightRadio);
        }
        return inSampleSize;
    }


    public static void getImageViewSize(ImageView imageView, int[] imageSize) {
        ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
        imageSize[0] = imageView.getWidth();
        if (imageSize[0] <= 0) {
            imageSize[0] = layoutParams.width;
        }

        if (imageSize[0] <= 0) {
            imageSize[0] = getImageViewField(imageView, "mMaxWidth");
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
            imageSize[1] = getImageViewField(imageView, "mMaxHeight");
        }

        if (imageSize[1] <= 0) {
            DisplayMetrics displayMetrics = imageView.getContext().getResources().getDisplayMetrics();
            imageSize[1] = displayMetrics.heightPixels;
        }

    }

    private static int getImageViewField(Object object, String fieldName) {
        int value = 0;
        try {
            Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);

            int fieldValue = field.getInt(object);
            if (fieldValue > 0 && fieldValue < Integer.MAX_VALUE) {
                value = fieldValue;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    public static void setImageBitmap(ImageView imageView, String imagePath) {
        //resize image and load image
        int[] imageSize = new int[2];
        getImageViewSize(imageView, imageSize);
        //resize bitmap
        Bitmap bitmap = ImageUtils.getResizeBitmap(imagePath, imageSize);
        imageView.setImageBitmap(bitmap);
    }
}

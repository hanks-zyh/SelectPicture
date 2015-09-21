package hanks.com.mylibrary;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import hanks.com.mylibrary.util.ImageUtils;

/**
 * Created by Hanks on 2015/9/21.
 */
public class CropImageView extends View {
    private Paint cropPaint;
    private Paint layerPaint;
    private Rect cropRect;
    private int viewWidth, viewHeight;

    private float mScaleSize = 1f;
    private Bitmap mBitmap;
    private Matrix mMatrix;
    private int viewSize;
    private float dx,dy;

    public CropImageView(Context context) {
        super(context);
        setup(context);
    }

    public CropImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup(context);
    }

    public CropImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CropImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setup(context);
    }

    private void setup(Context context) {

        cropPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cropPaint.setColor(Color.WHITE);
        cropPaint.setAlpha(0);

        layerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        layerPaint.setColor(Color.WHITE);
        layerPaint.setAlpha(200);

        cropRect = new Rect();


        mMatrix = new Matrix();

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        cropRect.set(0, (h - w) / 2, 0, w + (h - w) / 2);
        viewWidth = w;
        viewHeight = h;
        viewSize = viewWidth;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mBitmap == null) return;
        canvas.drawBitmap(mBitmap, mMatrix, layerPaint);
        canvas.drawRect(0, 0, viewWidth, (viewHeight - viewWidth) / 2, layerPaint);
        canvas.drawRect(cropRect, cropPaint);
        canvas.drawRect(0, (viewHeight - viewWidth) / 2 + viewWidth, viewWidth, viewHeight, layerPaint);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return handleTouchEvent(event);
    }

    private boolean handleTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                showL("down");
                dx = event.getX();
                dy = event.getY();
                break;

            case MotionEvent.ACTION_MOVE:
                showL("move");
                mMatrix.postTranslate(event.getX()-dx,event.getY()-dy);
                dx = event.getX();
                dy = event.getY();
                invalidate();
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                showL("up");
                break;
        }
        return true;
    }

    public void setImagePath(String imagePath) {
        int[] imageSize = new int[2];
        imageSize[0] = viewWidth;
        imageSize[1] = viewHeight;
        mBitmap = ImageUtils.getResizeBitmap(imagePath, imageSize);

        calcMatrix();
        invalidate();
    }

    private void calcMatrix() {
        if (mBitmap == null) return;
        int w = mBitmap.getWidth();
        int h = mBitmap.getHeight();
        float scale = 1f;
        if (w > viewSize) {
            if (h > viewSize) {
                scale = 1f / Math.min(w * 1f / viewSize, h * 1f / viewSize);
            } else {
                scale = viewSize * 1f / h;
            }
        } else {
            if (h > viewSize) {
                scale = viewSize * 1f / w;
            } else {
                scale = Math.max(viewSize * 1f / w, viewSize * 1f / h);
            }
        }

        showL(scale+"-------------------------------");
        mMatrix.reset();
        mMatrix.setScale(scale, scale);
        mMatrix.postTranslate(viewWidth / 2 - w*scale/2, viewHeight / 2-h*scale/2);
    }

    public void showL(String string){
        Log.i("",string);
    }
}

package hanks.com.mylibrary;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import hanks.com.mylibrary.util.ImageUtils;

/**
 * Created by Hanks on 2015/9/21.
 */
public class CropImageView extends View {
    private Paint cropPaint;
    private Paint layerPaint;
    private Paint bmPaint;

    private Rect cropRect;
    private int viewWidth, viewHeight;

    private float mScaleSize = 1f;
    private Bitmap mBitmap;
    private Matrix mMatrix;
    private int viewSize;
    private float dx, dy;

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

        bmPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bmPaint.setColor(Color.WHITE);

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


        canvas.drawBitmap(mBitmap, mMatrix, bmPaint);
        canvas.drawRect(0, 0, viewWidth, (viewHeight - viewWidth) / 2, layerPaint);
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
                int tempx = (int) (event.getX() - dx);
                int tempy = (int) (event.getY() - dy);
                totalLeft += tempx;
                totalRight += tempx;

                totalBottom += tempy;
                totalTop += tempy;

                showL("left:" + totalLeft + ",right:" + totalRight+ ",top:" + totalTop+ ",bottom:" + totalBottom);
                if (!bitmapInBound()) {
                    //检测到要超出边界， 回退
                    totalLeft -= tempx;
                    totalRight -= tempx;
                    tempx = 0;
                }

                mMatrix.postTranslate(tempx, tempy);
                dx = event.getX();
                dy = event.getY();
                invalidate();
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:

                //手指抬起
                int y= 0;
                if(totalTop>0){
                    y = -totalTop;
                    totalBottom -= totalTop;
                    totalTop = 0;

                }
                if(totalBottom<0){
                    y = -totalBottom;
                    totalTop -= totalBottom;
                    totalBottom = 0;
                }
                mMatrix.postTranslate(0, y);
                showL("up");
                invalidate();
                break;
        }
        return true;
    }

    int totalLeft = 0;
    int totalRight = 0;
    int totalTop = 0;
    int totalBottom = 0;

    private boolean bitmapInBound() {
        return totalRight >= 0 && totalLeft <= 0  ;
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

        showL(scale + "-------------------------------");
        mMatrix.reset();
        mMatrix.setScale(scale, scale);
        mMatrix.postTranslate(viewWidth / 2 - w * scale / 2, viewHeight / 2 - h * scale / 2);

        //计算左右可以滑动的距离
        totalLeft = (int) (-(w * scale - viewSize) / 2);
        totalRight = (int) ((w * scale - viewSize) / 2);
        totalTop = (int) (-(h * scale - viewSize) / 2);
        totalBottom = (int) ((h * scale - viewSize) / 2);
        showL("**left:" + totalLeft + "right:" + totalRight);
    }

    public void showL(String string) {
        Log.i("", string);
    }
}

package hanks.com.mylibrary;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.util.FloatMath;
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

    private PointF mid;
    private int MODE_DRAG = 0;
    private int MODE_ZOOM = 1;
    private int MODE_NONE = 2;
    private int mode;
    private float oldDist;
    private float maxZoom;

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

        mid = new PointF();


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

        canvas.save();
        canvas.drawBitmap(mBitmap, mMatrix, bmPaint);
        canvas.drawRect(0, 0, viewWidth, (viewHeight - viewWidth) / 2, layerPaint);
        canvas.drawRect(0, (viewHeight - viewWidth) / 2 + viewWidth, viewWidth, viewHeight, layerPaint);
        canvas.restore();


    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return handleTouchEvent(event);
    }

    private boolean handleTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            //一个手指按下
            case MotionEvent.ACTION_DOWN:
                showL("down");
                mode = MODE_DRAG;
                dx = event.getX();
                dy = event.getY();
                break;
            //其他手指按下
            case MotionEvent.ACTION_POINTER_DOWN:
                mode = MODE_ZOOM;
                oldDist = spacing(event);
                midPoint(mid, event);
                break;

            case MotionEvent.ACTION_POINTER_UP:
                mode = MODE_ZOOM;
                oldDist = spacing(event);
                midPoint(mid, event);
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == MODE_DRAG) {
                    showL("move");
                    int tempx = (int) (event.getX() - dx);
                    int tempy = (int) (event.getY() - dy);
                    totalLeft += tempx;
                    totalRight += tempx;

                    totalBottom += tempy;
                    totalTop += tempy;

                    showL("left:" + totalLeft + ",right:" + totalRight + ",top:" + totalTop + ",bottom:" + totalBottom);
                    if (!bitmapInBound(tempx)) {
                        //检测到要超出边界， 回退
                        totalLeft -= tempx;
                        totalRight -= tempx;
                        tempx = 0;
                    }

                    mMatrix.postTranslate(tempx, tempy);
                    dx = event.getX();
                    dy = event.getY();
                    invalidate();
                } else if (mode == MODE_ZOOM) {
                    float newDist = spacing(event);
                    showL("oldDis:" + oldDist + ",newDIs:" + newDist);
                    showL("width:" + mBitmap.getWidth() * getScale(mMatrix) + ",height:" + mBitmap.getHeight() * getScale(mMatrix));

                    zoomTo(newDist, mid.x, mid.y);// 縮放
                    oldDist = spacing(event);

                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mode = MODE_NONE;
                //手指抬起
                int y = 0;
                if (totalTop > 0) {
                    y = -totalTop;
                    totalBottom -= totalTop;
                    totalTop = 0;

                }
                if (totalBottom < 0) {
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


    // 触碰两点间距离
    private float spacing(MotionEvent event) {
        float result = 0;
        if (event.getPointerCount() >= 2) {
            float x = event.getX(0) - event.getX(1);
            float y = event.getY(0) - event.getY(1);
            result = (float) Math.sqrt(x * x + y * y);
        }
        return result;
    }


    // 取手势中心点
    private void midPoint(PointF point, MotionEvent event) {
        if (event.getPointerCount() >= 2) {
            float x = event.getX(0) + event.getX(1);
            float y = event.getY(0) + event.getY(1);
            point.set(x / 2, y / 2);
        }
    }


    protected void zoomTo(float newDis, float centerX, float centerY) {
        if (newDis > 0) {
            float scale = 1;
            if (newDis > oldDist) { //放大
                scale = (newDis - oldDist) / dp2px(512) + 1;
            } else {//缩小
                scale = 1 - (oldDist - newDis) / dp2px(512);
            }

            showL("scale:" + scale);
//        scale:0~5

            if (bitmapInBound(scale,centerX,centerY)) {
                mMatrix.postScale(scale, scale, centerX, centerY);
                invalidate();
            }


        }
    }


    private int dp2px(int dpValue) {
        return (int) (dpValue * (getResources().getDisplayMetrics().densityDpi / 160f) + 0.5f);
    }

    protected float getScale(Matrix matrix) {
        return getValue(matrix, Matrix.MSCALE_X);
    }

    protected float getValue(Matrix matrix, int whichValue) {
        matrix.getValues(matrixValues);
        return matrixValues[whichValue];
    }

    protected float calculateMaxZoom() {
        if (mBitmap == null) {
            return 1F;
        }
        float fw = (float) mBitmap.getWidth() / (float) viewWidth;
        float fh = (float) mBitmap.getHeight() / (float) viewHeight;
        return Math.max(fw, fh) * 4; // 400%
    }


    private final float[] matrixValues = new float[9];

    int totalLeft = 0;
    int totalRight = 0;
    int totalTop = 0;
    int totalBottom = 0;

    private boolean bitmapInBound(int tempx) {


        float[] f = new float[9];
        mMatrix.getValues(f);
        // 图片4个顶点的坐标
        int x1 = (int) (f[0] * 0 + f[1] * 0 + f[2]);
        int y1 = (int) (f[3] * 0 + f[4] * 0 + f[5]);
        int x2 = (int) (f[0] * mBitmap.getWidth() + f[1] * 0 + f[2]);
        int y2 = (int) (f[3] * mBitmap.getWidth() + f[4] * 0 + f[5]);
        int x3 = (int) (f[0] * 0 + f[1] * mBitmap.getHeight() + f[2]);
        int y3 = (int) (f[3] * 0 + f[4] * mBitmap.getHeight() + f[5]);
        int x4 = (int) (f[0] * mBitmap.getWidth() + f[1] * mBitmap.getHeight() + f[2]);
        int y4 = (int) (f[3] * mBitmap.getWidth() + f[4] * mBitmap.getHeight() + f[5]);


        showL("x1:" + x1 + ",y1:" + y1);
        showL("x2:" + x2 + ",y2:" + y2);
        showL("x3:" + x3 + ",y3:" + y3);
        showL("x4:" + x4 + ",y4:" + y4);


        boolean result = x1 + tempx <= -dp2px(5) && x3 + tempx <= -dp2px(5) && x2 + tempx >= viewWidth + dp2px(5) && x4 + tempx >= viewWidth + dp2px(5);


        return result;
    }

    private boolean bitmapInBound(float scale,float centerX, float centerY) {


        float[] f = new float[9];
        mMatrix.getValues(f);
        // 图片4个顶点的坐标
        int x1 = (int) (f[0] * 0 + f[1] * 0 + f[2]);
        int y1 = (int) (f[3] * 0 + f[4] * 0 + f[5]);
        int x2 = (int) (f[0] * mBitmap.getWidth() + f[1] * 0 + f[2]);
        int y2 = (int) (f[3] * mBitmap.getWidth() + f[4] * 0 + f[5]);
        int x3 = (int) (f[0] * 0 + f[1] * mBitmap.getHeight() + f[2]);
        int y3 = (int) (f[3] * 0 + f[4] * mBitmap.getHeight() + f[5]);
        int x4 = (int) (f[0] * mBitmap.getWidth() + f[1] * mBitmap.getHeight() + f[2]);
        int y4 = (int) (f[3] * mBitmap.getWidth() + f[4] * mBitmap.getHeight() + f[5]);


        showL("x1:" + x1 + ",y1:" + y1);
        showL("x2:" + x2 + ",y2:" + y2);
        showL("x3:" + x3 + ",y3:" + y3);
        showL("x4:" + x4 + ",y4:" + y4);


        boolean result;
        if (scale > 1) { //放大
            result = true;
        } else { //缩小
            result = x1 + ((centerX-x1)*1f/(x2-x2)) * ( (x2-x1) - (x2-x1)*Math.sqrt(scale)) <= -dp2px(5)   && x2 -  ((centerX-x1)*1f/(x2-x2)) * ( (x2-x1) - (x2-x1)*Math.sqrt(scale)) >= viewWidth + dp2px(5)  ;
        }

        return result;
    }

    public void setImagePath(String imagePath) {
        int[] imageSize = new int[2];
        imageSize[0] = viewWidth;
        imageSize[1] = viewHeight;
        mBitmap = ImageUtils.getResizeBitmap(imagePath, imageSize);
        maxZoom = calculateMaxZoom();
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

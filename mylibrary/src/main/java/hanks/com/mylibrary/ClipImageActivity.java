package hanks.com.mylibrary;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

/**
 * Created by Administrator on 2015/9/14.
 */
public class ClipImageActivity extends Activity {

    private CropImageView cropImageView;

    public static void launch(Activity activity, String imagePath) {
        Intent intent = new Intent(activity, ClipImageActivity.class);
        intent.putExtra("imagePath", imagePath);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clip_picture);
        bindViews();
        initViews();
    }

    private void bindViews() {
        cropImageView = (CropImageView) findViewById(R.id.cropImageView);
    }

    private void initViews() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                String imagePath = getIntent().getStringExtra("imagePath");
                cropImageView.setImagePath(imagePath);
            }
        }, 100);
    }

    public void back(View view) {
        onBackPressed();
    }

}

package hanks.com.mylibrary;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import java.io.File;

/**
 * Created by Administrator on 2015/9/14.
 */
public class ClipImageActivity extends Activity{

    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clip_picture);
        bindViews();
        initViews();
        setLisenters();
    }

    private void bindViews() {
        imageView = (ImageView) findViewById(R.id.imageView);

    }

    private void initViews() {
        String imagePath = getIntent().getStringExtra("imagePath");
        imageView.setImageURI(Uri.fromFile(new File(imagePath)));
    }

    private void setLisenters() {

    }

    public static void launch(Activity activity,String imagePath){
        Intent intent = new Intent(activity, ClipImageActivity.class);
        intent.putExtra("imagePath",imagePath);
        activity.startActivity(intent);
    }

}

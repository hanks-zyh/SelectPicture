package hanks.com.myapplication;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import hanks.com.mylibrary.HGallery;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        HGallery.init(this, new GlideImageLoader());
        HGallery.start(this);
    }

    public void launchSecound(View view) {
        HGallery.start(this);
    }


}

package hanks.com.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import hanks.com.mylibrary.GridImageActivity;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startActivity(new Intent(this, GridImageActivity.class));
    }

    public void launchSecound(View view) {
        startActivity(new Intent(this, GridImageActivity.class));
    }


}

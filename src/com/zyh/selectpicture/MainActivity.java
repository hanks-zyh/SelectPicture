package com.zyh.selectpicture;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

public class MainActivity extends Activity {

    private static final int REQUEST_PICK = 0;
    private GridView gridview;
    private GridAdapter adapter;
    private ArrayList<String> selectedPicture = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .threadPriority(Thread.NORM_PRIORITY - 2).denyCacheImageMultipleSizesInMemory()
                .diskCacheFileNameGenerator(new Md5FileNameGenerator()).diskCacheSize(100 * 1024 * 1024)
                .diskCacheFileCount(300).tasksProcessingOrder(QueueProcessingType.LIFO).build();
        ImageLoader.getInstance().init(config);
        gridview = (GridView) findViewById(R.id.gridview);
        adapter = new GridAdapter();
        gridview.setAdapter(adapter);
    }

    public void selectPicture(View view) {
        startActivityForResult(new Intent(this, SelectPictureActivity.class), REQUEST_PICK);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            selectedPicture = (ArrayList<String>) data
                    .getSerializableExtra(SelectPictureActivity.INTENT_SELECTED_PICTURE);
            adapter.notifyDataSetChanged();
        }
    }

    class GridAdapter extends BaseAdapter {
        LayoutParams params = new AbsListView.LayoutParams(100, 100);

        @Override
        public int getCount() {
            return selectedPicture.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = new ImageView(MainActivity.this);
                ((ImageView) convertView).setScaleType(ScaleType.CENTER_CROP);
                convertView.setLayoutParams(params);
            }
            ImageLoader.getInstance().displayImage("file://" + selectedPicture.get(position),
                    (ImageView) convertView);
            return convertView;
        }

    }
}

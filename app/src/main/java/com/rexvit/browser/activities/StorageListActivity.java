package com.rexvit.browser.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.gridlayout.widget.GridLayout;

import com.rexvit.browser.R;
import com.rexvit.browser.utils.StorageUtil;

import java.io.File;
import java.util.List;


public class StorageListActivity extends AppCompatActivity {
    public static final String STORAGE_ROOT = "${applicationId}.STORAGE_ROOT";

    GridLayout gridLayoutStorages;
    LinearLayout listItemStorageDevice;
    Toolbar toolbar;

    
    @Override 
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_storage_list);
        this.gridLayoutStorages = (GridLayout) findViewById(R.id.grid_layout_storages);
        this.toolbar= findViewById(R.id.toolbar_storage_list);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_left);

        }
        listStorages(this);
    }

    public void listStorages(final Context context) {
        Drawable drawable;
        String str;
        List<String> storageDirectories = StorageUtil.getStorageDirectories(this);
        try {
            storageDirectories.remove("/storage/emulated/0/");
            storageDirectories.add(0, "/storage/emulated/0");
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (String str2 : storageDirectories) {
            File file = new File(str2);
            LinearLayout linearLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.list_item_storage_device, (ViewGroup) null, false);
            this.listItemStorageDevice = linearLayout;
            linearLayout.setOnClickListener(new View.OnClickListener() { 
                @Override 
                public final void onClick(View view) {
                    Intent intent = new Intent(context, FileBrowserActivity.class);
                    intent.putExtra(STORAGE_ROOT, ((TextView) view.findViewById(R.id.storage_path)).getText().toString());
                    startActivity(intent);
                    finish();
                }
            });
            str2.hashCode();
            if (str2.equals("/storage/emulated/legacy/") || str2.equals("/storage/emulated/0")) {
                drawable = ContextCompat.getDrawable(this, R.drawable.ic_storage_device);
                str = "Storage";
            } else {
                str = str2.contains("otg://") ? "OTG" : file.getName();
                drawable = ContextCompat.getDrawable(this, R.drawable.ic_storage_sd_card);
            }
            this.gridLayoutStorages.addView(storageView(str, str2, drawable));
        }
    }



    public View storageView(String str, String str2, Drawable drawable) {
        ((TextView) this.listItemStorageDevice.findViewById(R.id.storage_name)).setText(str);
        ((TextView) this.listItemStorageDevice.findViewById(R.id.storage_path)).setText(str2);
        ((AppCompatImageView) this.listItemStorageDevice.findViewById(R.id.storage_icon)).setImageDrawable(drawable);
        GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
        layoutParams.columnSpec = GridLayout.spec(Integer.MIN_VALUE, GridLayout.FILL, 1.0f);
        layoutParams.width = 0;
        this.listItemStorageDevice.setLayoutParams(layoutParams);
        return this.listItemStorageDevice;
    }
}

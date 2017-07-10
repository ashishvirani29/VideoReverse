package com.veeradeveloper.videoreverse.activity;

import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Created by Aspiration-3 on 6/20/2017.
 */

public class BaseActivity extends AppCompatActivity {
    private static final String TAG = "BaseActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    public String makeAppFolder(String folderName) {
        String path = null;
        File file = new File(Environment.getExternalStorageDirectory() + "/" + folderName);
        if (!file.exists()) {
            file.mkdirs();
            path = file.getPath();
        } else {
            File folder = new File(Environment.getExternalStorageDirectory() + "/" + folderName);
            path = folder.getPath();
        }
        return path;
    }

    public String makeSubAppFolder(String path, String subFolderName) {
        String subFolder = null;
        File file = new File(path + "/" + subFolderName);
        if (!file.exists()) {
            file.mkdirs();
            subFolder = file.getPath();
        } else {
            File folder = new File(path + "/" + subFolderName);
            subFolder = folder.getPath();
        }
        return subFolder;
    }

    public ArrayList<String> getAssertFile(String path) {
        ArrayList<String> fontName = null;
        AssetManager assetManager = this.getAssets();
        String assets[] = null;
        String name;
        try {
            assets = assetManager.list(path);
            if (assets.length == 0) {

            } else {
                fontName = new ArrayList<>();
                for (int i = 0; i < assets.length; ++i) {
                    Log.e(TAG, "getAllFont: " + assets[i]);
                    name = assets[i];
                    fontName.add(name);
                }
            }
        } catch (IOException ex) {
            Log.e("tag", "I/O Exception", ex);
        }
        return fontName;
    }
}

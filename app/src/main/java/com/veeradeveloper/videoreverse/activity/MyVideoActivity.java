package com.veeradeveloper.videoreverse.activity;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.veeradeveloper.videoreverse.R;
import com.veeradeveloper.videoreverse.adapter.VideoAdapter;
import com.veeradeveloper.videoreverse.model.VideoModel;
import com.veeradeveloper.videoreverse.utils.ConstantFlag;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

public class MyVideoActivity extends PermissionActivity {
    private static final String TAG = "MyVideoActivity";
    public static ArrayList<VideoModel> videos = null;
    private TextView errorDisplay;
    private ProgressBar progressBar;
    private GridView gridView;
    private VideoAdapter adapter;
    private ContentObserver observer;
    private Handler handler;
    private Thread thread;
    private final String[] projection = new String[]{MediaStore.Video.Media._ID, MediaStore.Video.Media.DISPLAY_NAME, MediaStore.Video.Media.DATA};
    File dirFolder;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.myvideo_activity);
        setView(findViewById(R.id.myvideo_activity));

        dirFolder = Environment.getExternalStorageDirectory();

        dialog = new ProgressDialog(MyVideoActivity.this);
        dialog.setTitle("Please wait...");
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setCancelable(false);

        errorDisplay = (TextView) findViewById(R.id.text_view_error);
        errorDisplay.setVisibility(View.INVISIBLE);

        progressBar = (ProgressBar) findViewById(R.id.progress_bar_image_select);
        gridView = (GridView) findViewById(R.id.grid_view_image_select);

    }


    @Override
    protected void onStart() {
        super.onStart();
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case ConstantFlag.PERMISSION_GRANTED: {
                        loadImages();
                        break;
                    }
                    case ConstantFlag.FETCH_STARTED: {
                        progressBar.setVisibility(View.VISIBLE);
                        gridView.setVisibility(View.INVISIBLE);
                        break;
                    }
                    case ConstantFlag.FETCH_COMPLETED: {

                        if (adapter == null) {
                            adapter = new VideoAdapter(MyVideoActivity.this, videos);
                            gridView.setAdapter(adapter);

                            progressBar.setVisibility(View.INVISIBLE);
                            gridView.setVisibility(View.VISIBLE);
                            orientationBasedUI(getResources().getConfiguration().orientation);

                        } else {
                            adapter.notifyDataSetChanged();
                        }
                        break;
                    }

                    case ConstantFlag.ERROR: {
                        progressBar.setVisibility(View.INVISIBLE);
                        errorDisplay.setVisibility(View.VISIBLE);
                        break;
                    }

                    default: {
                        super.handleMessage(msg);
                    }
                }
            }
        };
        observer = new ContentObserver(handler) {
            @Override
            public void onChange(boolean selfChange) {
                loadImages();
            }
        };
        getContentResolver().registerContentObserver(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, false, observer);
        checkPermission();
    }

    @Override
    protected void onStop() {
        super.onStop();

        stopThread();
        getContentResolver().unregisterContentObserver(observer);
        observer = null;

        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        orientationBasedUI(newConfig.orientation);
    }

    private void orientationBasedUI(int orientation) {
        final WindowManager windowManager = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        final DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);

        if (adapter != null) {
            int size = orientation == Configuration.ORIENTATION_PORTRAIT ? metrics.widthPixels / 3 : metrics.widthPixels / 5;
            adapter.setLayoutParams(size);
        }
        gridView.setNumColumns(orientation == Configuration.ORIENTATION_PORTRAIT ? 3 : 5);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                onBackPressed();
                return true;
            }
            default: {
                return false;
            }
        }
    }

    private void loadImages() {
        startThread(new ImageLoaderRunnable());
    }

    private class ImageLoaderRunnable implements Runnable {
        @Override
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            if (adapter == null) {
                sendMessage(ConstantFlag.FETCH_STARTED);
            }
            File file;
            HashSet<Long> selectedImages = new HashSet<>();
            if (videos != null) {
                VideoModel image;
                for (int i = 0, l = videos.size(); i < l; i++) {
                    image = videos.get(i);
                    file = new File(image.path);
                    if (file.exists() && image.isSelected) {

                        selectedImages.add(image.id);
                    }
                }
            }

            Cursor cursor = getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, null, null, MediaStore.Video.Media.DATE_ADDED);
            if (cursor == null) {
                sendMessage(ConstantFlag.ERROR);
                return;
            }

            int tempCountSelected = 0;
            ArrayList<VideoModel> temp = new ArrayList<>(cursor.getCount());
            if (cursor.moveToLast()) {
                do {
                    if (Thread.interrupted()) {
                        return;
                    }

                    long id = cursor.getLong(cursor.getColumnIndex(projection[0]));
                    String name = cursor.getString(cursor.getColumnIndex(projection[1]));
                    String path = cursor.getString(cursor.getColumnIndex(projection[2]));
                    boolean isSelected = selectedImages.contains(id);
                    if (isSelected) {
                        tempCountSelected++;
                    }

                    file = new File(path);
                    if (path.equals("")) {
                        Log.e(TAG, path);
                    } else {
                        if (file.exists()) {
                            String fullPath = file.getAbsolutePath();
                            int dot = fullPath.lastIndexOf(".");
                            String ext = fullPath.substring(dot + 1);
                            if (ext != null || !ext.equals("")) {
                                if (ext.equals("MP4") || ext.equals("mp4") || ext.equals("3GP") || ext.equals("3gp")) {
                                    temp.add(new VideoModel(id, name, path, isSelected));
                                }
                            } else {
                                Log.e(TAG, "no image found on sdcard");
                            }

                        } else {
                            Log.e(TAG, "file not exist");
                        }
                    }
                } while (cursor.moveToPrevious());
            }
            cursor.close();

            if (videos == null) {
                videos = new ArrayList<VideoModel>();
            }

            videos.clear();
            videos.addAll(temp);
            sendMessage(ConstantFlag.FETCH_COMPLETED, tempCountSelected);
        }
    }

    private void startThread(Runnable runnable) {
        stopThread();
        thread = new Thread(runnable);
        thread.start();
    }

    private void stopThread() {
        if (thread == null || !thread.isAlive()) {
            return;
        }

        thread.interrupt();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(int what) {
        sendMessage(what, 0);
    }

    private void sendMessage(int what, int arg1) {
        if (handler == null) {
            return;
        }

        Message message = handler.obtainMessage();
        message.what = what;
        message.arg1 = arg1;
        message.sendToTarget();
    }

    @Override
    protected void permissionGranted() {
        sendMessage(ConstantFlag.PERMISSION_GRANTED);
    }

    @Override
    protected void hideViews() {
        progressBar.setVisibility(View.INVISIBLE);
        gridView.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onPause() {
        if (dialog != null)
            dialog.dismiss();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (dialog != null)
            dialog.dismiss();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        videos = null;
        if (adapter != null) {
            adapter.releaseResources();
        }
        gridView.setOnItemClickListener(null);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}

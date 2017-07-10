package com.veeradeveloper.videoreverse.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.VideoView;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.veeradeveloper.videoreverse.R;
import com.veeradeveloper.videoreverse.utils.ConstantFlag;
import com.veeradeveloper.videoreverse.utils.VideoControl;

public class VideoReverseActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "VideoReverseActivity";
    private VideoView videoView;
    private String videoPath;
    private ImageView btn_back;
    private Button reverseVideo, reverseAudio;
    private long durationInMs;
    private LinearLayout demoLinearLayout;
    FFmpeg ffmpeg;
    private ProgressDialog progressDialog;
    private String coomandStr = "null";
    private String output_path;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.videoreverse);
        makeSubAppFolder(makeAppFolder("ReverseVideo"), "Video");

        Intent extraIntent = getIntent();
        if (extraIntent != null) {
            videoPath = extraIntent.getStringExtra(ConstantFlag.VIDEO_PATH);
        }
        this.durationInMs = VideoControl.getDuration(this, Uri.parse(videoPath));

        videoView = (VideoView) findViewById(R.id.demovideoview);
        videoView.setVideoPath(videoPath);
        demoLinearLayout = (LinearLayout) findViewById(R.id.demoLinearLayout);


        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                float videoWidth = mp.getVideoWidth();
                float videoHeight = mp.getVideoHeight();
                float videoProportion = (float) videoWidth / (float) videoHeight;
                float screenWidth = demoLinearLayout.getWidth();
                float screenHeight = demoLinearLayout.getHeight();
                float screenProportion = (float) screenWidth / (float) screenHeight;
                ViewGroup.LayoutParams lp = videoView.getLayoutParams();

                if (videoProportion > screenProportion) {
                    lp.width = (int) screenWidth;
                    lp.height = (int) (screenWidth / videoProportion);
                } else {
                    lp.width = (int) (videoProportion * screenHeight);
                    lp.height = (int) screenHeight;
                }

                videoView.setLayoutParams(lp);
            }
        });


        videoView.start();
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.start();
            }
        });

        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Log.e(TAG, " videoView setOnErrorListener(: " + what);
                return true;
            }
        });

        btn_back = (ImageView) findViewById(R.id.btnback_text);
        btn_back.setOnClickListener(this);

        reverseVideo = (Button) findViewById(R.id.reverseVideo);
        reverseVideo.setOnClickListener(this);

        reverseAudio = (Button) findViewById(R.id.reversewithAudio);
        reverseAudio.setOnClickListener(this);

        initUI();
        this.ffmpeg = FFmpeg.getInstance(this);
        loadFFMpegBinary();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnback_text:
                onBackPressed();
                break;
            case R.id.reverseVideo:
                try {
                    output_path = makeSubAppFolder(makeAppFolder("ReverseVideo"), "Video") + "/VID_" + System.currentTimeMillis() + ".mp4";
                    coomandStr = "-i " + videoPath + " -strict experimental -vcodec mpeg4 -b 2097152 -vf reverse -an " + output_path;

                    String[] command = coomandStr.split(" ");
                    if (command.length != 0) {
                        progressDialog.show();
                        execFFmpegBinary(command);
                    } else {
                        Toast.makeText(VideoReverseActivity.this, "null command", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.reversewithAudio:
                try {
                    output_path = makeSubAppFolder(makeAppFolder("ReverseVideo"), "Video") + "/VID_" + System.currentTimeMillis() + ".mp4";
                    coomandStr = "-i " + videoPath + " -strict experimental -vcodec mpeg4 -b 2097152 -vf reverse -af areverse -r 25 -b:v 5000k -minrate 5000k -maxrate 5000k -vcodec mpeg4 -ab 48000 -ac 2 -ar 22050 -c:v libx264 -preset superfast " + output_path;

                    String[] command = coomandStr.split(" ");
                    if (command.length != 0) {
                        progressDialog.show();
                        execFFmpegBinary(command);
                    } else {
                        Toast.makeText(VideoReverseActivity.this, "null command", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }

    }


    private void initUI() {
        progressDialog = new ProgressDialog(VideoReverseActivity.this);
        progressDialog.setMessage("Please wait. ");
        progressDialog.setCancelable(false);
    }

    private void loadFFMpegBinary() {
        try {
            this.ffmpeg.loadBinary(new loadBinaryResponseHandler());
        } catch (FFmpegNotSupportedException e) {
            showUnsupportedExceptionDialog();
        }
    }

    private void showUnsupportedExceptionDialog() {
        new AlertDialog.Builder(this).setIcon(getResources().getDrawable(R.mipmap.ic_launcher)).setTitle("devise not supported").setMessage("devise not supported").setCancelable(false).setPositiveButton("Cancel", new progressDialogInterface()).create().show();
    }


    class loadBinaryResponseHandler extends LoadBinaryResponseHandler {
        loadBinaryResponseHandler() {
        }

        public void onFailure() {
            VideoReverseActivity.this.showUnsupportedExceptionDialog();
        }
    }

    class progressDialogInterface implements DialogInterface.OnClickListener {
        progressDialogInterface() {
        }

        public void onClick(DialogInterface dialog, int which) {
            VideoReverseActivity.this.finish();
        }
    }

    private void execFFmpegBinary(String[] command) {
        try {
            this.ffmpeg.execute(command, new executeBinaryResponseHandler(command));
        } catch (FFmpegCommandAlreadyRunningException e) {
        }
    }

    class executeBinaryResponseHandler extends ExecuteBinaryResponseHandler {
        final String[] val$command;

        executeBinaryResponseHandler(String[] strArr) {
            this.val$command = strArr;
        }

        public void onFailure(String s) {
            Log.e(TAG, "Failure command : ffmpeg " + s);
            progressDialog.dismiss();
            Toast.makeText(VideoReverseActivity.this, "Failed to save Video", Toast.LENGTH_SHORT).show();
        }

        public void onSuccess(String s) {
            Log.e(TAG, "Succes " + s);
            progressDialog.dismiss();
            Toast.makeText(VideoReverseActivity.this, "save at " + output_path, Toast.LENGTH_SHORT).show();

        }

        public void onProgress(String s) {
            Log.e(TAG, "Progress command : ffmpeg " + s);
            if (s.contains("time=")) {
                progressDialog.setMessage("Please wait. " + ((int) ((((double) VideoControl.progressDurationInMs(s.substring(s.lastIndexOf("time=") + 5, s.lastIndexOf("time=") + 16))) / ((double) VideoReverseActivity.this.durationInMs)) * 100.0d)) + "%             ");
            }
        }

        public void onStart() {
            Log.d(TAG, "Started command : ffmpeg " + this.val$command);

        }

        public void onFinish() {
            Log.d(TAG, "Finished command : ffmpeg " + this.val$command);

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (videoView != null) {
            videoView.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (videoView != null) {
            videoView.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (videoView != null) {
            videoView.stopPlayback();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}

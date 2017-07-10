package com.veeradeveloper.videoreverse.utils;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.widget.MediaController;
import android.widget.VideoView;

public class VideoControl {
    public static void injectVideo(Context context, VideoView vv, String path) {
        vv.setVisibility(0);
        MediaController videoMediaController = new MediaController(context);
        vv.setVideoPath(path);
        videoMediaController.setMediaPlayer(vv);
        vv.setMediaController(videoMediaController);
        vv.requestFocus();
        vv.start();
    }

    public static long getDuration(Context context, Uri uri) {
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(context, uri);
            return Long.parseLong(retriever.extractMetadata(9));
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static String getDuration(long timeInmillisec) {
        long duration = timeInmillisec / 1000;
        long hours = duration / 3600;
        long minutes = (duration - (3600 * hours)) / 60;
        long seconds = duration - ((3600 * hours) + (60 * minutes));
        long percent = (timeInmillisec % 1000) / 10;
        return (hours < 10 ? "0" + Long.toString(hours) : Long.toString(hours)) + ":" + (minutes < 10 ? "0" + Long.toString(minutes) : Long.toString(minutes)) + ":" + (seconds < 10 ? "0" + Long.toString(seconds) : Long.toString(seconds)) + "." + (percent < 10 ? "0" + Long.toString(percent) : Long.toString(percent));
    }

    public static String getDurationInSecond(long timeInmillisec) {
        long duration = timeInmillisec / 1000;
        long hours = duration / 3600;
        long minutes = (duration - (3600 * hours)) / 60;
        long seconds = duration - ((3600 * hours) + (60 * minutes));
        return (hours < 10 ? "0" + Long.toString(hours) : Long.toString(hours)) + ":" + (minutes < 10 ? "0" + Long.toString(minutes) : Long.toString(minutes)) + ":" + (seconds < 10 ? "0" + Long.toString(seconds) : Long.toString(seconds));
    }

    public static long progressDurationInMs(String s) {
        String[] timeCode = s.split(":");
        return (long) (((((Double.parseDouble(timeCode[0].trim()) * 60.0d) * 60.0d) * 1000.0d) + ((Double.parseDouble(timeCode[1].trim()) * 60.0d) * 1000.0d)) + (Double.parseDouble(timeCode[2].trim()) * 1000.0d));
    }
}

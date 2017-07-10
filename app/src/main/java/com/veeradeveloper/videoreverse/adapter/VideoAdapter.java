package com.veeradeveloper.videoreverse.adapter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.veeradeveloper.videoreverse.R;
import com.veeradeveloper.videoreverse.activity.VideoReverseActivity;
import com.veeradeveloper.videoreverse.model.VideoModel;
import com.veeradeveloper.videoreverse.utils.ConstantFlag;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class VideoAdapter extends GenericAdapter<VideoModel> {
    String tempVideoPath;
    ProgressDialog progressDialog;

    public VideoAdapter(Activity context, ArrayList<VideoModel> video) {
        super(context, video);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.myvideo_gridrow, null);

            viewHolder = new ViewHolder();
            viewHolder.imageView = (ImageView) convertView.findViewById(R.id.image_view_image_select);
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.imageView.getLayoutParams().width = size;
        viewHolder.imageView.getLayoutParams().height = size;

        Glide.with(context).load(arrayList.get(position).path).placeholder(R.drawable.video_placeholder).into(viewHolder.imageView);

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, VideoReverseActivity.class);
                intent.putExtra(ConstantFlag.VIDEO_PATH, arrayList.get(position).path);
                context.startActivity(intent);
            }
        });
        return convertView;
    }

    private static class ViewHolder {
        public ImageView imageView;

    }
}

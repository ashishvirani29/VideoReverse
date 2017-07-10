package com.veeradeveloper.videoreverse.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by  ashish virani on 4/18/2016.
 */
public class VideoModel implements Parcelable {
    public long id;
    public String name;
    public String path;
    public boolean isSelected;

    public VideoModel(long id, String name, String path, boolean isSelected) {
        this.id = id;
        this.name = name;
        this.path = path;
        this.isSelected = isSelected;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeString(path);
    }

    public static final Creator<VideoModel> CREATOR = new Creator<VideoModel>() {
        @Override
        public VideoModel createFromParcel(Parcel source) {
            return new VideoModel(source);
        }

        @Override
        public VideoModel[] newArray(int size) {
            return new VideoModel[size];
        }
    };

    private VideoModel(Parcel in) {
        id = in.readLong();
        name = in.readString();
        path = in.readString();
    }
}

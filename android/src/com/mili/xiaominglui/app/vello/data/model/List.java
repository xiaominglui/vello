package com.mili.xiaominglui.app.vello.data.model;

import android.os.Parcel;
import android.os.Parcelable;

public class List implements Parcelable {
    public String id;
    public String name;
    public String closed;

    public List() {
    }

    public List(Parcel in) {
        id = in.readString();
        name = in.readString();
        closed = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(closed);
    }

    public static final Parcelable.Creator<List> CREATOR
            = new Parcelable.Creator<List>() {
        public List createFromParcel(Parcel in) {
            return new List(in);
        }

        public List[] newArray(int size) {
            return new List[size];
        }
    };
}

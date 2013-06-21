package com.mili.xiaominglui.app.vello.data.model;

import android.os.Parcel;
import android.os.Parcelable;

public class WordList implements Parcelable {
    public String id;
    public String name;
    public String closed;
    public String idBoard;
    public String pos;
    public String subscribed;
    
    @Override
    public int describeContents() {
        return 0;
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(closed);
        dest.writeString(idBoard);
        dest.writeString(pos);
        dest.writeString(subscribed);
    }
}

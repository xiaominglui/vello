package com.mili.xiaominglui.app.vello.data.model;

import android.os.Parcel;
import android.os.Parcelable;

public class List implements Parcelable {
    public String id;
    public String name;
    public String closed;

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
}

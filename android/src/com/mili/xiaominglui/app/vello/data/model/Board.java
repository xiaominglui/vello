package com.mili.xiaominglui.app.vello.data.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public final class Board implements Parcelable {
    public String id;
    public String name;
    public String desc;
    public String closed;
    public String idOrganization;
    
    public Board() {
    }

    @Override
    public int describeContents() {
	return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
	dest.writeString(id);
	dest.writeString(name);
	dest.writeString(desc);
	dest.writeString(closed);
	dest.writeString(idOrganization);
    }
}

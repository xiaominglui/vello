package com.mili.xiaominglui.app.vello.data.model;

import android.os.Parcel;
import android.os.Parcelable;

public class WordCard implements Parcelable {
    public String id;
    public String name;
    public String desc;
    public String due;
    public String idList;
    
    public WordCard() {
	
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
	dest.writeString(due);
	dest.writeString(idList);
    }
}

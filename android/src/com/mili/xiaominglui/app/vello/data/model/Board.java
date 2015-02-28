package com.mili.xiaominglui.app.vello.data.model;

import android.os.Parcel;
import android.os.Parcelable;

public final class Board implements Parcelable {
    public String id;
    public String name;
    public String desc;
    public String closed;
    public String idOrganization;

    public Board() {
    }

    public Board(Parcel in) {
        id = in.readString();
        name = in.readString();
        desc = in.readString();
        closed = in.readString();
        idOrganization = in.readString();
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

    public static final Parcelable.Creator<Board> CREATOR
            = new Parcelable.Creator<Board>() {
        public Board createFromParcel(Parcel in) {
            return new Board(in);
        }

        public Board[] newArray(int size) {
            return new Board[size];
        }
    };
}

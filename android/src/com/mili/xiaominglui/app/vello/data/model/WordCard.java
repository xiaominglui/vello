
package com.mili.xiaominglui.app.vello.data.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.mili.xiaominglui.app.vello.data.provider.VelloContent.DbWordCard;

public class WordCard implements Parcelable {
    public int idColumns;
    public String id;
    public String name;
    public String desc;
    public String due;
    public String idList;
    public String closed;
    public String dateLastActivity;
    
    public WordCard() {

    }
    
    public static final Parcelable.Creator<WordCard> CREATOR = new Creator<WordCard>() {

        public WordCard createFromParcel(Parcel source) {

            return new WordCard(source);
        }

        public WordCard[] newArray(int size) {

            return new WordCard[size];
        }

    };

    
    
    public WordCard(Cursor c) {
        idColumns = c.getInt(DbWordCard.Columns.ID.getIndex());
        id = c.getString(DbWordCard.Columns.ID_CARD.getIndex());
        name = c.getString(DbWordCard.Columns.NAME.getIndex());
        desc = c.getString(DbWordCard.Columns.DESC.getIndex());
        due = c.getString(DbWordCard.Columns.DUE.getIndex());
        idList = c.getString(DbWordCard.Columns.ID_LIST.getIndex());
        closed = c.getString(DbWordCard.Columns.CLOSED.getIndex());
        dateLastActivity = c.getString(DbWordCard.Columns.DATE_LAST_ACTIVITY.getIndex());
    }

    public WordCard(Parcel source) {
        idColumns = source.readInt();
        id = source.readString();
        name = source.readString();
        desc = source.readString();
        due = source.readString();
        idList = source.readString();
        closed = source.readString();
        dateLastActivity = source.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(idColumns);
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(desc);
        dest.writeString(due);
        dest.writeString(idList);
        dest.writeString(closed);
        dest.writeString(dateLastActivity);
    }

    public ContentValues toContentVaalues() {
        ContentValues cv = new ContentValues();
        cv.put(DbWordCard.Columns.ID_CARD.getName(), id);
        cv.put(DbWordCard.Columns.NAME.getName(), name);
        cv.put(DbWordCard.Columns.DESC.getName(), desc);
        cv.put(DbWordCard.Columns.DUE.getName(), due);
        cv.put(DbWordCard.Columns.CLOSED.getName(), closed);
        cv.put(DbWordCard.Columns.ID_LIST.getName(), idList);
        cv.put(DbWordCard.Columns.DATE_LAST_ACTIVITY.getName(), dateLastActivity);
        cv.put(DbWordCard.Columns.SYNCINNEXT.getName(), "false");
        return cv;
    }
}

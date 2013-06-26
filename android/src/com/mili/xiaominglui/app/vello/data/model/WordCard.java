
package com.mili.xiaominglui.app.vello.data.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.mili.xiaominglui.app.vello.data.provider.VelloContent.DbWordCard;

public class WordCard implements Parcelable {
    public int id;
    public String idCard;
    public String name;
    public String desc;
    public String due;
    public String idList;
    public String closed;
    public String dateLastActivity;

    public WordCard() {

    }
    
    public WordCard(Cursor c) {
        id = c.getInt(DbWordCard.Columns.ID.getIndex());
        idCard = c.getString(DbWordCard.Columns.ID_CARD.getIndex());
        name = c.getString(DbWordCard.Columns.NAME.getIndex());
        desc = c.getString(DbWordCard.Columns.DESC.getIndex());
        due = c.getString(DbWordCard.Columns.DUE.getIndex());
        idList = c.getString(DbWordCard.Columns.ID_LIST.getIndex());
        closed = c.getString(DbWordCard.Columns.CLOSED.getIndex());
        dateLastActivity = c.getString(DbWordCard.Columns.DATE_LAST_ACTIVITY.getIndex());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(idCard);
        dest.writeString(name);
        dest.writeString(desc);
        dest.writeString(due);
        dest.writeString(idList);
        dest.writeString(closed);
        dest.writeString(dateLastActivity);
    }

    public ContentValues toContentVaalues() {
        ContentValues cv = new ContentValues();
        cv.put(DbWordCard.Columns.ID_CARD.getName(), idCard);
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

package com.mili.xiaominglui.app.vello.data.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.mili.xiaominglui.app.vello.data.provider.VelloContent.DbWordCard;

public class TrelloCard implements Parcelable {
	public String id;
	public String name;
	public String desc;
	public String due;
	public String idList;
	public String closed;
	public String dateLastActivity;
//	public String markDeleted;
//	public String dateLastOperation;

	public TrelloCard() {

	}

	public static final Parcelable.Creator<TrelloCard> CREATOR = new Creator<TrelloCard>() {

		public TrelloCard createFromParcel(Parcel source) {

			return new TrelloCard(source);
		}

		public TrelloCard[] newArray(int size) {

			return new TrelloCard[size];
		}

	};

	public TrelloCard(Cursor c) {
		id = c.getString(DbWordCard.Columns.CARD_ID.getIndex());
		name = c.getString(DbWordCard.Columns.NAME.getIndex());
		desc = c.getString(DbWordCard.Columns.DESC.getIndex());
		due = c.getString(DbWordCard.Columns.DUE.getIndex());
		idList = c.getString(DbWordCard.Columns.LIST_ID.getIndex());
		closed = c.getString(DbWordCard.Columns.CLOSED.getIndex());
		dateLastActivity = c.getString(DbWordCard.Columns.DATE_LAST_ACTIVITY.getIndex());
	}

	public TrelloCard(Parcel source) {
		id = source.readString();
		name = source.readString();
		desc = source.readString();
		due = source.readString();
		idList = source.readString();
		closed = source.readString();
		dateLastActivity = source.readString();
//		markDeleted = source.readString();
//		dateLastOperation = source.readString();
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
		dest.writeString(closed);
		dest.writeString(dateLastActivity);
//		dest.writeString(markDeleted);
//		dest.writeString(dateLastOperation);
	}

	public ContentValues toContentValues() {
		ContentValues cv = new ContentValues();
		cv.put(DbWordCard.Columns.CARD_ID.getName(), id);
		cv.put(DbWordCard.Columns.NAME.getName(), name);
		cv.put(DbWordCard.Columns.DESC.getName(), desc);
		cv.put(DbWordCard.Columns.DUE.getName(), due);
		cv.put(DbWordCard.Columns.CLOSED.getName(), closed);
		cv.put(DbWordCard.Columns.LIST_ID.getName(), idList);
		cv.put(DbWordCard.Columns.DATE_LAST_ACTIVITY.getName(), dateLastActivity);
//		cv.put(DbWordCard.Columns.MARKDELETED.getName(), markDeleted);
//		cv.put(DbWordCard.Columns.DATE_LAST_OPERATION.getName(), dateLastOperation);

		return cv;
	}
}

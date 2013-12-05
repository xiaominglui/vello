package com.mili.xiaominglui.app.vello.data.model;

import com.mili.xiaominglui.app.vello.data.provider.VelloContent.DbWordCard;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

public class DirtyCard implements Parcelable {
	public String id;
	public String markDeleted;
	public String dateLastOperation;

	public static final Parcelable.Creator<DirtyCard> CREATOR = new Creator<DirtyCard>() {

		@Override
		public DirtyCard createFromParcel(Parcel source) {
			return new DirtyCard(source);
		}

		@Override
		public DirtyCard[] newArray(int size) {
			return new DirtyCard[size];
		}
		
	};
	public DirtyCard(Parcel source) {
		id = source.readString();
		markDeleted = source.readString();
		dateLastOperation = source.readString();
	}
	
	public DirtyCard(Cursor c) {
		id = c.getString(DbWordCard.Columns.CARD_ID.getIndex());
		markDeleted = c.getString(DbWordCard.Columns.MARKDELETED.getIndex());
		dateLastOperation = c.getString(DbWordCard.Columns.DATE_LAST_OPERATION.getIndex());		
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(id);
		dest.writeString(markDeleted);
		dest.writeString(dateLastOperation);
	}

}

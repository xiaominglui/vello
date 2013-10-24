package com.mili.xiaominglui.app.vello.data.model;

import android.content.ContentValues;

import com.mili.xiaominglui.app.vello.data.provider.VelloContent.DbDictCard;


public class DictCard {
	public id _id;
	public String keyword;
//	public String dicdata;
	
	static class id {
		public String $oid;
		
		public id() {
		}
	}
	
	public ContentValues toContentValues() {
		ContentValues cv = new ContentValues();
		cv.put(DbDictCard.Columns.OID.getName(), _id.$oid);
		cv.put(DbDictCard.Columns.KEYWORD.getName(), keyword);
//		cv.put(DbDictCard.Columns.DICDATA.getName(), dicdata);

		return cv;
	}
}

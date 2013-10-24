package com.mili.xiaominglui.app.vello.data.model;

import android.content.ContentValues;

import com.mili.xiaominglui.app.vello.data.provider.VelloContent.DbDictCard;


public class DictCard {
	public id _id;
	public String keyword;
//	public Pronunciation[] pron;
//	public Accettation[] accettation;
	public String pic;
	public String frequency;
//	public Example[] examples;
	public String created_at;
	public String updated_at;
	
	static class id {
		public String $oid;
		
		public id() {
		}
	}
	
	static class Pronunciation {
		public String type;
		public String ps;
		public String link;
		
		public Pronunciation() {
		}
	}
	
	static class Accettation {
		public String pos;
		public String accep;
		
		public Accettation() {
		}
	}
	
	static class Example {
		public String sentence;
		public String explain;
		
		public Example() {
		}
	}
	
	public ContentValues toContentValues() {
		ContentValues cv = new ContentValues();
//		cv.put(DbDictCard.Columns.ID.getName(), _id.$oid);
		cv.put(DbDictCard.Columns.KEYWORD.getName(), keyword);
		cv.put(DbDictCard.Columns.DICDATA.getName(), frequency);

		return cv;
	}
}

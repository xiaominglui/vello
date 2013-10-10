package com.mili.xiaominglui.app.vello.data.model;

import android.content.ContentValues;

import com.mili.xiaominglui.app.vello.data.provider.VelloContent.DbDictCard;


public class DictCard {
	public id _id;
	public String spell;
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
		cv.put(DbDictCard.Columns.SPELL.getName(), spell);
		cv.put(DbDictCard.Columns.PIC.getName(), pic);
		cv.put(DbDictCard.Columns.FREQUENCY.getName(), frequency);
		cv.put(DbDictCard.Columns.CREATED_AT.getName(), created_at);
		cv.put(DbDictCard.Columns.UPDATED_AT.getName(), updated_at);

		return cv;
	}
}

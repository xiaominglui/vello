package com.mili.xiaominglui.app.vello.data.model;

import android.content.ContentValues;

import com.google.gson.Gson;
import com.mili.xiaominglui.app.vello.data.provider.VelloContent.DbDictCard;


public class DictCard {
	public id _id;
	public String keyword;
	public DICDATA dicdata;
	
	static class id {
		public String $oid;
		
		public id() {
		}
	}
	
	static class DICDATA {
		public PRON[] pron;
		public String spell;
		public ACCETTATION[] accettation;
		public String pic;
		public String frequency;
		public String more;
		public EXAMPLES[] examples;
		public String created_at;
		public String updated_at;
		
		static class PRON {
			public String type;
			public String ps;
			public String link;
			
			public PRON() {
			}
		}
		
		static class ACCETTATION {
			public String pos;
			public String accep;
			
			public ACCETTATION() {
			}
		}
		
		static class EXAMPLES {
			public String sentence;
			public String explain;
			
			public EXAMPLES() {
			}
		}
		
		public DICDATA() {
		}
	}
	
	public ContentValues toContentValues() {
		ContentValues cv = new ContentValues();
		Gson gson = new Gson();
		String dicdataString = gson.toJson(dicdata);
		cv.put(DbDictCard.Columns.OID.getName(), _id.$oid);
		cv.put(DbDictCard.Columns.KEYWORD.getName(), keyword);
		cv.put(DbDictCard.Columns.DICDATA.getName(), dicdataString);

		return cv;
	}
}

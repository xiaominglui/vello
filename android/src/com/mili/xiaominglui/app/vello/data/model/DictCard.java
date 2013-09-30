package com.mili.xiaominglui.app.vello.data.model;


public class DictCard {
	public id _id;
	public String spell;
	public Pronunciation[] pron;
	public Accettation[] accettation;
	public String pic;
	public String frequency;
	public Example[] examples;
	public String created_at;
	public String updated_at;
	
	public static class id {
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
}

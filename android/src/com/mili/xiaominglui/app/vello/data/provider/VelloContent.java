package com.mili.xiaominglui.app.vello.data.provider;

import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;

import com.mili.xiaominglui.app.vello.data.provider.util.ColumnMetaData;

public class VelloContent {
	public static final Uri CONTENT_URI = Uri.parse("content://" + VelloProvider.AUTHORITY);

	private VelloContent() {
	}

	/**
	 * Created in version 1
	 */
	public static final class DbDictCard extends VelloContent {
		private static final String TAG = DbDictCard.class.getSimpleName();
		
		public static final String TABLE_NAME = "dict_cards";
		public static final String TYPE_ELEM_TYPE = "vnd.android.cursor.item/vello-dbdictcard";
		public static final String TYPE_DIR_TYPE = "vnd.android.cursor.dir/vello-dbdictcard";
		
		public static final Uri CONTENT_URI = Uri.parse(VelloContent.CONTENT_URI + "/" + TABLE_NAME);
		
		public static enum Columns implements ColumnMetaData {
			ID(BaseColumns._ID, "INTEGER"),
			SPELL("spell", "VARCHAR"),
			PIC("pic", "VARCHAR"),
			FREQUENCY("frequency", "VARCHAR"),
			CREATED_AT("created_at", "VARCHAR"),
			UPDATED_AT("updated_at", "VARCHAR");

			private final String mName;
			private final String mType;
			
			private Columns(String name, String type) {
				mName = name;
				mType = type;
			}
			@Override
			public int getIndex() {
				return ordinal();
			}

			@Override
			public String getName() {
				return mName;
			}

			@Override
			public String getType() {
				return mType;
			}
		}
		
		public static final String[] PROJECTION = new String[] {
			Columns.ID.getName(),
			Columns.SPELL.getName(),
			Columns.PIC.getName(),
			Columns.FREQUENCY.getName(),
			Columns.CREATED_AT.getName(),
			Columns.UPDATED_AT.getName() };
		
		private DbDictCard() {
		}
		
		public static void createTable(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + TABLE_NAME + " ("
					+ Columns.ID.getName() + " " + Columns.ID.getType() + ", "
					+ Columns.SPELL.getName() + " " + Columns.SPELL.getType() + ", "
					+ Columns.PIC.getName() + " " + Columns.PIC.getType() + ", "
					+ Columns.FREQUENCY.getName() + " " + Columns.FREQUENCY.getType() + ", "
					+ Columns.CREATED_AT.getName() + " " + Columns.CREATED_AT.getType()
					+ Columns.UPDATED_AT.getName() + " " + Columns.UPDATED_AT.getType() + ", "
					+ ", PRIMARY KEY (" + Columns.ID.getName() + ")" + ");");
		}

		public static void upgradeTable(SQLiteDatabase db, int oldVersion,
				int newVersion) {
			// TODO
		}
	}

	/**
	 * Created in version 1
	 */
	public static final class DbWordCard extends VelloContent {
		private static final String TAG = DbWordCard.class.getSimpleName();

		public static final String TABLE_NAME = "word_cards";
		public static final String TYPE_ELEM_TYPE = "vnd.androd.cursor.item/vello-dbwordcard";
		public static final String TYPE_DIR_TYPE = "vnd.android.cursor.dir/vello-dbwordcard";

		public static final Uri CONTENT_URI = Uri.parse(VelloContent.CONTENT_URI + "/" + TABLE_NAME);

		public static enum Columns implements ColumnMetaData {
			ID(BaseColumns._ID, "INTEGER"),
			CARD_ID("card_id", "VARCHAR"), // id in trello card
			NAME("name", "VARCHAR"), // name in trello card
			DESC("desc", "VARCHAR"), // desc in trello card
			DUE("due", "VARCHAR"), // due in trello card
			CLOSED("closed", "SMALLINT"), // closed in trello card
			LIST_ID("list_id", "VARCHAR"), // list_id in trello card
			DATE_LAST_ACTIVITY("dateLastActivity", "VARCHAR"), // dateLastActivity in trello card
			MARKDELETED("deletedInLocal", "SMALLINT"), // flag indicate if marked deleted in vello
			DATE_LAST_OPERATION("dateLastOperation", "VARCHAR"); // date Last Operation in vello

			private final String mName;
			private final String mType;

			private Columns(String name, String type) {
				mName = name;
				mType = type;
			}

			@Override
			public int getIndex() {
				return ordinal();
			}

			@Override
			public String getName() {
				return mName;
			}

			@Override
			public String getType() {
				return mType;
			}
		}

		public static final String[] PROJECTION = new String[] {
				Columns.ID.getName(),
				Columns.CARD_ID.getName(),
				Columns.NAME.getName(),
				Columns.DESC.getName(),
				Columns.DUE.getName(),
				Columns.CLOSED.getName(),
				Columns.LIST_ID.getName(),
				Columns.DATE_LAST_ACTIVITY.getName(),
				Columns.MARKDELETED.getName(),
				Columns.DATE_LAST_OPERATION.getName() };

		private DbWordCard() {
		}

		public static void createTable(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + TABLE_NAME + " ("
					+ Columns.ID.getName() + " " + Columns.ID.getType() + ", "
					+ Columns.CARD_ID.getName() + " " + Columns.CARD_ID.getType() + ", "
					+ Columns.NAME.getName() + " " + Columns.NAME.getType() + ", "
					+ Columns.DESC.getName() + " " + Columns.DESC.getType() + ", "
					+ Columns.DUE.getName() + " " + Columns.DUE.getType() + ", "
					+ Columns.CLOSED.getName() + " " + Columns.CLOSED.getType() + ", "
					+ Columns.DATE_LAST_OPERATION.getName() + " " + Columns.DATE_LAST_OPERATION.getType() + ", "
					+ Columns.LIST_ID.getName() + " " + Columns.LIST_ID.mType + ", "
					+ Columns.DATE_LAST_ACTIVITY.getName() + " " + Columns.DATE_LAST_ACTIVITY.getType() + ", "
					+ Columns.MARKDELETED.getName() + " " + Columns.MARKDELETED.getType()
					+ ", PRIMARY KEY (" + Columns.ID.getName() + ")" + ");");
		}

		public static void upgradeTable(SQLiteDatabase db, int oldVersion,
				int newVersion) {
			// TODO
		}
	}

}

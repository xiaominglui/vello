package com.mili.xiaominglui.app.vello.data.provider;

import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;

import com.mili.xiaominglui.app.vello.data.provider.util.ColumnMetadata;

public class VelloContent {
    public static final Uri CONTENT_URI = Uri.parse("content://" + VelloProvider.AUTHORITY);
    
    private VelloContent() {
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
        
        public static enum Columns implements ColumnMetadata {
            ID(BaseColumns._ID, "INTEGER"),
            ID_CARD("card_id", "text"),
            NAME("name", "text"),
            DESC("desc", "text"),
            DUE("due", "VARCHAR"),
            CLOSED("closed", "text"),
            ID_LIST("list_id", "text"),
            DATE_LAST_ACTIVITY("dateLastActivity", "VARCHAR");

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
            Columns.ID_CARD.getName(),
            Columns.NAME.getName(),
            Columns.DESC.getName(),
            Columns.DUE.getName(),
            Columns.CLOSED.getName(),
            Columns.ID_LIST.getName(),
            Columns.DATE_LAST_ACTIVITY.getName()
        };
        
        private DbWordCard() {
        }
        
        public static void createTable(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + TABLE_NAME + " (" + Columns.ID.getName() + " " + Columns.ID.getType() + ", " + Columns.ID_CARD.getName() + " " + Columns.ID_CARD.getType() + ", " + Columns.NAME.getName() + " " + Columns.NAME.getType() + ", " + Columns.DESC.getName() + " " + Columns.DESC.getType() + ", " + Columns.DUE.getName() + " " + Columns.DUE.getType() + ", " + Columns.CLOSED.getName() + " " + Columns.CLOSED.getType() + ", " + Columns.ID_LIST.getName() + " " + Columns.ID_LIST.mType + ", " + Columns.DATE_LAST_ACTIVITY.getName() + " " + Columns.DATE_LAST_ACTIVITY.getType() + ", PRIMARY KEY (" + Columns.ID.getName() + ")" + ");");
//            db.execSQL("CREATE UNIQUE INDEX card_id on " + TABLE_NAME + "(" + Columns.ID_CARD.getName() + ");");
        }
        
        public static void upgradeTable(SQLiteDatabase db, int oldVersion, int newVersion) {
            // TODO
        }
    }

}

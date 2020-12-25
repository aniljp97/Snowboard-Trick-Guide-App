package my.apps.snowboardtrickguide.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import my.apps.snowboardtrickguide.TrickData;

public class TricksDBHepler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "tricks_database";
    public static final String TRICKS_TABLE_NAME = "tricks";
    public static final String TRICKS_COLUMN_ID = "_id";
    public static final String TRICKS_COLUMN_CATEGORY = "category";
    public static final String TRICKS_COLUMN_NAME = "name";
    public static final String TRICKS_COLUMN_SHORT = "short";
    public static final String TRICKS_COLUMN_DATE = "date";
    public static final String TRICKS_COLUMN_COMPLETED = "completed";
    public static final String TRICKS_COLUMN_NOTES = "notes";
    public static final String TRICKS_COLUMN_DISPLAY_SHORT = "displayshort";

    public TricksDBHepler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE " + TRICKS_TABLE_NAME + " (" +
                TRICKS_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TRICKS_COLUMN_CATEGORY + " TEXT, " +
                TRICKS_COLUMN_NAME + " TEXT, " +
                TRICKS_COLUMN_SHORT + " TEXT, " +
                TRICKS_COLUMN_DATE + " TEXT," +
                TRICKS_COLUMN_NOTES + " TEXT," +
                TRICKS_COLUMN_COMPLETED + " INTEGER DEFAULT 0,"+
                TRICKS_COLUMN_DISPLAY_SHORT + " INTEGER DEFAULT 0"+")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TRICKS_TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public boolean addData(TrickData item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(TRICKS_COLUMN_CATEGORY, item.getTrick_category());
        contentValues.put(TRICKS_COLUMN_NAME, item.getTrick_name());
        contentValues.put(TRICKS_COLUMN_SHORT, item.getShorten_trick_name());
        contentValues.put(TRICKS_COLUMN_DATE, item.getDate_discovered());
        contentValues.put(TRICKS_COLUMN_NOTES, item.getNotes());
        contentValues.put(TRICKS_COLUMN_COMPLETED, item.getCompleted());
        contentValues.put(TRICKS_COLUMN_DISPLAY_SHORT, item.getDisplay_short());

        long result = db.insert(TRICKS_TABLE_NAME, null, contentValues);

        if(result == -1)
            return false;
        else
            return true;
    }

    public int deleteData(TrickData item) {
        SQLiteDatabase db = this.getWritableDatabase();
        int num_deleted = db.delete(TRICKS_TABLE_NAME,
                TRICKS_COLUMN_NAME + "=?" + " and " + TRICKS_COLUMN_CATEGORY + "=?",
                new String[]{item.getTrick_name(), item.getTrick_category()});
        return num_deleted;
    }

    public int updateData(TrickData item, String trick_category, String trick_name, String shorten_trick_name, String date_discovered, String notes, int completed, int display_short) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        if(trick_category != null)
            values.put(TRICKS_COLUMN_CATEGORY, trick_category);
        if(trick_name != null)
            values.put(TRICKS_COLUMN_NAME, trick_name);
        if(shorten_trick_name != null)
            values.put(TRICKS_COLUMN_SHORT, shorten_trick_name);
        if(date_discovered != null)
            values.put(TRICKS_COLUMN_DATE, date_discovered);
        if(notes != null)
            values.put(TRICKS_COLUMN_NOTES, notes);
        if(completed == 0 || completed == 1)
            values.put(TRICKS_COLUMN_COMPLETED, completed);
        if(display_short == 0 || display_short == 1)
            values.put(TRICKS_COLUMN_DISPLAY_SHORT, display_short);

        return db.update(TRICKS_TABLE_NAME, values,
                TRICKS_COLUMN_NAME + "=?" + " and " + TRICKS_COLUMN_CATEGORY + "=?",
                new String[]{item.getTrick_name(), item.getTrick_category()});
    }

    public Cursor getData() {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TRICKS_TABLE_NAME;
        Cursor data = db.rawQuery(query, null);
        return data;
    }
}

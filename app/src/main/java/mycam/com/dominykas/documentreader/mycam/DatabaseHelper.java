package mycam.com.dominykas.documentreader.mycam;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "db.db";
    public static final String TABLE_NAME = "this";
    SQLiteDatabase db;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 4);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table this (ID INTEGER PRIMARY KEY AUTOINCREMENT, DATE TEXT, PATH TEXT)");
    }

    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS this");
        onCreate(db);
    }

    public long InsertData(String path, String date) {
        SQLiteDatabase db = getDatabase();
        ContentValues cv = new ContentValues();
        cv.put("DATE", date);
        cv.put("PATH", path);
        return db.insert(TABLE_NAME, null, cv);
    }

    public int deleteRow(String id) {
        return getDatabase().delete(TABLE_NAME, "ID = ?", new String[]{id});
    }

    public Cursor showData() {
        return getDatabase().rawQuery("SELECT * FROM this", null);
    }

    public void deleteAll() {
        this.db.execSQL("delete from this");
    }

    public SQLiteDatabase getDatabase() {
        if (this.db == null) {
            this.db = getWritableDatabase();
        }
        if (this.db.isOpen()) {
            return this.db;
        }
        this.db = getWritableDatabase();
        return this.db;
    }

    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width;
        int height;
        float bitmapRatio = ((float) image.getWidth()) / ((float) image.getHeight());
        if (bitmapRatio > 1.0f) {
            width = maxSize;
            height = (int) (((float) width) / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (((float) height) * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }
}

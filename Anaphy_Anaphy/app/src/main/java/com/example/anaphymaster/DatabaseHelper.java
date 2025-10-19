package com.example.anaphymaster;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "quizApp.db";
    private static final int DATABASE_VERSION = 1;

    // Table name
    private static final String TABLE_NAME = "quizCounts";

    // Column names
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_MODE = "mode"; // Mode (Practice or Challenge)
    private static final String COLUMN_COUNT = "count"; // Count of how many times a mode has been taken

    // Create table query
    private static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_MODE + " TEXT, "
            + COLUMN_COUNT + " INTEGER)";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // Method to update the count of a specific mode
    public void updateQuizCount(String mode) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_MODE, mode);

        // Check if the mode already exists
        Cursor cursor = db.query(TABLE_NAME, new String[]{COLUMN_ID, COLUMN_COUNT},
                COLUMN_MODE + " = ?", new String[]{mode}, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            // Mode exists, update the count
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(COLUMN_COUNT);
            if (columnIndex != -1) { // Check if the column exists
                int count = cursor.getInt(columnIndex);
                count++;  // Increment the count
                contentValues.put(COLUMN_COUNT, count);
                db.update(TABLE_NAME, contentValues, COLUMN_MODE + " = ?", new String[]{mode});
            }
        } else {
            // Mode doesn't exist, insert new entry
            contentValues.put(COLUMN_COUNT, 1); // Set initial count to 1
            db.insert(TABLE_NAME, null, contentValues);
        }
        cursor.close();
        db.close();
    }

    // Method to get the count of a specific mode
    public int getQuizCount(String mode) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, new String[]{COLUMN_COUNT},
                COLUMN_MODE + " = ?", new String[]{mode}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndex(COLUMN_COUNT);
            if (columnIndex != -1) { // Check if the column exists
                int count = cursor.getInt(columnIndex);
                cursor.close();
                return count;
            }
        }
        cursor.close();
        return 0; // Return 0 if mode doesn't exist or no data is found
    }
}

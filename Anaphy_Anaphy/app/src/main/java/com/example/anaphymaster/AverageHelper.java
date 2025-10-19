package com.example.anaphymaster;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AverageHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "AverageStats.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_NAME = "average_scores";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_MODE = "mode";
    private static final String COLUMN_TOPIC = "topic";
    private static final String COLUMN_CORRECT = "correct";
    private static final String COLUMN_INCORRECT = "incorrect";

    public AverageHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_MODE + " TEXT, " +
                COLUMN_TOPIC + " TEXT, " +
                COLUMN_CORRECT + " INTEGER, " +
                COLUMN_INCORRECT + " INTEGER)";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void updateScore(String mode, String topic, int correct, int total) {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.query(TABLE_NAME,
                null,
                COLUMN_MODE + "=? AND " + COLUMN_TOPIC + "=?",
                new String[]{mode, topic},
                null, null, null);

        int incorrect = total - correct;

        if (cursor.moveToFirst()) {
            int currentCorrect = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CORRECT));
            int currentIncorrect = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_INCORRECT));

            ContentValues values = new ContentValues();
            values.put(COLUMN_CORRECT, currentCorrect + correct);
            values.put(COLUMN_INCORRECT, currentIncorrect + incorrect);

            db.update(TABLE_NAME, values,
                    COLUMN_MODE + "=? AND " + COLUMN_TOPIC + "=?",
                    new String[]{mode, topic});
        } else {
            ContentValues values = new ContentValues();
            values.put(COLUMN_MODE, mode);
            values.put(COLUMN_TOPIC, topic);
            values.put(COLUMN_CORRECT, correct);
            values.put(COLUMN_INCORRECT, incorrect);
            db.insert(TABLE_NAME, null, values);
        }

        cursor.close();
    }

    // Returns percentages: [correctPercentage, incorrectPercentage]
    public float[] getAveragePercentages() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(" + COLUMN_CORRECT + "), SUM(" + COLUMN_INCORRECT + ") FROM " + TABLE_NAME, null);

        int totalCorrect = 0;
        int totalIncorrect = 0;

        if (cursor.moveToFirst()) {
            totalCorrect = cursor.getInt(0);
            totalIncorrect = cursor.getInt(1);
        }

        cursor.close();
        int total = totalCorrect + totalIncorrect;
        if (total == 0) {
            return new float[]{0f, 0f};
        }

        float correctPercentage = (totalCorrect * 100f) / total;
        float incorrectPercentage = (totalIncorrect * 100f) / total;

        return new float[]{correctPercentage, incorrectPercentage};
    }
}

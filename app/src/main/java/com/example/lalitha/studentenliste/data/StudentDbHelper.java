package com.example.lalitha.studentenliste.data;

/**
 * Created by Lalitha on 02.11.2017.
 */
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class StudentDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = StudentDbHelper.class.getSimpleName();

    private static final String DATABASE_NAME = "studenten.db";

    private static final int DATABASE_VERSION = 1;


    public StudentDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        // Erzeugt einen String, der die SQL statement enthält um die Studententabelle zu erstellen
        String SQL_CREATE_STUDENTEN_TABLE =  "CREATE TABLE " + StudentContract.StudentEntry.TABLE_NAME + " ("
                + StudentContract.StudentEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + StudentContract.StudentEntry.COLUMN_VORNAME + " TEXT NOT NULL, "
                + StudentContract.StudentEntry.COLUMN_NACHNAME + " TEXT NOT NULL, "
                + StudentContract.StudentEntry.COLUMN_FACHBEREICH + " TEXT NOT NULL, "
                + StudentContract.StudentEntry.COLUMN_STUDIENGANG + " TEXT NOT NULL, "
                + StudentContract.StudentEntry.COLUMN_WOHNORT + " TEXT NOT NULL, "
                + StudentContract.StudentEntry.COLUMN_TELEFON + " TEXT NOT NULL, "
                + StudentContract.StudentEntry.COLUMN_IMAGE + " BLOB NOT NULL, "
                + StudentContract.StudentEntry.COLUMN_EMAIL + " TEXT NOT NULL, "
                + StudentContract.StudentEntry.COLUMN_STUDENT_GESCHLECHT + " INTEGER NOT NULL " + " );";

        // Führt SQL statement aus
        db.execSQL(SQL_CREATE_STUDENTEN_TABLE);
    }

    /**
     * Wird aufgerufen, wenn die DB ein Upgrade braucht
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}


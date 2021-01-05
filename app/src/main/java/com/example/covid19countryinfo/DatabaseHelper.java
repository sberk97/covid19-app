package com.example.covid19countryinfo;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * create custom DatabaseHelper class that extends SQLiteOpenHelper
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static com.example.covid19countryinfo.DatabaseHelper mInstance = null;

    private static final String DATABASE_NAME = "covid19data";
    private static final String DATABASE_TABLE = "recentCountryData";
    private static final int DATABASE_VERSION = 1;

    final private static String CREATE_DB = "CREATE TABLE IF NOT EXISTS " + DATABASE_TABLE + " " +
            "(country_name TEXT, today_cases INTEGER, today_deaths INTEGER, today_recovered INTEGER, date INTEGER)";

    private Context mCxt;

    public static com.example.covid19countryinfo.DatabaseHelper getInstance(Context ctx) {
        /**
         * use the application context as suggested by CommonsWare.
         * this will ensure that you dont accidentally leak an Activitys
         * context (see this article for more information:
         * http://android-developers.blogspot.nl/2009/01/avoiding-memory-leaks.html)
         */
        if (mInstance == null) {
            mInstance = new com.example.covid19countryinfo.DatabaseHelper(ctx.getApplicationContext());
        }
        return mInstance;
    }

    /**
     * constructor should be private to prevent direct instantiation.
     * make call to static factory method "getInstance()" instead.
     */
    private DatabaseHelper(Context ctx) {
        super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
        this.mCxt = ctx;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_DB);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
        onCreate(db);
    }
}

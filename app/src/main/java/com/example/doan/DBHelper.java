package com.example.doan;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

public class DBHelper {
    SQLiteDatabase db;
    static String databaseName;
    static String packageName;
    private static DBHelper instance;

    private DBHelper(String packageName, String databaseName) {
        DBHelper.databaseName = databaseName;
        DBHelper.packageName = packageName;

        try {
            db = SQLiteDatabase.openOrCreateDatabase("/data/data/" + packageName
                    + "/" + databaseName + ".db", null);
            isTableExist();
        } catch (SQLiteException ex) {
            initDatabase();
        }
    }

    public static DBHelper getInstance(String packageName, String databaseName) {
        if (instance == null || !DBHelper.packageName.equals(packageName) || !DBHelper.databaseName.equals(databaseName)) {
            instance = new DBHelper(packageName, databaseName);
        }

        return instance;
    }

    boolean isTableExist() {
        Cursor cursor = db.rawQuery("SELECT name FROM AutoTask WHERE name=?", new String[]{"auto mode"});
        boolean tableExist = (cursor.getCount() != 0);
        cursor.close();
        return tableExist;
    }

    public Cursor getAll() {
        return db.rawQuery("SELECT value FROM AutoTask", new String[]{null});
    }

    public String getByName(String name) {
        Cursor cursor = db.rawQuery("SELECT value FROM AutoTask WHERE name=?", new String[]{name});
        try {
            if (cursor.moveToNext())
                return cursor.getString(0);
        }catch (Exception e){}
            return "";
    }

    public void onCreate() {
        if (!isTableExist()) {
            initDatabase();
        }

    }

    private void initDatabase() {
        db.execSQL("CREATE TABLE AutoTask(name text, value text)");

        insert(Constants.AUTO_MODE, "0");
        insert(Constants.AUTO_RUNNING, "0");
        insert(Constants.SCHEDULE_MODE, "0");
        insert(Constants.SCHEDULE_RUNNING, "0");
        insert(Constants.SCHEDULE_START, "00:00:00");
        insert(Constants.SCHEDULE_END, "00:00:00");
        insert(Constants.HUMIDITY, "70");
        insert(Constants.TEMPERATURE, "38");
    }

    public void insert(String name, String value) {
        db.execSQL("INSERT INTO AutoTask(name, value) VALUES('" + name + "', '" + value + "')");

    }

    public void update(String name, String value) {
        if (name.equals(Constants.SCHEDULE_MODE))
            Log.d("Mode Schedule", value);
        db.execSQL("UPDATE AutoTask SET value=? WHERE name=?", new String[]{value, name});
    }
}

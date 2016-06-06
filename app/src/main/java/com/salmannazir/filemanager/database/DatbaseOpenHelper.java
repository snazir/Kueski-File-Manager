package com.salmannazir.filemanager.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Salman Nazir on 06/06/2016.
 */

public class DatbaseOpenHelper extends SQLiteOpenHelper {


	private Context mContext;

	private static final String DATABASE_NAME = "kueski.db";
	private static final int DATABASE_VERSION = 1;

	public DatbaseOpenHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		mContext = context;
	}

	public static synchronized DatbaseOpenHelper getAppdb(Context context) {
		return new DatbaseOpenHelper(context);
	}

	public SQLiteDatabase getsqldb() {
		SQLiteDatabase db = getWritableDatabase();
		db.setLockingEnabled(true);
		return db;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		Log.d("DB","CREATED");
//		createItemList(db);
		tableStudent(db);
//		tableRooms();


	}

	private void tableStudent(SQLiteDatabase db) {
		db.execSQL(" CREATE TABLE IF NOT EXISTS  "
				+ DbInterfaces.Table.STUDENT + "( "
				+ DbInterfaces.TABLE_STUDENT.ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ DbInterfaces.TABLE_STUDENT.FIRST_NAME + " TEXT,"
				+ DbInterfaces.TABLE_STUDENT.LAST_NAME + " TEXT"
				+ ")");

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		if (newVersion > oldVersion) {

			db.execSQL(" DROP TABLE IF EXISTS " + DbInterfaces.Table.STUDENT);

			onCreate(db);

		}
	}

}

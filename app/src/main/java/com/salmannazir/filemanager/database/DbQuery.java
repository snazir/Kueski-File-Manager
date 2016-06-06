package com.salmannazir.filemanager.database;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;


import java.util.ArrayList;

/**
 * Created by Salman Nazir on 06/06/2016.
 */


public class DbQuery {

	// singleton instance
	private static DbQuery instance;
	private DatbaseOpenHelper dbHelper;
	SQLiteDatabase database;


	public static DbQuery getIntance(Context mContext) {
		if(instance == null) {
			instance = new DbQuery(mContext);
		}
		return instance;
	}

	private DbQuery(Context mContext) {
		dbHelper = new DatbaseOpenHelper(mContext);
	}

	public void open() throws SQLiteException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		if(database == null) {
			return;
		}
		database.close();
	}

	public boolean isDBOpen() {
		if(database == null) {
			return false;
		}
		return database.isOpen();
	}

	public SQLiteDatabase getdb() {

		SQLiteDatabase db = this.dbHelper.getWritableDatabase();
		return db;
	}




	//==============Insert Students=============

//	public long insertStudentToDatabase(Student object) {
//		if(object == null) {
//			return -1;
//		}
//		if(!isDBOpen()) {
//			open();
//		}
//		ContentValues values = new ContentValues();
//		values.put(DbInterfaces.TABLE_STUDENT.FIRST_NAME, object.getFirstName());
//		values.put(DbInterfaces.TABLE_STUDENT.LAST_NAME, object.getLastName());
//		return database.insert(DbInterfaces.Table.STUDENT, null, values);
//
//	}



	// ============Get ALL Students=======================

//	public ArrayList<Student> getallStudents() {
//		ArrayList<Student> list = new ArrayList<Student>();
//		SQLiteDatabase db = getdb();
//
//		Cursor c = db.rawQuery("Select *" + " FROM "
//				+ DbInterfaces.Table.STUDENT, null);
//
//
//		int id = c.getColumnIndex(DbInterfaces.TABLE_STUDENT.ID);
//		int firstName = c.getColumnIndex(DbInterfaces.TABLE_STUDENT.FIRST_NAME);
//		int lastName = c.getColumnIndex(DbInterfaces.TABLE_STUDENT.LAST_NAME);
//
//
//		while (c.moveToNext()) {
//			int studentID = c.getInt(id);
//			String fName = c.getString(firstName);
//			String lName = c.getString(lastName);
//
//			Student student = new Student(studentID,fName,lName);
//			list.add(student);
//		}
//
//		db.close();
//		return list;
//	}


	//=============Delete One Student====================
//
//	public int deleteStudent(Student object) {
//		return database.delete(DbInterfaces.Table.STUDENT, DbInterfaces.TABLE_STUDENT.ID + "=" + object.getId(), null);
//	}

	// ==============Update One Student==================

//	public int updateStudent(Student student) {
//		ContentValues values = new ContentValues();
//		values.put(DbInterfaces.TABLE_STUDENT.FIRST_NAME, student.getFirstName());
//		values.put(DbInterfaces.TABLE_STUDENT.LAST_NAME, student.getLastName());
//		return database.update(DbInterfaces.Table.STUDENT, values, DbInterfaces.TABLE_STUDENT.ID + "=" + student.getId(), null);
//
//	}
}

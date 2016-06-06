package com.salmannazir.filemanager.database;


/**
 * Created by Salman Nazir on 06/06/2016.
 */

public interface DbInterfaces {

	public static interface Table {
		public static final String STUDENT = "student_table";
//		public static final String TEACHER = "teacher_table";

	}

	public static abstract interface TABLE_STUDENT {
		public static final String ID = "student_id";
		public static final String FIRST_NAME = "first_name";
		public static final String LAST_NAME = "last_name";
	}

//	public static abstract interface TEACHER {
//		public static final String ID = "tescher_id";
//		public static final String STRING_IMAGE = "image";
//	}
}

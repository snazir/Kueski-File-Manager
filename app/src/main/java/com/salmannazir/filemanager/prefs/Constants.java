package com.salmannazir.filemanager.prefs;

import android.os.Environment;

/**
 * Created by Salman Nazir on 04/06/2016.
 */
public class Constants {

    public static final int CAMERA_REQUEST = 1;
    public static final String PREFS_NAME = "FileManagerPrefsFile";    //user preference file name
    public static final String HOME_PATH = Environment.getExternalStorageDirectory().getPath() + "/Kueski";
    public static final String PREFS_HIDDEN = "prefsHidden";
    public static final String PREFS_COLOR = "prefsColor";
    public static final String PREFS_THUMBNAIL = "prefsThumbnail";
    public static final String PREFS_SORT = "prefsSort";
    public static final String PREFS_STORAGE = "sdcard space";

    public static final String[] EMAIL = {"salman.nazir532@gmail.com"};
    public static final String WEB = "http://salmannazir.com";

    //    public static final int MENU_MKDIR =    0x00;			//option menu id
//    public static final int MENU_SETTING =  0x01;			//option menu id
//    public static final int MENU_SEARCH =   0x02;			//option menu id
//    public static final int MENU_SPACE =    0x03;			//option menu id
//    public static final int MENU_QUIT = 	0x04;			//option menu id
    public static final int SEARCH_B = 0x09;

    public static final int D_MENU_DELETE = 0x05;            //context menu id
    public static final int D_MENU_RENAME = 0x06;            //context menu id
    public static final int D_MENU_COPY = 0x07;            //context menu id
    public static final int D_MENU_PASTE = 0x08;            //context menu id
    public static final int D_MENU_ZIP = 0x0e;            //context menu id
    public static final int D_MENU_UNZIP = 0x0f;            //context menu id
    public static final int D_MENU_MOVE = 0x30;            //context menu id
    public static final int F_MENU_MOVE = 0x20;            //context menu id
    public static final int F_MENU_DELETE = 0x0a;            //context menu id
    public static final int F_MENU_RENAME = 0x0b;            //context menu id
    public static final int F_MENU_ATTACH = 0x0c;            //context menu id
    public static final int F_MENU_COPY = 0x0d;            //context menu id
    public static final int SETTING_REQ = 0x10;            //request code for intent


    /*
 * Unique types to control which file operation gets
 * performed in the background
 */
    public static final int SEARCH_TYPE = 0x00;
    public static final int COPY_TYPE = 0x01;
    public static final int UNZIP_TYPE = 0x02;
    public static final int UNZIPTO_TYPE = 0x03;
    public static final int ZIP_TYPE = 0x04;
    public static final int DELETE_TYPE = 0x05;
    public static final int MANAGE_DIALOG = 0x06;


    public static final int BUFFER = 2048;
    public static final int SORT_NONE = 0;
    public static final int SORT_ALPHA = 1;
    public static final int SORT_TYPE = 2;
    public static final int SORT_SIZE = 3;


    public static final String BACKUP_LOC = "/sdcard/file manager/AppBackup/";
    public static final int SET_PROGRESS = 0x00;
    public static final int FINISH_PROGRESS = 0x01;
    public static final int FLAG_UPDATED_SYS_APP = 0x80;


    public static final int KB = 1024;
    public static final int MG = KB * KB;
    public static final int GB = MG * KB;

    public static final int REQUEST_STORAGE_PERMISSION = 77;
}

package com.example.downloadtest;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper{

	public DBHelper(Context context) {
        super(context, "download.db", null, 1);
}

	//执行数据库创建操作,用于保存数据
	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		 db.execSQL("create table download_info("
		 		 + "_id integer PRIMARY KEY AUTOINCREMENT, "
		 		 + "thread_id integer, "
                 + "start_pos integer, "
                 + "end_pos integer, "
                 + "compelete_size integer,"
                 + "url char)");
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}

}

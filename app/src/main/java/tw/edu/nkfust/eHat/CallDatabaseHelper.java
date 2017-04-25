package tw.edu.nkfust.eHat;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class CallDatabaseHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "MySQLOfCall";
	private static final int DATABASE_VERSION = 1;

	private static final String TABLE_NAME = "Directory";
	private static final String FIELD_ID = "_id";
	private static final String FIELD_NAME = "Name";
	private static final String FIELD_PHONE = "Phone";

	private SQLiteDatabase db;

	public CallDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);// 建構子(Context/資料庫名稱/Cursor/版本)
		db = this.getWritableDatabase();// 開啟可讀寫資料庫
	}// End of structure

	@Override
	public void onCreate(SQLiteDatabase db) {// 建立資料庫物件 database
		db.execSQL("CREATE TABLE IF NOT EXISTS "
				+ TABLE_NAME + "("
				+ FIELD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ FIELD_NAME + " TEXT, "
				+ FIELD_PHONE + " TEXT" + ")"
				);
	}// End of onCreate

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);// 刪除資料庫
		onCreate(db);// 重新建立資料庫
	}// End of onUpgrade

	// Get all data
	public Cursor get() {
		Cursor cursor = db.query(TABLE_NAME,// 資料表名稱
				new String[] { FIELD_ID, FIELD_NAME, FIELD_PHONE },// 欄位名稱
				null,// SELECTION
				null,// SELECTION 的參數
				null,// GROUP BY
				null,// HAVING
				null// ORDOR BY
				);
		return cursor;
	}// End of get

	// Insert data
	public void insert(String name, String phone) {
		ContentValues cv = new ContentValues();
		cv.put(FIELD_NAME, name);
		cv.put(FIELD_PHONE, phone);
		db.insert(TABLE_NAME, null, cv);
	}// End of insert

	// Query data
	public Cursor query(String phone) {
		Cursor cursor = db.query(TABLE_NAME,
				new String[] { FIELD_NAME },
				FIELD_PHONE + "=?",
				new String[] { phone },
				null,
				null,
				null
				);
		return cursor;
	}// End of query

	// Delete data
	public void delete(int id) {
		db.delete(TABLE_NAME, FIELD_ID + "=" + Integer.toString(id), null);
	}// End of delete

	// Edit data
	public void edit(int id, String name, String phone) {
		ContentValues cv = new ContentValues();
		cv.put(FIELD_NAME, name);
		cv.put(FIELD_PHONE, phone);
		db.update(TABLE_NAME, cv, FIELD_ID + "=" + Integer.toString(id), null);
	}// End of edit
}// End of CallDatabaseHelper

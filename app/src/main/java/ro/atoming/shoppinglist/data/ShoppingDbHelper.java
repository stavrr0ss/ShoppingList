package ro.atoming.shoppinglist.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import static ro.atoming.shoppinglist.data.ShoppingContract.ShoppingEntry;

/**
 * Created by Bogdan on 10/3/2017.
 */

public class ShoppingDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "shoppingList.db";
    public static final int DATABASE_VERSION = 1;
    private static final String SQL_CREATE_ENTRIES = "CREATE TABLE " + ShoppingEntry.TABLE_NAME + "(" +
            ShoppingEntry.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            ShoppingEntry.COLUMN_PRODUCTS + " TEXT NOT NULL," +
            ShoppingEntry.COLUMN_PRICE + " INTEGER NOT NULL DEFAULT 0," +
            ShoppingEntry.COLUMN_QUANTITY + " INTEGER NOT NULL DEFAULT 1);";

    public ShoppingDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_CREATE_ENTRIES);
        Log.v("The table created = ", SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ShoppingEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}

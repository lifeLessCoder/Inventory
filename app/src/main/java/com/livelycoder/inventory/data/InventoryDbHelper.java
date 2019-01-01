package com.livelycoder.inventory.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.livelycoder.inventory.data.InventoryContract.InventoryEntry;

public final class InventoryDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "inventory.db";
    private static final int DATABASE_VERSION = 1;

    public InventoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE " + InventoryEntry.TABLE_NAME + "("
                        + InventoryEntry._ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,"
                        + InventoryEntry.COLUMN_PRODUCT_NAME + " TINYTEXT NOT NULL,"
                        + InventoryEntry.COLUMN_PRICE + " FLOAT NOT NULL,"
                        + InventoryEntry.COLUMN_QUANTITY + " INT NOT NULL,"
                        + InventoryEntry.COLUMN_SUPPLIER_NAME + " TINYTEXT NOT NULL,"
                        + InventoryEntry.COLUMN_SUPPLIER_PHONE_NUMBER + " BIGINT NOT NULL);"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
    }
}

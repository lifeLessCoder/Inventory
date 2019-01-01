package com.livelycoder.inventory.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.livelycoder.inventory.data.InventoryContract.InventoryEntry;

public class InventoryProvider extends ContentProvider {
    private static final String LOG_TAG = InventoryProvider.class.getSimpleName();
    private InventoryDbHelper dbHelper;

    /**
     * URI matcher code for the content URI for the inventory table
     */
    private static final int INVENTORY = 100;

    /**
     * URI matcher code for the content URI for a single product in the inventory table
     */
    private static final int PRODUCT_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {

        uriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY, INVENTORY);

        uriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY + "/#", PRODUCT_ID);
    }

    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {
        dbHelper = new InventoryDbHelper(getContext());
        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        String[] selectionArgs, @Nullable String sortOrder) {

        SQLiteDatabase database = dbHelper.getReadableDatabase();

        Cursor cursor;

        switch (uriMatcher.match(uri)) {
            case INVENTORY:
                cursor = database.query(
                        InventoryEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case PRODUCT_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                cursor = database.query(
                        InventoryEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            default:
                throw new IllegalArgumentException("Can't query known uri : " + uri);
        }

        if (getContext() != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }

        return cursor;
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        String productName = values.getAsString(InventoryEntry.COLUMN_PRODUCT_NAME);
        if (productName == null) {
            throw new IllegalArgumentException("Product requires a name");
        }

        String productPrice = values.getAsString(InventoryEntry.COLUMN_PRICE);
        if (productPrice == null) {
            throw new IllegalArgumentException("Product requires a price");
        }

        String productQuantity = values.getAsString(InventoryEntry.COLUMN_QUANTITY);
        if (productQuantity == null) {
            throw new IllegalArgumentException("Product requires a quantity");
        }

        switch (uriMatcher.match(uri)) {
            case INVENTORY:
                return insertProduct(values, uri);
            default:
                throw new IllegalArgumentException("Insertion is not supported for : " + uri);
        }
    }

    /**
     * Helper method to insert a product to the inventory table
     *
     * @param values to insert
     * @param uri    to insert at
     * @return uri of the newly inserted row
     */
    private Uri insertProduct(ContentValues values, Uri uri) {

        SQLiteDatabase database = dbHelper.getWritableDatabase();

        long rowId = database.insert(InventoryEntry.TABLE_NAME, null, values);

        if (rowId == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        if (getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return ContentUris.withAppendedId(uri, rowId);

    }

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(@NonNull Uri uri, ContentValues values, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        if (values.containsKey(InventoryEntry.COLUMN_PRODUCT_NAME)) {
            String productName = values.getAsString(InventoryEntry.COLUMN_PRODUCT_NAME);
            if (productName == null) {
                throw new IllegalArgumentException("Product requires a name");
            }
        }

        if (values.containsKey(InventoryEntry.COLUMN_PRICE)) {
            String productPrice = values.getAsString(InventoryEntry.COLUMN_PRICE);
            if (productPrice == null) {
                throw new IllegalArgumentException("Product requires a price");
            }
        }

        if (values.containsKey(InventoryEntry.COLUMN_QUANTITY)) {
            String productQuantity = values.getAsString(InventoryEntry.COLUMN_QUANTITY);
            if (productQuantity == null) {
                throw new IllegalArgumentException("Product requires quantity");
            }
        }

        if (values.size() == 0) {
            return 0;
        }
        switch (uriMatcher.match(uri)) {
            case INVENTORY:
                return updatePets(values, selection, selectionArgs, uri);
            case PRODUCT_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updatePets(values, selection, selectionArgs, uri);
            default:
                throw new IllegalArgumentException("Updating not supported for uri : " + uri);
        }
    }

    /**
     * Update a product
     *
     * @param values        to update with
     * @param selection     for the rows
     * @param selectionArgs for the selection
     * @return no of rows updates
     */
    private int updatePets(ContentValues values, String selection, String[] selectionArgs, Uri uri) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        int rowsUpdated = database.update(
                InventoryEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs
        );

        if (getContext() != null && rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;

    }


    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        int rowsDeleted;

        switch (uriMatcher.match(uri)) {
            case INVENTORY:
                rowsDeleted = database.delete(
                        InventoryEntry.TABLE_NAME,
                        selection,
                        selectionArgs
                );

                if (getContext() != null && rowsDeleted != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }

                return rowsDeleted;
            case PRODUCT_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(
                        InventoryEntry.TABLE_NAME,
                        selection,
                        selectionArgs
                );

                if (getContext() != null) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }

                return rowsDeleted;
            default:
                throw new IllegalArgumentException("Deletion not supported for uri : " + uri);
        }
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        int match = uriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return InventoryEntry.CONTENT_DIR_TYPE;
            case PRODUCT_ID:
                return InventoryEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}

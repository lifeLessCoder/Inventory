package com.livelycoder.inventory.activities;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.livelycoder.inventory.R;
import com.livelycoder.inventory.adapters.ProductCursorAdapter;
import com.livelycoder.inventory.data.InventoryContract.InventoryEntry;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private ProductCursorAdapter adapter;

    private static final int LOADER_ID = 77;

    @BindView(R.id.main_product_list_view)
    ListView listView;

    @BindView(R.id.empty_view)
    View emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        adapter = new ProductCursorAdapter(this, null);

        listView.setAdapter(adapter);

        listView.setEmptyView(emptyView);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                Uri uri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);
                intent.setData(uri);
                startActivity(intent);
            }
        });

        getSupportLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @OnClick(R.id.main_fab_add)
    public void addProduct(View view) {
        startActivity(new Intent(this, EditorActivity.class));
    }

    /**
     * Inserts dummy data into the database
     */
    private void insertIntoDatabase() {
        ContentValues values = new ContentValues();

        values.put(InventoryEntry.COLUMN_PRODUCT_NAME, "Apple juice");
        values.put(InventoryEntry.COLUMN_PRICE, 25.23);
        values.put(InventoryEntry.COLUMN_QUANTITY, 12);
        values.put(InventoryEntry.COLUMN_SUPPLIER_NAME, "REAL");
        values.put(InventoryEntry.COLUMN_SUPPLIER_PHONE_NUMBER, 8889998889L);

        Uri uri = getContentResolver()
                .insert(InventoryEntry.CONTENT_URI, values);

        if (uri != null)
            Toast.makeText(this, R.string.editor_insert_product_successful,
                    Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(this, R.string.editor_insert_product_failed,
                    Toast.LENGTH_SHORT).show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                insertIntoDatabase();
                return true;
            case R.id.action_delete_all:
                deleteInventory();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteInventory() {
        int rowsDeleted = getContentResolver().delete(InventoryEntry.CONTENT_URI, null, null);
        if (rowsDeleted != 0) {
            Toast.makeText(this, R.string.main_delete_inventory_successful, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.main_delete_inventory_failed, Toast.LENGTH_SHORT).show();
        }
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int i, @Nullable Bundle bundle) {

        String[] projection = new String[]{
                InventoryEntry._ID,
                InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryEntry.COLUMN_PRICE,
                InventoryEntry.COLUMN_QUANTITY
        };

        return new CursorLoader(
                this,
                InventoryEntry.CONTENT_URI,
                projection,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        adapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }
}

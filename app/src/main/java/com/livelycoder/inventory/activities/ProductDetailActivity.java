package com.livelycoder.inventory.activities;

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
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.livelycoder.inventory.R;
import com.livelycoder.inventory.data.InventoryContract.InventoryEntry;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ProductDetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private Uri currentProductUri;

    private static final int LOADER_ID = 55;

    @BindView(R.id.detail_name)
    TextView detailName;

    @BindView(R.id.detail_price)
    TextView detailPrice;

    @BindView(R.id.detail_quantity)
    TextView detailQuantity;

    @BindView(R.id.detail_supplier)
    TextView detailSupplier;

    @BindView(R.id.detail_supplier_phone)
    TextView detailSupplierPhone;

    private int productQuantity;

    private long supplierPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);
        ButterKnife.bind(this);

        currentProductUri = getIntent().getData();

        if (currentProductUri != null) {
            getSupportLoaderManager().initLoader(LOADER_ID, null, this);
        }

    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int i, @Nullable Bundle bundle) {
        return new CursorLoader(
                this,
                currentProductUri,
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {

        if (cursor.moveToFirst()) {
            int productNameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_QUANTITY);
            int supplierNameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_SUPPLIER_NAME);
            int supplierPhoneColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_SUPPLIER_PHONE_NUMBER);

            String productName = cursor.getString(productNameColumnIndex);
            detailName.setText(getString(R.string.item_product_name, productName));

            float productPrice = cursor.getFloat(priceColumnIndex);
            detailPrice.setText(getString(R.string.item_product_price, productPrice));

            productQuantity = cursor.getInt(quantityColumnIndex);

            detailQuantity.setText(getString(R.string.item_product_quantity, productQuantity));

            String productSupplier = cursor.getString(supplierNameColumnIndex);

            if (TextUtils.isEmpty(productSupplier)) {
                productSupplier = "NA";
            }
            detailSupplier.setText(getString(R.string.detail_supplier_name, productSupplier));

            supplierPhone = cursor.getLong(supplierPhoneColumnIndex);

            if (supplierPhone == 0) {
                detailSupplierPhone.setText(R.string.phone_not_available);
            } else {
                detailSupplierPhone.setText(getString(R.string.detail_supplier_phone, supplierPhone));
            }
        }

    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }

    @OnClick(R.id.minus_button)
    public void decreaseQuantity() {
        if (productQuantity == 0) {
            return;
        }
        productQuantity--;
        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_QUANTITY, productQuantity);
        int rowsUpdated = getContentResolver().update(
                currentProductUri,
                values,
                null,
                null
        );

        if (rowsUpdated != 0) {
            detailQuantity.setText(getString(R.string.item_product_quantity, productQuantity));
        }

    }

    @OnClick(R.id.plus_button)
    public void increaseQuantity() {
        productQuantity++;
        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_QUANTITY, productQuantity);
        int rowsUpdated = getContentResolver().update(
                currentProductUri,
                values,
                null,
                null
        );

        if (rowsUpdated != 0) {
            detailQuantity.setText(getString(R.string.item_product_quantity, productQuantity));
        }
    }

    @OnClick(R.id.order_button)
    public void order() {
        if (supplierPhone == 0) {
            Toast.makeText(this, R.string.no_phone_number, Toast.LENGTH_SHORT).show();
            return;
        }
        Intent callIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + supplierPhone));
        startActivity(callIntent);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.product_detail, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_delete) {
            int rowsDeleted = getContentResolver().delete(
                    currentProductUri,
                    null,
                    null
            );

            if (rowsDeleted != 0) {
                Toast.makeText(this, R.string.editor_delete_successful, Toast.LENGTH_SHORT).show();
                finish();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

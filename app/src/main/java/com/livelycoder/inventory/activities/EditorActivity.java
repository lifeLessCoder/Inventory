package com.livelycoder.inventory.activities;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.livelycoder.inventory.R;
import com.livelycoder.inventory.data.InventoryContract.InventoryEntry;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    @BindView(R.id.product_name_edit_text)
    EditText productNameEditText;

    @BindView(R.id.price_edit_text)
    EditText priceEditText;

    @BindView(R.id.quantity_edit_text)
    EditText quantityEditText;

    @BindView(R.id.supplier_name_edit_text)
    EditText supplierNameEditText;

    @BindView(R.id.supplier_phone_edit_text)
    EditText supplierPhoneEditText;

    private Uri currentProductUri;

    private static final int LOADER_ID = 99;

    private boolean productHasChanged;

    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            productHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        ButterKnife.bind(this);

        productNameEditText.setOnTouchListener(touchListener);
        priceEditText.setOnTouchListener(touchListener);
        quantityEditText.setOnTouchListener(touchListener);
        supplierNameEditText.setOnTouchListener(touchListener);
        supplierPhoneEditText.setOnTouchListener(touchListener);

        Intent intent = getIntent();

        currentProductUri = intent.getData();

        if (currentProductUri != null) {
            setTitle(R.string.editor_edit_product);
            getSupportLoaderManager().initLoader(LOADER_ID, null, this);
        } else {
            setTitle(R.string.editor_add_product);
            invalidateOptionsMenu();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.editor, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveProduct();
                return true;
            case android.R.id.home:
                if (!productHasChanged) {
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }

                DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    }
                };

                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (currentProductUri == null) {
            MenuItem item = menu.findItem(R.id.action_delete);
            item.setVisible(false);
        }
        return true;
    }

    private void deleteProduct() {
        int rowsDeleted = getContentResolver().delete(currentProductUri, null, null);
        if (rowsDeleted != 0) {
            Toast.makeText(this, R.string.editor_delete_successful, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.editor_delete_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteProduct();
                finish();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Saves the product into the database
     */
    private void saveProduct() {

        String productName = productNameEditText.getText().toString().trim();
        String priceString = priceEditText.getText().toString();
        String quantityString = quantityEditText.getText().toString();
        String supplierName = supplierNameEditText.getText().toString();
        String supplierPhoneString = supplierPhoneEditText.getText().toString();

        if (currentProductUri == null && TextUtils.isEmpty(productName) && TextUtils.isEmpty(priceString)
                && TextUtils.isEmpty(quantityString)
                && TextUtils.isEmpty(supplierName) && TextUtils.isEmpty(supplierPhoneString))
            return;

        if (TextUtils.isEmpty(productName)) {
            Toast.makeText(this, R.string.editor_name_required, Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(priceString)) {
            Toast.makeText(this, R.string.editor_price_required, Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(quantityString)) {
            Toast.makeText(this, R.string.editor_quantity_required, Toast.LENGTH_SHORT).show();
            return;
        }

        float price = Float.parseFloat(priceString);
        int quantity = Integer.parseInt(quantityString);
        long supplierPhone = 0;

        if (!TextUtils.isEmpty(supplierPhoneString))
            supplierPhone = Long.parseLong(supplierPhoneString);

        ContentValues values = new ContentValues();

        values.put(InventoryEntry.COLUMN_PRODUCT_NAME, productName);
        values.put(InventoryEntry.COLUMN_PRICE, price);
        values.put(InventoryEntry.COLUMN_QUANTITY, quantity);
        values.put(InventoryEntry.COLUMN_SUPPLIER_NAME, supplierName);
        values.put(InventoryEntry.COLUMN_SUPPLIER_PHONE_NUMBER, supplierPhone);

        if (currentProductUri == null) {
            // Inserting product
            Uri uri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);

            if (uri != null)
                Toast.makeText(this, R.string.editor_insert_product_successful,
                        Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, R.string.editor_insert_product_failed,
                        Toast.LENGTH_SHORT).show();

        } else {
            // Updating product
            int rowsUpdated = getContentResolver().update(
                    currentProductUri,
                    values,
                    null,
                    null
            );

            if (rowsUpdated != 0) {
                Toast.makeText(this, R.string.editor_update_product_successful,
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.editor_update_product_failed,
                        Toast.LENGTH_SHORT).show();
            }
        }

        finish();
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg)
                .setPositiveButton(R.string.discard, discardButtonClickListener)
                .setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int i, @Nullable Bundle bundle) {
        String[] projection = new String[]{
                InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryEntry.COLUMN_PRICE,
                InventoryEntry.COLUMN_QUANTITY,
                InventoryEntry.COLUMN_SUPPLIER_NAME,
                InventoryEntry.COLUMN_SUPPLIER_PHONE_NUMBER
        };

        return new CursorLoader(
                this,
                currentProductUri,
                projection,
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

            productNameEditText.setText(cursor.getString(productNameColumnIndex));
            priceEditText.setText(cursor.getString(priceColumnIndex));
            quantityEditText.setText(cursor.getString(quantityColumnIndex));
            supplierNameEditText.setText(cursor.getString(supplierNameColumnIndex));
            supplierPhoneEditText.setText(cursor.getString(supplierPhoneColumnIndex));
        }

    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
    }

    @Override
    public void onBackPressed() {
        if (!productHasChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        };

        showUnsavedChangesDialog(discardButtonClickListener);
    }
}

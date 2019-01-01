package com.livelycoder.inventory.adapters;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.livelycoder.inventory.R;
import com.livelycoder.inventory.activities.ProductDetailActivity;
import com.livelycoder.inventory.data.InventoryContract.InventoryEntry;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ProductCursorAdapter extends CursorAdapter {

    @BindView(R.id.item_name)
    TextView productName;

    @BindView(R.id.item_price)
    TextView productPrice;

    @BindView(R.id.item_quantity)
    TextView productQuantity;

    @BindView(R.id.sale_button)
    Button saleButton;

    @BindView(R.id.info_button)
    ImageView infoButton;

    private Context context;

    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
        this.context = context;
    }

    // The newView method is used to inflate a new view and return it,
    // you don't bind any data to the view at this point.
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.item_product, viewGroup, false);
    }

    // The bindView method is used to bind all data to a given view
    // such as setting the text on a TextView.
    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        ButterKnife.bind(this, view);
        int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRICE);
        final int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_QUANTITY);
        int idColumnIndex = cursor.getColumnIndex(InventoryEntry._ID);

        String name = cursor.getString(nameColumnIndex);
        float price = cursor.getFloat(priceColumnIndex);
        final long id = cursor.getLong(idColumnIndex);
        int quantity = cursor.getInt(quantityColumnIndex);

        productName.setText(context.getString(R.string.item_product_name, name));
        productPrice.setText(context.getString(R.string.item_product_price, price));
        productQuantity.setText(context.getString(R.string.item_product_quantity, quantity));

        saleButton.setTag(id);
        infoButton.setTag(id);

        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                long id = (long) v.getTag();

                Cursor curs = null;

                try {
                    curs = context.getContentResolver().query(
                            ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id),
                            new String[]{InventoryEntry.COLUMN_QUANTITY},
                            null,
                            null,
                            null
                    );

                    int qty = 0;

                    if (curs != null && curs.moveToFirst()) {
                        qty = curs.getInt(curs.getColumnIndex(InventoryEntry.COLUMN_QUANTITY));
                    }

                    qty--;

                    if (qty > -1) {
                        ContentValues values = new ContentValues();
                        values.put(InventoryEntry.COLUMN_QUANTITY, qty);

                        context.getContentResolver().update(
                                ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id),
                                values,
                                null,
                                null
                        );
                    } else {
                        Toast.makeText(context, R.string.item_out_of_stock, Toast.LENGTH_SHORT).show();
                    }
                } finally {
                    if (curs != null) {
                        curs.close();
                    }
                }
            }
        });

    }

    @OnClick(R.id.info_button)
    public void viewProductDetails(View v) {
        long id = (long) v.getTag();
        Intent intent = new Intent(context, ProductDetailActivity.class);
        intent.setData(ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id));
        context.startActivity(intent);
    }
}

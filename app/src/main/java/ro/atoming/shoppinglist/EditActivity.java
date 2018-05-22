package ro.atoming.shoppinglist;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import static ro.atoming.shoppinglist.data.ShoppingContract.ShoppingEntry;

public class EditActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_PRODUCT_LOADER = 12;
    private Uri mUri;
    private EditText mProductEdit;
    private EditText mPriceEdit;
    private EditText mQuantityEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        Intent intent = getIntent();
        mUri = intent.getData();

        if (mUri == null) {
            // This is a new product, so change the app bar to say "Add a Product"
            setTitle("Add Product");
        } else {
            // Otherwise this is an existing product, so change app bar to say "Edit Product"
            setTitle("Edit Product");
            // Initialize a loader to read the product data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }


        mProductEdit = findViewById(R.id.edit_product_name);
        mPriceEdit = findViewById(R.id.edit_price);
        mQuantityEdit = findViewById(R.id.edit_quantity);

        Button saveButton = findViewById(R.id.save_product);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveProduct();
            }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {ShoppingEntry._ID,
                ShoppingEntry.COLUMN_PRODUCTS,
                ShoppingEntry.COLUMN_PRICE,
                ShoppingEntry.COLUMN_QUANTITY
        };
        return new CursorLoader(this,
                mUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        boolean cursorHasValidData = false;
        if (cursor != null && cursor.moveToFirst()) {
            /* We have valid data, continue on to bind the data to the UI */

            int productIndex = cursor.getColumnIndex(ShoppingEntry.COLUMN_PRODUCTS);
            int priceIndex = cursor.getColumnIndex(ShoppingEntry.COLUMN_PRICE);
            int quantityIndex = cursor.getColumnIndex(ShoppingEntry.COLUMN_QUANTITY);

            String product = cursor.getString(productIndex);
            double price = cursor.getDouble(priceIndex);
            int quantity = cursor.getInt(quantityIndex);

            mProductEdit.setText(product);
            mPriceEdit.setText(String.valueOf(price));
            mQuantityEdit.setText(String.valueOf(quantity));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // mProductEdit.setText("");
        // mPriceEdit.setText("");
        // mQuantityEdit.setText("");
    }

    private void deleteProduct() {
        // Only perform the delete if this is an existing product.
        if (mUri != null) {
            int rowsDeleted = getContentResolver().delete(mUri, null, null);
            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    private void saveProduct() {

        String productString = mProductEdit.getText().toString().trim();
        String priceString = mPriceEdit.getText().toString().trim();
        String quantityString = mQuantityEdit.getText().toString().trim();

        if (TextUtils.isEmpty(productString)) {
            Toast.makeText(EditActivity.this, "Please insert product !", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(priceString)) {
            Toast.makeText(this, "Needs a price !", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(quantityString)) {
            Toast.makeText(this, "Needs quantity !", Toast.LENGTH_SHORT).show();
        } else {
            ContentValues values = new ContentValues();
            values.put(ShoppingEntry.COLUMN_PRODUCTS, productString);

            int quantity = 0;
            if (!TextUtils.isEmpty(quantityString)) {
                quantity = Integer.parseInt(quantityString);
            }
            values.put(ShoppingEntry.COLUMN_QUANTITY, quantity);

            double price = 0;
            if (!TextUtils.isEmpty(priceString)) {
                price = Double.parseDouble(priceString);
            }
            values.put(ShoppingEntry.COLUMN_PRICE, price);

            if (mUri == null) {
                Uri newUri = getContentResolver().insert(ShoppingEntry.CONTENT_URI, values);
                if (newUri == null) {
                    Toast.makeText(this, getString(R.string.editor_insert_productt_failed),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, getString(R.string.editor_insert_product_successful),
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                int rowsAffected = getContentResolver().update(mUri, values, null, null);
                if (rowsAffected == 0) {
                    Toast.makeText(this, "Update error !", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Product updated !", Toast.LENGTH_SHORT).show();
                }
            }
            finish();
        }
    }
}

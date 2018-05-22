package ro.atoming.shoppinglist.data;

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

import static android.provider.BaseColumns._ID;
import static ro.atoming.shoppinglist.data.ShoppingContract.ShoppingEntry.COLUMN_PRICE;
import static ro.atoming.shoppinglist.data.ShoppingContract.ShoppingEntry.COLUMN_PRODUCTS;
import static ro.atoming.shoppinglist.data.ShoppingContract.ShoppingEntry.COLUMN_QUANTITY;
import static ro.atoming.shoppinglist.data.ShoppingContract.ShoppingEntry.CONTENT_TYPE_ITEM;
import static ro.atoming.shoppinglist.data.ShoppingContract.ShoppingEntry.CONTENT_TYPE_LIST;
import static ro.atoming.shoppinglist.data.ShoppingContract.ShoppingEntry.TABLE_NAME;

/**
 * Created by Bogdan on 10/8/2017.
 */

public class ProductProvider extends ContentProvider {

    //constant used for defining the table path.
    public static final int PRODUCTS = 100;
    //defining the 2 integer constants for the UriMatcher builder.
    //constant used for defining a single item path .
    public static final int PRODUCT_ID = 101;
    //create a member variable static for the UriMatcher ;
    public static final UriMatcher sUriMatcher = buildUriMatcher();
    private static final String LOG_TAG = ProductProvider.class.getSimpleName();
    //initialize the ShoppingDbHelper in onCreate
    private ShoppingDbHelper mProductDbHelper;

    //constructing an empty UriMatcher
    public static final UriMatcher buildUriMatcher() {

        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        uriMatcher.addURI(ShoppingContract.AUTHORITY, ShoppingContract.PATH_PRODUCTS, PRODUCTS);

        uriMatcher.addURI(ShoppingContract.AUTHORITY, ShoppingContract.PATH_PRODUCTS + "/#", PRODUCT_ID);

        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        mProductDbHelper = new ShoppingDbHelper(getContext());
        return true;
    }
    //  (1) implement query method .


    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase database = mProductDbHelper.getReadableDatabase();
        //Uri match code
        int match = sUriMatcher.match(uri);

        Cursor returnedCursor;

        switch (match) {
            // the Uri for the entire table.
            case PRODUCTS:
                returnedCursor = database.query(TABLE_NAME, projection, selection, selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            // the Uri for a specific item
            case PRODUCT_ID:
                selection = PRODUCT_ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                returnedCursor = database.query(TABLE_NAME, projection, selection, selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknow Uri " + uri);
        }
        returnedCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return returnedCursor;
    }

    // (3) implement the getType
    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return CONTENT_TYPE_LIST;
            case PRODUCT_ID:
                return CONTENT_TYPE_ITEM;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }


    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return insertProduct(contentValues, uri);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertProduct(ContentValues values, Uri uri) {

        String product = values.getAsString(COLUMN_PRODUCTS);
        if (product == null) {
            throw new IllegalArgumentException("Product requires a name");
        }

        Double price = values.getAsDouble(COLUMN_PRICE);
        if (price < 0 || price == null) {
            throw new IllegalArgumentException("Products needs a price ");
        }
        Integer quantity = values.getAsInteger(COLUMN_QUANTITY);
        if (quantity < 0 || quantity == null) {
            throw new IllegalArgumentException("Insert Quantity");
        }

        SQLiteDatabase database = mProductDbHelper.getWritableDatabase();

        // Insert the new product with the given values
        long id = database.insert(TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        // Notify all listeners that the data has changed for the product content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    //implement the delete method
    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        // Get access to the database and write URI matching code to recognize a single item
        final SQLiteDatabase db = mProductDbHelper.getWritableDatabase();

        int match = sUriMatcher.match(uri);

        int productsDeleted;// starts as 0

        switch (match) {
            case PRODUCT_ID:
                // Get the task ID from the URI path
                String id = uri.getPathSegments().get(1);
                // Use selections/selectionArgs to filter for this ID
                productsDeleted = db.delete(TABLE_NAME, "_id=?", new String[]{id});
                break;
            case PRODUCTS:
                productsDeleted = db.delete(TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri" + uri);
        }
        // Notify the resolver of a change and return the number of items deleted
        if (productsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // Return the number of tasks deleted
        return productsDeleted;
    }

    // (5) implement the update method
    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection, @Nullable String[] selectionArgs) {
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return updateProduct(uri, contentValues, selection, selectionArgs);
            case PRODUCT_ID:
                selection = _ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateProduct(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update not suported for " + uri);
        }
    }

    private int updateProduct(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (values.containsKey(ShoppingContract.ShoppingEntry.COLUMN_PRODUCTS)) {
            String product = values.getAsString(ShoppingContract.ShoppingEntry.COLUMN_PRODUCTS);
            if (product == null) {
                throw new IllegalArgumentException("Product requires a name");
            }
        }
        if (values.containsKey(ShoppingContract.ShoppingEntry.COLUMN_QUANTITY)) {
            Integer quantity = values.getAsInteger(ShoppingContract.ShoppingEntry.COLUMN_QUANTITY);
            if (quantity != null && quantity < 0) {
                throw new IllegalArgumentException("Product quantity not available");
            }
        }
        if (values.containsKey(ShoppingContract.ShoppingEntry.COLUMN_PRICE)) {
            Double price = values.getAsDouble(ShoppingContract.ShoppingEntry.COLUMN_PRICE);
            if (price != null && price < 0) {
                throw new IllegalArgumentException("Product requires a price");
            }
        }
        if (values.size() == 0) {
            return 0;
        }
        SQLiteDatabase database = mProductDbHelper.getWritableDatabase();
        int rowsUpdated = database.update(TABLE_NAME, values, selection, selectionArgs);

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

}

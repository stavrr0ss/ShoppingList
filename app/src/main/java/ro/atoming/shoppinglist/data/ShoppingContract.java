package ro.atoming.shoppinglist.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Bogdan on 10/3/2017.
 */

public class ShoppingContract {

    //defining constants for building the Uri.

    public static final String AUTHORITY = "ro.atoming.shoppinglist";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    public static final String PATH_PRODUCTS = "shoppingList";

    private ShoppingContract() {
    }

    public static class ShoppingEntry implements BaseColumns {

        //building the content Uri
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_PRODUCTS).build();

        //defining constants for table name and columns

        public static final String TABLE_NAME = "shoppingList";
        public static final String COLUMN_ID = BaseColumns._ID;
        public static final String COLUMN_PRODUCTS = "products";
        public static final String COLUMN_PRICE = "price";
        public static final String COLUMN_QUANTITY = "quantity";

        public static final String CONTENT_TYPE_LIST =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + AUTHORITY + PATH_PRODUCTS;

        public static final String CONTENT_TYPE_ITEM =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + AUTHORITY + PATH_PRODUCTS;
    }

}

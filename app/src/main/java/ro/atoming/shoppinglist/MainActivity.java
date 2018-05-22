package ro.atoming.shoppinglist;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import ro.atoming.shoppinglist.data.ShoppingContract;
import ro.atoming.shoppinglist.data.ShoppingDbHelper;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int PRODUCT_LOADER_ID = 11;
    private RecyclerView mRecyclerView;
    private TextView mTotalPrice;
    private ProductsCursorAdapter mCursorAdapter;
    private ShoppingDbHelper mProductDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mProductDbHelper = new ShoppingDbHelper(getApplicationContext());//initialize the ShoppingDbHelper

        FloatingActionButton fab = findViewById(R.id.fab_addProduct);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditActivity.class);
                startActivity(intent);
            }
        });
        FloatingActionButton deleteListFab = findViewById(R.id.fab_deleteList);
        deleteListFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteList();
            }
        });
        mTotalPrice = findViewById(R.id.display_total_price);

        mRecyclerView = findViewById(R.id.products_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mCursorAdapter = new ProductsCursorAdapter(this);
        mRecyclerView.setAdapter(mCursorAdapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int id = (int) viewHolder.itemView.getTag();
                String stringId = Integer.toString(id);
                Uri uri = ShoppingContract.ShoppingEntry.CONTENT_URI;
                uri = uri.buildUpon().appendPath(stringId).build();

                getContentResolver().delete(uri, null, null);

                getLoaderManager().restartLoader(PRODUCT_LOADER_ID, null, MainActivity.this);
            }
        }).attachToRecyclerView(mRecyclerView);


        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                Intent intent = new Intent(MainActivity.this, EditActivity.class);
                int id = (int) viewHolder.itemView.getTag();

                //String stringId = Integer.toString(id);
                //Uri uri = ShoppingContract.ShoppingEntry.CONTENT_URI;
                //uri = uri.buildUpon().appendPath(stringId).build();

                Uri currentProductUri = ContentUris.withAppendedId(ShoppingContract.ShoppingEntry.CONTENT_URI, id);

                intent.setData(currentProductUri);
                startActivity(intent);
            }
        }).attachToRecyclerView(mRecyclerView);

        getLoaderManager().initLoader(PRODUCT_LOADER_ID, null, this);

    }

    private void deleteList() {
        SQLiteDatabase db = mProductDbHelper.getWritableDatabase();
        //db.delete(ShoppingContract.ShoppingEntry.TABLE_NAME,null,null);
        String selection = ShoppingContract.ShoppingEntry.COLUMN_ID;
        int rowsDeleted = getContentResolver().delete(ShoppingContract.ShoppingEntry.CONTENT_URI, selection, null);
        getLoaderManager().restartLoader(PRODUCT_LOADER_ID, null, this);
        if (rowsDeleted != 0) {
            Toast.makeText(this, "List deleted !", Toast.LENGTH_SHORT).show();
        }
        //finish();
    }

    //get the total price for this list
    public double totalPrice() {
        SQLiteDatabase db = mProductDbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(" + ShoppingContract.ShoppingEntry.COLUMN_PRICE +
                "*" + ShoppingContract.ShoppingEntry.COLUMN_QUANTITY
                + ") as Total FROM " + ShoppingContract.ShoppingEntry.TABLE_NAME, null);
        Log.d("MainActivity.class", "Total price cursor : " + cursor);
        double total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(cursor.getColumnIndex("Total"));// get final total
        }
        cursor.close();
        return total;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                ShoppingContract.ShoppingEntry._ID,
                ShoppingContract.ShoppingEntry.COLUMN_PRODUCTS,
                ShoppingContract.ShoppingEntry.COLUMN_QUANTITY,
                ShoppingContract.ShoppingEntry.COLUMN_PRICE,};

        return new CursorLoader(this,
                ShoppingContract.ShoppingEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mCursorAdapter.swapCursor(cursor);
        mTotalPrice.setText(String.valueOf(totalPrice()));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }

}

package ro.atoming.shoppinglist;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Locale;

import ro.atoming.shoppinglist.data.ShoppingContract;

/**
 * Created by Bogdan on 10/8/2017.
 */

public class ProductsCursorAdapter extends RecyclerView.Adapter<ProductsCursorAdapter.ProductsViewHolder> {

    private Cursor mCursor;
    private Context mContext;

    /**
     * Constructor for the CustomCursorAdapter that initializes the Context.
     *
     * @param mContext the current Context
     */

    // (1) Implement the cursorAdapter methods .
    public ProductsCursorAdapter(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public ProductsCursorAdapter.ProductsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_list, parent, false);
        return new ProductsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ProductsCursorAdapter.ProductsViewHolder holder, int position) {
        //get the indices

        int productIndex = mCursor.getColumnIndex(ShoppingContract.ShoppingEntry.COLUMN_PRODUCTS);
        int priceIndex = mCursor.getColumnIndex(ShoppingContract.ShoppingEntry.COLUMN_PRICE);
        int quantityIndex = mCursor.getColumnIndex(ShoppingContract.ShoppingEntry.COLUMN_QUANTITY);
        int productId = mCursor.getColumnIndex(ShoppingContract.ShoppingEntry.COLUMN_ID);

        mCursor.moveToPosition(position);//get the current cursor position

        //retrieve the values from the cursor
        String product = mCursor.getString(productIndex);
        double price = mCursor.getDouble(priceIndex);
        int quantity = mCursor.getInt(quantityIndex);
        int id = mCursor.getInt(productId);

        //set the values
        holder.productNameTextView.setText(product);
        holder.priceTextView.setText(String.valueOf(price));
        holder.quantityTextView.setText(String.valueOf(quantity));

        //set the font
        AssetManager am = mContext.getAssets();
        Typeface custom_font = Typeface.createFromAsset(am, String.format(Locale.US, "font/%s", "architects_daughter.ttf"));

        holder.productNameTextView.setTypeface(custom_font);
        holder.priceTextView.setTypeface(custom_font);
        holder.quantityTextView.setTypeface(custom_font);

        holder.itemView.setTag(id);

    }

    @Override
    public int getItemCount() {
        if (mCursor == null) {
            return 0;
        }
        return mCursor.getCount();
    }

    /**
     * When data changes and a re-query occurs, this function swaps the old Cursor
     * with a newly updated Cursor (Cursor c) that is passed in.
     */
    public Cursor swapCursor(Cursor c) {
        // check if this cursor is the same as the previous cursor (mCursor)
        if (mCursor == c) {
            return null; // bc nothing has changed
        }
        Cursor temp = mCursor;
        this.mCursor = c; // new cursor value assigned

        //check if this is a valid cursor, then update the cursor
        if (c != null) {
            this.notifyDataSetChanged();
        }
        return temp;
    }

    class ProductsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        //Class variable for the product details in item_list layout.
        private TextView productNameTextView;
        private TextView priceTextView;
        private TextView quantityTextView;
        private View crossLine;


        //Contructor for the ViewHolder .
        public ProductsViewHolder(View itemView) {
            super(itemView);

            productNameTextView = itemView.findViewById(R.id.product_name);
            priceTextView = itemView.findViewById(R.id.product_price);
            quantityTextView = itemView.findViewById(R.id.product_quantity);
            crossLine = itemView.findViewById(R.id.cross_line);

            itemView.setOnClickListener(this);
        }

        /**
         * used to linecross the checked item, setting the visibility of the View .
         */
        @Override
        public void onClick(View view) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            if (crossLine.getVisibility() == View.GONE) {
                crossLine.setVisibility(View.VISIBLE);
            } else {
                crossLine.setVisibility(View.GONE);
            }
        }
    }
}

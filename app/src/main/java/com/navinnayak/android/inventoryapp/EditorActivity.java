package com.navinnayak.android.inventoryapp;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.navinnayak.android.inventoryapp.data.ProductContract.ProductEntry;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final int EXISTING_PRODUCT_LOADER = 0;
    private Uri mCurrentProductUri;

    private EditText mNameEditText;
    private EditText mPriceEditText;
    private EditText mQuantityEditText;
    private EditText mSupplierNameEditText;
    private EditText mPhoneNumberEditText;
    private boolean mProductHasChanged = false;
    private boolean validProductData = true;

    /**
     * OnTouchListener that listens for any touch on a View.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            Log.d("message", "onTouch");

            return false;

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        Log.d("message", "onCreate");

        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();
        if (mCurrentProductUri == null) {
            setTitle(getString(R.string.editor_activity_title_new_product));
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.editor_activity_title_edit_product));
            getSupportLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }

        mNameEditText = findViewById(R.id.product_name_edit_text);
        mPriceEditText = findViewById(R.id.product_price_edit_text);
        mQuantityEditText = findViewById(R.id.product_quantity_edit_text);
        mSupplierNameEditText = findViewById(R.id.product_supplier_name);
        mPhoneNumberEditText = findViewById(R.id.product_supplier_phone_number_edit_text);

        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mSupplierNameEditText.setOnTouchListener(mTouchListener);
        mPhoneNumberEditText.setOnTouchListener(mTouchListener);
    }

    private void saveProduct() {

        String nameString = mNameEditText.getText().toString().trim();
        String supplierNameString = mSupplierNameEditText.getText().toString().trim();
        String supplierPhoneNumberString = mPhoneNumberEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();

        if (mCurrentProductUri == null && TextUtils.isEmpty(nameString) && TextUtils.isEmpty(supplierNameString) &&
                TextUtils.isEmpty(supplierPhoneNumberString) && TextUtils.isEmpty(priceString) && TextUtils.isEmpty(quantityString)) {
            return;
        }

        // Create a ContentValues object where column names are the keys,
        // and product attributes from the editor are the values.
        ContentValues values = new ContentValues();

        // Check that the product has a name.
        if (!TextUtils.isEmpty(nameString)) {
            values.put(ProductEntry.COLUMN_PRODUCT_NAME, nameString);
        } else {
            Toast.makeText(this, getString(R.string.editor_product_requires_name),
                    Toast.LENGTH_SHORT).show();
            validProductData = false;
            return;
        }

        // Check that the name of the supplier of the product is provided.
        if (!TextUtils.isEmpty(supplierNameString)) {
            values.put(ProductEntry.COLUMN_SUPPLIER_NAME, supplierNameString);
        } else {
            Toast.makeText(this, getString(R.string.editor_product_requires_supplier_name),
                    Toast.LENGTH_SHORT).show();
            validProductData = false;

            return;
        }

        // Check that the product has a valid phone number.
        if (!TextUtils.isEmpty(supplierPhoneNumberString) && Integer.parseInt(supplierPhoneNumberString) > 0) {
            values.put(ProductEntry.COLUMN_PHONE_NUMBER, supplierPhoneNumberString);
        } else if (supplierPhoneNumberString.length() == 0) {
            Toast.makeText(this, getString(R.string.editor_product_requires_supplier_phone_number),
                    Toast.LENGTH_SHORT).show();
            return;
        } else if (Integer.parseInt(supplierPhoneNumberString) == 0) {
            Toast.makeText(this, getString(R.string.editor_product_invalid_supplier_phone_number),
                    Toast.LENGTH_SHORT).show();
            validProductData = false;

            return;
        }

        // Check that the product has a valid unit price.
        if (!TextUtils.isEmpty(priceString) && Integer.parseInt(priceString) > 0) {
            values.put(ProductEntry.COLUMN_PRICE, priceString);
        } else if (priceString.length() == 0) {
            Toast.makeText(this, getString(R.string.editor_product_requires_price),
                    Toast.LENGTH_SHORT).show();
            return;
        } else if (Integer.parseInt(priceString) == 0) {
            Toast.makeText(this, getString(R.string.editor_product_requires_positive_price),
                    Toast.LENGTH_SHORT).show();
            validProductData = false;

            return;
        }

        // Check that the product has a valid unit stock.
        if (!TextUtils.isEmpty(quantityString) && Integer.parseInt(quantityString) > 0) {
            values.put(ProductEntry.COLUMN_QUANTITY, quantityString);
        } else if (quantityString.length() == 0) {
            Toast.makeText(this, getString(R.string.editor_product_requires_positive_stock_level),
                    Toast.LENGTH_SHORT).show();
            return;
        } else if (Integer.parseInt(quantityString) == 0) {
            Toast.makeText(this, getString(R.string.editor_product_requires_positive_stock_level),
                    Toast.LENGTH_SHORT).show();
            validProductData = false;

            return;
        }

        // If all the checks on the input values are passed,
        // then the data for the product are valid.
        validProductData = true;
        if (mCurrentProductUri == null) {
            Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);
            if (newUri == null) {
                Toast.makeText(this, getString(R.string.editor_insert_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_insert_product_successful),
                        Toast.LENGTH_SHORT).show();

            }
        } else {
            int rowsAffected = getContentResolver().update(mCurrentProductUri, values, null, null);
            if (rowsAffected == 0) {
                Toast.makeText(this, getString(R.string.editor_update_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_update_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRICE,
                ProductEntry.COLUMN_QUANTITY,
                ProductEntry.COLUMN_SUPPLIER_NAME,
                ProductEntry.COLUMN_PHONE_NUMBER
        };
        return new CursorLoader(this,
                mCurrentProductUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        if (cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_QUANTITY);
            int supplierNameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_SUPPLIER_NAME);
            int supplierPhoneColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PHONE_NUMBER);

            String currentName = cursor.getString(nameColumnIndex);
            final int currentPrice = cursor.getInt(priceColumnIndex);
            final int currentQuantity = cursor.getInt(quantityColumnIndex);
            String currentSupplierName = cursor.getString(supplierNameColumnIndex);
            final int currentSupplierPhone = cursor.getInt(supplierPhoneColumnIndex);

            mNameEditText.setText(currentName);
            mPriceEditText.setText(Integer.toString(currentPrice));
            mQuantityEditText.setText(Integer.toString(currentQuantity));
            mPhoneNumberEditText.setText(Integer.toString(currentSupplierPhone));
            mSupplierNameEditText.setText(currentSupplierName);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mSupplierNameEditText.setText("");
        mPhoneNumberEditText.setText("");
        mPriceEditText.setText("");
        mQuantityEditText.setText("");
    }

    /**
     * Perform the deletion of the product in the database.
     */
    private void deleteProduct() {
        if (mCurrentProductUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);
            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.editor_delete_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the product.
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mCurrentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveProduct();
                if (validProductData) {
                    finish();
                }
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;

            case android.R.id.home:
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
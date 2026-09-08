package com.sannie.smartpantry.ui;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.sannie.smartpantry.R;
import com.sannie.smartpantry.data.DatabaseHelper;
import com.sannie.smartpantry.data.PantryItem;

import java.util.Calendar;
import java.util.Locale;

/**
 * Add/Edit Ingredient screen (Section 2.2 and 3.1). Handles both modes:
 * - Add mode: launched with no extra, creates a new pantry_items row.
 * - Edit mode: launched with EXTRA_ITEM_ID, updates the existing row and
 *   reveals a Delete button.
 *
 * All fields are validated before saving (Section 3.1: "Input validation
 * on any data entry form").
 */
public class AddEditIngredientActivity extends AppCompatActivity {

    public static final String EXTRA_ITEM_ID = "extra_item_id";
    private static final long NO_ID = -1L;

    private DatabaseHelper dbHelper;
    private long editingItemId = NO_ID;

    private TextInputLayout tilName;
    private TextInputLayout tilQuantity;
    private TextInputLayout tilUnit;
    private TextInputEditText etName;
    private TextInputEditText etQuantity;
    private TextInputEditText etUnit;
    private TextInputEditText etExpiryDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_ingredient);

        dbHelper = DatabaseHelper.getInstance(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        tilName = findViewById(R.id.til_name);
        tilQuantity = findViewById(R.id.til_quantity);
        tilUnit = findViewById(R.id.til_unit);
        etName = findViewById(R.id.et_name);
        etQuantity = findViewById(R.id.et_quantity);
        etUnit = findViewById(R.id.et_unit);
        etExpiryDate = findViewById(R.id.et_expiry_date);

        android.widget.Button btnSave = findViewById(R.id.btn_save_ingredient);
        android.widget.Button btnDelete = findViewById(R.id.btn_delete_ingredient);

        etExpiryDate.setOnClickListener(v -> showDatePicker());

        editingItemId = getIntent().getLongExtra(EXTRA_ITEM_ID, NO_ID);
        if (editingItemId != NO_ID) {
            setTitle(R.string.title_add_edit_ingredient);
            btnDelete.setVisibility(android.view.View.VISIBLE);
            populateForEdit(editingItemId);
        }

        btnSave.setOnClickListener(v -> saveIngredient());
        btnDelete.setOnClickListener(v -> {
            dbHelper.deletePantryItem(editingItemId);
            finish();
        });
    }

    private void populateForEdit(long id) {
        PantryItem item = dbHelper.getPantryItemById(id);
        if (item == null) {
            finish();
            return;
        }
        etName.setText(item.getName());
        etQuantity.setText(formatQuantity(item.getQuantity()));
        etUnit.setText(item.getUnit());
        if (item.hasExpiryDate()) {
            etExpiryDate.setText(item.getExpiryDate());
        }
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String date = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            etExpiryDate.setText(date);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    private void saveIngredient() {
        String name = getText(etName);
        String quantityStr = getText(etQuantity);
        String unit = getText(etUnit);
        String expiryDate = getText(etExpiryDate);

        boolean isValid = true;

        if (TextUtils.isEmpty(name)) {
            tilName.setError(getString(R.string.error_name_required));
            isValid = false;
        } else {
            tilName.setError(null);
        }

        double quantity = 0;
        if (TextUtils.isEmpty(quantityStr)) {
            tilQuantity.setError(getString(R.string.error_quantity_invalid));
            isValid = false;
        } else {
            try {
                quantity = Double.parseDouble(quantityStr);
                if (quantity <= 0) {
                    tilQuantity.setError(getString(R.string.error_quantity_invalid));
                    isValid = false;
                } else {
                    tilQuantity.setError(null);
                }
            } catch (NumberFormatException e) {
                tilQuantity.setError(getString(R.string.error_quantity_invalid));
                isValid = false;
            }
        }

        if (TextUtils.isEmpty(unit)) {
            tilUnit.setError(getString(R.string.error_unit_required));
            isValid = false;
        } else {
            tilUnit.setError(null);
        }

        if (!isValid) {
            return;
        }

        PantryItem item = new PantryItem();
        item.setName(name.trim());
        item.setQuantity(quantity);
        item.setUnit(unit.trim());
        item.setExpiryDate(TextUtils.isEmpty(expiryDate) ? null : expiryDate.trim());

        if (editingItemId == NO_ID) {
            dbHelper.addPantryItem(item);
        } else {
            item.setId(editingItemId);
            dbHelper.updatePantryItem(item);
        }

        Toast.makeText(this, R.string.btn_save, Toast.LENGTH_SHORT).show();
        finish();
    }

    private String getText(EditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }

    private String formatQuantity(double quantity) {
        if (quantity == Math.floor(quantity) && !Double.isInfinite(quantity)) {
            return String.valueOf((long) quantity);
        }
        return String.valueOf(quantity);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

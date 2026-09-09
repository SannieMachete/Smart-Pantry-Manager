package com.sannie.smartpantry.ui;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.sannie.smartpantry.R;
import com.sannie.smartpantry.data.DatabaseHelper;
import com.sannie.smartpantry.data.Recipe;
import com.sannie.smartpantry.data.RecipeIngredient;

import java.util.List;
import java.util.Locale;

/**
 * Recipe Detail screen: shows the full ingredient list and
 * preparation method for a single recipe, reached via an Intent extra
 * carrying the recipe's database id.
 */
public class RecipeDetailActivity extends AppCompatActivity {

    public static final String EXTRA_RECIPE_ID = "extra_recipe_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        long recipeId = getIntent().getLongExtra(EXTRA_RECIPE_ID, -1);
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);

        TextView tvName = findViewById(R.id.tv_detail_name);
        TextView tvIngredients = findViewById(R.id.tv_detail_ingredients);
        TextView tvInstructions = findViewById(R.id.tv_detail_instructions);

        Recipe recipe = dbHelper.getRecipeById(recipeId);
        if (recipe == null) {
            finish();
            return;
        }

        setTitle(recipe.getName());
        tvName.setText(recipe.getName());
        tvInstructions.setText(recipe.getInstructions());

        List<RecipeIngredient> ingredients = dbHelper.getIngredientsForRecipe(recipeId);
        StringBuilder builder = new StringBuilder();
        for (RecipeIngredient ingredient : ingredients) {
            builder.append("\u2022 ")
                    .append(formatQuantity(ingredient.getQuantity()))
                    .append(" ")
                    .append(ingredient.getUnit())
                    .append(" ")
                    .append(ingredient.getIngredientName())
                    .append("\n");
        }
        tvIngredients.setText(builder.toString().trim());
    }

    private String formatQuantity(double quantity) {
        if (quantity == Math.floor(quantity) && !Double.isInfinite(quantity)) {
            return String.valueOf((long) quantity);
        }
        return String.format(Locale.getDefault(), "%.1f", quantity);
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

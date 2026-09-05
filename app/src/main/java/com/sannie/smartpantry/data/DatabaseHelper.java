package com.sannie.smartpantry.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Central persistence layer for the Smart Pantry Manager app.
 * Three tables:
 *  - pantry_items:        ingredients the user currently has at home
 *  - recipes:              the recipe catalogue (seeded on first run)
 *  - recipe_ingredients:   the ingredients required by each recipe
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "smart_pantry.db";
    private static final int DB_VERSION = 1;

    // pantry_items columns
    public static final String TABLE_PANTRY = "pantry_items";
    public static final String COL_PANTRY_ID = "_id";
    public static final String COL_PANTRY_NAME = "name";
    public static final String COL_PANTRY_QUANTITY = "quantity";
    public static final String COL_PANTRY_UNIT = "unit";
    public static final String COL_PANTRY_EXPIRY = "expiry_date";

    // recipes columns
    public static final String TABLE_RECIPES = "recipes";
    public static final String COL_RECIPE_ID = "_id";
    public static final String COL_RECIPE_NAME = "name";
    public static final String COL_RECIPE_INSTRUCTIONS = "instructions";

    // recipe_ingredients columns
    public static final String TABLE_RECIPE_INGREDIENTS = "recipe_ingredients";
    public static final String COL_RI_ID = "_id";
    public static final String COL_RI_RECIPE_ID = "recipe_id";
    public static final String COL_RI_NAME = "ingredient_name";
    public static final String COL_RI_QUANTITY = "quantity";
    public static final String COL_RI_UNIT = "unit";

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_PANTRY + " (" +
                COL_PANTRY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_PANTRY_NAME + " TEXT NOT NULL, " +
                COL_PANTRY_QUANTITY + " REAL NOT NULL, " +
                COL_PANTRY_UNIT + " TEXT NOT NULL, " +
                COL_PANTRY_EXPIRY + " TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_RECIPES + " (" +
                COL_RECIPE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_RECIPE_NAME + " TEXT NOT NULL, " +
                COL_RECIPE_INSTRUCTIONS + " TEXT NOT NULL)");

        db.execSQL("CREATE TABLE " + TABLE_RECIPE_INGREDIENTS + " (" +
                COL_RI_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_RI_RECIPE_ID + " INTEGER NOT NULL, " +
                COL_RI_NAME + " TEXT NOT NULL, " +
                COL_RI_QUANTITY + " REAL NOT NULL, " +
                COL_RI_UNIT + " TEXT NOT NULL, " +
                "FOREIGN KEY(" + COL_RI_RECIPE_ID + ") REFERENCES " + TABLE_RECIPES + "(" + COL_RECIPE_ID + "))");

        seedRecipes(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPE_INGREDIENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PANTRY);
        onCreate(db);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    // ---------------------------------------------------------------
    // Pantry CRUD
    // ---------------------------------------------------------------

    public long addPantryItem(PantryItem item) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = pantryToValues(item);
        return db.insert(TABLE_PANTRY, null, values);
    }

    public int updatePantryItem(PantryItem item) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = pantryToValues(item);
        return db.update(TABLE_PANTRY, values, COL_PANTRY_ID + "=?",
                new String[]{String.valueOf(item.getId())});
    }

    public int deletePantryItem(long id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_PANTRY, COL_PANTRY_ID + "=?", new String[]{String.valueOf(id)});
    }

    public PantryItem getPantryItemById(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_PANTRY, null, COL_PANTRY_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);
        PantryItem item = null;
        if (cursor.moveToFirst()) {
            item = cursorToPantryItem(cursor);
        }
        cursor.close();
        return item;
    }

    public List<PantryItem> getAllPantryItems() {
        List<PantryItem> items = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_PANTRY, null, null, null, null, null,
                COL_PANTRY_NAME + " ASC");
        while (cursor.moveToNext()) {
            items.add(cursorToPantryItem(cursor));
        }
        cursor.close();
        return items;
    }

    private ContentValues pantryToValues(PantryItem item) {
        ContentValues values = new ContentValues();
        values.put(COL_PANTRY_NAME, item.getName());
        values.put(COL_PANTRY_QUANTITY, item.getQuantity());
        values.put(COL_PANTRY_UNIT, item.getUnit());
        values.put(COL_PANTRY_EXPIRY, item.getExpiryDate());
        return values;
    }

    private PantryItem cursorToPantryItem(Cursor cursor) {
        PantryItem item = new PantryItem();
        item.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COL_PANTRY_ID)));
        item.setName(cursor.getString(cursor.getColumnIndexOrThrow(COL_PANTRY_NAME)));
        item.setQuantity(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_PANTRY_QUANTITY)));
        item.setUnit(cursor.getString(cursor.getColumnIndexOrThrow(COL_PANTRY_UNIT)));
        item.setExpiryDate(cursor.getString(cursor.getColumnIndexOrThrow(COL_PANTRY_EXPIRY)));
        return item;
    }

    // ---------------------------------------------------------------
    // Recipe reads (recipes are seeded, not user-editable)
    // ---------------------------------------------------------------

    public List<Recipe> getAllRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_RECIPES, null, null, null, null, null,
                COL_RECIPE_NAME + " ASC");
        while (cursor.moveToNext()) {
            recipes.add(cursorToRecipe(cursor));
        }
        cursor.close();
        return recipes;
    }

    public Recipe getRecipeById(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_RECIPES, null, COL_RECIPE_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);
        Recipe recipe = null;
        if (cursor.moveToFirst()) {
            recipe = cursorToRecipe(cursor);
        }
        cursor.close();
        return recipe;
    }

    public List<RecipeIngredient> getIngredientsForRecipe(long recipeId) {
        List<RecipeIngredient> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_RECIPE_INGREDIENTS, null, COL_RI_RECIPE_ID + "=?",
                new String[]{String.valueOf(recipeId)}, null, null, COL_RI_NAME + " ASC");
        while (cursor.moveToNext()) {
            list.add(cursorToRecipeIngredient(cursor));
        }
        cursor.close();
        return list;
    }

    /**
     * Groups every recipe_ingredients row by recipe id in a single pass,
     * so the matching logic doesn't need to hit the database once per recipe.
     */
    public Map<Long, List<RecipeIngredient>> getAllRecipeIngredientsGrouped() {
        Map<Long, List<RecipeIngredient>> map = new HashMap<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_RECIPE_INGREDIENTS, null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            RecipeIngredient ri = cursorToRecipeIngredient(cursor);
            List<RecipeIngredient> list = map.get(ri.getRecipeId());
            if (list == null) {
                list = new ArrayList<>();
                map.put(ri.getRecipeId(), list);
            }
            list.add(ri);
        }
        cursor.close();
        return map;
    }

    private Recipe cursorToRecipe(Cursor cursor) {
        Recipe recipe = new Recipe();
        recipe.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COL_RECIPE_ID)));
        recipe.setName(cursor.getString(cursor.getColumnIndexOrThrow(COL_RECIPE_NAME)));
        recipe.setInstructions(cursor.getString(cursor.getColumnIndexOrThrow(COL_RECIPE_INSTRUCTIONS)));
        return recipe;
    }

    private RecipeIngredient cursorToRecipeIngredient(Cursor cursor) {
        RecipeIngredient ri = new RecipeIngredient();
        ri.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COL_RI_ID)));
        ri.setRecipeId(cursor.getLong(cursor.getColumnIndexOrThrow(COL_RI_RECIPE_ID)));
        ri.setIngredientName(cursor.getString(cursor.getColumnIndexOrThrow(COL_RI_NAME)));
        ri.setQuantity(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_RI_QUANTITY)));
        ri.setUnit(cursor.getString(cursor.getColumnIndexOrThrow(COL_RI_UNIT)));
        return ri;
    }

    // ---------------------------------------------------------------
    // Seed data - 20 recipes covering common pantry staples
    // ---------------------------------------------------------------

    private void seedRecipes(SQLiteDatabase db) {
        seedRecipe(db, "Scrambled Eggs on Toast",
                "1. Whisk the eggs with a splash of milk.\n" +
                        "2. Melt the butter in a pan over low heat.\n" +
                        "3. Pour in the eggs and stir gently until softly set.\n" +
                        "4. Toast the bread and serve the eggs on top.",
                new Object[][]{
                        {"egg", 2.0, "unit"},
                        {"bread", 2.0, "slice"},
                        {"butter", 10.0, "g"},
                        {"milk", 30.0, "ml"}
                });

        seedRecipe(db, "Tomato Pasta",
                "1. Boil the pasta until al dente.\n" +
                        "2. Fry garlic in olive oil until fragrant.\n" +
                        "3. Add chopped tomato and salt, simmer 10 minutes.\n" +
                        "4. Toss the drained pasta through the sauce.",
                new Object[][]{
                        {"pasta", 200.0, "g"},
                        {"tomato", 3.0, "unit"},
                        {"garlic", 2.0, "clove"},
                        {"olive oil", 15.0, "ml"},
                        {"salt", 2.0, "g"}
                });

        seedRecipe(db, "Vegetable Fried Rice",
                "1. Scramble the egg in a hot pan and set aside.\n" +
                        "2. Stir-fry chopped onion and carrot until soft.\n" +
                        "3. Add the rice and soy sauce, stir-fry 3 minutes.\n" +
                        "4. Fold the egg back in and serve.",
                new Object[][]{
                        {"rice", 300.0, "g"},
                        {"egg", 2.0, "unit"},
                        {"carrot", 1.0, "unit"},
                        {"onion", 1.0, "unit"},
                        {"soy sauce", 15.0, "ml"}
                });

        seedRecipe(db, "Grilled Cheese Sandwich",
                "1. Butter one side of each bread slice.\n" +
                        "2. Place cheese between the unbuttered sides.\n" +
                        "3. Grill in a pan until golden on both sides.",
                new Object[][]{
                        {"bread", 2.0, "slice"},
                        {"cheese", 2.0, "slice"},
                        {"butter", 10.0, "g"}
                });

        seedRecipe(db, "Garlic Butter Rice",
                "1. Cook the rice according to package instructions.\n" +
                        "2. Melt butter and fry chopped garlic until golden.\n" +
                        "3. Stir the garlic butter through the rice with salt.",
                new Object[][]{
                        {"rice", 250.0, "g"},
                        {"garlic", 3.0, "clove"},
                        {"butter", 20.0, "g"},
                        {"salt", 2.0, "g"}
                });

        seedRecipe(db, "Potato and Onion Hash",
                "1. Dice the potato and onion.\n" +
                        "2. Fry in oil over medium heat until golden and tender.\n" +
                        "3. Season with salt and pepper.",
                new Object[][]{
                        {"potato", 3.0, "unit"},
                        {"onion", 1.0, "unit"},
                        {"oil", 15.0, "ml"},
                        {"salt", 2.0, "g"},
                        {"pepper", 1.0, "g"}
                });

        seedRecipe(db, "Simple Bean Soup",
                "1. Fry onion and garlic until soft.\n" +
                        "2. Add chopped tomato and beans with a little water.\n" +
                        "3. Simmer 15 minutes and season with salt.",
                new Object[][]{
                        {"beans", 400.0, "g"},
                        {"onion", 1.0, "unit"},
                        {"garlic", 2.0, "clove"},
                        {"tomato", 2.0, "unit"},
                        {"salt", 2.0, "g"}
                });

        seedRecipe(db, "Cheesy Spinach Omelette",
                "1. Whisk the eggs.\n" +
                        "2. Melt butter in a pan, add spinach until wilted.\n" +
                        "3. Pour in the eggs, sprinkle cheese, fold when set.",
                new Object[][]{
                        {"egg", 3.0, "unit"},
                        {"spinach", 50.0, "g"},
                        {"cheese", 30.0, "g"},
                        {"butter", 10.0, "g"}
                });

        seedRecipe(db, "Lemon Butter Pasta",
                "1. Boil the pasta until al dente.\n" +
                        "2. Melt butter with garlic and a squeeze of lemon juice.\n" +
                        "3. Toss the pasta through the sauce with salt.",
                new Object[][]{
                        {"pasta", 200.0, "g"},
                        {"butter", 20.0, "g"},
                        {"lemon", 1.0, "unit"},
                        {"garlic", 2.0, "clove"},
                        {"salt", 2.0, "g"}
                });

        seedRecipe(db, "Chicken and Rice Bowl",
                "1. Cook the rice.\n" +
                        "2. Fry diced chicken with onion and garlic in oil until cooked through.\n" +
                        "3. Serve the chicken over the rice.",
                new Object[][]{
                        {"chicken", 200.0, "g"},
                        {"rice", 250.0, "g"},
                        {"onion", 1.0, "unit"},
                        {"garlic", 2.0, "clove"},
                        {"oil", 15.0, "ml"}
                });

        seedRecipe(db, "Carrot and Potato Mash",
                "1. Boil the potato and carrot until soft.\n" +
                        "2. Drain and mash with butter and milk.\n" +
                        "3. Season with salt.",
                new Object[][]{
                        {"potato", 3.0, "unit"},
                        {"carrot", 2.0, "unit"},
                        {"butter", 15.0, "g"},
                        {"milk", 50.0, "ml"},
                        {"salt", 2.0, "g"}
                });

        seedRecipe(db, "Tomato and Onion Chutney",
                "1. Fry chopped onion until soft.\n" +
                        "2. Add chopped tomato, sugar, and salt.\n" +
                        "3. Simmer until thickened.",
                new Object[][]{
                        {"tomato", 4.0, "unit"},
                        {"onion", 2.0, "unit"},
                        {"sugar", 10.0, "g"},
                        {"salt", 2.0, "g"}
                });

        seedRecipe(db, "Banana Milk Smoothie",
                "1. Peel and slice the banana.\n" +
                        "2. Blend with milk and sugar until smooth.",
                new Object[][]{
                        {"banana", 2.0, "unit"},
                        {"milk", 200.0, "ml"},
                        {"sugar", 10.0, "g"}
                });

        seedRecipe(db, "French Toast",
                "1. Whisk the egg with milk and sugar.\n" +
                        "2. Dip the bread slices in the mixture.\n" +
                        "3. Fry in butter until golden on both sides.",
                new Object[][]{
                        {"bread", 3.0, "slice"},
                        {"egg", 2.0, "unit"},
                        {"milk", 50.0, "ml"},
                        {"sugar", 10.0, "g"},
                        {"butter", 10.0, "g"}
                });

        seedRecipe(db, "Peanut Butter Toast",
                "1. Toast the bread.\n" +
                        "2. Spread peanut butter evenly on top.",
                new Object[][]{
                        {"bread", 2.0, "slice"},
                        {"peanut butter", 30.0, "g"}
                });

        seedRecipe(db, "Simple Fried Rice with Egg",
                "1. Scramble the egg in a hot, oiled pan.\n" +
                        "2. Add the rice and soy sauce, stir-fry until heated through.",
                new Object[][]{
                        {"rice", 250.0, "g"},
                        {"egg", 2.0, "unit"},
                        {"soy sauce", 15.0, "ml"},
                        {"oil", 10.0, "ml"}
                });

        seedRecipe(db, "Chickpea Salad",
                "1. Drain the chickpeas and chop the tomato and onion.\n" +
                        "2. Toss everything with lemon juice, oil, and salt.",
                new Object[][]{
                        {"chickpeas", 300.0, "g"},
                        {"tomato", 2.0, "unit"},
                        {"onion", 1.0, "unit"},
                        {"lemon", 1.0, "unit"},
                        {"oil", 15.0, "ml"},
                        {"salt", 2.0, "g"}
                });

        seedRecipe(db, "Cheese and Tomato Pasta Bake",
                "1. Boil the pasta until al dente.\n" +
                        "2. Mix with chopped tomato, garlic, and oil.\n" +
                        "3. Top with cheese and bake until melted and golden.",
                new Object[][]{
                        {"pasta", 200.0, "g"},
                        {"cheese", 100.0, "g"},
                        {"tomato", 3.0, "unit"},
                        {"garlic", 2.0, "clove"},
                        {"oil", 10.0, "ml"}
                });

        seedRecipe(db, "Buttered Corn",
                "1. Heat the corn through in a pan.\n" +
                        "2. Stir in butter, salt, and pepper.",
                new Object[][]{
                        {"corn", 200.0, "g"},
                        {"butter", 15.0, "g"},
                        {"salt", 2.0, "g"},
                        {"pepper", 1.0, "g"}
                });

        seedRecipe(db, "Spinach and Garlic Rice",
                "1. Cook the rice.\n" +
                        "2. Fry garlic in oil, add spinach until wilted.\n" +
                        "3. Stir through the rice with salt.",
                new Object[][]{
                        {"rice", 250.0, "g"},
                        {"spinach", 50.0, "g"},
                        {"garlic", 3.0, "clove"},
                        {"oil", 15.0, "ml"},
                        {"salt", 2.0, "g"}
                });

        seedRecipe(db, "Mac and Cheese",
                "1. Boil the pasta until al dente and drain.\n" +
                        "2. Melt the butter in a pan, stir in the milk.\n" +
                        "3. Add the cheese and stir over low heat until melted and smooth.\n" +
                        "4. Fold the pasta through the sauce and season with salt.",
                new Object[][]{
                        {"pasta", 200.0, "g"},
                        {"cheese", 100.0, "g"},
                        {"butter", 20.0, "g"},
                        {"milk", 100.0, "ml"},
                        {"salt", 2.0, "g"}
                });

        seedRecipe(db, "Lasagna",
                "1. Boil the pasta sheets until just soft.\n" +
                        "2. Fry onion and garlic in oil, add chopped tomato, and simmer into a sauce.\n" +
                        "3. Layer pasta sheets, tomato sauce, and cheese in a dish, repeating the layers.\n" +
                        "4. Top with a final layer of cheese and bake until golden and bubbling.",
                new Object[][]{
                        {"pasta", 250.0, "g"},
                        {"tomato", 4.0, "unit"},
                        {"cheese", 150.0, "g"},
                        {"onion", 1.0, "unit"},
                        {"garlic", 2.0, "clove"},
                        {"oil", 15.0, "ml"}
                });
    }

    private void seedRecipe(SQLiteDatabase db, String name, String instructions, Object[][] ingredients) {
        ContentValues recipeValues = new ContentValues();
        recipeValues.put(COL_RECIPE_NAME, name);
        recipeValues.put(COL_RECIPE_INSTRUCTIONS, instructions);
        long recipeId = db.insert(TABLE_RECIPES, null, recipeValues);

        for (Object[] ingredient : ingredients) {
            ContentValues riValues = new ContentValues();
            riValues.put(COL_RI_RECIPE_ID, recipeId);
            riValues.put(COL_RI_NAME, (String) ingredient[0]);
            riValues.put(COL_RI_QUANTITY, (Double) ingredient[1]);
            riValues.put(COL_RI_UNIT, (String) ingredient[2]);
            db.insert(TABLE_RECIPE_INGREDIENTS, null, riValues);
        }
    }
}

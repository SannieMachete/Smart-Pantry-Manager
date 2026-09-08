package com.sannie.smartpantry.logic;

import com.sannie.smartpantry.data.PantryItem;
import com.sannie.smartpantry.data.Recipe;
import com.sannie.smartpantry.data.RecipeIngredient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implements the assignment's core business logic, the
 * strict-matching rule.
 */
public final class RecipeMatcher {

    private RecipeMatcher() {
        // static utility class - no instances
    }

    /**
     * Evaluates every recipe against the current pantry contents and
     * returns one MatchResult per recipe (whether it fully matches,
     * partially matches, or matches nothing at all).
     */
    public static List<MatchResult> evaluateRecipes(List<PantryItem> pantryItems,
                                                      List<Recipe> recipes,
                                                      Map<Long, List<RecipeIngredient>> ingredientsByRecipeId) {
        Map<String, List<PantryItem>> pantryMap = buildPantryMap(pantryItems);
        List<MatchResult> results = new ArrayList<>();

        for (Recipe recipe : recipes) {
            List<RecipeIngredient> required = ingredientsByRecipeId.get(recipe.getId());
            if (required == null) {
                required = new ArrayList<>();
            }

            List<String> missing = new ArrayList<>();
            for (RecipeIngredient requiredIngredient : required) {
                if (!isSatisfied(requiredIngredient, pantryMap)) {
                    missing.add(requiredIngredient.getIngredientName());
                }
            }

            results.add(new MatchResult(recipe, missing));
        }

        return results;
    }

    public static List<Recipe> getStrictSuggestions(List<MatchResult> allResults) {
        List<Recipe> suggestions = new ArrayList<>();
        for (MatchResult result : allResults) {
            if (result.isFullMatch()) {
                suggestions.add(result.getRecipe());
            }
        }
        return suggestions;
    }

    /**
     * Recipes missing exactly one ingredient,
     * kept clearly separate from the strict suggestions list.
     */
    public static List<MatchResult> getAlmostThere(List<MatchResult> allResults) {
        List<MatchResult> almostThere = new ArrayList<>();
        for (MatchResult result : allResults) {
            if (result.isAlmostThere()) {
                almostThere.add(result);
            }
        }
        return almostThere;
    }

    /**
     * Groups pantry items by normalized ingredient name so a recipe
     * ingredient can be looked up in O(1) instead of scanning the whole
     * pantry for every required ingredient of every recipe.
     */
    private static Map<String, List<PantryItem>> buildPantryMap(List<PantryItem> pantryItems) {
        Map<String, List<PantryItem>> map = new HashMap<>();
        for (PantryItem item : pantryItems) {
            String key = IngredientNormalizer.normalize(item.getName());
            List<PantryItem> bucket = map.get(key);
            if (bucket == null) {
                bucket = new ArrayList<>();
                map.put(key, bucket);
            }
            bucket.add(item);
        }
        return map;
    }

    /**
     * Checks whether a single required ingredient is satisfied by the
     * pantry: present at all, and present in at least the required
     * quantity when units line up.
     */
    private static boolean isSatisfied(RecipeIngredient required, Map<String, List<PantryItem>> pantryMap) {
        String key = IngredientNormalizer.normalize(required.getIngredientName());
        List<PantryItem> matches = pantryMap.get(key);

        if (matches == null || matches.isEmpty()) {
            // Ingredient not in the pantry at all under this normalized name.
            return false;
        }

        double sameUnitTotal = 0;
        boolean hasSameUnitEntry = false;

        for (PantryItem item : matches) {
            if (item.getUnit() != null && required.getUnit() != null &&
                    item.getUnit().trim().equalsIgnoreCase(required.getUnit().trim())) {
                hasSameUnitEntry = true;
                sameUnitTotal += item.getQuantity();
            }
        }

        if (hasSameUnitEntry) {
            return sameUnitTotal >= required.getQuantity();
        }
        return true;
    }
}

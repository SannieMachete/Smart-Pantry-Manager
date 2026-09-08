package com.sannie.smartpantry.logic;

import com.sannie.smartpantry.data.Recipe;

import java.util.ArrayList;
import java.util.List;

/**
 * The outcome of checking one recipe against the current pantry:
 * the recipe itself, plus the list of ingredient names (if any) that
 * are still missing or under-quantity.
 */
public class MatchResult {

    private final Recipe recipe;
    private final List<String> missingIngredients;

    public MatchResult(Recipe recipe, List<String> missingIngredients) {
        this.recipe = recipe;
        this.missingIngredients = missingIngredients != null ? missingIngredients : new ArrayList<String>();
    }

    public Recipe getRecipe() {
        return recipe;
    }

    public List<String> getMissingIngredients() {
        return missingIngredients;
    }

    public boolean isFullMatch() {
        return missingIngredients.isEmpty();
    }

    public boolean isAlmostThere() {
        return missingIngredients.size() == 1;
    }

    public int getMissingCount() {
        return missingIngredients.size();
    }
}

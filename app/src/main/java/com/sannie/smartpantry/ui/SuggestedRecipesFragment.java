package com.sannie.smartpantry.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sannie.smartpantry.R;
import com.sannie.smartpantry.adapters.RecipeAdapter;
import com.sannie.smartpantry.data.DatabaseHelper;
import com.sannie.smartpantry.data.PantryItem;
import com.sannie.smartpantry.data.Recipe;
import com.sannie.smartpantry.data.RecipeIngredient;
import com.sannie.smartpantry.logic.MatchResult;
import com.sannie.smartpantry.logic.RecipeMatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Suggested Recipes screen: runs the strict-matching logic
 * against the current pantry and shows only recipes the
 * user can make right now, plus an optional "Almost There" bonus list
 * for recipes missing exactly one ingredient.
 */
public class SuggestedRecipesFragment extends Fragment implements RecipeAdapter.OnRecipeClickListener {

    private DatabaseHelper dbHelper;
    private RecipeAdapter suggestedAdapter;
    private RecipeAdapter almostThereAdapter;

    private RecyclerView rvSuggested;
    private RecyclerView rvAlmostThere;
    private View tvEmptySuggestions;
    private View tvEmptyAlmostThere;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_suggested_recipes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbHelper = DatabaseHelper.getInstance(requireContext());

        rvSuggested = view.findViewById(R.id.rv_suggested_recipes);
        rvAlmostThere = view.findViewById(R.id.rv_almost_there);
        tvEmptySuggestions = view.findViewById(R.id.layout_empty_suggestions);
        tvEmptyAlmostThere = view.findViewById(R.id.layout_empty_almost_there);

        suggestedAdapter = new RecipeAdapter(this, false);
        almostThereAdapter = new RecipeAdapter(this, true);

        rvSuggested.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvSuggested.setAdapter(suggestedAdapter);

        rvAlmostThere.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvAlmostThere.setAdapter(almostThereAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Re-run the matching logic every time this screen is shown, since
        // the pantry may have changed since the last visit.
        runMatching();
    }

    private void runMatching() {
        List<PantryItem> pantryItems = dbHelper.getAllPantryItems();
        List<Recipe> recipes = dbHelper.getAllRecipes();
        Map<Long, List<RecipeIngredient>> ingredientsByRecipe = dbHelper.getAllRecipeIngredientsGrouped();

        List<MatchResult> allResults = RecipeMatcher.evaluateRecipes(pantryItems, recipes, ingredientsByRecipe);

        List<Recipe> strictSuggestions = RecipeMatcher.getStrictSuggestions(allResults);
        List<MatchResult> suggestionResults = new ArrayList<>();
        for (MatchResult result : allResults) {
            if (result.isFullMatch()) {
                suggestionResults.add(result);
            }
        }
        List<MatchResult> almostThereResults = RecipeMatcher.getAlmostThere(allResults);

        suggestedAdapter.setResults(suggestionResults);
        almostThereAdapter.setResults(almostThereResults);

        tvEmptySuggestions.setVisibility(strictSuggestions.isEmpty() ? View.VISIBLE : View.GONE);
        rvSuggested.setVisibility(strictSuggestions.isEmpty() ? View.GONE : View.VISIBLE);

        tvEmptyAlmostThere.setVisibility(almostThereResults.isEmpty() ? View.VISIBLE : View.GONE);
        rvAlmostThere.setVisibility(almostThereResults.isEmpty() ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onRecipeClick(Recipe recipe) {
        Intent intent = new Intent(requireContext(), RecipeDetailActivity.class);
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.getId());
        startActivity(intent);
    }
}

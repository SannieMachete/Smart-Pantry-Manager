package com.sannie.smartpantry.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.sannie.smartpantry.R;

/**
 * Single host Activity for the app's three main tabs (Pantry, Suggested
 * Recipes, Settings), switched via the bottom navigation bar.
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG_PANTRY = "tag_pantry";
    private static final String TAG_RECIPES = "tag_recipes";
    private static final String TAG_SETTINGS = "tag_settings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        if (savedInstanceState == null) {
            showFragment(new PantryListFragment(), TAG_PANTRY);
        }

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_pantry) {
                showFragment(new PantryListFragment(), TAG_PANTRY);
                return true;
            } else if (id == R.id.nav_recipes) {
                showFragment(new SuggestedRecipesFragment(), TAG_RECIPES);
                return true;
            } else if (id == R.id.nav_settings) {
                showFragment(new SettingsFragment(), TAG_SETTINGS);
                return true;
            }
            return false;
        });
    }

    private void showFragment(Fragment fragment, String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment, tag)
                .commit();
    }
}

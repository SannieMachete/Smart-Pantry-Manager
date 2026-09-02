# Smart Pantry Manager

An Android app (Java, Android Studio) that helps a user reduce food waste
by tracking the ingredients they have at home and suggesting only the
recipes they can make **right now** with strictly what's already in their
pantry — no shopping trip required.

Built for the Mobile App Development 700 practical assignment.

## Core feature: strict recipe matching

A recipe is only shown under **Suggested Recipes** if every single
ingredient it needs is present in the pantry, in at least the required
quantity. Recipes missing even one ingredient are excluded from that list.
Recipes missing exactly one ingredient appear separately under an
**Almost There** section, so the two lists are never mixed.

Matching is normalized (lowercase, trimmed, naive singular/plural
handling — e.g. "Tomatoes" matches "tomato") so it isn't broken by trivial
real-world differences. See `logic/IngredientNormalizer.java` and
`logic/RecipeMatcher.java` for the implementation.

## Screens

1. **Pantry List** — view, add, edit, and delete pantry ingredients (RecyclerView + CRUD).
2. **Add/Edit Ingredient** — form with input validation, reached via Intent from the Pantry List.
3. **Suggested Recipes** — runs the strict-matching logic against the current pantry; includes the "Almost There" bonus list.
4. **Recipe Detail** — full ingredient list and method for a selected recipe, reached via Intent with the recipe id.
5. **Settings** — toggle expiring-soon alerts and a preferred unit system, saved to SharedPreferences.

Navigation is via a bottom navigation bar (Pantry / Recipes / Settings)
hosted in a single `MainActivity`, with `AddEditIngredientActivity` and
`RecipeDetailActivity` launched as separate Activities via explicit
Intents that pass data (ingredient id / recipe id).

## Database

**SQLite**, via `SQLiteOpenHelper` (`data/DatabaseHelper.java`) — chosen
because it matches the module's persistent-data chapter directly, needs
no network dependency for the demo, and keeps the CRUD SQL fully explicit
and easy to walk through in the report and video. It supports full CRUD
on pantry items, and data persists after the app is closed and reopened.

Schema:

- `pantry_items` — the user's own ingredients (id, name, quantity, unit, expiry_date)
- `recipes` — the recipe catalogue, seeded on first run with 20 recipes (id, name, instructions)
- `recipe_ingredients` — the ingredients each recipe requires (id, recipe_id, ingredient_name, quantity, unit)

## Out of scope

Per the assignment brief, this app does **not** use Google Maps, any
mapping SDK, or device location/GPS services.

## Setup / run instructions

1. Open the `SmartPantryManager/` folder in Android Studio (Giraffe or later recommended).
2. Let Gradle sync (requires the Google and Maven Central repositories, configured in `settings.gradle`).
3. Run on an emulator or physical device with **minSdk 24** (Android 7.0) or higher.
4. On first launch, the app seeds its own recipe database automatically — no setup steps needed beyond running it.

## Tech

- Java, Android Studio
- SQLiteOpenHelper (raw SQL, no Room)
- RecyclerView + custom Adapters
- Material Components (BottomNavigationView, TextInputLayout, SwitchMaterial)
- SharedPreferences for settings

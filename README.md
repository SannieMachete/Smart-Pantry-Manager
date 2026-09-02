# Smart Pantry Manager

An Android app (Java) that helps you reduce food waste by tracking what's
in your pantry and suggesting only the recipes you can make **right now**
with what you already have — no shopping trip required.

Built for the Mobile App Development 700 practical assignment.

## How it works

A recipe only shows up under **Suggested Recipes** if you have every
ingredient it needs, in the required quantity. Recipes missing exactly
one ingredient show up separately under **Almost There**.

## Screens

1. **Pantry List** — add, edit, delete ingredients
2. **Add/Edit Ingredient** — form with validation
3. **Suggested Recipes** — strict matches + "Almost There"
4. **Recipe Detail** — full ingredients and method
5. **Settings** — expiry alerts, unit preference

## Database

**SQLite** (`SQLiteOpenHelper`) — matches the module's persistent-data
chapter, needs no network dependency, and keeps the CRUD SQL explicit for
the report/video walkthrough. Full CRUD on pantry items; data persists
across app restarts.

Three tables: `pantry_items`, `recipes`, `recipe_ingredients`.

## Out of scope

No Google Maps, mapping SDK, or GPS/location services, per the brief.

## Setup

1. Open the `SmartPantryManager/` folder in Android Studio
2. Let Gradle sync
3. Run on a device or emulator with minSdk 24 (Android 7.0) or higher
4. Recipes seed automatically on first launch

## Tech

Java · SQLiteOpenHelper · RecyclerView · Material Components · SharedPreferences

package com.sannie.smartpantry.logic;

import java.util.Locale;

/**
 * Normalizes ingredient names so matching survives simple real-world
 * messiness (case, whitespace, singular vs plural) without needing a
 * full NLP solution, per the assignment brief (Section 2.3).
 *
 * Examples:
 *   "Tomatoes"  -> "tomato"
 *   " Onion "   -> "onion"
 *   "Berries"   -> "berry"
 *   "Eggs"      -> "egg"
 */
public final class IngredientNormalizer {

    private IngredientNormalizer() {
        // static utility class - no instances
    }

    public static String normalize(String rawName) {
        if (rawName == null) {
            return "";
        }

        String name = rawName.trim().toLowerCase(Locale.ROOT);
        name = name.replaceAll("\\s+", " ");

        if (name.length() > 3 && name.endsWith("ies")) {
            // "berries" -> "berry"
            name = name.substring(0, name.length() - 3) + "y";
        } else if (name.length() > 2 &&
                (name.endsWith("shes") || name.endsWith("ches") || name.endsWith("xes") || name.endsWith("oes"))) {
            // "tomatoes" -> "tomato", "dishes" -> "dish"
            name = name.substring(0, name.length() - 2);
        } else if (name.length() > 1 && name.endsWith("s") && !name.endsWith("ss")) {
            // "eggs" -> "egg" (but "grass" stays "grass")
            name = name.substring(0, name.length() - 1);
        }

        return name;
    }
}

package com.sannie.smartpantry.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sannie.smartpantry.R;
import com.sannie.smartpantry.data.Recipe;
import com.sannie.smartpantry.logic.MatchResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays a list of recipes, each as a card showing the recipe name and
 * a subtitle. Used for both the strict "Suggested Recipes" list (subtitle
 * confirms a full match) and the optional "Almost There" list (subtitle
 * names the single missing ingredient).
 */
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);
    }

    private List<MatchResult> results = new ArrayList<>();
    private final OnRecipeClickListener listener;
    private final boolean showMissingSubtitle;

    
    @param showMissingSubtitle 
    
    public RecipeAdapter(OnRecipeClickListener listener, boolean showMissingSubtitle) {
        this.listener = listener;
        this.showMissingSubtitle = showMissingSubtitle;
    }

    public void setResults(List<MatchResult> results) {
        this.results = results != null ? results : new ArrayList<MatchResult>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        MatchResult result = results.get(position);
        Recipe recipe = result.getRecipe();

        holder.tvName.setText(recipe.getName());

        if (showMissingSubtitle && !result.getMissingIngredients().isEmpty()) {
            String missingName = result.getMissingIngredients().get(0);
            holder.tvSubtitle.setText(holder.itemView.getContext()
                    .getString(R.string.missing_prefix, missingName));
            holder.tvSubtitle.setTextColor(holder.itemView.getContext()
                    .getResources().getColor(R.color.rust));
            holder.statusStripe.setBackgroundColor(holder.itemView.getContext()
                    .getResources().getColor(R.color.gold));
        } else {
            holder.tvSubtitle.setText(R.string.you_have_everything);
            holder.tvSubtitle.setTextColor(holder.itemView.getContext()
                    .getResources().getColor(R.color.herb));
            holder.statusStripe.setBackgroundColor(holder.itemView.getContext()
                    .getResources().getColor(R.color.herb));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRecipeClick(recipe);
            }
        });
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    static class RecipeViewHolder extends RecyclerView.ViewHolder {
        final TextView tvName;
        final TextView tvSubtitle;
        final View statusStripe;

        RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_recipe_name);
            tvSubtitle = itemView.findViewById(R.id.tv_recipe_subtitle);
            statusStripe = itemView.findViewById(R.id.view_status_stripe);
        }
    }
}

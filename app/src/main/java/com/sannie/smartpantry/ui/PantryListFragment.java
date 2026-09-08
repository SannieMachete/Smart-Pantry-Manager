package com.sannie.smartpantry.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sannie.smartpantry.R;
import com.sannie.smartpantry.adapters.PantryAdapter;
import com.sannie.smartpantry.data.DatabaseHelper;
import com.sannie.smartpantry.data.PantryItem;

import java.util.List;

/**
 * Pantry List screen: shows every ingredient currently in
 * the pantry, with add/edit/delete access.
 */
public class PantryListFragment extends Fragment implements PantryAdapter.OnPantryItemActionListener {

    private DatabaseHelper dbHelper;
    private PantryAdapter adapter;
    private RecyclerView recyclerView;
    private View emptyStateView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pantry_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbHelper = DatabaseHelper.getInstance(requireContext());

        recyclerView = view.findViewById(R.id.rv_pantry);
        emptyStateView = view.findViewById(R.id.layout_empty_pantry);
        FloatingActionButton fab = view.findViewById(R.id.fab_add_ingredient);

        adapter = new PantryAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        fab.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AddEditIngredientActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh every time this screen becomes visible, since the user
        // may be returning from Add/Edit with new or changed data.
        refreshList();
    }

    private void refreshList() {
        List<PantryItem> items = dbHelper.getAllPantryItems();
        adapter.setItems(items);

        boolean isEmpty = items.isEmpty();
        emptyStateView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onEdit(PantryItem item) {
        Intent intent = new Intent(requireContext(), AddEditIngredientActivity.class);
        intent.putExtra(AddEditIngredientActivity.EXTRA_ITEM_ID, item.getId());
        startActivity(intent);
    }

    @Override
    public void onDelete(PantryItem item) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.dialog_delete_title)
                .setMessage(R.string.dialog_delete_message)
                .setNegativeButton(R.string.dialog_cancel, null)
                .setPositiveButton(R.string.dialog_delete_confirm, (dialog, which) -> {
                    dbHelper.deletePantryItem(item.getId());
                    refreshList();
                })
                .show();
    }
}

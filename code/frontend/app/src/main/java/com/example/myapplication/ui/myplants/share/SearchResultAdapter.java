package com.example.myapplication.ui.myplants.share;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * SearchResultAdapter - RecyclerView adapter for plant name search results.
 * 
 * Purpose:
 * - Display searchable list of plant names in AddPlantFragment
 * - Handle user selection of plant names
 * - Support real-time filtering of plant names
 * 
 * Usage:
 * - Used in AddPlantFragment for plant name selection
 * - Displays filtered results from Plant Wiki database
 * - Updates dynamically as user types
 * 
 * Layout:
 * - Uses Android's built-in simple_list_item_1 layout
 * - Single TextView per item (plant name)
 * - Simple, clean list appearance
 * 
 * Data Flow:
 * 1. AddPlantFragment fetches all plant names from API
 * 2. User types in search field
 * 3. AddPlantFragment filters names and calls updateData()
 * 4. Adapter displays filtered results
 * 5. User clicks item, callback fires
 * 6. AddPlantFragment fills search field with selected name
 */
public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ViewHolder> {

    /** List of plant names to display (filtered by AddPlantFragment) */
    private final List<String> searchResults = new ArrayList<>();
    
    /** Click listener for item selection */
    private final OnItemClickListener listener;

    /**
     * Callback interface for handling plant name selection.
     * Implemented by AddPlantFragment to handle user clicks.
     */
    public interface OnItemClickListener {
        void onItemClick(String scientificName);
    }

    /**
     * Constructor with click listener.
     * @param listener Callback for item clicks
     */
    public SearchResultAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    /**
     * Creates ViewHolder for each list item.
     * Uses Android's built-in simple_list_item_1 layout for simplicity.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Binds plant name to ViewHolder and sets up click listener.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String name = searchResults.get(position);
        holder.bind(name, listener);
    }

    /**
     * Returns number of items in filtered results.
     */
    @Override
    public int getItemCount() {
        return searchResults.size();
    }

    /**
     * Updates displayed plant names with new filtered results.
     * Called by AddPlantFragment when search query changes.
     * 
     * @param newResults Filtered list of plant names to display
     */
    public void updateData(List<String> newResults) {
        this.searchResults.clear();
        this.searchResults.addAll(newResults);
        notifyDataSetChanged(); // Refresh RecyclerView
    }

    /**
     * ViewHolder for search result items.
     * Holds reference to TextView and handles click events.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;

        /**
         * Constructor - finds TextView from simple_list_item_1 layout.
         */
        ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
        }

        /**
         * Binds plant name to TextView and sets click listener.
         * 
         * @param scientificName Plant name to display
         * @param listener Click callback
         */
        void bind(final String scientificName, final OnItemClickListener listener) {
            textView.setText(scientificName);
            itemView.setOnClickListener(v -> listener.onItemClick(scientificName));
        }
    }
}

package com.example.myapplication.ui.myplants;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for the search results RecyclerView. It displays a list of plant names
 * and handles click events on each item.
 */
public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ViewHolder> {

    private final List<String> searchResults = new ArrayList<>();
    private final OnItemClickListener listener;

    // Interface to handle clicks on search result items
    public interface OnItemClickListener {
        void onItemClick(String scientificName);
    }

    public SearchResultAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Create a new view for each item, using a simple built-in layout
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String name = searchResults.get(position);
        holder.bind(name, listener);
    }

    @Override
    public int getItemCount() {
        return searchResults.size();
    }

    /**
     * Updates the data in the adapter and refreshes the RecyclerView.
     * @param newResults The new list of search results to display.
     */
    public void updateData(List<String> newResults) {
        this.searchResults.clear();
        this.searchResults.addAll(newResults);
        notifyDataSetChanged();
    }

    /**
     * ViewHolder for each search result item.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;

        ViewHolder(View itemView) {
            super(itemView);
            // Use the default TextView ID from android.R.layout.simple_list_item_1
            textView = itemView.findViewById(android.R.id.text1);
        }

        void bind(final String scientificName, final OnItemClickListener listener) {
            textView.setText(scientificName);
            // Set a click listener on the item view
            itemView.setOnClickListener(v -> listener.onItemClick(scientificName));
        }
    }
}

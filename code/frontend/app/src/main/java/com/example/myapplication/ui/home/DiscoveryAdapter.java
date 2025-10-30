package com.example.myapplication.ui.home; // Or your package

import android.content.Context;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.ui.home.DiscoveryItem;
import java.util.List;

public class DiscoveryAdapter extends RecyclerView.Adapter<DiscoveryAdapter.ViewHolder> {

    private static final String TAG = "DiscoveryAdapter";
    private Context context;
    private List<DiscoveryItem> discoveryList;

    public DiscoveryAdapter(Context context, List<DiscoveryItem> discoveryList) {
        this.context = context;
        this.discoveryList = discoveryList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Ensure R.layout.item_discovery_card exists
        View view = LayoutInflater.from(context).inflate(R.layout.item_discovery_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DiscoveryItem item = discoveryList.get(position);

        holder.nameTextView.setText(item.getName());
        holder.distanceTextView.setText(item.getDistance());

        // Load image from Base64 if available, otherwise use resource ID
        if (item.hasBase64Image()) {
            try {
                byte[] imageBytes = Base64.decode(item.getBase64Image(), Base64.DEFAULT);
                Glide.with(context)
                    .load(imageBytes)
                    .placeholder(R.drawable.plantbulb_foreground)
                    .error(R.drawable.plantbulb_foreground)
                    .centerCrop()
                    .into(holder.photoImageView);
                Log.d(TAG, "Loaded Base64 image for: " + item.getName());
            } catch (Exception e) {
                Log.e(TAG, "Failed to decode Base64 image for: " + item.getName(), e);
                holder.photoImageView.setImageResource(R.drawable.plantbulb_foreground);
            }
        } else if (item.getImageResId() != 0) {
            holder.photoImageView.setImageResource(item.getImageResId());
        } else {
            holder.photoImageView.setImageResource(R.drawable.plantbulb_foreground); // Fallback
        }

        holder.itemView.setOnClickListener(v -> {
            Toast.makeText(context, "Clicked: " + item.getName(), Toast.LENGTH_SHORT).show();
            // TODO: Implement detail view navigation or other action for discovery items
        });
    }

    @Override
    public int getItemCount() {
        return discoveryList != null ? discoveryList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView photoImageView;
        TextView nameTextView;
        TextView distanceTextView;
        // Add other views from item_discovery_card.xml if needed

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ensure these IDs match your item_discovery_card.xml
            photoImageView = itemView.findViewById(R.id.image_discovery_photo);
            nameTextView = itemView.findViewById(R.id.text_discovery_name);
            distanceTextView = itemView.findViewById(R.id.text_discovery_distance);
        }
    }

    // Method to update data
    public void updateData(List<DiscoveryItem> newDiscoveryList) {
        this.discoveryList.clear();
        if (newDiscoveryList != null) {
            this.discoveryList.addAll(newDiscoveryList);
        }
        notifyDataSetChanged();
    }
}
    
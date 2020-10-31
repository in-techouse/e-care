package kc.fyp.ecare.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import kc.fyp.ecare.R;
import kc.fyp.ecare.activities.AnnouncementDetail;
import kc.fyp.ecare.models.Announcement;

public class AnnouncementAdapter extends RecyclerView.Adapter<AnnouncementAdapter.AnnouncementHolder> {
    public static final String TAG = "AnnouncementAdapter";
    private List<Announcement> data;
    private final Context context;

    public AnnouncementAdapter(Context context) {
        data = new ArrayList<>();
        this.context = context;
    }

    public void setData(List<Announcement> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AnnouncementHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_announcement, parent, false);
        return new AnnouncementHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AnnouncementHolder holder, int position) {
        final Announcement announcement = data.get(position);
        if (announcement.getImages() != null && announcement.getImages().size() > 0) {
            Log.e(TAG, "Announcement Image is loaded");
            Glide.with(context).load(announcement.getImages().get(0)).into(holder.image);
        } else {
            holder.image.setVisibility(View.GONE);
        }
        holder.name.setText(announcement.getName());
        holder.category.setText(announcement.getCategory());
        holder.address.setText(announcement.getAddress());
        holder.mainCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent it = new Intent(context, AnnouncementDetail.class);
                it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Bundle bundle = new Bundle();
                bundle.putSerializable("announcement", announcement);
                it.putExtras(bundle);
                context.startActivity(it);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }


    public static class AnnouncementHolder extends RecyclerView.ViewHolder {
        CardView mainCard;
        ImageView image;
        TextView name, category, address;

        public AnnouncementHolder(@NonNull View itemView) {
            super(itemView);
            mainCard = itemView.findViewById(R.id.mainCard);
            image = itemView.findViewById(R.id.image);
            name = itemView.findViewById(R.id.name);
            category = itemView.findViewById(R.id.category);
            address = itemView.findViewById(R.id.address);
        }
    }
}

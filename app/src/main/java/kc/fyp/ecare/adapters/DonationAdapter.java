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
import kc.fyp.ecare.activities.DonationDetail;
import kc.fyp.ecare.models.Donation;

public class DonationAdapter extends RecyclerView.Adapter<DonationAdapter.DonationHolder> {
    public static final String TAG = "DonationAdapter";
    private List<Donation> data;
    private Context context;

    public DonationAdapter(Context context) {
        data = new ArrayList<>();
        this.context = context;
    }

    public void setData(List<Donation> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DonationHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_donation, parent, false);
        return new DonationHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull DonationHolder holder, int position) {
        final Donation donation = data.get(position);
        if (donation.getImages() != null && donation.getImages().size() > 0) {
            Log.e(TAG, "Donation Image is loaded");
            Glide.with(context).load(donation.getImages().get(0)).into(holder.image);
        }
        holder.name.setText(donation.getName());
        holder.category.setText(donation.getCategory());
        holder.address.setText(donation.getAddress());
        holder.mainCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent it = new Intent(context, DonationDetail.class);
                it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Bundle bundle = new Bundle();
                bundle.putSerializable("donation", donation);
                it.putExtras(bundle);
                context.startActivity(it);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class DonationHolder extends RecyclerView.ViewHolder {
        CardView mainCard;
        ImageView image;
        TextView name, category, address;

        public DonationHolder(@NonNull View itemView) {
            super(itemView);
            mainCard = itemView.findViewById(R.id.mainCard);
            image = itemView.findViewById(R.id.image);
            name = itemView.findViewById(R.id.name);
            category = itemView.findViewById(R.id.category);
            address = itemView.findViewById(R.id.address);
        }
    }
}
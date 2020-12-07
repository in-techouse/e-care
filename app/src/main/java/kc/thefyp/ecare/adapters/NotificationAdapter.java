package kc.thefyp.ecare.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import kc.thefyp.ecare.R;
import kc.thefyp.ecare.activities.RequestDetail;
import kc.thefyp.ecare.models.Notification;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationHolder> {
    public static final String TAG = "NotificationAdapter";
    private List<Notification> data;
    private final Context context;

    public NotificationAdapter(Context context) {
        data = new ArrayList<>();
        this.context = context;
    }

    public void setData(List<Notification> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NotificationHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new NotificationHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationHolder holder, int position) {
        final Notification notification = data.get(position);
        holder.message.setText(notification.getMessage());
        holder.mainCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (notification.getType().equals("ViewRequest")) {
                    Intent it = new Intent(context, RequestDetail.class);
                    it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("notification", notification);
                    it.putExtras(bundle);
                    context.startActivity(it);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class NotificationHolder extends RecyclerView.ViewHolder {
        CardView mainCard;
        TextView message;

        public NotificationHolder(@NonNull View itemView) {
            super(itemView);
            mainCard = itemView.findViewById(R.id.mainCard);
            message = itemView.findViewById(R.id.message);
        }
    }
}

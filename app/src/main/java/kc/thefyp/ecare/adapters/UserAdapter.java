package kc.thefyp.ecare.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import kc.thefyp.ecare.R;
import kc.thefyp.ecare.models.User;
import me.zhanghai.android.materialratingbar.MaterialRatingBar;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserHolder> {
    public static final String TAG = "UserAdapter";
    private List<User> data;
    private final Context context;

    public UserAdapter(Context context) {
        data = new ArrayList<>();
        this.context = context;
    }

    public void setData(List<User> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    public void clear() {
        this.data.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull UserHolder holder, int position) {
        final User user = data.get(position);
        if (user.getImage() != null && user.getImage().length() > 0) {
            Glide.with(context).load(user.getImage()).into(holder.userImage);
        }
        holder.userName.setText(user.getName());
        holder.ratingBar.setRating(Float.parseFloat("" + user.getRating()));
        holder.mainCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class UserHolder extends RecyclerView.ViewHolder {
        CardView mainCard;
        CircleImageView userImage;
        TextView userName;
        MaterialRatingBar ratingBar;

        public UserHolder(@NonNull View itemView) {
            super(itemView);
            mainCard = itemView.findViewById(R.id.mainCard);
            userImage = itemView.findViewById(R.id.userImage);
            userName = itemView.findViewById(R.id.userName);
            ratingBar = itemView.findViewById(R.id.ratingBar);
        }
    }
}

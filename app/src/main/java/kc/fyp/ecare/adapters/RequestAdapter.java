package kc.fyp.ecare.adapters;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import kc.fyp.ecare.R;
import kc.fyp.ecare.models.Request;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestHolder> {
    public static final String TAG = "RequestAdapter";
    private List<Request> data;
    private final Context context;

    public RequestAdapter(Context context) {
        data = new ArrayList<>();
        this.context = context;
    }

    public void setData(List<Request> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RequestHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_request, parent, false);
        return new RequestHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestHolder holder, int position) {
        final Request request = data.get(position);
        holder.userName.setText(request.getUserName());
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(request.getTimestamps());
        String date = DateFormat.format("EEEE, dd, MMM yyyy hh:mm:ss", cal).toString();
        holder.requestTime.setText(date);
        holder.message.setText(request.getDescription());
        holder.mainCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class RequestHolder extends RecyclerView.ViewHolder {
        CardView mainCard;
        TextView userName, requestTime, message;

        public RequestHolder(@NonNull View itemView) {
            super(itemView);
            mainCard = itemView.findViewById(R.id.mainCard);
            userName = itemView.findViewById(R.id.userName);
            requestTime = itemView.findViewById(R.id.requestTime);
            message = itemView.findViewById(R.id.message);
        }
    }
}

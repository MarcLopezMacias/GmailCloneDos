package cat.itb.gmailclone2.Fragments.RecyclerView;

import android.graphics.Color;
import android.icu.util.Calendar;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import Model.Email;
import Resources.CircleTransformation;
import cat.itb.gmailclone2.R;

import static cat.itb.gmailclone2.MainActivity.getContextOfApplication;


public class EmailAdapter extends FirebaseRecyclerAdapter<Email, EmailAdapter.EmailViewHolder> implements View.OnClickListener {

    private View.OnClickListener listener;


    public EmailAdapter(FirebaseRecyclerOptions<Email> emails) {
        super(emails);
    }

    ;

    @NonNull
    @Override
    public EmailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_view, parent, false);

        v.setOnClickListener(this);
        return new EmailViewHolder(v);
    }


    @Override
    protected void onBindViewHolder(@NonNull EmailViewHolder holder, int position, @NonNull Email model) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {


            holder.bindData(model);


        }
    }


    public void setOnClickListener(View.OnClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onClick(View v) {
        if (listener != null) {
            listener.onClick(v);
        }
    }

    public void notifyData() {
        notifyDataSetChanged();
    }

    class EmailViewHolder extends RecyclerView.ViewHolder {
        ImageButton imageItem;
        TextView originItem;
        TextView titleItem;
        TextView descriptionItem;
        TextView dateItem;
        CheckBox starItem;


        public EmailViewHolder(@NonNull View itemView) {
            super(itemView);
            imageItem = itemView.findViewById(R.id.imageButton);
            originItem = itemView.findViewById(R.id.originItem);
            titleItem = itemView.findViewById(R.id.titleItem);
            descriptionItem = itemView.findViewById(R.id.descriptionItem);
            dateItem = itemView.findViewById(R.id.dateItem);
            starItem = itemView.findViewById(R.id.starItem);

        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        public void bindData(final Email email) {

            Picasso.with(getContextOfApplication())
                    .load(email.getPhotoUrl())
                    .resize(130, 130)
                    .centerCrop().transform(new CircleTransformation())
                    .into(imageItem);

            originItem.setText(email.getOrigin());
            titleItem.setText(email.getTitle());
            descriptionItem.setText(email.getBody());

            Date mailDate = email.getDate();
            String date;
            SimpleDateFormat today = new SimpleDateFormat("HH:mm", Locale.getDefault());
            SimpleDateFormat notToday = new SimpleDateFormat("dd MMM", Locale.getDefault());
            SimpleDateFormat compareDay = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
            if (compareDay.format(mailDate).equals(compareDay.format(Calendar.getInstance().getTime()))) {
                date = today.format(mailDate);

            } else {
                date = notToday.format(mailDate);
            }
            dateItem.setText(date);

            starItem.setEnabled(true);
            starItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    email.setFavorite(starItem.isChecked());
                    notifyData();
                }
            });

            if (email.isRead()) {
                originItem.setTextColor(Color.GRAY);
                titleItem.setTextColor(Color.GRAY);
                dateItem.setTextColor(Color.GRAY);
            } else {
                originItem.setTextColor(Color.WHITE);
                titleItem.setTextColor(Color.WHITE);
                dateItem.setTextColor(Color.WHITE);
            }
            descriptionItem.setTextColor(Color.GRAY);

        }

    }

}

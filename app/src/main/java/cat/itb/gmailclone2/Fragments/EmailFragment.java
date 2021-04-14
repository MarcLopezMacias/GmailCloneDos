package cat.itb.gmailclone2.Fragments;

import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.view.menu.ActionMenuItemView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.navigation.Navigation;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import Model.Email;

import cat.itb.gmailclone2.R;

public class EmailFragment extends Fragment {

    MaterialToolbar toolbar;

    TextView subjectTextView;
    MaterialButton inboxLabel;
    MaterialCheckBox favCheckBox;

    ImageView originProfilePicture;
    TextView originTextView;
    TextView dateTextView;
    MaterialButton originReplyButton;
    Button optionsButton;

    TextView bodyTextView;

    View.OnClickListener replyButtonListener;

    MaterialButton replyButton;
    MaterialButton replyAllButton;
    MaterialButton forwardButton;

    ActionMenuItemView archiveButton;
    ActionMenuItemView deleteButton;
    ActionMenuItemView markAsUnreadButton;


    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getParentFragmentManager().setFragmentResultListener("email", this, new FragmentResultListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {

                Email email = (Email) result.getSerializable("email");

                subjectTextView.setText(email.getTitle());
                //    inboxLabel.setText(email.getInboxes().toString());
                if (email.isFavorite()) {
                    favCheckBox.setChecked(true);
                } else {
                    favCheckBox.setChecked(false);
                }

                originTextView.setText(email.getOrigin());


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
                dateTextView.setText(date);
                //originReplyButton.setOnClickListener(replyButtonListener);


                bodyTextView.setText(email.getBody());

            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.email_view, container, false);

        toolbar = v.findViewById(R.id.emailToolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(getActivity(), R.id.email_view).navigate(R.id.mainFragmentRecyclerView);
            }
        });

        subjectTextView = v.findViewById(R.id.subjectTextView);
        bodyTextView = v.findViewById(R.id.bodyTextView);
        inboxLabel = v.findViewById(R.id.inboxLabel);
        favCheckBox = v.findViewById(R.id.favCheckBox);

        originProfilePicture = v.findViewById(R.id.profilePicture);
        originTextView = v.findViewById(R.id.address);
        dateTextView = v.findViewById(R.id.dateTextView);
        originReplyButton = v.findViewById(R.id.replyButton);
        optionsButton = v.findViewById(R.id.imageButton);

        replyButton = v.findViewById(R.id.replyButton);
        replyAllButton = v.findViewById(R.id.replyAllButton);
        forwardButton = v.findViewById(R.id.forwardButton);

        archiveButton = v.findViewById(R.id.archive);
        deleteButton = v.findViewById(R.id.delete);
        markAsUnreadButton = v.findViewById(R.id.markAsUnread);

        return v;
    }


}

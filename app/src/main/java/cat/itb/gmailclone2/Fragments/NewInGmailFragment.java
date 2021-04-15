package cat.itb.gmailclone2.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import cat.itb.gmailclone2.R;


public class NewInGmailFragment extends Fragment {
    public static FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.new_in_gmail, container, false);

        mAuth = FirebaseAuth.getInstance();

        Button b = v.findViewById(R.id.got_it);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(getActivity(), R.id.new_in_gmail).navigate(R.id.add_init_email_Fragment);
            }
        });

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
//        Toast.makeText(getContext(), currentUser.getEmail()+"", Toast.LENGTH_LONG).show();
        if (currentUser != null) {
            Navigation.findNavController(getActivity(), R.id.new_in_gmail).navigate(R.id.mainFragmentRecyclerView);
        }
    }
}

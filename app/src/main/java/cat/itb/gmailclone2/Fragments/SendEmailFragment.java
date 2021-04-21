package cat.itb.gmailclone2.Fragments;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.ActionMenuItemView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Date;

import Model.Email;
import Model.User;
import cat.itb.gmailclone2.R;

import static Resources.GetAccountEmails.getAccount;
import static android.content.ContentValues.TAG;


public class SendEmailFragment extends Fragment {
    public static FirebaseAuth mAuth;
    Spinner spinnerEmails;
    TextInputEditText to;
    TextInputEditText subject;
    TextInputEditText body;
    boolean changed = false;
    DatabaseReference imgref;
    MaterialToolbar toolbar;
    ActionMenuItemView sendButton;
    private FirebaseUser user;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createRequest();
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.send_email, container, false);
        imgref = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        spinnerEmails = v.findViewById(R.id.spinner_Emails);
        to = v.findViewById(R.id.toEditText);
        subject = v.findViewById(R.id.subjectEditText);
        body = v.findViewById(R.id.composeEmailBody);

        sendButton = v.findViewById(R.id.send);
        toolbar = v.findViewById(R.id.sendEmailToolbar);

        setHasOptionsMenu(true);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(getActivity(), R.id.send_email_layout).navigate(R.id.mainFragmentRecyclerView);
            }
        });

        final AccountManager accountManager = AccountManager.get(getContext());

        final Account[] account = getAccount(accountManager);
        final String[] items = new String[account.length];

        for (int i = 0; i < account.length; i++) {
            items[i] = account[i].name;
        }


        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_dropdown_item, items);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEmails.setAdapter(adapter);
        spinnerEmails.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (changed = true) {
                    DatabaseReference firebaseRef = imgref.getDatabase().getReference("users");
                    Query f = firebaseRef.orderByChild("email").equalTo(items[position]);
                    f.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            User u = null;
                            for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                                u = postSnapshot.getValue(User.class);
                            }
                            firebaseAuthWithGoogle(u.getUid());
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
                    changed = true;
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String key = imgref.push().getKey();
                Date currentTime = Calendar.getInstance().getTime();
                Toast.makeText(getContext(), spinnerEmails.getSelectedItem().toString(), Toast.LENGTH_LONG).show();
                Toast.makeText(getContext(), user.getPhotoUrl() + "", Toast.LENGTH_LONG).show();
                Email m = new Email(user.getPhotoUrl().toString(), user.getDisplayName(), to.getText().toString(), subject.getText().toString(), body.getText().toString(), currentTime, false, false, "Received");
                imgref.child("emails").child(key).setValue(m);
                Navigation.findNavController(getActivity(), R.id.send_email_layout).navigate(R.id.mainFragmentRecyclerView);
            }
        });
        return v;
    }


    private void createRequest() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail().requestProfile()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(getContext(), gso);
    }

    private void firebaseAuthWithGoogle(final String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener((Activity) getContext(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            user = mAuth.getCurrentUser();
                            //        Navigation.findNavController(getActivity(), R.id.send_email_layout).navigate(R.id.send_email_Fragment);

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());

                        }
                    }
                });
    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.send_email_top, menu);

    }
}

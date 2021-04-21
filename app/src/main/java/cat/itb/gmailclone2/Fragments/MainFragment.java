package cat.itb.gmailclone2.Fragments;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.ObservableSnapshotArray;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import com.squareup.picasso.Picasso;

import Model.Email;
import Model.User;
import Resources.CircleTransformation;
import cat.itb.gmailclone2.Fragments.RecyclerView.EmailAdapter;
import cat.itb.gmailclone2.R;

import static Resources.GetAccountEmails.getAccount;
import static android.content.ContentValues.TAG;


public class MainFragment extends Fragment {
    private static final int RC_SIGN_IN = 123;
    public static FirebaseDatabase database = FirebaseDatabase.getInstance();
    public static DatabaseReference myRef = database.getReference();
    public static FirebaseUser user;
    public static FirebaseAuth mAuth;
    RecyclerView recyclerView;
    DrawerLayout drawer;
    ImageButton profileIcon;
    EmailAdapter adapter;
    FloatingActionButton writeEmail;
    boolean in = false;
    private Button signIn;
    private GoogleSignInClient mGoogleSignInClient;
    private SearchView searchView;





    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createRequest();
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();

    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.main_fragment, container, false);


        //FireBase

        mAuth = FirebaseAuth.getInstance();

        user = mAuth.getCurrentUser();

        Query filter = database.getReference().child("emails").orderByChild("to").equalTo(user.getEmail());

        final FirebaseRecyclerOptions<Email> options = new FirebaseRecyclerOptions.Builder<Email>().setQuery(filter, Email.class).build();


        // SearchView
        searchView = v.findViewById(R.id.searchView);
        searchView.setOn


        //Ir a escribir email
        writeEmail = v.findViewById(R.id.writeEmail);
        writeEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(getActivity(), R.id.recyclerview).navigate(R.id.sendEmailFragment);
            }
        });






        //RecyclerView
        recyclerView = v.findViewById(R.id.recyclerview);
        adapter = new EmailAdapter(options);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mLayoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(mLayoutManager);
        adapter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle b = new Bundle();
                ObservableSnapshotArray<Email> mSnapshots = options.getSnapshots();
                Email e = mSnapshots.get(recyclerView.getChildAdapterPosition(v));
                b.putSerializable("email", e);
                getParentFragmentManager().setFragmentResult("email", b);
                Navigation.findNavController(getActivity(), R.id.recyclerview).navigate(R.id.emailFragment);
            }
        });
        recyclerView.setAdapter(adapter);


        //TopAppBar
        Toolbar toolbar = v.findViewById(R.id.topAppBar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.open();
            }
        });


        //Email Icon
        profileIcon = v.findViewById(R.id.mainFragmentProfileIcon);
        profileIcon.setOnClickListener(new View.OnClickListener() {

            //Iniciar sesion sin añadir cuenta repetidamente
            @Override
            public void onClick(View v) {
                final AccountManager accountManager = AccountManager.get(getContext());
                final Account[] account = getAccount(accountManager);
                final String[] items = new String[account.length + 1];
                for (int i = 0; i < account.length; i++) {
                    items[i] = account[i].name;
                }
                items[items.length - 1] = "Add acc";
                new MaterialAlertDialogBuilder(getContext())
                        .setTitle(getResources().getString(R.string.project_id))
                        .setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog, final int which) {
                                if (which == items.length - 1) {
                                    signIn();
                                } else {
                                    DatabaseReference firebaseRef = myRef.getDatabase().getReference("users");
                                    Query f = firebaseRef.orderByChild("email").equalTo(items[which]);

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
                                }
                            }
                        }).show();
            }
        });
        Bitmap bitmap = null;
        if (user != null) {
            Picasso.with(getContext())
                    .load(user.getPhotoUrl())
                    .placeholder(R.drawable.userimage)
                    .resize(130, 130)
                    .centerCrop().transform(new CircleTransformation())
                    .into(profileIcon);
        }

        //NavigationDrawer
        drawer = v.findViewById(R.id.draweLayout);
        final ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(getActivity(), drawer, R.string.open, R.string.close);
        drawer.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        return v;
    }


    //Metodo para Conseguir conectarse con google acc
    private void createRequest() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail().requestProfile()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(getContext(), gso);
    }

    //Iniciar el intent para conectarse a la cuenta y poder elegir que cuenta usamos
    public void signIn() {
        mGoogleSignInClient.signOut();
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    //Metodo necesario en fragment para poder usar onActivityResult
    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        super.startActivityForResult(intent, requestCode);

    }

    //Metodo Para resolver Intents
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase

                final GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());


                firebaseAuthWithGoogle(account.getIdToken());

            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
            }
        }
    }

    //Metodo que resuelve una task para conseguir conectarse con la cuenta de google
    private void firebaseAuthWithGoogle(final String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener((Activity) getContext(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            User u = new User(idToken, user.getEmail());
                            myRef.child("users").child(user.getUid()).setValue(u);
                            in = false;
                            Navigation.findNavController(getActivity(), R.id.mainFragment).navigate(R.id.mainFragmentRecyclerView);

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());

                        }
                    }
                });
    }



//    navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
//        @Override
//        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
//            int id=menuItem.getItemId();
//            //it's possible to do more actions on several items, if there is a large amount of items I prefer switch(){case} instead of if()
//            if (id==R.id.nav_home){
//                Toast.makeText(getApplicationContext(), "Home", Toast.LENGTH_SHORT).show();
//            }
//            //This is for maintaining the behavior of the Navigation view
//            NavigationUI.onNavDestinationSelected(menuItem,navController);
//            //This is for closing the drawer after acting on it
//            drawer.closeDrawer(GravityCompat.START);
//            return true;
//        }
//    });


}

package cat.itb.gmailclone2.Fragments;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
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

import java.util.ArrayList;
import java.util.List;


import cat.itb.gmailclone2.Model.Email;
import cat.itb.gmailclone2.Model.User;
import cat.itb.gmailclone2.Resources.CircleTransformation;
import cat.itb.gmailclone2.Fragments.RecyclerView.EmailAdapter;
import cat.itb.gmailclone2.Fragments.RecyclerView.EmailAdapterWIthoutFirebase;
import cat.itb.gmailclone2.R;

import static cat.itb.gmailclone2.Resources.GetAccountEmails.getAccount;
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
    EmailAdapterWIthoutFirebase adapter;
    FloatingActionButton writeEmail;
    boolean in = false;
    private Button signIn;
    private GoogleSignInClient mGoogleSignInClient;
    private SearchView searchView;
    public static List<Email> Emails = new ArrayList<>();
    private SwipeRefreshLayout swipeRefreshLayout;
    Query filter;
    private NavigationView navigationView;
     Email deletedEmail = null;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createRequest();
    }

    @Override
    public void onStart() {
        super.onStart();


    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.main_fragment, container, false);


        //FireBase

        mAuth = FirebaseAuth.getInstance();

        user = mAuth.getCurrentUser();

       filter = database.getReference().child("emails").orderByChild("to").equalTo(user.getEmail());

    //    final FirebaseRecyclerOptions<Email> options = new FirebaseRecyclerOptions.Builder<Email>().setQuery(filter, Email.class).build();

       swipeRefreshLayout = v.findViewById(R.id.swipeRefreshLayout);

        // swipe para refrescar datos
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Esto se ejecuta cada vez que se realiza el gesto
                swipeRefreshLayout.setRefreshing(true);

                cargarDatos();


            }
        });

        //Cargar datos del recycler view
        cargarDatos();

        // SearchView
        searchView = v.findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }
        //searchView.setOn

            @Override
            public boolean onQueryTextChange(String newText) {
                filter.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Emails.clear();
                        for (DataSnapshot movieSnapshot : snapshot.getChildren()) {
                            boolean not= false;
                            Email email = movieSnapshot.getValue(Email.class);
                            System.out.println(email.getTitle());
                            if (email!=null){
//                                for (int i = 0; i < Emails.size(); i++) {
//                                    if (email.getTitle().contains(query) && Emails.get(i).getKey().equals(email.getKey())) {
//                                        not = true;
//                                    }else {
//                                        not=false;
//                                    }
//                                }

                                if (email.getTitle().toLowerCase().contains(newText)) {
                                    Emails.add(email);
                                }else {
                                    Emails.remove(email);
                                }

                            }
                        }
                        if (Emails!=null){

                        }
                        adapter.notifyDataSetChanged();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                return false;
            }
        });


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
        adapter = new EmailAdapterWIthoutFirebase();
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mLayoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(mLayoutManager);
        adapter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle b = new Bundle();

                Email e = Emails.get(recyclerView.getChildAdapterPosition(v));
                b.putSerializable("email", e);
                getParentFragmentManager().setFragmentResult("email", b);
                Navigation.findNavController(getActivity(), R.id.recyclerview).navigate(R.id.emailFragment);
            }
        });


        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

                    int position = viewHolder.getAdapterPosition();
                switch (direction){
                    case ItemTouchHelper.LEFT:

                    case ItemTouchHelper.RIGHT:
                        deletedEmail = Emails.get(position);
                        Emails.remove(position);
                        adapter.notifyItemRemoved(position);
                        //      myRef.child("emails").child(deletedEmail.getKey()).removeValue();

                        Snackbar.make(recyclerView, "Deleted Email "+deletedEmail.getTitle() , Snackbar.LENGTH_LONG)
                                .setAction("Undo", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Emails.add(position,deletedEmail);
                                        adapter.notifyItemInserted(position);
                                    }
                                }).show();

                        break;
                }

            }
        }).attachToRecyclerView(recyclerView);

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

        navigationView = v.findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int id=menuItem.getItemId();
                //it's possible to do more actions on several items, if there is a large amount of items I prefer switch(){case} instead of if()
                switch (id) {
                    case 2131296641:
                        Toast.makeText(getContext(), "RECEIVED", Toast.LENGTH_SHORT).show();
                        filtradoPorInbox("Received");
                        //filter = "Received";
                        break;
                    case 2131296716:
                        Toast.makeText(getContext(), "STARRED", Toast.LENGTH_SHORT).show();
                        //filter = "Starred";
                        filtradoPorInbox("Starred");
                        break;
                    case 2131296701:
                        Toast.makeText(getContext(), "SNOOZED", Toast.LENGTH_SHORT).show();
                        //filter = "Snoozed";
                        filtradoPorInbox("Snoozed");
                        break;
                    case 2131296686:
                        Toast.makeText(getContext(), "SENT", Toast.LENGTH_SHORT).show();
                        //filter = "Sent";
                        filtradoPorInbox("Sent");
                        break;
                    case 2131296433:
                        Toast.makeText(getContext(), "DRAFTS", Toast.LENGTH_SHORT).show();
                        //filter = "Drafts";
                        filtradoPorInbox("Drafts");
                        break;
                    case 2131296345:
                        Toast.makeText(getContext(), "All Mail", Toast.LENGTH_SHORT).show();
                        //filter = "";
                        cargarDatos();
                        break;
                    case 2131296703:
                        Toast.makeText(getContext(), "Spam", Toast.LENGTH_SHORT).show();
                        //filter = "Spam";
                        filtradoPorInbox("Spam");
                        break;
                    case 2131296361:
                        Toast.makeText(getContext(), "Bin", Toast.LENGTH_SHORT).show();
                        //filter = "Deleted";
                        filtradoPorInbox("Deleted");
                        break;
                    case 2131296371:
                        Toast.makeText(getContext(), "Calendar", Toast.LENGTH_SHORT).show();
                        break;
                    case 2131296397:
                        Toast.makeText(getContext(), "Contacts", Toast.LENGTH_SHORT).show();
                        break;
                    case 2131296687:
                        Toast.makeText(getContext(), "Settings", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.emailFragment:
                }

                return true;
            }
        });

        return v;
    }

    private void cargarDatos() {
        filter.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot movieSnapshot : snapshot.getChildren()) {
                    Boolean areInArray = false;
                    Email email = movieSnapshot.getValue(Email.class);
                    System.out.println(email.getTitle());
                    if (email!=null){
                        for (int i = 0; i < Emails.size(); i++) {
                            if (email.getKey().equals(Emails.get(i).getKey())){
                                areInArray = true;
                            }
                        }
                        if (!areInArray){
                            Emails.add(email);
                        }
                        adapter.notifyDataSetChanged();
                        areInArray= false;

                    }
                }
                swipeRefreshLayout.setRefreshing(false);

                if (Emails!=null){

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void filtradoPorInbox(String inbox){
        filter.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Emails.clear();
                for (DataSnapshot movieSnapshot : snapshot.getChildren()) {
                    boolean not= false;
                    Email email = movieSnapshot.getValue(Email.class);
             //       System.out.println(email.getTitle());
                    if (email!=null){
//                                for (int i = 0; i < Emails.size(); i++) {
//                                    if (email.getTitle().contains(query) && Emails.get(i).getKey().equals(email.getKey())) {
//                                        not = true;
//                                    }else {
//                                        not=false;
//                                    }
//                                }

                        if (email.getInbox().toLowerCase().equals(inbox.toLowerCase())) {
                            Emails.add(email);
                        }else {
                            Emails.remove(email);
                        }

                    }
                }
                if (Emails!=null){

                }
                adapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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


}

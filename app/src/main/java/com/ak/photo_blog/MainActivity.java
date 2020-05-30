package com.ak.photo_blog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private Toolbar mainToolBar;
    private FirebaseAuth mAuth;
    private DatabaseReference mRef;
    private FirebaseDatabase database;

    private String currentUserId;
    private ImageButton addPost;
    private BottomNavigationView mainBottomNav;

    private HomeFragment homeFragment;
    private NotificationFragment notificationFragment;
    private AccountFragment accountFragment;

    private SharedPreferences modeSetting;
    private SharedPreferences.Editor modeEdit;
    private boolean nightMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        modeSetting = getSharedPreferences("ModeSetting",0);
        modeEdit = modeSetting.edit();
        nightMode = modeSetting.getBoolean("NightMode",false);

        if(!nightMode)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        else
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        mRef = database.getReference();

        mainToolBar = findViewById(R.id.mainToolBar);
        mainBottomNav = findViewById(R.id.mainBottomNav);

        homeFragment = new HomeFragment();
        notificationFragment = new NotificationFragment();
        accountFragment = new AccountFragment();

        replaceFragment(homeFragment);

        mainBottomNav.setItemIconTintList(null);
        mainBottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {

                    case R.id.bottom_home:
                        replaceFragment(homeFragment);
                        return true;

                    case R.id.bottom_notification:
                        replaceFragment(notificationFragment);
                        return true;

                    case R.id.bottom_account: {
                        replaceFragment(accountFragment);
                    }
                        return true;

                    default:
                        return false;
                }
            }
        });

        setSupportActionBar(mainToolBar);
        getSupportActionBar().setTitle("Photo Blog");

        addPost = findViewById(R.id.addPost);
        addPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent postIntent = new Intent(MainActivity.this,NewPostActivity.class);
                startActivity(postIntent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            sendToLogin();
        }else{
            currentUserId = mAuth.getCurrentUser().getUid();
            mRef.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(!dataSnapshot.hasChild(currentUserId)){
                        Intent settingsIntent = new Intent(MainActivity.this,SettingsActivity.class);
                        startActivity(settingsIntent);
                        finish();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(MainActivity.this,"Error:"+databaseError.getMessage(),Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){

            case R.id.action_change_mode_btn:
                if(nightMode) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    modeEdit.putBoolean("NightMode",false);
                    modeEdit.apply();
                }else{
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    modeEdit.putBoolean("NightMode",true);
                    modeEdit.apply();
                }
                return true;

            case R.id.action_logout_btn:
                logout();
                return true;

            case R.id.action_settings_btn:
                Intent settingsIntent = new Intent(MainActivity.this,SettingsActivity.class);
//              settingsIntent.putExtra("NightMode",String.valueOf(nightMode));
                startActivity(settingsIntent);
                return true;

            default:
                return false;
        }
    }

    private void logout() {
        mAuth.signOut();
        Log.i("Information MA :","User logged out.");
        sendToLogin();
    }

    private void sendToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void replaceFragment(Fragment fragment) {

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_container,fragment);
        fragmentTransaction.commit();
    }
}

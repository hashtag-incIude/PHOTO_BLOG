package com.ak.photo_blog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText loginEditText;
    private EditText loginPassText;
    private Button loginButton;
    private Button newButton;
    private FirebaseAuth mAuth;
    private ProgressBar loginProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginEditText = findViewById(R.id.loginEmailText);
        loginPassText = findViewById(R.id.loginPassText);
        loginButton = findViewById(R.id.loginButton);
        newButton = findViewById(R.id.newButton);
        loginProgressBar = findViewById(R.id.loginProgressBar);

        mAuth = FirebaseAuth.getInstance();

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String loginEmail = loginEditText.getText().toString();
                String loginPass = loginPassText.getText().toString();

                if (TextUtils.isEmpty(loginEmail) && TextUtils.isEmpty(loginPass)){

                    Toast.makeText(LoginActivity.this,"Please enter all the credentials!",Toast.LENGTH_SHORT).show();

                }else{
                    loginProgressBar.setVisibility(View.VISIBLE);
                    mAuth.signInWithEmailAndPassword(loginEmail,loginPass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful()){

                                Log.i("Information LA :","User logged in.");
                                sendToMain();

                            }else{
                                String errorMsg = task.getException().getMessage();
                                Toast.makeText(LoginActivity.this,"Error:"+errorMsg, Toast.LENGTH_LONG).show();
                            }
                            loginProgressBar.setVisibility(View.INVISIBLE);
                        }
                    });
                }
            }
        });

        newButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent regIntent = new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(regIntent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser!=null){
            sendToMain();
        }
    }

    private void sendToMain() {
        Intent mainIntent = new Intent(LoginActivity.this,MainActivity.class);
        startActivity(mainIntent);
        finish();
    }
}



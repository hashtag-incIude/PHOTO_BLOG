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

public class RegisterActivity extends AppCompatActivity {

    private EditText regEmailText;
    private EditText regPassText;
    private EditText regPassConfText;
    private Button regButton;
    private Button returnLoginButton;
    private ProgressBar regProgressBar;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        regPassConfText = findViewById(R.id.regPassConfText);
        regEmailText = findViewById(R.id.regEmailText);
        regPassText = findViewById(R.id.regPassText);
        regButton = findViewById(R.id.regButton);
        returnLoginButton = findViewById(R.id.returnLoginButton);
        regProgressBar = findViewById(R.id.regProgressBar);

        mAuth = FirebaseAuth.getInstance();

        regButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String regEmail = regEmailText.getText().toString();
                String regPass = regPassText.getText().toString();
                String regConf = regPassConfText.getText().toString();

                if (!TextUtils.isEmpty(regEmail) && !TextUtils.isEmpty(regConf) && !TextUtils.isEmpty(regPass)){
                    if(regPass.equals(regConf)) {
                        regProgressBar.setVisibility(View.VISIBLE);
                        mAuth.createUserWithEmailAndPassword(regEmail, regPass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {

                                    Intent settingsIntent = new Intent(RegisterActivity.this,SettingsActivity.class);
                                    Log.i("Information RA :","New User created.");
                                    startActivity(settingsIntent);
                                    finish();

                                } else {
                                    String errorMsg = task.getException().getMessage();
                                    Toast.makeText(RegisterActivity.this, "Error:" + errorMsg, Toast.LENGTH_LONG).show();
                                }
                                regProgressBar.setVisibility(View.INVISIBLE);
                            }
                        });
                    }else{
                        Toast.makeText(RegisterActivity.this,"Passwords do not match",Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(RegisterActivity.this,"Please enter all the credentials.",Toast.LENGTH_SHORT).show();
                }
            }
        });


        returnLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginIntent = new Intent(RegisterActivity.this,LoginActivity.class);
                startActivity(loginIntent);
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!=null){
            sendToMain();
        }
    }

    private void sendToMain() {
        Intent mainIntent = new Intent(RegisterActivity.this,MainActivity.class);
        startActivity(mainIntent);
        finish();
    }
}

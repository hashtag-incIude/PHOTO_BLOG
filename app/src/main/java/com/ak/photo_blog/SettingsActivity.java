package com.ak.photo_blog;

import androidx.annotation.NonNull;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private CircleImageView profileImage;
    private Uri mainImageURI = null;
    private Uri downloadUrl;
    private EditText profileName;
    private Button saveSettings;
    private ProgressBar settingsProgressBar;
    private String userId;
    private String userName;

    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase database;
    private DatabaseReference mRef;
    private StorageReference imagePath;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        profileImage = findViewById(R.id.profileImage);
        profileName = findViewById(R.id.profileName);
        saveSettings = findViewById(R.id.saveSettings);
        settingsProgressBar = findViewById(R.id.settingsProgressBar);

        firebaseAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        database = FirebaseDatabase.getInstance();
        mRef = database.getReference();

        userId = firebaseAuth.getCurrentUser().getUid();

        Toolbar settingToolbar = findViewById(R.id.settingToolbar);
        setSupportActionBar(settingToolbar);
        getSupportActionBar().setTitle("Account Settings");

        mRef.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(userId)){

                    Map<String,String> retrieveMap = (Map<String, String>) dataSnapshot.child(userId).getValue();
                    Log.i("Information SA:",String.valueOf(retrieveMap.get("Image")));
                    mainImageURI = Uri.parse(retrieveMap.get("Image"));
                    userName = String.valueOf(retrieveMap.get("Name"));
                    profileName.setText(String.valueOf(retrieveMap.get("Name")));

                    Picasso.get().load(String.valueOf(retrieveMap.get("Image"))).into(profileImage);
                    profileName.setFocusable(false);

                }else{
                    Toast.makeText(SettingsActivity.this,"Setup Required",Toast.LENGTH_LONG).show();
                }
                settingsProgressBar.setVisibility(View.INVISIBLE);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(SettingsActivity.this,"Error Ma",Toast.LENGTH_SHORT).show();
                settingsProgressBar.setVisibility(View.INVISIBLE);
            }
        });

        saveSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userName = profileName.getText().toString();
                if(!TextUtils.isEmpty(userName) && mainImageURI != null){

                    settingsProgressBar.setVisibility(View.VISIBLE);

                    imagePath = storageReference.child("Profile_Images").child("Users").child(userId+".jpg");
                    UploadTask uploadTask = imagePath.putFile(mainImageURI);

                    storeInFirebase(uploadTask,userName);

                }else if(TextUtils.isEmpty(userName)){
                    Toast.makeText(SettingsActivity.this,"Please choose a USERNAME",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(SettingsActivity.this,"Please choose a Profile Picture",Toast.LENGTH_SHORT).show();
                }
            }
        });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.M){
                    if (ContextCompat.checkSelfPermission(SettingsActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){

                        ActivityCompat.requestPermissions(SettingsActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);

                    }else{
                        bringImageSelection();
                    }
                }else{
                    bringImageSelection();
                }
            }
        });
    }

    private void storeInFirebase(UploadTask uploadTask,final String userName) {
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(SettingsActivity.this,"An error has occurred",Toast.LENGTH_SHORT).show();
                settingsProgressBar.setVisibility(View.INVISIBLE);
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.i("Information SA:","Upload Successful");
            }
        });
        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if(!task.isSuccessful()){
                    throw task.getException();
                }
                return imagePath.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()){
                    downloadUrl = task.getResult();
                    Log.i("Information SA:","The URL : " + downloadUrl.toString());

                    Map<String, String> userMap = new HashMap<>();
                    userMap.put("Name",userName);
                    userMap.put("Image",downloadUrl.toString());

                    mRef.child("Users").child(userId).setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){

                                Toast.makeText(SettingsActivity.this,"Changes Saved",Toast.LENGTH_SHORT).show();
                                Intent mainIntent = new Intent(SettingsActivity.this,MainActivity.class);
                                startActivity(mainIntent);
                                finish();

                            }else{
                                String error = task.getException().getMessage();
                                Toast.makeText(SettingsActivity.this,"(FIREBASE Error): "+error,Toast.LENGTH_SHORT).show();
                                settingsProgressBar.setVisibility(View.INVISIBLE);
                            }
                        }
                    });
                }
            }
        });
    }

    private void bringImageSelection() {
        saveSettings.setAlpha(1);
        saveSettings.setEnabled(true);
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,1)
                .start(SettingsActivity.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mainImageURI = result.getUri();
                profileImage.setImageURI(mainImageURI);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(SettingsActivity.this,error.toString(),Toast.LENGTH_SHORT).show();
            }
        }
    }

}

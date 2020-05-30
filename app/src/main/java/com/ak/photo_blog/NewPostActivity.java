package com.ak.photo_blog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

public class NewPostActivity extends AppCompatActivity {

    private Toolbar addPostToolbar;
    private Button postButton;
    private EditText postDesc;
    private ImageView newPostImage;
    private ProgressBar newPostProgressBar;

    private Uri postImageUri = null;
    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase database;
    private DatabaseReference mRef;
    private StorageReference imagePath;
    private StorageReference thumbsPath;
    private String userId;
    private Uri downloadUrl;

    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        newPostImage = findViewById(R.id.newPostImage);
        postDesc = findViewById(R.id.postDesc);
        postButton = findViewById(R.id.postButton);
        newPostProgressBar = findViewById(R.id.newPostProgressBar);

        addPostToolbar = findViewById(R.id.addPostToolbar);
        setSupportActionBar(addPostToolbar);
        getSupportActionBar().setTitle("Add New Post");

        firebaseAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        database = FirebaseDatabase.getInstance();
        mRef = database.getReference();
        userId = firebaseAuth.getCurrentUser().getUid();

        newPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setMinCropResultSize(512,512)
                        .setAspectRatio(2,1)
                        .start(NewPostActivity.this);
            }
        });

        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String desc = postDesc.getText().toString();

                if(!TextUtils.isEmpty(desc) && postImageUri != null){
                    newPostProgressBar.setVisibility(View.VISIBLE);
                    String currentUNIX = String.valueOf(System.currentTimeMillis());
                    imagePath = storageReference.child("User_Posts").child(userId+" "+currentUNIX+".jpg");
                    UploadTask uploadImage = imagePath.putFile(postImageUri);

//                    File newImageFile = new File(postImageUri.getPath());
//                    try {
//                        bitmap = new Compressor(NewPostActivity.this)
//                                .setMaxHeight(10)
//                                .setMaxHeight(10)
//                                .setQuality(1)
//                                .compressToBitmap(newImageFile);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    ByteArrayOutputStream out = new ByteArrayOutputStream();
//                    bitmap.compress(Bitmap.CompressFormat.PNG, 1, out);
//                    byte[] thumbData = out.toByteArray();
//                    thumbsPath = storageReference.child("User_Posts").child("Thumbs").child(userId+" "+currentUNIX+".jpg");
//                    UploadTask uploadThumb = thumbsPath.putBytes(thumbData);

                    storeInFirebase(uploadImage,userId,desc,currentUNIX);

                }else{
                    Toast.makeText(NewPostActivity.this,"All fields are compulsory",Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    private void storeInFirebase(UploadTask uploadTask, final String userId, final String desc, final String UNIXstring) {
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(NewPostActivity.this,"An error has occurred",Toast.LENGTH_SHORT).show();
                newPostProgressBar.setVisibility(View.INVISIBLE);
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.i("Information NPA:","Post Upload Successful");
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
                    Log.i("Information NPA:","The PostImage URL :"+downloadUrl.toString());

                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("Description",desc);
                    userMap.put("Image",downloadUrl.toString());
                    userMap.put("TimeStamp", UNIXstring);
                    userMap.put("Username",userId);

                    mRef.child("Posts").child(UNIXstring).setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){

                                Toast.makeText(NewPostActivity.this,"Post Uploaded",Toast.LENGTH_SHORT).show();
                                Intent mainIntent = new Intent(NewPostActivity.this,MainActivity.class);
                                startActivity(mainIntent);
                                finish();

                            }else{
                                String error = task.getException().getMessage();
                                Toast.makeText(NewPostActivity.this,"(FIREBASE Error): "+error,Toast.LENGTH_SHORT).show();
                                newPostProgressBar.setVisibility(View.INVISIBLE);
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                postImageUri = result.getUri();
                newPostImage.setImageURI(postImageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(NewPostActivity.this,error.toString(),Toast.LENGTH_SHORT).show();
            }
        }
    }
}

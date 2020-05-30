package com.ak.photo_blog;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class AccountFragment extends Fragment {

    public static final String ANONYMOUS = "anonymous";
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 1000;
    private static final int RC_PHOTO_PICKER = 2;


    private ListView mMessageListView;
    private MessageAdapter mMessageAdapter;
    private ProgressBar mProgressBar;
    private EditText mMessageEditText;
    private Button mSendButton;
    public static final int RC_SIGN_IN = 1;//RC STAND FOR request code



    private String mUsername;

    //firebase variable

    //write database
    private FirebaseDatabase mfirebaseDatabase;
    private DatabaseReference mMessagedatabaseReference;

    //read database
    private ChildEventListener mchildEventListener;

    //auth
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    //storage
    private StorageReference storageReferencephoto;
    private FirebaseStorage firebaseStorage;


    public AccountFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        mUsername = ANONYMOUS;//use intent to pass the username.

        //start using the firebase database features
        mfirebaseDatabase=FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        mMessagedatabaseReference=mfirebaseDatabase.getReference().child("messages");
        storageReferencephoto= firebaseStorage.getReference().child("chat_photos");




        // Initialize references to views
        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        mMessageListView = (ListView) view.findViewById(R.id.messageListView);
        mMessageEditText = (EditText) view.findViewById(R.id.messageEditText);
        mSendButton = (Button) view.findViewById(R.id.sendButton);

        // Initialize message ListView and its adapter
        List<FriendlyMessage> friendlyMessages = new ArrayList<>();
        mMessageAdapter = new MessageAdapter(getContext(), R.layout.item_message, friendlyMessages);
        mMessageListView.setAdapter(mMessageAdapter);

        // Initialize progress bar
        mProgressBar.setVisibility(ProgressBar.INVISIBLE);

        // ImagePickerButton shows an image picker to upload a image for a message
        // Enable Send button when there's text to send
        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mSendButton.setEnabled(true);
                } else {
                    mSendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(DEFAULT_MSG_LENGTH_LIMIT)});

        // Send button sends a message and clears the EditText
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: Send messages on click
                //adding the firebase database friendly message object

                FriendlyMessage friendlyMessage = new FriendlyMessage(mMessageEditText.getText().toString(), mUsername, null);
                mMessagedatabaseReference.push().setValue(friendlyMessage);



                // Clear input box
                mMessageEditText.setText("");
            }
        });//initiate the new child event listener

        mchildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                //datasnapshot contains the data from the firebase database
                FriendlyMessage friendlyMessage= dataSnapshot.getValue(FriendlyMessage.class);
                //get value is used to get the data of the new message
                //the getvalue can take parameter as a class
                //by passing these parameter the code deselerize the message from the databbase
                //
                mMessageAdapter.add(friendlyMessage);
            }


            public void onChildChanged( DataSnapshot dataSnapshot,  String s) { }

            public void onChildRemoved( DataSnapshot dataSnapshot) { }

            public void onChildMoved( DataSnapshot dataSnapshot,  String s) { }

            public void onCancelled( DatabaseError databaseError) { }
        };
        mMessagedatabaseReference.addChildEventListener(mchildEventListener);




        return view;

    }
}

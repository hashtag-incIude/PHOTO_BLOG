package com.ak.photo_blog;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private RecyclerView blogPostView;
    private DatabaseReference mRef;
    private List<BlogPost> listData;
    private MyAdapter adapter;

    private LinearLayoutManager mLayout;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        mRef = FirebaseDatabase.getInstance().getReference("Posts");
        mRef.keepSynced(true);

        listData = new ArrayList<>();

        blogPostView = view.findViewById(R.id.blogPostView);
        blogPostView.setHasFixedSize(true);
        mLayout = new LinearLayoutManager(getContext());
        mLayout.setReverseLayout(true);
        mLayout.setStackFromEnd(true);

        blogPostView.setLayoutManager(mLayout);

        blogPostView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                Boolean reachedBottom  = !recyclerView.canScrollVertically(1);
                if(reachedBottom){
                    Toast.makeText(getContext(),"You have viewed all the blogs.",Toast.LENGTH_SHORT).show();
                }
            }
        });

        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    for (DataSnapshot data : dataSnapshot.getChildren()){
                        String postId = data.getKey();
                        BlogPost blogPost = data.getValue(BlogPost.class).withId(postId);
                        listData.add(blogPost);
                    }
                    adapter = new MyAdapter(listData);
                    blogPostView.setAdapter(adapter);
                }
            }
            @Override
            public void  onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(),databaseError.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });

        // Inflate the layout for this fragment
        return view;
    }
}

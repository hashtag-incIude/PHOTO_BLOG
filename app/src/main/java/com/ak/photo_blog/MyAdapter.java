package com.ak.photo_blog;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder>{

    private List<BlogPost> listData;
    private Context context;

    private DatabaseReference mRef;
    private FirebaseAuth mAuth;

    public MyAdapter(List<BlogPost> listData) {
        this.listData = listData;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view= LayoutInflater.from(context).inflate(R.layout.blog_list_item,parent,false);
        mRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        holder.setIsRecyclable(false);

        final String postId = listData.get(position).BlogPostId;
        final String currentUserId = mAuth.getCurrentUser().getUid();
        final BlogPost blogPost=listData.get(position);

        mRef.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<String, String> retrieveMap = (Map<String, String>) dataSnapshot.child(blogPost.getUsername()).getValue();
                holder.blogUsername.setText(String.valueOf(retrieveMap.get("Name")));
                Glide.with(context).load(Uri.parse(retrieveMap.get("Image"))).into(holder.userProfileImage);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(context,databaseError.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });

        holder.blogDate.setText(blogPost.getTimeStamp());
        holder.blogDesc.setText(blogPost.getDescription());
        Glide.with(context).load(blogPost.getImage()).into(holder.postImage);

        mRef.child("Posts/" + postId + "/Likes").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren())
                    holder.likeCount.setText(dataSnapshot.getChildrenCount() + " Likes");
                else
                    holder.likeCount.setText("0 Likes");
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        mRef.child("Posts/" + postId + "/Likes").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(currentUserId)){
                    holder.likeIcon.setImageDrawable(context.getDrawable(R.drawable.like_icon_red));
                }else{
                    holder.likeIcon.setImageDrawable(context.getDrawable(R.drawable.like_icon_grey));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        holder.likeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRef.child("Posts/" + postId + "/Likes").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(currentUserId)){
                            mRef.child("Posts/" + postId + "/Likes").child(currentUserId).removeValue();
                            holder.likeIcon.setImageDrawable(context.getDrawable(R.drawable.like_icon_grey));
                        }else{
                            Map<String,Object> likesMap = new HashMap<>();
                            likesMap.put("timestamp",System.currentTimeMillis());
                            mRef.child("Posts/" + postId + "/Likes").child(currentUserId).setValue(likesMap);
                            holder.likeIcon.setImageDrawable(context.getDrawable(R.drawable.like_icon_red));
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView blogUsername,blogDate,blogDesc,likeCount;
        private CircleImageView userProfileImage;
        private ImageView postImage,likeIcon;

        public ViewHolder(View itemView) {
            super(itemView);
            blogUsername = itemView.findViewById(R.id.blogUsername);
            blogDate = itemView.findViewById(R.id.blogDate);
            blogDesc = itemView.findViewById(R.id.blogDesc);
            userProfileImage = itemView.findViewById(R.id.userProfileImage);
            postImage = itemView.findViewById(R.id.postImage);
            likeIcon = itemView.findViewById(R.id.likeIcon);
            likeCount = itemView.findViewById(R.id.likeCount);
        }
    }
}
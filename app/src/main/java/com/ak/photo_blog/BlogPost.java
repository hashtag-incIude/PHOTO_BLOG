package com.ak.photo_blog;

import android.text.format.DateFormat;

public class BlogPost extends BlogPostId{

    private String Image, TimeStamp, Description, Username;

    public BlogPost() {
    }

    public BlogPost(String Image, String TimeStamp, String Description, String Username) {
        this.Image = Image;
        this.TimeStamp = TimeStamp;
        this.Description = Description;
        this.Username = Username;
    }

    public String getImage() {
        return Image;
    }

    public void setImage(String image) {
        Image = image;
    }

    public String getTimeStamp() {
        return DateFormat.format("dd/MM/yyyy", Long.parseLong(TimeStamp)).toString();
    }

    public void setTimeStamp(String timeStamp) {
        TimeStamp = timeStamp;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getUsername() {
        return Username;
    }

    public void setUsername(String username) {
        Username = username;
    }

}
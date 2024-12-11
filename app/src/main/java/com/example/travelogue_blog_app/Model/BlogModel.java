package com.example.travelogue_blog_app.Model;

public class BlogModel {
    String id, title, content, location, image, creator;

    // constructor
    public BlogModel(String id, String title, String content, String location, String image, String creator) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.location = location;
        this.image = image;
        this.creator = creator;
    }

    // getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.image = creator;
    }
}

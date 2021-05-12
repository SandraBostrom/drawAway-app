package com.example.drawawaytest.Cards;

public class Cards {


    private String userId;
    private String name;
    private String category;
    private String description;
    private String profilBildUrl;

    public Cards(String userId, String name, String profilBildUrl, String category, String description){
        this.description = description;
        this.category = category;
        this.userId = userId;
        this.name = name;
        this.profilBildUrl = profilBildUrl;

    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfilBildUrl() {
        return profilBildUrl;
    }

    public void setProfilBildUrl(String profilBildUrl) {
        this.profilBildUrl = profilBildUrl;
    }
}

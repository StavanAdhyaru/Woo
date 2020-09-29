package com.example.woo.Cards;

public class cards {
    private String userId;
    private String userAge;
    private String name;
    private String profileImageUrl;
    private String aboutUser;
    public cards(String userId, String name, String profileImageUrl,String aboutUser,String userAge){
        this.userId = userId;
        this.name = name;
        this.profileImageUrl = profileImageUrl;
        this.aboutUser = aboutUser;
        this.userAge= userAge;
    }
    public String getUserId(){ return userId; }
    public void setUserId(String userId){ this.userId = userId; }
    public String getUserAge(){ return userAge; }
    public void setUserAge(String userAge){ this.userAge = userAge; }
    public String getName(){
        return name;
    }
    public void setName(String name){
        this.name = name;
    }
    public String getAboutUser(){
        return aboutUser;
    }
    public void setAboutUser(String AboutUser){
        this.aboutUser = aboutUser;
    }
    public String getProfileImageUrl(){ return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl){ this.profileImageUrl = profileImageUrl; }
}

package com.example.melanomaly;

public class InfoSave {

    private int user_id;
    private float score;

    public InfoSave(int user_id, float score) {
        this.user_id = user_id;
        this.score = score;
    }

    public int getUser_id() {
        return user_id;
    }
    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public float getScore() {return score;}
    public void setScore(float score) {this.score = score;}

}

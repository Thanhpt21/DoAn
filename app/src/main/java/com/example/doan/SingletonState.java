package com.example.doan;

import android.util.Log;

public class SingletonState {
    private String humid="70", temp="30";
    public static SingletonState singletonState;

    public static SingletonState getInstance(){
        if(singletonState == null){
            singletonState = new SingletonState();
            Log.d("abc", "chua co");
        }
        return singletonState;
    }

    public String getHumid() {
        return humid;
    }

    public void setHumid(String humid) {
        this.humid = humid;
    }

    public String getTemp() {
        return temp;
    }

    public void setTemp(String temp) {
        this.temp = temp;
        Log.d("d",temp);
    }
}

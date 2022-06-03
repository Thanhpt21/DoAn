package com.example.doan;

public class FirebaseModel {
    private boolean pump;
    private double humi;
    private double temp;

    public FirebaseModel() {
    }

    public boolean getPump() {
        return pump;
    }

    public double getHumi() {
        return humi;
    }

    public double getTemp() {
        return temp;
    }

    public void setPump(boolean pump) {
        this.pump = pump;
    }

    public void setHumi(double humi) {
        this.humi = humi;
    }

    public void setTemp(double temp) {
        this.temp = temp;
    }
}

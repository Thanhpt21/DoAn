package com.example.doan.model;

public class Device {
    private int temp;
    private int humi;
    boolean alive;
    boolean pump;

    public Device(int temp, int humi, boolean alive, boolean pump) {
        this.temp = temp;
        this.humi = humi;
        this.alive = alive;
        this.pump = pump;
    }

    public Device() {
    }

    public int getTemp() {
        return temp;
    }

    public void setTemp(int temp) {
        this.temp = temp;
    }

    public int getHumi() {
        return humi;
    }

    public void setHumi(int humi) {
        this.humi = humi;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public boolean isPump() {
        return pump;
    }

    public void setPump(boolean pump) {
        this.pump = pump;
    }


}

package com.cycos.controllers;

import java.math.BigInteger;

public class Lot {

    private int number;
    private int status;
    private int rssi;
    private long lastSeen;
    private char mac;
    private float batteryState;

    // Standard-Konstruktoren, Getter und Setter
    public Lot() {
    }

    public Lot(int number, int status) {
        this.number = number;
        this.status = status;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
    public int getRssi(){
        return rssi;
    }
    public void setRssi(int rssi){
        this.rssi = rssi;
    }
    public long getLastSeen(){
        return lastSeen;
    }
    public void setLastSeen(long lastSeen){
        this.lastSeen = lastSeen;
    }
    public char getMac(){
        return mac;
    }
    public void setMac(char mac){
        this.mac = mac;
    }
    public int setBatteryState(int batteryState){
        return batteryState;
    }
    public float getBatteryState(){
        return batteryState;
    }


}


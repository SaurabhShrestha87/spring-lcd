package com.example.demo.model;
public enum DeviceType {
    DEVICE0("/dev/ttyACM0"),
    DEVICE1("/dev/ttyACM1"),
    // ...
    DEVICE2("/dev/ttyACM2");

    public final String device;

    DeviceType(String device) {
        this.device = device;
    }
    @Override
    public String toString() {
        return this.device;
    }
}
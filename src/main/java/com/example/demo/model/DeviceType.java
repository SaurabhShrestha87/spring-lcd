package com.example.demo.model;

public enum DeviceType {
    DEVICE0("/dev/ttyACM0"), DEVICE1("/dev/ttyACM1"), DEVICE2("/dev/ttyACM2");
    private final String device;

    DeviceType(String device) {
        this.device = device;
    }

    public static DeviceType fromString(String text) {
        for (DeviceType deviceType : DeviceType.values()) {
            if (deviceType.device.equalsIgnoreCase(text)) {
                return deviceType;
            }
        }
        return null;
    }

    public String getText() {
        return this.device;
    }

    @Override
    public String toString() {
        return this.device;
    }
}
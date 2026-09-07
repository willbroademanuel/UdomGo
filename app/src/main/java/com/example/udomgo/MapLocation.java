
package com.example.udomgo;

public class MapLocation {

    private String name;
    private String college;
    private double latitude;
    private double longitude;

    public MapLocation(
            String name,
            String college,
            double latitude,
            double longitude
    ) {
        this.name = name;
        this.college = college;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public String getCollege() {
        return college;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Override
    public String toString() {
        return name;
    }
}
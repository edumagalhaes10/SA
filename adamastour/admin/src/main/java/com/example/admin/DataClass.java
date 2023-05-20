package com.example.admin;

public class DataClass {
    private String Name;
    private String City;
    private String Lat;
    private String Long;

    public DataClass(String Name, String city, String lat, String aLong) {
        this.Name = Name;
        this.City = city;
        this.Lat = lat;
        this.Long = aLong;
    }

    public String getName() {
        return Name;
    }

    public String getCity() {
        return City;
    }

    public String getLat() {
        return Lat;
    }

    public String getLong() {
        return Long;
    }
}

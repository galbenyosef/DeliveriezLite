package com.gal.deliveriez;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Gal on 18/10/2017.
 */

public class Deliverer  {

    private String name,phoneNumber;
    private double latitude,longtitude;


    public Deliverer(){}


//    protected Deliverer(Parcel in) {
//        name = in.readString();
//        phoneNumber = in.readString();
//        latitude = in.readDouble();
//        longtitude = in.readDouble();
//    }
//
//    public static final Creator<Deliverer> CREATOR = new Creator<Deliverer>() {
//        @Override
//        public Deliverer createFromParcel(Parcel in) {
//            return new Deliverer(in);
//        }
//
//        @Override
//        public Deliverer[] newArray(int size) {
//            return new Deliverer[size];
//        }
//    };

    public void setName(String name) {
        this.name = name;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setLongtitude(double longtitude) {
        this.longtitude = longtitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongtitude() {
        return longtitude;
    }

    @Override
    public String toString(){
        return getName()+"\n"+getPhoneNumber();
    }

//    @Override
//    public int describeContents() {
//        return 0;
//    }
//
//    @Override
//    public void writeToParcel(Parcel parcel, int i) {
//        parcel.writeString(name);
//        parcel.writeString(phoneNumber);
//        parcel.writeDouble(latitude);
//        parcel.writeDouble(longtitude);
//    }
}

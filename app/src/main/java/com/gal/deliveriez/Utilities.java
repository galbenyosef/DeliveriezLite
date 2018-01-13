package com.gal.deliveriez;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;
import android.widget.EditText;
import android.widget.RemoteViews;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Gal on 22/10/2017.
 */

public class Utilities {

    public static final int MESSAGE_SHUTDOWN = 99;
    public static final int MESSAGE_ACTIVITY_ONLINE = 100;
    public static final int MESSAGE_HIRE_DELIVERERS = 101;
    public static final int MESSAGE_GET_DATA = 102;
    public static final int MESSAGE_SET_DATA = 103;
    public static final int MESSAGE_DELIVERERS_MARKED = 104;
    public static final int MESSAGE_ACTIVITY_OFFLINE = 105;



    public static List<Address> validateLocation(Context context, EditText ed){
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        String textToSearch = ed.getText().toString()+", Israel";
        List<Address> fromLocationName = null;

        try {
            fromLocationName = geocoder.getFromLocationName(textToSearch,5);
        }
        catch (Exception ex){
            Log.d("H",ex.getMessage());
            return null;
        }
        return fromLocationName;
    }

}

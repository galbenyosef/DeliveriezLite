package com.gal.deliveriez;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

/**
 * Created by Gal on 16/10/2017.
 */

public final class FragmentHandler {

    public static void switchFragment(FragmentManager fm, int xamlresid, Fragment fragment){
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(xamlresid,fragment);
        transaction.commit();
    }
}

package com.gal.deliveriez;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class FragmentList extends Fragment {

    private ArrayList<String> deliverers_info;
    ArrayAdapter adapter;
    ListView listview;

    private BroadcastReceiver mBroadcastReceiver;
    private IntentFilter intentFilter;


    public FragmentList() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        intentFilter = new IntentFilter();
        intentFilter.addAction("population_list");
        deliverers_info = new ArrayList<>();
        adapter = new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, deliverers_info);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_list, container, false);

        listview = v.findViewById(R.id.deliverers_list);

        listview.setAdapter(adapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                final String item = (String) parent.getItemAtPosition(position);
            }

        });
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, final Intent intent) {
                final String action = intent.getAction();
                switch (action) {
                    case "population_list":
                        populate_list(intent);
                        break;
                    case (Intent.ACTION_CLOSE_SYSTEM_DIALOGS):
                        context.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
                        break;
                    default:
                        Log.d("H","default");
                        break;
                }
            }
        };
        return v;
    }

    private void populate_list(final Intent intent){

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                Bundle bundle = intent.getExtras();
                HashMap<String, Deliverer> deliverers = new HashMap<>();
                deliverers = deliverers.getClass().cast(bundle.getSerializable("deliverers"));
                deliverers_info.clear();
                if (deliverers != null) {
                    for (String key : deliverers.keySet()) {
                        Deliverer next = deliverers.get(key);
                        deliverers_info.add(next.getName() + " " + getCompleteAddressString(next.getLatitude(),next.getLongtitude()));
                    }
                }
                adapter.notifyDataSetChanged();
            }
        });

    }

    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(getContext(), Locale.getDefault());
        String address = null;
        try {
            addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            if (addresses.size() == 0) return "";
            address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()

        }
        catch (IOException ex){}


        return address;
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mBroadcastReceiver,intentFilter);

    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mBroadcastReceiver);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

}

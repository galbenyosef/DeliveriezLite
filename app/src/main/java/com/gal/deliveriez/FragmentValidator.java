package com.gal.deliveriez;


import android.app.Dialog;
import android.location.Address;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import java.util.List;
import static com.gal.deliveriez.Utilities.validateLocation;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentValidator extends Fragment {

    private FirebaseUser currentUser;
    private DatabaseReference persons_db,deliverers_db;

    private TextView status;
    private EditText firstName, lastName, phoneNumber, address;
    private Spinner spinner;
    private String position;

    public FragmentValidator() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseDatabase db  = FirebaseDatabase.getInstance();
        currentUser = auth.getCurrentUser();
        persons_db = db.getReference("persons");
        deliverers_db = db.getReference("deliverers");
        final View v = inflater.inflate(R.layout.fragment_validator, container, false);
        status = v.findViewById(R.id.status);
        firstName = v.findViewById(R.id.firstname);
        lastName = v.findViewById(R.id.lastname);
        phoneNumber = v.findViewById(R.id.phonenumber);
        address = v.findViewById(R.id.address);
        spinner = v.findViewById(R.id.role_spinner);
        position="Deliverer";
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.role_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i){
                    case 0:
                        position="Deliverer";
                        v.findViewById(R.id.address).setVisibility(View.GONE);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        getActivity().findViewById(R.id.validate_button).setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                if (validateForm() && currentUser!= null){
                    switch (position){
                        case "Deliverer":
                            Deliverer deliverer = new Deliverer();
                            deliverer.setName(firstName.getText().toString()+" "+lastName.getText().toString());
                            deliverer.setPhoneNumber(phoneNumber.getText().toString());
                            deliverers_db.child(currentUser.getUid()).setValue(deliverer);
                            break;
                    }
                    Person person = new Person();
                    person.setName(firstName.getText().toString()+" "+lastName.getText().toString());
                    person.setAuthorized(false);
                    person.setPosition(position);
                    person.setEmail(currentUser.getEmail());
                    persons_db.child(currentUser.getUid()).setValue(person);
                }
            }

        });

        setValidatingView();
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (currentUser!=null){
            persons_db.child(currentUser.getUid()).addValueEventListener(selectionEvent);
        }
        else{
            status.setText("Please sign in");
            status.setVisibility(View.VISIBLE);
        }
    }

    private ValueEventListener selectionEvent = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            GenericTypeIndicator<Person> holder = new GenericTypeIndicator<Person>(){};
            Person currentPerson = dataSnapshot.getValue(holder);
            if (currentPerson != null){
                if (currentPerson.isAuthorized()) {
                    getActivity().findViewById(R.id.validate_button).setVisibility(View.GONE);
                    persons_db.child(currentUser.getUid()).removeEventListener(selectionEvent);
                    switch (currentPerson.getPosition()) {
                        case "Deliverer":
                            FragmentHandler.switchFragment(getFragmentManager(), R.id.fragment_container, new FragmentDeliverer());
                            break;
                        case "Boss":
                            FragmentHandler.switchFragment(getFragmentManager(), R.id.fragment_container, new FragmentBoss());
                            break;
                    }
                } else {
                    setValidatingView();
                }
            }
            else {
                setValidationForm();
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private void setValidatingView(){

        firstName.setVisibility(View.GONE);
        lastName.setVisibility(View.GONE);
        phoneNumber.setVisibility(View.GONE);
        address.setVisibility(View.GONE);
        spinner.setVisibility(View.GONE);
        status.setVisibility(View.VISIBLE);
        getActivity().findViewById(R.id.validate_button).setVisibility(View.GONE);
        status.setText("Waiting for validation...");

    }


    private void setValidationForm(){

        firstName.setVisibility(View.VISIBLE);
        lastName.setVisibility(View.VISIBLE);
        phoneNumber.setVisibility(View.VISIBLE);
        spinner.setVisibility(View.VISIBLE);
        status.setText("Please fill validating form...");
        getActivity().findViewById(R.id.validate_button).setVisibility(View.VISIBLE);

    }
    private boolean validateForm() {
        boolean valid = true;

        String firstName = this.firstName.getText().toString();
        if (TextUtils.isEmpty(firstName) || firstName.length() < 2) {
            this.firstName.setError("Required.");
            valid = false;
        } else {
            this.firstName.setError(null);
        }

        String lastName = this.lastName.getText().toString();
        if (TextUtils.isEmpty(lastName) || lastName.length() < 2) {
            this.lastName.setError("Required.");
            valid = false;
        } else {
            this.lastName.setError(null);
        }

        String phoneNumber = this.phoneNumber.getText().toString();
        if (TextUtils.isEmpty(phoneNumber) || phoneNumber.length() != 10) {
            this.phoneNumber.setError("Required.");
            valid = false;
        } else {
            this.phoneNumber.setError(null);
        }


        return valid;
    }
}

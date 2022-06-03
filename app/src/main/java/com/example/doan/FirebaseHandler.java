package com.example.doan;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FirebaseHandler {
    private static FirebaseHandler handler;
    private FirebaseDatabase firebase;
    private DatabaseReference firebaseReference;

    private FirebaseHandler() {
        firebase = FirebaseDatabase.getInstance("https://doan-4abdf-default-rtdb.asia-southeast1.firebasedatabase.app/");
    }

    public static FirebaseHandler getInstance() {
        if (handler == null) {
            handler = new FirebaseHandler();
        }
        return handler;
    }

    public void onDbChange(HandleFirebaseChange onChange) {
        firebaseReference = firebase.getReference("Device");

        firebaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null)
                    onChange.handle(dataSnapshot.getValue(FirebaseModel.class));
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
    }

    public FirebaseDatabase getFirebase(){
        return firebase;
    }

    public void updateField(String field, Object value) {
        firebaseReference.child(field).setValue(value);
    }
}

package lk.edu.nchs.traincollisionavoidancesystem.m_Helper;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by Dulkith on 6/8/18.
 */

public class DataControl {

    private static FirebaseDatabase database = FirebaseDatabase.getInstance();
    private static int readIntData;
    private static String readStringData;
    private static double readDoubleData;

    // Read data.

    public static int readInt(String signature) {

        DatabaseReference myRef = database.getReference(signature);
        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                readIntData = dataSnapshot.getValue(Integer.class);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                // Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        return readIntData;
    }

    public static  String readString(String signature) {
        DatabaseReference myRef = database.getReference(signature);
        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                readStringData = dataSnapshot.getValue(String.class);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                // Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        return readStringData;
    }

    public static  double readDouble(String signature) {
        DatabaseReference myRef = database.getReference(signature);
        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                readDoubleData = dataSnapshot.getValue(Double.class);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                // Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
        return readDoubleData;
    }

    // Write data.

    public static  void writeInt(String signature, int data) {
        DatabaseReference myRef = database.getReference(signature);
        myRef.setValue(data);
    }

    public static  void writeString(String signature, String data) {
        DatabaseReference myRef = database.getReference(signature);
        myRef.setValue(data);
    }

    public static  void writeDouble(String signature, double data) {
        DatabaseReference myRef = database.getReference(signature);
        myRef.setValue(data);
    }
}

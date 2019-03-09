package lk.edu.nchs.traincollisionavoidancesystem;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class BulbsControlFragment extends Fragment {

    Button changeMode;
    TextView displayText;
    Switch switchBulb1,switchBulb2;
    int bulb1Status = 0, bulb2Status = 0;
    int mode = 0;

    FirebaseDatabase database = FirebaseDatabase.getInstance();

    Vibrator vibrator;

    Handler handler = new Handler();

    public BulbsControlFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.bulbs_control_layout, container, false);

        displayText = v.findViewById(R.id.displayText);
        switchBulb1 = v.findViewById(R.id.switchBulb1);
        switchBulb2 = v.findViewById(R.id.switchBulb2);
        changeMode = v.findViewById(R.id.changeMode);

        vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);

        handler.postDelayed(updateStatus, 0);

        switchBulb1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vibrator.vibrate(100);
                if (switchBulb1.isChecked()){
                    DatabaseReference myRef = database.getReference("BULB_1_STATUS");
                    myRef.setValue(1);
                }else{
                    DatabaseReference myRef = database.getReference("BULB_1_STATUS");
                    myRef.setValue(0);
                }
            }
        });

        switchBulb2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vibrator.vibrate(100);
                if (switchBulb2.isChecked()){
                    DatabaseReference myRef = database.getReference("BULB_2_STATUS");
                    myRef.setValue(1);
                }else{
                    DatabaseReference myRef = database.getReference("BULB_2_STATUS");
                    myRef.setValue(0);
                }
            }
        });

        changeMode.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                final CharSequence[] items = {
                        "1. Manual Door Lock / Unlock",
                        "2. Manual Lights On / Off",
                        "3. Fully Auto Control",
                        "4. Auto Door / Manual Lights",
                        "5. Manual Door / Auto Lights"};

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Select Happy Lock Mode");
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, final int item) {
                        vibrator.vibrate(60);
                        new AlertDialog.Builder(getActivity())
                                .setTitle("Change Mode")
                                .setMessage("Do you really want to change happy lock mode ?")
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        DatabaseReference myRef = database.getReference("MODE");
                                        myRef.setValue(item);
                                        Toast.makeText(getActivity().getApplicationContext(), "System mode changed", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .setNegativeButton(android.R.string.no, null).show();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        return v;
    }

    private Runnable updateStatus = new Runnable() {
        @Override
        public void run() {
            try {
                buttonStatus();
                getMode();
            } catch (Exception e) {

            }
            handler.postDelayed(this, 2000);
        }
    };

    void getMode() {
        DatabaseReference myRef = database.getReference("MODE");
        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                mode = dataSnapshot.getValue(Integer.class);

                switch (mode) {
                    case 0:
                        displayText.setText("Manual Door Lock / Unlock Mode");
                        break;
                    case 1:
                        displayText.setText("Manual Lights On / Off Mode");
                        break;
                    case 2:
                        displayText.setText("Fully Auto Mode");
                        break;
                    case 3:
                        displayText.setText("Auto Door & Manual Lights Mode");
                        break;
                    case 4:
                        displayText.setText("Manual Door & Auto Lights Mode");
                        break;
                    default:
                        displayText.setText("Loading...");
                        displayText.setText("");
                        break;
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                // Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    void buttonStatus() {
        DatabaseReference myRef1 = database.getReference("BULB_1_STATUS");
        // Read from the database
        myRef1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                bulb1Status = dataSnapshot.getValue(Integer.class);
                switchBulb1.setVisibility(View.VISIBLE);

                if (bulb1Status == 1)
                    switchBulb1.setChecked(true);
                else if (bulb1Status == 0)
                    switchBulb1.setChecked(false);

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                // Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        DatabaseReference myRef2 = database.getReference("BULB_2_STATUS");
        // Read from the database
        myRef2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                bulb2Status = dataSnapshot.getValue(Integer.class);
                switchBulb2.setVisibility(View.VISIBLE);

                if (bulb2Status == 1 )
                    switchBulb2.setChecked(true);
                else if (bulb1Status == 0 )
                    switchBulb2.setChecked(false);

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                // Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

}

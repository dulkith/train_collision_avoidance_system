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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.skyfishjy.library.RippleBackground;

public class DoorControlFragment extends Fragment {

    Button changeMode;
    TextView displayText;
    ImageView centerImageLocked, centerImageUnlocked;
    RippleBackground rippleBackground1, rippleBackground2;
    int doorStatus = 0;
    int mode = 0;
    Vibrator vibrator;

    FirebaseDatabase database = FirebaseDatabase.getInstance();

    Handler handler = new Handler();

    public DoorControlFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.door_control_layout, container, false);

        handler.postDelayed(updateStatus, 0);

        displayText = v.findViewById(R.id.displayText);
        changeMode = v.findViewById(R.id.changeMode);
        centerImageLocked = v.findViewById(R.id.centerImageLocked);
        centerImageUnlocked = v.findViewById(R.id.centerImageUnlocked);

        vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);

        centerImageLocked.setVisibility(View.GONE);
        centerImageUnlocked.setVisibility(View.GONE);

        centerImageLocked.setOnClickListener(lockUnlockClickListener);
        centerImageUnlocked.setOnClickListener(lockUnlockClickListener);

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
        // animation
        rippleBackground1 = v.findViewById(R.id.content_locked);
        rippleBackground2 = v.findViewById(R.id.content_unlocked);

        // readDoorStatus();
        return v;
    }

    private Runnable updateStatus = new Runnable() {
        @Override
        public void run() {
            try {
                changeDoorStatus();
                getMode();
            } catch (Exception e) {

            }
            handler.postDelayed(this, 2000);
        }
    };

    View.OnClickListener lockUnlockClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            vibrator.vibrate(200);
            if (v == centerImageLocked) {

                DatabaseReference myRef = database.getReference("DOOR_STATUS");
                myRef.setValue(1);

                centerImageLocked.setVisibility(View.GONE);
                centerImageUnlocked.setVisibility(View.VISIBLE);
                rippleBackground2.startRippleAnimation();
                rippleBackground1.stopRippleAnimation();
            } else if (v == centerImageUnlocked) {

                DatabaseReference myRef = database.getReference("DOOR_STATUS");
                myRef.setValue(0);

                centerImageUnlocked.setVisibility(View.GONE);
                centerImageLocked.setVisibility(View.VISIBLE);
                rippleBackground1.startRippleAnimation();
                rippleBackground2.stopRippleAnimation();
            }
        }
    };


    void changeDoorStatus() {
        DatabaseReference myRef = database.getReference("DOOR_STATUS");
        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                doorStatus = dataSnapshot.getValue(Integer.class);

                if (doorStatus == 1) {
                    centerImageLocked.setVisibility(View.GONE);
                    centerImageUnlocked.setVisibility(View.VISIBLE);
                    rippleBackground2.startRippleAnimation();
                    rippleBackground1.stopRippleAnimation();
                } else {
                    centerImageUnlocked.setVisibility(View.GONE);
                    centerImageLocked.setVisibility(View.VISIBLE);
                    rippleBackground1.startRippleAnimation();
                    rippleBackground2.stopRippleAnimation();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                // Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

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


}

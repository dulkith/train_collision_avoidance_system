package lk.edu.nchs.traincollisionavoidancesystem;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuBuilder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.mapzen.speakerbox.Speakerbox;
import com.pusher.pushnotifications.PushNotifications;

public class MainActivity extends AppCompatActivity implements
        View.OnClickListener {

    FirebaseDatabase database = FirebaseDatabase.getInstance();

    private Menu menu;

    Handler handler = new Handler();
    Handler handlerIcon = new Handler();

    // private static int CODE_AUTHENTICATION_VERIFICATION = 241;
    int checkOnline = 1;

    private ImageView imageView1;

    Speakerbox speakerbox;

    BottomNavigationViewEx navigation ;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        isOnline();


        PushNotifications.start(getApplicationContext(), "98f86d7e-a81e-4974-bc64-31e4530bcdea");
        PushNotifications.subscribe("hello");



        handler.postDelayed(updateStatus, 0);


        imageView1 = findViewById(R.id.imageBack);

        navigation = findViewById(R.id.bottomNavigation);
        navigation.enableAnimation(true);
        navigation.enableShiftingMode(false);
        navigation.enableItemShiftingMode(false);

        loadActivity();
        speakerbox = new Speakerbox(getApplication());
        speakerbox.play("Welcome to Train Collision Avoidance System.");

//        // Authentication - pattern or password or pin or fingerprint
//        KeyguardManager km = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
//        if (km.isKeyguardSecure()) {
//
//            Intent i = km.createConfirmDeviceCredentialIntent("Happy Lock Authentication", "Draw your pattern lock");
//            startActivityForResult(i, CODE_AUTHENTICATION_VERIFICATION);
//        } else {
//            Toast.makeText(this, "No any security setup done by user(pattern or password or pin or fingerprint", Toast.LENGTH_LONG).show();
//            // System.exit(0);
//        }

//        if(isLoginSuccessfully){
//            loadActivity();
//        }else {
//
//        }
    }

    protected void loadActivity() {
        //loading the default fragment
        loadFragment(new HomeActivity());

        navigation.setOnNavigationItemSelectedListener(new BottomNavigationViewEx.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment = null;

                switch (item.getItemId()) {
                    case R.id.action_home:
                        fragment = new HomeActivity();
                        break;

                    case R.id.action_door_lock:
                        fragment = new DoorControlFragment();
                        break;

                    case R.id.action_light:
                        fragment = new CameraFragment();
                        break;

                    case R.id.action_location:
                        fragment = new LoctionFragment();
                        break;
                    case R.id.action_door_history:
                        fragment = new DoorHistoryFragment();
                        break;
                }

                return loadFragment(fragment);
            }


        });

    }

    private boolean loadFragment(Fragment fragment) {
        //switching fragment
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_layout, fragment)
                    .commit();
            return true;
        }
        return false;

    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        this.menu = menu;
        // MenuInflater mainMenu = new MenuInflater(this);
        //  mainMenu.inflate(R.menu.mainmenu, menu)
        getMenuInflater().inflate(R.menu.mainmenu, menu);

        if (menu instanceof MenuBuilder) {
            MenuBuilder m = (MenuBuilder) menu;
            m.setOptionalIconsVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Fragment fragment = null;
        switch (item.getItemId()) {
            case R.id.home_menu:
                fragment = new HomeActivity();
                break;
            case R.id.voice_menu:
                fragment = new HomeActivity();
                break;
            case R.id.action_settings:
                Intent intent = new Intent(this,SettingsActivity.class);
                startActivity(intent);
                // launch settings activity
               // startActivity(new Intent(MainActivity.this, SettingsPrefActivity.class));
                //fragment = new HomeActivity();
                break;
            case R.id.about_menu:
                fragment = new AboutFragment();
                break;
            case R.id.exit_menu:
                System.exit(0);
                break;
        }
        loadFragment(fragment);
        return true;
    }

    private Runnable updateStatus = new Runnable() {
        @Override
        public void run() {
            try {
                checkHappyLockIsOnline();
            } catch (Exception e) {

            }
            //   handler.postDelayed(this, 000);
        }
    };

    void checkHappyLockIsOnline() {
        DatabaseReference myRef = database.getReference("ONLINE");
        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                checkOnline = dataSnapshot.getValue(Integer.class);

                if (menu != null) {
                    if (checkOnline == 1) {
                        MenuItem item = menu.findItem(R.id.onlineStatus);
                        if (item != null) {
                            item.setIcon(R.drawable.online_icon);
                        }
                        handlerIcon.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                DatabaseReference myRef = database.getReference("ONLINE");
                                myRef.setValue(0);
                            }
                        }, 1000);

                    } else {
                        MenuItem item = menu.findItem(R.id.onlineStatus);
                        if (item != null) {
                            item.setIcon(R.drawable.offline_icon);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                // Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    public void checkNetworkConnection() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("No internet Connection");
        builder.setMessage("Please turn on internet connection and try again");
        builder.setNegativeButton("close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                isOnline();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                System.exit(0);
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnected();
        if (isConnected) {
            Toast.makeText(this, "Connected", Toast.LENGTH_LONG).show();
            return true;
        } else {
            checkNetworkConnection();
            Toast.makeText(this, "Not Connected", Toast.LENGTH_LONG).show();
            return false;
        }
    }


    @Override
    public void onClick(View view) {

    }


}

package lk.edu.nchs.traincollisionavoidancesystem;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.mapzen.speakerbox.Speakerbox;
import com.skyfishjy.library.RippleBackground;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Dulkith on 5/29/18.
 */

public class HomeActivity extends Fragment implements View.OnClickListener{

    private TextView voiceInput;
    private ImageButton start;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private RippleBackground rippleBackground;
    Speakerbox speakerbox;

    FirebaseDatabase database = FirebaseDatabase.getInstance();

    Vibrator vibrator;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    public void onClick(View view) {
        Fragment fragment = null;
        switch (view.getId()) {
            case R.id.btnSpeak2:
                fragment = new CameraFragment();
                replaceFragment(fragment);
                break;

            case R.id.btnSpeak:
                fragment = new LoctionFragment();
                replaceFragment(fragment);
                break;

            case R.id.btnSpeak3:
                fragment = new DoorHistoryFragment();
                replaceFragment(fragment);
                break;
        }
    }

    public void replaceFragment(Fragment someFragment) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout, someFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.home_layout, container, false);


        start = v.findViewById(R.id.btnSpeak);

        vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);

        speakerbox = new Speakerbox(getActivity().getApplication());
        // speakerbox.play("Welcome to happy lock.");

        //droidSpeech.startDroidSpeechRecognition();

        start.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                vibrator.vibrate(100);
            }
        });

//        tts = new TextToSpeech(getActivity().getApplicationContext(), new TextToSpeech.OnInitListener() {
//            @Override
//            public void onInit(int status) {
//                if (status != TextToSpeech.ERROR) {
//                    tts.setLanguage(Locale.UK);
//                }
//            }
//        });
        return v;
    }

    @Override
    public void onPause() {
        super.onPause();


    }

    @Override
    public void onDestroy() {
        // Don't forget to shutdown!
        super.onDestroy();



    }
/*
    // MARK: OnClickListener Method

    @Override
    public void onClick(View view) {
//        switch (view.getId())
//        {
//            case R.id.start:
//
//                // Starting droid speech
//               // droidSpeech.startDroidSpeechRecognition();
//
//                // Setting the view visibilities when droid speech is running
//                //start.setVisibility(View.GONE);
//                //stop.setVisibility(View.VISIBLE);
//
//                break;
//
//            case R.id.stop:
//
//                // Closing droid speech
//                droidSpeech.closeDroidSpeechOperations();
//
//                //stop.setVisibility(View.GONE);
//                start.setVisibility(View.VISIBLE);
//
//                break;
//        }
    }
*/


}


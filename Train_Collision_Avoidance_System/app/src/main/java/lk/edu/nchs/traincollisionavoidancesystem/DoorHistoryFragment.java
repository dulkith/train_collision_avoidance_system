package lk.edu.nchs.traincollisionavoidancesystem;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lk.edu.nchs.traincollisionavoidancesystem.m_Model.DoorStatusHistory;
import lk.edu.nchs.traincollisionavoidancesystem.m_UI.CustomAdapter;


public class DoorHistoryFragment extends Fragment {

    DatabaseReference db;
   // FirebaseHelper helper;
    CustomAdapter adapter;
    ListView lv;
   // EditText status, timestamp;


    public DoorHistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.door_history_layout, container, false);

        lv = (ListView) v.findViewById(R.id.lv);

        DatabaseReference database = FirebaseDatabase.getInstance().getReference();

        database.child("DOOR_ACTION_HISTORY").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<DoorStatusHistory> list = new ArrayList();
                for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
                    DoorStatusHistory doorStatusHistory = noteDataSnapshot.getValue(DoorStatusHistory.class);
                    list.add(doorStatusHistory);
                   // notes.add(note);
                }
                Collections.reverse(list);
                adapter = new CustomAdapter(getContext(), list);
        lv.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });

//        //ADAPTER


        return v;
    }

}

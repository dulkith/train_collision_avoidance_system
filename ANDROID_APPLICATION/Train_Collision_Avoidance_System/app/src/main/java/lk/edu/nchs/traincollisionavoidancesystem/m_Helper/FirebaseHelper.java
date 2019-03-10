package lk.edu.nchs.traincollisionavoidancesystem.m_Helper;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

import lk.edu.nchs.traincollisionavoidancesystem.m_Model.DoorStatusHistory;

/**
 * Created by Oclemy on 6/21/2016 for ProgrammingWizards Channel and http://www.camposha.com.
 * 1.SAVE DATA TO FIREBASE
 * 2. RETRIEVE
 * 3.RETURN AN ARRAYLIST
 */
public class FirebaseHelper {

    DatabaseReference db;
    Boolean saved;
    ArrayList<DoorStatusHistory> doorLogHistories = new ArrayList<>();

    /*
 PASS DATABASE REFRENCE
  */
    public FirebaseHelper(DatabaseReference db) {
        this.db = db;
    }
    //WRITE IF NOT NULL
    public Boolean save(DoorStatusHistory DOORACTIONHISTORY)
    {
        if(DOORACTIONHISTORY ==null)
        {
            saved=false;
        }else
        {
            try
            {
                db.child("DOOR_ACTION_HISTORY").push().setValue(DOORACTIONHISTORY);
                saved=true;

            }catch (DatabaseException e)
            {
                e.printStackTrace();
                saved=false;
            }
        }

        return saved;
    }

    //IMPLEMENT FETCH DATA AND FILL ARRAYLIST
    private void fetchData(DataSnapshot dataSnapshot)
    {
        doorLogHistories.clear();

        for (DataSnapshot ds : dataSnapshot.getChildren())
        {
            DoorStatusHistory DOORACTIONHISTORY = ds.getValue(DoorStatusHistory.class);
            System.out.println(DOORACTIONHISTORY.getStatus());
            doorLogHistories.add(DOORACTIONHISTORY);
        }
    }

    //RETRIEVE
    public ArrayList<DoorStatusHistory> retrieve()
    {
        db.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                fetchData(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                fetchData(dataSnapshot);

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        return doorLogHistories;
    }


}
package lk.edu.nchs.traincollisionavoidancesystem.m_UI;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import lk.edu.nchs.traincollisionavoidancesystem.R;
import lk.edu.nchs.traincollisionavoidancesystem.m_Model.DoorStatusHistory;

public class CustomAdapter extends BaseAdapter {
    Context c;
    List<DoorStatusHistory> doorLogHistories;

    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

    public CustomAdapter(Context c, List<DoorStatusHistory> doorLogHistories) {
        this.c = c;
        this.doorLogHistories = doorLogHistories;
    }

    @Override
    public int getCount() {
        return doorLogHistories.size();
    }

    @Override
    public Object getItem(int position) {
        return doorLogHistories.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(c).inflate(R.layout.model, parent, false);
        }

        TextView status = convertView.findViewById(R.id.status);
        TextView timestamp = convertView.findViewById(R.id.timestamp);
        TextView timeAgoTextView = convertView.findViewById(R.id.timeAgoText);

        final DoorStatusHistory s = (DoorStatusHistory) this.getItem(position);

//        if (s.getStatus() == "LOCKED")
//            status.setTextColor(Color.RED);
//        else
//            status.setTextColor(Color.GREEN);

        status.setText(s.getStatus());

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd / hh:mm:ss a");
        String dateString = formatter.format(new Date(Long.parseLong(String.valueOf(s.getTimestamp()))));
        timestamp.setText(dateString);

        //////////////////////////
        timeAgoTextView.setText(getTimeAgo(s.getTimestamp()));

        ///////////////////////////

        //ONITECLICK
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(c, s.getStatus(), Toast.LENGTH_SHORT).show();
            }
        });
        return convertView;
    }

    public static String getTimeAgo(long time) {
        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000;
        }

        long now = System.currentTimeMillis();
        if (time > now || time <= 0) {
            return null;
        }

        // TODO: localize
        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return "just now";
        } else if (diff < 2 * MINUTE_MILLIS) {
            return "a minute ago";
        } else if (diff < 50 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + " minutes ago";
        } else if (diff < 90 * MINUTE_MILLIS) {
            return "an hour ago";
        } else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS + " hours ago";
        } else if (diff < 48 * HOUR_MILLIS) {
            return "yesterday";
        } else {
            return diff / DAY_MILLIS + " days ago";
        }
    }
}
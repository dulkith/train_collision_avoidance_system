package lk.edu.nchs.traincollisionavoidancesystem;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import lk.edu.nchs.traincollisionavoidancesystem.m_Helper.map.DataParser;

public class LoctionFragmentXXX extends Fragment implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private GoogleMap mMap;
    ArrayList<LatLng> MarkerPoints;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker, doorLocationMarker;
    LocationRequest mLocationRequest;


    FirebaseDatabase database = FirebaseDatabase.getInstance();

    Button viewMyLiveLocation, viewMyLockLocation, setDirection;
    CheckBox autoLockUnlock;
    TextView distanceDisplay;

    double lockLatitude, lockLongitude, myLatitude, myLongitude;
    int doorStatus = 0;
    String[] distanceAndDuration = new String[2];
    boolean notDirectionButton = false, loadData = true, isNavigateOn = false;

    Vibrator vibrator;

    Handler handler = new Handler();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

// Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.location_layout, container, false);

        viewMyLiveLocation = v.findViewById(R.id.viewMyLocation);
        viewMyLockLocation = v.findViewById(R.id.viewLockLocation);
        setDirection = v.findViewById(R.id.setDirection);
        autoLockUnlock.setChecked(false);
        distanceDisplay = v.findViewById(R.id.distanceView);

        vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);

        viewMyLockLocation.setEnabled(false);
        viewMyLiveLocation.setEnabled(false);

        viewMyLiveLocation.setOnClickListener(viewMyLocation);
        viewMyLockLocation.setOnClickListener(viewLockLocation);
        autoLockUnlock.setOnClickListener(checkBoxMyAutoMove);
        setDirection.setOnClickListener(setDirectionInMap);

        handler.postDelayed(updateStatus, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }
        // Initializing
        MarkerPoints = new ArrayList<>();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
//        SupportMapFragment mapFragment = (SupportMapFragment) getActivity().getSupportFragmentManager()
//                .findFragmentById(R.id.map);
//        mapFragment.getMapAsync(this);


        MapView mapView = v.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);

        mapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        mapView.getMapAsync(this);

        return v;
    }

    View.OnClickListener viewMyLocation = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            vibrator.vibrate(60);
            LatLng myLocation = new LatLng(myLatitude, myLongitude);
            CameraUpdate location = CameraUpdateFactory.newLatLngZoom(myLocation, 16);
            mMap.animateCamera(location);
            if(isNavigateOn){
                isNavigateOn = false;
                Toast toast = Toast.makeText(getActivity(),"Navigation - DISABLE", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        }
    };

    View.OnClickListener viewLockLocation = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            vibrator.vibrate(60);
            LatLng lockLocation = new LatLng(lockLatitude, lockLongitude);
            CameraUpdate location = CameraUpdateFactory.newLatLngZoom(lockLocation, 16);
            mMap.animateCamera(location);
            if(isNavigateOn){
                isNavigateOn = false;
                Toast toast = Toast.makeText(getActivity(),"Navigation - DISABLE", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        }
    };

    View.OnClickListener checkBoxMyAutoMove = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            vibrator.vibrate(100);
//            if (checkBoxAutoMove.isChecked()) {
//                Toast.makeText(getActivity().getApplicationContext(), "My location live view - ENABLE", Toast.LENGTH_SHORT).show();
//            } else {
//                Toast.makeText(getActivity().getApplicationContext(), "My location live view - DISABLE", Toast.LENGTH_SHORT).show();
//            }
        }
    };

    View.OnClickListener setDirectionInMap = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            if (isNavigateOn) {
                isNavigateOn = false;
                Toast toast = Toast.makeText(getActivity(),"Navigation - DISABLE", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            } else {
                isNavigateOn = true;
                loadNavigationData();
                Toast toast = Toast.makeText(getActivity(),"Navigation - ENABLE", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }

        }
    };

    void loadNavigationData(){
        // Checks, whether start and end locations are captured
        // if (MarkerPoints.size() >= 2) {
        LatLng origin = new LatLng(myLatitude, myLongitude);
        LatLng destination = new LatLng(lockLatitude, lockLongitude);
        //  LatLng origin = MarkerPoints.get(0);
        // LatLng dest = MarkerPoints.get(1);

        // Getting URL to the Google Directions API
        String url = getUrl(origin, destination);
        Log.d("onMapClick", url.toString());
        FetchUrl FetchUrl = new FetchUrl();

        mMap.clear();

        // Start downloading json data from Google Directions API
        FetchUrl.execute(url);
        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(origin));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
        currentMapLocation();
        changeDoorStatus();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //Initialize Google Play Services
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();

                mMap.setMyLocationEnabled(false);

                mMap.setMapType(mMap.MAP_TYPE_NORMAL); // Here is where you set the map type

                try {
                    // Customise map styling via JSON file
                    boolean success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.style_map_json));

                    if (!success) {
                        // Log.e(TAG, "Style parsing failed.");
                    }
                } catch (Resources.NotFoundException e) {
                    // Log.e(TAG, "Can't find style. Error: ", e);
                }
                //

                mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

                    @Override
                    public View getInfoWindow(Marker arg0) {
                        return null;
                    }

                    @Override
                    public View getInfoContents(Marker marker) {

                        Context context = getActivity(); //or getActivity(), YourActivity.this, etc.

                        LinearLayout info = new LinearLayout(context);
                        info.setOrientation(LinearLayout.VERTICAL);

                        TextView title = new TextView(context);
                        title.setTextColor(Color.parseColor("#259E30"));
                        title.setGravity(Gravity.CENTER);
                        title.setTypeface(null, Typeface.BOLD);
                        title.setText(marker.getTitle());

                        TextView snippet = new TextView(context);
                        snippet.setTextColor(Color.GRAY);
                        snippet.setText(marker.getSnippet());

                        info.addView(title);
                        info.addView(snippet);

                        return info;
                    }
                });
            }
        } else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(false);
        }

        //When touch map disable live move.
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                //checkBoxAutoMove.setChecked(false);
            }
        });

        // Setting onclick event listener for the map
//        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
//
//            @Override
//            public void onMapClick(LatLng point) {
//
//                // Already two locations
//                if (MarkerPoints.size() > 1) {
//                    MarkerPoints.clear();
//                    mMap.clear();
//                }
//
//                // Adding new item to the ArrayList
//                MarkerPoints.add(point);
//
//                // Creating MarkerOptions
//                MarkerOptions options = new MarkerOptions();
//
//                // Setting the position of the marker
//                options.position(point);
//
//                /**
//                 * For the start location, the color of marker is GREEN and
//                 * for the end location, the color of marker is RED.
//                 */
//                if (MarkerPoints.size() == 1) {
//                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
//                } else if (MarkerPoints.size() == 2) {
//                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
//                }
//
//
//                // Add new marker to the Google Map Android API V2
//                mMap.addMarker(options);
//
//                // Checks, whether start and end locations are captured
//                if (MarkerPoints.size() >= 2) {
//                    LatLng origin = MarkerPoints.get(0);
//                    LatLng dest = MarkerPoints.get(1);
//
//                    // Getting URL to the Google Directions API
//                    String url = getUrl(origin, dest);
//                    Log.d("onMapClick", url.toString());
//                    FetchUrl FetchUrl = new FetchUrl();
//
//                    // Start downloading json data from Google Directions API
//                    FetchUrl.execute(url);
//                    //move map camera
//                    mMap.moveCamera(CameraUpdateFactory.newLatLng(origin));
//                    mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
//                }
//
//            }
//        });

    }

    private String getUrl(LatLng origin, LatLng dest) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Sensor enabled
        String sensor = "sensor=true";
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&units=metric&mode=" + "walking" + "&alternatives=true" + "&key=AIzaSyBc36qu2_1pIQRjmCHvj6vDLnsCV_1uS5g";
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
        return url;
    }

    /**
     * A method to download json data from url
     */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();
            Log.d("downloadUrl", data.toString());
            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    // Fetches data from url passed
    private class FetchUrl extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
                Log.d("Background Task data", data.toString());
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);

        }
    }

    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;
            try {
                jObject = new JSONObject(jsonData[0]);
                Log.d("ParserTask", jsonData[0].toString());
                DataParser parser = new DataParser();
                Log.d("ParserTask", parser.toString());

                // Starts parsing data
                routes = parser.parse(jObject);
                Log.d("ParserTask", "Executing routes");
                Log.d("ParserTask", routes.toString());

                distanceAndDuration = parser.getDistanceAndDuration(jObject);

            } catch (Exception e) {
                Log.d("ParserTask", e.toString());
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(14);
                lineOptions.color(Color.parseColor("#4a80f5"));

                distanceDisplay.setText(distanceAndDuration[0].toUpperCase());

                Log.d("onPostExecute", "onPostExecute lineoptions decoded");

            }

            // Drawing polyline in the Google Map for the i-th route
            if (lineOptions != null) {
                mMap.addPolyline(lineOptions);
            } else {
                Log.d("onPostExecute", "without Polylines drawn");
            }
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {

        myLatitude = location.getLatitude();
        myLongitude = location.getLongitude();

        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        currentMapLocation();

        if (loadData == true) {
            loadNavigationData();
            loadData = false;
        }

        //this code stops location updates
//        if (mGoogleApiClient != null) {
//            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
//        }
    }

    private void currentMapLocation() {
        //Showing Current Location Marker on Map
        LatLng latLng = new LatLng(myLatitude, myLongitude);
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);

        LocationManager locationManager = null;
        String provider = null;
        if (getActivity() != null) {

            locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            provider = locationManager.getBestProvider(new Criteria(), true);

            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }

        }
        try {
            Location locations = locationManager.getLastKnownLocation(provider);
            List<String> providerList = locationManager.getAllProviders();
            if (null != locations && null != providerList && providerList.size() > 0) {
                double longitude = locations.getLongitude();
                double latitude = locations.getLatitude();
                Geocoder geocoder = new Geocoder(getContext().getApplicationContext(), Locale.getDefault());

                List<Address> listAddresses = geocoder.getFromLocation(latitude, longitude, 1);
                if (null != listAddresses && listAddresses.size() > 0) {

//                    Here we are finding , whatever we want our marker to show when clicked
                    String state = listAddresses.get(0).getAdminArea();
                    String country = listAddresses.get(0).getCountryName();
                    String subLocality = listAddresses.get(0).getSubLocality();
                    markerOptions.title("i'm here");
                    markerOptions.snippet("" + subLocality + "," + state + "," + country);
                }
            }
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }

        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_you));
        mCurrLocationMarker = mMap.addMarker(markerOptions);

        if (isNavigateOn) {
            //move map camera
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }


    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted. Do the
                    // contacts-related task you need to do.
                    if (ContextCompat.checkSelfPermission(getContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }

                } else {

                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(getContext(), "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other permissions this app might request.
            // You can add here other case statements according to your requirement.
        }
    }


    //server connecting.

    private Runnable updateStatus = new Runnable() {
        @Override
        public void run() {
            try {
                setDoorLocation();
                if (isNavigateOn)
                    loadNavigationData();
            } catch (Exception e) {
            }
            handler.postDelayed(this, 10000);
        }
    };

    void setDoorLocation() {
        DatabaseReference myRef1 = database.getReference("DOOR_LOCATION_LATITUDE");
        // Read from the database
        myRef1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                lockLatitude = dataSnapshot.getValue(Double.class);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                // Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        DatabaseReference myRef2 = database.getReference("DOOR_LOCATION_LONGITUDE");
        // Read from the database
        myRef2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                lockLongitude = dataSnapshot.getValue(Double.class);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                // Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
        //distanceDisplay.setText(getDistanceInfo(myLatitude,myLongitude,lockLatitude,lockLongitude));
        // distanceDisplay.setText(Double.toString(CalculationByDistance(myLatitude, myLongitude, lockLatitude, lockLongitude)) + " KM ");
        notDirectionButton = true;
        viewMyLiveLocation.setEnabled(true);

        changeDoorStatus();
    }

    private double CalculationByDistance(double myLatitude, double myLongitude, double lockLatitude, double lockLongitude) {
        Location loc1 = new Location("");
        loc1.setLatitude(myLatitude);
        loc1.setLongitude(myLongitude);

        Location loc2 = new Location("");
        loc2.setLatitude(lockLatitude);
        loc2.setLongitude(lockLongitude);

        DecimalFormat newFormat = new DecimalFormat("##");
        return Double.valueOf(newFormat.format(loc1.distanceTo(loc2))) / 1000;

    }

    void changeDoorStatus() {
        DatabaseReference myRef = database.getReference("DOOR_STATUS");
        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                doorStatus = dataSnapshot.getValue(Integer.class);

                int doorStatusIcon;
                String doorStatusInfo;
                if (doorStatus == 1) {
                    doorStatusIcon = R.drawable.ic_lock_map;
                    doorStatusInfo = "Now LOCKED";
                } else {
                    doorStatusIcon = R.drawable.ic_unlock_map;
                    doorStatusInfo = "Now UNLOCKED";
                }

                if (doorLocationMarker != null) {
                    doorLocationMarker.remove();
                }
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(new LatLng(lockLatitude, lockLongitude));
                markerOptions.title("My HAPPY LOCK");
                markerOptions.snippet(doorStatusInfo);

                markerOptions.icon(BitmapDescriptorFactory.fromResource(doorStatusIcon));
                doorLocationMarker = mMap.addMarker(markerOptions);

                mMap.getUiSettings().setScrollGesturesEnabled(true);
                viewMyLockLocation.setEnabled(true);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                // Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }


}

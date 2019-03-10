package lk.edu.nchs.traincollisionavoidancesystem;

import android.Manifest;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
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
import com.google.android.gms.maps.model.PolygonOptions;
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

import lk.edu.nchs.traincollisionavoidancesystem.m_Helper.map.AppConstants;
import lk.edu.nchs.traincollisionavoidancesystem.m_Helper.map.DataParser;


public class LoctionFragment extends Fragment implements OnMapReadyCallback,
        GoogleMap.OnInfoWindowClickListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private static final int REQUEST_LOCATION = 0;
    private Location mLastLocation;
    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;
    // boolean flag to toggle periodic location updates
    private boolean mRequestingLocationUpdates = false;
    private LocationRequest mLocationRequest;
    private static final String TAG = "";
    private GoogleMap mMap;
    private int markerCount;

    private String test;

    ArrayList<LatLng> MarkerPoints;
    Marker mCurrLocationMarker, n1L, n1R, n2L, n2R;
    FirebaseDatabase database = FirebaseDatabase.getInstance();

    Button viewMyLiveLocation, viewMyLockLocation, setDirection;
    TextView distanceDisplay;

    double node1LeftLatitude, node1rightLatitude, node2LeftLatitude, node2rightLatitude,
            node1LeftLongitude, node1rightLongitude, node2LeftLongitude, node2rightLongitude,
            myLatitude, myLongitude;
    int node1LeftStatus = 0, node1RightStatus = 0, node1MotionStatus = 0;
    int node2LeftStatus = 0, node2RightStatus = 0, node2MotionStatus = 0;
    String[] distanceAndDuration = new String[2];
    boolean notDirectionButton = false, loadData = true, isNavigateOn = false;

    Vibrator vibrator;
    Handler handler = new Handler();
    int mode = 0;
    int lastAction = -1;
    double distance;
    Ringtone r;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
         r = RingtoneManager.getRingtone(getActivity().getApplicationContext(), notification);

// Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.location_layout, container, false);

        viewMyLiveLocation = v.findViewById(R.id.viewMyLocation);
        viewMyLockLocation = v.findViewById(R.id.viewLockLocation);
        setDirection = v.findViewById(R.id.setDirection);
        distanceDisplay = v.findViewById(R.id.distanceView);

        vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);

        viewMyLockLocation.setEnabled(false);
        viewMyLiveLocation.setEnabled(false);

        viewMyLiveLocation.setOnClickListener(viewMyLocation);
        viewMyLockLocation.setOnClickListener(viewLockLocation);
        setDirection.setOnClickListener(setDirectionInMap);

        handler.postDelayed(updateStatus, 0);


        markerCount = 0;

        //Check If Google Services Is Available
        if (getServicesAvailable()) {
            // Building the GoogleApi client
            buildGoogleApiClient();
            createLocationRequest();
            //Toast.makeText(getActivity(), "Google Service Is Available!!", Toast.LENGTH_SHORT).show();
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
            if (isNavigateOn) {
                isNavigateOn = false;
                Toast toast = Toast.makeText(getActivity(), "Navigation - DISABLE", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 200);
                toast.show();
            }
            mMap.clear();
            markerCount = 0;
            addMarker(mMap, myLatitude, myLongitude);
        }
    };

    View.OnClickListener viewLockLocation = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            vibrator.vibrate(60);
            LatLng lockLocation = new LatLng(node1rightLatitude, node2LeftLongitude);
            CameraUpdate location = CameraUpdateFactory.newLatLngZoom(lockLocation, 16);
            mMap.animateCamera(location);
            if (isNavigateOn) {
                isNavigateOn = false;
                Toast toast = Toast.makeText(getActivity(), "Navigation - DISABLE", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 200);
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
            vibrator.vibrate(80);
            if (isNavigateOn) {
                isNavigateOn = false;
                LatLng myLocation = new LatLng(myLatitude, myLongitude);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 16));
                Toast toast = Toast.makeText(getActivity(), "Navigation - DISABLE", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 200);
                toast.show();
            } else {
                isNavigateOn = true;
                loadNavigationData();
                Toast toast = Toast.makeText(getActivity(), "Navigation - ENABLE", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 200);
                toast.show();
            }

        }
    };

    void loadNavigationData() {
        // Checks, whether start and end locations are captured
        // if (MarkerPoints.size() >= 2) {
        LatLng origin = new LatLng(myLatitude, myLongitude);
        LatLng destination = new LatLng(node1rightLatitude, node2LeftLongitude);
        //  LatLng origin = MarkerPoints.get(0);
        // LatLng dest = MarkerPoints.get(1);

        // Getting URL to the Google Directions API
        String url = getUrl(origin, destination);
        Log.d("onMapClick", url.toString());
        FetchUrl FetchUrl = new FetchUrl();

        mMap.clear();
        markerCount = 0;
        addMarker(mMap, myLatitude, myLongitude);
        // Start downloading json data from Google Directions API
        FetchUrl.execute(url);
        //move map camera
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(origin, 17));
        //currentMapLocation();
        changen1LStatus();
        changen1RStatus();
        changen2LStatus();
        changen2RStatus();
    }

    /**
     * GOOGLE MAPS AND MAPS OBJECTS
     */

    // After Creating the Map Set Initial Location
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

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
        //Uncomment To Show Google Location Blue Pointer
        // mMap.setMyLocationEnabled(true);
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

                //distanceDisplay.setText(distanceAndDuration[0].toUpperCase());

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

    Marker mk = null;

    // Add A Map Pointer To The MAp
    public void addMarker(GoogleMap googleMap, double lat, double lon) {

        if (markerCount == 1) {
            animateMarker(mLastLocation, mk);
        } else if (markerCount == 0) {
            //Set Custom BitMap for Pointer
            int height = 80;
            int width = 70;

            LatLng latlong = new LatLng(lat, lon);


            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latlong);
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


            BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.ic_you_train);
            Bitmap b = bitmapdraw.getBitmap();
            Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
            mMap = googleMap;

            markerOptions.icon(BitmapDescriptorFactory.fromBitmap((smallMarker)));
            mk = mMap.addMarker(markerOptions);
            //.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin3))
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlong, 16));

            //Set Marker Count to 1 after first marker is created
            markerCount = 1;

            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                return;
            }
            //mMap.setMyLocationEnabled(true);
            startLocationUpdates();
        }
    }


    @Override
    public void onInfoWindowClick(Marker marker) {
        Toast.makeText(getActivity(), marker.getTitle(), Toast.LENGTH_LONG).show();
    }


    public boolean getServicesAvailable() {
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int isAvailable = api.isGooglePlayServicesAvailable(getActivity());
        if (isAvailable == ConnectionResult.SUCCESS) {
            return true;
        } else if (api.isUserResolvableError(isAvailable)) {

            Dialog dialog = api.getErrorDialog(getActivity(), isAvailable, 0);
            dialog.show();
        } else {
            Toast.makeText(getActivity(), "Cannot Connect To Play Services", Toast.LENGTH_SHORT).show();
        }
        return false;
    }


    /**
     * LOCATION LISTENER EVENTS
     */

    @Override
    public void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
//        startLocationUpdates();
    }

    @Override
    public void onResume() {
        super.onResume();

        getServicesAvailable();

        // Resuming the periodic location updates
        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    //Method to display the location on UI
    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            // Check Permissions Now
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        } else {


            mLastLocation = LocationServices.FusedLocationApi
                    .getLastLocation(mGoogleApiClient);

            if (mLastLocation != null) {
                double latitude = mLastLocation.getLatitude();
                double longitude = mLastLocation.getLongitude();
                String loc = "" + latitude + " ," + longitude + " ";
                // Toast.makeText(getActivity(),loc, Toast.LENGTH_SHORT).show();


                if (isNavigateOn) {
                    // mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(latitude, longitude)));
                    //  mMap.animateCamera(CameraUpdateFactory.zoomTo(16));

                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 17));
                }
//Add pointer to the map at location
                addMarker(mMap, latitude, longitude);
            } else {

                Toast.makeText(getActivity(), "Couldn't get the location. Make sure location is enabled on the device",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }


    // Creating google api client object
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    //Creating location request object
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(AppConstants.UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(AppConstants.FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(AppConstants.DISPLACEMENT);
    }


    //Starting the location updates
    protected void startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Check Permissions Now
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        } else {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        }
    }

    //Stopping location updates
    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    /**
     * Google api callback methods
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }

    @Override
    public void onConnected(Bundle arg0) {

        // Once connected with google api, get the location
        displayLocation();

        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }


    public void onLocationChanged(Location location) {
        myLatitude = location.getLatitude();
        myLongitude = location.getLongitude();

        // Assign the new location
        mLastLocation = location;

        // Toast.makeText(getContext(), "Location changed!", Toast.LENGTH_SHORT).show();

        // Displaying the new location on UI
        displayLocation();
    }


    public static void animateMarker(final Location destination, final Marker marker) {
        if (marker != null) {
            final LatLng startPosition = marker.getPosition();
            final LatLng endPosition = new LatLng(destination.getLatitude(), destination.getLongitude());

            final float startRotation = marker.getRotation();

            final LatLngInterpolator latLngInterpolator = new LatLngInterpolator.LinearFixed();
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
            valueAnimator.setDuration(1000); // duration 1 second
            valueAnimator.setInterpolator(new LinearInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    try {
                        float v = animation.getAnimatedFraction();
                        LatLng newPosition = latLngInterpolator.interpolate(v, startPosition, endPosition);
                        marker.setPosition(newPosition);
                        // marker.setRotation(computeRotation(v, startRotation, destination.getBearing()));


                    } catch (Exception ex) {
                        // I don't care atm..
                    }
                }
            });

            valueAnimator.start();
        }
    }

    private static float computeRotation(float fraction, float start, float end) {
        float normalizeEnd = end - start; // rotate start to 0
        float normalizedEndAbs = (normalizeEnd + 360) % 360;

        float direction = (normalizedEndAbs > 180) ? -1 : 1; // -1 = anticlockwise, 1 = clockwise
        float rotation;
        if (direction > 0) {
            rotation = normalizedEndAbs;
        } else {
            rotation = normalizedEndAbs - 360;
        }

        float result = fraction * rotation + start;
        return (result + 360) % 360;
    }

    private interface LatLngInterpolator {
        LatLng interpolate(float fraction, LatLng a, LatLng b);

        class LinearFixed implements LatLngInterpolator {
            @Override
            public LatLng interpolate(float fraction, LatLng a, LatLng b) {
                double lat = (b.latitude - a.latitude) * fraction + a.latitude;
                double lngDelta = b.longitude - a.longitude;
                // Take the shortest path across the 180th meridian.
                if (Math.abs(lngDelta) > 180) {
                    lngDelta -= Math.signum(lngDelta) * 360;
                }
                double lng = lngDelta * fraction + a.longitude;
                return new LatLng(lat, lng);
            }
        }
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
                //mMap.clear();
                n1L();
                n1R();
                n2L();
                n2R();
                changeMotionStatus();
                //myLocationDis();
                //getMode();
                //test();
            } catch (Exception e) {
            }
            handler.postDelayed(this, 4000);
        }
    };

    private void myLocationDis() {
        LatLng myLocation = new LatLng(myLatitude, myLongitude);
       // CameraUpdate location = CameraUpdateFactory.newLatLngZoom(myLocation, 16);
       // mMap.animateCamera(location);
        if (isNavigateOn) {
            isNavigateOn = false;
            //Toast toast = Toast.makeText(getActivity(), "Navigation - DISABLE", Toast.LENGTH_LONG);
            //toast.setGravity(Gravity.CENTER, 0, 200);
            //toast.show();
        }
        mMap.clear();
        markerCount = 0;
        addMarker(mMap, myLatitude, myLongitude);
    }

    void test() {

    }





    void n1L() {
        DatabaseReference myRef1 = database.getReference("EDGES/NODE_1/LOCATION_LATITUDE_LEFT");
        // Read from the database
        myRef1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                node1LeftLatitude = dataSnapshot.getValue(Double.class);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        DatabaseReference myRef2 = database.getReference("EDGES/NODE_1/LOCATION_LONGITUDE_LEFT");
        // Read from the database
        myRef2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                node1LeftLongitude = dataSnapshot.getValue(Double.class);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
        //distanceDisplay.setText(getDistanceInfo(myLatitude,myLongitude,lockLatitude,lockLongitude));
        distance = CalculationByDistance(myLatitude, myLongitude, node1LeftLatitude, node1LeftLongitude);
        distanceDisplay.setText(Double.toString(distance) + " KM ");
        //autoLockUnlock(distance);
        notDirectionButton = true;
        viewMyLiveLocation.setEnabled(true);

        changen1LStatus();
    }

    void n1R() {
        DatabaseReference myRef1 = database.getReference("EDGES/NODE_1/LOCATION_LATITUDE_RIGHT");
        // Read from the database
        myRef1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                node1rightLatitude = dataSnapshot.getValue(Double.class);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        DatabaseReference myRef2 = database.getReference("EDGES/NODE_1/LOCATION_LONGITUDE_RIGHT");
        // Read from the database
        myRef2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                node1rightLongitude = dataSnapshot.getValue(Double.class);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }

        });

        changen1RStatus();
    }

    void n2L() {
        DatabaseReference myRef1 = database.getReference("EDGES/NODE_2/LOCATION_LATITUDE_LEFT");
        // Read from the database
        myRef1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                node2LeftLatitude = dataSnapshot.getValue(Double.class);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        DatabaseReference myRef2 = database.getReference("EDGES/NODE_2/LOCATION_LONGITUDE_LEFT");
        // Read from the database
        myRef2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                node2LeftLongitude = dataSnapshot.getValue(Double.class);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        changen2LStatus();
    }

    void n2R() {
        DatabaseReference myRef1 = database.getReference("EDGES/NODE_2/LOCATION_LATITUDE_RIGHT");
        // Read from the database
        myRef1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                node2rightLatitude = dataSnapshot.getValue(Double.class);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        DatabaseReference myRef2 = database.getReference("EDGES/NODE_2/LOCATION_LONGITUDE_RIGHT");
        // Read from the database
        myRef2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                node2rightLongitude = dataSnapshot.getValue(Double.class);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        changen2RStatus();
    }
    /*
        private void autoLockUnlock(double distance) {
            if (mode == 2 || mode == 3) {
                if (distance <= 0.020) {
                    DatabaseReference myRef = database.getReference("DOOR_STATUS");
                    myRef.setValue(0);
                    // lastAction = 0;
                    //Toast.makeText(getActivity().getApplicationContext(), "Door Auto UNLOCKED", Toast.LENGTH_SHORT).show();
                } else if (distance >= 0.020) {
                    DatabaseReference myRef = database.getReference("DOOR_STATUS");
                    myRef.setValue(1);
                    //lastAction = 1;
                    // Toast.makeText(getActivity().getApplicationContext(), "Door Auto LOCKED", Toast.LENGTH_SHORT).show();

                }
            }
        }
    */
    private double CalculationByDistance(double myLatitude, double myLongitude, double lockLatitude, double lockLongitude) {
        Location loc1 = new Location("");
        loc1.setLatitude(myLatitude);
        loc1.setLongitude(myLongitude);

        Location loc2 = new Location("");
        loc2.setLatitude(lockLatitude);
        loc2.setLongitude(lockLongitude);

        DecimalFormat newFormat = new DecimalFormat("#");
        return Double.valueOf(newFormat.format(loc1.distanceTo(loc2))) / 1000;

    }

    void changen1LStatus() {
        DatabaseReference myRef = database.getReference("EDGES/NODE_1/LASER_SENSOR_LEFT");
        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                 node1LeftStatus = dataSnapshot.getValue(Integer.class);

                int doorStatusIcon;
                String doorStatusInfo;
                if (node1LeftStatus == 1) {
                    doorStatusIcon = R.drawable.ic_node_inactive;
                    doorStatusInfo = "Activate";
                } else {
                    doorStatusIcon = R.drawable.ic_node_active;
                    doorStatusInfo = "In-Activate";
                }

                if (n1L != null) {
                    n1L.remove();
                }
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(new LatLng(node1LeftLatitude, node1LeftLongitude));
                markerOptions.title("NODE 01 - LEFT");
                markerOptions.snippet(doorStatusInfo);

                markerOptions.icon(BitmapDescriptorFactory.fromResource(doorStatusIcon));
                n1L = mMap.addMarker(markerOptions);

                mMap.getUiSettings().setScrollGesturesEnabled(true);
                viewMyLockLocation.setEnabled(true);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    void changen1RStatus() {
        DatabaseReference myRef = database.getReference("EDGES/NODE_1/LASER_SENSOR_RIGHT");
        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                node2RightStatus = dataSnapshot.getValue(Integer.class);

                int doorStatusIcon;
                String doorStatusInfo;
                if (node2RightStatus == 1) {
                    doorStatusIcon = R.drawable.ic_node_inactive;
                    doorStatusInfo = "Activate";
                } else {
                    doorStatusIcon = R.drawable.ic_node_active;
                    doorStatusInfo = "In-Activate";
                }

                if (n1R != null) {
                    n1R.remove();
                }
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(new LatLng(node1rightLatitude, node1rightLongitude));
                markerOptions.title("NODE 01 - RIGHT");
                markerOptions.snippet(doorStatusInfo);

                markerOptions.icon(BitmapDescriptorFactory.fromResource(doorStatusIcon));
                n1R = mMap.addMarker(markerOptions);

                mMap.getUiSettings().setScrollGesturesEnabled(true);
                viewMyLockLocation.setEnabled(true);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    void changen2LStatus() {
        DatabaseReference myRef = database.getReference("EDGES/NODE_1/LASER_SENSOR_LEFT");
        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                node1LeftStatus = dataSnapshot.getValue(Integer.class);

                int doorStatusIcon;
                String doorStatusInfo;
                if (node1LeftStatus == 1) {
                    doorStatusIcon = R.drawable.ic_node_inactive;
                    doorStatusInfo = "Activate";
                } else {
                    doorStatusIcon = R.drawable.ic_node_active;
                    doorStatusInfo = "In-Activate";
                }

                if (n2L != null) {
                    n2L.remove();
                }
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(new LatLng(node2LeftLatitude, node2LeftLongitude));
                markerOptions.title("NODE 02 - LEFT");
                markerOptions.snippet(doorStatusInfo);

                markerOptions.icon(BitmapDescriptorFactory.fromResource(doorStatusIcon));
                n2L = mMap.addMarker(markerOptions);

                mMap.getUiSettings().setScrollGesturesEnabled(true);
                viewMyLockLocation.setEnabled(true);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    void changen2RStatus() {
        DatabaseReference myRef = database.getReference("EDGES/NODE_1/LASER_SENSOR_RIGHT");
        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                node1LeftStatus = dataSnapshot.getValue(Integer.class);

                int doorStatusIcon;
                String doorStatusInfo;
                if (node1LeftStatus == 1) {
                    doorStatusIcon = R.drawable.ic_node_inactive;
                    doorStatusInfo = "Activate";
                } else {
                    doorStatusIcon = R.drawable.ic_node_active;
                    doorStatusInfo = "In-Activate";
                }

                if (n2R != null) {
                    n2R.remove();
                }
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(new LatLng(node2rightLatitude, node2rightLongitude));
                markerOptions.title("NODE 02 - RIGHT");
                markerOptions.snippet(doorStatusInfo);

                markerOptions.icon(BitmapDescriptorFactory.fromResource(doorStatusIcon));
                n2R = mMap.addMarker(markerOptions);

                mMap.getUiSettings().setScrollGesturesEnabled(true);
                viewMyLockLocation.setEnabled(true);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }




    ////////
    void changeMotionStatus() {
        DatabaseReference myRef = database.getReference("EDGES/NODE_1/MOTION_SENSOR");
        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                node1MotionStatus = dataSnapshot.getValue(Integer.class);

                if (node1MotionStatus == 1) {

                    r.play();

                    PolylineOptions options1 = new PolylineOptions().width(5).color(Color.RED).geodesic(true);

                    LatLng n1L = new LatLng(node1LeftLatitude,node1LeftLongitude);
                    LatLng n1R = new LatLng(node1rightLatitude,node1rightLongitude);
                    LatLng n2L = new LatLng(node2LeftLatitude,node2LeftLongitude);
                    LatLng n2R = new LatLng(node2rightLatitude,node2rightLongitude);

                    options1.add(new LatLng[] {
                            n1L,
                            n2L
                    });

                    mMap.addPolyline(options1);

                    PolylineOptions options2 = new PolylineOptions().width(5).color(Color.RED).geodesic(true);

                    options2.add(new LatLng[] {
                            n1R,
                            n2R
                    });

                    mMap.addPolyline(options2);

                    vibrator.vibrate(200);

                    options1 = null;
                    options2 = null;


                } else {

                    PolylineOptions options1 = new PolylineOptions().width(5).color(Color.BLACK).geodesic(true);

                    LatLng n1L = new LatLng(node1LeftLatitude,node1LeftLongitude);
                    LatLng n1R = new LatLng(node1rightLatitude,node1rightLongitude);
                    LatLng n2L = new LatLng(node2LeftLatitude,node2LeftLongitude);
                    LatLng n2R = new LatLng(node2rightLatitude,node2rightLongitude);

                    options1.add(new LatLng[] {
                            n1L,
                            n2L
                    });

                    mMap.addPolyline(options1);

                    PolylineOptions options2 = new PolylineOptions().width(5).color(Color.BLACK).geodesic(true);

                    options2.add(new LatLng[] {
                            n1R,
                            n2R
                    });

                    mMap.addPolyline(options2);

                    options1 = null;
                    options2 = null;


                }


            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(updateStatus);
        super.onDestroy();
    }

}


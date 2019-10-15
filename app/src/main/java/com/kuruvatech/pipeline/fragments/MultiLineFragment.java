package com.kuruvatech.pipeline.fragments;
import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.kuruvatech.pipeline.MainActivity;
import com.kuruvatech.pipeline.R;
import com.kuruvatech.pipeline.model.Coordinate;
import com.kuruvatech.pipeline.model.LineInfo;
import com.kuruvatech.pipeline.model.location;
import com.kuruvatech.pipeline.utils.Constants;
import com.kuruvatech.pipeline.utils.GPSTracker;
import com.kuruvatech.pipeline.utils.PermissionUtils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import android.net.ParseException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import static android.content.ContentValues.TAG;

/**
 * Created by dayas on 05-08-2019.
 */





public class MultiLineFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMapClickListener,
        AdapterView.OnItemSelectedListener{



    View rootview;

    /**
     * Request code for location permission request.
     *
     * @see #onRequestPermissionsResult(int, String[], int[])
     */
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int LREQUEST_CODE_ASK_PERMISSIONS = 123;
    // City locations for mutable polyline.

    private static final LatLng KURUVA = new LatLng(14.142235317478407, 75.66676855087282);


    // Airport locations for geodesic polyline.

    private long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */


    private static final String LOCATIONS = "Locations";
    private static final String LATITUDE = "Lat";
    private static final String LONGITUDE = "Logt";
    private static final int PATTERN_DASH_LENGTH_PX = 50;
    private static final int PATTERN_GAP_LENGTH_PX = 20;
    private static final Dot DOT = new Dot();
    private static final Dash DASH = new Dash(PATTERN_DASH_LENGTH_PX);
    private static final Gap GAP = new Gap(PATTERN_GAP_LENGTH_PX);
    private static final List<PatternItem> PATTERN_DOTTED = Arrays.asList(DOT, GAP);
    private static final List<PatternItem> PATTERN_DASHED = Arrays.asList(DASH, GAP);
    private static final List<PatternItem> PATTERN_MIXED = Arrays.asList(DOT, GAP, DOT, DASH, GAP);
    float mLinewidth =(float)5.0;
    private Polyline mMutablePolyline;
    private PolylineOptions mPolylineOptions ;

    private CheckBox mClickabilityCheckbox;
    SupportMapFragment mFragment;
    FragmentManager fragmentManager;
    private GoogleApiClient mGoogleApiClient;
    protected Location mLastLocation;
    Marker mCurrLocationMarker;
    private LatLng mSelectedLatlang;
    Location lastLocationloc=null;
    int zoomleval = 15;
    LocationManager locationManager =  null;
    // These are the options for polyline caps, joints and patterns. We use their
    // string resource IDs as identifiers.
    private GoogleMap mMap;
    private GPSTracker gps;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationcallback;
    private int locationRequestCode = 1000;
    private double wayLatitude = 0.0, wayLongitude = 0.0;
    JSONArray mJsonArray = new JSONArray();
    boolean mIsStartPipeLine =false;
    private ToggleButton togglePlayButton, togglePauseButton;
    FirebaseFirestore mDb;
    FirebaseStorage mStorage ;
    Gson gson ;
    ArrayList<LineInfo> lineInfoList ;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       // MobileAds.initialize(getActivity(), Constants.ADMOBAPPID);
        rootview = inflater.inflate(R.layout.fragment_multiline, container, false);
        fragmentManager=getChildFragmentManager();
        gson = new Gson();


        mDb = FirebaseFirestore.getInstance();
 //Get a non-default Storage bucket

        mClickabilityCheckbox = (CheckBox) rootview.findViewById(R.id.toggleClickability);
        togglePlayButton = (ToggleButton) rootview.findViewById(R.id.togglebutton);
        togglePauseButton = (ToggleButton) rootview.findViewById(R.id.togglebutton2);
//        togglePlayButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//               // openfileFromFirebaseStorage();
//
//
//            }
//        });
   //     getPipelineWithinCoordinates();
//        openlinesfromfirestorage();
//        togglePauseButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                startLocationUpdates(isChecked);
//                //togglePauseButton.setVisibility(.INVISIBLE);
//
//            }
//        });


        mFragment = (SupportMapFragment)fragmentManager.findFragmentById(R.id.map);
        mFragment.getMapAsync(this);
        return rootview;
    }


    private String[] getResourceStrings(int[] resourceIds) {
        String[] strings = new String[resourceIds.length];
        for (int i = 0; i < resourceIds.length; i++) {
            strings[i] = getString(resourceIds[i]);
        }
        return strings; //08202923069
    }
    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(getContext(), "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        getCurrentLocation();
        return false;
    }

    //and then register for location
    @Override
    public void onMapReady(GoogleMap map) {

        mIsStartPipeLine =false;
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission((AppCompatActivity) getActivity(), LREQUEST_CODE_ASK_PERMISSIONS,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE, true);

        }
        //  savetofile();
        mMap =  map;
        mLocationcallback = new LocationCallback() {

            @Override
            public void onLocationResult(LocationResult locationResult) {

                // do work here
             //   onLocationChanged(locationResult.getLastLocation());
            }
        };
        // Override the default conte
        // nt description on the view, for accessibility mode.
        map.setContentDescription(getString(R.string.polyline_demo_description));


//        int color = Color.HSVToColor(
//                mAlphaBar.getProgress(), new float[]{mHueBar.getProgress(), 1, 1});
        mPolylineOptions = new PolylineOptions()
                .color(Color.RED)
                .width(mLinewidth)
                .clickable(mClickabilityCheckbox.isChecked());
        // .add(KURUVA, KURUVA2, KURUVA3, KURUVA4);


        mMutablePolyline = map.addPolyline(mPolylineOptions);
        mMutablePolyline.setWidth(mLinewidth);
        mMutablePolyline.setPattern(PATTERN_MIXED);

        // Move the map so that it is centered on the mutable polyline.
        // map.moveCamera(CameraUpdateFactory.newLatLngZoom(MELBOURNE, 5));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(KURUVA, 18));
        // map.setMyLocationEnabled(true);
        // Add a listener for polyline clicks that changes the clicked polyline's color.
        map.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
            @Override
            public void onPolylineClick(Polyline polyline) {
                // Flip the values of the red, green and blue components of the polyline's color.
                polyline.setColor(polyline.getColor() ^ 0x00ffffff);
            }
        });
        //openlinesfromfirestorage();
//        map.setLatLngBoundsForCameraTarget();

        enableMyLocation();
        map.setOnMyLocationButtonClickListener(this);
        map.setOnMapClickListener(this);

        VisibleRegion visibleRegion = map.getProjection().getVisibleRegion();
        LatLngBounds latLngBounds = visibleRegion.latLngBounds;
        getPipelineWithinCoordinates(latLngBounds);

    }

    private void enableMyLocation() {
        //  Toast.makeText(getCon"enableMyLocation ", Toast.LENGTH_SHORT).show();

        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission((AppCompatActivity) getActivity(), LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
            buildGoogleApiClient();

            //  Toast.makeText(getContext(), "enableMyLocation startLocationUpdates ", Toast.LENGTH_SHORT).show();

        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Don't do anything here.
    }



    /**
     * Toggles the clickability of the polyline based on the state of the View that triggered this
     * call.
     * This callback is defined on the CheckBox in the layout for this Activity.
     */
    public void toggleClickability(View view) {
        if (mMutablePolyline != null) {
            mMutablePolyline.setClickable(((CheckBox) view).isChecked());
        }
    }

    public void getCurrentLocation()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            }
        }
        if(mLastLocation!=null) {
            //Toast.makeText(getContext(), "MyLocation button clicked 2", Toast.LENGTH_SHORT).show();
            setPosition(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
        }
        else
        {
            // Toast.makeText(getContext(), "MyLocation button clicked 3", Toast.LENGTH_SHORT).show();
        }


    }
    private void setPosition(LatLng latLng)
    {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }
        mCurrLocationMarker = mMap.addMarker(markerOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(zoomleval));
        mSelectedLatlang = latLng;
    }
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();//kasturbainsurance@gmail.com
    }
    public void openlinesfromfirestorage()
    {
        mDb.collection("pipeline")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                //   Log.d(TAG, document.getId() + " => " + document.getData());
                                if (document.getData().get("line") != null) {
                                    String responseBody = document.getData().get("line").toString();
//                                Toast.makeText(getActivity(), "DocumentSnapshot data: " + document.getId(), Toast.LENGTH_SHORT)
//                                .show();
                                    //  String responseBody=new String(bytes);
                                    try {
                                        JSONArray testV = new JSONArray(new String(responseBody));
                                        mJsonArray = testV;
                                        mPolylineOptions = new PolylineOptions()
                                                .color(Color.MAGENTA)
                                                .width(mLinewidth)
                                                .clickable(mClickabilityCheckbox.isChecked());
                                        for (int i = 0; i < mJsonArray.length(); i++) {
                                            double lat = Double.parseDouble(mJsonArray.getJSONObject(i).get(LATITUDE).toString());
                                            double lon = Double.parseDouble(mJsonArray.getJSONObject(i).get(LONGITUDE).toString());
                                            LatLng latLng = new LatLng(lat, lon);
                                            mPolylineOptions = mPolylineOptions.add(latLng);


                                        }
                                        mMutablePolyline = mMap.addPolyline(mPolylineOptions);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

    }

//    public void openlinesfromserver()
//    {
//        LatLng southwest = new LatLng(14.1603438,75.6205914);
//        LatLng northeast = new LatLng(14.0510405,75.7768592);
//        LatLngBounds bounds = new LatLngBounds(southwest,northeast);
//        String coordinates = new String();
//        double g =bounds.northeast.longitude;
//bounds
//        {
//                    "coordinates":  [[
//            [14.1603438,75.6205914],
//            [14.0697727,75.6018832],
//            [14.0510405,75.7768592],
//            [14.2538865,75.7388695],
//            [14.1603438,75.6205914]
//          ]]
//        }
//    }

    @Override
    public void onMapClick(LatLng latLng) {

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case LREQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    Toast.makeText(getActivity(), "Permission Granted 10", Toast.LENGTH_SHORT)
                            .show();
                } else {
                    // Permission Denied
                    Toast.makeText(getActivity(), "Permission Denied 11", Toast.LENGTH_SHORT)
                            .show();
                }
                break;

            case LOCATION_PERMISSION_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    Toast.makeText(getActivity(), "Permission Granted 20", Toast.LENGTH_SHORT)
                            .show();
                    // mMap.setMyLocationEnabled(true);
                } else {
                    // Permission Denied
                    Toast.makeText(getActivity(), "Permission Denied 21", Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
    public void getPipelineWithinCoordinates(LatLngBounds latLngBounds)
    {
//        LatLng southwest = new LatLng(14.1603438,75.6205914);
//        LatLng northeast = new LatLng(14.0510405,75.7768592);
//        //ArrayList<Double, Double>[] al = new ArrayList[n];
//        LatLngBounds bounds = null;
//        try{
//             bounds = new LatLngBounds(northeast,southwest);
//        }
//        catch(Exception e)
//        {
//            e.printStackTrace();
//        }

        String southwestlatitude =  Double.toString(latLngBounds.southwest.latitude);
        String southwestlongitude = Double.toString(latLngBounds.southwest.longitude);
        String northeastlatitude =  Double.toString(latLngBounds.northeast.latitude);
        String northeastlongitude=  Double.toString(latLngBounds.northeast.longitude);
        Coordinate box = new Coordinate();
        box.setNortheastlatitude(northeastlatitude);
        box.setNortheastlongitude(northeastlongitude);
        box.setSouthwestlatitude(southwestlatitude);
        box.setSouthwestlongitude(southwestlongitude);

        String strbox = gson.toJson(box);
        new PostJSONAsyncTask().execute(Constants.GET_PIPELINE_WITHIN_URL,strbox);
    }
    public  class PostJSONAsyncTask extends AsyncTask<String, Void, Boolean> {
        Dialog dialog;
        public  PostJSONAsyncTask()
        {
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new Dialog(getActivity(),android.R.style.Theme_Translucent);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.custom_progress_dialog);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.show();
            dialog.setCancelable(true);
        }

        @Override
        protected Boolean doInBackground(String... urls) {
            try {
                ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
                HttpPost request = new HttpPost(urls[0]);
                HttpClient httpclient = new DefaultHttpClient();
                UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(postParameters);
                StringEntity se = new StringEntity(urls[1]);
                request.setEntity(se);
                request.setHeader("Accept", "application/json");
                request.setHeader("Content-type", "application/json");
                request.setHeader(Constants.SECUREKEY_KEY, Constants.SECUREKEY_VALUE);
                request.setHeader(Constants.VERSION_KEY, Constants.VERSION_VALUE);
                request.setHeader(Constants.CLIENT_KEY, Constants.CLIENT_VALUE);
                HttpResponse response = httpclient.execute(request);

                int status = response.getStatusLine().getStatusCode();
                if (status == 200) {
                    HttpEntity entity = response.getEntity();

                    String responseOrder = EntityUtils.toString(entity);
                    try {
                        lineInfoList = new ArrayList<LineInfo>();
                        JSONArray jsonArray = new JSONArray(responseOrder);
                        for(int i = 0; i < jsonArray.length(); i++)
                        {
                            LineInfo lineInfo = null;
                            try {
                                lineInfo = gson.fromJson(jsonArray.getString(i), LineInfo.class);
                                JSONObject obj = jsonArray.getJSONObject(i);
                                {
                                    if (obj.has("location")) {
                                        JSONObject obj2 = obj.getJSONObject("location");
                                        if (obj2.has("coordinates")){
                                            JSONArray obj3 = obj2.getJSONArray("coordinates");
                                            for (int j = 0; j < obj3.length(); j++) {
                                                JSONArray obj4 = obj3.getJSONArray(j);
                                                Double objlat = obj4.getDouble(0);
                                                Double objlong = obj4.getDouble(1);
                                                lineInfo.getLoc().getCoordinates().add(new LatLng(objlat, objlong));
                                            }
                                        }
                                    }
                                }
                            }
                            catch(Exception e)
                            {
                                e.printStackTrace();
                            }
                            String str = lineInfo.getName();
                            lineInfoList.add(lineInfo);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    return true;
                }
            } catch (ParseException e1) {
                e1.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }
        protected void onPostExecute(Boolean result) {

            dialog.cancel();
            if(result == true){

                    try {
                        for (int i = 0; i < lineInfoList.size(); i++) {
                            location loc = lineInfoList.get(i).getLoc();
                            int points  = loc.getCoordinates().size();
                            mPolylineOptions = new PolylineOptions()
                                    .color(Color.MAGENTA)
                                    .width(mLinewidth)
                                    .clickable(mClickabilityCheckbox.isChecked());
                            for (int j = 0; j < points; j++) {
                                double lat = loc.getCoordinates().get(j).latitude;
                                double lon = loc.getCoordinates().get(j).longitude;
                                LatLng latLng = new LatLng(lat, lon);
                                mPolylineOptions = mPolylineOptions.add(latLng);
                            }
                            mMutablePolyline = mMap.addPolyline(mPolylineOptions);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

            }
            else if (result == false)
                Toast.makeText(getContext(), "Unable to fetch data from server", Toast.LENGTH_LONG).show();
        }
    }


}

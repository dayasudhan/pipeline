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
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.gson.Gson;
import com.kuruvatech.pipeline.R;
import com.kuruvatech.pipeline.model.Coordinate;
import com.kuruvatech.pipeline.model.GeoPoint;
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
import org.apache.http.client.methods.HttpGet;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

/**
 * Created by dayas on 05-08-2019.
 */

public class MultiLineFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMapClickListener, GoogleMap.OnCameraIdleListener,
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

    //private CheckBox mClickabilityCheckbox;
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
    private Button togglePlayButton;
    FirebaseFirestore mDb;
    FirebaseStorage mStorage ;
    Gson gson ;
    ArrayList<location> lineInfoList ;
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

//        mClickabilityCheckbox = (CheckBox) rootview.findViewById(R.id.toggleClickability);
        togglePlayButton = (Button) rootview.findViewById(R.id.maptypebutton);
   //     togglePauseButton = (ToggleButton) rootview.findViewById(R.id.togglebutton2);
        togglePlayButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int t = mMap.getMapType();
                t = (t + 1) % 5;
                if(t == 0)
                {
                    t++;
                }
                mMap.setMapType(t);
                //Toast.makeText(getContext(), mMap.getMapType().toString(), Toast.LENGTH_SHORT).show();

                   // GoogleMap.MAP_TYPE_NONE
            }
        });

        mFragment = (SupportMapFragment)fragmentManager.findFragmentById(R.id.map);
        mFragment.getMapAsync(this);
        return rootview;
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
        //Toast.makeText(getContext(), "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        getCurrentLocation();
        return false;
    }
    public void alertMessage(location obj) {
        DialogInterface.OnClickListener dialogClickListeneryesno = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {

                    case DialogInterface.BUTTON_NEUTRAL:
                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
      //  builder.setTitle("PipeLine");
       // builder.setView()
        final View loginFormView = getLayoutInflater().inflate(R.layout.lineinfo, null);
        TextView name = loginFormView.findViewById(R.id.infoName);
        TextView phone = loginFormView.findViewById(R.id.infoPhone);
        TextView type = loginFormView.findViewById(R.id.infoType);
        TextView purpose = loginFormView.findViewById(R.id.infoPurpose);
        TextView size = loginFormView.findViewById(R.id.infoSize);
        TextView datetv = loginFormView.findViewById(R.id.infoDate);
        name.setText(obj.getName());
        phone.setText(obj.getPhone());
        type.setText(obj.getType());
        purpose.setText(obj.getPurpose());
        size.setText(obj.getSize());
        datetv.setText(obj.getDate());
        builder.setView(loginFormView);
        builder.setNeutralButton("Ok", dialogClickListeneryesno).show();
             //   .setIcon(R.drawable.ic_action_about).show();

    }
    //and then register for location
    @Override
    public void onMapReady(GoogleMap map) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }
        mIsStartPipeLine =false;
//        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                != PackageManager.PERMISSION_GRANTED) {
//            // Permission to access the location is missing.
//            PermissionUtils.requestPermission((AppCompatActivity) getActivity(), LREQUEST_CODE_ASK_PERMISSIONS,
//                    Manifest.permission.WRITE_EXTERNAL_STORAGE, true);
//        }

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
        mMap.setContentDescription(getString(R.string.polyline_demo_description));


//        int color = Color.HSVToColor(
//                mAlphaBar.getProgress(), new float[]{mHueBar.getProgress(), 1, 1});
        mPolylineOptions = new PolylineOptions()
                .color(Color.RED)
                .width(mLinewidth)
                .clickable(true);
        // .add(KURUVA, KURUVA2, KURUVA3, KURUVA4);


        mMutablePolyline = map.addPolyline(mPolylineOptions);
        mMutablePolyline.setWidth(mLinewidth);
        mMutablePolyline.setPattern(PATTERN_MIXED);
       // mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        // Move the map so that it is centered on the mutable polyline.
        // map.moveCamera(CameraUpdateFactory.newLatLngZoom(MELBOURNE, 5));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(KURUVA, 10));
        // map.setMyLocationEnabled(true);
        // Add a listener for polyline clicks that changes the clicked polyline's color.

        mMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
            @Override
            public void onPolylineClick(Polyline polyline) {
                // Flip the values of the red, green and blue components of the polyline's color.
                polyline.setColor(polyline.getColor() ^ 0x00ffffff);
      //          polyline.getTag().toString();
               // Toast.makeText(getActivity(),  polyline.getT ag().toString(), Toast.LENGTH_LONG).show();
                alertMessage((location) polyline.getTag());
               // polyline.getId()

            }
        });
       // mMap.setOnMarkerDragListener();
     //   mMap.
        //openlinesfromfirestorage();
//        map.setLatLngBoundsForCameraTarget();

        enableMyLocation();
//        if (mGoogleApiClient == null) {
//            buildGoogleApiClient();
//        }
//        mMap.setMyLocationEnabled(true);
//        // mMap.setMyLocationButtonEnabled (true);
//        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMapClickListener(this);
        mMap.setOnCameraIdleListener(this);

//        map.onCameraChange(new GoogleMap.OnCameraChangeListener() {
//
//            @Override
//            public void onCameraChange(CameraPosition arg0) {
//                moveMapCameraToBoundsAndInitClusterkraf();
//            }
//        });
        VisibleRegion visibleRegion = mMap.getProjection().getVisibleRegion();
        LatLngBounds latLngBounds = visibleRegion.latLngBounds;
        getPipelineWithinCoordinates(latLngBounds);

    }
    public void initLocationbutton()
    {
        if (mGoogleApiClient == null) {
            buildGoogleApiClient();
        }
        mMap.setMyLocationEnabled(true);
        // mMap.setMyLocationButtonEnabled (true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
    }
    public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (shouldShowRequestPermissionRationale( Manifest.permission.ACCESS_FINE_LOCATION)) {

                new AlertDialog.Builder(getActivity())
                        .setTitle("Permission Required")
                        .setMessage("This permission was denied earlier by you. This permission is required to get your location. So, in order to use this feature please allow this permission by clicking ok.")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                ActivityCompat.requestPermissions(getActivity(),
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        LOCATION_PERMISSION_REQUEST_CODE);
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            } else {
                // No explanation needed, we can request the permission.
//                ActivityCompat.requestPermissions(getActivity(),
//                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                        LOCATION_PERMISSION_REQUEST_CODE);
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSION_REQUEST_CODE);
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
            case LOCATION_PERMISSION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    if (ContextCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        int off = 0;
                        try {
                            off = Settings.Secure.getInt(getActivity().getContentResolver(), Settings.Secure.LOCATION_MODE);
                        } catch (Settings.SettingNotFoundException e) {
                            e.printStackTrace();
                        }
                        if(off==0){
                            Intent onGPS = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(onGPS);
                        }
                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                       // mMap.setMyLocationButtonEnabled (true);
                        mMap.getUiSettings().setMyLocationButtonEnabled(true);

                    }

                } else {

                    //Toast.makeText(getActivity(), "Permission11111 Denied", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
//        switch (requestCode) {
//            case LREQUEST_CODE_ASK_PERMISSIONS:
//                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    // Permission Granted
//                    Toast.makeText(getActivity(), "Permission Granted 10", Toast.LENGTH_SHORT)
//                            .show();
//                } else {
//                    // Permission Denied
//                    Toast.makeText(getActivity(), "Permission Denied 11", Toast.LENGTH_SHORT)
//                            .show();
//                }
//                break;
//
//            case LOCATION_PERMISSION_REQUEST_CODE:
//                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    // Permission Granted
//                    Toast.makeText(getActivity(), "Permission Granted 20", Toast.LENGTH_SHORT)
//                            .show();
//                    // mMap.setMyLocationEnabled(true);
//                } else {
//                    // Permission Denied
//                    Toast.makeText(getActivity(), "Permission Denied 21", Toast.LENGTH_SHORT)
//                            .show();
//                }
//                break;
//            default:
//                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        }
//    }
    private void enableMyLocation() {
        //  Toast.makeText(getCon"enableMyLocation ", Toast.LENGTH_SHORT).show();

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
//            // Permission to access the location is missing.
//            PermissionUtils.requestPermission((AppCompatActivity) getActivity(), LOCATION_PERMISSION_REQUEST_CODE,
//                    Manifest.permission.ACCESS_FINE_LOCATION, true);
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            buildGoogleApiClient();

            //  Toast.makeText(getContext(), "enableMyLocation startLocationUpdates ", Toast.LENGTH_SHORT).show();

        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        }
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Don't do anything here.
    }


//    public void toggleClickability(View view) {
//        if (mMutablePolyline != null) {
//            mMutablePolyline.setClickable(((CheckBox) view).isChecked());
//        }
//    }

    public void getCurrentLocation()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                mMap.setMyLocationEnabled(true);
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

    @Override
    public void onMapClick(LatLng latLng) {

    }
//
    @Override
    public void onCameraIdle() {
        VisibleRegion visibleRegion = mMap.getProjection().getVisibleRegion();
        LatLngBounds latLngBounds = visibleRegion.latLngBounds;
        getPipelineWithinCoordinates(latLngBounds);
    }

    public void getPipelineWithinCoordinates(LatLngBounds latLngBounds)
    {
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

       //GET_PIPELINE_URL_FINAL_ALL
        //new PostJSONAsyncTask().execute(Constants.GET_PIPELINE_WITHIN_URL,strbox);
        new PostJSONAsyncTask().execute(Constants.GET_PIPELINE_WITHIN_URL_FINAL_ALL,strbox);
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
                        lineInfoList = new ArrayList<location>();
                        JSONArray jsonArray = new JSONArray(responseOrder);
                        for(int i = 0; i < jsonArray.length(); i++)
                        {
                            location lineInfo = null;
                            try {
                                lineInfo = gson.fromJson(jsonArray.getString(i), location.class);
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
                                                Double objele = obj4.getDouble(2);
                                                Double objres = obj4.getDouble(3);
//                                        1        if(obj4.length() > 2)
//                                                {
//                                                    Double objlong = obj4.getDouble(2);
//                                                }2
                                                lineInfo.getCoordinates().add(new LatLng(objlat, objlong));
                                                lineInfo.getElevation().add(new GeoPoint(new LatLng(objlat, objlong),objele,objres));
                                            }
                                        }
                                    }
                                    if(obj.has("name"))
                                    {
                                        lineInfo.setName(obj.getString("name"));
                                    }
                                    if(obj.has("phone"))
                                    {
                                        lineInfo.setPhone(obj.getString("phone"));
                                    }
                                    if(obj.has("size"))
                                    {
                                        lineInfo.setSizeofpipeline(obj.getString("size"));
                                    }
                                    if(obj.has("purpose"))
                                    {
                                        lineInfo.setPurpose(obj.getString("purpose"));
                                    }
                                    if(obj.has("pipe_type"))
                                    {
                                        lineInfo.setPipe_type(obj.getString("pipe_type"));
                                    }
                                    if(obj.has("remarks"))
                                    {
                                        lineInfo.setRemarks(obj.getString("remarks"));
                                    }
                                    if(obj.has("date"))
                                    {
                                        lineInfo.setDate(obj.getString("date"));
                                    }
                                }
                            }
                            catch(Exception e)
                            {
                                e.printStackTrace();
                            }
                          //  String str = lineInfo.getName();
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

            if ((dialog != null) && dialog.isShowing()) {
                dialog.cancel();
            }

            if(result == true){

                    try {
                       // int sx = lineInfoList.size() - 3;
                        for (int i = 0 ; i < lineInfoList.size() ; i++) {
                            location loc = lineInfoList.get(i);
                            int points  = loc.getCoordinates().size();
                            mPolylineOptions = new PolylineOptions()
                                    .color(Color.MAGENTA)
                                    .width(mLinewidth)
                                    .clickable(true);
                            for (int j = 0; j < points; j++) {
                                double lat = loc.getCoordinates().get(j).latitude;
                                double lon = loc.getCoordinates().get(j).longitude;
                                LatLng latLng = new LatLng(lat, lon);
                                mPolylineOptions = mPolylineOptions.add(latLng);
                                if ((j % 20 == 0) || j == 0 || j == (points -1))
                                {
                                    double elevation = loc.getElevation().get(j).getElevation();
                                    mMap.addMarker(new MarkerOptions()
                                            .position(latLng)
                                            .title(getString(R.string.elevation))
                                            .snippet(String.valueOf(elevation))
                                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker)));
                                }
                            }
                            mMutablePolyline = mMap.addPolyline(mPolylineOptions);
                           // String tag = lineInfoList.get(i).getName() + " ( " + lineInfoList.get(i).getPhone() + " ) " + "Size:" + lineInfoList.get(i).getSizeofpipeline();
                            mMutablePolyline.setTag(lineInfoList.get(i));

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

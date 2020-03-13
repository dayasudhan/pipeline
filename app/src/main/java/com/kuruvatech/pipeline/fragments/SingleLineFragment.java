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
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.GroundOverlayOptions;
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
import com.kuruvatech.pipeline.FullScreenViewActivity;
import com.kuruvatech.pipeline.R;
import com.kuruvatech.pipeline.model.Coordinate;
import com.kuruvatech.pipeline.model.LineInfo;
import com.kuruvatech.pipeline.model.location;
import com.kuruvatech.pipeline.utils.Constants;
import com.kuruvatech.pipeline.utils.GPSTracker;
import com.kuruvatech.pipeline.utils.PermissionUtils;
import com.kuruvatech.pipeline.utils.SessionManager;
import com.kuruvatech.pipeline.model.GeoPoint;
import com.kuruvatech.pipeline.SingleViewActivity;
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

import static androidx.core.content.ContextCompat.checkSelfPermission;


/**
 * Created by dayas on 05-08-2019.
 */

public class SingleLineFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMapClickListener, GoogleMap.OnCameraIdleListener,GoogleMap.OnMarkerClickListener, GoogleMap.OnInfoWindowClickListener,
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

    //and then register for location
    @Override
    public void onMapReady(GoogleMap map) {

        mIsStartPipeLine =false;
//        if (checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED) {
//            // Permission to access the location is missing.
////            PermissionUtils.requestPermission((AppCompatActivity) getActivity(), LREQUEST_CODE_ASK_PERMISSIONS,
////                    Manifest.permission.WRITE_EXTERNAL_STORAGE, true);
//            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                    LOCATION_PERMISSION_REQUEST_CODE);
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
                polyline.getTag().toString();
                alertMessage((location) polyline.getTag());
                //Toast.makeText(getActivity(),  polyline.getTag().toString(), Toast.LENGTH_LONG).show();
                // polyline.getId()

            }
        });
        //openlinesfromfirestorage();
//        map.setLatLngBoundsForCameraTarget();

        //enableMyLocation();
        initLocationbutton();
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMapClickListener(this);
        mMap.setOnMarkerClickListener((GoogleMap.OnMarkerClickListener) this);
        mMap.setOnInfoWindowClickListener(this);
     //   mMap.setOnCameraIdleListener(this);
//        map.onCameraChange(new GoogleMap.OnCameraChangeListener() {
//
//            @Override
//            public void onCameraChange(CameraPosition arg0) {
//                moveMapCameraToBoundsAndInitClusterkraf();
//            }
//        });
//        VisibleRegion visibleRegion = mMap.getProjection().getVisibleRegion();
//        LatLngBounds latLngBounds = visibleRegion.latLngBounds;
        getPipelineWithinCoordinates();

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



    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Don't do anything here.
    }



    public void getCurrentLocation()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(getActivity(),
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

    public void initLocationbutton()
    {
        if (checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED) {
           // getActivity().checkLocationPermission();
            return;
        }
        else {
            if (mGoogleApiClient == null) {
                buildGoogleApiClient();
            }
            mMap.setMyLocationEnabled(true);
            // mMap.setMyLocationButtonEnabled (true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        }
    }
    @Override
    public void onCameraIdle() {
        VisibleRegion visibleRegion = mMap.getProjection().getVisibleRegion();
        LatLngBounds latLngBounds = visibleRegion.latLngBounds;

    }

    public void getPipelineWithinCoordinates()
    {
        SessionManager mSession = new SessionManager(getContext());
        String url = Constants.GET_PIPELINE_URL_FINAL + mSession.getEmail();
       // String url = Constants.GET_PIPELINE_URL_FINAL;
        new PostJSONAsyncTask().execute(url);
    }

//    @Override
//    public boolean onMarkerClick(Marker marker) {
//        Toast.makeText(getContext(), "onMarkerClick", Toast.LENGTH_LONG).show();
//        Intent i = new Intent(getActivity(), FullScreenViewActivity.class);
//        i.putExtra("position", 0);
//        ArrayList<String> imageList = new ArrayList<String>();
//        imageList.add("https://chunavane.s3.ap-south-1.amazonaws.com/bsy/image/main1513709806497.jpg");
//        imageList.add("https://chunavane.s3.ap-south-1.amazonaws.com/bsy/image/main1513709891180.jpg");
//        imageList.add("https://chunavane.s3.ap-south-1.amazonaws.com/bsy/image/main1513710152785.jpg");
//        i.putExtra("imageurls",imageList);
//        startActivity(i);
//        return false;
//    }
    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Intent i = new Intent(getContext(), SingleViewActivity.class);
        i.putExtra("url", "https://chunavane.s3.ap-south-1.amazonaws.com/bsy/image/main1513709806497.jpg");
        startActivity(i);

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
                HttpGet request = new HttpGet(urls[0]);
                HttpClient httpclient = new DefaultHttpClient();
           //     UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(postParameters);
                //StringEntity se = new StringEntity(urls[1]);
             //   request.setEntity(se);
                request.addHeader(Constants.SECUREKEY_KEY, Constants.SECUREKEY_VALUE);
                request.addHeader(Constants.VERSION_KEY, Constants.VERSION_VALUE);
                request.addHeader(Constants.CLIENT_KEY, Constants.CLIENT_VALUE);
                HttpResponse response = httpclient.execute(request);

                int status = response.getStatusLine().getStatusCode();
                if (status == 200) {
                    HttpEntity entity = response.getEntity();

                    String responseOrder = EntityUtils.toString(entity);
                    try {
                        lineInfoList = new ArrayList<location>();
                        JSONArray jsonArray = new JSONArray(responseOrder);
                        for(int i = 0; i < (jsonArray.length() ); i++)
                        {
                            location lineInfo = new location();
                            try {
                               // lineInfo = gson.fromJson(jsonArray.getString(i), location.class);
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

//                                        1        if(obj4.length() > 2)
//                                                {
//                                                    Double objlong = obj4.getDouble(2);
//                                                }2
                                                lineInfo.getCoordinates().add(new LatLng(objlat, objlong));
                                                lineInfo.getElevation().add(new GeoPoint(new LatLng(objlat, objlong),objele,""));
                                            }
                                        }
                                    }
                                    if (obj.has("markers")) {
                                        JSONObject obj2 = obj.getJSONObject("markers");
                                        if (obj2.has("coordinates")){
                                            JSONArray obj3 = obj2.getJSONArray("coordinates");
                                            for (int j = 0; j < obj3.length(); j++) {
                                                JSONArray obj4 = obj3.getJSONArray(j);
                                                Double objlat = obj4.getDouble(0);
                                                Double objlong = obj4.getDouble(1);
                                                Double objele = obj4.getDouble(2);
                                                String objres = obj4.getString(3);
//                                        1        if(obj4.length() > 2)
//                                                {
//                                                    Double objlong = obj4.getDouble(2);
//                                                }2
                                              //  lineInfo.getCoordinates().add(new LatLng(objlat, objlong));
                                                lineInfo.getMarkers().add(new GeoPoint(new LatLng(objlat, objlong),objele,objres));
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
                        double baseelevation = 0;
                        for (int j = 0; j < points; j++) {
                            double lat = loc.getElevation().get(j).getLatlng().latitude;
                            double lon = loc.getElevation().get(j).getLatlng().longitude;
                            LatLng latLng = new LatLng(lat, lon);

                            mPolylineOptions = mPolylineOptions.add(latLng);

                            if(j == 0)
                            {
                                baseelevation = loc.getElevation().get(j).getElevation();
                            }

                            if ((j % 20 == 0) || j == 0 || j == (points -1))
                            {
                                double elevation =( loc.getElevation().get(j).getElevation() - baseelevation) ;

                                mMap.addMarker(new MarkerOptions()
                                        .position(latLng)
                                        .title(getString(R.string.elevation))
                                        .snippet(String.valueOf(elevation))
                                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker)));
                            }
                        }
                        int markerlength  = loc.getMarkers().size();
                        for (int k = 0; k < markerlength; k++) {
                            double lat = loc.getMarkers().get(k).getLatlng().latitude;
                            double lon = loc.getMarkers().get(k).getLatlng().longitude;
                            String markername = loc.getMarkers().get(k).getName();
                            LatLng latLng = new LatLng(lat, lon);
                                mMap.addMarker(new MarkerOptions()
                                        .position(latLng)
                                        .title(markername)
                                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_flag)));
                        }
                        mMutablePolyline = mMap.addPolyline(mPolylineOptions);
                      //  String tag = lineInfoList.get(i).getName() + " ( " + lineInfoList.get(i).getPhone() + " ) ";
                        mMutablePolyline.setTag(lineInfoList.get(i));
                        
                    }
                    if(lineInfoList.size() ==0)
                    {
                        Toast.makeText(getContext(), "No Pipeline Associated with This PhoneNumber", Toast.LENGTH_LONG).show();
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

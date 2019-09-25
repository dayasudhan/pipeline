package com.kuruvatech.pipeline.fragments;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;

//import android.location.LocationManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import androidx.fragment.app.FragmentManager;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;

import androidx.appcompat.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kuruvatech.pipeline.R;

import com.kuruvatech.pipeline.model.PipelineObject;
//import com.kuruvatech.pipeline.utils.Constants;
import com.kuruvatech.pipeline.utils.GPSTracker;
import com.kuruvatech.pipeline.utils.PermissionUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import android.os.Build;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;


public class MainFragment extends Fragment  implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, GoogleMap.OnMyLocationButtonClickListener,
        SeekBar.OnSeekBarChangeListener,GoogleMap.OnMapClickListener,
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
    private static final LatLng KURUVA2 = new LatLng(14.143235517478407, 75.66776655087282);
    private static final LatLng KURUVA3 = new LatLng(14.14423717478407, 75.66876455087282);
    private static final LatLng KURUVA4 = new LatLng(14.145239317478407, 75.66976355087282);

    // Airport locations for geodesic polyline.
    private static final LatLng AKL = new LatLng(-37.006254, 174.783018);
    private static final LatLng JFK = new LatLng(40.641051, -73.777485);
    private static final LatLng LAX = new LatLng(33.936524, -118.377686);
    private static final LatLng LHR = new LatLng(51.471547, -0.460052);
    private long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */


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
//    private static final int[] PATTERN_TYPE_NAME_RESOURCE_IDS;
//
//    static {
//        PATTERN_TYPE_NAME_RESOURCE_IDS = new int[]{
//                R.string.pattern_solid, // Default
//                R.string.pattern_dashed,
//                R.string.pattern_dotted,
//                R.string.pattern_mixed,
//        };
//    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        rootview = inflater.inflate(R.layout.polyline_demo, container, false);
        fragmentManager=getChildFragmentManager();
        mDb = FirebaseFirestore.getInstance();
// Get a non-default Storage bucket

        mClickabilityCheckbox = (CheckBox) rootview.findViewById(R.id.toggleClickability);
        togglePlayButton = (ToggleButton) rootview.findViewById(R.id.togglebutton);
        togglePauseButton = (ToggleButton) rootview.findViewById(R.id.togglebutton2);
        togglePlayButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                startLocationUpdates(isChecked);
                if(isChecked) {
                    togglePauseButton.setVisibility(View.VISIBLE);
                }
                else
                {
                    togglePauseButton.setVisibility(View.INVISIBLE);
                }

            }
        });
        togglePauseButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //startLocationUpdates(isChecked);
                togglePauseButton.setVisibility(View.INVISIBLE);

            }
        });

        //SupportMapFragment mapFragment =
//                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
//        mapFragment.getMapAsync(this);
        //   googleMap = ((SupportMapFragment)fragmentManager.findFragmentById(R.id.map)).getMap();
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
        //   Toast.makeText(getContext(), "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        getCurrentLocation();
        return false;
    }

    //and then register for location
    @Override
    public void onMapReady(GoogleMap map) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }
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
                onLocationChanged(locationResult.getLastLocation());
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
        enableMyLocation();
        map.setOnMyLocationButtonClickListener(this);
        map.setOnMapClickListener(this);

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

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // Don't do anything here.
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // Don't do anything here.
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (mMutablePolyline == null) {
            return;
        }

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

    //    public static LatLng locationToLatLng(Location loc) {
//        if(loc != null)
//            return new LatLng(loc.getLatitude(), loc.getLongitude());
//        return null;
//    }
    public void savetodb()
    {
// Create a new user with a first and last name
        Map<String, Object> user = new HashMap<>();
        GeoPoint gp = new GeoPoint(1,1);

        PipelineObject pobj= new PipelineObject();
//        pobj.setRegions(Arrays.asList("west_coast", "sorcal"));
        pobj.setName("Devraj");
        pobj.setPhone("9566229075");
        pobj.setVillage("kuruva");
        try {
//            JSONObject obj21 = new JSONObject();
//            JSONObject obj31 = new JSONObject();
//            obj21.put("Latitude", Double.toString(15.9808098));
//            obj21.put("Longitude", Double.toString(75.980980980));
//            obj31.put("Latitude", Double.toString(14.9808098));
//            obj31.put("Longitude", Double.toString(17.980980980));
//            mJsonArray.put(obj21);
//            mJsonArray.put(obj31);
            for(int i = 0; i < mJsonArray.length();i++)
            {
                HashMap<String,String> l1=  new HashMap<>();
                l1.put("Lat", mJsonArray.getJSONObject(i).getString("Latitude"));
                l1.put("Logt", mJsonArray.getJSONObject(i).getString("Longitude"));
                pobj.getLine().add(l1);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mDb.collection("pipeline")
                .add(pobj)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(getActivity(), documentReference.getId(), Toast.LENGTH_LONG).show();
                        //    Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), "Error adding document->" + e.getMessage(), Toast.LENGTH_LONG).show();
                        //    Log.w(TAG, "Error adding document", e);
                    }
                });

    }
    public void savetodb2()
    {
// Create a new user with a first and last name
        Map<String, Object> user = new HashMap<>();
        GeoPoint gp = new GeoPoint(1,1);

        PipelineObject pobj= new PipelineObject();
//        pobj.setRegions(Arrays.asList("west_coast", "sorcal"));
        pobj.setName("Devraj");
        pobj.setPhone("9566229075");
        pobj.setVillage("kuruva");
        try {
            JSONObject obj21 = new JSONObject();
            JSONObject obj31 = new JSONObject();
            obj21.put("Latitude", Double.toString(15.9808098));
            obj21.put("Longitude", Double.toString(75.980980980));
            obj31.put("Latitude", Double.toString(14.9808098));
            obj31.put("Longitude", Double.toString(17.980980980));
            mJsonArray.put(obj21);
            mJsonArray.put(obj31);
            for(int i = 0; i < mJsonArray.length();i++)
            {
                HashMap<String,String> l1=  new HashMap<>();
                l1.put("Lat", mJsonArray.getJSONObject(i).getString("Latitude"));
                l1.put("Logt", mJsonArray.getJSONObject(i).getString("Longitude"));
                pobj.getLine().add(l1);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mDb.collection("pipeline")
                .add(pobj)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(getActivity(), documentReference.getId(), Toast.LENGTH_LONG).show();
                        //    Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), "Error adding document->" + e.getMessage(), Toast.LENGTH_LONG).show();
                        //    Log.w(TAG, "Error adding document", e);
                    }
                });

    }
    public void savetofirebasestorage()
    {
        //JSONObject obj = new JSONObject();
        String  h = DateFormat.format("MM-dd-yyyyy-h-mmss", System.currentTimeMillis()).toString();
        JSONObject obj = new JSONObject();

        try {
            obj.put("Locations",mJsonArray);

            mStorage = FirebaseStorage.getInstance();
            StorageReference storageRef = mStorage.getReference();
            // Create a reference to "mountains.jpg"
            //StorageReference mountainsRef = storageRef.child(h+".json");

            // Create a reference to 'images/mountains.jpg'
            StorageReference pipelineRef = storageRef.child("pipeline/" + h+".json");
            byte[] data = obj.toString().getBytes("utf-8");
            UploadTask uploadTask = pipelineRef.putBytes(data);

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(getContext(), new String("File generated with name failure" ), Toast.LENGTH_SHORT).show();
                    // Handle unsuccessful uploads
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(getContext(),  new String("File generated with name success"), Toast.LENGTH_SHORT).show();
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                    // ...
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void savetofile()
    {
        try {
            String  h = DateFormat.format("MM-dd-yyyyy-h-mmss", System.currentTimeMillis()).toString();
            // this will create a new name everytime and unique
            File rootr = new File(Environment.getExternalStorageDirectory(), "Notes");
            File root2= new File("/sdcard/", "Notes");
            File root= new File(getContext().getCacheDir(), "Notes");

            Toast.makeText(getContext(), "m1", Toast.LENGTH_SHORT).show();
            // if external memory exists and folder with name Notes
            if (!root.exists()) {
                Toast.makeText(getContext(), "m2", Toast.LENGTH_SHORT).show();
                root.mkdirs(); // this will create folder.
            }
            Toast.makeText(getContext(), root.getAbsolutePath(), Toast.LENGTH_SHORT).show();
            JSONObject obj = new JSONObject();
            JSONObject obj2 = new JSONObject();
            JSONObject obj3 = new JSONObject();
            obj2.put("Latitude", Double.toString(14.9808098));
            obj2.put("Longitude", Double.toString(74.980980980));
            obj3.put("Latitude", Double.toString(14.9808098));
            obj3.put("Longitude", Double.toString(74.980980980));
            mJsonArray.put(obj2);
            mJsonArray.put(obj3);
            obj.put("Object",mJsonArray);
            mStorage = FirebaseStorage.getInstance();
            // StorageReference storageRef = mStorage.getReference();

            File filepath = new File(root, h + ".json");  // file path to save
            FileWriter writer = new FileWriter(filepath);
            Toast.makeText(getContext(), "m4", Toast.LENGTH_SHORT).show();
            writer.write(obj.toString());
            writer.flush();
            writer.close();
            Toast.makeText(getContext(), "m5", Toast.LENGTH_SHORT).show();
            //    String m = "File generated with name " + h + ".json";
            Toast.makeText(getContext(), "File generated with name " + h + ".json", Toast.LENGTH_SHORT).show();
            StorageReference storageRef = mStorage.getReference();
            // Create a reference to "mountains.jpg"
            StorageReference mountainsRef = storageRef.child(h+".json");

            // Create a reference to 'images/mountains.jpg'
            StorageReference mountainImagesRef = storageRef.child("pipeline/" + h+".json");
            byte[] data = obj.toString().getBytes("utf-8");
            UploadTask uploadTask = mountainImagesRef.putBytes(data);
            //mountainImagesRef.getStorage().
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(getContext(), "File generated with name failure" +  ".json", Toast.LENGTH_SHORT).show();
                    // Handle unsuccessful uploads
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(getContext(), "File generated with name success" +  ".json", Toast.LENGTH_SHORT).show();
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                    // ...
                }
            });
            // Create a reference to 'images/mountains.jpg'
            //     StorageReference mountainImagesRef = storageRef.child("images/mountains.jpg");
//                String[] listOfFiles = getActivity().getCacheDir().list();
//                for(int i = 0 ; i < listOfFiles.length; i++)
//                {
//                    Toast.makeText(getContext(), listOfFiles[i], Toast.LENGTH_SHORT).show();
//                    if(listOfFiles[i].equals("Notes"))
//                    {
//                        String k = new String(getActivity().getCacheDir().getAbsolutePath().toString() + "/Notes");
//                        String[] listOfFiles2 = getActivity().getCacheDir().list();
//                        File directory = new File(k);
//                        File[] files = directory.listFiles();
//                        Toast.makeText(getContext(), "Size: "+ files.length, Toast.LENGTH_SHORT).show();
////                        Log.d("Files", "Size: "+ files.length);
//                        for (int jj = 0; jj < files.length; jj++)
//                        {
//                            Toast.makeText(getContext(), "Size: "+ files[jj].getName(), Toast.LENGTH_SHORT).show();
////                            Log.d("Files", "FileName:" + files[i].getName());
//                        }
//                    }
//                }

            //   result.setText(m);


        } catch (IOException e) {
            e.printStackTrace();
            //  result.setText(e.getMessage().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }


    @Override
    public void onMapClick(LatLng latLng) {

    }
    // Trigger new location updates at interval
    protected void startLocationUpdates(boolean IsStartStop) {
        if(IsStartStop) {
            Toast.makeText(getContext(), "startLocationUpdates 1 true", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(getContext(), "startLocationUpdates 1 false", Toast.LENGTH_SHORT).show();
        }
        // Create the location request to start receiving updates
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        SettingsClient settingsClient = LocationServices.getSettingsClient(getActivity());
        settingsClient.checkLocationSettings(locationSettingsRequest);
        Toast.makeText(getContext(), "startLocationUpdates 2", Toast.LENGTH_SHORT).show();
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            //Toast.makeText(getContext(), "startLocationUpdates 3", Toast.LENGTH_SHORT).show();
            // Permission to access the location is missing.
            PermissionUtils.requestPermission((AppCompatActivity) getActivity(), LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
            // new Google API SDK v11 uses getFusedLocationProviderClient(this)
            if(IsStartStop == true) {
                Toast.makeText(getContext(), "startLocationUpdates 31", Toast.LENGTH_SHORT).show();
                getFusedLocationProviderClient(getActivity()).requestLocationUpdates(mLocationRequest, mLocationcallback,
                        Looper.myLooper());


            }
            else
            {
                Toast.makeText(getContext(), "startLocationUpdates 32", Toast.LENGTH_SHORT).show();
                getFusedLocationProviderClient(getActivity()).requestLocationUpdates(mLocationRequest, null,
                        null);
            }
        }
        else
        {
            mMap.setMyLocationEnabled(true);
            //  Toast.makeText(getContext(), "startLocationUpdates 4", Toast.LENGTH_SHORT).show();
            if(IsStartStop == true) {
                Toast.makeText(getContext(), "startLocationUpdates 41", Toast.LENGTH_SHORT).show();

                getFusedLocationProviderClient(getActivity()).requestLocationUpdates(mLocationRequest, mLocationcallback,
                        null);
                //savetodb();
            }
            else
            {
                Toast.makeText(getContext(), "startLocationUpdates 42", Toast.LENGTH_SHORT).show();
                getFusedLocationProviderClient(getActivity()).removeLocationUpdates(mLocationcallback);
                // savetofile();
                // savetofirebasestorage();
                savetodb();
                Toast.makeText(getContext(), "startLocationUpdates 43", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    public void onLocationChanged(Location location) {
        // New location has now been determined
        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        JSONObject loc = new JSONObject();
        try {
            loc.put("Latitude", Double.toString(location.getLatitude()));
            loc.put("Longitude", Double.toString(location.getLongitude()));

            mJsonArray.put(loc);

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        // You can now create a LatLng Object for use with maps
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mPolylineOptions = mPolylineOptions.add(latLng);

        mMutablePolyline = mMap.addPolyline(mPolylineOptions);
    }
    public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
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
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
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

                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(getActivity(), "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

}

package com.safecity.Fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.safecity.Dialogs.ViewReportDialog;
import com.safecity.Objects.Report;
import com.safecity.R;
import com.safecity.Utils.Utils;

import java.util.ArrayList;

public class AdminMapsFragment extends Fragment implements GoogleMap.OnInfoWindowClickListener {

    private FirebaseDatabase SAFECITY;
    private FirebaseUser USER;
    private DatabaseReference dbReports;
    private ValueEventListener velReports;

    private void initializeFirebase() {
        SAFECITY = FirebaseDatabase.getInstance();
        USER = FirebaseAuth.getInstance().getCurrentUser();
    }

    View view;
    GoogleMap googleMap;
    Location currentLocation;

    FusedLocationProviderClient fusedLocationProviderClient;
    ExtendedFloatingActionButton ebtnMapStyle;

    // report markers
    ArrayList<Report> arrReports;
    ArrayList<Marker> arrMarkers;

    private OnMapReadyCallback callback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(@NonNull GoogleMap map) {
            googleMap = map;
            // googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style));
            googleMap.getUiSettings().setMapToolbarEnabled(false);
            googleMap.clear();
            // getCurrentLocation();
            renderReports();

            /*btnHelp.setOnClickListener(view -> {

                LatLng currentPosition = googleMap.getCameraPosition().target;

                Bundle args = new Bundle();
                args.putDouble("latitude", currentPosition.latitude);
                args.putDouble("longitude", currentPosition.longitude);

                EmergencyDialog emergencyDialog = new EmergencyDialog();
                emergencyDialog.setArguments(args);
                emergencyDialog.show(requireActivity().getSupportFragmentManager(), "EMERGENCY_DIALOG");
            });

            btnStopHelp.setOnClickListener(view -> {
                DatabaseReference dbMyEmergency = SAFECITY.getReference("emergencies/"+ Objects.requireNonNull(USER).getUid());
                dbMyEmergency.removeValue();
            });*/

            ebtnMapStyle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (googleMap.getMapType() == GoogleMap.MAP_TYPE_NORMAL) {
                        googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                        ebtnMapStyle.setIcon(getResources().getDrawable(R.drawable.map_style_alt_24));
                    }
                    else {
                        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        ebtnMapStyle.setIcon(getResources().getDrawable(R.drawable.map_style_24));
                    }
                }
            });
        }
    };

    @Override
    public void onResume() {
        super.onResume();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_admin_maps, container, false);

        initializeFirebase();
        initialize();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext());
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (velReports != null) {
            dbReports.removeEventListener(velReports);
        }
    }

    private void initialize() {
        /*DatabaseReference dbMyEmergency = SAFECITY.getReference("emergencies/"+ Objects.requireNonNull(USER).getUid());
        dbMyEmergency.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) { // if user is calling for help
                    btnStopHelp.setVisibility(View.VISIBLE);
                    tvLocationWarning.setVisibility(View.VISIBLE);
                    btnHelp.setVisibility(View.GONE);
                }
                else { // if user is NOT calling for help
                    btnHelp.setVisibility(View.VISIBLE);
                    btnStopHelp.setVisibility(View.GONE);
                    tvLocationWarning.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });*/
        /*btnHelp = view.findViewById(R.id.btnHelp);
        btnStopHelp = view.findViewById(R.id.btnStopHelp);
        tvLocationWarning = view.findViewById(R.id.tvLocationWarning);*/
        ebtnMapStyle = view.findViewById(R.id.ebtnMapStyle);
    }

    private void renderReports() {
        DatabaseReference dbUserClass = SAFECITY.getReference("user_" + USER.getUid() + "/userType");
        dbUserClass.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int userClass = Integer.parseInt(snapshot.getValue().toString());

                DatabaseReference dbAdminLocation = SAFECITY.getReference("user_"+USER.getUid()+"/location");
                dbAdminLocation.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String adminLocation = snapshot.getValue().toString();
                        Log.d("DEBUG", "Admin's location is: "+adminLocation);

                        arrReports = new ArrayList<>();
                        arrMarkers = new ArrayList<>();
                        arrReports.clear();
                        arrMarkers.clear();

                        dbReports = SAFECITY.getReference("reports");
                        velReports = dbReports.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                arrReports.clear();
                                arrMarkers.clear();

                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                    Report report = dataSnapshot.getValue(Report.class);
                                    arrReports.add(report);

                                    LatLng reportLatLng = new LatLng(report.getLatitude(), report.getLongitude());
                                    MarkerOptions markerOptions = new MarkerOptions();
                                    if (report.getClasss() == 0) {
                                        markerOptions
                                                .position(reportLatLng)
                                                .icon(Utils.bitmapDescriptorFromVector(getContext(), R.drawable.ems_24, 60, 60))
                                                .title(report.getType())
                                                .snippet(report.getDetails());
                                    }
                                    else if (report.getClasss() == 1) {
                                        markerOptions
                                                .position(reportLatLng)
                                                .icon(Utils.bitmapDescriptorFromVector(getContext(), R.drawable.police_report_24, 60, 60))
                                                .title(report.getType())
                                                .snippet(report.getDetails());
                                    }

                                    Log.d("DEBUG", "User's class is: "+userClass+"; User's location is : "+adminLocation);
                                    if (report.getClasss() == userClass && report.getAddress().contains(adminLocation)) {
                                        Log.d("DEBUG", "Report class is : "+report.getClasss()+"; Report location is: "+report.getAddress()+"; Drawing marker");
                                        arrMarkers.add(googleMap.addMarker(markerOptions));
                                        googleMap.setOnInfoWindowClickListener(AdminMapsFragment.this);
                                    }
                                    else {
                                        Log.d("DEBUG", "Report class is : "+report.getClasss()+"; Report location is: "+report.getAddress()+"; Skipping");
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION }, 100);
            return;
        }

        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(60000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationCallback mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Toast.makeText(getContext()," location result is  " + locationResult, Toast.LENGTH_LONG).show();

                if (locationResult == null) {
                    Toast.makeText(getContext(),"current location is null ", Toast.LENGTH_LONG).show();

                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        Toast.makeText(getContext(),"current location is " + location.getLongitude(), Toast.LENGTH_LONG).show();

                        //TODO: UI updates.
                    }
                }
            }
        };

        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(location -> {
            if (location != null) {
                currentLocation = location;

                LatLng myLocation = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                /*googleMap.addMarker(new MarkerOptions()
                                .position(myLocation)
                                .title("Sakto ni nga location?"));*/
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 15));
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            }
        }
    }

    @Override
    public void onInfoWindowClick(@NonNull Marker marker) {
        int reportIndex = arrMarkers.indexOf(marker);
        Bundle reportArgs = new Bundle();
        reportArgs.putString("report_uid", arrReports.get(reportIndex).getUid());
        reportArgs.putString("report_type", arrReports.get(reportIndex).getType());
        reportArgs.putString("report_details", arrReports.get(reportIndex).getDetails());
        reportArgs.putLong("report_timestamp", arrReports.get(reportIndex).getTimestamp());/*

        ViewReportDialog viewReportDialog = new ViewReportDialog();
        viewReportDialog.setArguments(reportArgs);
        viewReportDialog.show(requireActivity().getSupportFragmentManager(), "VIEW_REPORT_DIALOG");*/

        ViewReportFragment viewReportFragment = new ViewReportFragment();
        viewReportFragment.setArguments(reportArgs);
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.frameLayout, viewReportFragment, "VIEW_REPORT_FRAGMENT")
                .addToBackStack("VIEW_REPORT_FRAGMENT")
                .commit();
    }
}
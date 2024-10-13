package com.safecity.Fragments;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.safecity.Objects.Report;
import com.safecity.R;
import com.safecity.Utils.Utils;
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
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class UserMapsFragment extends Fragment implements GoogleMap.OnInfoWindowClickListener {

    private static final FirebaseDatabase SAFECITY = FirebaseDatabase.getInstance();
    private static final FirebaseUser USER = FirebaseAuth.getInstance().getCurrentUser();

    View view;
    GoogleMap googleMap;
    Location currentLocation;

    ConstraintLayout clReportInformation, clEmergencyInformation;
    TextView tvLocationWarning;
    MaterialButton btnCancelReport, btnCancelEmergency, btnSendReport, btnSendEmergency;
    TextInputEditText etReportType, etReportDetails, etEmergencyType, etEmergencyDetails;
    ExtendedFloatingActionButton ebtnReport, ebtnMapStyle, ebtnEMS;
    ImageView ivPin;

    FusedLocationProviderClient fusedLocationProviderClient;

    // workshop markers
    /*ArrayList<Workshop> arrWorkshops;
    ArrayList<Marker> arrMarkers;*/

    private OnMapReadyCallback callback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(@NonNull GoogleMap map) {
            googleMap = map;
            // googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style));
            googleMap.getUiSettings().setMapToolbarEnabled(false);
            getCurrentLocation();

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
                DatabaseReference dbMyEmergency = safecity_DB.getReference("emergencies/"+ Objects.requireNonNull(USER).getUid());
                dbMyEmergency.removeValue();
            });*/
            
            googleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
                @Override
                public void onCameraMove() {
                    if (ebtnReport.isExtended()) {
                        ebtnReport.shrink();
                    }
                    if (ebtnEMS.isExtended()) {
                        ebtnEMS.shrink();
                    }
                    if (ebtnMapStyle.isShown()) {
                        ebtnMapStyle.hide();
                    }
                }
            });

            googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                @Override
                public void onCameraIdle() {
                    if (!ebtnReport.isExtended()) {
                        ebtnReport.extend();
                    }
                    if (!ebtnEMS.isExtended()) {
                        ebtnEMS.extend();
                    }
                    if (!ebtnMapStyle.isShown()) {
                        ebtnMapStyle.show();
                    }
                }
            });

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

            ebtnReport.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    clReportInformation.setVisibility(View.VISIBLE);
                    ebtnReport.hide();
                    ebtnEMS.hide();
                }
            });

            ebtnEMS.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    clEmergencyInformation.setVisibility(View.VISIBLE);
                    ebtnReport.hide();
                    ebtnEMS.hide();
                }
            });

            btnSendEmergency.setOnClickListener(view -> {
                sendReport(0);
            });

            btnSendReport.setOnClickListener(view -> {
                sendReport(1);
            });
        }
    };

    private void sendReport(int classs) {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(requireContext(), Locale.getDefault());
        LatLng markerLocation = googleMap.getCameraPosition().target;

        DatabaseReference dbReports = SAFECITY.getReference("reports").push();

        try {
            // get data
            String uid = dbReports.getKey();
            String type = "";
            String details = "";
            if (classs == 0) {
                type = etEmergencyType.getText().toString().trim();
                details = etEmergencyDetails.getText().toString().trim();
            }
            else {
                type = etReportType.getText().toString().trim();
                details = etReportDetails.getText().toString().trim();
            }
            double latitude = markerLocation.latitude;
            double longitude = markerLocation.longitude;
            int status = 0;

            addresses = geocoder.getFromLocation(latitude, longitude, 1);
            String subAdminArea = addresses.get(0).getSubAdminArea();
            String locality = addresses.get(0).getLocality();
            String address = locality + ", " + subAdminArea;

            if (type.isEmpty() || details.isEmpty()) {
                Toast.makeText(requireContext(), "Please provide the required information.", Toast.LENGTH_SHORT).show();
                return;
            }

            MaterialAlertDialogBuilder dialogUnavailable = new MaterialAlertDialogBuilder(requireContext());
            dialogUnavailable.setTitle("SafeCity is not available in this area");
            dialogUnavailable.setMessage("Sorry for the inconvenience. SafeCity is not yet operating in this area.");
            dialogUnavailable.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });

            if (locality == null) {
                dialogUnavailable.show();
                return;
            }

            if (classs == 0 && locality.equalsIgnoreCase("Valencia")) {
                MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(requireContext());
                dialog.setTitle("EMS reports are not available in Valencia");
                dialog.setMessage("Sorry for the inconvenience. The EMS are not yet operating in this area.");
                dialog.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                dialog.show();
                return;
            }

            if (!locality.equalsIgnoreCase("Dumaguete") &&
                    !locality.equalsIgnoreCase("Sibulan") &&
                    !locality.equalsIgnoreCase("Valencia")) {
                dialogUnavailable.show();
                return;
            }

            // create new report
            Report report = new Report(uid, type, classs, details, latitude, longitude, address, status, USER.getUid(), System.currentTimeMillis());
            dbReports.setValue(report);

            // update last report or emergency node
            DatabaseReference dbLastReport = SAFECITY.getReference("last_report_or_emergency");
            dbLastReport.setValue(report);

            // reset UI
            clEmergencyInformation.setVisibility(View.GONE);
            clReportInformation.setVisibility(View.GONE);
            ebtnReport.show();
            ebtnEMS.show();

            etEmergencyType.getText().clear();
            etEmergencyDetails.getText().clear();
            etReportType.getText().clear();
            etReportDetails.getText().clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_maps, container, false);
        
        initialize();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext());
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }

        btnCancelReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (clReportInformation.getVisibility() == View.VISIBLE) {
                    clReportInformation.setVisibility(View.GONE);
                    ebtnReport.setVisibility(View.VISIBLE);
                    ebtnEMS.setVisibility(View.VISIBLE);
                }
            }
        });

        btnCancelEmergency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (clEmergencyInformation.getVisibility() == View.VISIBLE) {
                    clEmergencyInformation.setVisibility(View.GONE);
                    ebtnReport.setVisibility(View.VISIBLE);
                    ebtnEMS.setVisibility(View.VISIBLE);
                }
            }
        });

        return view;
    }

    private void initialize() {
        /*DatabaseReference dbMyEmergency = safecity_DB.getReference("emergencies/"+ Objects.requireNonNull(USER).getUid());
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
        clReportInformation = view.findViewById(R.id.clReportInformation);
        clEmergencyInformation = view.findViewById(R.id.clEmergencyInformation);
        ebtnReport = view.findViewById(R.id.ebtnReport);
        ebtnEMS = view.findViewById(R.id.ebtnEMS);
        ebtnMapStyle = view.findViewById(R.id.ebtnMapStyle);
        btnCancelReport = view.findViewById(R.id.btnCancelReport);
        btnCancelEmergency = view.findViewById(R.id.btnCancelEmergency);
        btnSendReport = view.findViewById(R.id.btnSendReport);
        btnSendEmergency = view.findViewById(R.id.btnSendEmergency);
        etReportType = view.findViewById(R.id.etReportType);
        etReportDetails = view.findViewById(R.id.etReportDetails);
        etEmergencyType = view.findViewById(R.id.etEmergencyType);
        etEmergencyDetails = view.findViewById(R.id.etEmergencyDetails);
        ivPin = view.findViewById(R.id.ivPin);
        Glide.with(requireContext()).load(R.drawable.pin).into(ivPin);
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
                googleMap.setMyLocationEnabled(true);
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 15));
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
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
        /*int workshopIndex = arrMarkers.indexOf(marker);

        Workshop clickedWorkshop = arrWorkshops.get(workshopIndex);
        Bundle workshopArgs = new Bundle();
        workshopArgs.putString("uid", clickedWorkshop.getUid());
        workshopArgs.putString("name", clickedWorkshop.getName());
        workshopArgs.putDouble("latitude", clickedWorkshop.getLatitude());
        workshopArgs.putDouble("longitude", clickedWorkshop.getLongitude());
        workshopArgs.putString("address", clickedWorkshop.getAddress());
        workshopArgs.putString("available_services", clickedWorkshop.getAvailableServices());
        workshopArgs.putString("owner_uid", clickedWorkshop.getOwnerUid());

        WorkshopDialog workshopDialog = new WorkshopDialog();
        workshopDialog.setArguments(workshopArgs);
        workshopDialog.show(requireActivity().getSupportFragmentManager(), "WORKSHOP_DIALOG");*/
    }
}
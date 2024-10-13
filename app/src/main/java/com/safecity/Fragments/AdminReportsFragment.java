package com.safecity.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.ChipGroup;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.safecity.Activities.AdminActivity;
import com.safecity.Activities.AuthenticationActivity;
import com.safecity.Activities.UserActivity;
import com.safecity.Adapters.ReportAdapter;
import com.safecity.Objects.Report;
import com.safecity.R;

import java.util.ArrayList;
import java.util.Objects;

public class AdminReportsFragment extends Fragment implements ReportAdapter.OnReportListener {

    private FirebaseDatabase SAFECITY;
    private FirebaseUser USER;

    private void initializeFirebase() {
        SAFECITY = FirebaseDatabase.getInstance();
        USER = FirebaseAuth.getInstance().getCurrentUser();
    }

    View view;
    RecyclerView rvReports;
    CircularProgressIndicator loadingBar;

    ArrayList<Report> arrReports;
    ReportAdapter reportAdapter;
    ReportAdapter.OnReportListener onReportListener = this;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_admin_report, container, false);

        initializeFirebase();
        initialize();

        return view;
    }

    private void initialize() {
        rvReports = view.findViewById(R.id.rvReports);
        loadingBar = view.findViewById(R.id.loadingBar);

        loadReports();
    }

    private void loadReports() {
        arrReports = new ArrayList<>();
        rvReports = view.findViewById(R.id.rvReports);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        rvReports.setLayoutManager(linearLayoutManager);

        DatabaseReference dbUserClass = SAFECITY.getReference("user_" + USER.getUid() + "/classs");
        dbUserClass.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int userClass = Integer.parseInt(snapshot.getValue().toString());

                DatabaseReference dbAdminLocation = SAFECITY.getReference("user_"+USER.getUid()+"/location");
                dbAdminLocation.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String adminLocation = snapshot.getValue().toString();

                        DatabaseReference dbReports = SAFECITY.getReference("reports");
                        dbReports.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                arrReports.clear();

                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                    Report report = dataSnapshot.getValue(Report.class);

                                    if (report.getClasss() == userClass && report.getAddress().contains(adminLocation)) {
                                        arrReports.add(report);
                                    }
                                }

                                loadingBar.hide();

                                reportAdapter = new ReportAdapter(getContext(), arrReports, onReportListener);
                                rvReports.setAdapter(reportAdapter);
                                reportAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    @Override
    public void onReportClick(int position) {
        Bundle reportArgs = new Bundle();
        reportArgs.putString("report_uid", arrReports.get(position).getUid());
        reportArgs.putString("report_type", arrReports.get(position).getType());
        reportArgs.putString("report_details", arrReports.get(position).getDetails());
        reportArgs.putLong("report_timestamp", arrReports.get(position).getTimestamp());

        ViewReportFragment viewReportFragment = new ViewReportFragment();
        viewReportFragment.setArguments(reportArgs);
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.frameLayout, viewReportFragment, "VIEW_REPORT_FRAGMENT")
                .addToBackStack("VIEW_REPORT_FRAGMENT")
                .commit();
    }
}
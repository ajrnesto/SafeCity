package com.safecity.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import com.safecity.Adapters.ReportAdapter;
import com.safecity.Objects.Report;
import com.safecity.R;

import java.util.ArrayList;

public class ReportsFragment extends Fragment implements ReportAdapter.OnReportListener {

    private FirebaseDatabase SAFECITY;
    private FirebaseUser USER;
    private ValueEventListener velReport;
    private Query qryReports;

    private void initializeFirebase() {
        SAFECITY = FirebaseDatabase.getInstance();
        USER = FirebaseAuth.getInstance().getCurrentUser();
    }

    View view;
    RecyclerView rvReports;
    CircularProgressIndicator loadingBar;
    ChipGroup cgReportClasses;

    ArrayList<Report> arrReports;
    ReportAdapter reportAdapter;
    ReportAdapter.OnReportListener onReportListener = this;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_report, container, false);

        initializeFirebase();
        initialize();

        cgReportClasses.setOnCheckedStateChangeListener((group, checkedIds) -> {
            loadReports(checkedIds.get(0));
        });

        return view;
    }

    private void initialize() {
        rvReports = view.findViewById(R.id.rvReports);
        loadingBar = view.findViewById(R.id.loadingBar);
        cgReportClasses = view.findViewById(R.id.cgReportClasses);

        loadReports(R.id.chipPolice);
    }

    private void loadReports(int checkedId) {
        arrReports = new ArrayList<>();
        rvReports = view.findViewById(R.id.rvReports);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        rvReports.setLayoutManager(linearLayoutManager);

        DatabaseReference dbInbox;


        dbInbox = SAFECITY.getReference("reports");
        dbInbox.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                arrReports.clear();

                int selectedClass = 0;

                if (checkedId == R.id.chipPolice){
                    selectedClass = 1;
                }

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Report report = dataSnapshot.getValue(Report.class);
                    if (report.getClasss() == selectedClass && report.getUserUid().equals(USER.getUid())) {
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
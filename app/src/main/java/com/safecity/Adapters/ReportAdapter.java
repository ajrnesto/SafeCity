package com.safecity.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.safecity.Objects.Report;
import com.safecity.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Objects;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.reportViewHolder>{

    private static final FirebaseDatabase FIXCARE_DB = FirebaseDatabase.getInstance();
    private static final FirebaseUser USER = FirebaseAuth.getInstance().getCurrentUser();

    Context context;
    ArrayList<Report> arrReport = new ArrayList<>();
    private ReportAdapter.OnReportListener mOnReportListener;
    boolean userIsMechanic;

    public ReportAdapter(Context context, ArrayList<Report> arrReport, ReportAdapter.OnReportListener onReportListener) {
        this.context = context;
        this.arrReport = arrReport;
        this.mOnReportListener = onReportListener;
        this.userIsMechanic = userIsMechanic;
    }

    @NonNull
    @Override
    public ReportAdapter.reportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cardview_report, parent, false);
        return new ReportAdapter.reportViewHolder(view, mOnReportListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportAdapter.reportViewHolder holder, int position) {
        Report report = arrReport.get(position);

        String type = report.getType();
        String details = report.getDetails();
        long timestamp = report.getTimestamp();

        holder.tvReportType.setText(type);
        holder.tvReportDetails.setText(details);

        SimpleDateFormat sdfDate = new SimpleDateFormat("MM/dd/yy");
        SimpleDateFormat sdfTime = new SimpleDateFormat("hh:mm aa");
        holder.tvTimestamp.setText(sdfDate.format(timestamp) + " - " + sdfTime.format(timestamp));
    }

    @Override
    public int getItemCount() {
        return arrReport.size();
    }

    public class reportViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView tvReportType, tvTimestamp, tvReportDetails;
        ReportAdapter.OnReportListener onReportListener;

        public reportViewHolder(@NonNull View itemView, ReportAdapter.OnReportListener onReportListener) {
            super(itemView);
            tvReportType = itemView.findViewById(R.id.tvReportType);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvReportDetails = itemView.findViewById(R.id.tvReportDetails);

            this.onReportListener = onReportListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onReportListener.onReportClick(getAdapterPosition());
        }
    }

    public interface OnReportListener{
        void onReportClick(int position);
    }
}
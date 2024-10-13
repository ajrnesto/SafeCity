package com.safecity.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.safecity.Adapters.ChatAdapter;
import com.safecity.Adapters.ReportAdapter;
import com.safecity.Objects.Chat;
import com.safecity.Objects.Report;
import com.safecity.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Objects;

public class ViewReportFragment extends Fragment implements ChatAdapter.OnChatListener{

    private FirebaseDatabase SAFECITY;
    private FirebaseUser USER;

    private void initializeFirebase() {
        SAFECITY = FirebaseDatabase.getInstance();
        USER = FirebaseAuth.getInstance().getCurrentUser();
    }

    View view;
    TextView tvType, tvTimestamp, tvDetails;
    TextInputEditText etChatBox;
    MaterialButton btnSend, btnResolved;

    RecyclerView rvChat;
    CircularProgressIndicator loadingBar;

    ArrayList<Chat> arrChat;
    ChatAdapter chatAdapter;
    ChatAdapter.OnChatListener onChatListener = this;

    String reportUid;
    String chat = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_view_report, container, false);

        initializeFirebase();
        initialize();
        loadReport();
        loadChat();

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chat = Objects.requireNonNull(etChatBox.getText()).toString().trim();
                if (chat.isEmpty()) {
                    return;
                }
                sendMessage();
                etChatBox.getText().clear();
            }

            private void sendMessage() {
                DatabaseReference dbChat = SAFECITY.getReference("report_"+reportUid+"_chat").push();
                Chat newChat = new Chat(dbChat.getKey(), chat, USER.getUid(), System.currentTimeMillis());
                dbChat.setValue(newChat);
            }
        });

        btnResolved.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // delete report
                SAFECITY.getReference("reports/"+reportUid).removeValue();
                // delete chat
                SAFECITY.getReference("report_"+reportUid+"_chat").removeValue();

                requireActivity().onBackPressed();
            }
        });

        return view;
    }

    private void loadChat() {
        arrChat = new ArrayList<>();
        rvChat = view.findViewById(R.id.rvChat);
        rvChat.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(true);
        rvChat.setLayoutManager(linearLayoutManager);

        DatabaseReference dbChat = SAFECITY.getReference("report_"+reportUid+"_chat");
        dbChat.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                arrChat.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Chat chat = dataSnapshot.getValue(Chat.class);
                    arrChat.add(chat);
                    chatAdapter.notifyDataSetChanged();
                    rvChat.scrollToPosition(arrChat.size() - 1);
                }

                loadingBar.hide();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        chatAdapter = new ChatAdapter(getContext(), arrChat, onChatListener);
        rvChat.setAdapter(chatAdapter);
        chatAdapter.notifyDataSetChanged();
    }

    private void loadReport() {
        Bundle reportArgs = getArguments();
        reportUid = reportArgs.getString("report_uid", "");
        String reportType = reportArgs.getString("report_type", "");
        String reportDetails = reportArgs.getString("report_details", "");
        long reportTimestamp = reportArgs.getLong("report_timestamp", 0);

        tvType.setText(reportType);
        tvDetails.setText(reportDetails);

        SimpleDateFormat sdfDate = new SimpleDateFormat("MM/dd/yy");
        SimpleDateFormat sdfTime = new SimpleDateFormat("hh:mm aa");
        tvTimestamp.setText(sdfDate.format(reportTimestamp) + " - " + sdfTime.format(reportTimestamp));
    }

    private void initialize() {
        tvType = view.findViewById(R.id.tvType);
        tvDetails = view.findViewById(R.id.tvDetails);
        tvTimestamp = view.findViewById(R.id.tvTimestamp);
        etChatBox = view.findViewById(R.id.etChatBox);
        btnSend = view.findViewById(R.id.btnSend);
        loadingBar = view.findViewById(R.id.loadingBar);
        btnResolved = view.findViewById(R.id.btnResolved);

        if (USER.getEmail().contains("admin")) {
            btnResolved.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onChatClick(int position) {

    }
}
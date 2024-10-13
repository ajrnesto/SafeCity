package com.safecity.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
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
import com.safecity.Objects.Chat;
import com.safecity.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Objects;

public class ViewReportDialog extends AppCompatDialogFragment implements ChatAdapter.OnChatListener {

    private FirebaseDatabase SAFECITY;
    private FirebaseUser USER;

    private void initializeFirebase() {
        SAFECITY = FirebaseDatabase.getInstance();
        USER = FirebaseAuth.getInstance().getCurrentUser();
    }

    TextView tvType2, tvTimestamp2, tvDetails2;
    TextInputEditText etChatBox2;
    MaterialButton btnSend2, btnResolved2;

    RecyclerView rvChat;
    CircularProgressIndicator loadingBar;

    ArrayList<Chat> arrChat;
    ChatAdapter chatAdapter;
    ChatAdapter.OnChatListener onChatListener = this;

    String chat = "";

    // arguments
    String reportUid, reportType, reportDetails, reportTimestamp;

    @Override
    public int getTheme() {
        return R.style.FullScreenDialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_view_report, null);

        initializeFirebase();
        initialize(view);
        loadReport();
        loadChat(view);

        btnSend2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chat = Objects.requireNonNull(etChatBox2.getText()).toString().trim();
                if (chat.isEmpty()) {
                    return;
                }
                sendMessage();
                etChatBox2.getText().clear();
            }

            private void sendMessage() {
                DatabaseReference dbChat = SAFECITY.getReference("report_"+reportUid+"_chat").push();
                Chat newChat = new Chat(dbChat.getKey(), chat, USER.getUid(), System.currentTimeMillis());
                dbChat.setValue(newChat);
            }
        });

        btnResolved2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // delete report
                SAFECITY.getReference("reports/"+reportUid).removeValue();
                // delete chat
                SAFECITY.getReference("report_"+reportUid+"_chat").removeValue();

                requireActivity().onBackPressed();
            }
        });

        builder.setView(view);
        return builder.create();
    }

    private void initialize(View view) {
        tvType2 = view.findViewById(R.id.tvType2);
        tvDetails2 = view.findViewById(R.id.tvDetails2);
        tvTimestamp2 = view.findViewById(R.id.tvTimestamp2);
        etChatBox2 = view.findViewById(R.id.etChatBox2);
        btnSend2 = view.findViewById(R.id.btnSend2);
        loadingBar = view.findViewById(R.id.loadingBar);
        btnResolved2 = view.findViewById(R.id.btnResolved2);

        if (USER.getEmail().contains("admin")) {
            btnResolved2.setVisibility(View.VISIBLE);
        }

        // retrieve argument values
        Bundle workshopArgs = getArguments();
        reportUid = Objects.requireNonNull(workshopArgs).getString("uid");
        reportType = workshopArgs.getString("report_type");
        reportDetails = workshopArgs.getString("report_details");
        reportTimestamp = workshopArgs.getString("report_timestamp");
    }

    private void loadChat(View view) {
        arrChat = new ArrayList<>();
        rvChat = view.findViewById(R.id.rvChat2);
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

        tvType2.setText(reportType);
        tvDetails2.setText(reportDetails);

        SimpleDateFormat sdfDate = new SimpleDateFormat("MM/dd/yy");
        SimpleDateFormat sdfTime = new SimpleDateFormat("hh:mm aa");
        tvTimestamp2.setText(sdfDate.format(reportTimestamp) + " - " + sdfTime.format(reportTimestamp));
    }

    @Override
    public void onChatClick(int position) {

    }
}

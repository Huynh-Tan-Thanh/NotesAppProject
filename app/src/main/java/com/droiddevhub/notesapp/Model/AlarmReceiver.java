package com.droiddevhub.notesapp.Model;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.droiddevhub.notesapp.R;
import com.droiddevhub.notesapp.databinding.ActivityAlarmReceiverBinding;

public class AlarmReceiver extends AppCompatActivity {

    private ActivityAlarmReceiverBinding binding;
    private static final int PERMISSION_REQUEST_CODE = 1234;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAlarmReceiverBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Get the intent that started this activity
        Intent intent = getIntent();
        String noteTitle = intent.getStringExtra("note_title");
        String noteContent = intent.getStringExtra("note_content");

        // Display note information
        binding.noteTitleTextView.setText(noteTitle);
        binding.noteContentTextView.setText(noteContent);

        // Dismiss button action
        binding.dismissbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Dismiss the notification
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(1001); // The notification ID should match with the one used when creating the notification
                finish(); // Close the activity
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }
}

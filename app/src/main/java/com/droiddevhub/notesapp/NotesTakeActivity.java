package com.droiddevhub.notesapp;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.EditText;

import com.droiddevhub.notesapp.Model.AlarmReceiver;
import com.droiddevhub.notesapp.Model.Notes;
import com.droiddevhub.notesapp.databinding.ActivityNotesTakeBinding;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class NotesTakeActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_SPEECH_INPUT = 1000;
    private ActivityNotesTakeBinding binding;
    Notes notes;
    boolean isOldNotes = false;
    ActivityResultLauncher<Intent> launcher;
    boolean openCam;
    private Uri imageUri;  // Biến này để lưu URI của ảnh

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(getResources().getColor(R.color.black));
        binding = ActivityNotesTakeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        notes = new Notes();
        try {
            notes = (Notes) getIntent().getSerializableExtra("old_notes");
            binding.titleEdt.setText(notes.getTitle());
            binding.noteEdt.setText(notes.getNotes());
            isOldNotes = true;

            // Kiểm tra và hiển thị ảnh nếu có URI ảnh trong note cũ
            if (notes.getImageUri() != null && !notes.getImageUri().isEmpty()) {
                imageUri = Uri.parse(notes.getImageUri());
                binding.noteEdt.post(() -> insertImageIntoEditText(binding.noteEdt, imageUri));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), results -> {
            if (results.getResultCode() == RESULT_OK && results.getData() != null) {
                if (openCam) {
                    Bitmap bitmap = (Bitmap) results.getData().getExtras().get("data");
                    assert bitmap != null;

                    // Lưu ảnh dưới dạng URI trong Note
                    imageUri = getImageUri(bitmap);
                    insertImageIntoEditText(binding.noteEdt, imageUri);  // Chèn ảnh vào EditText
                } else {
                    imageUri = results.getData().getData();
                    insertImageIntoEditText(binding.noteEdt, imageUri);  // Chèn ảnh từ URI vào EditText
                }
            }
        });

        binding.savebtn.setOnClickListener(view -> saveNote());

        binding.backbtn.setOnClickListener(view -> onBackPressed());

        binding.btnMic.setOnClickListener(v -> startSpeechToText());

        binding.btnAddPhoto.setOnClickListener(view -> showPhotoDialog());

        binding.btnRemider.setOnClickListener(view -> showDateTimePickerDialog());
    }

    private void saveNote() {
        String title = binding.titleEdt.getText().toString();
        String description = binding.noteEdt.getText().toString();

        if (description.isEmpty()) {
            Toast.makeText(NotesTakeActivity.this, "Please enter the description", Toast.LENGTH_SHORT).show();
            return;
        }

        SimpleDateFormat format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm a", Locale.getDefault());
        Date date = new Date();

        if (!isOldNotes) {
            notes = new Notes();
        }

        notes.setTitle(title);
        notes.setNotes(description);
        notes.setDate(format.format(date));

        // Lưu URI của ảnh vào Note
        if (imageUri != null) {
            notes.setImageUri(imageUri.toString());
        }

        Intent intent = new Intent();
        intent.putExtra("note", notes);
        setResult(Activity.RESULT_OK, intent);

        finish();
    }

    private void startSpeechToText() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something...");

        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(this, "Your device does not support speech-to-text", Toast.LENGTH_SHORT).show();
        }
    }

    private void showPhotoDialog() {
        Dialog dialog = new Dialog(NotesTakeActivity.this);
        dialog.setContentView(R.layout.custom_add_photo_dialog);

        LinearLayout camera = dialog.findViewById(R.id.layout_camera);
        camera.setOnClickListener(v -> {
            openCam = true;
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            launcher.launch(intent);
            dialog.dismiss();
        });

        LinearLayout gallery = dialog.findViewById(R.id.layout_gallery);
        gallery.setOnClickListener(v -> {
            openCam = false;
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            launcher.launch(intent);
            dialog.dismiss();
        });

        Button cancelPhoto = dialog.findViewById(R.id.btnCancleAddPhoto);
        cancelPhoto.setOnClickListener(v -> dialog.dismiss());

        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setGravity(Gravity.BOTTOM);
        dialog.show();
    }

    private void showDateTimePickerDialog() {
        final Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                NotesTakeActivity.this, (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    NotesTakeActivity.this, (view1, hourOfDay, minute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);

                setReminder(calendar);
            },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
            );
            timePickerDialog.show();
        },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void setReminder(Calendar calendar) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        // Set the alarm to trigger at the chosen time
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

        Toast.makeText(this, "Reminder set!", Toast.LENGTH_SHORT).show();
    }

    private Uri getImageUri(Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "Title", null);
        return Uri.parse(path);
    }

    private void insertImageIntoEditText(EditText editText, Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            insertImageIntoEditText(editText, bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void insertImageIntoEditText(EditText editText, Bitmap bitmap) {
        if (bitmap == null) {
            Log.e("InsertImage", "Bitmap is null");
            Toast.makeText(this, "Bitmap is null", Toast.LENGTH_SHORT).show();
            return;
        }

        if (editText == null) {
            Log.e("InsertImage", "EditText is null");
            Toast.makeText(this, "EditText is null", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo đối tượng ImageSpan với hình ảnh bitmap
        ImageSpan imageSpan = new ImageSpan(this, bitmap);

        // Tạo một đối tượng Spannable để chứa văn bản và hình ảnh
        Spannable spannable = editText.getText();

        // Xác định vị trí chèn hình ảnh
        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();

        // Kiểm tra vị trí hợp lệ
        if (start < 0 || end < 0 || start > end) {
            Log.e("InsertImage", "Invalid cursor position: start=" + start + ", end=" + end);
            Toast.makeText(this, "Invalid cursor position", Toast.LENGTH_SHORT).show();
            return;
        }

        // Chèn hình ảnh vào Spannable tại vị trí hiện tại
        spannable.setSpan(imageSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Đặt lại văn bản của EditText với các thay đổi
        editText.setText(spannable);
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SPEECH_INPUT && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String spokenText = result.get(0);

            String currentText = binding.noteEdt.getText().toString();
            String updatedText = currentText + " " + spokenText;
            binding.noteEdt.setText(updatedText);
        }
    }
}

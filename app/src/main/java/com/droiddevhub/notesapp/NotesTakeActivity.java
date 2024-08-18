package com.droiddevhub.notesapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.droiddevhub.notesapp.Model.Notes;
import com.droiddevhub.notesapp.databinding.ActivityNotesTakeBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class NotesTakeActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_SPEECH_INPUT = 1000;
    private ActivityNotesTakeBinding binding;
    Notes notes;
    boolean isOldNotes = false;


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
        } catch (Exception e) {
            e.printStackTrace();
        }

        binding.savebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = binding.titleEdt.getText().toString();
                String description = binding.noteEdt.getText().toString();

                if (description.isEmpty()) {
                    Toast.makeText(NotesTakeActivity.this, "Please enter the description", Toast.LENGTH_SHORT).show();
                    return;
                }

                SimpleDateFormat format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm a");
                Date date = new Date();
                if (!isOldNotes) {
                    notes = new Notes();
                }

                notes.setTitle(title);
                notes.setNotes(description);
                notes.setDate(format.format(date));

                Intent intent = new Intent();
                intent.putExtra("note", notes);
                setResult(Activity.RESULT_OK, intent);

                finish();
            }
        });

        binding.btnMic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSpeechToText();
            }
        });

        binding.backbtn.setOnClickListener(new View.OnClickListener() { //còn cần chỉnh sửa thêm
            @Override
            public void onClick(View view) {
                String title = binding.titleEdt.getText().toString();
                String description = binding.noteEdt.getText().toString();

                if (description.isEmpty()) {
                    Toast.makeText(NotesTakeActivity.this, "Please enter the description", Toast.LENGTH_SHORT).show();
                    return;
                }

                SimpleDateFormat format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm a");
                Date date = new Date();
                if (!isOldNotes) {
                    notes = new Notes();
                }

                notes.setTitle(title);
                notes.setNotes(description);
                notes.setDate(format.format(date));

                Intent intent = new Intent();
                intent.putExtra("note", notes);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });

        //thiet lap dialog cho add_photo
        binding.btnAddPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog dialog = new Dialog(NotesTakeActivity.this);
                dialog.setContentView(R.layout.custom_add_photo_dialog);

                LinearLayout camera = dialog.findViewById(R.id.layout_camera);
                camera.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        //mở camera
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivity(intent);
                        dialog.dismiss();
                    }
                });

                LinearLayout gallery = dialog.findViewById(R.id.layout_gallery);
                gallery.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(NotesTakeActivity.this, "Open Photo Gallery", Toast.LENGTH_LONG).show();
                    }
                });

                // setting cancle add photo
                Button canclePhoto = dialog.findViewById(R.id.btnCancleAddPhoto);
                canclePhoto.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss(); //đóng dialog
                    }
                });

                //hien thi dialog o duoi
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.getWindow().setGravity(Gravity.BOTTOM);
                dialog.show();
            }
        });



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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SPEECH_INPUT && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String spokenText = result.get(0);

            // Lấy nội dung hiện tại trong EditText
            String currentText = binding.noteEdt.getText().toString();

            // Nối tiếp văn bản mới vào nội dung hiện tại
            String updatedText = currentText + " " + spokenText;

            // Đặt văn bản đã nối tiếp vào EditText
            binding.noteEdt.setText(updatedText);
        }
    }
}

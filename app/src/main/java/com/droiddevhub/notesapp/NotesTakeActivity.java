package com.droiddevhub.notesapp;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.droiddevhub.notesapp.Model.Notes;
import com.droiddevhub.notesapp.databinding.ActivityNotesTakeBinding;

import java.util.ArrayList;
import java.util.Locale;

public class NotesTakeActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_SPEECH_INPUT = 1000;
    private ActivityNotesTakeBinding binding;
    private Notes oldNotes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotesTakeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getIntent().hasExtra("old_notes")) {
            oldNotes = (Notes) getIntent().getSerializableExtra("old_notes");
            populateFields();
        } else {
            oldNotes = new Notes();
        }

        binding.savebtn.setOnClickListener(v -> saveNote());
        binding.micbtn.setOnClickListener(v -> startSpeechToText());
    }

    private void populateFields() {
        binding.titleEdt.setText(oldNotes.getTitle());
        binding.noteEdt.setText(oldNotes.getNotes());
    }

    private void saveNote() {
        String title = binding.titleEdt.getText().toString().trim();
        String notes = binding.noteEdt.getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(notes)) {
            if (TextUtils.isEmpty(title)) {
                binding.titleEdt.setError("Title is required");
            }
            if (TextUtils.isEmpty(notes)) {
                binding.noteEdt.setError("Notes are required");
            }
            return;
        }

        oldNotes.setTitle(title);
        oldNotes.setNotes(notes);

        Intent resultIntent = new Intent();
        resultIntent.putExtra("note", oldNotes);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    private void startSpeechToText() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...");

        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(this, "Speech recognition is not supported on this device", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SPEECH_INPUT && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                String spokenText = result.get(0);
                String currentText = binding.noteEdt.getText().toString();
                // Nối văn bản mới vào cuối văn bản hiện tại
                binding.noteEdt.setText(currentText + " " + spokenText);
            }
        }
    }
}

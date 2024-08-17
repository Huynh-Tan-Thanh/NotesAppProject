package com.droiddevhub.notesapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.droiddevhub.notesapp.Adapter.NoteListAdapter;
import com.droiddevhub.notesapp.Database.RoomDb;
import com.droiddevhub.notesapp.Interface.NotesClickListener;
import com.droiddevhub.notesapp.Model.Notes;
import com.droiddevhub.notesapp.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {

    private ActivityMainBinding binding;
    private NoteListAdapter noteListAdapter;
    private RoomDb database;
    private List<Notes> notes = new ArrayList<>();
    private Notes selectedNotes;

    private static final int ADD_NOTE_REQUEST_CODE = 101;
    private static final int EDIT_NOTE_REQUEST_CODE = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = RoomDb.getInstance(this);

        notes = database.mainDAO().getAll();
        updateRecycler(notes);

        binding.addBtn.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, NotesTakeActivity.class);
            startActivityForResult(intent, ADD_NOTE_REQUEST_CODE);
        });

        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return true;
            }
        });
    }

    private void filter(String newtext) {
        List<Notes> filterList = new ArrayList<>();
        for (Notes singleNote : notes) {
            if (singleNote.getTitle().toLowerCase().contains(newtext.toLowerCase())
                    || singleNote.getNotes().toLowerCase().contains(newtext.toLowerCase())) {
                filterList.add(singleNote);
            }
        }
        noteListAdapter.filterList(filterList);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_NOTE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Notes newNotes = (Notes) data.getSerializableExtra("note");
            database.mainDAO().insert(newNotes);
            notes.clear();
            notes.addAll(database.mainDAO().getAll());
            noteListAdapter.notifyDataSetChanged();
        } else if (requestCode == EDIT_NOTE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Notes updatedNotes = (Notes) data.getSerializableExtra("note");
            database.mainDAO().update(updatedNotes.getID(), updatedNotes.getTitle(), updatedNotes.getNotes());
            notes.clear();
            notes.addAll(database.mainDAO().getAll());
            noteListAdapter.notifyDataSetChanged();
        }
    }

    private void updateRecycler(List<Notes> notes) {
        binding.noteRv.setHasFixedSize(true);
        binding.noteRv.setLayoutManager(new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL));
        noteListAdapter = new NoteListAdapter(MainActivity.this, notes, notesClickListener);
        binding.noteRv.setAdapter(noteListAdapter);
    }

    private final NotesClickListener notesClickListener = new NotesClickListener() {
        @Override
        public void onClick(Notes notes) {
            Intent intent = new Intent(MainActivity.this, NotesTakeActivity.class);
            intent.putExtra("old_notes", notes);
            startActivityForResult(intent, EDIT_NOTE_REQUEST_CODE);
        }

        @Override
        public void onLongPress(Notes notes, CardView cardView) {
            selectedNotes = notes;
            showPopupMenu(cardView);
        }
    };

    private void showPopupMenu(CardView cardView) {
        PopupMenu popupMenu = new PopupMenu(this, cardView);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.inflate(R.menu.popup_menu);
        popupMenu.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.pin) {
            if (selectedNotes.getPinned()) {
                database.mainDAO().pin(selectedNotes.getID(), false);
                Toast.makeText(this, "UnPinned", Toast.LENGTH_SHORT).show();
            } else {
                database.mainDAO().pin(selectedNotes.getID(), true);
                Toast.makeText(this, "Pinned", Toast.LENGTH_SHORT).show();
            }
            notes.clear();
            notes.addAll(database.mainDAO().getAll());
            noteListAdapter.notifyDataSetChanged();
            return true;
        } else if (item.getItemId() == R.id.delete) {
            database.mainDAO().delete(selectedNotes);
            notes.remove(selectedNotes);
            noteListAdapter.notifyDataSetChanged();
            Toast.makeText(this, "Note is Deleted successfully..", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }
}

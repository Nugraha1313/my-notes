package com.example.notes.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.notes.R;
import com.example.notes.activities.CreateNoteActivity;
import com.example.notes.adapters.NotesAdapter;
import com.example.notes.database.NotesDatabase;
import com.example.notes.entities.Note;
import com.example.notes.listeners.NotesListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NotesListener {

//    request code nantinya akan digunakan ngepass value, sehingga bisa digunakan sbg perkondisian
    public static final int REQUEST_CODE_ADD_NOTE = 1,
                            REQUEST_CODE_UPDATE_NOTE = 2,
                            REQUEST_CODE_SHOW_NOTES = 3;

    private RecyclerView notesRecyclerView;
    private List<Note> noteList;
    private NotesAdapter notesAdapter;
    private int noteClickedPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        agar ketika button plus di home ditekan maka ke CreateNote Activity
        ImageView imageAddNoteMain = findViewById(R.id.imageAddNoteMain);
        imageAddNoteMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(
                    new Intent(getApplicationContext(), CreateNoteActivity.class),
                    REQUEST_CODE_ADD_NOTE
                );
            }
        });

//      untuk tampilan daftar list notes
        notesRecyclerView = findViewById(R.id.notesRecyclerView);
        notesRecyclerView.setLayoutManager(
//                buatlah tampilan dengan 2 kolom, secara vertikal
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        );

//        note List adalah kumpulan notes
        noteList = new ArrayList<>();
//        noteadapter akan memakai data dari notelist
        notesAdapter = new NotesAdapter(noteList, this);
//        recyclerView membutuhkan adapter untuk menampilkan data notes ke RecyclerView
        notesRecyclerView.setAdapter(notesAdapter);

//        ini untuk display notes di main activity
        getNotes(REQUEST_CODE_SHOW_NOTES, false);

//        untuk search
        EditText inputSearch = findViewById(R.id.inputSearch);
        inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                notesAdapter.cancelTimer();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(noteList.size() != 0) {
                    notesAdapter.searchNotes(editable.toString());
                }
            }
        });

    }

//    ketika note diclick / maka tampilkan
    @Override
    public void onNoteClicked(Note note, int position) {
        noteClickedPosition = position;
        Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
        intent.putExtra("isViewOrUpdate", true);
        intent.putExtra("note", note);
        startActivityForResult(intent, REQUEST_CODE_UPDATE_NOTE);

    }

//    dapetin notes
    private void getNotes(final int requestCode, final boolean isNoteDeleted) {

        @SuppressLint("StaticFieldLeak")
        class GetNotesTask extends AsyncTask<Void, Void, List<Note>> {

//            dibackground lakukan get data note dari database
            @Override
            protected List<Note> doInBackground(Void... voids) {
                return NotesDatabase.getDatabase(getApplicationContext()).noteDao().getAllNotes();
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            protected void onPostExecute(List<Note> notes) {
                super.onPostExecute(notes);
//                Log.d("MY_NOTES", notes.toString());
//                kita cek jika note list empty
//                if(noteList.size() == 0) {
//                    noteList.addAll(notes);
//                    notesAdapter.notifyDataSetChanged();
//                }
//                else {
//                    noteList.add(0, notes.get(0));
//                    notesAdapter.notifyItemInserted(0);
//                }
//                notesRecyclerView.smoothScrollToPosition(0);

//                perkondisian get Notes
                if(requestCode == REQUEST_CODE_SHOW_NOTES) {
                    noteList.addAll(notes);
                    notesAdapter.notifyDataSetChanged();
                } else if(requestCode == REQUEST_CODE_ADD_NOTE) {
//                    maka hanya tambahkan noteList ke posisi awal / paling atas / terbaru
                    noteList.add(0, notes.get(0));
                    notesAdapter.notifyItemInserted(0);
                    notesRecyclerView.smoothScrollToPosition(0);
                } else if(requestCode == REQUEST_CODE_UPDATE_NOTE) {
                    noteList.remove(noteClickedPosition);
//                    untuk delete
                    if(isNoteDeleted) {
                        notesAdapter.notifyItemRemoved(noteClickedPosition);
                    }
//                    untuk update
                    else {
                        noteList.add(noteClickedPosition, notes.get(noteClickedPosition));
                        notesAdapter.notifyItemChanged(noteClickedPosition);
                    }

                }
            }

        }
//        mengeksekusi task untuk menampilkan notes dari db
        new GetNotesTask().execute();
    }


// Ngehandle result untuk update noteList setelah ditambahkan note baru dari CreateNoteActivity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//    perkondisian agar ketika sudah add note dan balik ke main activity maka akan getNotes dari database
//    dalam artian get new notes
        if(requestCode == REQUEST_CODE_ADD_NOTE && resultCode == RESULT_OK) {
            getNotes(REQUEST_CODE_ADD_NOTE, false);
        }
//        karna ini update note yang sudah ada, maka ada kemungkinan note didelete
        else if(requestCode == REQUEST_CODE_UPDATE_NOTE && resultCode == RESULT_OK) {
//            kalo data tidak null bearti update
            if(data != null) {
                getNotes(REQUEST_CODE_UPDATE_NOTE, data.getBooleanExtra("isNoteDeleted", false));
            }
        }
    }
}
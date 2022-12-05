package com.example.notes.adapters;

import android.annotation.SuppressLint;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notes.R;
import com.example.notes.entities.Note;
import com.example.notes.listeners.NotesListener;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

// adapeter /adaptor menghubungkan data (notes) ke RecyclerView
public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder>{

    private List<Note> notes;
    private NotesListener notesListener;
//    untuk search note
    private Timer timer;
    private List<Note> notesSource;

    public NotesAdapter(List<Note> notes, NotesListener notesListener) {
        this.notes = notes;
        this.notesListener = notesListener;
        notesSource = notes;
    }

//    untuk bikin viewHolder Baru
    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NoteViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.item_container_note,parent,false
                )
        );
    }

// untuk updadte viewHolder Content ketika di scroll sesuai posisi
//    ketika Note ditekan, maka tampilkan notes
    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.setNote(notes.get(position));
        holder.layoutNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notesListener.onNoteClicked(notes.get(position), position);
            }
        });
    }

//    untuk tau berapa banyak notes
    @Override
    public int getItemCount() {
        return notes.size();
    }

//    mengetahui possisi notes
    @Override
    public int getItemViewType(int position) {
        return position;
    }


//    view holder extends kelas ViewHolder. Ini berisi informasi tampilan untuk menampilkan satu
//    item dari layout item
    static class NoteViewHolder extends RecyclerView.ViewHolder {

        TextView textTitle, textSubtitle, textDateTime;
        LinearLayout layoutNote;
        RoundedImageView imageNote;

//        Konstruktor
        NoteViewHolder(@NonNull View itemView) {
            super(itemView);
//            define notes yang akan ditampilakn
            textTitle = itemView.findViewById(R.id.textTitle);
            textSubtitle = itemView.findViewById(R.id.textSubtitle);
            textDateTime = itemView.findViewById(R.id.textDateTime);
            layoutNote = itemView.findViewById(R.id.layoutNote);
            imageNote = itemView.findViewById(R.id.imageNote);
        }

//        untuk data yang ditampilkan
        void setNote(Note note) {
//            title
            textTitle.setText(note.getTitle());
//            subtitle
            if(note.getSubtitle().trim().isEmpty()) {
//                jika kosong / tidak ada maka visibility di hilangkan
                textSubtitle.setVisibility(View.GONE);
            } else {
                textSubtitle.setText(note.getSubtitle());
            }
//            textDateTime (waktu)
            textDateTime.setText(note.getDateTime());
//            ini untuk background notes (warna notes)
            GradientDrawable gradientDrawable = (GradientDrawable) layoutNote.getBackground();
//            jika color note bukan null, dalam artian, user menyetting warna notes
//            maka set color notes menjadi color tersebut
            if(note.getColor() != null) {
                gradientDrawable.setColor(Color.parseColor(note.getColor()));
            }
            else {
//               jika user tidak menyeting color notes, maka set default
                gradientDrawable.setColor(Color.parseColor("#333333"));
            }

//            untuk gambar
//            jika notes memiliki image, yang dibuktikan bahwa imagepath tidak null
            if(note.getImagePath() != null) {
//                maka tampilkan image tersebut
                imageNote.setImageBitmap(BitmapFactory.decodeFile(note.getImagePath()));
                imageNote.setVisibility(View.VISIBLE);
            }
            else {
//                jika tidak maka jangan ditampilkan imageNote
                imageNote.setVisibility(View.GONE);
            }

        }

    }

//    untuk search
    public void searchNotes(final String searchKeyword) {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(searchKeyword.trim().isEmpty()) {
                    notes = notesSource;
                } else {
                    ArrayList<Note> temp = new ArrayList<>();
                    for (Note note : notesSource) {
                        if(note.getTitle().toLowerCase().contains(searchKeyword.toLowerCase())
                           || note.getSubtitle().toLowerCase().contains(searchKeyword.toLowerCase())
                           || note.getNoteText().toLowerCase().contains(searchKeyword.toLowerCase())
                        ) {
                            temp.add(note);
                        }
                    }
                    notes = temp;
                }
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        notifyDataSetChanged();
                    }
                });
            }
        }, 500);
    }

    public void cancelTimer() {
        if(timer != null) {
            timer.cancel();
        }
    }


}

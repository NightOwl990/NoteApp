package com.example.note.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.note.Interface.IClickItem;
import com.example.note.R;
import com.example.note.model.Note;

import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.ViewHolder>{
    private Context context;
    private List<Note> mList;
    private IClickItem iClickItem;
    private int textColor = Color.BLACK;
    private int itemColor = Color.WHITE;

    public NoteAdapter(Context context, List<Note> mList, IClickItem iClickItem) {
        this.context = context;
        this.mList = mList;
        this.iClickItem = iClickItem;
    }
    public void setTextColor(int color) {
        textColor = color;
        notifyDataSetChanged();
    }
    public void setItemColor(int color){
        itemColor = color;
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dong_note, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Note note = mList.get(position);
        holder.tvNoiDung.setText(note.getNoidung());
        holder.tvNoiDung.setTextColor(textColor);
        holder.itemLayout.setBackgroundColor(itemColor);

        holder.itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                iClickItem.onItemClickNote(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (mList == null){
            return 0;
        } else return mList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView tvNoiDung;
        public LinearLayout itemLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNoiDung = itemView.findViewById(R.id.tv_noi_dung);
            itemLayout = itemView.findViewById(R.id.item_layout);
        }
    }

    public void removeItem(int index){
        mList.remove(index);
        notifyItemRemoved(index);
    }

    public void undoItem(Note note, int index){
//        NoteDatabase database = new NoteDatabase(context);
//        database.insertNote(note);
        mList.add(index, note);
        notifyItemInserted(index);
    }
}

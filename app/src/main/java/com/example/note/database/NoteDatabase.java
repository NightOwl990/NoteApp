package com.example.note.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.example.note.model.Note;

import java.util.ArrayList;
import java.util.List;

public class NoteDatabase extends SQLiteOpenHelper {
    public static final String DATABASE_TABLE = "note.db";
    public static final String TABLE_NAME = "note";
    public static final int VERSON_APP = 1;
    public NoteDatabase(@Nullable Context context) {
        super(context, DATABASE_TABLE, null, VERSON_APP);
    }

//    private String SQLquery = "INSERT INTO note VAlUES (null, 'Tạo ghi chú đầu tiên của bạn')";

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String query = "CREATE TABLE " + TABLE_NAME + " ( " +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "noidung TEXT" + ")";
        sqLiteDatabase.execSQL(query);
//        sqLiteDatabase.execSQL(SQLquery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public List<Note> getAllData(){
        List<Note> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * from note ORDER BY id DESC", null);   // lấy ra tất cả dữ liệu theo thứ tự giảm dần id
        cursor.moveToFirst();
        while (!cursor.isAfterLast()){
            int idNote = cursor.getInt(0);
            String noidungNote = cursor.getString(1);
            list.add(new Note(idNote, noidungNote));
            cursor.moveToNext();
        }
        cursor.close();
        return list;
    }

    public Note getIdNote(int ID){
        Note note = null;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * from note where id = ?", new String[]{ID + ""});

        if (cursor.getCount() > 0){
            cursor.moveToFirst();
            int idNote = cursor.getInt(0);
            String noidungNote = cursor.getString(1);
            note = new Note(idNote, noidungNote);
        }
        cursor.close();
        return note;
    }

    public void updateNote(Note note){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("UPDATE note SET noidung=? where id=?",
                new String[]{note.getNoidung(), note.getId() + ""});
    }
    public void insertNote(Note note){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("INSERT INTO note (noidung) VALUES (?)",
                new String[]{note.getNoidung()});
    }

    public void deleteNote(int idNote){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM note where id=?",
                new String[]{String.valueOf(idNote)});
    }
}

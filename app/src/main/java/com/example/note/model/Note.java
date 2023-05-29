package com.example.note.model;

public class Note {
    public int id;
    public String noidung;

    public Note(int id, String noidung) {
        this.id = id;
        this.noidung = noidung;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNoidung() {
        return noidung;
    }

    public void setNoidung(String noidung) {
        this.noidung = noidung;
    }
}

package com.example.note;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.ShareActionProvider;
import android.widget.Toast;

import com.example.note.Interface.UndoRedoHelper;
import com.example.note.database.NoteDatabase;
import com.example.note.model.Note;

public class DetailActivity extends AppCompatActivity {

    private Toolbar toolbarDetail;
    boolean isupdate;
    int idnote;
    private EditText edtDetail;
    private Note note;
    private NoteDatabase database;
    private MenuItem itemDelete, itemUndo, itemRedo, itemShare;
    private String mPreviousNote = "";
    private int idNoteCurrent = 0, count = 0;
    private boolean isSwitch;
    private RelativeLayout layoutDetail;

    UndoRedoHelper undoRedoHelper;
    private SharedPreferences mSharedPreferences;
    private static final String UNDO_REDO_KEY = "undo_redo_key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        InitView();
        Event();
        Log.e("status", "----------Detail: onCreate");
    }

    private void Event() {
        toolbarDetail.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        edtDetail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Không làm gì ở đây
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Lưu trữ nội dung ghi chú trước đó vào biến mPreviousNote
                mPreviousNote = s.toString();
                // Khi editText thay đổi thì cho phép itemUndo (hoàn tác)
                itemUndo.setIcon(R.drawable.ic_undo);
                itemUndo.setEnabled(true);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Không làm gì ở đây
            }
        });

    }

    private void InitView() {
        toolbarDetail = findViewById(R.id.toolbar_detail);
        setSupportActionBar(toolbarDetail);
        toolbarDetail.setNavigationIcon(R.drawable.ic_back);
        edtDetail = findViewById(R.id.edt_detail);
        layoutDetail = findViewById(R.id.layout_detail);
        database = new NoteDatabase(this);
        Intent intent = getIntent();
        isSwitch = intent.getBooleanExtra("isswitch", false);
        isupdate = intent.getBooleanExtra("isupdate", false);
        if (isupdate){
            idnote = intent.getIntExtra("idnote", 0);
            note = database.getIdNote(idnote);
            Log.e("idnote", "--------id: " + idnote);

        } else {
            // Nếu là thêm mới dữ liệu
            note = new Note(0, "");
            edtDetail.requestFocus();       // Hiển thị con trỏ

        }
        edtDetail.setText(note.getNoidung());

        // Khai báo UndoRedoHelper
        undoRedoHelper = new UndoRedoHelper(edtDetail);
        undoRedoHelper.setMaxHistorySize(20);
        mSharedPreferences = getSharedPreferences("UndoRedo", MODE_PRIVATE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_action, menu);
        itemDelete = menu.findItem(R.id.item_delte);
        itemUndo = menu.findItem(R.id.item_undo);
        itemRedo = menu.findItem(R.id.item_redo);
        itemShare = menu.findItem(R.id.item_share);

        // Khởi tạo ban đầu: Không cho phép itemUndo và itemRedo
        itemUndo.setIcon(R.drawable.ic_undo_gray);
        itemUndo.setEnabled(false);
        itemRedo.setIcon(R.drawable.ic_redo_gray);
        itemRedo.setEnabled(false);

        if (!isupdate){
            itemDelete.setVisible(false);
        }
        itemUndo.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem menuItem) {
                if (undoRedoHelper.getCanUndo()){
                    itemRedo.setIcon(R.drawable.ic_redo);
                    itemRedo.setEnabled(true);
                    undoRedoHelper.undo();
                    SharedPreferences.Editor editor = mSharedPreferences.edit();
                    editor.putString(UNDO_REDO_KEY, mPreviousNote);
                    editor.apply();
                    undoRedoHelper.storePersistentState(editor, mPreviousNote);
                } else {
                    itemUndo.setIcon(R.drawable.ic_undo_gray);
                    itemUndo.setEnabled(false);
                }
                return true;
            }
        });

        itemRedo.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem menuItem) {
                if (undoRedoHelper.getCanRedo()){
                    itemUndo.setIcon(R.drawable.ic_undo);
                    itemUndo.setEnabled(true);
                    undoRedoHelper.redo();
                    SharedPreferences.Editor editor = mSharedPreferences.edit();
                    editor.putString(UNDO_REDO_KEY, mPreviousNote);
                    editor.apply();
                    undoRedoHelper.restorePersistentState(mSharedPreferences, mPreviousNote);
                } else {
                    itemRedo.setIcon(R.drawable.ic_redo_gray);
                    itemRedo.setEnabled(false);
                }
                return true;
            }
        });

        itemShare.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem menuItem) {
                //create the sharing intent
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "App: Ghi chú");
//                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Đường link ứng dụng trên CH Play");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, edtDetail.getText().toString());
                startActivity(Intent.createChooser(sharingIntent, "Chia sẻ ứng dụng"));
                return true;
            }
        });
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.item_delte:
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setTitle("Xóa");
                alertDialogBuilder.setMessage("Bạn có chắc chắn muốn xóa ghi chú?");
                alertDialogBuilder.setPositiveButton("Xóa", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        database.deleteNote(idnote);
                        finish();
                        Toast.makeText(DetailActivity.this, "Đã xóa ghi chú", Toast.LENGTH_SHORT).show();
                    }
                }).setNegativeButton("Hủy", null).show();
                return true;

            default:
                return false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        idNoteCurrent = 1;
        count++;
        note.noidung = edtDetail.getText().toString().trim();
        if (!note.noidung.equals("")){
            if (isupdate) {
                if (note.getNoidung().equals(mPreviousNote)){
                    database.updateNote(note);
                    Toast toast = Toast.makeText(getApplicationContext(), "Ghi chú đã được lưu lại", Toast.LENGTH_SHORT);
                    toast.show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            toast.cancel();
                        }
                    }, 800);
                }

            } else {
                if (idNoteCurrent == count){
                    database.insertNote(note);
                    count++;
                    Toast toast = Toast.makeText(getApplicationContext(), "Ghi chú đã được lưu lại", Toast.LENGTH_SHORT);
                    toast.show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            toast.cancel();
                        }
                    }, 800);
                }
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (isSwitch){
            edtDetail.setTextColor(Color.WHITE);
            layoutDetail.setBackgroundColor(Color.BLACK);
//            toolbarDetail.setBackgroundColor(Color.GREEN);
        } else {
            edtDetail.setTextColor(Color.BLACK);
            layoutDetail.setBackgroundColor(Color.WHITE);
//            toolbarDetail.setBackgroundColor(Color.YELLOW);
        }
        Log.e("status", "----------Detail: onStart");
    }
}
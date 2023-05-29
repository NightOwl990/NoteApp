package com.example.note;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.note.Interface.IClickItem;
import com.example.note.Interface.ItemTouchHelperListener;
import com.example.note.Interface.RecyclerViewItemTouchHelper;
import com.example.note.adapter.NoteAdapter;
import com.example.note.database.NoteDatabase;
import com.example.note.model.Note;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ItemTouchHelperListener {
    private LinearLayout layoutNull;
    private Toolbar toolbarMain;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch aSwitch;
    private static final String SWITCH_KEY = "switch_key";

    private SharedPreferences mSharedPreferences;
    private RelativeLayout mMainLayout;
    private boolean isSwitch;
    private RecyclerView rcvNote;
    private FloatingActionButton btnThem;
    private NoteAdapter adapter;
    private List<Note> mListNote;
    private NoteDatabase database;
    private int idNote;
    Snackbar snackbar;

    private AdView mAdView;

    // Đăng ký ActivityResultLauncher
    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK) {
                        getDataFromDB();
                        adapter.notifyDataSetChanged();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        InitView();
        Event();
        getDataFromDB();
        Log.e("status", "----------Main: onCreate");
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.primary));
        }

        HienThiQuangCao();
    }

    private void HienThiQuangCao() {
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    private void Event() {
        btnThem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean stateSwitch = mSharedPreferences.getBoolean(SWITCH_KEY, isSwitch);
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                intent.putExtra("isupdate", false);
                intent.putExtra("isswitch", stateSwitch);
                activityResultLauncher.launch(intent);
            }
        });

        ItemTouchHelper.SimpleCallback simpleCallback = new RecyclerViewItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(simpleCallback).attachToRecyclerView(rcvNote);

    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void InitView() {

        mAdView = findViewById(R.id.adView);

        layoutNull = findViewById(R.id.layout_null);
        toolbarMain = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbarMain);
        btnThem = findViewById(R.id.btn_them);
        rcvNote = findViewById(R.id.rcv_note);
        mMainLayout = findViewById(R.id.layout_main);

        layoutNull.setVisibility(View.VISIBLE);
        mSharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        mListNote = new ArrayList<>();
        database = new NoteDatabase(this);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rcvNote.setLayoutManager(linearLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rcvNote.getContext(), LinearLayoutManager.VERTICAL);
        dividerItemDecoration.setDrawable(rcvNote.getContext().getResources().getDrawable(R.drawable.sk_line_divider));
        rcvNote.addItemDecoration(dividerItemDecoration);
        adapter = new NoteAdapter(this, mListNote, new IClickItem() {
            @Override
            public void onItemClickNote(int position) {
                boolean stateSwitch = mSharedPreferences.getBoolean(SWITCH_KEY, isSwitch);
                Note note = mListNote.get(position);
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                intent.putExtra("isupdate", true);
                intent.putExtra("idnote", note.getId());
                intent.putExtra("isswitch", stateSwitch);
                activityResultLauncher.launch(intent);
            }
        });
        if (mListNote.size() != 0){
            layoutNull.setVisibility(View.GONE);
        } else layoutNull.setVisibility(View.VISIBLE);
        Log.e("list", "List: " + mListNote.size());
        rcvNote.setAdapter(adapter);
    }

    private void getDataFromDB(){
        mListNote.clear();
        mListNote.addAll(database.getAllData());
        if (mListNote.size() != 0){
            layoutNull.setVisibility(View.GONE);
        } else layoutNull.setVisibility(View.VISIBLE);
        Log.e("list", "-------------" + mListNote.size());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_mode, menu);
        aSwitch = (Switch) menu.findItem(R.id.menu_item_switch).getActionView();
        boolean stateSwitch = mSharedPreferences.getBoolean(SWITCH_KEY, isSwitch);
        if (stateSwitch){
            aSwitch.setChecked(true);
        }
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isSwitch = isChecked;
                if (isChecked) {
                    // Switch được bật
                    adapter.setTextColor(Color.WHITE);
                    adapter.setItemColor(Color.BLACK);
                    mMainLayout.setBackgroundColor(Color.BLACK);
//                    toolbarMain.setBackgroundColor(Color.GREEN);
                } else {
                    // Switch được tắt
                    adapter.setTextColor(Color.BLACK);
                    adapter.setItemColor(Color.WHITE);
                    mMainLayout.setBackgroundColor(Color.WHITE);
//                    toolbarMain.setBackgroundColor(Color.YELLOW);
                }
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putBoolean(SWITCH_KEY, isSwitch);
                editor.apply();
            }
        });
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Load background state
        boolean stateSwitch = mSharedPreferences.getBoolean(SWITCH_KEY, isSwitch);
        if (stateSwitch){
            adapter.setTextColor(Color.WHITE);
            adapter.setItemColor(Color.BLACK);
            mMainLayout.setBackgroundColor(Color.BLACK);
//            toolbarMain.setBackgroundColor(Color.GREEN);
        } else {
            adapter.setTextColor(Color.BLACK);
            adapter.setItemColor(Color.WHITE);
            mMainLayout.setBackgroundColor(Color.WHITE);
//            toolbarMain.setBackgroundColor(Color.YELLOW);
        }
        Log.e("state", "----------onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e("status", "----------Main: onStop: " + isSwitch);
        if (snackbar != null && snackbar.isShown()) {
            snackbar.dismiss();
            database.deleteNote(idNote);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
        getDataFromDB();
        adapter.notifyDataSetChanged();

        Log.e("status", "----------Main: onResume");
    }

    @Override
    protected void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder) {
        if (viewHolder instanceof NoteAdapter.ViewHolder) {
            String strNote = mListNote.get(viewHolder.getAdapterPosition()).getNoidung();
            idNote = mListNote.get(viewHolder.getAdapterPosition()).getId();

            final Note note = mListNote.get(viewHolder.getAdapterPosition());
            final int indexDelete = viewHolder.getAdapterPosition();

            //Remove item
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
            alertDialogBuilder.setTitle("Xóa");
            alertDialogBuilder.setMessage("Bạn có chắc chắn muốn xóa ghi chú?");
            alertDialogBuilder.setPositiveButton("Xóa", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    adapter.removeItem(indexDelete);
//                    database.deleteNote(indexDelete);
                    if (mListNote.size() != 0){
                        layoutNull.setVisibility(View.GONE);
                    } else layoutNull.setVisibility(View.VISIBLE);
                    snackbar = Snackbar.make(mMainLayout, "Ghi chú đã được xóa", Snackbar.LENGTH_SHORT);
                    snackbar.setAction("Hoàn tác", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            adapter.undoItem(note, indexDelete);
                            if (mListNote.size() != 0){
                                layoutNull.setVisibility(View.GONE);
                            } else layoutNull.setVisibility(View.VISIBLE);
                            if (indexDelete == 0 || indexDelete == mListNote.size() - 1) {
                                rcvNote.scrollToPosition(indexDelete);
                            }
                        }
                    });

                    snackbar.setActionTextColor(Color.YELLOW);
                    snackbar.show();
                }
            });
            alertDialogBuilder.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    adapter.notifyDataSetChanged();
                }
            }).show();

        }
    }
}
package com.snatik.storage.app;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.snatik.storage.Storage;
import com.snatik.storage.helpers.OrderType;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class BootlogoActivity extends AppCompatActivity implements
        FilesAdapter.OnFileItemListener{

    private static final int PERMISSION_REQUEST_CODE = 1000;
    private RecyclerView mRecyclerView;
    private FilesAdapter mFilesAdapter;
    private Storage mStorage;
    private TextView mPathView;
    private TextView mMovingText;
    private Button mCustomize;
    private View mMovingLayout;
    private int mTreeSteps = 0;
    private final String USBPATH = "/mnt/media_rw";
    private final String BASEPATH = "/";
    private final String DATAPATH = "/data";
    private final String topath = DATAPATH+"/bootanimation.zip";
    private String filepath = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mStorage = new Storage(getApplicationContext());

        setContentView(R.layout.activity_bootanimation);

        getSupportActionBar().setDisplayShowHomeEnabled(true); //设置返回键可用
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Boot Logo");


        mRecyclerView = (RecyclerView) findViewById(R.id.recycler);
        mPathView = (TextView) findViewById(R.id.path);
        mCustomize = (Button) findViewById(R.id.customize);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        mFilesAdapter = new FilesAdapter(getApplicationContext());
        mFilesAdapter.setListener(this);
        mRecyclerView.setAdapter(mFilesAdapter);

        disablebtnstyle(mCustomize);
        mCustomize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customize();
                showNormalDialog();
            }
        });
        // load files
        showFiles(USBPATH);
        checkPermission();
    }
    private void customize(){
        mStorage.copy(this.filepath,topath);
        try {
           Process p = Runtime.getRuntime().exec("chmod 777 " + topath);
            try{
                int status = p.waitFor();
                if(status != 0){
                    Helper.showSnackbar("change file permission error", mRecyclerView);
                    return;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Helper.showSnackbar("Bootlogo Success", mRecyclerView);
    }

    private void showNormalDialog(){
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(BootlogoActivity.this);
        normalDialog.setMessage("bootlogo has been set up successfully, whether or not to reboot?");
        normalDialog.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //...To-do
                        try {
                            Intent intent = new Intent(Intent.ACTION_REBOOT);
                            intent.putExtra("nowait", 1);
                            intent.putExtra("interval", 1);
                            intent.putExtra("window", 0);
                            startActivity(intent);
                        } catch (Exception e){
                            new AlertDialog.Builder(BootlogoActivity.this).setTitle("Error").setMessage(e.getMessage()).setPositiveButton("OK", null).show();
                        }
                    }
                });
        normalDialog.setNegativeButton("No",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        normalDialog.show();
    }
    private void showFiles(String path) {
        mPathView.setText(path);
        List<File> files = mStorage.getFiles(path);
//        List<File> files = mStorage.getFiles(path,".*\\.zip$");
        if (files != null) {
            Collections.sort(files, OrderType.NAME.getComparator());
        }
        mFilesAdapter.setFiles(files);
        mFilesAdapter.notifyDataSetChanged();
    }
    private void showFiles(String path,String Regex) {
        mPathView.setText(path);
        List<File> files = mStorage.getFiles(path, Regex);
//        List<File> files = mStorage.getFiles(path,".*\\.zip$");
        if (files != null) {
            Collections.sort(files, OrderType.NAME.getComparator());
        }
        mFilesAdapter.setFiles(files);
        mFilesAdapter.notifyDataSetChanged();
    }
    @Override
    public void onClick(File file) {
        if (file.isDirectory()) {
            mTreeSteps++;
            String path = file.getAbsolutePath();
            disablebtnstyle(mCustomize);
            showFiles(path);
        } else {
            if(file.getName().matches(".*\\.zip$")){
                this.filepath = file.getAbsolutePath();
                mPathView.setText(this.filepath);
                ablebtnstyle(mCustomize);

            }else{
                disablebtnstyle(mCustomize);
                Helper.showSnackbar("Bootlogo image must be a zip package", mRecyclerView);
            }


        }
    }
    private void ablebtnstyle(Button btn){
        btn.setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_bg_abled));
        btn.setEnabled(true);
    }
    private void disablebtnstyle(Button btn){
        btn.setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_bg_disable));
        btn.setEnabled(false);
    }
    @Override
    public void onBackPressed() {
        if (mTreeSteps > 0) {
            String path = getPreviousPath();
            mTreeSteps--;
            showFiles(path);
            return;
        }
        super.onBackPressed();
    }

    private String getCurrentPath() {
        return mPathView.getText().toString();
    }

    private String getPreviousPath() {
        String path = getCurrentPath();
        int lastIndexOf = path.lastIndexOf(File.separator);
        if (lastIndexOf >0) {
            return path.substring(0, lastIndexOf);
        }else{
            return BASEPATH;
        }

    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager
                .PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[]
            grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            showFiles(USBPATH);
        } else {
            finish();
        }
    }
}

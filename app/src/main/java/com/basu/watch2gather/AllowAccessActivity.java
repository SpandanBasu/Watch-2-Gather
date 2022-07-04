package com.basu.watch2gather;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class AllowAccessActivity extends AppCompatActivity {

    public static final String SHARED_PREFERENCES_FILE_NAME="Allow Access";
    public static final int REQUEST_PERMISSION_SETTINGS=2;
    public static final int STORAGE_PERMISSION=1;
    Button allowBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_allow_access);
        allowBtn=(Button)findViewById(R.id.allowButton);
        SharedPreferences preferences = getSharedPreferences(SHARED_PREFERENCES_FILE_NAME, MODE_PRIVATE);
        String val= preferences.getString("Allow","");
        if(val.equals("OK")){
            startActivity(new Intent(AllowAccessActivity.this, MainActivity.class));
            finish();
        }else{
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("Allow","Ok");
            editor.apply();
        }
        allowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(AllowAccessActivity.this, "Allow Button Clicked", Toast.LENGTH_SHORT).show();
                if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){
                    startActivity(new Intent(AllowAccessActivity.this, MainActivity.class));
                    finish();
                }else{
                    ActivityCompat.requestPermissions(AllowAccessActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},STORAGE_PERMISSION);
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==STORAGE_PERMISSION){
            for(int i=0; i<permissions.length; i++){
                String per=permissions[i];
                if(grantResults[i]==PackageManager.PERMISSION_DENIED){
                    boolean showRationale= shouldShowRequestPermissionRationale(per);
                    if(!showRationale){
                        //clicked on never ask again
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle(R.string.permmisionDialogueTitle)
                        .setMessage(R.string.permmisionDialogueMsg)
                        .setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",getPackageName(),null);
                                intent.setData(uri);
                                startActivityForResult(intent,REQUEST_PERMISSION_SETTINGS);
                            }
                        }).create().show();
                    }else{
                        ActivityCompat.requestPermissions(AllowAccessActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},STORAGE_PERMISSION);
                    }
                }else{
                    //accepted
                    startActivity(new Intent(AllowAccessActivity.this, MainActivity.class));
                    finish();
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onResume() {
        super.onResume();
        if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED){
            startActivity(new Intent(AllowAccessActivity.this, MainActivity.class));
            finish();
        }
    }
}
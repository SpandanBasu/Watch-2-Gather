package com.basu.watch2gather;

import static android.content.ContentValues.TAG;
import static com.basu.watch2gather.AllowAccessActivity.REQUEST_PERMISSION_SETTINGS;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    public static FirebaseDatabase database;
    public static DatabaseReference myRef;
    RelativeLayout videoCatBtn,browseBtn,connectBtn;
    public static FirebaseAuth mAuth;
    public static String user_id="";

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_DENIED){
            Toast.makeText(this, "Please allow Storage permission", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package",getPackageName(),null);
            intent.setData(uri);
            startActivityForResult(intent,REQUEST_PERMISSION_SETTINGS);
        }

        database=FirebaseDatabase.getInstance();
        myRef=database.getReference("Users");
        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser()==null){
            mAuth.signInAnonymously().addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        user_id=mAuth.getCurrentUser().getUid();
                        HashMap<String,Object> map= new HashMap<>();
                        map.put("Status","IDLE");
                        myRef.child(user_id).updateChildren(map);
                        Toast.makeText(MainActivity.this, "loggedIn anonymously", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(MainActivity.this, "failed login", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        if(mAuth.getCurrentUser()!=null)
            user_id=mAuth.getCurrentUser().getUid();
        try {
            if(!user_id.equals("")){
                ConnectActivity.checkIfConnected();
            }
        }catch (NullPointerException e){
            e.printStackTrace();
        }
        videoCatBtn=(RelativeLayout)findViewById(R.id.video_cat_btn);
        browseBtn=(RelativeLayout)findViewById(R.id.browse_btn);
        connectBtn=(RelativeLayout)findViewById(R.id.connect_btn);

        videoCatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(MainActivity.this, "Video Gallery Open", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MainActivity.this, VideoCatalogueActivity.class));
            }
        });
        browseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(MainActivity.this, "Browser Open", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MainActivity.this, BrowseFolderActivity.class));
            }
        });
        connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(MainActivity.this, "Connect with others open", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MainActivity.this, ConnectActivity.class));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();
        switch(id){
            case R.id.share_app_main:
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT,"Check out this app via\n"+"https://play.google.com/store/apps/details?id="+
                        getApplicationContext().getPackageName());
                shareIntent.setType("text/plain");
                startActivity(Intent.createChooser(shareIntent,"Share app via"));
                break;

            case R.id.rate_us_main:
                Uri uri= Uri.parse("https://play.google.com/store/apps/details?id="+
                        getApplicationContext().getPackageName());
                startActivity(new Intent(Intent.ACTION_VIEW,uri));
                break;
        }
        return true;
    }
    public void disconnect(){
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //setting own data
                if(snapshot.child(user_id).child("Status").getValue(String.class).equals("CONNECTED")){
                    myRef.child(user_id).child("Status").setValue("IDLE");
                    String other_uid= snapshot.child(user_id).child("Destination_ID").getValue(String.class);
                    Log.e(TAG,other_uid+"!!!!!!!!!!!!!!!!!!!!!");

                    //setting destination data
                    Log.e(TAG,other_uid+"!!!!!!!!!!!!!!!!!!!!!");
                    myRef.child(other_uid).child("Status").setValue("IDLE");
                    myRef.child(other_uid).child("Destination_ID").removeValue();
                    myRef.child(user_id).child("Destination_ID").removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Cancelled midway", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG,"ON Destroy called from MainActivity!!!!!!!!!");
        try {
            disconnect();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
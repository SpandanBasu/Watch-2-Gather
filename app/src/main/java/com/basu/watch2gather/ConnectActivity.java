package com.basu.watch2gather;

import static android.content.ContentValues.TAG;

import static com.basu.watch2gather.MainActivity.database;
import static com.basu.watch2gather.MainActivity.mAuth;
import static com.basu.watch2gather.MainActivity.myRef;
import static com.basu.watch2gather.MainActivity.user_id;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.net.InetAddress;

public class ConnectActivity extends AppCompatActivity {





    public static String other_uid="";
    public static boolean isConnected=false;
    TextView userId;
    RelativeLayout connectBtn,disconnectBtn,connectedImg;
    ImageView copyIcon,shareIcon;
    SwipeRefreshLayout swipeRefreshLayout;
    EditText otherUserId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        database=FirebaseDatabase.getInstance();
        checkIfConnected();
        userId=(TextView)findViewById(R.id.user_id);
        connectBtn=(RelativeLayout)findViewById(R.id.connect_button);
        disconnectBtn=(RelativeLayout)findViewById(R.id.disconnect_button);
        copyIcon=(ImageView)findViewById(R.id.copy_user_name);
        shareIcon=(ImageView)findViewById(R.id.share_user_name);
        swipeRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.connect_swipe);
        otherUserId=(EditText)findViewById(R.id.other_user_id);
        connectedImg=(RelativeLayout) findViewById(R.id.connected_img);

        signInAnonymously();
        copyIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("user_id", user_id);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(ConnectActivity.this, "Copied", Toast.LENGTH_SHORT).show();
            }
        });

        shareIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "userId");
                intent.putExtra(android.content.Intent.EXTRA_TEXT, user_id);
                startActivity(Intent.createChooser(intent, "Share via "));
            }
        });

        connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connect();
            }
        });

        disconnectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                disconnect();
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                FirebaseUser user=mAuth.getCurrentUser();
                updateUI(user);
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void signInAnonymously() {
        if(mAuth.getCurrentUser()==null){
            mAuth.signInAnonymously()
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "signInAnonymously:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                updateUI(user);
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "signInAnonymously:failure", task.getException());
                                Toast.makeText(ConnectActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                                updateUI(null);
                            }
                        }
                    });
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!=null){
            updateUI(currentUser);
        }
    }

    private void updateUI(FirebaseUser currentUser) {
        signInAnonymously();
        if(currentUser==null){
            userId.setText("Not loggedIn");
            return;
        }
        String userIdText =currentUser.getUid();
        userId.setText(userIdText);
        user_id=userIdText;
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try{
                    if(snapshot.child(user_id).child("Status").getValue(String.class).equals("CONNECTED")){
                        //already connected
                        otherUserId.setVisibility(View.INVISIBLE);
                        connectBtn.setVisibility(View.INVISIBLE);
                        connectedImg.setVisibility(View.VISIBLE);
                        disconnectBtn.setVisibility(View.VISIBLE);
                        other_uid=(snapshot.child(user_id).child("Destination_ID").getValue(String.class));
                    }else{
                        otherUserId.setVisibility(View.VISIBLE);
                        connectBtn.setVisibility(View.VISIBLE);
                        connectedImg.setVisibility(View.INVISIBLE);
                        disconnectBtn.setVisibility(View.INVISIBLE);
                        other_uid="";
                    }
                }catch (NullPointerException e){
                    e.printStackTrace();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void connect(){
        other_uid= otherUserId.getText().toString().trim();
        if(other_uid.equals(user_id)){
            Toast.makeText(this, "Can not connect with self", Toast.LENGTH_SHORT).show();
        }else{
            if(isNetworkConnected()){
                //Toast.makeText(ConnectActivity.this, ""+other_uid+"  and  "+user_id, Toast.LENGTH_SHORT).show();
                myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean found=false;
                        for(DataSnapshot childSnapshot : snapshot.getChildren()){

                            if(childSnapshot.getKey().equals(other_uid)){
                                if(childSnapshot.child("Status").getValue(String.class).equals("IDLE")){
                                    otherUserId.setVisibility(View.INVISIBLE);
                                    //setting own data
                                    myRef.child(user_id).child("Status").setValue("CONNECTED");
                                    myRef.child(user_id).child("Destination_ID").setValue(other_uid);

                                    //setting destination data
                                    myRef.child(other_uid).child("Status").setValue("CONNECTED");
                                    myRef.child(other_uid).child("Destination_ID").setValue(user_id);

                                    Toast.makeText(ConnectActivity.this, "Connected Successfully", Toast.LENGTH_SHORT).show();
                                    connectedImg.setVisibility(View.VISIBLE);
                                    connectBtn.setVisibility(View.INVISIBLE);
                                    disconnectBtn.setVisibility(View.VISIBLE);
                                }else{
                                    Toast.makeText(ConnectActivity.this, "Destination Busy", Toast.LENGTH_SHORT).show();
                                }
                                found=true;
                                break;
                            }
                        }
                        if(!found){
                            Toast.makeText(ConnectActivity.this, "Not found", Toast.LENGTH_SHORT).show();
                        }

                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ConnectActivity.this, "Cancelled midway", Toast.LENGTH_SHORT).show();
                    }
                });
            }else{
                Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show();
            }

        }

    }
    public void disconnect(){
        if(isNetworkConnected()){
            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    //setting own data
                    myRef.child(user_id).child("Status").setValue("IDLE");
                    String other_uid= snapshot.child(user_id).child("Destination_ID").getValue(String.class);
                    Log.e(TAG,other_uid+"!!!!!!!!!!!!!!!!!!!!!");

                    //setting destination data
                    Log.e(TAG,other_uid+"!!!!!!!!!!!!!!!!!!!!!");
                    myRef.child(other_uid).child("Status").setValue("IDLE");
                    myRef.child(other_uid).child("Destination_ID").removeValue();
                    myRef.child(user_id).child("Destination_ID").removeValue();
                    Toast.makeText(ConnectActivity.this, "Disconnected", Toast.LENGTH_SHORT).show();
                    connectedImg.setVisibility(View.INVISIBLE);
                    connectBtn.setVisibility(View.VISIBLE);
                    otherUserId.setVisibility(View.VISIBLE);
                    disconnectBtn.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(ConnectActivity.this, "Cancelled midway", Toast.LENGTH_SHORT).show();
                }
            });
        }else{
            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show();
        }
    }
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    public boolean isInternetAvailable() {
        try {
            InetAddress ipAddr = InetAddress.getByName("google.com");
            //You can replace it with your name
            return !ipAddr.equals("");

        } catch (Exception e) {
            return false;
        }
    }
    public static void checkIfConnected() {
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!user_id.equals("")){
                    try{
                        //Log.e(TAG,"data videoPlayer!!!!!!!!"+snapshot.child(user_id).child("Status").getValue(String.class));
                        if(snapshot.child(user_id).child("Status").getValue(String.class).equals("CONNECTED")){
                            isConnected =true;
                            other_uid=snapshot.child(user_id).child("Destination_ID").getValue(String.class);
                        }else{
                            isConnected =false;
                        }
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}
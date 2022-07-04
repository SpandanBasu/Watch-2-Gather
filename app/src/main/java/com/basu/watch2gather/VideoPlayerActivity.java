package com.basu.watch2gather;

import static android.content.ContentValues.TAG;

import static com.basu.watch2gather.ConnectActivity.isConnected;
import static com.basu.watch2gather.ConnectActivity.other_uid;
import static com.basu.watch2gather.MainActivity.mAuth;
import static com.basu.watch2gather.MainActivity.myRef;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.DefaultTimeBar;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.sql.Array;
import java.util.ArrayList;

public class VideoPlayerActivity extends AppCompatActivity implements View.OnClickListener {



    private String mUserId="";
    boolean connected=false;
    ArrayList<MediaFiles> myVideoFiles= new ArrayList<>();
    PlayerView playerView;
    SimpleExoPlayer player;
    int position;
    private String videoTitle;
    TextView title;
    boolean hiddenUI=true;
    ImageView prevImg,nextImg,playBtn,pauseBtn;
    ConcatenatingMediaSource concatenatingMediaSource;

    LinearLayout bottomIcons;
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullScreen();
        hideSystemUI();
        //hideSystemBars();
        setContentView(R.layout.activity_video_player);
        getSupportActionBar().hide();

        mUserId= mAuth.getCurrentUser().getUid();
        Toast.makeText(this, "Connected: "+isConnected, Toast.LENGTH_SHORT).show();
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,WindowManager.LayoutParams.TYPE_STATUS_BAR);
        playerView=(PlayerView)findViewById(R.id.exoplayer_view);
        prevImg=(ImageView)findViewById(R.id.exo_prev);
        nextImg=(ImageView)findViewById(R.id.exo_next);
        bottomIcons=(LinearLayout)findViewById(R.id.bottom_icons);
        playBtn=(ImageView)findViewById(R.id.exo_play);
        pauseBtn=(ImageView)findViewById(R.id.exo_pause);
        //progressBar=(DefaultTimeBar)findViewById(R.id.exo_progress);
        if(this.getResources().getConfiguration().orientation==Configuration.ORIENTATION_LANDSCAPE){
            setMargins(bottomIcons, 0, 0, 0, 5);
        }
        prevImg.setOnClickListener(this);
        nextImg.setOnClickListener(this);
        playBtn.setOnClickListener(this);
        pauseBtn.setOnClickListener(this);
        position=getIntent().getIntExtra("position",1);
        videoTitle=getIntent().getStringExtra("videoTitle");
        myVideoFiles=getIntent().getExtras().getParcelableArrayList("videoListArray");
        title=(TextView)findViewById(R.id.video_title);
        title.setText(videoTitle);

        playVideo();
        if(isConnected){
            updateUI();
            setPlayPauseBehaviour();
        }
    }

    private void setPlayPauseBehaviour() {

    }

    private void playVideo() {
        String path= myVideoFiles.get(position).getPath();
        Uri uri= Uri.parse(path);
        player=new SimpleExoPlayer.Builder(this).build();
        DefaultDataSourceFactory dataSourceFactory= new DefaultDataSourceFactory(this, Util.getUserAgent(this,"app"));
        concatenatingMediaSource= new ConcatenatingMediaSource();
        for(int i=0; i<myVideoFiles.size(); i++){
            new File(String.valueOf(myVideoFiles.get(i)));
            MediaSource mediaSource= new ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(Uri.parse(String.valueOf(uri)));
            concatenatingMediaSource.addMediaSource(mediaSource);
        }
        playerView.setPlayer(player);
        playerView.setKeepScreenOn(true);
        //playerView.setControlDispatcher(new PlayerControlDispatcher(this));
        player.prepare(concatenatingMediaSource);
        player.seekTo(position, C.TIME_UNSET);
        playError();
        if(isConnected){
            myRef.child(mUserId).child("Playback_State").setValue("PLAY");
            myRef.child(mUserId).child("Playback_Cur_Time").setValue(0);
            myRef.child(other_uid).child("Playback_State").setValue("PLAY");
            myRef.child(other_uid).child("Playback_Cur_Time").setValue(0);
        }
    }

    private void playError() {
        player.addListener(new Player.EventListener() {
            @Override
            public void onPlayerError(ExoPlaybackException error) {
                Player.EventListener.super.onPlayerError(error);
                Toast.makeText(VideoPlayerActivity.this, "Video Playing Error", Toast.LENGTH_SHORT).show();
            }
        });
        player.setPlayWhenReady(true);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(player.isPlaying()){
            player.stop();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        player.setPlayWhenReady(false);
        player.getPlaybackState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        player.setPlayWhenReady(true);
        player.getPlaybackState();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        player.setPlayWhenReady(true);
        player.getPlaybackState();
    }

    @Override
    protected void onStop() {
        super.onStop();
        player.stop();
        player.release();
    }

    private void setFullScreen(){
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }
    private void hideSystemUI() {
        hiddenUI=true;
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        this.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    // This snippet shows the system bars. It does this by removing all the flags
// except for the ones that make the content appear under the system bars.
    private void showSystemUI() {
        hiddenUI=false;
        this.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }


    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.exo_prev:
                try{
                    player.stop();
                    position--;
                    playVideo();
                }catch (Exception e){
                    Toast.makeText(this, "No previous video found", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            case R.id.exo_next:
                try{
                    player.stop();
                    position++;
                    playVideo();
                }catch (Exception e){
                    Toast.makeText(this, "No next video found", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            case R.id.exo_play:
                player.pause();
                try{
                    mUserId=mAuth.getCurrentUser().getUid();
                    if(isConnected){
                        long cur_time =(long)player.getCurrentPosition()/1000;
                        myRef.child(mUserId).child("Playback_State").setValue("PLAY");
                        myRef.child(mUserId).child("Playback_Cur_Time").setValue(cur_time);
                        myRef.child(other_uid).child("Playback_State").setValue("PLAY");
                        myRef.child(other_uid).child("Playback_Cur_Time").setValue(cur_time);

                    }else{
                        player.play();
                    }
                }catch (Exception e){
                    Toast.makeText(this, "Can not play video exception", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            case R.id.exo_pause:
                try{
                    mUserId=mAuth.getCurrentUser().getUid();
                    if(isConnected){
                        long cur_time =(long)player.getCurrentPosition()/1000;
                        myRef.child(mUserId).child("Playback_State").setValue("PAUSE");
                        myRef.child(mUserId).child("Playback_Cur_Time").setValue(cur_time);
                        myRef.child(other_uid).child("Playback_State").setValue("PAUSE");
                        myRef.child(other_uid).child("Playback_Cur_Time").setValue(cur_time);
                    }else{
                        player.pause();
                    }
                }catch (Exception e){
                    Toast.makeText(this, "Can not pause video exception", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }

    private void hideSystemBars() {
        hiddenUI=true;
        WindowInsetsControllerCompat windowInsetsController =
                ViewCompat.getWindowInsetsController(getWindow().getDecorView());
        if (windowInsetsController == null) {
            return;
        }
        // Configure the behavior of the hidden system bars
        windowInsetsController.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        );
        // Hide both the status bar and the navigation bar
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
    }

    private void showSystemBars() {
        hiddenUI=false;
        WindowInsetsControllerCompat windowInsetsController =
                ViewCompat.getWindowInsetsController(getWindow().getDecorView());
        if (windowInsetsController == null) {
            return;
        }
        // Configure the behavior of the hidden system bars
        windowInsetsController.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_BARS_BY_SWIPE
        );
        // Hide both the status bar and the navigation bar
        windowInsetsController.show(WindowInsetsCompat.Type.systemBars());
    }

    private void setMargins (View view, int left, int top, int right, int bottom) {
        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            p.setMargins(left, top, right, bottom);
            view.requestLayout();
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){
            //Toast.makeText(this, "Landscape", Toast.LENGTH_SHORT).show();
            setMargins(bottomIcons, 0, 0, 0, 5);
        }else{
            //Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
            setMargins(bottomIcons, 0, 0, 0, 100);
        }
    }
    void updateUI(){
        myRef.child(mUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //Toast.makeText(VideoPlayerActivity.this, "Dataset Updated", Toast.LENGTH_SHORT).show();
                if(snapshot.hasChild("Playback_Cur_Time")){
                    long dur=snapshot.child("Playback_Cur_Time").getValue(Integer.class)*1000;
                    player.seekTo(dur);
                }
                if(snapshot.hasChild("Playback_State")){
                    if(snapshot.child("Playback_State").getValue(String.class).equals("PLAY")){
                        //Toast.makeText(VideoPlayerActivity.this, "PLAY Order Detected", Toast.LENGTH_SHORT).show();
                        if(snapshot.hasChild("Playback_Cur_Time")){
                            long dur=snapshot.child("Playback_Cur_Time").getValue(Integer.class)*1000+800;
                            player.seekTo(dur);
                        }
                        player.play();
                    }else{
                        //Toast.makeText(VideoPlayerActivity.this, "PAUSE Order Detected", Toast.LENGTH_SHORT).show();
                        player.pause();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //Do nothing
            }
        });
    }

}
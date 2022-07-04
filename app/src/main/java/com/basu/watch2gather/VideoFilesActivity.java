package com.basu.watch2gather;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import java.util.ArrayList;

public class VideoFilesActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    SwipeRefreshLayout swipeRefreshLayout;
    VideoFilesAdapter videoFilesAdapter;
    private ArrayList<MediaFiles> videoList;
    private String folderName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_files);
        swipeRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.swipe_refresh_videos);
        recyclerView=(RecyclerView)findViewById(R.id.recycler_video_list);
        folderName=getIntent().getStringExtra("folderName");
        getSupportActionBar().setTitle(folderName);
        loadVideoFiles();
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadVideoFiles();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

    }

    private void loadVideoFiles() {
        videoList=fetchMedia(folderName);
        videoFilesAdapter= new VideoFilesAdapter(videoList,this);
        recyclerView.setAdapter(videoFilesAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,RecyclerView.VERTICAL,false));
        videoFilesAdapter.notifyDataSetChanged();
    }

    private ArrayList<MediaFiles> fetchMedia(String folderName) {
        ArrayList<MediaFiles> videoFilesArray= new ArrayList<>();
        Uri uri= MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String selection= MediaStore.Video.Media.DATA+" like?";
        String selectionArg[]= new String[]{"%"+folderName+"%"};
        Cursor cursor= getContentResolver().query(uri,null,selection,selectionArg,null);
        if(cursor!=null && cursor.moveToNext()){
            do{
                String id=cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media._ID));
                String title=cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE));
                String displayName=cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME));
                String size=cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.SIZE));
                String duration=cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DURATION));
                String path=cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
                String dateAdded=cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATE_ADDED));
                MediaFiles mediaFiles= new MediaFiles(id,title,displayName,size,duration,path,dateAdded);
                videoFilesArray.add(mediaFiles);
            }while(cursor.moveToNext());
        }
        return videoFilesArray;
    }
}
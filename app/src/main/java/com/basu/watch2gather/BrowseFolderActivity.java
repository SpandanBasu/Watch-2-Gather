package com.basu.watch2gather;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;

public class BrowseFolderActivity extends AppCompatActivity {

    private ArrayList<MediaFiles> mediaFiles= new ArrayList<>();
    private ArrayList<String> allFolderList = new ArrayList<>();
    RecyclerView recyclerView;
    VideoFoldersAdapter adapter;
    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_folder);

        recyclerView=(RecyclerView) findViewById(R.id.browse_list);
        swipeRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.swipe_refresh_folders);

        showFolders();
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                showFolders();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void showFolders() {
        mediaFiles=fetchMedia();
        adapter=new VideoFoldersAdapter(mediaFiles,allFolderList,this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));
        adapter.notifyDataSetChanged();
    }

    private ArrayList<MediaFiles> fetchMedia() {
        ArrayList<MediaFiles> mediaFilesArrayList = new ArrayList<>();
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

        Cursor cursor = getContentResolver().query(uri,null,null,null,null);
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
                mediaFilesArrayList.add(mediaFiles);

                int index=path.lastIndexOf('/');
                String subString = path.substring(0,index);
                if(!allFolderList.contains(subString)){
                    allFolderList.add(subString);
                }
            }while(cursor.moveToNext());
        }
        return mediaFilesArrayList;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.folder_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();
        switch(id){
            case R.id.search:
                break;
            case R.id.refresh_folders:
                finish();
                startActivity(getIntent());
                break;
            case R.id.share_app:
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT,"Check this app via \n"+"https://play.google.com/store/apps/details?id="+
                        getApplicationContext().getPackageName());
                shareIntent.setType("text/plain");
                startActivity(Intent.createChooser(shareIntent,"Share app via"));
                break;
            case R.id.rate_us:
                Uri uri= Uri.parse("https://play.google.com/store/apps/details?id="+
                        getApplicationContext().getPackageName());
                Intent intent= new Intent(Intent.ACTION_VIEW,uri);
                startActivity(intent);
                break;
        }
        return true;
    }
}
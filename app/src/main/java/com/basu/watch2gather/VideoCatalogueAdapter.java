package com.basu.watch2gather;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;

public class VideoCatalogueAdapter extends RecyclerView.Adapter<VideoCatalogueAdapter.ViewHolder> {

    ArrayList<MediaFiles> videoList= new ArrayList<>();
    private Context context;

    public VideoCatalogueAdapter(ArrayList<MediaFiles> videoList, Context context) {
        this.videoList = videoList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.catalogue_grid,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.videoTitle.setText(videoList.get(position).getDisplayName());
        double milliSecond=-1;
        try{
            //Log.e("TAG", "onBindViewHolder: !!!!!!!!!!!!!!!!!!!"+videoList.get(position).getDuration(),new NullPointerException() );
            milliSecond=Double.parseDouble(videoList.get(position).getDuration());

        }catch(Exception e){
            e.printStackTrace();
        }
        String timeDur="--";
        if(milliSecond!=-1)
            timeDur=formatDuration((long)milliSecond);
        holder.videoDuration.setText(timeDur);
        Glide.with(context).load(new File(videoList.get(position).getPath())).into(holder.thumbnailImg);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(context, VideoPlayerActivity.class);
                intent.putExtra("position",position);
                intent.putExtra("videoTitle",videoList.get(position).getDisplayName());
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList("videoListArray",videoList);
                intent.putExtras(bundle);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnailImg;
        TextView videoDuration,videoTitle;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnailImg=(ImageView)itemView.findViewById(R.id.img_thumb_cat);
            videoDuration=(TextView)itemView.findViewById(R.id.video_dur_cat);
            videoTitle=(TextView)itemView.findViewById(R.id.video_title_cat);
        }
    }
    private String formatDuration(long milliSecond) {
        String videoTime;
        long hours=0;
        long minutes=0;
        long seconds=0;
        long secondsR=0;
        long minutesR=0;
        seconds=(milliSecond/1000);
        minutes=seconds/60;
        secondsR=seconds%60;
        hours=minutes/60;
        minutesR=minutes%60;

        if(hours>0){
            videoTime=String.format("%02d:%02d:%02d",hours,minutesR,secondsR);
        }else if(minutes>0){
            videoTime=String.format("%02d:%02d",minutes,secondsR);
        }else{
            videoTime=String.format("%02ds",seconds);
        }
        return videoTime;
    }
}

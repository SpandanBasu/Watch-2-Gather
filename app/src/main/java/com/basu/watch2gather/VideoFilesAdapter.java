package com.basu.watch2gather;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.File;
import java.util.ArrayList;

public class VideoFilesAdapter extends RecyclerView.Adapter<VideoFilesAdapter.ViewHolder> {
    private ArrayList<MediaFiles> videoList;
    private Context context;
    BottomSheetDialog bottomSheetDialog;
    public VideoFilesAdapter(ArrayList<MediaFiles> videoList, Context context) {
        this.videoList = videoList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.video_list_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.videoName.setText(videoList.get(position).getDisplayName());
        String size=videoList.get(position).getSize();
        holder.videoSize.setText(android.text.format.Formatter.formatFileSize(context, Long.parseLong(size)));
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
        double finalMilliSecond = milliSecond;
        holder.menuMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog= new BottomSheetDialog(context,R.style.BottomSheetTheme);
                View bsView= LayoutInflater.from(context).inflate(R.layout.video_bs_layout,
                        view.findViewById(R.id.bottom_sheet));
                bsView.findViewById(R.id.bs_play).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        holder.itemView.performClick();
                        bottomSheetDialog.dismiss();
                    }
                });
                bsView.findViewById(R.id.bs_rename).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder alertDialogue= new AlertDialog.Builder(context);
                        alertDialogue.setTitle("Rename to");
                        EditText editText= new EditText(context);
                        String path= videoList.get(position).getPath();
                        final File file= new File(path);
                        String videoName= file.getName();
                        videoName=videoName.substring(0,videoName.lastIndexOf("."));
                        editText.setText(videoName);
                        alertDialogue.setView(editText);
                        editText.requestFocus();

                        alertDialogue.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String onlyPath= file.getParentFile().getAbsolutePath();
                                String ext= file.getAbsolutePath();
                                ext=ext.substring(ext.lastIndexOf("."));
                                String newPath = onlyPath + "/" + editText.getText().toString() + ext;
                                File newFile= new File(newPath);
                                boolean rename= file.renameTo(newFile);
                                if(rename){
                                    ContentResolver resolver= context.getApplicationContext().getContentResolver();
                                    resolver.delete(MediaStore.Files.getContentUri("external"),
                                            MediaStore.MediaColumns.DATA+"=?",new String[]
                                                    {file.getAbsolutePath()});
                                    Intent intent= new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                                    intent.setData(Uri.fromFile(newFile));
                                    context.getApplicationContext().sendBroadcast(intent);

                                    notifyDataSetChanged();
                                    Toast.makeText(context, "Video Renamed", Toast.LENGTH_SHORT).show();
                                    SystemClock.sleep(200);
                                    ((Activity)context).recreate();
                                }else{
                                    Toast.makeText(context, "Process Failed", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        alertDialogue.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                        alertDialogue.create().show();
                        bottomSheetDialog.dismiss();
                    }
                });
                bsView.findViewById(R.id.bs_share).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Uri uri = Uri.parse(videoList.get(position).getPath());
                        Intent shareIntent= new Intent(Intent.ACTION_SEND);
                        shareIntent.setType("video/*");
                        shareIntent.putExtra(Intent.EXTRA_STREAM,uri);
                        context.startActivity(Intent.createChooser(shareIntent,"Share video via"));
                        bottomSheetDialog.dismiss();
                    }
                });
                bsView.findViewById(R.id.bs_delete).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder alertDialogue= new AlertDialog.Builder(context);
                        alertDialogue.setTitle("Delete");
                        alertDialogue.setMessage("Do you really want to delete?");
                        alertDialogue.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Uri contentUri= ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                                        Long.parseLong(videoList.get(position).getId()));
                                File file= new File(videoList.get(position).getPath());
                                boolean delete = file.delete();
                                if(delete){
                                    context.getContentResolver().delete(contentUri,null,null);
                                    videoList.remove(position);
                                    notifyItemRemoved(position);
                                    notifyItemRangeChanged(position,videoList.size());
                                    Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show();
                                }else{
                                    Toast.makeText(context, "Can not delete file", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        alertDialogue.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                        alertDialogue.show();
                        bottomSheetDialog.dismiss();
                    }
                });
                bsView.findViewById(R.id.bs_properties).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder alertDialogue= new AlertDialog.Builder(context);
                        alertDialogue.setTitle("Properties");
                        String one ="File: "+videoList.get(position).getDisplayName();
                        String path= videoList.get(position).getPath();
                        int indexOfPath= path.lastIndexOf("/");
                        String two="Path: "+path.substring(0,indexOfPath);
                        String three="Size: "+android.text.format.Formatter.formatFileSize(context,
                                Long.parseLong(videoList.get(position).getSize()));
                        String four="Length: "+formatDuration((long) finalMilliSecond);
                        String nameWithFormat= videoList.get(position).getDisplayName();
                        int index= nameWithFormat.lastIndexOf(".");
                        String format= nameWithFormat.substring(index+1);
                        String five="Format: "+format;
                        MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
                        metadataRetriever.setDataSource(videoList.get(position).getPath());
                        String height= metadataRetriever.extractMetadata(MediaMetadataRetriever.
                                METADATA_KEY_VIDEO_HEIGHT);
                        String width= metadataRetriever.extractMetadata(MediaMetadataRetriever.
                                METADATA_KEY_VIDEO_WIDTH);
                        String six="Resolution: "+width+"X"+height;
                        alertDialogue.setMessage(one+"\n\n"+two+"\n\n"+three+"\n\n"+four+"\n\n"+five+"\n\n"+six);
                        alertDialogue.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                        alertDialogue.show();
                        bottomSheetDialog.dismiss();
                    }
                });
                bottomSheetDialog.setContentView(bsView);
                bottomSheetDialog.show();
            }
        });
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

    @Override
    public int getItemCount() {
        return videoList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnailImg,menuMore;
        TextView videoName,videoSize,videoDuration;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnailImg=(ImageView)itemView.findViewById(R.id.thumbnail_img);
            menuMore=(ImageView)itemView.findViewById(R.id.video_more_option);
            videoName=(TextView)itemView.findViewById(R.id.video_name);
            videoSize=(TextView)itemView.findViewById(R.id.video_size);
            videoDuration=(TextView)itemView.findViewById(R.id.video_dur);
        }
    }
}

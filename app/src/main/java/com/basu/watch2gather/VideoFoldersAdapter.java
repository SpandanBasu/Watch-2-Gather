package com.basu.watch2gather;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class VideoFoldersAdapter extends RecyclerView.Adapter<VideoFoldersAdapter.ViewHolder> {
    private ArrayList<MediaFiles> mediaFiles;
    private ArrayList<String> folderPath;
    private Context context;

    public VideoFoldersAdapter(ArrayList<MediaFiles> mediaFiles, ArrayList<String> folderPath, Context context) {
        this.mediaFiles = mediaFiles;
        this.folderPath = folderPath;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.folder_list_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        int indexPath=folderPath.get(position).lastIndexOf('/');
        String title=folderPath.get(position).substring(indexPath+1);
        holder.folderTitle.setText(title);
        holder.folder_path.setText(folderPath.get(position));
        int number=noOfFilesM(folderPath.get(position));
        holder.noOfFiles.setText(number+" Videos");

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(context, VideoFilesActivity.class);
                intent.putExtra("folderName",title);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return folderPath.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView folderTitle, folder_path, noOfFiles;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            folderTitle=(TextView)itemView.findViewById(R.id.folder_title);
            folder_path=(TextView)itemView.findViewById(R.id.folder_path_id);
            noOfFiles=(TextView)itemView.findViewById(R.id.file_number);
        }
    }
    int noOfFilesM(String folder_name){
        int files_no=0;
        for(MediaFiles mediaFiles : mediaFiles){
            if(mediaFiles.getPath().substring(0,mediaFiles.getPath().lastIndexOf("/"))
                    .endsWith(folder_name)){
                files_no++;
            }
        }
        return files_no;
    }
}

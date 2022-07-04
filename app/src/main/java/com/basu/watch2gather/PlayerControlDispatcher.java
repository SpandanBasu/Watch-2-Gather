package com.basu.watch2gather;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.android.exoplayer2.DefaultControlDispatcher;
import com.google.android.exoplayer2.Player;


public class PlayerControlDispatcher extends DefaultControlDispatcher {
    public PlayerControlDispatcher(Context context) {
        this.context = context;
    }

    private Context context;

    @Override
    public boolean dispatchSetPlayWhenReady(Player player, boolean playWhenReady) {
        if(playWhenReady){
            //Play button clicked
            Toast.makeText(context, "Play clicked", Toast.LENGTH_SHORT).show();
        }else{
            //Pause Button Clicked

        }
        return super.dispatchSetPlayWhenReady(player, playWhenReady);
    }
}

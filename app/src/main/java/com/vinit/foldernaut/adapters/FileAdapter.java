package com.vinit.foldernaut.adapters;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.vinit.foldernaut.R;
import com.vinit.foldernaut.objects.FileObject;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.MyViewHolder> {


    private List<FileObject> fileObjects;
    private Context ctx;
    private RecyclerViewClickListener myRecyclerViewClickListener;
    private RecyclerViewLongClickListener myRecyclerViewLongClickListener;
    private boolean is_in_action_mode = false;
    //private String[] colors = {"#1976D2", "#FF9800", "#388E3C", "#D32F2F", "#FFC107", "#FF5722"};
    private int lastPosition = -1;

    private ArrayList<Integer> tohighlight = new ArrayList<>();

    public FileAdapter(List<FileObject> fileObjects, Context ctx, RecyclerViewClickListener myRecyclerViewClickListener,
                       RecyclerViewLongClickListener myRecyclerViewLongClickListener, ArrayList<Integer> tohighlight) {

        this.fileObjects = fileObjects;
        this.ctx = ctx;
        this.myRecyclerViewClickListener = myRecyclerViewClickListener;
        this.myRecyclerViewLongClickListener = myRecyclerViewLongClickListener;
        this.tohighlight = tohighlight;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.folder_list_item_layout, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        final FileObject fo = fileObjects.get(position);
        holder.itemFolderName.setText(fo.getFilename());
        String attrib = fo.isDirectory() ? Integer.toString(fo.getChildCount()):
                FileUtils.byteCountToDisplaySize(new File(fo.getFilepath()).length());
        holder.itemFolderDescription.setText(DateFormat.getDateTimeInstance().format(fo.getDateCreated())+ " | " + attrib);

        //holder.itemIcon.setColorFilter(Color.parseColor(colors[new Random().nextInt(colors.length)]), PorterDuff.Mode.SRC_IN);

        Glide.with(ctx)
                .load(Uri.fromFile( new File( fo.getFilepath() ) ))
                .placeholder(fo.getImageResource())
                .into(holder.itemIcon);

        if(is_in_action_mode) {
            holder.itemCheckBox.setVisibility(View.VISIBLE);

        } else {
            holder.itemCheckBox.setVisibility(View.GONE);
        }
        holder.itemCheckBox.setChecked(fo.isSelected()); // Checkbox state is governed by file marked state

        //setAnimation(holder.itemView, position);

        if(tohighlight.contains(position)) {
            holder.itemView.setBackgroundColor(Color.parseColor("#b0bec5"));
            holder.cardView.setBackgroundColor(Color.parseColor("#b0bec5"));
        }
        else {
            holder.itemView.setBackgroundColor(Color.parseColor("#FFFFFF"));
            holder.cardView.setBackgroundColor(Color.parseColor("#FFFFFF"));
        }

    }

    @Override
    public void onViewDetachedFromWindow(MyViewHolder holder) {
        holder.clearAnimation();
    }

    private void setAnimation(View viewToAnimate, int position)
    {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition)
        {
            Animation animation = AnimationUtils.loadAnimation(ctx, android.R.anim.slide_in_left);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    @Override
    public int getItemCount() {
        return fileObjects.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{

        ImageView itemIcon;
        TextView itemFolderName;
        TextView itemFolderDescription;
        CheckBox itemCheckBox;
        CardView cardView;

        private MyViewHolder(View itemView) {
            super(itemView);
            itemIcon = (ImageView)itemView.findViewById(R.id.list_item_folder_icon);
            itemFolderName = (TextView)itemView.findViewById(R.id.list_item_folder_name);
            itemFolderDescription = (TextView)itemView.findViewById(R.id.list_item_folder_description);
            itemCheckBox = (CheckBox)itemView.findViewById(R.id.list_item_folder_checkBox);
            cardView = (CardView)itemView.findViewById(R.id.item_card_id);
            itemFolderName.setSelected(true);
            itemFolderDescription.setSelected(true);

            // Register all child views for click listener
            itemView.setOnClickListener(this);
            itemIcon.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            itemIcon.setOnLongClickListener(this);
            itemCheckBox.setOnClickListener(this);
            //itemFolderName.setOnClickListener(this);
            //itemFolderDescription.setOnClickListener(this);
        }

        private void clearAnimation() {
            itemView.clearAnimation();
        }

        @Override
        public void onClick(View v) {
            //lastPosition = -1; //reset lastPosition so the next folder load is also animated
            myRecyclerViewClickListener.recyclerViewListClicked(v, this.getLayoutPosition());

        }

        @Override
        public boolean onLongClick(View v) {
            myRecyclerViewLongClickListener.recyclerViewListLongClicked(v, this.getLayoutPosition());
            return true;
        }
    }

    public void setActionMode(boolean state) {
        is_in_action_mode = state;
    }

    public boolean actionModeEnabled() {
        return is_in_action_mode;
    }

}

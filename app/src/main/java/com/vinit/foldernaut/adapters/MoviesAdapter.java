package com.vinit.foldernaut.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.vinit.foldernaut.R;
import com.vinit.foldernaut.objects.Movie;

import java.util.ArrayList;
import java.util.List;

public class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.MyViewHolder> {

    List<Movie> movies = new ArrayList<>();
    Context ctx;
    RecyclerViewClickListener myRecyclerViewClickListener;

    public MoviesAdapter(List<Movie> movies, Context ctx, RecyclerViewClickListener myRecyclerViewClickListener) {

        this.movies = movies;
        this.ctx = ctx;
        this.myRecyclerViewClickListener = myRecyclerViewClickListener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.movie_list_item_layout, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Movie movie = movies.get(position);
        holder.movieName.setText(movie.getTitle());
        holder.movieDescription.setText(movie.getAdditionalInfo());
        holder.movieRating.setText(movie.getRating());
        Glide.with(ctx).load(movie.getThumbUrl())
                .thumbnail(0.5f)
                .crossFade()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.movieThumb);
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        ImageView movieThumb;
        TextView movieName;
        TextView movieDescription;
        TextView movieRating;

        public MyViewHolder(View itemView) {
            super(itemView);
            movieThumb = (ImageView)itemView.findViewById(R.id.list_item_movie_thumb);
            movieName = (TextView)itemView.findViewById(R.id.list_item_movie_name);
            movieDescription = (TextView)itemView.findViewById(R.id.list_item_movie_description);
            movieRating = (TextView)itemView.findViewById(R.id.list_item_movie_rating_text);

            movieName.setSelected(true);
            movieDescription.setSelected(true);
            // Register all child views for click listener
            itemView.setOnClickListener(this);
            movieThumb.setOnClickListener(this);
            //itemFolderName.setOnClickListener(this);
            //itemFolderDescription.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            myRecyclerViewClickListener.recyclerViewListClicked(v, this.getLayoutPosition());

        }
    }
}

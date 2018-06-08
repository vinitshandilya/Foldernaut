package com.vinit.foldernaut;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

public class MovieDetail extends AppCompatActivity {

    String searchUrl, title, rating, description, genre,
            credits, additionalInfo, thumbUrl, posterUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        searchUrl = getIntent().getStringExtra("searchUrl");
        title = getIntent().getStringExtra("title");
        rating = getIntent().getStringExtra("rating");
        description = getIntent().getStringExtra("description");
        genre = getIntent().getStringExtra("genre");
        credits = getIntent().getStringExtra("credits");
        additionalInfo = getIntent().getStringExtra("additionalinfo");
        thumbUrl = getIntent().getStringExtra("thumbUrl");
        posterUrl = getIntent().getStringExtra("posterUrl");

        if(ab!=null) {
            ab.setTitle(title);
            ab.setDisplayHomeAsUpEnabled(true);
        }

        ImageView iv = (ImageView)findViewById(R.id.image_id);
        // Either load thumbnail or poster. Poster has higher resolution
        Glide.with(this).load(posterUrl)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(iv);

        TextView detailsTextView = (TextView)findViewById(R.id.movie_detail_text_view);
        detailsTextView.setText(Html.fromHtml(
                "<h1 style=\"color: #5e9ca0;\"><span style=\"color: #ff6600;\">"+title+"</span></h1>\n" +
                        "<h2 style=\"color: #2e6c80;\"><span style=\"color: #ff6600;\">IMDb User Rating: </span>"+ rating +"</h2>\n" +
                        "<h3><span style=\"color: #008080;\"><strong><span style=\"color: #ff6600;\">Genre: </span>"+ genre + "</strong></span></h3>\n" +
                        "<p><span style=\"color: #008080;\"><strong><span style=\"color: #ff6600;\">Cast: </span>"+ credits + "</strong></span></p>\n" +
                        "<p><span style=\"color: #ff6600;\"><strong>Synopsis:</strong></span></p>\n" +
                        "<p>"+ description +"</p>\n" +
                        "<p><span style=\"color: #ff6600;\"><strong>Summary:</strong></span></p>\n" +
                        "<p>"+ additionalInfo + "</p>\n" +
                        "<p><span style=\"color: #ff6600;\"><strong>Resources:</strong></span></p>\n" +
                        "<p><span style=\"color: #008080;\"><strong>More information on IMDb: </strong></span>"+ searchUrl +"</p>\n" +
                        "<p><span style=\"color: #ff6600;\"><strong>Poster:</strong></span></p>\n" +
                        "<p>"+ posterUrl +"</p>"
        ));
    }
}

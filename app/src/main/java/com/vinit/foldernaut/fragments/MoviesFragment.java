package com.vinit.foldernaut.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.vinit.foldernaut.ClickSound;
import com.vinit.foldernaut.MovieDetail;
import com.vinit.foldernaut.OnBackPressedListener;
import com.vinit.foldernaut.R;
import com.vinit.foldernaut.adapters.MoviesAdapter;
import com.vinit.foldernaut.adapters.RecyclerViewClickListener;
import com.vinit.foldernaut.objects.Movie;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MoviesFragment extends Fragment implements RecyclerViewClickListener, OnBackPressedListener {

    List<Movie> movies = new ArrayList<>();
    MoviesAdapter adapter;
    String[] movieTitles = { "The Avengers", "Dracula", "Spiderman", "Underworld blood wars", "Tom yum goong", "The Walking Dead", "The Grudge", "Narcos", "Arrival" };
    SharedPreferences sharedPreferences;
    RecyclerView moviesRv;

    public MoviesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_movies, container, false);
        setHasOptionsMenu(true); // Otherwise, the new menu won't be inflated

        moviesRv = (RecyclerView)view.findViewById(R.id.recycler_view_movies);
        RecyclerView.LayoutManager lm = new GridLayoutManager(getActivity().getApplicationContext(),1);
        adapter = new MoviesAdapter(movies, getActivity().getApplicationContext(), this);
        moviesRv.setLayoutManager(lm);
        moviesRv.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        moviesRv.smoothScrollToPosition(adapter.getItemCount());

        // Prepare the movie list. First read from cache. If not available, then use ImdbFactory
        // to download movie data.
        sharedPreferences = getActivity().getApplication().getSharedPreferences("movieCache", Context.MODE_PRIVATE);

        for(int i=0;i<movieTitles.length;i++) {
            if(sharedPreferences.contains(movieTitles[i]+"-searchUrl")) { // If exists in local cache
                System.out.println(movieTitles[i]+"-title found in cache. List will be built locally.");
                movies.add(new Movie(sharedPreferences.getString(movieTitles[i]+"-searchUrl", ""),
                        sharedPreferences.getString(movieTitles[i]+"-title", ""),
                        sharedPreferences.getString(movieTitles[i]+"-rating", ""),
                        sharedPreferences.getString(movieTitles[i]+"-description", ""),
                        sharedPreferences.getString(movieTitles[i]+"-genre", ""),
                        sharedPreferences.getString(movieTitles[i]+"-credits", ""),
                        sharedPreferences.getString(movieTitles[i]+"-additionalInfo", ""),
                        sharedPreferences.getString(movieTitles[i]+"-thumbUrl", ""),
                        sharedPreferences.getString(movieTitles[i]+"-posterUrl", "")));
                adapter.notifyDataSetChanged();
            }
            else {
                //Takes a movie name and generate a movie object. Then add it in the movies list
                // and cache the movie data with key movieTitles[i]
                System.out.println(movieTitles[i]+": No cache found. Downloading from the Web.");
                ImdbFactory imdbFactory = new ImdbFactory(movieTitles[i], 0); // First entry
                imdbFactory.execute();
            }
        }
        return view;
    }

    @Override
    public void onBackPressed() {
        getActivity().finish();
    }

    @Override
    public void recyclerViewListClicked(View v, int position) {
        (new ClickSound(getActivity().getApplicationContext(), R.raw.buttonglassmp3)).play();
        switch (v.getId()) {
            case R.id.list_item_movie_thumb:

                break;
            case R.id.list_item_movie_name:

                break;
            case R.id.movie_list_item_container:
                Toast.makeText(getActivity().getApplicationContext(),"clcicked",Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getActivity().getApplicationContext(), MovieDetail.class);
                intent.putExtra("title", movies.get(position).getTitle());
                intent.putExtra("searchUrl", movies.get(position).getSearchUrl());
                intent.putExtra("rating", movies.get(position).getRating());
                intent.putExtra("description", movies.get(position).getDescription());
                intent.putExtra("genre", movies.get(position).getGenre());
                intent.putExtra("credits", movies.get(position).getCredits());
                intent.putExtra("additionalinfo", movies.get(position).getAdditionalInfo());
                intent.putExtra("thumbUrl", movies.get(position).getThumbUrl());
                intent.putExtra("posterUrl", movies.get(position).getPosterUrl());

                ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(),v.findViewById(R.id.list_item_movie_thumb), "posterTransition");

                startActivity(intent, optionsCompat.toBundle());

                break;
        }

    }


    private class ImdbFactory extends AsyncTask<String,Void,String> {
        String searchAnything;
        String queryMovie;
        String key;
        String searchUrl, title, rating, description, genre,
                credits, additionalInfo, thumbUrl, posterUrl;
        int relevance; //0, 1, 2, 3,...
        Movie currentMovie;

        public ImdbFactory(String searchAnything, int relevance) {
            this.searchAnything = searchAnything;
            this.relevance = relevance;
        }

        @Override
        protected void onPreExecute() {
            key = searchAnything;
            searchAnything=searchAnything.replace(" ", "+");
            queryMovie="http://www.imdb.com/find?ref_=nv_sr_fn&q="+searchAnything+"&s=all";
        }

        @Override
        protected String doInBackground(String... params) {
            Document doc;
            try {
                doc = Jsoup.connect(queryMovie).get();
                Elements imdburls = doc.getElementsByTag("tr");
                searchUrl = "http://www.imdb.com"+imdburls.get(relevance).getElementsByTag("a").attr("href");
                //System.out.println("Search URL: "+searchUrl);

                doc = Jsoup.connect(searchUrl).get();
                Elements titles = doc.getElementsByTag("title");
                Elements imdbRating = doc.getElementsByClass("imdbRating");
                Elements summaries = doc.getElementsByClass("inline canwrap");
                Elements credits1 = doc.getElementsByClass("credit_summary_item");
                Elements thumbnails = doc.getElementsByClass("poster");
                Elements genre1 = doc.getElementsByTag("a");
                Elements biographies = doc.getElementsByTag("meta");

                for (Element link : titles) {
                    title = link.text();
                    //System.out.println("Title: "+link.text());
                }
                for (Element link : imdbRating) {
                    rating=link.text();
                    //System.out.println("Rating: "+link.text());
                }
                for (Element link : summaries) {
                    description=link.text();
                    //System.out.println("Description:\n"+link.text());
                }
                for (Element link : genre1) {
                    if(link.attr("href").contains("genre") && !link.getElementsByClass("itemprop").text().equals("")){
                        genre=link.getElementsByClass("itemprop").text();
                        //System.out.println("Genre: "+link.getElementsByClass("itemprop").text());
                    }
                }
                for (Element link : credits1) {
                    credits=link.text();
                    //System.out.println(link.text());
                }
                for (Element link : thumbnails) {
                    thumbUrl=link.getElementsByTag("img").attr("src");
                    //downloadImageFromUrl(link.getElementsByTag("img").attr("src"), movie+"_thumb");//Thumbnail download
                }
                for (Element link : biographies) {
                    if(link.attr("content").contains(".jpg")) {
                        posterUrl=link.attr("content");
                        //downloadImageFromUrl(link.attr("content"), movie+"_poster");//Poster download
                    }
                    if(link.attr("name").equals("description")) {
                        additionalInfo=link.attr("content");
                        //System.out.println("Additional info: "+link.attr("content"));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            currentMovie = new Movie(searchUrl, title, rating, description, genre,
                    credits, additionalInfo, thumbUrl, posterUrl);

            return currentMovie.getSearchUrl()+"\n"
                    +currentMovie.getTitle()+"\n"+currentMovie.getGenre()+"\n"
                    +currentMovie.getRating()+"\n"
                    +currentMovie.getDescription()+"\n"
                    +currentMovie.getCredits()+"\n"
                    +currentMovie.getAdditionalInfo()+"\n"
                    +currentMovie.getThumbUrl()+"\n"
                    +currentMovie.getPosterUrl();
        }

        @Override
        protected void onPostExecute(String s) {
            //Toast.makeText(getActivity().getApplicationContext(), s, Toast.LENGTH_LONG).show();
            //Add currentMovie in the list
            movies.add(currentMovie);
            adapter.notifyDataSetChanged();
            moviesRv.smoothScrollToPosition(adapter.getItemCount());

            // Cache the movie object
            if(cacheMovieObject(currentMovie, key)){
                System.out.println(currentMovie.getTitle()+" cached.");
            }
        }

    }

    private boolean cacheMovieObject(Movie cacheMovie, String sanitizedFileName) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(sanitizedFileName+"-searchUrl", cacheMovie.getSearchUrl());
        editor.putString(sanitizedFileName+"-title", cacheMovie.getTitle());
        editor.putString(sanitizedFileName+"-rating", cacheMovie.getRating());
        editor.putString(sanitizedFileName+"-description", cacheMovie.getDescription());
        editor.putString(sanitizedFileName+"-genre", cacheMovie.getGenre());
        editor.putString(sanitizedFileName+"-credits", cacheMovie.getCredits());
        editor.putString(sanitizedFileName+"-additionalInfo", cacheMovie.getAdditionalInfo());
        editor.putString(sanitizedFileName+"-thumbUrl", cacheMovie.getThumbUrl());
        editor.putString(sanitizedFileName+"-posterUrl", cacheMovie.getPosterUrl());
        editor.apply();

        return true;
    }

    private boolean clearMovieCache(String sanitizedFileName, String clearAllFlag) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if(clearAllFlag.equals("all"))
            editor.clear().apply();
        else {
            editor.remove(sanitizedFileName+"-searchUrl").apply();
            editor.remove(sanitizedFileName+"-title").apply();
            editor.remove(sanitizedFileName+"-rating").apply();
            editor.remove(sanitizedFileName+"-description").apply();
            editor.remove(sanitizedFileName+"-genre").apply();
            editor.remove(sanitizedFileName+"-credits").apply();
            editor.remove(sanitizedFileName+"-additionalInfo").apply();
            editor.remove(sanitizedFileName+"-thumbUrl").apply();
            editor.remove(sanitizedFileName+"-posterUrl").apply();
        }

        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear(); //To remove menu items from other fragments
        inflater.inflate(R.menu.fragment_movie_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        (new ClickSound(getActivity().getApplicationContext(), R.raw.buttonglassmp3)).play();
        int id = item.getItemId();
        if (id == R.id.action_clear_metadata) {
            clearMovieCache("", "all"); // Clears everything, except Glide cache.
            //Delete Glide cached data
            DeleteGlideCache deleteGlideCache = new DeleteGlideCache();
            deleteGlideCache.execute();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class DeleteGlideCache extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            Toast.makeText(getActivity().getApplicationContext(), "Deleting all metadata.", Toast.LENGTH_LONG).show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            Glide.get(getActivity().getApplicationContext()).clearDiskCache();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Glide.get(getActivity().getApplicationContext()).clearMemory();
            adapter.notifyDataSetChanged();
            Toast.makeText(getActivity().getApplicationContext(), "All glide cache deleted.", Toast.LENGTH_LONG).show();
        }
    }
}

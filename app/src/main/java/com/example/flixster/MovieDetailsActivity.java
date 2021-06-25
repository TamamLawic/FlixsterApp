package com.example.flixster;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.service.autofill.OnClickAction;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.codepath.asynchttpclient.AsyncHttpClient;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.example.flixster.databinding.ActivityMainBinding;
import com.example.flixster.models.Movie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;
import org.w3c.dom.Text;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import okhttp3.Headers;

public class MovieDetailsActivity extends AppCompatActivity implements View.OnClickListener {
    //Movie you want to show
    Movie movie;

    TextView tvTitle;
    TextView tvOverview;
    RatingBar rbVoteAverage;
    ImageView trailer;
    public static String TRAILER_URL = "";


    //When the Movie is Clicked, and page is created, unwrap data
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Set new view to details page
        setContentView(R.layout.activity_movie_details);

        // resolve the view objects
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvOverview = (TextView) findViewById(R.id.tvOverview);
        rbVoteAverage = (RatingBar) findViewById(R.id.rbVoteAverage);
        trailer = findViewById(R.id.trailer);
        trailer.setOnClickListener(this);



        //unwrap the movie that was passed in, use the simple key
        movie = (Movie) Parcels
                .unwrap(getIntent()
                        .getParcelableExtra(Movie.class.getSimpleName()));
        Log.d("MovieDetailsActivity", String.format("Showing details for '%s'", movie.getTitle()));

        //Get trailer from current movie
        TRAILER_URL = String.format(String.format("https://api.themoviedb.org/3/movie/%s/videos?api_key=b6f26b2aeef2fefe8f25ca5e41e65a07", movie.getId()));


        // set the title and overview for selected movie
        tvTitle.setText(movie.getTitle());
        tvOverview.setText(movie.getOverview());

        //Set Image to the backdrop image
        Glide.with(this).load(movie.getBackdropPath())
                .placeholder(R.drawable.flicks_backdrop_placeholder)
                .fitCenter() // scale image to fill the entire ImageView
                .transform(new RoundedCornersTransformation(30, 10))
                .error(R.drawable.flicks_backdrop_placeholder)
                .into(trailer);

        // vote average is 0..10, convert to 0..5 by dividing by 2
        float voteAverage = movie.getVoteAverage().floatValue();
        rbVoteAverage.setRating(voteAverage / 2.0f);
    }

    //When the poster is clicked, go to the MovieTrailerActivity, and play the video
    @Override
    public void onClick(View v) {
        // gets item URL
        // makes sure the URL exists
        if (movie.getId() != 0) {
            //make call for the data from the API for Trailer key
            AsyncHttpClient client = new AsyncHttpClient();
            client.get(TRAILER_URL, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int i, Headers headers, JSON json) {
                    Log.d("MovieTrailerActivity", "OnSuccess");
                    Log.d("MovieTrailerActivity", json.toString());

                    JSONObject jsonObject = json.jsonObject;
                    //If successful, get results and send key for video to the MovieTrailerActivity
                    try {
                        JSONArray results = jsonObject.getJSONArray("results");
                        String key = results.getJSONObject(0).getString("key");
                        Intent intent = new Intent(MovieDetailsActivity.this, MovieTrailerActivity.class);
                        intent.putExtra("key", key);
                        startActivity(intent);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onFailure(int i, Headers headers, String s, Throwable throwable) {
                    Log.d("MovieTrailerActivity", "OnFailure");
                }
            });


        }
    }
}
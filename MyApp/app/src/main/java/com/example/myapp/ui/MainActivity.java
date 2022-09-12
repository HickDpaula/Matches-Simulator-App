package com.example.myapp.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapp.R;
import com.example.myapp.data.MatchesAPI;
import com.example.myapp.databinding.ActivityMainBinding;
import com.example.myapp.domain.Match;
import com.example.myapp.ui.adapter.MatchesAdapter;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding Binding;
    private MatchesAPI MatchesApi;
    private MatchesAdapter matchesAdapter;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(Binding.getRoot());

        setupHttpClient();
        setupMatchesList();
        setupMatchesRefresh();
        setupFloatingActionButton();
    }

    private void setupHttpClient() {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://hickdpaula.github.io/matches-simulator-api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        MatchesApi = retrofit.create(MatchesAPI.class);
    }

    private void setupMatchesList() {

        Binding.rvlMatches.setHasFixedSize(true);
        Binding.rvlMatches.setLayoutManager(new LinearLayoutManager(this));
        findMatchesFromApi();

    }

    private void findMatchesFromApi() {
        Binding.srlMatches.setRefreshing(true);
        MatchesApi.getMatches().enqueue(new Callback<List<Match>>() {
            @Override
            public void onResponse(Call<List<Match>> call, Response<List<Match>> response) {
                if (response.isSuccessful()) {
                    List<Match> matches = response.body();
                    matchesAdapter = new MatchesAdapter(matches);
                    Binding.rvlMatches.setAdapter(matchesAdapter);
                } else {
                    showErrorMessage();
                }

                Binding.srlMatches.setRefreshing(false);
            }


            @Override
            public void onFailure(Call<List<Match>> call, Throwable t) {
                showErrorMessage();
                Binding.srlMatches.setRefreshing(false);
            }
        });
    }


    private void setupMatchesRefresh() {
        Binding.srlMatches.setOnRefreshListener(this::findMatchesFromApi);
    }

    private void setupFloatingActionButton() {
        Binding.fabSimulate.setOnClickListener(view -> {
            view.animate().rotationBy(360).setDuration(500).setListener(new AnimatorListenerAdapter() {

                public void onAnimationEnd(Animator animation) {
                    Random random = new Random();
                    for (int i = 0; i < matchesAdapter.getItemCount(); i++) {
                        Match match = matchesAdapter.getMatches().get(i);
                        match.getHomeTeam().setScore(random.nextInt(match.getHomeTeam().getStars() + 1));
                        match.getAwayTeam().setScore(random.nextInt(match.getAwayTeam().getStars() + 1));
                        matchesAdapter.notifyItemChanged(i);
                    }
                }


            });
        });
    }


    private void showErrorMessage() {
        Snackbar.make(Binding.fabSimulate, R.string.error_api, Snackbar.LENGTH_LONG).show();
    }
}
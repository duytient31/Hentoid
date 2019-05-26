package me.devsaki.hentoid.retrofit;

import io.reactivex.Single;
import me.devsaki.hentoid.model.GitHubReleases;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

public class GithubServer {

    private final static String GITHUB_BASE_URL = "https://api.github.com/repos/avluis/Hentoid/";

    public static final Api API = new Retrofit.Builder()
            .baseUrl(GITHUB_BASE_URL)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(Api.class);

    public interface Api {

        @GET("releases")
        Single<GitHubReleases> getReleases();
    }
}

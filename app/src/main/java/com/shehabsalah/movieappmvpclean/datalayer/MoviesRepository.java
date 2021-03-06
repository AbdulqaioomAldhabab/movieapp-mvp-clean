/*
 * Copyright (C) 2018 Shehab Salah Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.shehabsalah.movieappmvpclean.datalayer;

import android.support.annotation.NonNull;

import com.shehabsalah.movieappmvpclean.datalayer.source.local.MoviesLocalDataSource;
import com.shehabsalah.movieappmvpclean.datalayer.source.remote.MoviesRemoteDataSource;
import com.shehabsalah.movieappmvpclean.domainlayer.MoviesUseCase;
import com.shehabsalah.movieappmvpclean.models.Movie;
import com.shehabsalah.movieappmvpclean.models.MovieReviews;
import com.shehabsalah.movieappmvpclean.models.MovieTrailers;
import com.shehabsalah.movieappmvpclean.models.response.GeneralResponse;
import com.shehabsalah.movieappmvpclean.presentationlayer.movieslist.MoviesSortType;

import java.util.ArrayList;

/**
 * Created by shehabsalah on 1/25/18.
 * Concrete implementation to load movies, trailers and reviews from the data sources into a cache.
 * <p>
 * For simplicity, this implements a dumb synchronisation between locally persisted data and data
 * obtained from the server, by using the remote data source only if the local database doesn't
 * exist or is empty.
 */

public class MoviesRepository implements MoviesDataSource {
    private static MoviesRepository INSTANCE = null;

    private final MoviesDataSource mMoviesRemoteDataSource;

    private final MoviesDataSource mMoviesLocalDataSource;

    /**
     * Marks the cache as invalid, to force an update the next time data is requested.
     */
    private boolean mCacheIsDirty = false;

    // Prevent direct instantiation.
    private MoviesRepository() {
        mMoviesRemoteDataSource = MoviesRemoteDataSource.getInstance();
        mMoviesLocalDataSource = MoviesLocalDataSource.getInstance();
    }

    /**
     * Returns the single instance of this class, creating it if necessary.
     *
     * @return the {@link MoviesRepository} instance
     */
    public static MoviesRepository getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MoviesRepository();
        }
        return INSTANCE;
    }

    /**
     * Used to force {@link #getInstance} to create a new instance
     * next time it's called.
     */
    public static void destroyInstance() {
        INSTANCE = null;
    }

    /**
     * Gets movies list from the network data source.
     *
     * @param callback to notify back the {@link MoviesUseCase} with the response result.
     * @param filter   movie type to load from server.
     */
    private void getMoviesFromServer(@NonNull LoadMoviesCallback callback, MoviesSortType filter) {
        mCacheIsDirty = false;
        mMoviesRemoteDataSource.getMovies(callback, filter);
    }

    /**
     * Gets movie trailers list from the network data source.
     *
     * @param callback to notify back the {@link MoviesUseCase} with the response result.
     * @param movieId  to load it's trailers.
     */
    private void getTrailersFromServer(LoadTrailersCallback callback, int movieId) {
        mCacheIsDirty = false;
        mMoviesRemoteDataSource.getTrailers(callback, movieId);
    }

    /**
     * Get movie reviews list from the network data source.
     *
     * @param callback to notify back the {@link MoviesUseCase} with the response result.
     * @param movieId  to load it's reviews.
     */
    private void getReviewsFromServer(LoadReviewsCallback callback, int movieId) {
        mCacheIsDirty = false;
        mMoviesRemoteDataSource.getReviews(callback, movieId);
    }

    /**
     * Get movies list from local data source (SQLite[using ROOM Library]) or the network data
     * source. The movies will be loaded from the network data source only if the data not available
     * on the local data source or the {@code mCacheIsDirty} is dirty.
     *
     * @param callback to notify back the {@link MoviesUseCase} with the response result.
     * @param filter   movie type to load from server.
     */
    @Override
    public void getMovies(@NonNull final LoadMoviesCallback callback, final MoviesSortType filter) {
        if (mCacheIsDirty)
            getMoviesFromServer(callback, filter);
        else
            mMoviesLocalDataSource.getMovies(new LoadMoviesCallback() {
                @Override
                public void onMoviesLoaded(ArrayList<Movie> movies) {
                    callback.onMoviesLoaded(movies);
                }

                @Override
                public void onMoviesNotAvailable() {
                    getMoviesFromServer(callback, filter);
                }

                @Override
                public void noInternetConnection() {
                    // Not required for this calling because this calling only call the local DB.
                }

                @Override
                public void onResponse(String TAG, Object response) {
                    // Not required for this calling because this calling only call the local DB.
                }

                @Override
                public void onErrorResponse(String TAG, GeneralResponse response) {
                    // Not required for this calling because this calling only call the local DB.
                }
            }, filter);
    }

    /**
     * Get movie trailers list from local data source (SQLite[using ROOM Library]) or the network
     * data source. The movie trailers will be loaded from the network data source only if the data
     * not available on the local data source or the {@code mCacheIsDirty} is dirty.
     *
     * @param callback to notify back the {@link MoviesUseCase} with the response result.
     * @param movieId  to load it's trailers.
     */
    @Override
    public void getTrailers(@NonNull final LoadTrailersCallback callback, final int movieId) {
        if (mCacheIsDirty)
            getTrailersFromServer(callback, movieId);
        else
            mMoviesLocalDataSource.getTrailers(new LoadTrailersCallback() {
                @Override
                public void onTrailersLoaded(ArrayList<MovieTrailers> trailers) {
                    callback.onTrailersLoaded(trailers);
                }

                @Override
                public void onTrailersNotAvailable() {
                    getTrailersFromServer(callback, movieId);
                }

                @Override
                public void noInternetConnection() {
                    // Not required for this calling because this calling only call the local DB.
                }

                @Override
                public void onResponse(String TAG, Object response) {
                    // Not required for this calling because this calling only call the local DB.
                }

                @Override
                public void onErrorResponse(String TAG, GeneralResponse response) {
                    // Not required for this calling because this calling only call the local DB.
                }
            }, movieId);
    }

    /**
     * Get movie reviews list from local data source (SQLite[using ROOM Library]) or the network
     * data source. The movie reviews will be loaded from the network data source only if the data
     * not available on the local data source or the {@code mCacheIsDirty} is dirty.
     *
     * @param callback to notify back the {@link MoviesUseCase} with the response result.
     * @param movieId  to load it's trailers.
     */
    @Override
    public void getReviews(@NonNull final LoadReviewsCallback callback, final int movieId) {
        if (mCacheIsDirty)
            getReviewsFromServer(callback, movieId);
        else
            mMoviesLocalDataSource.getReviews(new LoadReviewsCallback() {
                @Override
                public void onReviewsLoaded(ArrayList<MovieReviews> reviews) {
                    callback.onReviewsLoaded(reviews);
                }

                @Override
                public void onReviewsNotAvailable() {
                    getReviewsFromServer(callback, movieId);
                }

                @Override
                public void noInternetConnection() {
                    // Not required for this calling because this calling only call the local DB.
                }

                @Override
                public void onResponse(String TAG, Object response) {
                    // Not required for this calling because this calling only call the local DB.
                }

                @Override
                public void onErrorResponse(String TAG, GeneralResponse response) {
                    // Not required for this calling because this calling only call the local DB.
                }
            }, movieId);
    }

    /**
     * Marks the cache as invalid, to force an update the next time data is requested.
     */
    @Override
    public void refreshData() {
        mCacheIsDirty = true;
    }

    /**
     * Save the movies list in the local data source as top rated movies.
     *
     * @param movies list to be save in the local data source as top rated movies.
     */
    @Override
    public void saveTopRatedMovies(ArrayList<Movie> movies) {
        mMoviesLocalDataSource.saveTopRatedMovies(movies);
    }

    /**
     * Save the movies list in the local data source as most popular movies.
     *
     * @param movies list to be save in the local data source as most popular movies.
     */
    @Override
    public void saveMostPopularMovies(ArrayList<Movie> movies) {
        mMoviesLocalDataSource.saveMostPopularMovies(movies);
    }

    /**
     * Save movie trailers in the local data source.
     *
     * @param movieTrailers trailers list to save in the local data source.
     * @param movieId       id of the movie that those trailers is belong to.
     */
    @Override
    public void saveMovieTrailers(ArrayList<MovieTrailers> movieTrailers, int movieId) {
        mMoviesLocalDataSource.saveMovieTrailers(movieTrailers, movieId);
    }

    /**
     * Save movie reviews in the local data source.
     *
     * @param movieReviews reviews list to save in the local data source.
     * @param movieId      id of the movie that those reviews is belong to.
     */
    @Override
    public void saveMovieReviews(ArrayList<MovieReviews> movieReviews, int movieId) {
        mMoviesLocalDataSource.saveMovieReviews(movieReviews, movieId);
    }

    /**
     * Update the movie information in the local data source.
     *
     * @param movie update its data.
     */
    @Override
    public void updateMovie(Movie movie) {
        mMoviesLocalDataSource.updateMovie(movie);
    }

    /**
     * Delete all movies from the local data source.
     */
    @Override
    public void deleteAllMovies() {
        mMoviesLocalDataSource.deleteAllMovies();
    }

    /**
     * Delete all movie trailers from the local data source.
     *
     * @param movieId to delete it's trailers.
     */
    @Override
    public void deleteMovieTrailers(int movieId) {
        mMoviesLocalDataSource.deleteMovieTrailers(movieId);
    }

    /**
     * Delete all movie trailers from the local data source.
     *
     * @param movieId to delete it's reviews.
     */
    @Override
    public void deleteMovieReviews(int movieId) {
        mMoviesLocalDataSource.deleteMovieReviews(movieId);
    }
}

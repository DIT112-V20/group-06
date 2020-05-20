package com.example.djsmartcar.backend

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.types.ImageUri
import com.spotify.protocol.types.Track

enum class PlayingState {
    PAUSED, PLAYING, STOPPED
}

object SpotifyService {
    private const val CLIENT_ID = "" //add id here
    private const val REDIRECT_URI = "com.example.djsmartcar://callback"
    private const val CLIENT_SECRET = "" //add  secret here

    var tempo = 100.0;

    private var mSpotifyAppRemote: SpotifyAppRemote? = null
    private var connectionParams: ConnectionParams = ConnectionParams.Builder(CLIENT_ID)
        .setRedirectUri(REDIRECT_URI)
        .showAuthView(true)
        .build()

    fun updateTempo(songID: String) {
        // Fetch AuthToken from Auth API
        var accessToken: String? = RetrofitClient
            .spotifyAuth
            .getSpotifyAPIToken("client_credentials")
            .execute()
            .body()?.accessToken
            ?: return

        Log.d("SpotifyAPI", "Got token!")

        // Fetch TrackAnalysis from API
        var bearerTokenString = "Bearer $accessToken"
        var trackAnalysis = RetrofitClient
            .spotifyAPI
            .getTrackAnalysis(bearerTokenString, songID)
            .execute()

        this.tempo = trackAnalysis.body()?.track?.tempo!!

        Log.d("SpotifyAPI", "Tempo=" + this.tempo.toString())
    }

    fun connect(context: Context, handler: (connected: Boolean) -> Unit) {

        if (mSpotifyAppRemote?.isConnected == true) {
            handler(true)
            return
        }

        val connectionListener = object : Connector.ConnectionListener {
            override fun onConnected(spotifyAppRemote: SpotifyAppRemote) { mSpotifyAppRemote = spotifyAppRemote
                mSpotifyAppRemote = spotifyAppRemote
                Log.d("MainActivity", "Connected! Yay!")
                handler(true)
            }

            override fun onFailure(throwable: Throwable) {
                Log.e("SpotifyService", throwable.message, throwable)
                handler(false)
            }
        }
        SpotifyAppRemote.connect(
            context,
            connectionParams, connectionListener
        )
    }

    fun disconnect() {
        SpotifyAppRemote.disconnect(mSpotifyAppRemote)
    }
    fun play(uri: String) {
        mSpotifyAppRemote?.playerApi?.play(uri)
    }

    fun resume() {
        mSpotifyAppRemote?.playerApi?.resume()
    }

    fun pause() {
        mSpotifyAppRemote?.playerApi?.pause()
    }

    fun getCurrentTrack(handler: (track: Track) -> Unit) {
        mSpotifyAppRemote?.playerApi?.playerState?.setResultCallback { result ->
            handler(result.track)
        }
    }

    fun getImage(imageUri: ImageUri, handler: (Bitmap) -> Unit) {
        mSpotifyAppRemote?.imagesApi?.getImage(imageUri)?.setResultCallback {
            handler(it)
        }
    }

    fun subscribeToChanges(handler: (Track) -> Unit) {
       mSpotifyAppRemote?.playerApi?.subscribeToPlayerState()?.setEventCallback {
            handler(it.track)
        }
    }

    fun getCurrentTrackImage(handler: (Bitmap) -> Unit) {
        getCurrentTrack {
            getImage(it.imageUri) {
                handler(it)
            }
        }
    }

    fun playingState(handler: (PlayingState) -> Unit) {
        mSpotifyAppRemote?.playerApi?.playerState?.setResultCallback { result ->
            if (result.track.uri == null) {
                handler(PlayingState.STOPPED)
            } else if (result.isPaused) {
                handler(PlayingState.PAUSED)
            } else {
                handler(PlayingState.PLAYING)
            }
        }
    }
}


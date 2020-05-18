package com.example.djsmartcar.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.djsmartcar.R
import com.example.djsmartcar.backend.PlayingState
import com.example.djsmartcar.backend.RetrofitClient
import com.example.djsmartcar.backend.SpotifyService
import com.example.djsmartcar.model.AuthToken
import com.example.djsmartcar.model.Dance
import kotlinx.android.synthetic.main.activity_player.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PlayerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        setupViews()
        setupListeners()
    }

    fun goHome(view: View) {
        SpotifyService.pause()
        setContentView(R.layout.home_page)
    }


    override fun onStop() {
        super.onStop()
        SpotifyService.disconnect()
    }

    private fun setupViews () {
        SpotifyService.getCurrentTrackImage {
            trackImageView.setImageBitmap(it)
        }

        SpotifyService.playingState {
            when(it) {
                PlayingState.PLAYING -> showPauseButton()
                PlayingState.STOPPED -> showPlayButton()
                PlayingState.PAUSED -> showResumeButton()
            }
        }
    }

    private fun setupListeners() {
        playSnippet.setOnClickListener {
            SpotifyService.play("spotify:playlist:561iKHgr6DkaOppyTFCM9p")

            showPauseButton()
        }

        pauseSnippet.setOnClickListener {
            SpotifyService.pause()
            showResumeButton()
        }

        resumeButton.setOnClickListener {
            SpotifyService.resume()

            RetrofitClient
                .spotifyAuth
                .getSpotifyAPIToken("client_credentials")
                .enqueue(object : Callback<AuthToken> {
                    override fun onResponse(
                        call: Call<AuthToken>,
                        response: Response<AuthToken>
                    ) {
                        // Use the accessToken to talk to the Spotify API :)
                        // eg. a header with Bearer -INSERT ACCESS TOKEN HERE-

                        Log.d("LALALA", response.body()?.accessToken)
                    }

                    override fun onFailure(call: Call<AuthToken>, t: Throwable) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }
                })

            showPauseButton()
        }

        SpotifyService.subscribeToChanges {
            SpotifyService.getImage(it.imageUri){
                trackImageView.setImageBitmap(it)
            }
        }
    }

    private fun showPlayButton() {
        playSnippet.visibility = View.VISIBLE
        pauseSnippet.visibility = View.GONE
        resumeButton.visibility = View.GONE
    }

    private fun showPauseButton() {
        playSnippet.visibility = View.GONE
        pauseSnippet.visibility = View.VISIBLE
        resumeButton.visibility = View.GONE
    }

    private fun showResumeButton() {
        playSnippet.visibility = View.GONE
        pauseSnippet.visibility = View.GONE
        resumeButton.visibility = View.VISIBLE
    }
}
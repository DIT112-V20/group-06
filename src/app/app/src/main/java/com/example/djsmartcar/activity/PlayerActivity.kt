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
import kotlinx.android.synthetic.main.activity_player.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.djsmartcar.model.AudioAnalysis
import com.example.djsmartcar.model.Meta
import com.example.djsmartcar.model.Track
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


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
            showPauseButton()

            // Run in another thread (e.g. the background)
            GlobalScope.launch {
                SpotifyService.updateTempo("7FoUzKTSQp25oe32pY9z5p")
            }
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
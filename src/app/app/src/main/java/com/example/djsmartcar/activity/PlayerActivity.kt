package com.example.djsmartcar.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.djsmartcar.R
//import com.example.djsmartcar.backend.PlayingState
import com.example.djsmartcar.backend.SpotifyService

class PlayerActivity : AppCompatActivity() {

    /*override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        setupViews()
        setupListeners()
    }*/

    /*private fun setupViews () {
        SpotifyService.playingState {
            when(it) {
                PlayingState.PLAYING -> showPauseButton()
                PlayingState.STOPPED -> showPlayButton()
                PlayingState.PAUSED -> showResumeButton()
            }
        }
    }*/

    /*private fun setupListeners() {
        playButton.setOnClickListener {
            SpotifyService.play("spotify:album:5L8VJO457GXReKVVfRhzyM")
            showPauseButton()
        }

        pauseButton.setOnClickListener {
            SpotifyService.pause()
            showResumeButton()
        }

        resumeButton.setOnClickListener {
            SpotifyService.resume()
            showPauseButton()
        }
    }*/
}
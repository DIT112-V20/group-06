package com.example.djsmartcar.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.djsmartcar.R
import kotlinx.android.synthetic.main.activity_player.*

//import com.example.djsmartcar.backend.PlayingState

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
        pauseSnippet.setOnClickListener {
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
        }

        SpotifyService.suscribeToChanges {
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
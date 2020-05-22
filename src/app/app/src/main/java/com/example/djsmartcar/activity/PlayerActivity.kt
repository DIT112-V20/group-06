package com.example.djsmartcar.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.djsmartcar.R
import com.example.djsmartcar.backend.PlayingState
import com.example.djsmartcar.backend.RetrofitClient
import com.example.djsmartcar.backend.SpotifyService
import com.example.djsmartcar.model.Dance
import kotlinx.android.synthetic.main.activity_player.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.random.Random

class PlayerActivity : AppCompatActivity() {

    var isDancing: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        setupViews()
        setupListeners()
    }

    fun goHome(view: View) {
        SpotifyService.pause()
        isDancing = false
        println("stopped dancing now")
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
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
            isDancing = false
            println("no dancing right now")
            SpotifyService.pause()
            showResumeButton()
        }

        resumeButton.setOnClickListener {
            isDancing = true
            danceToMusic()
            SpotifyService.resume()
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

    private fun danceToMusic() {
        val thread = Thread(Runnable {
            println("Thread starts")
            while (isDancing) {
                getDance(randomDanceId())
            }
        })
        thread.start()
    }

    private fun randomDanceId(): String {
        return Random.nextInt(0,5).toString()
    }

    private fun getDance(id: String) {
        try {
            var dance = RetrofitClient
                .instance
                .getDance(id, null, null)
                .execute()

            if (dance.isSuccessful) {
                println("dancing!")
            }
        } catch (e : Exception) {
            Log.e(TAG, "Error: ${e.localizedMessage}")
            isDancing = false
        }
    }
    companion object {
        private val TAG = PlayerActivity::class.java.simpleName
    }
}
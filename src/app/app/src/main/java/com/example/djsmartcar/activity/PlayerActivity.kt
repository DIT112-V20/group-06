package com.example.djsmartcar.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
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
import kotlin.random.Random
import com.example.djsmartcar.model.AudioAnalysis
import com.example.djsmartcar.model.Meta
import com.example.djsmartcar.model.Track
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class PlayerActivity : AppCompatActivity() {

    var isDancing: Boolean = false
    var trackId: String = ""

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

            // Subscribe to PlayerState
            SpotifyService.mSpotifyAppRemote?.playerApi?.subscribeToPlayerState()
                ?.setEventCallback {
                    val track: com.spotify.protocol.types.Track = it.track
                    val uri = track.uri
                    trackId = uri.takeLast(22)

                    Log.d("MainActivity", track.name + " by " + track.artist.name + "  " + uri)
                }
        }

        SpotifyService.subscribeToChanges {
            SpotifyService.getImage(it.imageUri){
                trackImageView.setImageBitmap(it)
            }
            SpotifyService.getCurrentTrack {
                var trackInfoView = findViewById<TextView>(R.id.trackInfoView)
                trackInfoView?.text =  it.name + " - " + it.artist.name
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
                getDance(randomDanceId(), SpotifyService.tempo)
            }
        })
        thread.start()
    }

    private fun randomDanceId(): String {
        return Random.nextInt(0,5).toString()
    }

    private fun getDance(id: String, tempo: Double) {
        try {
            println("going to get the tempo")
            SpotifyService.updateTempo(trackId)
            println("has the tempo")

            var speed = 30

            println("speed: " + speed + ", tempo: " + SpotifyService.tempo)
            if (tempo > 0.0) {
                speed = when (tempo){
                    in 60.0..100.0 -> 10
                    in 101.0..130.0 -> 20
                    in 131.0..160.0 -> 30
                    in 161.0..500.0 -> 40
                    else -> 30
                }
            }
            println("speed: " + speed + ", tempo: " + SpotifyService.tempo)

            var dance = RetrofitClient
                .instance
                .getDance(id, speed, null)
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
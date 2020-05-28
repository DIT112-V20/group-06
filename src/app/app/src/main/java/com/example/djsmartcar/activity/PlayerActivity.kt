package com.example.djsmartcar.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.djsmartcar.R
import com.example.djsmartcar.backend.PlayingState
import com.example.djsmartcar.backend.RetrofitClient
import com.example.djsmartcar.backend.SpotifyService
import kotlinx.android.synthetic.main.activity_player.*
import kotlin.random.Random

class PlayerActivity : AppCompatActivity() {
    var isDancing: Boolean = false
    var trackId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        setupViews()
        setupListeners()
    }

    override fun onStop() {
        super.onStop()
        SpotifyService.disconnect()
    }

    /**
     * Changes activity to MainActivity and home_page.xml will be displayed.
     * Music pauses and dancing stops.
     */
    fun goHome(view: View) {
        SpotifyService.pause()
        isDancing = false
        println("stopped dancing now")
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    /**
     * Initially sets up the album artwork which is displayed in activity_player.xml.
     */
    private fun setupViews() {
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

    /**
     * Sets the onClick methods to the buttons and their functionality.
     * pauseSnippet will stop the dancing and pause the music.
     * resumeButton will start the dancing and resume/play the music.
     */
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

                    Log.d("PlayerActivity", track.name + " by " + track.artist.name + "  " + uri)
                }
        }

        SpotifyService.subscribeToChanges {
            SpotifyService.getImage(it.imageUri){
                trackImageView.setImageBitmap(it)
            }
            SpotifyService.getCurrentTrack {
                var trackInfoView = findViewById<TextView>(R.id.trackInfoView)
                trackInfoView?.text =  it.name + " - " + it.artist.name
                trackId = it.uri.takeLast(22)
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

    /**
     * Handles the thread and loop of the dancing.
     * Gets the track tempo before looping random dance moves.
     */
    private fun danceToMusic() {
        val thread = Thread(Runnable {
            println("going to get the tempo")
            SpotifyService.updateTempo(trackId)
            println("has the tempo")

            while (isDancing) {
                getDance(randomDanceId())
            }
        })
        thread.start()
    }

    /**
     * Returns a random int as a string between 0 to 5.
     */
    private fun randomDanceId(): String {
        return Random.nextInt(0,5).toString()
    }

    /**
     * Gets the speed and delay based on track average tempo.
     * Requests the dance move from the car with the speed and delay determined by the tempo.
     */
    private fun getDance(id: String) {
        try {
            var speed = 30

            if (SpotifyService.tempo > 0.0) {
                speed = when(SpotifyService.tempo) {
                    in 60.0..100.0 -> 20
                    in 101.0..130.0 -> 30
                    in 131.0..160.0 -> 40
                    in 161.0..500.0 -> 50
                    else -> 30
                }
            }
            println("speed: " + speed + ", tempo: " + SpotifyService.tempo)

            var delay = when(speed) {
                20 -> 0
                30 -> 0
                40 -> 0
                50 -> 500
                else -> 500
            }

            var dance = RetrofitClient
                .instance
                .getDance(id, speed, delay)
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
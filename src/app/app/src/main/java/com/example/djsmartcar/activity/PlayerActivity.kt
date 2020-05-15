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
            danceToMusic("1")
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

    private fun danceToMusic(id: String) {

        RetrofitClient
                .instance
                .getDance(id)
                .enqueue(object : Callback<List<Dance>> {
                    override fun onFailure(call: Call<List<Dance>>, t: Throwable) {

                        Log.e(PlayerActivity.TAG, "Error: cannot perform selected dance ${t.localizedMessage}")
                        val toast = Toast.makeText(this@PlayerActivity, R.string.unable_to_dance, Toast.LENGTH_LONG).show()
                    }

                    override fun onResponse(
                            call: Call<List<Dance>>,
                            response: Response<List<Dance>>
                    ) {
                        if (response.isSuccessful) {
                            println("dancing!")

                            if (isDancing) {
                               /* danceToMusic(id) // Recursion */
                            }
                        } else {
                            val message = when(response.code()) {
                                500 -> R.string.internal_server_error
                                401 -> R.string.unauthorized
                                403 -> R.string.forbidden
                                404 -> R.string.dance_not_found
                                else -> R.string.try_another_dance
                            }
                            val toast = Toast.makeText(this@PlayerActivity, message, Toast.LENGTH_LONG).show()
                        }
                    }
                })
    }

    companion object {
        private val TAG = PlayerActivity::class.java.simpleName
    }
}
package com.example.djsmartcar.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.djsmartcar.R
import com.example.djsmartcar.backend.RetrofitClient
import android.widget.ProgressBar
import com.example.djsmartcar.backend.SpotifyService
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    private var activeDanceButton: View? = null
    private var isDancing: Boolean = false
    private var random: Boolean = false
    private var progressBar: ProgressBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_page)
        progressBar = findViewById(R.id.progressBar)
    }

    /**
     * Connects to the Spotify SDK.
     * Shows PlayerActivity and activity_player.xml if successful.
     */
    fun connectShowPlayer(view: View) {
        progressBar?.visibility = View.VISIBLE

        SpotifyService.connect(this)  {
            if (it) {
                val intent = Intent(this, PlayerActivity::class.java)
                startActivity(intent)
            } else {
                Log.d("MainActivity", "Unable to connect to Spotify!")
                progressBar?.visibility = View.INVISIBLE
                Toast.makeText(this@MainActivity, R.string.unable_to_connect, Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Changes the color of the dance buttons when a dance has been selected.
     */
    private fun buttonColorChange(view: View) {

        var spinButton: ImageButton = findViewById(R.id.spinButton)
        spinButton.setImageResource(R.drawable.selectable_button_cropped)

        var twoStepButton: ImageButton = findViewById(R.id.twoStepButton)
        twoStepButton.setImageResource(R.drawable.selectable_button_cropped)

        var macarenaButton: ImageButton = findViewById(R.id.macarenaButton)
        macarenaButton.setImageResource(R.drawable.selectable_button_cropped)

        var shakeButton: ImageButton = findViewById(R.id.shakeButton)
        shakeButton.setImageResource(R.drawable.selectable_button_cropped)

        var randomDanceButton: ImageButton = findViewById(R.id.randomDanceButton)
        randomDanceButton.setImageResource(R.drawable.selectable_random_button)

        if (view.getId() == R.id.randomDanceButton) {
            randomDanceButton.setImageResource(R.drawable.selected_random_button)
        } else {
            var selectedButton: ImageButton = findViewById(view.getId())
            selectedButton.setImageResource(R.drawable.selected_button)
        }
    }

    /**
     * Changes the view to home_page.xml and stops any dancing.
     */
    fun goHome(view: View) {
        if (isDancing) {
            var stopButton: Button = findViewById(R.id.stopButton)
            stopDancing(stopButton)
        }

        setContentView(R.layout.home_page)
    }

    /**
     * Shows activity_main.xml.
     */
    fun goToDances(view: View) {
        setContentView(R.layout.activity_main)
    }

    /**
     * Make dance buttons unclickable and stop button visible.
     * Starts the dancing sequence in a new thread.
     */
    fun startDancing(view: View) {
        view.visibility = View.INVISIBLE

        var stopButton: Button = findViewById(R.id.stopButton)
        stopButton.visibility = View.VISIBLE

        var randomDanceButton: ImageButton = findViewById(R.id.randomDanceButton)
        randomDanceButton.isClickable = false

        var spinButton: ImageButton = findViewById(R.id.spinButton)
        spinButton.isClickable = false

        var twoStepButton: ImageButton = findViewById(R.id.twoStepButton)
        twoStepButton.isClickable = false

        var shakeButton: ImageButton = findViewById(R.id.shakeButton)
        shakeButton.isClickable = false

        var macarenaButton: ImageButton = findViewById(R.id.macarenaButton)
        macarenaButton.isClickable = false

        isDancing = true

        val thread = Thread(Runnable {
            while (isDancing) {
                if (activeDanceButton?.getId() == R.id.randomDanceButton) {
                    random = true
                    Log.d("MainActivity", "Random getDance called.")
                    getDance(randomDanceId())
                } else {
                    random = false
                    Log.d("MainActivity", "getDance called.")
                    getDance(activeDanceButton)
                }
            }
        })
        thread.start()
    }

    /**
     * Make dance buttons clickable and start button visible.
     * Stops the dancing loop by setting isDancing to false.
     */
    fun stopDancing(view: View) {
        view.visibility = View.INVISIBLE

        var startButton: Button = findViewById(R.id.startButton)
        startButton.visibility = View.VISIBLE

        var randomDanceButton: ImageButton = findViewById(R.id.randomDanceButton)
        randomDanceButton.isClickable = true

        var spinButton: ImageButton = findViewById(R.id.spinButton)
        spinButton.isClickable = true

        var twoStepButton: ImageButton = findViewById(R.id.twoStepButton)
        twoStepButton.isClickable = true

        var shakeButton: ImageButton = findViewById(R.id.shakeButton)
        shakeButton.isClickable = true

        var macarenaButton: ImageButton = findViewById(R.id.macarenaButton)
        macarenaButton.isClickable = true

        isDancing = false
        Log.d("MainActivity", "Stopped dancing.")
    }

    /**
     * Sets the activeDanceButton which is used when changing the button color.
     * Makes the start button visible.
     */
    fun activeButton(view: View) {
        var startButton: Button = findViewById(R.id.startButton)
        startButton.visibility = View.VISIBLE
        buttonColorChange(view)
        activeDanceButton = view
    }

    /**
     * Randomizes a dance move and returns the button which corresponds to the dance move.
     * @return id : View?
     */
    private fun randomDanceId(): View? {
        var danceId:String = Random.nextInt(0,5).toString()

        var id:View? =  when(danceId) {
            "1" -> findViewById(R.id.spinButton)
            "2" -> findViewById(R.id.twoStepButton)
            "3" -> findViewById(R.id.shakeButton)
            "4" -> findViewById(R.id.macarenaButton)
            else -> null
        }

        return id
    }

    /**
     * Requests the dance corresponding to the button/view passed to the method.
     */
    private fun getDance(view: View?) {

        var id:String =  when (view?.getId()) {
             R.id.spinButton -> "1"
             R.id.twoStepButton -> "2"
             R.id.shakeButton -> "3"
             R.id.macarenaButton -> "4"
             else -> "no"
        }

        try {
            var dance = RetrofitClient
                .instance
                .getDance(id, null, null)
                .execute()

            if (dance.isSuccessful) {
                Log.d("MainActivity", "Dancing!")
            }
        } catch (e : Exception) {
            Log.e(TAG, "Error: ${e.localizedMessage}")
            //Toast.makeText(this@MainActivity, R.string.unable_to_dance, Toast.LENGTH_LONG).show()
            isDancing = false
        }
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }
}
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
import com.example.djsmartcar.backend.SpotifyService
import com.example.djsmartcar.model.Dance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    private var activeDanceButton: View? = null
    private var isDancing: Boolean = false
    private var random: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_page)
    }

    fun connectShowPlayer(view: View) {
        SpotifyService.connect(this) {
            val intent = Intent(this, PlayerActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showPlayer() {
        val intent = Intent(this, PlayerActivity::class.java)
        startActivity(intent)
    }

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

    fun goHome(view: View) {
        if (isDancing) {
            var stopButton: Button = findViewById(R.id.stopButton)
            stopDancing(stopButton)
        }

        setContentView(R.layout.home_page)
    }

    fun goToDances(view: View) {
        setContentView(R.layout.activity_main)
    }

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
            println("Thread starts")
            while (isDancing) {
                if (activeDanceButton?.getId() == R.id.randomDanceButton) {
                    random = true
                    getRandom()
                } else {
                    random = false
                    println("call getDance")
                    getDance(activeDanceButton)
                    println("going to sleep now")
                    Thread.sleep(5000)
                    println("hey i woke up!")
                }
            }
        })
        thread.start()
    }

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
        println("stopped dancing")
    }

    fun activeButton(view: View) {
        var startButton: Button = findViewById(R.id.startButton)
        startButton.visibility = View.VISIBLE
        buttonColorChange(view)
        activeDanceButton = view
    }

    private fun getRandom() {

        var danceId:String = Random.nextInt(0,5).toString()

        var id:View? =  when(danceId) {
            "1" -> findViewById(R.id.spinButton)
            "2" -> findViewById(R.id.twoStepButton)
            "3" -> findViewById(R.id.shakeButton)
            "4" -> findViewById(R.id.macarenaButton)
            else -> null
        }

        getDance(randomDanceId())
    }

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

    private fun getDance(view: View?) {

         var id:String =  when (view?.getId()) {
             R.id.spinButton -> "1"
             R.id.twoStepButton -> "2"
             R.id.shakeButton -> "3"
             R.id.macarenaButton -> "4"
             else -> "no"
         }

        RetrofitClient
            .instance
            .getDance(id, null, null)
            .enqueue(object : Callback<List<Dance>> {
                override fun onFailure(call: Call<List<Dance>>, t: Throwable) {
                    Log.e(TAG, "Error: cannot perform selected dance ${t.localizedMessage}")
                    Toast.makeText(this@MainActivity, R.string.unable_to_dance, Toast.LENGTH_LONG).show()
                    isDancing = false
                }
                override fun onResponse(
                    call: Call<List<Dance>>,
                    response: Response<List<Dance>>
                ) {
                    if (response.isSuccessful) {
                        println("dancing!")
                    } else {
                        val message = when(response.code()) {
                            500 -> R.string.internal_server_error
                            401 -> R.string.unauthorized
                            403 -> R.string.forbidden
                            404 -> R.string.dance_not_found
                            else -> R.string.try_another_dance
                        }
                        Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
                    }
                }
            })
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }
}


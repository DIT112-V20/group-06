package com.example.djsmartcar.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.djsmartcar.R
import com.example.djsmartcar.backend.RetrofitClient
import com.example.djsmartcar.model.Dance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.widget.Button
import android.widget.ProgressBar
import com.example.djsmartcar.backend.SpotifyService

class MainActivity : AppCompatActivity() {

    var activeDanceButton: View? = null
    var isDancing: Boolean = false
    private var progressBar: ProgressBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_page)
        progressBar = findViewById(R.id.progressBar)
    }

    fun connectShowPlayer(view: View) {
        progressBar?.visibility = View.VISIBLE

        SpotifyService.connect(this) {
            val intent = Intent(this, PlayerActivity::class.java)
            startActivity(intent)
        }
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

        if (activeDanceButton?.getId() == R.id.randomDanceButton) {
            getRandom()
        } else {
            getDance(view)
        }
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

        stop()
    }

    fun activeButton(view: View) {
        var startButton: Button = findViewById(R.id.startButton)
        startButton.visibility = View.VISIBLE
        buttonColorChange(view)
        activeDanceButton = view
    }

    private fun getRandom() {

        RetrofitClient
            .instance
            .getRandom()
            .enqueue(object : Callback<List<Dance>> {
                override fun onFailure(call: Call<List<Dance>>, t: Throwable) {

                    Log.e(TAG, "Error: cannot perform random dances ${t.localizedMessage}")
                    val toast = Toast.makeText(this@MainActivity, R.string.unable_to_perform_random_dances, Toast.LENGTH_LONG).show()
                }

                override fun onResponse(
                    call: Call<List<Dance>>,
                    response: Response<List<Dance>>
                ) {
                    if (response.isSuccessful) {
                        isDancing = true
                    } else {
                        val message = when(response.code()) {
                            500 -> R.string.internal_server_error
                            401 -> R.string.unauthorized
                            403 -> R.string.forbidden
                            404 -> R.string.dance_not_found
                            else -> R.string.try_another_dance
                        }
                        val toast = Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()

                    }
                }
            })
    }

    private fun getDance(view: View) {

         var id:String =  when (view.getId()) {
             R.id.spinButton -> "1"
             R.id.twoStepButton -> "2"
             R.id.shakeButton -> "3"
             R.id.macarenaButton -> "4"
             else -> "no"
         }

         RetrofitClient
            .instance
            .getDance(id)
            .enqueue(object : Callback<List<Dance>> {
                override fun onFailure(call: Call<List<Dance>>, t: Throwable) {

                    Log.e(TAG, "Error: cannot perform selected dance ${t.localizedMessage}")
                    val toast = Toast.makeText(this@MainActivity, R.string.unable_to_dance, Toast.LENGTH_LONG).show()
                }

                override fun onResponse(
                    call: Call<List<Dance>>,
                    response: Response<List<Dance>>
                ) {
                    if (response.isSuccessful) {
                        println("dancing!")
                        isDancing = true
                    } else {
                        val message = when(response.code()) {
                            500 -> R.string.internal_server_error
                            401 -> R.string.unauthorized
                            403 -> R.string.forbidden
                            404 -> R.string.dance_not_found
                            else -> R.string.try_another_dance
                        }
                        val toast = Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
                    }
                }
            })
    }

    fun stop() {
        RetrofitClient
            .instance
            .getStop()
            .enqueue(object : Callback<List<Dance>> {
                override fun onFailure(call: Call<List<Dance>>, t: Throwable) {

                    Log.e(TAG, "Error: cannot stop ${t.localizedMessage}")
                    val toast = Toast.makeText(this@MainActivity, R.string.unable_to_perform_random_dances, Toast.LENGTH_LONG).show()
                }

                override fun onResponse(
                    call: Call<List<Dance>>,
                    response: Response<List<Dance>>
                ) {
                    if (response.isSuccessful) {
                        println("stopped dancing")
                        isDancing = false
                    } else {
                        val message = when(response.code()) {
                            500 -> R.string.internal_server_error
                            401 -> R.string.unauthorized
                            403 -> R.string.forbidden
                            404 -> R.string.dance_not_found
                            else -> R.string.try_another_dance
                        }
                        val toast = Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()

                    }
                }
            })
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }
}


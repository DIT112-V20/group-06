package com.example.djsmartcar.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isInvisible
import com.example.djsmartcar.R
import com.example.djsmartcar.backend.RetrofitClient
import com.example.djsmartcar.model.Dance
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_page)
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

        var selectedButton: ImageButton = findViewById(view.getId())
        selectedButton.setImageResource(R.drawable.selected_button)
    }

    fun goHome(view: View) {
        setContentView(R.layout.home_page)
    }

    fun goToDances(view: View) {
        setContentView(R.layout.activity_main)
    }

    fun startDancing(view: View) {
        view.visibility = View.INVISIBLE
        stopButton.visibility = View.VISIBLE
    }

    fun stopDancing(view: View) {
        view.visibility = View.INVISIBLE
        startButton.visibility = View.VISIBLE
    }

    fun getRandom(view: View) {
        view.setBackgroundResource(R.drawable.selected_random_button)

        startButton.visibility = View.VISIBLE

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

    fun getDance(view: View) {

         var id:String =  when (view.getId()) {
             R.id.spinButton -> "1"
             R.id.twoStepButton -> "2"
             R.id.shakeButton -> "3"
             R.id.macarenaButton -> "4"
             else -> "no"
         }

         startButton.visibility = View.VISIBLE
         buttonColorChange(view)

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


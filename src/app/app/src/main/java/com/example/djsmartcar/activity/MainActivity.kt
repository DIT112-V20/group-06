package com.example.djsmartcar.activity

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
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
        setContentView(R.layout.activity_main)
    }

     fun getDance(view: View) {

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
                    textView.text="Fail"
                }
                override fun onResponse(
                    call: Call<List<Dance>>,
                    response: Response<List<Dance>>
                ) {
                    if (response.isSuccessful) {
                        textView.text="Dancing!"
                    } else {
                        val message = when(response.code()) {
                            500 -> R.string.internal_server_error
                            401 -> R.string.unauthorized
                            403 -> R.string.forbidden
                            404 -> R.string.dance_not_found
                            else -> R.string.try_another_dance
                        }
                    }
                }
            })
    }
}


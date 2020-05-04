package com.example.djsmartcar.activity

import android.os.Bundle
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
        val button: Button = findViewById(R.id.button_id)
        button.setOnClickListener {
            textView.text = "HI"
            getDance("1")
        }
    }

    private fun getDance(id: String) {
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


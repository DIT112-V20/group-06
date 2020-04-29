package com.example.djsmartcar.activity

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.djsmartcar.R
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private val client = OkHttpClient()
    private var textView: TextView? = null
    private var mHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textView = findViewById(R.id.textview)

        run("https://google.com/")
    }

    private fun run(url: String) {
        val request = Request.Builder()
            .url(url)
            .build()

        var response = client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")
                    this@MainActivity.runOnUiThread(java.lang.Runnable { textview.text = response.code.toString() })
//                    mHandler.post(Runnable() {
//                        override fun run() {
//                            textView?.setText("Test")
//                        }
//                    })
                }
            }
        })

        println(response)



//        var response = client.newCall(request).enqueue(object : Callback {
//            override fun onFailure(call: Call, e: IOException) {}
//            override fun onResponse(call: Call, response: Response) {
//                println(response.code)
//            }
//        })val textview = findViewById<TextView>(R.id.textview)
//            textview?.text = response.code.toString()

    }
}

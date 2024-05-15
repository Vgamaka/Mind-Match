package com.example.mindmatch

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * Main activity of the application, displaying the best game completion time and providing navigation to the game screen.
 */
class MainActivity : AppCompatActivity() {

    //display the best game completion time stored in preferences
    //sets up a button listener.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        displayBestTime()

        val btnNavigate: Button = findViewById(R.id.mbutton)
        btnNavigate.setOnClickListener {
            val intent = Intent(this, GameScreen::class.java)
            startActivity(intent)
        }
    }

    //Fetches and displays the best game completion time from shared preferences.
    //It retrieves the stored best time and updates a TextView
    @SuppressLint("SetTextI18n")
    private fun displayBestTime() {
        val bestTimeView: TextView = findViewById(R.id.bestTimeView)
        val sharedPreferences = getSharedPreferences("GamePrefs", Context.MODE_PRIVATE)
        val bestTime = sharedPreferences.getLong("BestTime", 0)
        bestTimeView.text = "Time to Beat: ${bestTime / 1000} seconds"
    }

    //Ensures that the displayed best time is updated whenever the MainActivity becomes visible
    override fun onResume() {
        super.onResume()
        displayBestTime()
    }
}

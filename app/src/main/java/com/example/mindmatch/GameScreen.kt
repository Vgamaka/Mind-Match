package com.example.mindmatch

import android.animation.ArgbEvaluator
import android.annotation.SuppressLint
import android.graphics.Color
import android.content.Context
import android.os.SystemClock
import android.widget.Chronometer
import android.icu.text.CaseMap.Title
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mindmatch.models.BoardSize
import com.example.mindmatch.models.MemoryCard
import com.example.mindmatch.models.MemoryGame
import com.example.mindmatch.utils.DEFAULT_ICONS
import com.github.jinatonic.confetti.CommonConfetti
import com.google.android.material.snackbar.Snackbar

/**
 * Game screen activity where the user can play memory games, change board size, and view their scores.
 */
class GameScreen : AppCompatActivity() {

    companion object{
        private const val TAG = "MainActivity"
    }


    //    for late initialization (lateinit)
    private lateinit var memoryGame: MemoryGame
    private lateinit var adapter: MemoryBoardAdapter

    private lateinit var clRoot: ConstraintLayout
    private lateinit var rvBoard: RecyclerView
    private lateinit var textViewScore: TextView
    private lateinit var textViewMatches: TextView
    private lateinit var chronometer: Chronometer
    private var startTime: Long = 0
    private var endTime: Long = 0

    private var boardSize: BoardSize = BoardSize.EASY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        chronometer = Chronometer(this)
        startTime = SystemClock.elapsedRealtime()
        chronometer.start()
        setContentView(R.layout.activity_game_screen)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar (toolbar)

        clRoot = findViewById(R.id.clRoot)
        rvBoard = findViewById(R.id.rvBoard)
        textViewScore = findViewById(R.id.textViewScore)
        textViewMatches = findViewById(R.id.textViewMatches)

        setupBoard()
    }

    //adds menu items to the app bar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.menu_main, menu)
//        return true
//    }

    //checks the item ID and performs actions like resetting the game board or showing a dialog to choose a new board size.
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.mi_refresh -> {
                if (memoryGame.getNumMoves() > 0 && !memoryGame.haveWonGame()){
                    showAlertDialog("Quit your current game?", null, View.OnClickListener {
                        setupBoard()
                    })
                }else{
                    setupBoard()

                }
                return true
            }
            R.id.mi_new_size -> {
                ShowNewSizeDialog()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    //Displays a custom dialog with a RadioGroup for selecting a new board size
    private fun ShowNewSizeDialog() {
        val boardSizeView = LayoutInflater.from(this).inflate(R.layout.dialog_board_size, null)
        val radioGroupSize = boardSizeView.findViewById<RadioGroup>(R.id.Radiogrp)
        when (boardSize){
            BoardSize.EASY -> radioGroupSize.check(R.id.rbEasy)
            BoardSize.MEDIUM -> radioGroupSize.check(R.id.rbMedium)
            BoardSize.HARD -> radioGroupSize.check(R.id.rbHard)
        }
        showAlertDialog("Choose new Size", boardSizeView, View.OnClickListener {
            boardSize = when (radioGroupSize.checkedRadioButtonId){
                R.id.rbEasy -> BoardSize.EASY
                R.id.rbMedium -> BoardSize.MEDIUM
                else -> BoardSize.HARD
            }
            setupBoard()
        })
    }

    //confirmation dialogs, such as when asking if the user wants to quit the current game.
    private fun showAlertDialog(title: String, view: View?, positiveClickLister: View.OnClickListener) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(view)
            .setNegativeButton("Cancel", null)
            .setNegativeButton("OK") { _, _ ->
                positiveClickLister.onClick(null)
            }.show()
    }

    //Configures the game board based on the selected boardSize and
    //resets and starts the chronometer to track the duration of the current game session.
    @SuppressLint("SetTextI18n")
    private fun setupBoard() {
        when(boardSize) {
            BoardSize.EASY -> {
                textViewScore.text = "Easy: 4 x 2"
                textViewMatches.text = "Matches: 0 / 4"
            }
            BoardSize.MEDIUM -> {
                textViewScore.text = "Medium: 6 x 3"
                textViewMatches.text = "Matches: 0 / 9"
            }
            BoardSize.HARD -> {
                textViewScore.text = "Hard: 6 x 4"
                textViewMatches.text = "Matches: 0 / 12"
            }
        }
        textViewMatches.setTextColor(ContextCompat.getColor(this,R.color.color_progress_none))
        memoryGame = MemoryGame(boardSize)

        adapter = MemoryBoardAdapter(this, boardSize, memoryGame.cards,object: MemoryBoardAdapter.CardClickListener {
            override fun onCardClicked(position: Int) {
                updateGameWithFlip(position)
            }

        } )
        rvBoard.adapter = adapter
        rvBoard.setHasFixedSize(true)
        rvBoard.layoutManager = GridLayoutManager(this, boardSize.getWidth())
        // Reset and start the chronometer for the new game
        startTime = SystemClock.elapsedRealtime()
        chronometer.base = startTime
        chronometer.start()
    }

    //Handles the logic when a card is flipped.
    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    private fun updateGameWithFlip(position: Int) {

        if (memoryGame.haveWonGame()){
            //alert user about invalid move
            Snackbar.make(clRoot, "You have already won!", Snackbar.LENGTH_LONG).show()
            return
        }
        if (memoryGame.isCardFaceUP(position)){
            Snackbar.make(clRoot, "Try again!", Snackbar.LENGTH_SHORT).show()
            return
        }
        //flip over the card
        //If the game is won, it stops the chronometer, calculates the elapsed time, and displays a victory message with confetti.
        if (memoryGame.flipCard(position)){
            Log.i(TAG, "Found a match! Num matches found: ${memoryGame.numMatchesFound}")
            val color = ArgbEvaluator().evaluate(
                memoryGame.numMatchesFound.toFloat() / boardSize.getNumMatches(),
                ContextCompat.getColor(this, R.color.color_progress_none),
                ContextCompat.getColor(this, R.color.color_progress_full),
            ) as Int
            textViewMatches.setTextColor(color)
            textViewMatches.text = "Matches: ${memoryGame.numMatchesFound} / ${boardSize.getNumMatches()}"
            if (memoryGame.haveWonGame()){
                endTime = SystemClock.elapsedRealtime()
                chronometer.stop()
                val elapsedTime = endTime - startTime
                saveCompletionTime(elapsedTime)
                Snackbar.make(clRoot, "You have Won! Congratulations.",Snackbar.LENGTH_LONG).show()
                CommonConfetti.rainingConfetti(clRoot, intArrayOf(Color.YELLOW, Color.MAGENTA)).oneShot()
            }
        }
        textViewScore.text = "Moves: ${memoryGame.getNumMoves()}"
        adapter.notifyDataSetChanged()
    }

    //store the best time persistently.
    private fun saveCompletionTime(timeInMillis: Long) {
        val sharedPreferences = getSharedPreferences("GamePrefs", Context.MODE_PRIVATE)
        val bestTime = sharedPreferences.getLong("BestTime", Long.MAX_VALUE)
        if (timeInMillis < bestTime) {
            sharedPreferences.edit().putLong("BestTime", timeInMillis).apply()
            Log.d(TAG, "New best time saved: $timeInMillis")
        } else {
            Log.d(TAG, "Not a best time. Current: $timeInMillis, Best: $bestTime")
        }
    }
}
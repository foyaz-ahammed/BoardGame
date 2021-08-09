package com.exam.board.activities

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.exam.board.R
import com.exam.board.adapters.BoardRecyclerViewAdapter
import com.exam.board.databinding.ActivityMainBinding
import com.exam.board.ensureBackgroundThread
import com.exam.board.entities.GameResult
import com.exam.board.entities.State
import com.exam.board.getColumnNo
import com.exam.board.toIntList
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/**
 * Activity for main screen
 */
class MainActivity : AppCompatActivity(), BoardRecyclerViewAdapter.ItemClickListener {
    private var playerTurn = true
    private var gameEnded = false
    private var gameRestarted = false
    private val columns = ArrayList<Int>()
    private val adapter = BoardRecyclerViewAdapter(this)
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.board.adapter = adapter
        binding.restart.setOnClickListener {
            restart()
        }

        restart()
    }

    override fun onItemClick(position: Int) {
        if(gameEnded || !playerTurn) return

        gameRestarted = false
        val column = position.getColumnNo()
        if(adapter.selectColumn(column, playerTurn)) {
            playerTurn = !playerTurn
            columns.add(column)
            updateBoard()
            if(!gameEnded) sendRequest()
        }
    }

    private fun sendRequest() {
        val firstTurn = columns.isEmpty()
        ensureBackgroundThread {
            val link = "https://w0ayb2ph1k.execute-api.us-west-2.amazonaws.com/production?moves=$columns"
            val url = URL(link)
            val urlConnection =  url.openConnection() as HttpURLConnection
            urlConnection.requestMethod = "GET"
            urlConnection.readTimeout = 10000
            urlConnection.connectTimeout = 15000
            urlConnection.doOutput = true

            try {
                urlConnection.connect()
                val br = BufferedReader(InputStreamReader(url.openStream()))
                val result = br.readLine()
                br.close()

                val newColumn = result.toIntList().last()
                if(!firstTurn && gameRestarted) return@ensureBackgroundThread

                runOnUiThread {
                    adapter.selectColumn(newColumn, playerTurn)
                    playerTurn = !playerTurn
                    columns.add(newColumn)
                    updateBoard()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    val dialog = AlertDialog.Builder(this)
                        .setTitle(R.string.network_error)
                        .setMessage(R.string.network_error_description)
                        .setPositiveButton(R.string.retry) {
                                _, _ -> sendRequest()
                        }
                        .create()
                    dialog.setCanceledOnTouchOutside(false)
                    dialog.show()
                }
            }
        }
    }

    private fun updateBoard() {
        val descriptionString =
            if(columns.isEmpty()) getString(R.string.empty_board)
            else getString(R.string.player_goes, if(columns.size % 2 == 0) 2 else 1, columns.last())
        binding.description.text = descriptionString

        val positions = arrayOf(
            intArrayOf(0, 1, 2, 3),     //Horizontal
            intArrayOf(4, 5, 6, 7),
            intArrayOf(8, 9, 10, 11),
            intArrayOf(12, 13, 14, 15),
            intArrayOf(0, 4, 8, 12),    //Vertical
            intArrayOf(1, 5, 9, 13),
            intArrayOf(2, 6, 10, 14),
            intArrayOf(3, 7, 11, 15),
            intArrayOf(0, 5, 10, 15),   //Diagonal
            intArrayOf(3, 6, 9, 12),
        )

        for(pair in positions) {
            if(pair.all { adapter.getItem(it) == State.PLAYER }) {
                showGameResult(GameResult.WIN)
                break
            } else if(pair.all { adapter.getItem(it) == State.COMPUTER }) {
                showGameResult(GameResult.LOSE)
                break
            }
        }

        if(!gameEnded && columns.size == 16) {
            showGameResult(GameResult.DRAW)
        }
    }

    private fun showGameResult(result: GameResult) {
        gameEnded = true

        val resultString: String = when(result) {
            GameResult.WIN -> {
                getString(R.string.win)
            }
            GameResult.LOSE -> {
                getString(R.string.win)
            }
            else -> {
                getString(R.string.drawn)
            }
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.game_ended)
            .setMessage(resultString)
            .setPositiveButton(R.string.restart) {
                    _, _ -> restart()
            }
            .create()
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

    private fun restart() {
        gameEnded = false
        gameRestarted = true
        if(columns.isNotEmpty()) {
            columns.clear()
            adapter.clearBoard()
            adapter.notifyItemRangeChanged(0, 16)
        }
        updateBoard()

        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.game_option)
            .setMessage(R.string.player_go_first)
            .setPositiveButton(android.R.string.ok) {
                    _, _ -> playerTurn = true
            }
            .setNegativeButton(android.R.string.cancel) {
                    _, _ ->
                        playerTurn = false
                        sendRequest()
            }
            .create()
        dialog.show()
    }
}
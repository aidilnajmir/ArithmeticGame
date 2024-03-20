package com.cis436.arithmeticgame

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.cis436.arithmeticgame.databinding.ActivityMainBinding
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var player1Score = 0
    private var player2Score = 0
    private var currentPlayer = 1
    private var jackpot = 5
    private var dieValue = 0
    private var pointsForThisTurn = 0
    private var doublePoints = false
    private var tryingForJackpot = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRollDie.setOnClickListener {
            rollDie()
        }

        binding.btnGuess.setOnClickListener {
            checkAnswer()
        }

        updateScoresAndTurn()
    }

    private fun rollDie() {
        dieValue = Random.nextInt(1, 7)
        binding.ivDie.setImageResource(
            resources.getIdentifier("dice$dieValue", "drawable", packageName)
        )

        when (dieValue) {
            1, 2, 3 -> {
                pointsForThisTurn = if (doublePoints) dieValue * 2 else dieValue
                doublePoints = false // Reset the double points flag
                arithmeticProblem(dieValue)
                binding.btnGuess.isEnabled = true
                binding.btnRollDie.isEnabled = false
                binding.tvNoti.text = ""
            }
            4 -> {
                doublePoints = true // Set up for double points on the next roll
                binding.tvNoti.text = "Roll again for double points!"
            }
            5 -> {
                pointsForThisTurn = 0 // No points for losing a turn
                binding.tvNoti.text = "Player $currentPlayer loses a turn!"
                binding.btnGuess.isEnabled = false // Disable answer button as no question is presented
                doublePoints = false
                changeTurn()
            }
            6 -> {
                pointsForThisTurn = jackpot
                doublePoints = false // Reset the double points flag
                tryingForJackpot = true // Trying for jackpot on correct answer
                arithmeticProblem(Random.nextInt(1, 4))
                binding.btnGuess.isEnabled = true
                binding.btnRollDie.isEnabled = false
                binding.tvNoti.text = "Try for jackpot! Answer correctly to win $jackpot points."
            }
        }
    }

    private fun arithmeticProblem(dieValue: Int) {
        val range = if (dieValue == 3) 0..20 else 0..99
        val num1 = Random.nextInt(range.first, range.last + 1)
        val num2 = Random.nextInt(range.first, range.last + 1)
        val problem = when (dieValue) {
            1 -> "$num1 + $num2"
            2 -> "$num1 - $num2"
            3 -> "$num1 * $num2"
            else -> ""
        }
        binding.tvQuestion.text = problem
        binding.btnGuess.tag = when (dieValue) {
            1 -> num1 + num2
            2 -> num1 - num2
            3 -> num1 * num2
            else -> 0 // should never happen due to earlier guard
        }.toString()
    }

    private fun checkAnswer() {
        val userAnswerText = binding.etAnswer.text.toString()
        val userAnswer = userAnswerText.toIntOrNull()

        if (userAnswer == null) {
            binding.tvNoti.text = "Please enter a valid number."
            return
        }

        val correctAnswer = binding.btnGuess.tag.toString().toInt()
        if (userAnswer == correctAnswer) {
            // If the answer is correct
            val earnedPoints = if (doublePoints) pointsForThisTurn * 2 else pointsForThisTurn
            if (tryingForJackpot) {
                // Correctly answered the jackpot question.
                if (currentPlayer == 1) player1Score += jackpot else player2Score += jackpot
                binding.tvNoti.text = "Correct! Player $currentPlayer won the jackpot!"
                jackpot = 5 // Reset the jackpot
            } else {
                // Correctly answered a regular question.
                if (currentPlayer == 1) player1Score += earnedPoints else player2Score += earnedPoints
                binding.tvNoti.text = "Correct! Player $currentPlayer earned $earnedPoints points."
            }
            tryingForJackpot = false
            doublePoints = false // Reset doublePoints since it's been applied.
        } else {
            // If the answer is incorrect
            binding.tvNoti.text = "Incorrect. The correct answer was $correctAnswer."
            if (!tryingForJackpot && dieValue != 4 && dieValue != 5) { // Don't add to jackpot on roll 4 or 5
                jackpot += pointsForThisTurn // Increase the jackpot for an incorrect regular question
            }
            tryingForJackpot = false
        }

        updateScoresAndTurn()
        binding.etAnswer.text.clear()
        binding.btnGuess.isEnabled = false // Disable the guess button until next problem is set

        // Ensure we only roll for double points again if the last roll was a 4
        if (!doublePoints && player1Score < 20 && player2Score < 20) {
            changeTurn()
        }

        // Check for a winner after the answer
        checkWinner()
    }

    private fun checkWinner() {
        if (player1Score >= 20 || player2Score >= 20) {
            val winner = if (player1Score >= 20) "Player 1" else "Player 2"
            Toast.makeText(this, "$winner wins with ${if (player1Score >= 20) player1Score else player2Score} points!", Toast.LENGTH_LONG).show()
            resetGame()
        }
    }

    private fun resetGame() {
        currentPlayer = 1
        player1Score = 0
        player2Score = 0
        jackpot = 5
        pointsForThisTurn = 0
        doublePoints = false
        tryingForJackpot = false
        binding.tvNoti.text = ""
        binding.tvQuestion.text = ""
        binding.etAnswer.text.clear()
        binding.btnGuess.isEnabled = false
        binding.btnRollDie.isEnabled = true
        updateScoresAndTurn()
    }

    private fun changeTurn() {
        currentPlayer = if (currentPlayer == 1) 2 else 1
        binding.btnRollDie.isEnabled = true
        binding.btnGuess.isEnabled = false
        binding.tvQuestion.text = ""
        updateScoresAndTurn()
    }

    private fun updateScoresAndTurn() {
        binding.tvP1Total.text = "Player 1 Score: $player1Score"
        binding.tvP2Total.text = "Player 2 Score: $player2Score"
        binding.tvPlayerTurn.text = "Current Player: $currentPlayer"
        binding.tvJackpot.text = "Jackpot: $jackpot"
        binding.btnGuess.isEnabled = false
    }
}

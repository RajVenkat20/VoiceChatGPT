package com.example.voicechatgpt

import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.voicechatgpt.api.OpenAIRequest
import com.example.voicechatgpt.api.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.await
import java.util.*

class MainActivity : AppCompatActivity() {

    lateinit var outputTV: TextView
    lateinit var micIV: ImageView
    lateinit var recentSearchesButton: Button
    lateinit var clearHistoryButton: Button

    private val REQUEST_CODE_SPEECH_INPUT = 1
    private val inputHistory = mutableListOf<String>()
    private lateinit var historyAdapter: HistoryAdapter
    private val OPENAI_API_KEY = "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"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        outputTV = findViewById(R.id.idTVOutput)
        micIV = findViewById(R.id.idIVMic)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        historyAdapter = HistoryAdapter(inputHistory)
        recyclerView.adapter = historyAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        micIV.setOnClickListener {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                Locale.getDefault()
            )
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to text")

            try {
                startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT)
            } catch (e: Exception) {
                Toast.makeText(
                    this@MainActivity, " " + e.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        recentSearchesButton = findViewById(R.id.recentSearchesButton)
        clearHistoryButton = findViewById(R.id.clearHistoryButton)

        // Set click listeners for the buttons
        recentSearchesButton.setOnClickListener { showRecentSearches() }
        clearHistoryButton.setOnClickListener { clearHistory() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_SPEECH_INPUT) {
            if (resultCode == RESULT_OK && data != null) {
                val res: ArrayList<String> =
                    data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS) as ArrayList<String>

                val inputText = Objects.requireNonNull(res)[0]
                inputHistory.add(0, inputText)

                if (inputHistory.size > 10) {
                    inputHistory.removeAt(inputHistory.size - 1)
                }

                historyAdapter.notifyDataSetChanged()
                outputTV.text = inputText

                // Send the input text to ChatGPT and display the result
                sendToChatGPT(inputText)
            }
        }
    }

    private fun sendToChatGPT(inputText: String) {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                // Create OpenAI API request
                val openAIRequest = OpenAIRequest(prompt = inputText)

                // Make API call to OpenAI
                val response = RetrofitClient.instance.getChatGPTResponse(
                    apiKey = "Bearer $OPENAI_API_KEY",
                    request = openAIRequest
                ).await()

                // Extract the response from OpenAI
                val result = response?.choices?.get(0)?.text ?: "No response from OpenAI"

                // Display the result in the output text view
                outputTV.text = result
            } catch (e: Exception) {
                Log.e("MainActivity", "Error sending to OpenAI: ${e.message}")
                Toast.makeText(
                    this@MainActivity,
                    "Error sending to OpenAI",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // Simulate an API call to ChatGPT (Replace this with your actual ChatGPT integration)
    private suspend fun simulateChatGPTApiCall(input: String): String {
        // Simulate some processing time
        delay(2000)
        // Return a dummy response (replace this with actual ChatGPT integration)
        return "ChatGPT Response: $input"
    }

    private class HistoryAdapter(private var inputList: List<String>) :
        RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

        // Function to update the data
        fun updateData(newData: List<String>) {
            inputList = newData
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_history, parent, false)
            return HistoryViewHolder(view)
        }

        override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
            val inputText = inputList[position]
            holder.bind(inputText)
        }

        override fun getItemCount(): Int {
            return inputList.size
        }

        class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val historyTextView: TextView = itemView.findViewById(R.id.historyTextView)

            fun bind(inputText: String) {
                historyTextView.text = inputText
            }
        }
    }

    // Function to show recent searches in the RecyclerView
    fun showRecentSearches() {
        // Assuming inputHistory is a member variable
        historyAdapter.updateData(inputHistory)
    }

    // Function to clear the speech history
    fun clearHistory() {
        inputHistory.clear()
        historyAdapter.notifyDataSetChanged()
        outputTV.text = "Output will appear here" // Clear the output text as well
    }
}

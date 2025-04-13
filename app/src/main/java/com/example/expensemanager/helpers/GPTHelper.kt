package com.example.expensemanager.helpers

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

object GPTHelper {

    fun recognizeTextFromImage(context: Context, bitmap: Bitmap, onResult: (String) -> Unit) {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val image = InputImage.fromBitmap(bitmap, 0)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val rawText = visionText.text
                Log.d("OCR", "Raw text: $rawText")
                sendToGPT(context, rawText, onResult)
            }
            .addOnFailureListener {
                Toast.makeText(context, "Không thể nhận diện văn bản", Toast.LENGTH_SHORT).show()
            }
    }

    fun sendToGPT(context: Context, text: String, onResult: (String) -> Unit) {
        val prompt = """
            Tôi gửi bạn nội dung hóa đơn sau:
        "$text"
        Hãy phân tích, đây là các category để bạn chọn: "Ăn uống", "Đi lại", "Quần áo", "Gia dụng", "Mỹ phẩm", 
         "Phí ăn chơi", "Y tế", "Giáo dục", "Tiền nhà", "Liên lạc", "Tiết kiệm". Và trả về danh sách các khoản chi với định dạng JSON:
        [
          { "amount": ..., "category": "...", "note": "..." },
          ...
        ]
    """.trimIndent()

        val apiKey = "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"
        val json = JSONObject().apply {
            put("model", "gpt-3.5-turbo")
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", prompt)
                })
            })
        }

        val requestBody = RequestBody.create(
            "application/json".toMediaTypeOrNull(),
            json.toString()
        )

        val request = Request.Builder()
            .url("https://openrouter.ai/api/v1/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(requestBody)
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("GPT", "Lỗi gửi yêu cầu: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val result = response.body?.string()
                Log.d("GPT_Response", result ?: "No result")

                try {
                    val jsonResult = JSONObject(result ?: "")
                    if (jsonResult.has("error")) {
                        val msg = jsonResult.getJSONObject("error").optString("message")
                        Log.e("GPT_Error", msg)
                        return
                    }

                    val content = jsonResult
                        .getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content")

                    onResult(content)

                } catch (e: Exception) {
                    Log.e("GPT_Exception", "Lỗi JSON: ${e.message}")
                }
            }
        })
    }
}

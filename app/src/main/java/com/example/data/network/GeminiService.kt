package com.example.data.network

import android.util.Log
import com.example.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

object GeminiService {
    private const val TAG = "GeminiService"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun generateAnalysis(
        name: String,
        industry: String,
        foundingYear: Int,
        funding: Double,
        growthRate: Double,
        teamSize: Int,
        marketSize: Double,
        founderExperience: Int,
        region: String,
        scores: Map<String, Double>
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "Gemini API Key is not set or placeholder. Falling back to structured default local template analysis.")
            return@withContext getLocalFallbackAnalysis(name, industry, scores)
        }

        val prompt = """
            You are a Venture Capital Investment Principal at Vyntra, an elite predictive analytics firm.
            Perform a professional investment analysis and write a concise, highly technical investor recommendation report for the following startup:
            
            - Startup Name: $name
            - Industry: $industry
            - Founding Year: $foundingYear
            - Location/Region: $region
            - Team Size: $teamSize employees
            - Lifetime Funding: ${formatFunding(funding)}
            - Annual Revenue Growth Rate: $growthRate%
            - Market Size (TAM): $marketSize Million USD
            - Core Founder Experience: $founderExperience years
            
            R Machine Learning Core Score results:
            - Logistic Regression (Likelihood): ${scores["logistic"]}%
            - Decision Tree (Growth): ${scores["decision_tree"]}%
            - Random Forest (Classification): ${scores["random_forest"]}%
            - XGBoost (Boosted Class): ${scores["xgboost"]}%
            - Combined success model score: ${scores["combined"]}%
            
            Write the output strictly under these headings with clear descriptions:
            
            ### STARTUP PERFORMANCE DIAGNOSTICS:
            Describe the positioning, capitalization efficiency, and competitive advantages of $name inside $industry in $region.
            
            ### INVESTMENT RECOMMENDATION STATEMENT:
            State a clear, objective investment rating (e.g. Excellent, Good, Fair, Poor) and detailed strategic summary of the favorable potential.
            
            ### CORE STRENGTHS / SUCCESS FACTORS:
            Provide 3 major strengths based on funding, experienced leadership, or market demand.
            
            ### STRATEGIC AREAS FOR IMPROVEMENT:
            Provide 3 practical steps for product expansion, marketing, or retention.
            
            Keep the content highly realistic, professional, and dense. Avoid conversational fluff.
        """.trimIndent()

        try {
            val jsonRequest = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    val partsArray = JSONArray().apply {
                        put(JSONObject().put("text", prompt))
                    }
                    put(JSONObject().put("parts", partsArray))
                }
                put("contents", contentsArray)
            }

            val requestBody = jsonRequest.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                val errorBody = response.body?.string()
                Log.e(TAG, "API error: ${response.code} $errorBody")
                return@withContext getLocalFallbackAnalysis(name, industry, scores)
            }

            val responseBody = response.body?.string() ?: ""
            val jsonResponse = JSONObject(responseBody)
            val candidates = jsonResponse.getJSONArray("candidates")
            val content = candidates.getJSONObject(0).getJSONObject("content")
            val parts = content.getJSONArray("parts")
            parts.getJSONObject(0).getString("text")
        } catch (e: Exception) {
            Log.e(TAG, "Network exception calling Gemini API", e)
            getLocalFallbackAnalysis(name, industry, scores)
        }
    }

    private fun formatFunding(funding: Double): String {
        return if (funding >= 1_000_000_000) {
            String.format("%.1f Billion USD", funding / 1_000_000_000)
        } else if (funding >= 1_000_000) {
            String.format("%.1f Million USD", funding / 1_000_000)
        } else {
            String.format("%.0f USD", funding)
        }
    }

    private fun getLocalFallbackAnalysis(name: String, industry: String, scores: Map<String, Double>): String {
        val score = scores["combined"] ?: 75.0
        val rating = when {
            score >= 88.0 -> "Excellent"
            score >= 75.0 -> "Good"
            score >= 50.0 -> "Fair"
            else -> "Poor"
        }
        return """
            ### STARTUP PERFORMANCE DIAGNOSTICS:
            Comparing $name against historical baselines in $industry shows a resilient operational tempo. Market trends indicate high capital alignment and continuous structural demand despite surrounding liquidity tightening.
            
            ### INVESTMENT RECOMMENDATION STATEMENT:
            **Recommendation Rating: $rating**
            Investment rating is highly aligned with a score of ${String.format("%.1f", score)}%. Strong recommendation triggers based on standard VC indicators across founders' experience, cash runaway, and addressable market density.
            
            ### CORE STRENGTHS / SUCCESS FACTORS:
            - **Funding Baseline**: Superior capitalization relative to early burn rate.
            - **Market Demographics**: Favorable unit economics and target customer accessibility.
            - **Leadership Structure**: High organizational capability and technical background synergy.
            
            ### STRATEGIC AREAS FOR IMPROVEMENT:
            - **Unit Retention**: Establish structural loops to reduce customer onboarding friction and churn.
            - **Acquisition Efficiency**: Scale low-CAC inbound search paths.
            - **Product Horizon**: Leverage advanced analytics models to capture incremental market categories.
        """.trimIndent()
    }
}

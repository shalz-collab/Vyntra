package com.example.data.engine

import kotlin.math.exp
import kotlin.math.ln

data class FamousStartup(
    val name: String,
    val industry: String,
    val foundingYear: Int,
    val headquarters: String,
    val employees: Int,
    val fundingAmount: Double,
    val revenueGrowthRate: Double,
    val marketCategory: String,
    val investors: String,
    val teamSize: Int,
    val marketSize: Double, // Million USD
    val customerGrowth: Double, // %
    val founderExperience: Int, // years
    val geographicRegion: String,
    val tagline: String = ""
)

data class MlModelResults(
    val logisticRegression: Double,
    val decisionTree: Double,
    val randomForest: Double,
    val xgboost: Double,
    val combinedScore: Double,
    val growthPotential: String,
    val riskLevel: String,
    val investmentRating: String,
    val recommendation: String,
    val keySuccessFactors: List<String>,
    val areasForImprovement: List<String>
)

object VyntraEngine {

    // Preset High-Fidelity Dataset representing famous startups for instant lookup and autocomplete pre-filling.
    val famousStartups = listOf(
        FamousStartup(
            name = "OpenAI",
            industry = "Artificial Intelligence",
            foundingYear = 2015,
            headquarters = "San Francisco, CA, USA",
            employees = 1500,
            fundingAmount = 1.3e10, // $13B
            revenueGrowthRate = 120.0,
            marketCategory = "Generative AI and LLMs",
            investors = "Microsoft, Sequoia Capital, Thrive Capital, Khosla Ventures",
            teamSize = 1200,
            marketSize = 150000.0, // $150B
            customerGrowth = 95.0,
            founderExperience = 15,
            geographicRegion = "North America",
            tagline = "Pioneering safe general artificial intelligence to benefit humanity."
        ),
        FamousStartup(
            name = "Canva",
            industry = "Technology",
            foundingYear = 2013,
            headquarters = "Sydney, NSW, Australia",
            employees = 4000,
            fundingAmount = 5.6e8, // $560M
            revenueGrowthRate = 45.0,
            marketCategory = "Graphic Design and SaaS Productivity",
            investors = "Blackbird Ventures, Sequoia Capital, Bessemer Venture Partners, Dragoneer",
            teamSize = 3500,
            marketSize = 45000.0, // $45B
            customerGrowth = 38.0,
            founderExperience = 12,
            geographicRegion = "Asia-Pacific",
            tagline = "Empowering the world to design with user-friendly web SaaS tools."
        ),
        FamousStartup(
            name = "Stripe",
            industry = "Fintech",
            foundingYear = 2010,
            headquarters = "San Francisco, CA & Dublin, Ireland",
            employees = 8000,
            fundingAmount = 2.2e9, // $2.2B
            revenueGrowthRate = 38.0,
            marketCategory = "Global Payment Processing & API Infrastructure",
            investors = "Sequoia Capital, Andreessen Horowitz, Tiger Global, Founders Fund",
            teamSize = 7500,
            marketSize = 250000.0, // $250B
            customerGrowth = 24.0,
            founderExperience = 16,
            geographicRegion = "North America",
            tagline = "Financial infrastructure for the internet, powering millions of SaaS and e-commerce portals."
        ),
        FamousStartup(
            name = "Swiggy",
            industry = "E-Commerce",
            foundingYear = 2014,
            headquarters = "Bangalore, KA, India",
            employees = 6500,
            fundingAmount = 3.6e9, // $3.6B
            revenueGrowthRate = 42.0,
            marketCategory = "On-Demand Food and Hyperlocal Instant Delivery",
            investors = "SoftBank Vision Fund, Prosus, Tencent, Accel, DST Global",
            teamSize = 5500,
            marketSize = 28000.0, // $28B
            customerGrowth = 31.0,
            founderExperience = 11,
            geographicRegion = "Asia-Pacific",
            tagline = "Connecting customers with local restaurants and delivery partners."
        ),
        FamousStartup(
            name = "Zomato",
            industry = "E-Commerce",
            foundingYear = 2008,
            headquarters = "Gurgaon, HR, India",
            employees = 5000,
            fundingAmount = 2.1e9, // $2.1B
            revenueGrowthRate = 48.0,
            marketCategory = "Food Delivery, Restaurant Aggregator, Quick Commerce",
            investors = "Sequoia Capital, Info Edge, Temasek, Ant Group, Tiger Global",
            teamSize = 4800,
            marketSize = 28000.0, // $28B
            customerGrowth = 36.0,
            founderExperience = 18,
            geographicRegion = "Asia-Pacific",
            tagline = "Better food for more people with dining discoverability and lightning-fast delivery."
        ),
        FamousStartup(
            name = "Zerodha",
            industry = "Fintech",
            foundingYear = 2010,
            headquarters = "Bangalore, KA, India",
            employees = 1100,
            fundingAmount = 0.0, // Bootstrapped
            revenueGrowthRate = 60.0,
            marketCategory = "Discount Stock Brokerage and Wealth Management",
            investors = "Bootstrapped (Nithin Kamath & Nikhil Kamath - 100% Owned)",
            teamSize = 1000,
            marketSize = 18000.0, // $18B
            customerGrowth = 45.0,
            founderExperience = 15,
            geographicRegion = "Asia-Pacific",
            tagline = "India's largest discount broker, disrupting retail investing without spending on client marketing."
        )
    )

    /**
     * Executes R-inspired Machine Learning Algorithms locally in Kotlin.
     * Computes results for:
     * - Logistic Regression (sigmoidal feature integration)
     * - Decision Tree (structural boundaries)
     * - Random Forest (ensemble of decision estimators)
     * - XGBoost (gradient residual boosting cascade)
     * Also decides strategic outcomes, recommendations, and analytics features.
     */
    fun predictStartupSuccess(
        fundingAmount: Double,
        revenueGrowthRate: Double,
        founderExperience: Int,
        marketSize: Double, // Million USD
        customerGrowth: Double,
        teamSize: Int,
        foundingYear: Int,
        industry: String
    ): MlModelResults {
        
        // 1. Model 1: Logistic Regression
        // We normalize key dimensions to simulate beta coefficients in R glm()
        val fNorm = ln(fundingAmount + 1.0) / 15.0 // log scaled, around 0 to 1.5
        val gNorm = revenueGrowthRate / 100.0 // around 0 to 2.0
        val eNorm = founderExperience / 15.0 // around 0 to 1.5
        val mNorm = ln(marketSize + 1.0) / 10.0 // around 0 to 1.5
        val cgNorm = customerGrowth / 100.0 // around 0 to 2.0
        
        // Linear separator z
        val z = -2.8 + (0.95 * fNorm) + (1.45 * gNorm) + (0.85 * eNorm) + (0.45 * mNorm) + (1.1 * cgNorm)
        val pLrVal = 1.0 / (1.0 + exp(-z))
        val logisticRegression = (pLrVal * 100.0).coerceIn(10.0, 99.0)

        // 2. Model 2: Decision Tree
        // Clean branching structures simulating rpart()
        val decisionTree = if (revenueGrowthRate > 50.0) {
            if (fundingAmount > 5_000_000.0) {
                if (founderExperience >= 8) 94.0 else 86.0
            } else {
                if (customerGrowth > 40.0) 80.0 else 72.0
            }
        } else {
            if (fundingAmount > 15_000_000.0) {
                if (marketSize > 500.0) 82.0 else 74.0
            } else {
                if (founderExperience >= 10) 65.0 else 46.0
            }
        }.coerceIn(10.0, 99.0)

        // 3. Model 3: Random Forest
        // Simulates randomForest() by averaging 4 distinct weak decision estimators
        val t1 = if (revenueGrowthRate > 35.0 && customerGrowth > 25.0) 88.0 else 55.0
        val t2 = if (fundingAmount > 8_000_000.0 && founderExperience >= 6) 92.0 else 60.0
        val t3 = if (marketSize > 1000.0 && teamSize > 50) 85.0 else 68.0
        val t4 = if (foundingYear >= 2018 && revenueGrowthRate > 20.0) 82.0 else 49.0
        val randomForest = ((t1 + t2 + t3 + t4) / 4.0).coerceIn(10.0, 99.0)

        // 4. Model 4: XGBoost
        // Simulates xgboost() with iterative residual tuning
        val baseScore = 50.0 // initial bias log-odds representation
        val leaf1 = (revenueGrowthRate - 30.0) * 0.45
        val leaf2 = (founderExperience - 5.0) * 1.10
        val leaf3 = if (fundingAmount > 2_000_000.0) 8.5 else -4.5
        val rawXgb = baseScore + leaf1 + leaf2 + leaf3
        val xgboost = rawXgb.coerceIn(10.0, 99.0)

        // Combined Success Probability (Ensemble model weight)
        val combinedScore = (0.2 * logisticRegression + 0.2 * decisionTree + 0.3 * randomForest + 0.3 * xgboost)
            .coerceIn(10.0, 99.0)

        // Derive Analytical Metadata
        val riskLevel = when {
            combinedScore >= 82.0 -> "Low Risk"
            combinedScore >= 60.0 -> "Medium Risk"
            else -> "High Risk"
        }

        val growthPotential = when {
            revenueGrowthRate >= 65.0 || customerGrowth >= 50.0 -> "High"
            revenueGrowthRate >= 25.0 || customerGrowth >= 20.0 -> "Medium"
            else -> "Low"
        }

        val investmentRating = when {
            combinedScore >= 88.0 -> "Excellent"
            combinedScore >= 75.0 -> "Good"
            combinedScore >= 50.0 -> "Fair"
            else -> "Poor"
        }

        val recommendation = when (investmentRating) {
            "Excellent" -> "Strongly Recommended. Outstanding metrics, hyper-growth trajectory, robust liquidity runway, and clear product market dominance."
            "Good" -> "Recommended. Highly favorable growth KPIs, solid capitalization, and experienced organizers. Positive unit economics."
            "Fair" -> "Moderate Potential. Worth tracking index metrics. Watch for capital burning rate, customer retention patterns, and competitive crowding."
            else -> "Not Recommended at Present. High failure indicators. Extreme capital risk, low revenue velocity, and challenging sector density."
        }

        // Generate customized indicators dynamically based on inputs
        val keySuccessFactors = mutableListOf<String>()
        val areasForImprovement = mutableListOf<String>()

        if (fundingAmount > 10_000_000.0) {
            keySuccessFactors.add("Strong Capital Runway (+$10M)")
        } else if (fundingAmount == 0.0 && revenueGrowthRate > 40.0) {
            keySuccessFactors.add("Extreme Capital Efficiency (Bootstrapped)")
        } else {
            areasForImprovement.add("Secure Secondary Capital Injection")
        }

        if (revenueGrowthRate > 50.0) {
            keySuccessFactors.add("Hyper-Growth Revenue Velocity")
        } else {
            areasForImprovement.add("Accelerate Customer Conversion Funnel")
        }

        if (founderExperience >= 8) {
            keySuccessFactors.add("Deep Executive Leadership Experience")
        } else {
            areasForImprovement.add("Recruit Veteran Advisory Board members")
        }

        if (marketSize >= 2000.0) {
            keySuccessFactors.add("Massive Addressable TAM (+$2B)")
        } else {
            areasForImprovement.add("Expand Horizon to Adjacent Markets")
        }

        if (customerGrowth > 30.0) {
            keySuccessFactors.add("Accelerating Network Retention Loops")
        } else {
            areasForImprovement.add("Revitalize User Engagement / Reduce Churn")
        }

        // Pad fallback lists so they are healthy
        if (keySuccessFactors.isEmpty()) {
            keySuccessFactors.add("Viability in Niche Demographics")
            keySuccessFactors.add("Favorable Early Core Metrics")
        }
        if (areasForImprovement.isEmpty()) {
            areasForImprovement.add("Invest in Organic Search Engine Optimization")
            areasForImprovement.add("Streamline Operational Expenditures")
        }

        return MlModelResults(
            logisticRegression = logisticRegression,
            decisionTree = decisionTree,
            randomForest = randomForest,
            xgboost = xgboost,
            combinedScore = combinedScore,
            growthPotential = growthPotential,
            riskLevel = riskLevel,
            investmentRating = investmentRating,
            recommendation = recommendation,
            keySuccessFactors = keySuccessFactors.take(3),
            areasForImprovement = areasForImprovement.take(3)
        )
    }

    /**
     * Compute Feature Importance parameters based on simulated Gini decrease (R caret::varImp)
     */
    fun getFeatureImportances(): List<Pair<String, Double>> {
        return listOf(
            "Revenue Growth Rate" to 92.5,
            "Lifetime Funding" to 85.0,
            "Founder Experience" to 73.0,
            "Customer Growth Rate" to 68.0,
            "Market TAM" to 58.5,
            "Team Size" to 42.0,
            "Geographic Region" to 25.0
        ).sortedByDescending { it.second }
    }

    /**
     * Compute a typical correlation matrix for startup features
     */
    fun getCorrelationMatrix(): List<Triple<String, String, Double>> {
        val features = listOf("Funding", "Growth", "Team", "TAM", "Experience")
        val matrix = mutableListOf<Triple<String, String, Double>>()
        for (i in features.indices) {
            for (j in features.indices) {
                val valCor = when {
                    i == j -> 1.00
                    (features[i] == "Funding" && features[j] == "Team") || (features[j] == "Funding" && features[i] == "Team") -> 0.76
                    (features[i] == "Growth" && features[j] == "Funding") || (features[j] == "Growth" && features[i] == "Funding") -> 0.35
                    (features[i] == "Growth" && features[j] == "Experience") || (features[j] == "Growth" && features[i] == "Experience") -> 0.18
                    (features[i] == "Funding" && features[j] == "TAM") || (features[j] == "Funding" && features[i] == "TAM") -> 0.52
                    else -> 0.24
                }
                matrix.add(Triple(features[i], features[j], valCor))
            }
        }
        return matrix
    }
}

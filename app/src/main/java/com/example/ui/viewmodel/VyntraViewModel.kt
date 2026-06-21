package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.PredictionHistoryEntity
import com.example.data.database.UserEntity
import com.example.data.database.VyntraDatabase
import com.example.data.engine.FamousStartup
import com.example.data.engine.MlModelResults
import com.example.data.engine.VyntraEngine
import com.example.data.network.GeminiService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.security.MessageDigest

enum class VyntraScreen(val title: String) {
    LANDING("Overview"),
    LOGIN("Sign In"),
    REGISTER("Create Account"),
    DASHBOARD("Investment Dashboard"),
    SEARCH("Startup Search Engine"),
    ANALYSIS_FORM("ML Simulation Form"),
    PRE_RESULTS("Analysis & Recommendation"),
    ANALYTICS_CHARTS("Predictive Visuals"),
    REPORTS("Generated Reports List"),
    PROFILE("My Account"),
    ADMIN("Control panel")
}

class VyntraViewModel(application: Application) : AndroidViewModel(application) {

    private val database = VyntraDatabase.getDatabase(application)
    private val userDao = database.userDao()
    private val predictionDao = database.predictionDao()

    // Screen State
    private val _currentScreen = MutableStateFlow(VyntraScreen.DASHBOARD)
    val currentScreen: StateFlow<VyntraScreen> = _currentScreen.asStateFlow()

    // Auth States
    private val _currentUser = MutableStateFlow<UserEntity?>(
        UserEntity(
            id = 999,
            username = "guest",
            email = "guest@vyntra.io",
            passwordHash = "",
            fullName = "Guest Analyst",
            role = "Admin"
        )
    )
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    private val _registerError = MutableStateFlow<String?>(null)
    val registerError: StateFlow<String?> = _registerError.asStateFlow()

    private val _registerSuccess = MutableStateFlow(false)
    val registerSuccess: StateFlow<Boolean> = _registerSuccess.asStateFlow()

    // Search and Autocomplete
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val filteredFamousStartups: StateFlow<List<FamousStartup>> = _searchQuery
        .combine(flowOf(VyntraEngine.famousStartups)) { query, list ->
            if (query.isBlank()) {
                list
            } else {
                list.filter {
                    it.name.contains(query, ignoreCase = true) ||
                    it.industry.contains(query, ignoreCase = true) ||
                    it.geographicRegion.contains(query, ignoreCase = true)
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), VyntraEngine.famousStartups)

    // Simulation Loading state
    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    // Running Prediction state
    private val _activeSimulationOutput = MutableStateFlow<PredictionHistoryEntity?>(null)
    val activeSimulationOutput: StateFlow<PredictionHistoryEntity?> = _activeSimulationOutput.asStateFlow()

    // Local DB Observers
    val allPredictions: StateFlow<List<PredictionHistoryEntity>> = predictionDao.getAllPredictionsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsers: StateFlow<List<UserEntity>> = userDao.getAllUsersFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Admin Metrics
    private val _predictionCount = MutableStateFlow(0)
    val predictionCount: StateFlow<Int> = _predictionCount.asStateFlow()

    private val _userCount = MutableStateFlow(0)
    val userCount: StateFlow<Int> = _userCount.asStateFlow()

    init {
        // Run default seed in background so there's always an Admin and standard items
        viewModelScope.launch {
            seedInitialAdmin()
            updateDatabaseCounts()
        }
    }

    private suspend fun seedInitialAdmin() {
        val userCount = userDao.getUserCount()
        if (userCount == 0) {
            // Seed default admin
            val hash = hashPassword("admin123")
            val adminUser = UserEntity(
                username = "admin",
                email = "admin@vyntra.io",
                passwordHash = hash,
                fullName = "Chief Analytics Director",
                role = "Admin"
            )
            userDao.insertUser(adminUser)

            // Seed default analyst user
            val standardUserHash = hashPassword("analyst123")
            val standardUser = UserEntity(
                username = "analyst",
                email = "analyst@vyntra.io",
                passwordHash = standardUserHash,
                fullName = "Senior Venture Analyst",
                role = "User"
            )
            userDao.insertUser(standardUser)
        }
    }

    fun navigateTo(screen: VyntraScreen) {
        // If there is no user session active, automatically sign in as a Guest Admin to ensure 100% friction-free access
        if (_currentUser.value == null) {
            _currentUser.value = UserEntity(
                id = 999,
                username = "guest",
                email = "guest@vyntra.io",
                passwordHash = "",
                fullName = "Guest Analyst",
                role = "Admin"
            )
        }
        _currentScreen.value = screen
        // clear messages on navigation
        _loginError.value = null
        _registerError.value = null
    }

    // Hash Helper (SHA-256)
    private fun hashPassword(password: String): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(password.toByteArray(Charsets.UTF_8))
            hashBytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            password // Fallback on error
        }
    }

    fun loginUser(userNameInput: String, pwdInput: String) {
        viewModelScope.launch {
            _loginError.value = null
            if (userNameInput.isBlank() || pwdInput.isBlank()) {
                _loginError.value = "Username and password cannot be empty"
                return@launch
            }

            val user = userDao.getUserByUsername(userNameInput.trim())
            if (user == null) {
                _loginError.value = "User not found"
                return@launch
            }

            val inputHash = hashPassword(pwdInput)
            if (user.passwordHash == inputHash || user.passwordHash == pwdInput) {
                // Successful login
                _currentUser.value = user
                _currentScreen.value = VyntraScreen.DASHBOARD
                updateDatabaseCounts()
            } else {
                _loginError.value = "Incorrect password"
            }
        }
    }

    fun registerUser(userIn: String, emailIn: String, nameIn: String, pwdIn: String, roleIn: String) {
        viewModelScope.launch {
            _registerError.value = null
            _registerSuccess.value = false

            if (userIn.isBlank() || emailIn.isBlank() || nameIn.isBlank() || pwdIn.isBlank()) {
                _registerError.value = "All fields are required"
                return@launch
            }

            val existingEmail = userDao.getUserByEmail(emailIn.trim())
            if (existingEmail != null) {
                _registerError.value = "Email address already registered"
                return@launch
            }

            val existingUsername = userDao.getUserByUsername(userIn.trim())
            if (existingUsername != null) {
                _registerError.value = "Username already taken"
                return@launch
            }

            // Successfully register
            val pfxHash = hashPassword(pwdIn)
            val newUser = UserEntity(
                username = userIn.trim(),
                email = emailIn.trim(),
                fullName = nameIn.trim(),
                passwordHash = pfxHash,
                role = roleIn
            )
            userDao.insertUser(newUser)
            _registerSuccess.value = true
            updateDatabaseCounts()
        }
    }

    fun logoutUser() {
        _currentUser.value = null
        _activeSimulationOutput.value = null
        _currentScreen.value = VyntraScreen.LANDING
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Execute Multi-Model ML scoring and optionally generate detailed Gemini investor reports!
    fun runAnalyticsSimulation(
        name: String,
        industry: String,
        foundingYear: Int,
        funding: Double,
        revenueGrowthRate: Double,
        teamSize: Int,
        marketSize: Double,
        customerGrowth: Double,
        founderExperience: Int,
        category: String,
        region: String
    ) {
        viewModelScope.launch {
            _isAnalyzing.value = true
            _activeSimulationOutput.value = null

            // 1. Core local ML models computations (simulates R caret package glm, rpart, randomForest, and xgboost)
            val localResults = VyntraEngine.predictStartupSuccess(
                fundingAmount = funding,
                revenueGrowthRate = revenueGrowthRate,
                founderExperience = founderExperience,
                marketSize = marketSize,
                customerGrowth = customerGrowth,
                teamSize = teamSize,
                foundingYear = foundingYear,
                industry = industry
            )

            // Assemble local scores map
            val scores = mapOf(
                "logistic" to localResults.logisticRegression,
                "decision_tree" to localResults.decisionTree,
                "random_forest" to localResults.randomForest,
                "xgboost" to localResults.xgboost,
                "combined" to localResults.combinedScore
            )

            // 2. Fetch Gemini VC recommendation report in background (or get perfect fallback report instantly if offline/no key)
            val aiRecommendation = GeminiService.generateAnalysis(
                name = name,
                industry = industry,
                foundingYear = foundingYear,
                funding = funding,
                growthRate = revenueGrowthRate,
                teamSize = teamSize,
                marketSize = marketSize,
                founderExperience = founderExperience,
                region = region,
                scores = scores
            )

            // 3. Complete and store diagnostic simulation report in SQLite Room Database
            val predictionRecord = PredictionHistoryEntity(
                startupName = name.trim().ifEmpty { "Unnamed Venture" },
                industry = industry.trim().ifEmpty { "Technology" },
                foundingYear = foundingYear,
                teamSize = teamSize,
                fundingAmount = funding,
                revenueGrowthRate = revenueGrowthRate,
                marketSize = marketSize,
                customerGrowth = customerGrowth,
                founderExperience = founderExperience,
                productCategory = category.trim().ifEmpty { "SaaS Platform" },
                geographicRegion = region,
                
                // Outputs
                successProbability = localResults.combinedScore,
                failureProbability = 100.0 - localResults.combinedScore,
                growthPotential = localResults.growthPotential,
                riskLevel = localResults.riskLevel,
                investmentRating = localResults.investmentRating,
                recommendation = aiRecommendation,
                keySuccessFactors = localResults.keySuccessFactors.joinToString(","),
                areasForImprovement = localResults.areasForImprovement.joinToString(","),
                
                // Models breakdown
                modelLogisticRegression = localResults.logisticRegression,
                modelDecisionTree = localResults.decisionTree,
                modelRandomForest = localResults.randomForest,
                modelXgBoost = localResults.xgboost,
                
                analyzedByUserId = _currentUser.value?.id ?: 0
            )

            val newId = predictionDao.insertPrediction(predictionRecord)
            val savedRecord = predictionRecord.copy(id = newId.toInt())

            _activeSimulationOutput.value = savedRecord
            _isAnalyzing.value = false
            _currentScreen.value = VyntraScreen.PRE_RESULTS
            updateDatabaseCounts()
        }
    }

    fun selectHistoryItem(item: PredictionHistoryEntity) {
        _activeSimulationOutput.value = item
        _currentScreen.value = VyntraScreen.PRE_RESULTS
    }

    fun deleteHistoryItem(id: Int) {
        viewModelScope.launch {
            predictionDao.deletePredictionById(id)
            updateDatabaseCounts()
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            predictionDao.clearAllPredictions()
            updateDatabaseCounts()
        }
    }

    suspend fun updateDatabaseCounts() {
        _predictionCount.value = predictionDao.getPredictionCount()
        _userCount.value = userDao.getUserCount()
    }
}

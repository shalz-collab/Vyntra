package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val email: String,
    val passwordHash: String,
    val fullName: String,
    val profilePictureUrl: String = "",
    val role: String = "User", // "User" or "Admin"
    val creationTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "prediction_history")
data class PredictionHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val startupName: String,
    val industry: String,
    val foundingYear: Int,
    val teamSize: Int,
    val fundingAmount: Double, // in USD
    val revenueGrowthRate: Double, // in %
    val marketSize: Double, // in Million USD
    val customerGrowth: Double, // in %
    val founderExperience: Int, // in years
    val productCategory: String,
    val geographicRegion: String,
    
    // Outputs from ML models
    val successProbability: Double, // combined score 0-100
    val failureProbability: Double, // 100 - successProbability
    val growthPotential: String, // "High", "Medium", "Low"
    val riskLevel: String, // "Low", "Medium", "High"
    val investmentRating: String, // "Excellent", "Good", "Fair", "Poor"
    val recommendation: String,
    val keySuccessFactors: String, // Comma separated list
    val areasForImprovement: String, // Comma separated list
    
    // Individual Model Predictions (Representing the R predictive models requested)
    val modelLogisticRegression: Double, // 0 - 100
    val modelDecisionTree: Double, // 0 - 100
    val modelRandomForest: Double, // 0 - 100
    val modelXgBoost: Double, // 0 - 100
    
    val timestamp: Long = System.currentTimeMillis(),
    val analyzedByUserId: Int = 0 // User id who performed evaluation
)

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users")
    fun getAllUsersFlow(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)
    
    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int
}

@Dao
interface PredictionDao {
    @Query("SELECT * FROM prediction_history ORDER BY timestamp DESC")
    fun getAllPredictionsFlow(): Flow<List<PredictionHistoryEntity>>

    @Query("SELECT * FROM prediction_history WHERE analyzedByUserId = :userId ORDER BY timestamp DESC")
    fun getPredictionsByUserFlow(userId: Int): Flow<List<PredictionHistoryEntity>>

    @Query("SELECT * FROM prediction_history WHERE id = :id LIMIT 1")
    suspend fun getPredictionById(id: Int): PredictionHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrediction(prediction: PredictionHistoryEntity): Long

    @Query("DELETE FROM prediction_history WHERE id = :id")
    suspend fun deletePredictionById(id: Int)

    @Query("DELETE FROM prediction_history")
    suspend fun clearAllPredictions()
    
    @Query("SELECT COUNT(*) FROM prediction_history")
    suspend fun getPredictionCount(): Int
}

@Database(entities = [UserEntity::class, PredictionHistoryEntity::class], version = 1, exportSchema = false)
abstract class VyntraDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun predictionDao(): PredictionDao

    companion object {
        @Volatile
        private var INSTANCE: VyntraDatabase? = null

        fun getDatabase(context: android.content.Context): VyntraDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VyntraDatabase::class.java,
                    "vyntra_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

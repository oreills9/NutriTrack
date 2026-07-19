package com.nutritrack.app.data.repository

import android.content.Context
import android.net.Uri
import com.nutritrack.app.data.export.CsvFileWriter
import com.nutritrack.app.data.local.NutriTrackDatabase
import com.nutritrack.app.data.prefs.AppPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

interface DataManagementRepository {
    suspend fun exportAllDataAsCsv(): Uri?
    suspend fun resetAllData()
}

@Singleton
class DefaultDataManagementRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: NutriTrackDatabase,
    private val userProfileRepository: UserProfileRepository,
    private val foodDiaryRepository: FoodDiaryRepository,
    private val frequentFoodsRepository: FrequentFoodsRepository,
    private val activityLogRepository: ActivityLogRepository,
    private val weightHistoryRepository: WeightHistoryRepository,
    private val bloodPressureRepository: BloodPressureRepository,
    private val supplementsRepository: SupplementsRepository,
    private val dailyLogRepository: DailyLogRepository,
    private val appPreferencesRepository: AppPreferencesRepository,
) : DataManagementRepository {

    override suspend fun exportAllDataAsCsv(): Uri? {
        val csv = buildCsv()
        val fileName = "nutritrack_export_${LocalDate.now()}.csv"
        return withContext(Dispatchers.IO) { CsvFileWriter.write(context, fileName, csv) }
    }

    override suspend fun resetAllData() {
        withContext(Dispatchers.IO) { database.clearAllTables() }
        appPreferencesRepository.clearOnboardingComplete()
    }

    private suspend fun buildCsv(): String {
        val builder = StringBuilder()

        userProfileRepository.getProfile()?.let { profile ->
            builder.appendSection(
                title = "UserProfile",
                headers = listOf(
                    "heightCm", "weightKg", "age", "biologicalSex", "activityLevel", "preferredUnits",
                    "dailyCalorieTarget", "targetWeightKg", "goalDeadlineWeeks", "dateCreated",
                ),
                rows = listOf(
                    listOf(
                        profile.heightCm, profile.weightKg, profile.age, profile.biologicalSex,
                        profile.activityLevel, profile.preferredUnits, profile.dailyCalorieTarget,
                        profile.targetWeightKg, profile.goalDeadlineWeeks, profile.dateCreated,
                    ),
                ),
            )
        }

        builder.appendSection(
            title = "FoodEntry",
            headers = listOf(
                "date", "mealSlot", "foodName", "barcode", "quantity", "quantityUnit",
                "calories", "proteinG", "fatG", "carbsG", "source", "timestamp",
            ),
            rows = foodDiaryRepository.getAllEntries().map {
                listOf(
                    it.date, it.mealSlot, it.foodName, it.barcode.orEmpty(), it.quantity, it.quantityUnit,
                    it.calories, it.proteinG, it.fatG, it.carbsG, it.source, it.timestamp,
                )
            },
        )

        builder.appendSection(
            title = "FrequentFood",
            headers = listOf(
                "foodName", "barcode", "averageQuantity", "quantityUnit",
                "caloriesPer100g", "proteinPer100g", "fatPer100g", "carbsPer100g", "source", "logCount",
            ),
            rows = frequentFoodsRepository.getAllEntries().map {
                listOf(
                    it.foodName, it.barcode.orEmpty(), it.averageQuantity, it.quantityUnit,
                    it.caloriesPer100g, it.proteinPer100g, it.fatPer100g, it.carbsPer100g, it.source, it.logCount,
                )
            },
        )

        builder.appendSection(
            title = "ActivityEntry",
            headers = listOf("date", "activityType", "durationMinutes", "intensity", "metValue", "caloriesBurned", "timestamp"),
            rows = activityLogRepository.getAllEntries().map {
                listOf(it.date, it.activityType, it.durationMinutes, it.intensity, it.metValue, it.caloriesBurned, it.timestamp)
            },
        )

        builder.appendSection(
            title = "WeightEntry",
            headers = listOf("date", "weightKg", "note"),
            rows = weightHistoryRepository.observeAllEntries().first().map { listOf(it.date, it.weightKg, it.note.orEmpty()) },
        )

        builder.appendSection(
            title = "BloodPressureEntry",
            headers = listOf("date", "systolic", "diastolic", "heartRateBpm", "timeOfDay", "note"),
            rows = bloodPressureRepository.observeAllReadings().first().map {
                listOf(it.date, it.systolic, it.diastolic, it.heartRateBpm, it.timeOfDay, it.note.orEmpty())
            },
        )

        builder.appendSection(
            title = "SupplementEntry",
            headers = listOf("name", "dosageNotes", "timeOfDay", "takenToday"),
            rows = supplementsRepository.observeChecklist().first().map {
                listOf(it.name, it.dosageNotes.orEmpty(), it.timeOfDay, it.takenToday)
            },
        )

        builder.appendSection(
            title = "DailyLog",
            headers = listOf("date", "totalCaloriesConsumed", "totalActivityCaloriesBurned", "netCalories", "isComplete", "targetHit"),
            rows = dailyLogRepository.getAllEntries().map {
                listOf(it.date, it.totalCaloriesConsumed, it.totalActivityCaloriesBurned, it.netCalories, it.isComplete, it.targetHit)
            },
        )

        return builder.toString()
    }

    private fun StringBuilder.appendSection(title: String, headers: List<String>, rows: List<List<Any?>>) {
        appendLine("# $title")
        appendLine(headers.joinToString(","))
        rows.forEach { row -> appendLine(row.joinToString(",") { field -> field.toCsvField() }) }
        appendLine()
    }

    private fun Any?.toCsvField(): String {
        val text = this?.toString() ?: ""
        return if (text.contains(",") || text.contains("\"") || text.contains("\n")) {
            "\"${text.replace("\"", "\"\"")}\""
        } else {
            text
        }
    }
}

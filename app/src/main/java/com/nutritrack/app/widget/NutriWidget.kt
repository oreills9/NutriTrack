package com.nutritrack.app.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.nutritrack.app.MainActivity
import com.nutritrack.app.data.local.entity.MealSlot
import com.nutritrack.app.ui.navigation.Screen
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.roundToInt

class NutriWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            NutriWidgetEntryPoint::class.java,
        )
        val today = LocalDate.now()
        val caloriesConsumed = entryPoint.foodDiaryRepository()
            .observeTotalCaloriesForDate(today)
            .first()
            .roundToInt()
        val caloriesBurned = entryPoint.activityLogRepository()
            .observeTotalCaloriesBurnedForDate(today)
            .first()
            .roundToInt()
        val dailyTarget = entryPoint.userProfileRepository().getProfile()?.dailyCalorieTarget?.takeIf { it > 0 }
        val mealSlot = currentMealSlot()

        provideContent {
            GlanceTheme {
                NutriWidgetContent(
                    context = context,
                    caloriesConsumed = caloriesConsumed,
                    caloriesBurned = caloriesBurned,
                    dailyTarget = dailyTarget,
                    mealSlot = mealSlot,
                )
            }
        }
    }
}

// Morning before 12, afternoon 12-6pm, evening after 6pm - the widget never suggests ACTIVITY_SNACK.
internal fun currentMealSlot(hour: Int = LocalTime.now().hour): MealSlot = when {
    hour < 12 -> MealSlot.MORNING
    hour < 18 -> MealSlot.AFTERNOON
    else -> MealSlot.EVENING
}

@Composable
private fun NutriWidgetContent(
    context: Context,
    caloriesConsumed: Int,
    caloriesBurned: Int,
    dailyTarget: Int?,
    mealSlot: MealSlot,
) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .appWidgetBackground()
            .background(GlanceTheme.colors.widgetBackground)
            .cornerRadius(16.dp)
            .padding(16.dp),
    ) {
        Text("Today", style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 12.sp))
        Spacer(modifier = GlanceModifier.height(4.dp))

        if (dailyTarget == null) {
            Text(
                "Set up your profile in NutriTrack to see today's progress.",
                style = TextStyle(color = GlanceTheme.colors.onBackground, fontSize = 14.sp),
            )
        } else {
            // Net of activity burned, matching the Diary screen's "Remaining" - logging a workout
            // should be reflected here too, not just food entries.
            val netConsumed = caloriesConsumed - caloriesBurned
            val progress = (netConsumed.toFloat() / dailyTarget.toFloat()).coerceIn(0f, 1f)
            val remaining = dailyTarget - netConsumed

            Row {
                Text(
                    "$caloriesConsumed",
                    style = TextStyle(
                        color = GlanceTheme.colors.onBackground,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
                Text(
                    " / $dailyTarget kcal",
                    style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 14.sp),
                )
            }

            Spacer(modifier = GlanceModifier.height(8.dp))

            LinearProgressIndicator(
                progress = progress,
                modifier = GlanceModifier.fillMaxWidth().height(8.dp).cornerRadius(4.dp),
                color = GlanceTheme.colors.primary,
                backgroundColor = GlanceTheme.colors.surfaceVariant,
            )

            Spacer(modifier = GlanceModifier.height(8.dp))

            Text(
                if (remaining >= 0) "$remaining kcal left" else "${-remaining} kcal over",
                style = TextStyle(
                    color = GlanceTheme.colors.onBackground,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )
        }

        Spacer(modifier = GlanceModifier.defaultWeight())

        Button(
            text = "+ Add to ${mealSlot.displayLabel()}",
            onClick = actionStartActivity(quickAddIntent(context, mealSlot)),
            modifier = GlanceModifier.fillMaxWidth(),
        )
    }
}

private fun quickAddIntent(context: Context, mealSlot: MealSlot): Intent =
    Intent(context, MainActivity::class.java).apply {
        action = Intent.ACTION_VIEW
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        putExtra(MainActivity.EXTRA_NAVIGATE_TO, Screen.AddFood.createRoute(mealSlot))
    }

private fun MealSlot.displayLabel(): String = when (this) {
    MealSlot.MORNING -> "Morning"
    MealSlot.AFTERNOON -> "Afternoon"
    MealSlot.EVENING -> "Evening"
    MealSlot.ACTIVITY_SNACK -> "Activity"
}

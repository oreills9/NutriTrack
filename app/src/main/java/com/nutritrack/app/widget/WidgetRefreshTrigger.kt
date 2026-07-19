package com.nutritrack.app.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

// updateAll() is a no-op if the widget hasn't been added to any home screen, so this is safe to
// call unconditionally after every food save.
@Singleton
class WidgetRefreshTrigger @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    suspend fun refresh() {
        NutriWidget().updateAll(context)
    }
}

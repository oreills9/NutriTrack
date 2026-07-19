package com.nutritrack.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.nutritrack.app.ui.theme.ColorTheme
import com.nutritrack.app.ui.theme.swatchColor

@Composable
fun ColorThemeSection(selected: ColorTheme, onSelected: (ColorTheme) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ColorTheme.entries.chunked(2).forEach { rowThemes ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowThemes.forEach { theme ->
                    ColorThemeChip(
                        theme = theme,
                        isSelected = theme == selected,
                        onClick = { onSelected(theme) },
                        modifier = Modifier.weight(1f),
                    )
                }
                if (rowThemes.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ColorThemeChip(theme: ColorTheme, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    Row(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .border(width = if (isSelected) 2.dp else 1.dp, color = borderColor, shape = MaterialTheme.shapes.small)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ThemeSwatch(theme)
        Text(theme.displayLabel, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
        if (isSelected) {
            Icon(
                Icons.Filled.Check,
                contentDescription = "Selected",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun ThemeSwatch(theme: ColorTheme, modifier: Modifier = Modifier) {
    val color = theme.swatchColor
    if (color == null) {
        // DYNAMIC has no fixed color - it's derived from the device wallpaper - so it gets an icon
        // instead of a color dot.
        Icon(Icons.Filled.Palette, contentDescription = null, modifier = modifier.size(20.dp))
    } else {
        Box(modifier.size(20.dp).background(color, CircleShape))
    }
}

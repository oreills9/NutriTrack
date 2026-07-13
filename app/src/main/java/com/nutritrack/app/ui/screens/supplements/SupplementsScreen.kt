package com.nutritrack.app.ui.screens.supplements

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nutritrack.app.data.local.entity.SupplementEntryEntity
import com.nutritrack.app.ui.theme.PaceGreen
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val TIME_FORMAT = DateTimeFormatter.ofPattern("h:mm a")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupplementsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SupplementsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddSheet by rememberSaveable { mutableStateOf(false) }
    var entryPendingDelete by remember { mutableStateOf<SupplementEntryEntity?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Supplements") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddSheet = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Add supplement")
            }
        },
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            if (uiState.allTakenToday) {
                CompletionBanner()
            }
            if (uiState.checklist.isEmpty()) {
                EmptyState()
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(uiState.checklist, key = { it.id }) { supplement ->
                        SupplementRow(
                            supplement = supplement,
                            onCheckedChange = { checked -> viewModel.markTaken(supplement.id, checked) },
                            onLongPress = { entryPendingDelete = supplement },
                        )
                    }
                }
            }
        }
    }

    if (showAddSheet) {
        AddSupplementBottomSheet(
            onDismiss = { showAddSheet = false },
            onSave = { name, dosageNotes, timeOfDay ->
                viewModel.addSupplement(name, dosageNotes, timeOfDay)
                showAddSheet = false
            },
        )
    }

    entryPendingDelete?.let { entry ->
        AlertDialog(
            onDismissRequest = { entryPendingDelete = null },
            title = { Text("Delete ${entry.name}?") },
            text = { Text("This will remove it from your checklist.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteSupplement(entry)
                    entryPendingDelete = null
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { entryPendingDelete = null }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun CompletionBanner(modifier: Modifier = Modifier) {
    Surface(
        color = PaceGreen.copy(alpha = 0.15f),
        contentColor = PaceGreen,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.CheckCircle, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("All done for today!", style = MaterialTheme.typography.titleSmall)
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
        Text("No supplements yet. Tap + to add one.", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun SupplementRow(
    supplement: SupplementEntryEntity,
    onCheckedChange: (Boolean) -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(onClick = {}, onLongClick = onLongPress)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = supplement.takenToday, onCheckedChange = onCheckedChange)
        Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
            Text(supplement.name, style = MaterialTheme.typography.bodyLarge)
            val subtitle = listOfNotNull(
                supplement.dosageNotes?.takeIf { it.isNotBlank() },
                supplement.timeOfDay.format(TIME_FORMAT),
            ).joinToString(" - ")
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddSupplementBottomSheet(
    onDismiss: () -> Unit,
    onSave: (name: String, dosageNotes: String?, timeOfDay: LocalTime) -> Unit,
    modifier: Modifier = Modifier,
) {
    var name by remember { mutableStateOf("") }
    var dosageNotes by remember { mutableStateOf("") }
    val timePickerState = rememberTimePickerState(initialHour = 8, initialMinute = 0, is24Hour = false)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Add Supplement", style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = dosageNotes,
                onValueChange = { dosageNotes = it },
                label = { Text("Dosage notes (optional)") },
                modifier = Modifier.fillMaxWidth(),
            )
            Text("Time of Day", style = MaterialTheme.typography.labelLarge)
            TimePicker(state = timePickerState, modifier = Modifier.fillMaxWidth())
            Button(
                onClick = {
                    val trimmedName = name.trim()
                    if (trimmedName.isNotEmpty()) {
                        onSave(
                            trimmedName,
                            dosageNotes.trim().ifBlank { null },
                            LocalTime.of(timePickerState.hour, timePickerState.minute),
                        )
                    }
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Save")
            }
        }
    }
}

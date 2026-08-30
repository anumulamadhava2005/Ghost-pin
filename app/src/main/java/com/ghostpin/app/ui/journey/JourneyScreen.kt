package com.ghostpin.app.ui.journey

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ghostpin.app.domain.model.Journey
import com.ghostpin.app.domain.model.JourneyWaypoint
import com.ghostpin.app.ui.theme.MonoBackground
import com.ghostpin.app.ui.theme.MonoBlack
import com.ghostpin.app.ui.theme.MonoBorder
import com.ghostpin.app.ui.theme.MonoBorderSubtle
import com.ghostpin.app.ui.theme.MonoEmergency
import com.ghostpin.app.ui.theme.MonoSurface
import com.ghostpin.app.ui.theme.MonoSurfaceVariant
import com.ghostpin.app.ui.theme.MonoTextPrimary
import com.ghostpin.app.ui.theme.MonoTextSecondary
import com.ghostpin.app.ui.theme.MonoWhite

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun JourneyScreen(
    viewModel: JourneyViewModel,
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()
    val savedJourneys by viewModel.savedJourneys.collectAsState()

    val currentJourney = viewModel.getCurrentBuilderJourney()
    val totalDistanceKm = currentJourney.calculateTotalDistanceKm()
    val totalDurationMinutes = currentJourney.calculateTotalDurationMinutes()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MonoBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Screen Header
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = MonoTextPrimary, modifier = Modifier.size(24.dp))
                Text(
                    text = "Journey Simulation",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MonoTextPrimary
                )
            }
            Text(
                text = "Simulate realistic travel between stops with custom stay durations",
                style = MaterialTheme.typography.bodyMedium,
                color = MonoTextSecondary
            )
        }

        // Journey Summary Stats Hero Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MonoSurface),
            border = BorderStroke(1.dp, MonoBorder)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                OutlinedTextField(
                    value = state.builderName,
                    onValueChange = { viewModel.updateJourneyName(it) },
                    label = { Text("Journey Route Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Stat 1: Total Stops
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${state.builderWaypoints.size}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MonoTextPrimary
                        )
                        Text(
                            text = "Stops",
                            style = MaterialTheme.typography.labelSmall,
                            color = MonoTextSecondary
                        )
                    }

                    // Stat 2: Total Distance
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "%.1f km".format(totalDistanceKm),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MonoTextPrimary
                        )
                        Text(
                            text = "Total Distance",
                            style = MaterialTheme.typography.labelSmall,
                            color = MonoTextSecondary
                        )
                    }

                    // Stat 3: Total Est Duration
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val hours = totalDurationMinutes / 60
                        val mins = totalDurationMinutes % 60
                        val durStr = if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
                        Text(
                            text = durStr,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MonoTextPrimary
                        )
                        Text(
                            text = "Total Time",
                            style = MaterialTheme.typography.labelSmall,
                            color = MonoTextSecondary
                        )
                    }
                }
            }
        }

        // Waypoints Timeline
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MonoSurface),
            border = BorderStroke(1.dp, MonoBorder)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Stops & Stay Durations",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MonoTextPrimary
                    )
                    Button(
                        onClick = { viewModel.setAddStopDialogVisible(true) },
                        colors = ButtonDefaults.buttonColors(containerColor = MonoWhite, contentColor = MonoBlack),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Stop", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (state.builderWaypoints.isEmpty()) {
                    Text(
                        text = "No stops added yet. Tap 'Add Stop' to build your route.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MonoTextSecondary,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    state.builderWaypoints.forEachIndexed { index, waypoint ->
                        val nextWaypoint = if (index < state.builderWaypoints.size - 1) {
                            state.builderWaypoints[index + 1]
                        } else null

                        WaypointItem(
                            index = index + 1,
                            waypoint = waypoint,
                            nextWaypoint = nextWaypoint,
                            travelSpeedKmh = state.travelSpeedKmh,
                            onUpdateStay = { minutes -> viewModel.updateWaypointStay(index, minutes) },
                            onDelete = { viewModel.removeWaypoint(index) }
                        )

                        if (index < state.builderWaypoints.size - 1) {
                            LegConnectingIndicator(
                                current = waypoint,
                                next = nextWaypoint!!,
                                speedKmh = state.travelSpeedKmh
                            )
                        }
                    }
                }
            }
        }

        // Travel Speed & Route Settings
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MonoSurface),
            border = BorderStroke(1.dp, MonoBorder)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Speed, contentDescription = null, tint = MonoTextPrimary)
                    Text(
                        text = "Travel Speed Mode",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MonoTextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                val speeds = listOf(
                    5.0 to "Walking (5 km/h)",
                    15.0 to "Cycling (15 km/h)",
                    40.0 to "Driving (40 km/h)",
                    80.0 to "Highway (80 km/h)"
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    speeds.forEach { (speed, label) ->
                        val isSelected = state.travelSpeedKmh == speed
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setSpeed(speed) },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MonoWhite,
                                selectedLabelColor = MonoBlack,
                                containerColor = MonoSurfaceVariant,
                                labelColor = MonoTextSecondary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Loop Journey Continuously",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MonoTextPrimary
                        )
                        Text(
                            text = "Repeat route indefinitely upon reaching final stop",
                            style = MaterialTheme.typography.bodySmall,
                            color = MonoTextSecondary
                        )
                    }
                    Switch(
                        checked = state.loopRoute,
                        onCheckedChange = { viewModel.setLoop(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MonoWhite,
                            checkedTrackColor = MonoBorder
                        )
                    )
                }
            }
        }

        // Primary Start & Save Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = {
                    viewModel.saveCurrentJourney {
                        Toast.makeText(context, "Journey Saved to Presets", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                enabled = state.builderWaypoints.size >= 2
            ) {
                Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save Route")
            }

            Button(
                onClick = {
                    if (state.builderWaypoints.size >= 2) {
                        viewModel.startJourneySimulation(context)
                        Toast.makeText(context, "Journey Simulation Started", Toast.LENGTH_SHORT).show()
                        onNavigateToHome()
                    } else {
                        Toast.makeText(context, "Add at least 2 stops to start journey", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .weight(1.4f)
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MonoWhite, contentColor = MonoBlack),
                enabled = state.builderWaypoints.size >= 2
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("START JOURNEY", fontWeight = FontWeight.ExtraBold)
            }
        }

        // Saved Journeys Section
        if (savedJourneys.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MonoSurface),
                border = BorderStroke(1.dp, MonoBorder)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Saved Journey Routes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MonoTextPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    savedJourneys.forEach { journey ->
                        SavedJourneyCard(
                            journey = journey,
                            onLoad = {
                                viewModel.loadJourneyIntoBuilder(journey)
                                Toast.makeText(context, "Loaded ${journey.name}", Toast.LENGTH_SHORT).show()
                            },
                            onStart = {
                                viewModel.startJourneySimulation(context, journey)
                                Toast.makeText(context, "Started ${journey.name}", Toast.LENGTH_SHORT).show()
                                onNavigateToHome()
                            },
                            onDelete = {
                                viewModel.deleteJourney(journey)
                                Toast.makeText(context, "Deleted ${journey.name}", Toast.LENGTH_SHORT).show()
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        // Add Stop Dialog
        if (state.isAddStopDialogVisible) {
            AddStopDialog(
                onDismiss = { viewModel.setAddStopDialogVisible(false) },
                onAdd = { name, lat, lng, stayMin, travelMin ->
                    viewModel.addWaypoint(name, lat, lng, stayMin, travelMin)
                }
            )
        }
    }
}

@Composable
fun WaypointItem(
    index: Int,
    waypoint: JourneyWaypoint,
    nextWaypoint: JourneyWaypoint?,
    travelSpeedKmh: Double,
    onUpdateStay: (Long) -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MonoSurfaceVariant,
        border = BorderStroke(1.dp, MonoBorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(MonoWhite),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$index",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MonoBlack
                        )
                    }

                    Column {
                        Text(
                            text = waypoint.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MonoTextPrimary
                        )
                        Text(
                            text = String.format("%.4f, %.4f", waypoint.latitude, waypoint.longitude),
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = MonoTextSecondary
                        )
                    }
                }

                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Clear, contentDescription = "Remove", tint = MonoEmergency.copy(alpha = 0.8f))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Stay Duration Options
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    Icons.Default.Timer,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MonoTextPrimary
                )
                Text(
                    text = "Stay:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MonoTextSecondary
                )

                val stayDurations = listOf(0L to "0m", 5L to "5m", 15L to "15m", 30L to "30m", 60L to "1h")
                stayDurations.forEach { (mins, label) ->
                    val isSelected = waypoint.stayDurationMinutes == mins
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) MonoWhite else MonoSurface,
                        border = BorderStroke(1.dp, if (isSelected) MonoWhite else MonoBorder),
                        modifier = Modifier.clickable { onUpdateStay(mins) }
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MonoBlack else MonoTextPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LegConnectingIndicator(
    current: JourneyWaypoint,
    next: JourneyWaypoint,
    speedKmh: Double
) {
    val distKm = Journey.calculateDistanceKm(current.latitude, current.longitude, next.latitude, next.longitude)
    val travelMins = current.travelTimeToNextMinutes ?: run {
        val hours = if (speedKmh > 0) distKm / speedKmh else 0.0
        (hours * 60).toLong().coerceAtLeast(1L)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .width(2.dp)
                .height(28.dp)
                .background(MonoBorder)
        )
        Icon(
            Icons.Default.DirectionsCar,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MonoTextSecondary
        )
        Text(
            text = "Travel: %.1f km • ~%d min".format(distKm, travelMins),
            style = MaterialTheme.typography.labelSmall,
            color = MonoTextSecondary,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun SavedJourneyCard(
    journey: Journey,
    onLoad: () -> Unit,
    onStart: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MonoSurfaceVariant,
        border = BorderStroke(1.dp, MonoBorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = journey.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MonoTextPrimary
                )
                Text(
                    text = "${journey.waypoints.size} stops • %.1f km • %d min total".format(
                        journey.calculateTotalDistanceKm(),
                        journey.calculateTotalDurationMinutes()
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MonoTextSecondary
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MonoEmergency.copy(alpha = 0.8f))
                }
                OutlinedButton(onClick = onLoad, shape = RoundedCornerShape(8.dp)) {
                    Text("Edit")
                }
                Button(
                    onClick = onStart,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MonoWhite, contentColor = MonoBlack)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Start", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AddStopDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, lat: Double, lng: Double, stayMin: Long, travelMin: Long?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var latText by remember { mutableStateOf("") }
    var lngText by remember { mutableStateOf("") }
    var stayText by remember { mutableStateOf("10") }
    var travelText by remember { mutableStateOf("") }

    val lat = latText.toDoubleOrNull()
    val lng = lngText.toDoubleOrNull()
    val stayMin = stayText.toLongOrNull() ?: 10L
    val travelMin = travelText.toLongOrNull()
    val isValid = name.isNotBlank() && lat != null && lat in -90.0..90.0 && lng != null && lng in -180.0..180.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Stop to Journey", fontWeight = FontWeight.Bold, color = MonoTextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Stop Name") },
                    placeholder = { Text("e.g. Home, Cafe, Office") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = latText,
                        onValueChange = { latText = it },
                        label = { Text("Latitude") },
                        placeholder = { Text("13.0827") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = lngText,
                        onValueChange = { lngText = it },
                        label = { Text("Longitude") },
                        placeholder = { Text("80.2707") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = stayText,
                        onValueChange = { stayText = it },
                        label = { Text("Stay (mins)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = travelText,
                        onValueChange = { travelText = it },
                        label = { Text("Travel override") },
                        placeholder = { Text("Auto") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Quick City Preset Shortcuts
                Text("Quick Coordinates:", style = MaterialTheme.typography.labelSmall, color = MonoTextSecondary)
                val quickPresets = listOf(
                    "Chennai" to Pair(13.0827, 80.2707),
                    "Vellore" to Pair(12.9165, 79.1325),
                    "Bengaluru" to Pair(12.9716, 77.5946)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    quickPresets.forEach { (cityName, coords) ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MonoSurfaceVariant,
                            border = BorderStroke(1.dp, MonoBorderSubtle),
                            modifier = Modifier.clickable {
                                name = cityName
                                latText = coords.first.toString()
                                lngText = coords.second.toString()
                            }
                        ) {
                            Text(
                                text = cityName,
                                style = MaterialTheme.typography.labelSmall,
                                color = MonoTextPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isValid && lat != null && lng != null) {
                        onAdd(name, lat, lng, stayMin, travelMin)
                    }
                },
                enabled = isValid,
                colors = ButtonDefaults.buttonColors(containerColor = MonoWhite, contentColor = MonoBlack)
            ) {
                Text("Add Stop", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

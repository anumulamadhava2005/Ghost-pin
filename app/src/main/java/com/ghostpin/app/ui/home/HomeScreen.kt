package com.ghostpin.app.ui.home

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.EditLocation
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ghostpin.app.R
import com.ghostpin.app.domain.model.MockState
import com.ghostpin.app.ui.theme.MonoBackground
import com.ghostpin.app.ui.theme.MonoBlack
import com.ghostpin.app.ui.theme.MonoBorder
import com.ghostpin.app.ui.theme.MonoBorderSubtle
import com.ghostpin.app.ui.theme.MonoEmergency
import com.ghostpin.app.ui.theme.MonoSurface
import com.ghostpin.app.ui.theme.MonoSurfaceVariant
import com.ghostpin.app.ui.theme.MonoTextMuted
import com.ghostpin.app.ui.theme.MonoTextPrimary
import com.ghostpin.app.ui.theme.MonoTextSecondary
import com.ghostpin.app.ui.theme.MonoWhite
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToMap: () -> Unit,
    onNavigateToJourney: () -> Unit,
    onNavigateToSaved: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()

    val isRunning = state.mockState is MockState.Running
    val runningState = state.mockState as? MockState.Running
    val isJourneyRunning = runningState?.journeyState != null
    val journeyState = runningState?.journeyState

    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(isRunning) {
        if (isRunning) {
            while (isActive) {
                currentTime = System.currentTimeMillis()
                delay(1000)
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.checkMockAppConfiguration()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isRunning) 1.06f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val heroBorderColor by animateColorAsState(
        targetValue = if (isRunning) MonoWhite else MonoBorder,
        label = "heroBorder"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MonoBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // App Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_ghost_logo),
                        contentDescription = "GhostPin Logo",
                        modifier = Modifier.size(28.dp),
                        tint = MonoWhite
                    )
                    Text(
                        text = "GhostPin",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MonoTextPrimary,
                        letterSpacing = (-0.5).sp
                    )
                }
                Text(
                    text = "Location Privacy Switchboard",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MonoTextSecondary
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MonoSurfaceVariant,
                border = BorderStroke(1.dp, MonoBorderSubtle)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Privacy Shield",
                        modifier = Modifier.size(14.dp),
                        tint = MonoTextPrimary
                    )
                    Text(
                        text = "100% On-Device",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MonoTextSecondary
                    )
                }
            }
        }

        // Developer Options Setup Prompt (if needed)
        AnimatedVisibility(visible = !state.isMockAppConfigured) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MonoSurface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MonoBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Setup Required",
                            tint = MonoTextPrimary
                        )
                        Text(
                            text = "Developer Options Setup Required",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MonoTextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "1. Open Developer Options on your phone.\n2. Tap 'Select mock location app'.\n3. Choose GhostPin.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MonoTextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                context.startActivity(Intent(Settings.ACTION_SETTINGS))
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MonoWhite, contentColor = MonoBlack),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Open Developer Options", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Error message banner
        AnimatedVisibility(visible = state.errorMessage != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MonoSurface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MonoEmergency),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = MonoEmergency)
                    Text(
                        text = state.errorMessage ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MonoTextPrimary
                    )
                }
            }
        }

        // Live Journey Active Monitor Card
        if (isRunning && isJourneyRunning && journeyState != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MonoSurface),
                border = BorderStroke(1.5.dp, MonoWhite)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = MonoWhite)
                            Text(
                                text = "ACTIVE JOURNEY",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MonoWhite,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MonoSurfaceVariant
                        ) {
                            Text(
                                text = "Stop ${journeyState.currentStopIndex} of ${journeyState.totalStops}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MonoTextPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = journeyState.journeyName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MonoTextPrimary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    val statusDesc = if (journeyState.isStaying) {
                        val m = journeyState.phaseRemainingSeconds / 60
                        val s = journeyState.phaseRemainingSeconds % 60
                        "Staying at ${journeyState.currentStopName} • %02d:%02d remaining".format(m, s)
                    } else {
                        val m = journeyState.phaseRemainingSeconds / 60
                        val s = journeyState.phaseRemainingSeconds % 60
                        "Moving to ${journeyState.nextStopName} (%.0f km/h) • %02d:%02d left".format(journeyState.speedKmh, m, s)
                    }

                    Text(
                        text = statusDesc,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MonoTextSecondary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (!journeyState.isStaying) {
                        LinearProgressIndicator(
                            progress = { journeyState.legProgressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = MonoWhite,
                            trackColor = MonoSurfaceVariant,
                            strokeCap = StrokeCap.Round
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { viewModel.stopSimulation(context) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MonoSurfaceVariant, contentColor = MonoEmergency),
                        border = BorderStroke(1.dp, MonoEmergency.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(18.dp), tint = MonoEmergency)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("STOP JOURNEY", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Main Status Hero Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MonoSurface),
            border = BorderStroke(1.5.dp, heroBorderColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Animated Status Badge with Ghost Icon
                Box(
                    modifier = Modifier
                        .scale(if (isRunning) pulseScale else 1f)
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(if (isRunning) MonoWhite else MonoSurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_ghost_logo),
                        contentDescription = "GhostPin Status",
                        tint = if (isRunning) MonoBlack else MonoTextPrimary,
                        modifier = Modifier.size(42.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (isRunning) "MOCK LOCATION ACTIVE" else "MOCK LOCATION OFF",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp,
                    color = if (isRunning) MonoWhite else MonoTextSecondary
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Active Runtime / Countdown Ticker
                if (isRunning && runningState != null && !isJourneyRunning) {
                    val statusTicker = if (runningState.expiresAt != null) {
                        val remainingMs = (runningState.expiresAt - currentTime).coerceAtLeast(0)
                        val m = remainingMs / 1000 / 60
                        val s = (remainingMs / 1000) % 60
                        String.format("Auto-stops in %02d:%02d", m, s)
                    } else {
                        val elapsedMs = (currentTime - runningState.startedAt).coerceAtLeast(0)
                        val m = elapsedMs / 1000 / 60
                        val s = (elapsedMs / 1000) % 60
                        String.format("Running for %02d:%02d", m, s)
                    }

                    Surface(
                        color = MonoSurfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, MonoBorderSubtle)
                    ) {
                        Text(
                            text = statusTicker,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MonoTextPrimary,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                } else if (!isRunning) {
                    Text(
                        text = "Android is reporting genuine GPS coordinates",
                        style = MaterialTheme.typography.bodySmall,
                        color = MonoTextMuted
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Target Location Details
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onNavigateToMap() },
                    color = MonoSurfaceVariant,
                    border = BorderStroke(1.dp, MonoBorderSubtle)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MonoSurface),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = MonoTextPrimary
                                )
                            }

                            Column {
                                Text(
                                    text = if (isJourneyRunning && runningState != null) runningState.location.name else state.selectedLocation.name.ifBlank { "Custom Coordinates" },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MonoTextPrimary
                                )
                                val currentLat = if (isJourneyRunning && runningState != null) runningState.location.latitude else state.selectedLocation.latitude
                                val currentLng = if (isJourneyRunning && runningState != null) runningState.location.longitude else state.selectedLocation.longitude
                                Text(
                                    text = String.format("%.4f, %.4f", currentLat, currentLng),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontFamily = FontFamily.Monospace,
                                    color = MonoTextSecondary
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.EditLocation,
                            contentDescription = "Edit Location",
                            tint = MonoTextPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Primary Start / Emergency Stop Toggle Button
                if (isRunning) {
                    Button(
                        onClick = { viewModel.stopSimulation(context) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MonoSurfaceVariant,
                            contentColor = MonoEmergency
                        ),
                        border = BorderStroke(1.5.dp, MonoEmergency)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(24.dp), tint = MonoEmergency)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "STOP FAKE LOCATION",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        )
                    }
                } else {
                    Button(
                        onClick = { viewModel.startSimulation(context) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MonoWhite,
                            contentColor = MonoBlack
                        )
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(24.dp), tint = MonoBlack)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ENABLE FAKE LOCATION",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }

        // Multi-Stop Journey Quick Action Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .clickable { onNavigateToJourney() },
            colors = CardDefaults.cardColors(containerColor = MonoSurface),
            border = BorderStroke(1.dp, MonoBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MonoSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AltRoute, contentDescription = null, tint = MonoTextPrimary)
                    }
                    Column {
                        Text(
                            text = "Plan Multi-Stop Journey",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MonoTextPrimary
                        )
                        Text(
                            text = "Connect multiple points with stay & travel times",
                            style = MaterialTheme.typography.bodySmall,
                            color = MonoTextSecondary
                        )
                    }
                }

                Icon(Icons.Default.Navigation, contentDescription = null, tint = MonoTextPrimary)
            }
        }

        // Duration / Session Timer Selection
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MonoSurface),
            border = BorderStroke(1.dp, MonoBorder)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = MonoTextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Session Duration",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MonoTextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val durations = listOf(
                        null to "Until Stopped",
                        30L to "30 min",
                        60L to "1 hour",
                        120L to "2 hours"
                    )

                    durations.forEach { (minutes, label) ->
                        val isSelected = state.selectedDurationMinutes == minutes
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setDuration(minutes) },
                            label = { Text(label) },
                            leadingIcon = if (isSelected) {
                                {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MonoWhite,
                                selectedLabelColor = MonoBlack,
                                containerColor = MonoSurfaceVariant,
                                labelColor = MonoTextSecondary
                            )
                        )
                    }
                }
            }
        }

        // Quick Presets Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MonoSurface),
            border = BorderStroke(1.dp, MonoBorder)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Bookmark, contentDescription = null, tint = MonoTextPrimary, modifier = Modifier.size(18.dp))
                        Text(
                            text = "Quick Presets",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MonoTextPrimary
                        )
                    }
                    Text(
                        text = "Manage",
                        style = MaterialTheme.typography.labelLarge,
                        color = MonoTextPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onNavigateToSaved() }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (state.quickPresets.isEmpty()) {
                    Text(
                        text = "No saved locations yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MonoTextSecondary
                    )
                } else {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        state.quickPresets.forEach { preset ->
                            val isCurrent = state.selectedLocation.latitude == preset.latitude &&
                                    state.selectedLocation.longitude == preset.longitude
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isCurrent) MonoWhite else MonoSurfaceVariant,
                                border = BorderStroke(
                                    1.dp,
                                    if (isCurrent) MonoWhite else MonoBorderSubtle
                                ),
                                modifier = Modifier.clickable {
                                    viewModel.quickActivatePreset(context, preset)
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = preset.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isCurrent) MonoBlack else MonoTextPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

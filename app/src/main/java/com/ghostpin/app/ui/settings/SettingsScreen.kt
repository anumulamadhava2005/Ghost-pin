package com.ghostpin.app.ui.settings

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ghostpin.app.ui.theme.MonoBackground
import com.ghostpin.app.ui.theme.MonoBlack
import com.ghostpin.app.ui.theme.MonoBorder
import com.ghostpin.app.ui.theme.MonoBorderSubtle
import com.ghostpin.app.ui.theme.MonoSurface
import com.ghostpin.app.ui.theme.MonoSurfaceVariant
import com.ghostpin.app.ui.theme.MonoTextPrimary
import com.ghostpin.app.ui.theme.MonoTextSecondary
import com.ghostpin.app.ui.theme.MonoWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MonoBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Settings, contentDescription = null, tint = MonoTextPrimary, modifier = Modifier.size(24.dp))
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MonoTextPrimary
                )
            }
            Text(
                text = "Engine parameters & system permissions",
                style = MaterialTheme.typography.bodyMedium,
                color = MonoTextSecondary
            )
        }

        // Section 1: Developer Options / Mock Location App Status
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MonoSurface),
            border = BorderStroke(1.dp, MonoBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MonoSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = MonoTextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Developer Options",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MonoTextPrimary
                        )
                        Text(
                            text = if (state.isMockAppConfigured) "Mock provider active" else "Setup required",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (state.isMockAppConfigured) MonoWhite else MonoTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "GhostPin must be selected under Developer Options > 'Select mock location app' to simulate GPS coordinates.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MonoTextSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            context.startActivity(Intent(Settings.ACTION_SETTINGS))
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MonoWhite, contentColor = MonoBlack)
                ) {
                    Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Open Developer Options", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Section 2: Stream Update Frequency
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MonoSurface),
            border = BorderStroke(1.dp, MonoBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MonoSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = MonoTextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "GPS Stream Interval",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MonoTextPrimary
                        )
                        Text(
                            text = "Frequency of location broadcasts",
                            style = MaterialTheme.typography.bodySmall,
                            color = MonoTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                val intervals = listOf(
                    500L to "500ms (High)",
                    1000L to "1.0s (Default)",
                    2000L to "2.0s (Battery Saver)"
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    intervals.forEach { (intervalMs, label) ->
                        val isSelected = state.updateIntervalMs == intervalMs
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MonoWhite else MonoSurfaceVariant,
                            border = BorderStroke(1.dp, if (isSelected) MonoWhite else MonoBorderSubtle),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.setUpdateInterval(intervalMs) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MonoBlack else MonoTextPrimary
                                )
                                if (isSelected) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MonoBlack,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 3: Privacy & Architecture Notice
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MonoSurface),
            border = BorderStroke(1.dp, MonoBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MonoSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = MonoTextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Privacy Architecture",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MonoTextPrimary
                        )
                        Text(
                            text = "Zero tracking • On-device",
                            style = MaterialTheme.typography.bodySmall,
                            color = MonoTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "• GhostPin does NOT log or transmit location history.\n• Coordinates are broadcast exclusively via Android's test provider API.\n• Stopping simulation immediately restores genuine hardware GPS coordinates.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MonoTextSecondary,
                    lineHeight = 22.sp
                )
            }
        }

        // App Version
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "GhostPin v1.0.0 (Pure Native Android)",
                style = MaterialTheme.typography.labelSmall,
                color = MonoTextSecondary
            )
        }
    }
}

package com.ghostpin.app.ui.map

import android.content.Context
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.view.MotionEvent
import android.widget.Toast
import java.util.Locale
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.ghostpin.app.ui.theme.MonoBackground
import com.ghostpin.app.ui.theme.MonoBlack
import com.ghostpin.app.ui.theme.MonoBorder
import com.ghostpin.app.ui.theme.MonoSurface
import com.ghostpin.app.ui.theme.MonoSurfaceVariant
import com.ghostpin.app.ui.theme.MonoTextPrimary
import com.ghostpin.app.ui.theme.MonoTextSecondary
import com.ghostpin.app.ui.theme.MonoWhite
import org.osmdroid.events.DelayedMapListener
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapPickerScreen(
    viewModel: MapPickerViewModel,
    onNavigateBackToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val state by viewModel.uiState.collectAsState()

    var mapViewRef by remember { mutableStateOf<MapView?>(null) }

    // Lifecycle observer to pause/resume native MapView
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapViewRef?.onResume()
                Lifecycle.Event.ON_PAUSE -> mapViewRef?.onPause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapViewRef?.onDetach()
        }
    }

    // Uber-style Pin Elevation Animation
    val pinElevationOffsetY by animateDpAsState(
        targetValue = if (state.isMapDragging) (-24).dp else 0.dp,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "pinElevation"
    )
    val shadowScale by animateFloatAsState(
        targetValue = if (state.isMapDragging) 0.5f else 1.0f,
        animationSpec = tween(150),
        label = "shadowScale"
    )
    val shadowAlpha by animateFloatAsState(
        targetValue = if (state.isMapDragging) 0.3f else 0.8f,
        animationSpec = tween(150),
        label = "shadowAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MonoBackground)
    ) {
        // 1. Native OpenStreetMap Hardware-Accelerated Renderer (osmdroid)
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    isTilesScaledToDpi = true
                    zoomController.setVisibility(org.osmdroid.views.CustomZoomButtonsController.Visibility.NEVER)
                    controller.setZoom(15.0)
                    controller.setCenter(GeoPoint(state.selectedLatitude, state.selectedLongitude))

                    // Sleek Dark Monochrome Filter on Native Map Tiles
                    val colorMatrix = ColorMatrix().apply {
                        set(
                            floatArrayOf(
                                -0.7f, 0f, 0f, 0f, 220f,
                                0f, -0.7f, 0f, 0f, 220f,
                                0f, 0f, -0.7f, 0f, 220f,
                                0f, 0f, 0f, 1f, 0f
                            )
                        )
                    }
                    overlayManager.tilesOverlay.setColorFilter(ColorMatrixColorFilter(colorMatrix))

                    // Touch events for pin lifting and dropping
                    setOnTouchListener { _, event ->
                        when (event.action) {
                            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                                viewModel.onMapMoveStarted()
                            }
                            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                val center = mapCenter as? GeoPoint
                                if (center != null) {
                                    viewModel.onMapMoveEnded(context, center.latitude, center.longitude)
                                }
                            }
                        }
                        false
                    }

                    // Map scroll and movement listener
                    addMapListener(DelayedMapListener(object : MapListener {
                        override fun onScroll(event: ScrollEvent?): Boolean {
                            val center = mapCenter as? GeoPoint
                            if (center != null) {
                                viewModel.onMapMoving(center.latitude, center.longitude)
                                viewModel.onMapMoveEnded(context, center.latitude, center.longitude)
                            }
                            return true
                        }

                        override fun onZoom(event: ZoomEvent?): Boolean {
                            val center = mapCenter as? GeoPoint
                            if (center != null) {
                                viewModel.onMapMoving(center.latitude, center.longitude)
                            }
                            return true
                        }
                    }, 250))

                    mapViewRef = this
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // 2. Uber-Style Center Floating Pin & Ground Shadow
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-24).dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            // Ground Shadow beneath pin
            Box(
                modifier = Modifier
                    .offset(y = 24.dp)
                    .scale(shadowScale)
                    .alpha(shadowAlpha)
                    .size(16.dp, 6.dp)
                    .background(Color.Black.copy(alpha = 0.7f), CircleShape)
            )

            // Floating Animated Pin
            Column(
                modifier = Modifier
                    .offset(y = pinElevationOffsetY),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Pin Head Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MonoWhite,
                    border = BorderStroke(1.5.dp, MonoBorder),
                    shadowElevation = 10.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PinDrop,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MonoBlack
                        )
                        Text(
                            text = "PIN LOCATION",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MonoBlack,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                // Pin Needle
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(14.dp)
                        .background(MonoWhite)
                )

                // Pin Needle Point
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .background(MonoWhite, CircleShape)
                )
            }
        }

        // 3. Top Floating Search Bar & Results
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MonoSurface),
                border = BorderStroke(1.dp, MonoBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MonoTextPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = { viewModel.setSearchQuery(context, it) },
                        placeholder = {
                            Text(
                                "Search landmark, street, city...",
                                color = MonoTextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = MonoTextPrimary,
                            unfocusedTextColor = MonoTextPrimary
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                        modifier = Modifier.weight(1f)
                    )

                    if (state.isSearching) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MonoTextPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    } else if (state.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearSearch() }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = MonoTextPrimary)
                        }
                    }
                }
            }

            // Search Autocomplete Results List
            AnimatedVisibility(
                visible = state.searchResults.isNotEmpty(),
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MonoSurface),
                    border = BorderStroke(1.dp, MonoBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .heightIn(max = 240.dp)
                ) {
                    LazyColumn {
                        items(state.searchResults) { result ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.selectSearchResult(result)
                                        focusManager.clearFocus()
                                        mapViewRef?.controller?.animateTo(
                                            GeoPoint(result.latitude, result.longitude),
                                            16.0,
                                            1000L
                                        )
                                    }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = MonoTextPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = result.displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MonoTextPrimary,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Map Control Buttons
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Locate Real GPS
            FloatingActionButton(
                onClick = {
                    val loc = viewModel.getCurrentDeviceLocation(context)
                    if (loc != null) {
                        mapViewRef?.controller?.animateTo(
                            GeoPoint(loc.latitude, loc.longitude),
                            16.0,
                            1000L
                        )
                        viewModel.updateCoordinatesManual(loc.latitude, loc.longitude, "Device GPS")
                    } else {
                        Toast.makeText(context, "Genuine GPS not available", Toast.LENGTH_SHORT).show()
                    }
                },
                containerColor = MonoSurface,
                contentColor = MonoTextPrimary,
                shape = CircleShape,
                modifier = Modifier
                    .size(46.dp)
                    .border(1.dp, MonoBorder, CircleShape),
                elevation = FloatingActionButtonDefaults.elevation(4.dp)
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "Current Location", modifier = Modifier.size(22.dp))
            }

            // Zoom In
            FloatingActionButton(
                onClick = { mapViewRef?.controller?.zoomIn() },
                containerColor = MonoSurface,
                contentColor = MonoTextPrimary,
                shape = CircleShape,
                modifier = Modifier
                    .size(46.dp)
                    .border(1.dp, MonoBorder, CircleShape),
                elevation = FloatingActionButtonDefaults.elevation(4.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Zoom In", modifier = Modifier.size(22.dp))
            }

            // Zoom Out
            FloatingActionButton(
                onClick = { mapViewRef?.controller?.zoomOut() },
                containerColor = MonoSurface,
                contentColor = MonoTextPrimary,
                shape = CircleShape,
                modifier = Modifier
                    .size(46.dp)
                    .border(1.dp, MonoBorder, CircleShape),
                elevation = FloatingActionButtonDefaults.elevation(4.dp)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Zoom Out", modifier = Modifier.size(22.dp))
            }

            // Manual Edit Dialog
            FloatingActionButton(
                onClick = { viewModel.setManualDialogVisible(true) },
                containerColor = MonoSurface,
                contentColor = MonoTextPrimary,
                shape = CircleShape,
                modifier = Modifier
                    .size(46.dp)
                    .border(1.dp, MonoBorder, CircleShape),
                elevation = FloatingActionButtonDefaults.elevation(4.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Edit Coordinates", modifier = Modifier.size(20.dp))
            }
        }

        // 5. Uber-Style Bottom Location Selection Sheet
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MonoSurface),
            border = BorderStroke(1.dp, MonoBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MonoSurfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MonoTextPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = state.locationName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MonoTextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (state.isGeocoding) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(12.dp),
                                        strokeWidth = 1.5.dp,
                                        color = MonoTextPrimary
                                    )
                                }
                            }
                            Text(
                                text = String.format(
                                    Locale.US,
                                    "%.5f° %s, %.5f° %s",
                                    kotlin.math.abs(state.selectedLatitude),
                                    if (state.selectedLatitude >= 0) "N" else "S",
                                    kotlin.math.abs(state.selectedLongitude),
                                    if (state.selectedLongitude >= 0) "E" else "W"
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = FontFamily.Monospace,
                                color = MonoTextSecondary
                            )
                        }
                    }

                    // Save as Preset Button
                    IconButton(
                        onClick = {
                            viewModel.saveToPresets {
                                Toast.makeText(context, "Saved to Presets", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.BookmarkAdd,
                            contentDescription = "Save Preset",
                            tint = MonoTextPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Primary "Use Location" Action Button
                Button(
                    onClick = {
                        if (viewModel.applyLocation()) {
                            Toast.makeText(context, "Location Set", Toast.LENGTH_SHORT).show()
                            onNavigateBackToHome()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MonoWhite,
                        contentColor = MonoBlack
                    )
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "USE THIS LOCATION",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        // 6. Manual Coordinate Input Dialog
        if (state.isManualCoordDialogVisible) {
            ManualCoordinateDialog(
                currentLat = state.selectedLatitude,
                currentLng = state.selectedLongitude,
                currentName = state.locationName,
                onDismiss = { viewModel.setManualDialogVisible(false) },
                onApply = { lat, lng, name ->
                    viewModel.updateCoordinatesManual(lat, lng, name)
                    viewModel.setManualDialogVisible(false)
                    mapViewRef?.controller?.animateTo(
                        GeoPoint(lat, lng),
                        16.0,
                        1000L
                    )
                }
            )
        }
    }
}

@Composable
fun ManualCoordinateDialog(
    currentLat: Double,
    currentLng: Double,
    currentName: String,
    onDismiss: () -> Unit,
    onApply: (lat: Double, lng: Double, name: String) -> Unit
) {
    var latText by remember { mutableStateOf(currentLat.toString()) }
    var lngText by remember { mutableStateOf(currentLng.toString()) }
    var nameText by remember { mutableStateOf(currentName) }

    val lat = latText.toDoubleOrNull()
    val lng = lngText.toDoubleOrNull()
    val isValid = nameText.isNotBlank() && lat != null && lat in -90.0..90.0 && lng != null && lng in -180.0..180.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Fine-Tune Coordinates", fontWeight = FontWeight.Bold, color = MonoTextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = nameText,
                    onValueChange = { nameText = it },
                    label = { Text("Location Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = latText,
                    onValueChange = { latText = it },
                    label = { Text("Latitude (-90 to 90)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = lngText,
                    onValueChange = { lngText = it },
                    label = { Text("Longitude (-180 to 180)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isValid && lat != null && lng != null) {
                        onApply(lat, lng, nameText)
                    }
                },
                enabled = isValid,
                colors = ButtonDefaults.buttonColors(containerColor = MonoWhite, contentColor = MonoBlack)
            ) {
                Text("Apply", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

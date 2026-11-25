package com.example.syntax_camping.screens

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.syntax_camping.Campsite
import com.example.syntax_camping.R
import com.example.syntax_camping.screens.userScreens.userCampsiteList.CampsiteCard
import com.mapbox.geojson.Point
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.CircleAnnotation
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import com.mapbox.maps.extension.compose.annotation.generated.PolygonAnnotation
import com.mapbox.maps.extension.compose.annotation.rememberIconImage
import kotlin.math.cos
import kotlin.math.ln

/*
* The search map: a red pin for every campsite that has a map position.
* Tap a pin and that campsite's card shows under the map. Tap another pin and the card swaps.
* Tap the card to open the campsite.
* If a "near me" search is on (userLat/userLng/radiusKm), the map is centred on the user with a blue dot and the search radius,
* zoomed so the whole radius circle fits on screen.
* */
@OptIn(MapboxExperimental::class)
@Composable
fun CampsiteSearchMap(
    campsites: List<Campsite>,
    onOpenCampsite: (String) -> Unit,
    userLat: Double? = null,
    userLng: Double? = null,
    radiusKm: Int? = null
) {
    // only campsites whose address was found on the map can get a pin
    val mapped = campsites.filter { it.latitude != null && it.longitude != null }
    val hasUser = userLat != null && userLng != null && radiusKm != null

    var selectedId by remember { mutableStateOf<String?>(null) }
    val pin = rememberIconImage(key = R.drawable.ic_red_pin, painter = painterResource(R.drawable.ic_red_pin))

    // where the camera starts: on the user for a near-me search, otherwise the middle of all the pins (Edmonton if none)
    val centerLat = when {
        hasUser -> userLat!!
        mapped.isEmpty() -> 53.5461
        else -> mapped.map { it.latitude!! }.average()
    }
    val centerLng = when {
        hasUser -> userLng!!
        mapped.isEmpty() -> -113.4938
        else -> mapped.map { it.longitude!! }.average()
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        // Zoom so the whole radius circle fits on screen (with a little margin).
        // At zoom 0 one dp covers about 78271 m at the equator, and every zoom level halves that.
        val startZoom = if (hasUser) {
            val fitDp = minOf(maxWidth.value, maxHeight.value * 0.6f).toDouble()
            val metersPerDpAtZoom0 = 78271.517 * cos(Math.toRadians(centerLat))
            val neededMeters = 2.0 * radiusKm!! * 1000.0 * 1.15
            (ln(metersPerDpAtZoom0 * fitDp / neededMeters) / ln(2.0)).coerceIn(3.0, 16.0)
        } else if (mapped.size <= 1) {
            11.0
        } else {
            8.0
        }

        val viewport = rememberMapViewportState {
            setCameraOptions {
                zoom(startZoom)
                center(Point.fromLngLat(centerLng, centerLat))   // Mapbox wants longitude first
            }
        }

        // when the search, filter or radius changes, move the camera to fit
        val idsKey = mapped.joinToString(",") { it.documentId } + "|$userLat|$userLng|$radiusKm"
        LaunchedEffect(idsKey) {
            viewport.setCameraOptions {
                zoom(startZoom)
                center(Point.fromLngLat(centerLng, centerLat))
            }
        }

        val selected = mapped.find { it.documentId == selectedId }

        Column(modifier = Modifier.fillMaxSize()) {
            if (mapped.isEmpty() && !hasUser) {
                Text(
                    text = "No campsites with a map location yet.",
                    modifier = Modifier
                        .padding(16.dp)
                        .testTag("noMappedCampsitesText")
                )
            } else {
                MapboxMap(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .testTag("campsiteSearchMap"),
                    mapViewportState = viewport
                ) {
                    // the search radius and the user's position
                    if (hasUser) {
                        val ring = circleOutline(userLat!!, userLng!!, radiusKm!!.toDouble())
                            .map { Point.fromLngLat(it.second, it.first) }
                        PolygonAnnotation(points = listOf(ring)) {
                            fillColor = Color(0xFF1976D2)
                            fillOpacity = 0.15
                        }
                        CircleAnnotation(point = Point.fromLngLat(userLng, userLat)) {
                            circleRadius = 8.0
                            circleColor = Color(0xFF1976D2)
                            circleStrokeWidth = 2.0
                            circleStrokeColor = Color.White
                        }
                    }

                    mapped.forEach { camp ->
                        key(camp.documentId) {
                            PointAnnotation(point = Point.fromLngLat(camp.longitude!!, camp.latitude!!)) {
                                iconImage = pin
                                interactionsState.onClicked {
                                    selectedId = camp.documentId
                                    true
                                }
                            }
                        }
                    }
                }

                if (selected != null) {
                    CampsiteCard(selected) {
                        onOpenCampsite(selected.documentId)
                    }
                } else {
                    Text(
                        text = if (mapped.isEmpty()) "No campsites within this distance." else "Tap a pin to see the campsite.",
                        color = Color.Gray,
                        modifier = Modifier
                            .padding(16.dp)
                            .testTag("tapAPinHint")
                    )
                }
            }
        }
    }
}
package com.pocketremodel.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.ar.core.Anchor
import com.google.ar.core.Plane
import com.pocketremodel.app.ArViewModel
import com.pocketremodel.app.data.ModelCatalog
import com.pocketremodel.app.domain.RemodelCommand
import com.pocketremodel.app.ui.theme.BrandTeal
import com.pocketremodel.app.ui.theme.Glass
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.ar.arcore.createAnchorOrNull
import io.github.sceneview.ar.arcore.getUpdatedPlanes
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelInstance
import io.github.sceneview.rememberModelLoader
import java.util.UUID

/** One placed piece of furniture: which catalog item, pinned to which real-world anchor. */
private data class Placement(val id: String, val catalogId: String, val anchor: Anchor)

/**
 * The whole experience: a live AR camera feed with a glassy overlay. Furniture is
 * declared *declaratively* inside ARSceneView (SceneView 4.x style) — we keep a
 * Compose state list of placements and the scene re-renders itself automatically.
 */
@Composable
fun ArRemodelScreen(vm: ArViewModel) {
    val ui by vm.state.collectAsState()

    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)

    // What's currently in the room, and what's waiting for a floor to land on.
    val placements = remember { mutableStateListOf<Placement>() }
    val pending = remember { mutableStateListOf<String>() }

    var showSheet by remember { mutableStateOf(false) }

    // Translate AI commands into scene state.
    LaunchedEffect(Unit) {
        vm.commands.collect { command ->
            when (command) {
                is RemodelCommand.AddModel -> pending.add(command.catalogId)
                is RemodelCommand.RemoveModel -> placements.removeLastOrNull()
                is RemodelCommand.ClearAll -> { placements.clear(); pending.clear() }
                // v1: recolor / move / hide-real are acknowledged by voice; visual
                // upgrades plug in here later without touching the rest of the app.
                else -> {}
            }
        }
    }

    Box(Modifier.fillMaxSize()) {

        // ---------- Live AR world ----------
        ARSceneView(
            modifier = Modifier.fillMaxSize(),
            engine = engine,
            modelLoader = modelLoader,
            planeRenderer = true,
            onSessionUpdated = { _, frame ->
                // When something is waiting and we find a floor, drop it onto an anchor.
                if (pending.isNotEmpty()) {
                    val anchor: Anchor? = frame.getUpdatedPlanes()
                        .firstOrNull { it.type == Plane.Type.HORIZONTAL_UPWARD_FACING }
                        ?.let { plane -> plane.createAnchorOrNull(plane.centerPose) }
                    if (anchor != null) {
                        val catalogId = pending.removeAt(0)
                        placements.add(Placement(UUID.randomUUID().toString().take(6), catalogId, anchor))
                    }
                }
            }
        ) {
            // Declare a node for every placed item. Drag/pinch enabled via isEditable.
            placements.forEach { p ->
                key(p.id) {
                    val item = ModelCatalog.find(p.catalogId)
                    if (item != null) {
                        val instance = rememberModelInstance(modelLoader, item.assetFile)
                        if (instance != null) {
                            AnchorNode(anchor = p.anchor) {
                                ModelNode(
                                    modelInstance = instance,
                                    scaleToUnits = item.approxWidthMeters,
                                    isEditable = true
                                )
                            }
                        }
                    }
                }
            }
        }

        // ---------- Top hint chip ----------
        Surface(
            color = Glass,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 48.dp, start = 24.dp, end = 24.dp)
        ) {
            Text(
                text = statusLine(ui),
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
            )
        }

        // ---------- Assistant reply caption ----------
        if (ui.reply.isNotBlank()) {
            Surface(
                color = Glass,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 170.dp, start = 24.dp, end = 24.dp)
            ) {
                Text(
                    "“${ui.reply}”",
                    color = BrandTeal,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        // ---------- Bottom controls ----------
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 56.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlassCircle(size = 56, icon = Icons.Filled.Chair) { showSheet = true }
            Box(Modifier.size(24.dp))
            MicButton(
                listening = ui.status == ArViewModel.Status.LISTENING,
                onPress = { vm.startListening() },
                onRelease = { vm.stopListening() }
            )
            Box(Modifier.size(24.dp))
            GlassCircle(size = 56, icon = Icons.Filled.Save) { vm.saveDesignRequested() }
        }

        // ---------- Furniture sheet ----------
        if (showSheet) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color(0x99000000))
                    .pointerInput(Unit) { detectTapGestures(onTap = { showSheet = false }) }
            )
            Box(Modifier.align(Alignment.BottomCenter)) {
                FurnitureSheet(
                    onPick = { id -> vm.placeFromCatalog(id) },
                    onPhoto = { dataUrl -> vm.matchPhotoAndPlace(dataUrl) },
                    onClose = { showSheet = false }
                )
            }
        }
    }
}

private fun statusLine(ui: ArViewModel.UiState): String = when (ui.status) {
    ArViewModel.Status.LISTENING -> "Listening…"
    ArViewModel.Status.THINKING -> "Designing…"
    ArViewModel.Status.SPEAKING -> ui.reply.ifBlank { "Done!" }
    ArViewModel.Status.IDLE ->
        if (ui.transcript.isNotBlank()) "You said: “${ui.transcript}”" else ui.hint
}

@Composable
private fun MicButton(listening: Boolean, onPress: () -> Unit, onRelease: () -> Unit) {
    val ring = if (listening) BrandTeal else Color.White
    Box(
        modifier = Modifier
            .size(84.dp)
            .background(if (listening) BrandTeal.copy(alpha = 0.25f) else Glass, CircleShape)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        onPress()
                        tryAwaitRelease()
                        onRelease()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Filled.Mic, contentDescription = "Hold to talk", tint = ring,
            modifier = Modifier.size(34.dp))
    }
}

@Composable
private fun GlassCircle(size: Int, icon: ImageVector, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .background(Glass, CircleShape)
            .pointerInput(Unit) { detectTapGestures(onTap = { onClick() }) },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
    }
}

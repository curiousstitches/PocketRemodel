package com.pocketremodel.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.ar.core.Config
import com.pocketremodel.app.ArViewModel
import com.pocketremodel.app.ar.SceneController
import com.pocketremodel.app.ui.theme.BrandTeal
import com.pocketremodel.app.ui.theme.Glass
import io.github.sceneview.ar.ARScene
import io.github.sceneview.ar.rememberARCameraNode
import io.github.sceneview.rememberCollisionSystem
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNodes
import io.github.sceneview.rememberView
import kotlinx.coroutines.flow.collectLatest

/**
 * The whole experience on one screen: a live AR camera feed with a glassy overlay.
 * Hold the mic, talk, and watch the room change.
 */
@Composable
fun ArRemodelScreen(vm: ArViewModel) {
    val ui by vm.state.collectAsState()
    val scope = rememberCoroutineScope()

    // ---- SceneView (Filament + ARCore) plumbing ----
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val view = rememberView(engine)
    val collisionSystem = rememberCollisionSystem(view)
    val cameraNode = rememberARCameraNode(engine)
    val childNodes = rememberNodes()

    val controller = remember {
        SceneController(engine = engine, modelLoader = modelLoader, childNodes = childNodes)
    }

    var showSheet by remember { mutableStateOf(false) }

    // Pipe AI commands from the ViewModel into the live scene.
    androidx.compose.runtime.LaunchedEffect(Unit) {
        vm.commands.collectLatest { command -> controller.execute(command, scope) }
    }

    Box(Modifier.fillMaxSize()) {

        // ---------- Live AR world ----------
        ARScene(
            modifier = Modifier.fillMaxSize(),
            childNodes = childNodes,
            engine = engine,
            view = view,
            modelLoader = modelLoader,
            collisionSystem = collisionSystem,
            cameraNode = cameraNode,
            planeRenderer = true,
            sessionConfiguration = { _, config ->
                config.depthMode = Config.DepthMode.AUTOMATIC          // occlusion + look-under
                config.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
                config.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
                config.cloudAnchorMode = Config.CloudAnchorMode.ENABLED // Time Machine
            },
            onSessionUpdated = { session, frame ->
                controller.onSessionUpdated(session, frame)
            }
        )

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
            // Furniture drawer
            GlassCircle(size = 56, icon = Icons.Filled.Chair) { showSheet = true }
            Box(Modifier.size(24.dp))

            // Hold-to-talk mic (the hero button)
            MicButton(
                listening = ui.status == ArViewModel.Status.LISTENING,
                onPress = { vm.startListening() },
                onRelease = { vm.stopListening() }
            )
            Box(Modifier.size(24.dp))

            // Save
            GlassCircle(size = 56, icon = Icons.Filled.Save) { vm.saveDesignRequested() }
        }

        // ---------- Furniture sheet (slides up from bottom) ----------
        if (showSheet) {
            // Dim scrim behind the sheet; tap to dismiss.
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
        Icon(icon, contentDescription = null, tint = Color.White,
            modifier = Modifier.size(24.dp))
    }
}

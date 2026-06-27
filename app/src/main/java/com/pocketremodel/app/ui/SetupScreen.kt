package com.pocketremodel.app.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pocketremodel.app.SetupViewModel
import com.pocketremodel.app.ui.theme.BrandTeal

/**
 * One-time setup. Opens OpenRouter to grab a free key (the app stays alive in the
 * background), lets the user paste it, tests it, and shows a green check or red X.
 */
@Composable
fun SetupScreen(vm: SetupViewModel, onReady: () -> Unit) {
    val ui by vm.state.collectAsState()
    val context = LocalContext.current
    val clipboard: ClipboardManager = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Let's get you set up", fontSize = 26.sp, fontWeight = FontWeight.Bold,
            color = Color.White)
        Spacer(Modifier.height(8.dp))
        Text(
            "Pocket Remodel needs one free key to power its AI designer. Takes about a minute — you only do this once.",
            color = Color(0xFFB9C2CC), fontSize = 15.sp, textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(28.dp))

        // Step 1 — open the site (app keeps running in the background)
        Button(
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(vm.keyPageUrl))
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandTeal, contentColor = Color.Black)
        ) {
            Icon(Icons.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.size(8.dp))
            Text("1.  Get my free key", fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(8.dp))
        Text("Sign in, tap “Create Key”, then copy it. Press back to return here — the app stays open.",
            color = Color(0xFF8A95A1), fontSize = 12.sp, textAlign = TextAlign.Center)

        Spacer(Modifier.height(24.dp))

        // Step 2 — paste the key
        OutlinedTextField(
            value = ui.keyText,
            onValueChange = vm::onKeyChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("2.  Paste your key here") },
            singleLine = true
        )
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = { clipboard.getText()?.text?.let { vm.onKeyChanged(it) } },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.ContentPaste, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.size(8.dp))
            Text("Paste from clipboard")
        }

        Spacer(Modifier.height(20.dp))

        // Step 3 — test it (green / red)
        Button(
            onClick = vm::test,
            enabled = ui.keyText.isNotBlank() && ui.testState != SetupViewModel.TestState.TESTING,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            when (ui.testState) {
                SetupViewModel.TestState.TESTING ->
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp,
                        color = Color.White)
                else -> Text("3.  Test connection", fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(Modifier.height(16.dp))

        // Result light
        when (ui.testState) {
            SetupViewModel.TestState.CONNECTED -> ResultRow(true, ui.message)
            SetupViewModel.TestState.FAILED -> ResultRow(false, ui.message)
            else -> {}
        }

        Spacer(Modifier.height(24.dp))

        // Continue
        Button(
            onClick = onReady,
            enabled = ui.canContinue,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandTeal, contentColor = Color.Black)
        ) {
            Text("Start designing  →", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ResultRow(success: Boolean, message: String) {
    val color = if (success) Color(0xFF21D07A) else Color(0xFFE5484D)
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = if (success) Icons.Filled.CheckCircle else Icons.Filled.Error,
            contentDescription = null, tint = color, modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.size(10.dp))
        Text(message, color = color, fontWeight = FontWeight.Medium)
    }
}

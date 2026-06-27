package com.pocketremodel.app.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pocketremodel.app.data.FurnitureItem
import com.pocketremodel.app.data.ModelCatalog
import com.pocketremodel.app.ui.theme.BrandTeal
import com.pocketremodel.app.util.ImageUtil

/**
 * The "dead simple" furniture drawer. Search the library, tap to drop an item, or
 * snap/pick a photo of furniture and let the AI match the closest piece we have.
 */
@Composable
fun FurnitureSheet(
    onPick: (String) -> Unit,
    onPhoto: (String) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            ImageUtil.uriToDataUrl(context, uri)?.let { onPhoto(it) }
            onClose()
        }
    }

    val results = remember(query) {
        if (query.isBlank()) ModelCatalog.items
        else ModelCatalog.items.filter {
            it.displayName.contains(query, true) || it.category.contains(query, true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xF20E1116), RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .padding(20.dp)
    ) {
        Box(
            Modifier
                .size(width = 40.dp, height = 4.dp)
                .background(Color(0xFF3A434D), CircleShape)
                .align(Alignment.CenterHorizontally)
        )

        Text(
            "Add furniture", color = Color.White, fontSize = 20.sp,
            fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 12.dp)
        )

        // Photo reference button
        Button(
            onClick = {
                photoPicker.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandTeal, contentColor = Color.Black)
        ) {
            Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null,
                modifier = Modifier.size(20.dp))
            androidx.compose.foundation.layout.Spacer(Modifier.size(8.dp))
            Text("Match from a photo", fontWeight = FontWeight.SemiBold)
        }

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            label = { Text("Search furniture") },
            singleLine = true
        )

        LazyColumn(
            modifier = Modifier.padding(top = 8.dp).height(320.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(results, key = { it.id }) { item ->
                FurnitureRow(item) { onPick(item.id); onClose() }
            }
        }
    }
}

@Composable
private fun FurnitureRow(item: FurnitureItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(Color(0x14FFFFFF), RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.Chair, contentDescription = null, tint = BrandTeal,
            modifier = Modifier.size(22.dp))
        androidx.compose.foundation.layout.Spacer(Modifier.size(14.dp))
        Column {
            Text(item.displayName, color = Color.White, fontWeight = FontWeight.Medium)
            Text(item.category, color = Color(0xFF8A95A1), fontSize = 12.sp)
        }
    }
}

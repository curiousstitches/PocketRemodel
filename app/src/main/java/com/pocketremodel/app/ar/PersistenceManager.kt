package com.pocketremodel.app.ar

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * The "Time Machine" — saves a room design so it reappears days later.
 *
 * Two halves work together:
 *  1. The DESIGN (this file): a tiny JSON list of what was placed and where,
 *     stored locally with DataStore. No giant video — just coordinates + ids.
 *  2. The ROOM MAP (CloudAnchorService): ARCore Cloud Anchors save the invisible
 *     dot-map of the room's geometry so coordinates line up to the real space.
 *
 * On reopen: resolve the Cloud Anchor (scan ~3 s) -> re-drop every saved item
 * relative to it -> the remodel snaps back exactly where it was.
 */
private val Context.designStore by preferencesDataStore(name = "pocket_remodel_designs")

@Serializable
data class SavedItem(
    val catalogId: String,
    val color: String? = null,
    // Pose stored relative to the room's anchor (metres + quaternion).
    val px: Float, val py: Float, val pz: Float,
    val qx: Float, val qy: Float, val qz: Float, val qw: Float
)

@Serializable
data class SavedDesign(
    val cloudAnchorId: String? = null,   // links to the saved room map
    val items: List<SavedItem> = emptyList(),
    val hiddenRealObjects: List<String> = emptyList(),
    val savedAtEpoch: Long = 0L
)

class PersistenceManager(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }
    private val key = stringPreferencesKey("current_room")

    suspend fun save(design: SavedDesign) {
        context.designStore.edit { it[key] = json.encodeToString(SavedDesign.serializer(), design) }
    }

    suspend fun load(): SavedDesign? {
        val raw = context.designStore.data.first()[key] ?: return null
        return runCatching { json.decodeFromString(SavedDesign.serializer(), raw) }.getOrNull()
    }

    suspend fun clear() {
        context.designStore.edit { it.remove(key) }
    }
}

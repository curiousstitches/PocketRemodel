package com.pocketremodel.app.ar

import android.util.Log
import com.google.ar.core.Anchor
import com.google.ar.core.Session

/**
 * Saves & restores the room's invisible geometry map using ARCore Cloud Anchors.
 * This is the "Aha, I recognize this room!" part of the Time Machine.
 *
 * COST: Cloud Anchors use the ARCore API, which has a free tier (generous for a
 * single user / launch testing). Enable it once in Google Cloud — see README.
 *
 * Hosting returns a cloudAnchorId we persist; resolving turns that id back into a
 * live anchor after the user re-scans the room for a few seconds.
 */
class CloudAnchorService {

    var onHosted: (String) -> Unit = {}
    var onResolved: (Anchor) -> Unit = {}
    var onError: (String) -> Unit = {}

    private var pendingHost: Anchor? = null

    /** Ask ARCore to upload the room map anchored at [anchor]. Result arrives async. */
    fun host(session: Session, anchor: Anchor) {
        runCatching {
            // 1-year persistence (max supported); requires the ARCore API enabled.
            session.hostCloudAnchorAsync(anchor, /* ttlDays = */ 365) { cloudId, state ->
                if (state.isError) {
                    onError("Hosting failed: $state")
                } else if (!cloudId.isNullOrBlank()) {
                    Log.i(TAG, "Hosted cloud anchor: $cloudId")
                    onHosted(cloudId)
                }
            }
        }.onFailure { onError(it.message ?: "Host error") }
    }

    /** Re-download a previously hosted room map by its id. */
    fun resolve(session: Session, cloudAnchorId: String) {
        runCatching {
            session.resolveCloudAnchorAsync(cloudAnchorId) { anchor, state ->
                if (state.isError || anchor == null) {
                    onError("Resolve failed: $state")
                } else {
                    Log.i(TAG, "Resolved room map.")
                    onResolved(anchor)
                }
            }
        }.onFailure { onError(it.message ?: "Resolve error") }
    }

    companion object { private const val TAG = "CloudAnchorService" }
}

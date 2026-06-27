package com.pocketremodel.app.domain

/**
 * The structured "to-do list" the AI hands back to the app after the user speaks.
 *
 * The AI never touches the 3D scene directly. It only ever returns one of these
 * commands, which the [com.pocketremodel.app.ar.SceneController] then executes.
 * This keeps the AI on a tight leash and makes the whole pipeline testable.
 */
sealed interface RemodelCommand {

    /** Drop a piece of 3D furniture into the room. */
    data class AddModel(
        val catalogId: String,          // e.g. "coffee_table_walnut"  (maps to a .glb file)
        val placementHint: Placement = Placement.IN_FRONT,
        val colorHint: String? = null   // optional tint, e.g. "navy blue"
    ) : RemodelCommand

    /** Make a previously-added virtual object disappear. */
    data class RemoveModel(val nodeId: String) : RemodelCommand

    /** "Diminished reality" — hide a REAL object by painting the floor over it. */
    data class RemoveRealObject(val description: String) : RemodelCommand

    /** Recolor / reskin an existing virtual object. */
    data class RecolorModel(val nodeId: String, val color: String) : RemodelCommand

    /** Move the most-recently-touched object. */
    data class MoveModel(val nodeId: String, val placement: Placement) : RemodelCommand

    /** Wipe the room back to empty (real + virtual). */
    data object ClearAll : RemodelCommand

    /** No scene change — the AI just wants to chat / answer a design question. */
    data class JustTalk(val message: String) : RemodelCommand
}

enum class Placement {
    IN_FRONT, LEFT, RIGHT, CENTER, CORNER, AGAINST_WALL, WHERE_I_LOOK
}

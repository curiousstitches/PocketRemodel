package com.pocketremodel.app.ai

import android.util.Log
import com.pocketremodel.app.domain.Placement
import com.pocketremodel.app.domain.RemodelCommand
import kotlinx.serialization.json.Json

/**
 * Turns the AI's raw text reply into clean, typed [RemodelCommand]s the AR engine
 * can run. Defensive on purpose: free models sometimes wrap JSON in prose or
 * code fences, so we dig the JSON object out before parsing.
 */
object CommandParser {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    data class Parsed(val spokenReply: String, val commands: List<RemodelCommand>)

    fun parse(raw: String): Parsed {
        val jsonText = extractJsonObject(raw) ?: return fallback(raw)

        val plan = runCatching { json.decodeFromString(AiPlan.serializer(), jsonText) }
            .onFailure { Log.w(TAG, "Plan parse failed: ${it.message}") }
            .getOrNull() ?: return fallback(raw)

        val commands = plan.actions.mapNotNull { it.toCommand() }
        val spoken = plan.say.ifBlank { "Done!" }
        return Parsed(spoken, commands)
    }

    private fun AiAction.toCommand(): RemodelCommand? = when (type.lowercase()) {
        "add_model" -> catalogId?.let {
            RemodelCommand.AddModel(it, placement.toPlacement(), color)
        }
        "remove_model" -> nodeId?.let { RemodelCommand.RemoveModel(it) }
        "remove_real" -> description?.let { RemodelCommand.RemoveRealObject(it) }
        "recolor_model" -> if (nodeId != null && color != null)
            RemodelCommand.RecolorModel(nodeId, color) else null
        "move_model" -> nodeId?.let { RemodelCommand.MoveModel(it, placement.toPlacement()) }
        "clear_all" -> RemodelCommand.ClearAll
        else -> null
    }

    private fun String?.toPlacement(): Placement = when (this?.lowercase()) {
        "left" -> Placement.LEFT
        "right" -> Placement.RIGHT
        "center" -> Placement.CENTER
        "corner" -> Placement.CORNER
        "against_wall" -> Placement.AGAINST_WALL
        "where_i_look" -> Placement.WHERE_I_LOOK
        else -> Placement.IN_FRONT
    }

    /** Find the outermost {...} so stray prose or ``` fences don't break parsing. */
    private fun extractJsonObject(text: String): String? {
        val start = text.indexOf('{')
        val end = text.lastIndexOf('}')
        return if (start in 0 until end) text.substring(start, end + 1) else null
    }

    private fun fallback(raw: String): Parsed {
        // No usable JSON — treat the whole thing as friendly chatter.
        val clean = raw.replace("```", "").trim().take(160).ifBlank { "I'm listening!" }
        return Parsed(clean, listOf(RemodelCommand.JustTalk(clean)))
    }

    private const val TAG = "CommandParser"
}

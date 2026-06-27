package com.pocketremodel.app.data

/**
 * The furniture library. Each entry maps a friendly id (what the AI says) to a
 * real .glb 3D file living in  app/src/main/assets/models/.
 *
 * To add new furniture: drop a NAME.glb file in that folder and add one line here.
 * That's it — the AI is told about every item automatically (see SystemPrompt).
 */
data class FurnitureItem(
    val id: String,
    val displayName: String,
    val assetFile: String,      // file inside assets/models/
    val approxWidthMeters: Float,
    val category: String
)

object ModelCatalog {

    val items: List<FurnitureItem> = listOf(
        FurnitureItem("coffee_table_walnut", "Walnut Coffee Table", "models/coffee_table_walnut.glb", 1.1f, "tables"),
        FurnitureItem("sofa_modern_grey",    "Modern Grey Sofa",    "models/sofa_modern_grey.glb",    2.0f, "seating"),
        FurnitureItem("armchair_leather",    "Leather Armchair",    "models/armchair_leather.glb",    0.8f, "seating"),
        FurnitureItem("floor_lamp_arc",      "Arc Floor Lamp",      "models/floor_lamp_arc.glb",      0.6f, "lighting"),
        FurnitureItem("rug_geometric",       "Geometric Rug",       "models/rug_geometric.glb",       1.6f, "decor"),
        FurnitureItem("bookshelf_oak",       "Oak Bookshelf",       "models/bookshelf_oak.glb",       0.9f, "storage"),
        FurnitureItem("plant_monstera",      "Monstera Plant",      "models/plant_monstera.glb",      0.5f, "decor"),
        FurnitureItem("tv_stand_minimal",    "Minimal TV Stand",    "models/tv_stand_minimal.glb",    1.4f, "storage"),
        FurnitureItem("dining_table_round",  "Round Dining Table",  "models/dining_table_round.glb",  1.2f, "tables"),
        FurnitureItem("bed_platform_queen",  "Queen Platform Bed",  "models/bed_platform_queen.glb",  1.6f, "bedroom"),
    )

    private val byId = items.associateBy { it.id }

    fun find(id: String): FurnitureItem? = byId[id]

    /** A compact menu the AI can read so it only ever names furniture we actually have. */
    fun promptManifest(): String =
        items.joinToString("\n") { "  - ${it.id}  (\"${it.displayName}\", ${it.category})" }
}

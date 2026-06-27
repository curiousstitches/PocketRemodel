DROP YOUR 3D FURNITURE FILES HERE
=================================

This folder holds the actual 3D models the app places in your room.
Each file is a ".glb" — a single, lightweight, universal 3D object file
(it already contains the shape, legs, back, and bottom, so you can crouch
and look underneath it in AR).

The app expects these exact filenames (they match app/.../data/ModelCatalog.kt):

  coffee_table_walnut.glb
  sofa_modern_grey.glb
  armchair_leather.glb
  floor_lamp_arc.glb
  rug_geometric.glb
  bookshelf_oak.glb
  plant_monstera.glb
  tv_stand_minimal.glb
  dining_table_round.glb
  bed_platform_queen.glb

  floor_patch.glb      <-- a flat 1x1 m square used to hide real clutter

WHERE TO GET FREE (CC0 / royalty-free) .glb MODELS:
  - Google Poly successors / Khronos sample assets: https://github.com/KhronosGroup/glTF-Sample-Assets
  - Poly Haven (CC0):        https://polyhaven.com/models
  - Sketchfab (filter: Downloadable + CC0): https://sketchfab.com
  - Quaternius (CC0 furniture packs): https://quaternius.com

TIPS:
  - Keep each model under ~2 MB for smooth phone performance.
  - Models should be "Y-up" and sit on the origin (so they rest on the floor).
  - If a file is missing, that one item simply won't appear — the app won't crash.

To add a brand-new item: drop NAME.glb here, then add one line to ModelCatalog.kt.

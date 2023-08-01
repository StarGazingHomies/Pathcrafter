# Pathcrafter

Minecraft non-voxel-based pathfinding mod.

Unlike Baritone, Maple, Stevebot or any other voxel-based mods, 
this mod aims to navigate the 3d Minecraft landscape without 
restricting movement to whole blocks.

At its core is still the A* algorithm, although the generation of
vertices and edges are heavily modified.
By doing so, movement can occur at any angle and will
heavily reduce the travel time of the planned path.

Note:
- The mod tries to find a "good" solution, **not necessarily** _the most_
optimal one.
- This mod carries a heavy performance penalty over 
alternatives. The mod is not aimed at botting. It is meant 
to be a tool for general navigation without elytra or potentially
for set seed speedrunners.

The following are being implemented:

- Solid Blocks pathfinding
- Jumping v1.0

The following features have yet to be implemented:

- Overlays (make it look better)
---
- Head hitting
- Other collision boxes
- Jumping v1.1
  - Forward/Backward acceleration
- Blipping (post jump, such as 1/2gt space presses making staircases faster)
- Optimizations
- Clean up for v1.0.0 (Options menu, better commands, etc.)
---
- Head hitting v2 (partial blocks)
- Ladders, Water & MLG option
- Swimming
- Extra stops
- Jumping v2
  - Strafing while jumping (making neos possible)
- Damage avoidance (lava, cactus, drowning, etc.)
- Momentum handling
- Slime bounces
- Caching & Path segmentation
- Food calculations
- World state management (block placing, etc.)
- Slipperiness & ice boating
- Basic redstone solving (one could wish)
- Shadowmap Integration (when Shadowmap releases)

The following is outside the current scope of the mod. If you want to
see a feature listed here, implement it yourself.

- Mob avoidance
- Automatic movement
- Search
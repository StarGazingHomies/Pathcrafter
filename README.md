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
- The mod tries to find a "good enough" solution, **not** _the most_
optimal one.
- This mod carries a heavy performance penalty over 
alternatives. The mod is not aimed at botting. It is meant 
to be a tool for general navigation without elytra or potentially
for set seed speedrunners.

The following have been implemented:

Nothing. What, I literally *just* made it.

The following features have yet to be implemented:

- Solid Blocks pathfinding (in progress)
- Overlays (up next, for debugging)
---
- Jumping
- Head hitting
- Other collision boxes
- Advanced jumping
   - Air rerouting to thread through collision boxes
   - Holding keys while jumping (making neos possible)
- Blipping (post jump)
- Optimizations
- Clean up for v1.0.0
---
- Head hitting v2 (partial blocks)
- Ladders, Water & MLG option
- Swimming
- Extra stops
- Damage avoidance
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
# Pathcrafter

Minecraft non-voxel-based pathfinding mod.

Unlike Baritone, Maple, Stevebot or any other voxel-based mods, 
this mod aims to navigate the 3d Minecraft landscape without 
restricting movement to whole blocks.

At its core is still the A* algorithm, although the generation of
vertices and edges are heavily modified to _resemble_ a Navmesh.
By doing so, movement can occur between partial blocks and will
heavily reduce the travel time of the planned path.

Note that this mod carries a heavy performance penalty.

This mod is not aimed at botting. It is meant to be a tool for 
navigating survival worlds without elytra or for 
set seed speedrunners.

Very early alpha. The following features have yet to be implemented:

- Solid Blocks pathfinding
- Path overlay
- Jumping
- Head hitting
- Other collision boxes
- Advanced jumping
   - air critical points, for threading between hitboxes
   - Rerouting to take a (faster) jump across a corner / an edge
- Regular blipping (Blips ups are patched 1.14.4)
- Head hitting v2 (partial blocks)
- Ladders
- Water
- Damage avoidance
- Momentum handling
- Slime bounces
- Falling blocks (flat cost)
- Caching & Path segmentation
- Food
- World state management (block placing, etc.)
- Ice & ice boating (no idea how to do this yet)
- Basic redstone solving (one could wish)
- Shadowmap Integration (when Shadowmap releases)

The following have been implemented:

Nothing. What, I literally *just* made it.

The following is outside the scope of the mod. If you want to see
a feature listed here, ask for permission and implement it yourself.

- Mob avoidance
- Automatic movement
- Search
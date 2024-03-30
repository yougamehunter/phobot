# Phobot
[![CodeFactor](https://www.codefactor.io/repository/github/3arthqu4ke/phobot/badge/1.20.4)](https://www.codefactor.io/repository/github/3arthqu4ke/phobot/overview/main)
[![Docker Image Size (latest by date)](https://img.shields.io/docker/image-size/3arthqu4ke/phobot?logo=docker)](https://hub.docker.com/r/3arthqu4ke/phobot)
[![Lines of code](docs/loc.svg)](https://tokei.rs/b1/github/3arthqu4ke/phobot?category=code)
![Minecraft ](https://img.shields.io/badge/MC-1.20.4-31be51.svg)
[![Build](https://github.com/3arthqu4ke/phobot/actions/workflows/build.yml/badge.svg)](https://github.com/3arthqu4ke/phobot/actions)  
A plugin for [PingBypass](https://github.com/3arthqu4ke/PingBypass).
It contains a 1.20.4 utility mod with a CrystalPvP bot using its modules.
The bot has been built as a proof of concept and specifically for the crystalpvp.cc FFA meta.
It can jump down from spawn and navigate the bedrock surface.
It targets other players and attacks them with modules like KillAura, AutoCrystal, Bomber, AutoMine and AutoTrap.

I will take a break from maintaining this, as the bot has reached the main goals I set for the first milestone:

After I deployed it on a VM with a latency of 10ms it has reached over 2000 elo
(best I saw was 2037) in crystalpvp.cc FFA on its own within a few hours,
though it seems to average out between 1900 and 2000 elo in the presence of stronger players.
It has also managed to beat such 2000+ elo players on some occasions.
A small video from before can be found [here](https://youtu.be/4Mcz-MGM_g8?feature=shared).

## Limitations
Better players, I would say someone who can play above 2000 elo consistently on crystalpvp.cc, should also
win against Phobot consistently. Its main limitations stem from its decision-making and pathfinder.
Here are some of the most pressing issues:
- It has lots of problems with getting set back by the servers AntiCheat.
  I wrote the AntiRubberBand module to mitigate this issue somewhat,
  by making the bot crouch and move slightly into random directions when it gets set back,
  something I personally do when playing, too.
  But real players deal with lag much more efficiently, while the bot can get stuck for multiple seconds,
  something that should be looked into.
- The [logic controlling the bot](src/main/java/me/earth/phobot/bot/behaviours) is still relatively primitive.
  Especially around chasing and running away from targets.
  The bot could definitely benefit from playing more safely, as it mostly pops totems due to approaching players too
  closely or making unnecessary hole changes.
  On the other hand I do not want to make it too passive as that is boring.
- Also, even though crystalpvp has become very automated in general, players still make use of many small strategies,
  which just have not been implemented yet, like clipping into a block to block and mine it,
  making proper use of the Teleport-Killaura, baiting etc..
- As said, this is just a proof of concept, and it has been made specifically for crystalpvp.cc FFA,
  which is a very simple environment consisting only of natural bedrock and some other simple blocks.
  The bot moves between holes, of which there are many in the bedrock terrain of FFA, but will not be able to
  deal with terrain or flat pvp efficiently.
  Some of this is just a problem in the behaviour as the pathfinder can move between arbitrary nodes,
  so if a lack of holes is detected we should just move between nodes and surround on them.
  The pathfinder cannot scaffold up yet, it cannot swim, it cannot fly an elytra,
  it cannot jump for parkour, and it cannot mine a tunnel to reach its goal or to escape traps.
## Thoughts and TODOs
I'll go into some more detail about some issues and how they could be solved:
- **The Pathfinder**  
  Pathfinding in Phobot works in two passes.
  We have the NavigationMesh consisting of MeshNodes,
  which allows us finds a path of discrete BlockPositions very quickly,
  on which we then map the movements that happen each tick with the so called MovementPathfinder.
  Both of these parts leave room for improvement and need some extensions for new features:
  - **Scaffolding**: I think this is the easiest to implement.
    MeshNodes already reference adjacent MeshNodes that we can jump down on.
    We just need to add a special "edge" back to such nodes from the node we can jump down on,
    because then we can scaffold up to it.
    The XZMap could also be used but would be less efficient.
    Traversing such an edge should then cost more,
    or they should only be enabled on a Pathfinder if previous pathfinding without Scaffolding failed.  
    Lastly, the MovementPathfinder needs to know if such edges are present in the path,
    this can be done by reworking the Path class, which is kind of unnecessary in its current state.
  - **Parkour**: This is a lot harder.
    In theory we just need to add "jump edges" between MeshNodes if we can jump from one node to the other.
    The MovementPathfinder then needs to do some special handling so that it jumps at the right part of the block to
    reach the other node.  
    But practically there are some issues:
    - The sheer number of possible jumps.
      If we assume that we can jump 4 blocks, the area of the circle we could cover by jumping from one node would
      be around 49 nodes, and that is for every node in the mesh. Bloating not only the calculation of the Mesh,
      but also the OpenSet of the A* algorithm
    - Verifying jumps is much more costly than verifying if a BlockPosition is a MeshNode
      and checking the 4 potential adjacent nodes.
      This would add a lot to the cost of calculating the Mesh within a chunk when it is loaded.
      For each node we do not only need to calculate a circle,
      but more of a cylinder covering the entire volume that we can cover with our jumping parables.
    - The computation at the time of loading the chunk is not even that bad.
      We could defer it for later, so first the normal mesh is calculated, and then part by part the jump edges get added.
      But whenever a block changes we need to recalculate so many of the "jumping parable cylinders" that the great
      advantage of our mesh gets lost: how fast we can update it when a block changes.  
      Invalidation of "jump edges" can happen efficiently: every position that could invalidate a "jump edge"
      is stored in a lookup table. When an air position turns into a block we can just invalidate all "jump edges"
      that we look up for that position.

    <br></br>
    Now for the solutions:
    - First we could just calculate "jump edges" on demand inside the A* algorithm.
      But still, each time we need to verify the jump, which is costly.
    - Secondly, and I believe this is the best option, we could still manage "jump edges" on mesh level, but
      drastically reduce the number of jumps and only calculate a jump edge if there is no other path
      between two nodes.
      Even further: we only need jump edges between the connected components of our mesh.
      Of course, this can lead to some more inefficient paths, as we might take a longer route to traverse
      a component instead of just jumping over a gap in it. The MovementPathfinder already often
      optimizes such gaps away by finding a bunny hop over a gap.
      Of course maintaining the connected components comes with its own issue,
      but adding single unnecessary jump edges is not that bad, so we only need probabilistic guarantees
      for the connectivity between components.
  - **Tunneling:** This is an interesting one.
    I don't think calculating tunnels during the A* algorithm is that bad.
    We also need it to take the time it takes us to mine the blocks into account as cost.
    We can also pre-calculate this with the mesh.
    We only need to introduce node candidates that could form on each block if we mine the block above.
    Which is a lot, but also not thaaat much, since the world mostly consists of air,
    and it should be easy to maintain.
    Lastly, a thought that I have not yet thought out:
    we could categorize the borders between connected components.
    If two connected components are seperated by solid blocks we could mine through the border.
  - **Escaping Traps**: A bit of an edge case of tunneling:
    we just want to mine the block above our head and instantly step out.
    This could be solved easily with special MeshNodes that we calculate for the block that we want to break,
    which then gives us the edges that get created once the block is broken?
  - **Swimming:** Hmm, this rarely comes up in crystalpvp and as a player I also try to not do it.
    We already build our mesh through liquids, but only at the bottom where the blocks are.
    Just adding swimming logic to the MovementPlayer could already be enough to make the
    MovementPathfinder swim through water properly.
    However, we do not take into account that swimming should have an increased cost
    and that paths through water carry the risk of suffocation.
    I really do not want to bother with this topic, so I'll leave this for now.
  - **Climbables and narrow shafts:** We currently do not account for ladders.
    More generally, as soon as the block shape at a position is not empty we consider that position full.
    However, there are many blocks that are shaped in a way that a player can pass next to them,
    e.g. a player can fall down next to a ladder.
  - **Elytra:** Let's leave this one to baritone.
    <br></br>
  - **MovementPathfinder:** Apart from the features above that it definitely does not support yet,
    the MovementPathfinder is in need of lots of optimizing.
    Mainly jumps: often we just miss our landing on a block, but if we had strafed to the edge of the block
    before we jumped we could have made it.
    I have done some experiments over on the movement-experiments branch.
    I introduced strafe nodes (just linear, on-the-ground movement)
    everytime we were on the ground instead of going directly for the next jumping
    bunny hop node and then tried to run dijkstra or A* on the resulting graph.
    But the search space got far too big far too quickly.
    If we detect a missed jump we could try to optimize this with light backtracking to the point where we jumped?
- **TeamWork, multiple enemies and group fights:**
  Currently Phobots do not work together and also do not work with real players.
  Best you can do is to make them add each other as friends, but that's about it.
  Phobot instances should be connected over a network and automate the friending part,
  as well as sharing targets and supporting each other with loot etc..
  Also, singular Phobot instances are terrible at navigating FFA when many players are there.
  Phobot might start to chase a player and in the process jump over other players,
  catching all of their crystals in the process.

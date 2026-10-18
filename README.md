# Ludum Dare 60

A [libGDX](https://libgdx.com/) game using on [zenlib](https://github.com/zendo-games/zenlib),
a small game library that provides the loop, assets, flags, an entity-component world with collision,
movement, timers, state machines and sensors, Tiled map loading, and an ImGui debug window. This
repo holds the game only: code, art, maps, launchers. Everything named `Example*` is reference
material to copy from, not the game.

## Run it

| Task         | Command                                                                                          |
|--------------|--------------------------------------------------------------------------------------------------|
| Desktop      | `./gradlew lwjgl3:run` (packs `sprites/` into the atlas first; run it once before anything else) |
| Compile only | `./gradlew core:compileJava`                                                                     |
| Web          | `./gradlew teavm:run`, then open http://localhost:8181                                           |
| Runnable jar | `./gradlew lwjgl3:jar` -> `lwjgl3/build/libs`                                                    |

Java 25 (the wrapper downloads a toolchain), Gradle wrapper, Groovy build files. zenlib comes from
JitPack: `zenlibVersion` in `gradle.properties`; its sources jar attaches in the IDE, so
"go to definition" on a library class shows real code. In an IDE, run `Lwjgl3Launcher` with the
working directory set to `assets/`.

## Keys

Game: `WASD` move, `Space` jump / action, double-tap a direction to dash (platformer).

| Key              | Debug                                             |
|------------------|---------------------------------------------------|
| `` ` ``          | collapse / expand the debug window                |
| `5`              | show / hide the debug window (see Gotchas)        |
| `2`              | draw colliders and sensor whiskers                |
| `0` then `9`     | frame-step mode, then one tick per press          |
| `-` `=` or wheel | zoom the world camera                             |
| `M`              | reload the map from disk (save in Tiled, press M) |
| `F`              | rebuild the level from the loaded map             |
| `P`              | pause animations                                  |
| `Esc`            | back to the menu                                  |
| `Shift+Esc`      | quit                                              |

Every `M`/`F`/`P`/`Esc` action is also a button in the debug window's Screen section, and the
window has a view per system: entities and components, movers, contacts, behaviors, sensors,
timers, map layers (toggle visibility there).

## State as of 2026-10-16

```
core/src/main/java/lando/systems/ld60/
  Main                    startup: registers asset enums, picks the first screen
  Config                  window title, size, preferences name
  assets/GameAtlas        sprite atlas key(s)            assets/GameMap   map key(s)
  game/Factory            every entity the game can spawn, one method each; UNIT = 16 (the tile)
  game/GameMask           collision masks (who blocks / detects whom)
  game/GameTags           entity tags (finer than a mask, e.g. PROJECTILE within HAZARD)
  game/GameContacts       what happens when things touch
  game/state/             Behavior states: the player, the enemies
  game/components/        game components: Health, Trigger
  game/movement/          a custom Motion (FlightMotion)
  screens/BaseScreen      world + factory + collision + zoom + debug plumbing
  screens/ExampleScreen   + a Tiled map, the systems, a following camera (abstract)
  screens/ExamplePlatformer, ExampleOverhead   a map + a spawner table each
  screens/IntroScreen     the menu
assets/maps/              00-example-*.tmx, ld60.tiled-project (object classes), tilesets/
assets/sprites/           the packed atlas (generated, do not edit)
sprites/                  atlas input: drop PNGs here, rerun lwjgl3:run
```

## Examples

Two screens on one base. `MapScreen` holds what any map screen needs: the systems, the
spawner, `buildLevel()`, a camera that follows the player, the debug views and keys. A subclass is
three things: `super(game, MAP)`, one `spawner.on(...)` line per Tiled class, `buildLevel()`.

| Screen              | Map                         | Shows                                                                                       |
|---------------------|-----------------------------|---------------------------------------------------------------------------------------------|
| `ExamplePlatformer` | `00-example-platformer.tmx` | gravity, jumps, ramps, one-way platforms, every enemy kind, triggers                        |
| `ExampleOverhead`   | `00-example-top-down.tmx`   | no gravity, 8-way walking between walls, a patrolling enemy, the same spikes and teleporter |

The two players differ only in their `Mover` (a `PlatformerMotion` vs a `TopDownMotion`) and
collider; states, contacts, triggers and hazards are shared and check `mover.is(...)` where the
genre matters. A real game screen starts as a copy of one of the subclasses, with `01-` maps.

## Creating Entities

1. **Tiled**: place an object and give it a class. The project file defines the classes and their
   members with defaults; the map stores only what you override.
2. **`Factory`**: a `thing(MapObject)` overload reads the members with `TiledProps`, then calls a
   plain `thing(x, y, ...)` that assembles the entity from components. The plain one is what you
   call from code (and from a debug "arm" action to spawn at the pointer).
3. **The screen's spawner** maps the class name to the factory: `spawner.on(Factory.THING, (map,
   obj) -> factory.thing(obj))`. An unknown class throws at load, listing the known ones.

To add a kind of thing, copy the closest existing one all the way through (class, factory, spawner):

| You want                         | Copy                                       | What makes it tick                                              |
|----------------------------------|--------------------------------------------|-----------------------------------------------------------------|
| a player                         | `platformerPlayer` or `overheadPlayer`     | `Input(InputControls)`, `Behavior(NORMAL)`, `Contacts`, `Mover` |
| a static thing that hurts        | `spike`                                    | a `HAZARD` collider, nothing else                               |
| a zone with an effect            | `teleporter`, `bouncer` -> `trigger`       | `Trigger` record, handled in `GameContacts.trigger`             |
| a one-way platform               | `jumpthru`                                 | `Collider.platform`, width from a polyline                      |
| an enemy that decides every tick | `walker` (platformer), `patrol` (overhead) | a `Behavior` state that writes `Input`                          |
| an enemy that acts on a beat     | `jumper`, `shooter`, `leaper`              | `Timers.every` in the factory                                   |
| a projectile                     | `bullet`                                   | `Lifetime`, `onHitX`/`onHitY`, `Motion.NONE` or ballistic       |
| something with its own physics   | `flyer`                                    | your own `Motion` (`FlightMotion`)                              |
| a point other objects point at   | `marker`                                   | an `object` member, arrives as a `MapObject`                    |

## Library usage on one screen

| Need                  | Use                                                                                                                                       | See                                           |
|-----------------------|-------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------|
| an asset              | an enum implementing `AtlasType` / `MapType`, registered in `Main.init`; `KEY.get()` after load                                           | `GameAtlas`, `GameMap`                        |
| a screen              | extend `BaseScreen` (or `MapScreen` for a map); override `update`, `renderWorld`, `afterWorldUpdate`; keys via the `inputProcessor` hooks | `MapScreen`                                   |
| an entity             | `world.createEntity().add(component)...`; `world.despawn(e)`                                                                              | `Factory`                                     |
| a component           | one class, `public static final ComponentType<X> TYPE = ComponentType.register(X.class)` + `get(e)`                                       | `Health`                                      |
| a position            | `Position(x, y)`: integer pixels, interpolated for rendering                                                                              |                                               |
| a sprite              | `Image(region, w, h)` or `Animator(clip, originX, originY)`; `tint`, `scale`, `facing`                                                    | `pop`                                         |
| collision             | `Collider.rect/circ/capsule/platform(mask, ...)` `.blockedBy(...)` `.detects(...)`                                                        | `Factory`, "Make it yours"                    |
| movement              | `Mover(new PlatformerMotion(tuning))`, `TopDownMotion`, `Motion.NONE`, or your own                                                        | `platformerPlayer`, `overheadPlayer`, `flyer` |
| control               | `Input(InputControls.wasd(actionKey))`; `new Input()` with no controls = AI, a state writes it                                            | `walker`                                      |
| a brain               | `enum X implements State<Entity>` + `Behavior(initial)`; `request` (deferred) / `force`                                                   | `ExamplePlayerState`                          |
| a delay / a beat      | `Timers.after(name, s, t -> ...)`, `every(name, s, ...)`, `cancel(name)`                                                                  | `shooter`, `GameContacts.enter`               |
| touching things       | `Contacts` on the mover + a `ContactResponder`                                                                                            | `GameContacts`                                |
| feelers               | `Sensor` whiskers, cast each tick, `hits(mask)`                                                                                           | `walker`                                      |
| a map                 | `new Tilemap(GameMap.X.get()).spawn(world, SOLID, batch, camera)`, then `MapObjectSpawner`                                                | `ExampleScreen.buildLevel`                    |
| a camera              | `CameraFollow`: `follow(entity, snap)`, `bounds`                                                                                          | `ExampleScreen.afterWorldUpdate`              |
| a switch              | `ZenFlag` (`LOG_DEBUG`, `DEBUG_RENDER`, `FRAME_STEP`...); a game enum via `Flags.register`                                                | `Main.init`                                   |
| logging               | `Log.info(TAG, "fmt %s", arg)`, `Log.debug`, `Log.warn`                                                                                   | everywhere                                    |
| a debug button / view | `actions.add(label, key, runnable)`, `actions.arm(label, key, spawn)`, `DebugViews.xxx(world)`                                            | `BaseScreen`, `MapScreen`                     |

## Library Customization

### Movement

- **Tuning is data, in tiles and seconds.** `PlatformerTuning(unit, ground speed / accel time /
  stop time, air speed / accel / stop / max fall, jump apex height / time to apex, gravity
  multipliers for jump cut and fall, jump buffer / coyote time, Abilities)`.
  `Factory.platformerPlayer` has every field commented; `ENEMY_TUNING` is the minimal version
  (`Abilities.NONE`); the presets are `PlatformerTuning.player(UNIT)` and `ballistic(UNIT)`.
  `TopDownTuning(unit, speed, accelTime, stopTime)`: `overheadPlayer` and `PATROL_TUNING` are the
  two in use, preset `walker(UNIT)`.
- **Swap tuning at runtime** for ice, water, power-ups: `mover.motion(PlatformerMotion.class).tuning
  = ICE`. The derived numbers (gravity, impulses) are computed from the record on demand.
- **Impulses**: `motion.launch(mover, dir, distTiles, heightTiles, lockSeconds)` for knockback,
  bounces, hops (locks steering for the arc); `tuning.impulseForHeight(h)` for a plain vertical
  kick; `PlatformerMode.BALLISTIC` for things that only fall and bounce. Without gravity, set
  `mover.xVel/yVel` and have a state keep steering that way (`ExamplePlayerState.HURT`).
- **What the motion tells you**, read it from a state or a responder: platformer `grounded`,
  `jumped`, `landed`, `dropping`, `facing`, `mode`; top-down `faceX/faceY`, `wallX/wallY`; any
  mover `xVel/yVel`, `xHit/yHit` (blocked this tick, direction), `isMoving()`.
- **Hit callbacks**: `mover.onHitX = (entity, mover, dir) -> ...` runs after the step; bullets pop,
  balls reverse.
- **Your own motion**: implement `Motion` (`control` turns intent into velocity each tick, `after`
  reads the world once the sweep is done). `FlightMotion` is the whole pattern in 40 lines. The
  rest of the stack (collision, contacts, sensors, states) does not care which motion drives.

### Collision

- **Masks**: `GameMask` is an enum, one bit each, up to 32. A collider has one mask, is `blockedBy`
  some masks (cannot pass) and `detects` others (reported as overlaps, never stops). Add a mask by
  adding a constant.
- **Shapes**: `rect`, `circ`, `capsule` (walks ramps; rects do not), `platform` (one-way, from a
  width), `segment`, `grid` (the map's). Offsets are relative to the entity's `Position`. One
  collider per entity; smaller than the sprite is normal (the overhead player is a foot box, so
  its head may overlap a wall behind it).
- **Touching is not overlapping**: a mover resting on the floor is not "in" it. Detection reports
  overlaps only.
- **Queries**: the screen's `collision` (`CollisionWorld`): `probe(mover, dx, dy)` ("is there a
  SOLID one pixel that way"), `any/first/all(shape, x, y, dx, dy, masks, excluded)`, `raycast`,
  `detects(mover, out)`.
- **Map collision** is a grid collider built from tagged tiles, see "Tiled" below.

### Contact response

- Put `Contacts` on the entity that needs to react (the player, a bullet). The `ContactSystem`
  calls your `ContactResponder.respond(contact)` after the sweep, once per contact per tick:
  `contact.kind()` is `BLOCKED` (stopped by it) or `OVERLAP` (passed through / detected),
  `phase()` is `ENTER`, `STAY` or `EXIT`, `mover()` is yours, `other()` is the thing.
- `GameContacts` is the switch: the other's mask -> handler. A new interaction is a new case
  there; a tag (`GameTags`) refines within a mask when needed (a projectile is a hazard that also
  despawns on hit).
- Keep it reactive: request a state, start a timer, change a component, despawn. Do not move
  entities from inside a response; the state or the timer does that next tick. An `EXIT` may name
  an entity that is already gone, check `isAlive()` before touching it.

### Behavior

- A state is an enum constant implementing `State<Entity>`: `enter`, `update` (return the next
  state or `this`), `exit`, `interruptible(by)`. `Behavior(initial)` on the entity.
- `behavior.request(STATE)` is deferred to the next behavior tick, last request wins, refused if
  the current state is not interruptible by it; `behavior.machine.force(e, STATE)` is immediate.
  Turn on `ZenFlag.LOG_STATE` to see transitions and refusals.
- AI steers by writing `Input.moveX/moveY` and the action flags, the same fields the keyboard
  writes; `ExampleEnemyState` has two patterns (`WALK` with a sensor and `xHit`, `PATROL` with the
  motion's `wallX/wallY`; the comment on `PATROL` says why not `xHit` there). The behavior
  system runs after the input system, so a state can override the keyboard (the player's `HURT`
  does).
- Per-entity memory goes in a component, never in the enum (it is shared by every entity).

### Timers

- `Timers` component: `after(name, seconds, timer -> ...)` once, `every(name, seconds, ...)`
  repeating, `cancel(name)`; adding a name that exists replaces it. Inside the action,
  `timer.entity()` is the owner. Timers count ticks, so they freeze with pause and frame-step.

### Tiled

- **Classes**: `assets/maps/ld60.tiled-project` holds the object classes (Project > Custom Types).
  A class has members with defaults; an object only stores overrides. Read them in `Factory` with
  `TiledProps.require(obj, "name", Float.class)` (throws naming the object when missing),
  `get(obj, "name", fallback, type)`, `enumOf(obj, "name", MyEnum.class)`; `x/y/width/height(obj)`
  are integers, y-up; `region(obj)` is the tile a tile object was placed with.
- **References**: a member of type `object` arrives resolved as the referenced `MapObject` (a
  `marker` point gives you `x`/`y`). Used by the teleporter and the leaper.
- **Collision**: on a tile layer set the property `collision = tagged`: tiles whose `shape` tile
  property is `full`, `ramp_ne/nw/se/sw` collide, the rest is art. `collision = all` makes every
  tile a block. Tag tiles in the tileset (`.tsx`), once; flips and rotations of a tagged tile are
  honored. Both example maps do it this way (`middle` on the platformer, `walls` overhead).
- **Draw order**: layers draw in file order in the world layer; the `layer` property (`background`,
  `world`, `foreground`, `overlay`) moves a layer under or over the entities, `order` overrides
  the file position.
- **Tile properties are not object properties.** An object placed with a tile does not inherit
  the tile's properties even though Tiled shows them grayed in. `GameTiledProps.tileEnumOf` reads
  the tile's (the walker's direction comes from its art); `TiledProps` reads the object's (the
  patrol's direction is a class member). Decide which one a member lives on.
- **The loop**: edit, save in Tiled, press `M` in the game.

## Gotchas

- **Fresh clone shows a missing atlas**: run `./gradlew lwjgl3:run` once; it packs `sprites/`.
- **`KEY.get()` throws "not loaded"**: the enum is not registered in `Main.init`, or it is used
  before loading finishes (`createFirstScreen` is the first safe place).
- **"no spawn for map object"** at load: the Tiled class has no `spawner.on(...)` line.
- **A gray screen and no entities**: a new `MapScreen` subclass forgot to call `buildLevel()`.
- **Position is integers.** Velocity and sub-pixel remainders live on the `Mover` as floats; the
  renderer interpolates between ticks. `Position(float, float)` rounds; `teleport(x, y)` moves
  without a visible interpolation step.
- **Pooled vectors die next frame.** `FramePool.vec2(...)` and `Tilemap.bounds(...)` return scratch
  objects; copy what you keep.
- **Never poll `Gdx.input` in update.** Use the `Input` component for gameplay and the screen's
  `onKeyDown` hook for everything else; ImGui's capture of the mouse and keyboard is handled there.
- **`world.despawn(e)` is deferred inside a system tick**, immediate elsewhere; `e.isAlive()`
  and `e.isRemoving()` tell you where you stand.
- **One instance per system class**, priorities fixed by `SystemOrder`; add systems before
  `addCommonSystems()`.
- **The debug window cannot be hidden with `5`** while `Main.update` re-enables `SHOW_IMGUI` every
  frame; delete those lines or move the enable into `init()`.
- **Web build**: no reflection in game code without a `reflection.add` in `teavm/build.gradle`
  (vis-ui widgets are listed there), no `java.time`, `javax.*`, threads or NIO files. Records,
  `var`, `java.util.zip` are fine. Test with `teavm:run` before relying on it.
- **Two actions on one key**: `DebugActions.key` runs the first match only.
- **More examples**: zenlib's own README has the consumer recipe; the zenlib-demo project has
  more screens (movement, tilemaps, sensors, transitions) in the same style as `MapScreen`.

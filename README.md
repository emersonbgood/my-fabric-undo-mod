# Unclear Mod

A Fabric mod for Minecraft Java Edition **26.1.2** that intercepts `/clear` commands, logs them,
and lets players (or operators) restore their items with `/unclear` or the **Y** key.

## Features

- Every `/clear` run on any player is **logged** to the server console.
- Cleared items are **saved per player**, stacked as a history (multiple clears = multiple restores).
- Players are **notified in chat** when their inventory is cleared and who did it.
- `/unclear <player>` — operator command (permission level 2) to restore the last `/clear` snapshot.
- **Y key** (rebindable in Controls) — players can press it themselves to restore their own last snapshot.

## How It Works

```
/clear @s potato
  → Mixin captures all potato stacks from @s's inventory
  → /clear runs normally and removes them
  → Server logs: "[Unclear] /clear executed by Steve on Steve — captured 1 stack type(s)."
  → Steve sees in chat: "Your inventory was cleared by Steve. Use /unclear or press Y to restore."

/unclear @s   (or press Y)
  → Potatoes are put back into Steve's inventory (dropped if no space)
  → Server logs: "[Unclear] /unclear restored 1 stack(s) to Steve"
```

Multiple `/clear` calls stack up — each `/unclear`/Y restores one at a time, newest first.

## Building

### Requirements
- **Java SE 25** or higher (required by Minecraft 26.1.2)
  — Download from [Adoptium](https://adoptium.net/) or [Oracle](https://www.oracle.com/java/technologies/downloads/)
- No need to install Gradle — the wrapper handles it

### Before building — verify Fabric versions

Fabric releases new mappings and API builds for each Minecraft version. Before running `./gradlew build`,
go to **https://fabricmc.net/develop/** and check:

| Property in `gradle.properties` | Where to find the right value |
|---|---|
| `yarn_mappings` | "Yarn" column for Minecraft 26.1.2 |
| `loader_version` | "Fabric Loader" (latest stable) |
| `fabric_version` | "Fabric API" for 26.1.2 |

Update those three values in `gradle.properties`, then proceed.

### Steps

1. **Download** this `unclear-mod` folder.
2. Open a terminal inside it.
3. Update `gradle.properties` with the correct Fabric versions (see above).
4. Run:
   ```bash
   # macOS / Linux
   ./gradlew build

   # Windows
   gradlew.bat build
   ```
   First run downloads Minecraft, mappings, and Fabric API (~1 GB, takes a few minutes).
5. Find your built `.jar` in `build/libs/unclear-1.0.0.jar`.
6. Copy it into your Minecraft `mods/` folder alongside **Fabric API**.

### Development (IntelliJ IDEA)
```bash
./gradlew genSources idea
```
Then open the project in IntelliJ. The `runClient` task launches a test client.

## Permissions
- `/unclear` requires **permission level 2** (same as `/clear`).
- The Y key works for **any player** to restore their own items.

## Compatibility
- Minecraft Java Edition **26.1.2** (also compatible with 26.1 and 26.1.1 servers)
- Requires **Java SE 25** or higher

## Notes
- Item history is **in-memory only** — it is lost on server restart.
- If the player's inventory is full, restored items are **dropped at their feet**.
- The mixin captures the full stack (count + components) before `/clear` removes it.

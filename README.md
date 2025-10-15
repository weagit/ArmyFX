# HELBArmy

A Java/JavaFX strategy battle simulation developed for the HELB Java III course.

## Overview
HELBArmy simulates two opposing armies on a grid-based map. Cities generate units, trees provide collectable wood, and timed events (a flag and a philosophical stone) alter the battle flow. The simulation respects strict movement, resource, and combat rules with a one-second global tick. :contentReference[oaicite:1]{index=1}

## Features
- **Game space**: bounded grid; no overlap; obstacle-aware movement and path adjustment. :contentReference[oaicite:2]{index=2}
- **Trees**: 100 wood each; disappear when depleted, regenerate after 30 seconds at the same position. :contentReference[oaicite:3]{index=3}
- **Cities**: 5×5 structures with a single gate; unit spawn and wood deposit points; two opposing teams. :contentReference[oaicite:4]{index=4}
- **Units**: four types with distinct roles and costs:
  - Collectors (harvest wood; 25-unit capacity; deposit and repeat)
  - Deserters (hunt enemy collectors; bonuses vs deserters/pikemen)
  - Cavalry (protect collectors; group movement; dynamic safety distance)
  - Pikemen (move once to a fixed post; shared vision increases with allied pikemen; bonus vs cavalry) :contentReference[oaicite:5]{index=5}
- **Combat system**: adjacent combat loops until one unit dies; bonuses by matchup. :contentReference[oaicite:6]{index=6}
- **Movement system**: horizontal/vertical/diagonal; obstacle avoidance by trying adjacent alternatives. :contentReference[oaicite:7]{index=7}
- **Timing**: global 1-second tick updates logic and rendering. :contentReference[oaicite:8]{index=8}
- **Unit generation**: resource-checked and time-checked with random choice among eligible units. :contentReference[oaicite:9]{index=9}
- **Philosophical stone**: random effect on contact (instant death or temporary invincibility, 50/50). :contentReference[oaicite:10]{index=10}
- **Flag event**: spawns every two minutes; grants 50% HP bonus to the captor and allies. :contentReference[oaicite:11]{index=11}
- **Cheat codes**: quick unit spawn, toggle movements, kill all, spawn flag/stone, restart, etc. :contentReference[oaicite:12]{index=12}

## Architecture
- MVC-like structure with `Controller` orchestrating logic, `View` handling rendering, and a clear model hierarchy (`GameElement`, `Unit` subclasses, `City`, `Tree`, `Flag`, `PhilosophicalStone`). :contentReference[oaicite:13]{index=13}

## Technologies
- Java 17+
- JavaFX
- Runs on Linux

## How to Run
1. Open the project in an IDE that supports JavaFX and Maven/Gradle.
2. Build the project and run the main application class (e.g., `HELBArmyMain`), or use `run.sh` if provided.
3. Use the documented keys to test cheat codes and events.

## Known Limitations
- Cavalry safety-distance behaviour can be inconsistent in long runs.
- Production timing for large units may occasionally overlap. :contentReference[oaicite:14]{index=14}

## Project Report
See `docs/HELBArmy_Report.pdf` for the full write-up. :contentReference[oaicite:15]{index=15}

## Author
Walid El Aidouni El Idrissi  
LinkedIn: https://www.linkedin.com/in/walid-el-aidouni-el-idrissi-06bab8330/

## Overview
HELBArmy simulates two teams on a bounded grid. Cities generate units when resources and timing allow. Trees provide wood and regenerate after depletion. Periodic events (flag, philosophical stone) alter combat outcomes. A global one-second tick drives updates for movement, production, and fights.

---

## Core Features
- Grid-based battlefield with obstacles and no overlap.
- Two cities (one per team) producing units from stored resources.
- Trees as renewable resource nodes; wood is harvested and deposited in cities.
- Four distinct unit classes with complementary roles:
  - **Collector**: gathers wood (25 capacity), deposits, repeats.
  - **Deserter**: hunts enemy collectors; matchup bonuses vs deserters/pikemen.
  - **Cavalry**: escorts collectors; group movement; dynamic safety distance.
  - **Pikeman**: moves once to a fixed post; shared vision grows with allied pikemen.
- Timed global events:
  - **Flag**: spawns at intervals; capture grants HP bonus to holder and allies.
  - **Philosophical Stone**: on contact, 50% chance of instant death or temporary invincibility.
- Cheat codes to spawn units, toggle movement, kill all, spawn events, or restart for testing.

---

## Game Rules & Mechanics
- **Movement**: horizontal/vertical/diagonal. When a cell is blocked, the unit attempts the nearest viable alternative; no two entities share a cell.
- **Combat**: engages on adjacency and loops until one unit dies; class-specific bonuses apply.
- **Resources**:
  - Trees start at 100 wood and vanish at 0.
  - Depleted trees regenerate after a fixed cooldown at the same coordinates.
  - Cities are 5×5 with a single gate; they accept deposits and spawn units at valid times.
- **Production**: unit creation is constrained by stored wood and production timers; when multiple units are eligible, a random eligible type is selected.
- **Timing**: a global one-second tick advances simulation (movement, fights, production, events).

---

## Architecture
- **MVC-like design**:
  - **Model**: `GameElement`, `Unit` hierarchy (Collector, Deserter, Cavalry, Pikeman), `City`, `Tree`, `Flag`, `PhilosophicalStone`.
  - **Controller**: orchestrates tick updates, spawns, collisions, combat resolution, and event lifecycles.
  - **View (JavaFX)**: renders grid, entities, health/ownership states, and HUD.
- Clear separation of concerns; controller mediates user interactions and simulation stepping.

---

## Tech Stack
- Java 17+
- JavaFX
- Maven or Gradle
- Runs on Linux/Windows (IDE-friendly)

---

## Getting Started
1. Download or clone the repository into your IDE (IntelliJ/Eclipse).
2. Ensure JavaFX libraries are configured (if your IDE template does not include them).
3. If using Maven, open the project as a Maven project so dependencies resolve automatically.

---

## How to Run
- **From IDE**: run the main JavaFX application class (e.g., `HELBArmyMain`).
- **From script**: if a `run.sh` is provided, make it executable and run it on Linux.

---

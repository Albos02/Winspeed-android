# Winspeed Android Development Timeline

This timeline is adapted from the PWA roadmap to reflect the current state of the Kotlin/Jetpack Compose implementation.

## Phase 1: Project Foundation (Completed)
- [x] Initialize Android Project with Jetpack Compose
- [x] Set up Material3 Theme and Typography
- [x] Define Core Layout Modes (`TWO_S`, `FOUR_Q`, `FOUR_S`, `SIX_Q`, `SIX_S`)
- [x] Implement Navigation between Settings and Dashboard
- [x] **Current Milestone:** Visual prototype with functional layout switching and scaled fonts.

## Phase 2: Sensor & Data Integration (Current Focus)
- [x] Set up Location Services (FusedLocationProvider) for GPS speed and heading
- [x] Implement Orientation Sensor listeners for device heading
- [x] Implement fused heading logic (GPS + Magnetic) with smoothing
- [x] Create `SailingPoint` data classes for session recording
- [x] Build calculation utilities (Unit conversions, VMG, Wind angles)
- [ ] Implement wind direction calculation
- [ ] Write Unit Tests for all physics/calculation logic
- **Checkpoint:** Dashboard displays real-time GPS data instead of placeholders.

## Summary of Progress
| Phase | Focus | Status |
| :--- | :--- | :--- |
| 1 | UI & Layouts | 100% |
| 2 | Sensors & Physics | 80% |
| 3 | Recording & DB | 0% |
| 4 | Export | 0% |
| 5 | Polish | 10% |

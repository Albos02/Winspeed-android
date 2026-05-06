# Winspeed Android Development Timeline

This timeline is adapted from the PWA roadmap to reflect the current state of the Kotlin/Jetpack Compose implementation.

## Phase 1: Project Foundation (Completed)
- [x] Initialize Android Project with Jetpack Compose
- [x] Set up Material3 Theme and Typography
- [x] Define Core Layout Modes (`TWO_S`, `FOUR_Q`, `FOUR_S`, `SIX_Q`, `SIX_S`)
- [x] Implement Navigation between Settings and Dashboard
- [x] **Current Milestone:** Visual prototype with functional layout switching and scaled fonts.

## Phase 2: Sensor & Data Integration (Current Focus)
- [ ] Set up Location Services (FusedLocationProvider) for GPS speed and heading
- [ ] Implement Orientation Sensor listeners for device heading
- [ ] Create `DataPoint` data classes for session recording
- [ ] Build calculation utilities (Unit conversions, VMG, Wind angles)
- [ ] Write Unit Tests for all physics/calculation logic
- **Checkpoint:** Dashboard displays real-time GPS data instead of placeholders.

## Phase 3: Recording & Persistence (Next)
- [ ] Implement Session Recording service (Background capability)
- [ ] Set up Room Database or DataStore for session storage
- [ ] Create START/STOP/PAUSE recording logic
- [ ] Add session auto-save and crash recovery
- **Checkpoint:** App can record a sailing session and survive a process kill.

## Phase 4: Export & Sharing
- [ ] Implement GPX Export functionality (XML generation)
- [ ] Implement JSON Export for raw sensor data
- [ ] Add Android Share Intent to export files to other apps
- [ ] Build basic "Past Sessions" list view
- **Checkpoint:** User can record a track and export it to Strava/Google Earth.

## Phase 5: Polish & Optimization
- [ ] Screen Wake Lock implementation (keep screen on while recording)
- [ ] Battery optimization for long sessions
- [ ] Refine landscape mode UI for all 5 layout variants
- [ ] Add multiple unit support (Knots, km/h, m/s)
- **Checkpoint:** Production-ready release candidate.

## Summary of Progress
| Phase | Focus | Status |
| :--- | :--- | :--- |
| 1 | UI & Layouts | 100% |
| 2 | Sensors & Physics | 5% |
| 3 | Recording & DB | 0% |
| 4 | Export | 0% |
| 5 | Polish | 10% |

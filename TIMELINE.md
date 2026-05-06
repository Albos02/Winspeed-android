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
- [x] Implement wind direction calculation (Manual & Tack-based estimation)
- [x] Write Unit Tests for all physics/calculation logic
- **Checkpoint:** Dashboard displays real-time GPS data instead of placeholders.

## Phase 3: UI Polish & Safety Improvements (Next)
- [x] Branding: Rename "Settings" to "Winspeed" and integrate `Winspeed-AppIcon.svg`
- [x] Persistence: Remember user settings (Theme, Layout, Wind) on app restart
- [x] Safety: Implement double-click for EXIT button and reduce its size
- [x] Kiosk Mode: Prevent unintended touches and app exiting while recording
- [x] Data Clarity: Replace "Wind" with TWA and display Wind Direction in top-left corner
- [ ] Refactor Labels: Change mode labels to short format (e.g., "4s-data")
- [ ] Styling: Thinner borders, individual container borders, and smaller data labels
- [ ] Theming: Streamline Dark/Light theme with accent color `#0082eb`
- **Checkpoint:** Professional, hardened UI ready for on-water testing.

## Phase 4: Recording & Persistence
- [ ] Implement Session Recording service (Background capability)
- [ ] Set up Room Database or DataStore for session storage
- [ ] Create START/STOP/PAUSE recording logic
- [ ] Add session auto-save and crash recovery
- **Checkpoint:** App can record a sailing session and survive a process kill.

## Phase 5: Export & Sharing
- [ ] Implement GPX Export functionality (XML generation)
- [ ] Implement JSON Export for raw sensor data
- [ ] Add Android Share Intent to export files to other apps
- [ ] Build basic "Past Sessions" list view
- **Checkpoint:** User can record a track and export it to Strava/Google Earth.

## Phase 6: Optimization & Maintenance
- [ ] Screen Wake Lock implementation (keep screen on while recording)
- [ ] Battery optimization for long sessions
- [ ] Refine landscape mode UI for all 5 layout variants
- [ ] Add multiple unit support (Knots, km/h, m/s)
- **Checkpoint:** Production-ready release candidate.

## Summary of Progress
| Phase | Focus | Status |
| :--- | :--- | :--- |
| 1 | UI & Layouts | 100% |
| 2 | Sensors & Physics | 100% |
| 3 | UI Polish & Safety | 62% |
| 4 | Recording & DB | 0% |
| 5 | Export | 0% |
| 6 | Polish | 10% |

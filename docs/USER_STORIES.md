# Bowling Game - User Stories

## Overview
This document describes the user stories for the bowling game application from the perspective of a bowling player who wants to record and track their game performance.

---

## Story 1: Start a New Game
**As a** bowling player  
**I want** to start a new bowling game  
**So that** I can begin recording my rolls and score

### Acceptance Criteria
- [ ] Display a "New Game" button on the landing page
- [ ] Clicking "New Game" initializes a fresh game with 10 frames
- [ ] All game statistics are reset to zero
- [ ] The UI navigates to the game input screen

### Details
- A new game consists of exactly 10 frames
- Initial state shows Frame 1, Roll 1 ready for input
- No previous game data is displayed

---

## Story 2: Enter Roll Results
**As a** bowling player  
**I want** to record the number of pins knocked down for each roll  
**So that** my game is accurately tracked and scored

### Acceptance Criteria
- [ ] Display an input field for the current roll
- [ ] Input only accepts valid pin counts (0-10, or more in 10th frame with bonus rolls)
- [ ] After entering a valid roll, the system validates and updates the game state
- [ ] The form clears for the next roll entry
- [ ] Invalid entries display an error message

### Details
- Valid pin count depends on frame context:
  - **Frames 1-9**: First roll 0-10, second roll depends on first (0 to remaining pins)
  - **Frame 10**: Follows special rules based on strike/spare status
- Form should have a number input or button interface for selecting pin count
- Submission can be triggered by Enter key or a Submit button
- User cannot proceed to next roll if current roll is invalid

### Edge Cases
- Attempting to knock down more pins than available in the frame
- Attempting a second roll after a strike in frames 1-9
- Invalid 10th frame rolls (too many rolls, exceeding pin limits)

---

## Story 3: View Current Frame Information
**As a** bowling player  
**I want** to see the current frame number and roll number I'm on  
**So that** I know where I am in the game

### Acceptance Criteria
- [ ] Display the current frame number (1-10)
- [ ] Display the current roll number (1, 2, or 3 in 10th frame)
- [ ] Update automatically after each valid roll entry
- [ ] Highlight or emphasize the current frame

### Details
- Show format: "Frame X, Roll Y"
- Update in real-time as rolls are submitted
- Clear indication of progression through the game

---

## Story 4: View Knocked Pins Per Roll
**As a** bowling player  
**I want** to see a detailed breakdown of pins knocked down in each frame  
**So that** I can review the individual rolls and their results

### Acceptance Criteria
- [ ] Display a scoreboard showing all frames
- [ ] For each frame, show all individual rolls and pin counts
- [ ] Format clearly distinguishes multiple rolls in a frame
- [ ] Scoreboard updates in real-time as new rolls are entered

### Details
- Display format example:
  - Frame 1: 5, 4 (two rolls)
  - Frame 2: 10 (strike - one roll)
  - Frame 3: 7, / (spare - two rolls, shown as 7, /)
- Use visual indicators for strikes (e.g., "X" or "STR") and spares (e.g., "/")
- Show current frame in a different style/color

---

## Story 5: View Applied Bonus Points
**As a** bowling player  
**I want** to see the bonus points applied to each frame due to strikes and spares  
**So that** I understand how my score is calculated

### Acceptance Criteria
- [ ] Display bonus points for each frame in the scoreboard
- [ ] Show the calculation basis: "base score + bonus"
- [ ] Bonuses update when the frames that contribute to them are completed
- [ ] Bonus display is clear and non-confusing

### Details
- Spare bonus: Points from the next single roll
- Strike bonus: Points from the next two rolls
- Show in scoreboard format:
  - Frame 1 (Spare): Score = 10 + 3 = 13
  - Frame 2 (Strike): Score = 10 + 5 + 4 = 19
- Bonus may be pending until required rolls are entered

---

## Story 6: Strike Indication
**As a** bowling player  
**I want** to see when I've scored a strike in any frame  
**So that** I can recognize exceptional performance

### Acceptance Criteria
- [ ] When all 10 pins are knocked down on the first roll, indicate a strike
- [ ] Strike indicator is visible in the scoreboard
- [ ] Strike automatically advances to the next frame (no second roll needed)
- [ ] In frames 1-9, striking prevents second roll entry

### Details
- Visual indicator: "X", "STR", or highlighted display
- Strike in 10th frame allows 2 bonus rolls
- Strikes should be prominently displayed
- Strike information persists in the scoreboard

---

## Story 7: Spare Indication
**As a** bowling player  
**I want** to see when I've scored a spare in any frame  
**So that** I can recognize when I knocked down all pins using both rolls

### Acceptance Criteria
- [ ] When all 10 pins are knocked down using two rolls, indicate a spare
- [ ] Spare indicator is visible in the scoreboard
- [ ] Spare advances to the next frame after second roll
- [ ] Spare automatically triggers bonus roll in frame 10

### Details
- Visual indicator: "/", "SP", or highlighted display
- Spare in frames 1-9: Advances to next frame
- Spare in 10th frame: Grants 1 bonus roll
- Spare information persists in the scoreboard

---

## Story 8: View Overall Score
**As a** bowling player  
**I want** to see my cumulative score after each frame  
**So that** I know my current total and can track my performance

### Acceptance Criteria
- [ ] Display cumulative score for each completed frame
- [ ] Score updates in real-time as rolls are entered
- [ ] Current frame score is displayed prominently
- [ ] Final game score is shown after frame 10 is complete

### Details
- Show cumulative scores in the scoreboard (running total)
- Format: "Frame 1: 5", "Frame 2: 20" (cumulative)
- Scores for incomplete frames with pending bonuses show as "TBD" or similar
- Perfect game (300) should be recognizable

---

## Story 9: View Game Statistics Overview
**As a** bowling player  
**I want** to see a complete overview of my entire game in one place  
**So that** I can quickly review all key statistics

### Acceptance Criteria
- [ ] Display a comprehensive scoreboard with all 10 frames
- [ ] Scoreboard shows: frame number, rolls, pins, bonuses, strike/spare indicators
- [ ] Show cumulative score for each frame
- [ ] Display overall game score prominently
- [ ] Layout is clean and easy to read

### Details
- Scoreboard should include columns:
  - Frame (1-10)
  - Roll 1 (pins)
  - Roll 2 (pins, or indicator if strike/spare)
  - Bonus (if applicable)
  - Frame Score (cumulative)
- Highlight completed vs. incomplete frames
- Current frame is visually distinct
- Display final score in a prominent location

---

## Story 10: Error Handling for Invalid Rolls
**As a** bowling player  
**I want** to receive clear error messages when I enter invalid roll data  
**So that** I can correct my input and continue playing

### Acceptance Criteria
- [ ] Invalid inputs trigger error messages
- [ ] Error messages are clear and explain what went wrong
- [ ] Error messages suggest what input is valid
- [ ] Invalid rolls are not recorded
- [ ] Error messages disappear when valid input is provided

### Details
- Example errors:
  - "Invalid: Cannot knock down more pins than available in this frame"
  - "Invalid: Strike has been scored; no second roll allowed in frames 1-9"
  - "Invalid: Frame 10 allows maximum 3 rolls"
- Error display is prominent but doesn't block the interface
- Clear color/styling to distinguish errors

---

## Story 11: Reset Game
**As a** bowling player  
**I want** to start over with a new game at any time  
**So that** I can begin a fresh game without navigating away

### Acceptance Criteria
- [ ] Display a "Reset Game" or "New Game" button during gameplay
- [ ] Clicking the button clears all game data
- [ ] Confirmation prompt appears before resetting (to prevent accidents)
- [ ] After reset, UI returns to Frame 1, Roll 1 ready state

### Details
- Button is accessible from the game screen
- Confirmation dialog: "Start a new game? Current progress will be lost."
- Options: "Cancel" or "Start New Game"
- Upon confirmation, scoreboard clears and resets to initial state

---

## Story 12: Game Completion
**As a** bowling player  
**I want** to know when my game is complete  
**So that** I can celebrate my final score and optionally start a new game

### Acceptance Criteria
- [ ] After frame 10 is completed, display a "Game Complete" message
- [ ] Display final score prominently
- [ ] Show game summary (number of strikes, spares, etc. - optional)
- [ ] Provide option to start a new game
- [ ] Disable further roll entries after game completion

### Details
- Game complete after all required rolls in frame 10 are entered
- Display format: "Game Complete! Final Score: X"
- Optional stats: "Strikes: 3, Spares: 2"
- Prominent "New Game" button to continue

---

## Notes

### Technical Constraints
- Single player, single game session
- In-memory state (no persistence initially)
- Web-based UI with HTMX for dynamic updates
- REST API backend handles scoring logic

### Scoring Rules Reference
- Refer to [GLOSSARY.md](./concepts/GLOSSARY.md) for detailed bowling terminology
- [GLOSSARY.md](./concepts/GLOSSARY.md) contains complete scoring rule definitions

### UI/UX Considerations
- Focus on clarity and usability
- Clear visual feedback for user actions
- Real-time updates without full page reloads
- Responsive design for different screen sizes
- Input validation prevents invalid states


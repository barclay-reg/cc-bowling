# Bowling Game REST API Design

## Overview

This document describes the REST API design for the Bowling Game scoring application. The API provides endpoints for managing a single-player bowling game session, including starting new games, recording roll results, and retrieving comprehensive game statistics.

## Design Principles

1. **RESTful**: Uses standard HTTP methods and status codes
2. **Stateful**: Maintains a single in-memory game session
3. **Comprehensive**: Provides all necessary endpoints for the user stories
4. **Validation**: Server-side validation prevents invalid game states
5. **Clear Responses**: Consistent JSON response structure with detailed information

## API Overview

### Base URL
```
http://localhost:8080
```

### Versioning Strategy
API versioning strategy will be decided separately. Current implementation does not include versioning in the URL path. Future versions may use:
- URL-based versioning (`/api/v1/...`, `/api/v2/...`)
- Header-based versioning (Accept-Version, API-Version headers)
- No versioning with forward compatibility

## Resource Model

### Game
The main resource representing a complete bowling game session.

**Key Properties:**
- `gameId`: Unique identifier for the session
- `currentFrame`: Current frame number (1-10)
- `currentRoll`: Current roll number within frame (1-3)
- `frames`: Array of all 10 frames
- `totalScore`: Cumulative score for completed frames
- `isComplete`: Whether all 10 frames have been completed
- `createdAt`: When the game was created
- `updatedAt`: When the game was last updated

**Game Completion:**
- `isComplete` is `true` when all 10 frames are completed with all required rolls recorded
- `isComplete` is `false` while game is still in progress

### Frame
Represents a single frame (turn) in the game.

**Key Properties:**
- `frameNumber`: Position in game (1-10)
- `rolls`: Array of rolls in this frame
- `isStrike`: Whether all 10 pins knocked on first roll
- `isSpare`: Whether all 10 pins knocked using both rolls
- `baseScore`: Total pins knocked in frame
- `bonus`: Additional points from strike/spare (null if pending)
- `frameScore`: Total score for frame (base + bonus)
- `cumulativeScore`: Running total through this frame
- `isCompleted`: Whether frame is finished

### Roll
Represents a single delivery (throw) in a frame.

**Key Properties:**
- `rollNumber`: Position in frame (1-3)
- `pinsKnockedDown`: Number of pins knocked (0-10)

## Endpoint Overview

### Game Management

#### POST /games
**Story:** Story 1 - Start a New Game
- **Description:** Initialize new game with 10 frames
- **Response:** GameResponse with status IN_PROGRESS
- **Status Code:** 201 Created

#### POST /games/current/reset
**Story:** Story 11 - Reset Game
- **Description:** Clear all game data and reinitialize
- **Request:** ResetGameRequest with confirmation flag
- **Response:** GameResponse with new game state
- **Status Code:** 200 OK
- **Validation:** Requires confirmed=true to prevent accidents

### Roll Management

#### POST /games/current/frames/{frameNumber}/rolls
**Stories:** Story 2 - Enter Roll Results
- **Description:** Record pins knocked down for current roll
- **Path Params:** frameNumber (1-10)
- **Request:** RecordRollRequest with pinsKnockedDown
- **Response:** GameResponse with updated game state
- **Status Code:** 200 OK
- **Validation:** 
  - Pin count must match frame context
  - Frames 1-9: Roll 1 (0-10), Roll 2 (0 to remaining)
  - Frame 10: Special rules for strike/spare
  - Cannot record second roll after strike (frames 1-9)
  - Maximum 3 rolls in frame 10

### Game State Queries

#### GET /games/current
**Stories:** Story 3 - View Current Frame Information, Story 4 - View Knocked Pins
- **Description:** Get complete current game state
- **Response:** GameResponse with all frame and roll data
- **Status Code:** 200 OK

#### GET /games/current/frames/{frameNumber}
**Stories:** Story 3, Story 4, Story 5, Story 6, Story 7
- **Description:** Get detailed information for specific frame
- **Path Params:** frameNumber (1-10)
- **Response:** FrameResponse with rolls, bonuses, indicators
- **Status Code:** 200 OK
- **Includes:**
  - Individual rolls with pin counts
  - Strike/spare indicators
  - Base score and bonus points
  - Cumulative score
  - Pending bonus status

#### GET /games/current/scoreboard
**Stories:** Story 4 - View Knocked Pins, Story 5 - View Bonuses, Story 8 - View Overall Score, Story 9 - Game Statistics Overview
- **Description:** Get complete scoreboard with all frames
- **Response:** ScoreboardResponse formatted for display
- **Status Code:** 200 OK
- **Includes:**
  - All 10 frames with rolls
  - Visual indicators (strikes/spares)
  - Applied bonuses
  - Frame scores and cumulative totals
  - Game status and final score

## Scoring Validation

### Pins Per Roll Validation

**Frames 1-9:**
- First roll: 0-10 pins
- Second roll (if applicable): 0 to (10 - first roll pins)
- Maximum 2 rolls per frame
- If first roll = 10 (strike): no second roll allowed

**Frame 10:**
- If strike on roll 1: rolls 2-3 follow normal pin rules (0-10 each)
- If spare (roll 1 + roll 2 = 10): 1 bonus roll, 0-10 pins
- If neither strike nor spare: only 2 rolls allowed
- Maximum 3 rolls total in frame 10

### Bonus Calculation

**Spare Bonus:**
- Score = 10 + pins from next single roll
- Calculated when next roll is recorded
- Exception: Frame 10 spares have no bonus (game ends)

**Strike Bonus:**
- Score = 10 + pins from next two rolls
- Calculated when second roll following strike is recorded
- Exception: Frame 10 strikes have no bonus (game ends)

### Game Completion

**Completion Criteria:**
- Frame 10 is fully recorded with all required rolls
- All bonuses are calculated
- Game status changes to COMPLETED
- Further rolls cannot be recorded

**Final Score:**
- Maximum 300 (perfect game - all strikes)
- Minimum 0 (all gutter balls)
- Calculated as sum of all frame scores

## Error Handling

### Error Response Format

All errors return appropriate HTTP status codes with consistent JSON structure:

```json
{
  "error": "INVALID_ROLL",
  "message": "Cannot knock down more pins than available in this frame",
  "details": "Frame 3, Roll 1: 7 pins knocked. Roll 2 allows maximum 3 pins.",
  "timestamp": "2024-06-07T12:00:00Z"
}
```

### Common Error Scenarios

| Scenario | Status | Error Type | Message |
|----------|--------|-----------|---------|
| Invalid pin count | 400 | INVALID_PIN_COUNT | "Pins must be 0-10" |
| Exceeds available pins | 400 | INVALID_ROLL | "Cannot knock down more pins than available" |
| Roll after strike (frames 1-9) | 400 | INVALID_ROLL | "Strike scored; no second roll allowed" |
| Frame 10 with >3 rolls | 400 | INVALID_ROLL | "Maximum 3 rolls in frame 10" |
| Game already completed | 409 | GAME_COMPLETED | "No further rolls allowed" |
| No active game session | 404 | NOT_FOUND | "No active game session" |
| Invalid frame number | 400 | INVALID_FRAME | "Frame number must be 1-10" |
| Server error | 500 | SERVER_ERROR | "Unexpected server error" |

## Story Coverage

| Story | Endpoint | Method | Path |
|-------|----------|--------|------|
| S1: Start New Game | POST /games | POST | /games |
| S2: Enter Roll Results | recordRoll | POST | /games/current/frames/{frameNumber}/rolls |
| S3: View Current Frame | getCurrentGame, getFrameDetails | GET | /games/current, /games/current/frames/{frameNumber} |
| S4: View Knocked Pins | getScoreboard | GET | /games/current/scoreboard |
| S5: View Bonus Points | getFrameDetails, getScoreboard | GET | /games/current/frames/{frameNumber}, /games/current/scoreboard |
| S6: Strike Indication | getFrameDetails, getScoreboard | GET | /games/current/frames/{frameNumber}, /games/current/scoreboard |
| S7: Spare Indication | getFrameDetails, getScoreboard | GET | /games/current/frames/{frameNumber}, /games/current/scoreboard |
| S8: View Overall Score | getScoreboard | GET | /games/current/scoreboard |
| S9: Game Statistics Overview | getScoreboard | GET | /games/current/scoreboard |
| S10: Error Handling | All endpoints | - | All endpoints |
| S11: Reset Game | resetGame | POST | /games/current/reset |
| S12: Game Completion | getCurrentGame, getScoreboard | GET | /games/current, /games/current/scoreboard |

## Data Persistence

**Current Design:** In-memory session state
- Single active game session at a time
- Game state lost on server restart
- Suitable for single-player, single-session gameplay

**Future Enhancement:** Could add database persistence
- Store game history
- Support multiple concurrent sessions
- Implement game resumption

## Security Considerations

1. **Input Validation:** All numeric inputs validated server-side
2. **Reset Confirmation:** Requires explicit confirmation flag to prevent accidents
3. **Frame Access:** Players cannot modify past frames or skip ahead
4. **No Authentication:** Single-player local game (no auth required)

## Response Format

All successful responses follow consistent JSON structure:

**Game Response Example:**
```json
{
  "gameId": "game-2024-001",
  "currentFrame": 3,
  "currentRoll": 1,
  "frames": [ /* frame array */ ],
  "totalScore": 35,
  "isComplete": false,
  "createdAt": "2024-06-07T12:00:00Z",
  "updatedAt": "2024-06-07T12:05:00Z"
}
```

## API Client Usage Example

```bash
# Start new game
curl -X POST http://localhost:8080/games

# Record a roll (5 pins knocked in frame 1)
curl -X POST http://localhost:8080/games/current/frames/1/rolls \
  -H "Content-Type: application/json" \
  -d '{"pinsKnockedDown": 5}'

# Get current game state
curl -X GET http://localhost:8080/games/current

# Get scoreboard
curl -X GET http://localhost:8080/games/current/scoreboard

# Get frame 5 details
curl -X GET http://localhost:8080/games/current/frames/5

# Reset game (with confirmation)
curl -X POST http://localhost:8080/games/current/reset \
  -H "Content-Type: application/json" \
  -d '{"confirmed": true}'
```

## Implementation Roadmap

1. **Phase 1:** Core scoring engine (frames, rolls, strikes, spares)
2. **Phase 2:** API endpoints with validation
3. **Phase 3:** Error handling and edge cases
4. **Phase 4:** UI integration with HTMX
5. **Phase 5:** Enhanced features (persistence, statistics)

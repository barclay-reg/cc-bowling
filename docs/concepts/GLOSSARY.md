# Bowling Game Glossary

This document defines all business-relevant terms used in the Bowling Game Kata application.

## Core Concepts

### Game
A complete bowling session consisting of exactly 10 frames played by a single player. The game ends after the 10th frame is completed.

### Frame
A single turn in a bowling game. In frames 1-9, a player has up to 2 rolls to knock down pins. In the 10th frame, special rules apply (see **10th Frame**). Each frame accumulates a score based on pins knocked down and applicable bonuses.

### Roll (Attempt)
A single delivery of the bowling ball. The player attempts to knock down pins using one roll. In frames 1-9, a player has a maximum of 2 rolls per frame, unless a strike is scored (which ends the frame immediately).

### Pin
The 10 white cylindrical objects standing at the end of the lane. The objective is to knock down as many pins as possible with each roll.

### Score
The total number of pins knocked down in a frame, including any applicable bonuses (spare or strike bonuses). Each frame's score is accumulated to calculate the total game score.

## Scoring Concepts

### Spare
A spare occurs when a player knocks down all 10 pins using both rolls in a single frame (not on the first roll). When a spare is scored:
- The frame score is 10 (all pins knocked down)
- **Bonus**: The score is increased by the number of pins knocked down on the **next roll** (first roll of the following frame)

**Example**: Frame 3 has 2 rolls: 5 pins + 5 pins = spare. If the first roll of Frame 4 knocks down 3 pins, the Frame 3 score is 10 + 3 = 13.

### Strike
A strike occurs when a player knocks down all 10 pins on the **first roll** of a frame. When a strike is scored:
- The frame ends immediately (no second roll needed)
- The frame score is 10 (all pins knocked down)
- **Bonus**: The score is increased by the number of pins knocked down on the **next two rolls**

**Example**: Frame 2 is a strike (10 pins on first roll). If the next two rolls knock down 5 and 3 pins respectively, the Frame 2 score is 10 + 5 + 3 = 18.

### Bonus
Additional points awarded to a frame due to a spare or strike. The bonus is calculated from subsequent rolls and is added to the frame's base score of 10 pins.

## Special Rules

### 10th Frame
The 10th frame has special rules that differ from frames 1-9:
- If a **strike** is scored on the first roll: the player gets **2 bonus rolls** to complete the frame (3 rolls total)
- If a **spare** is scored (pins knocked down on rolls 1 and 2): the player gets **1 bonus roll** (3 rolls total)
- If neither a strike nor spare is scored: the player takes **only 2 rolls** (standard)
- A maximum of **3 rolls** may be taken in the 10th frame
- No additional bonus is applied after the 10th frame (game ends)

## Game Rules

### Standard Frame (Frames 1-9)
- Maximum 2 rolls per frame
- A strike ends the frame immediately
- Score includes the pins knocked down plus applicable bonuses

### Perfect Game
A theoretical maximum score of 300, achieved by scoring a strike in all 10 frames (12 strikes total: 10 in frames 1-9, plus 2 bonus rolls in the 10th frame).

## Related Concepts

### Gutter Ball
A roll where the ball goes into the gutter (side channels) and no pins are knocked down. This counts as a roll with a score of 0.

### Open Frame
A frame where a player fails to knock down all 10 pins (no strike or spare). The score for an open frame is simply the total number of pins knocked down, with no bonus.

### Lane
The playing surface where the game is conducted. The lane has 10 pins arranged in a triangular pattern at the far end.

### Ball
The heavy spherical object thrown by the player to knock down pins.

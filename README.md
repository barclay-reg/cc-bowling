# Bowling Game Kata

## Abstract

Please create a program that can calculate the
total score of a bowling game based on the scores of individual frames. A simple interface is sufficient: for a backend
solution, for example, text-based, and for a frontend solution, with a focus on usability. One player is
sufficient.

## Customer Requirements

The game consists of 10 frames. In each frame, the player has two attempts (rolls) and can knock down
up to 10 pins. The score per frame is made up of the number of pins knocked down
and additional bonuses. These bonuses result from spares and strikes.

A spare occurs when the player knocks down all 10 pins in a frame. The bonus for this
frame is the number of pins knocked down on the very next roll. An example is
Frame 3 in the image above. The score is 10 (the number of pins knocked down) plus a bonus of 5 (the
number of pins left standing after the first roll of the 4th frame).

A strike occurs when the player knocks down all 10 pins on the first attempt. The bonus for this is the
number of pins knocked down on the next two rolls.

In the tenth frame, a player who achieves a spare or a strike may take an additional roll
to finish the frame. However, no more than 3 rolls may be taken in the last frame.

## Implementation Steps

1. Derive User stories
2. Design the rest-api (API first)
3. Implement the rest-api
4. Implement the business logic
5. Implement the HTMLX frontend

## Possible Improvements

1. use mapstruct to map between the rest-api and the business logic
2. use a database to store the game state

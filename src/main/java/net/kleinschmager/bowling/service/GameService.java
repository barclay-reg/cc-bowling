package net.kleinschmager.bowling.service;

import net.kleinschmager.bowling.api.model.*;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static net.kleinschmager.bowling.service.GameConstants.*;

@Service
public class GameService {

  private final ConcurrentMap<String, Game> games = new ConcurrentHashMap<>();

  private final ScoreService scoreService;

  public GameService(ScoreService scoreService) {
    this.scoreService = scoreService;
  }

  public Game startNewGame() {
    Game currentGame = new Game();
    String id = "game-" + UUID.randomUUID();
    currentGame.setGameId(id);
    initializeGame(currentGame);
    games.put(id, currentGame);
    return currentGame;
  }

  public Game getGame(String gameId) {
    Game g = games.get(gameId);
    if (g == null) throw new NotFoundException("Game not found: " + gameId);
    return g;
  }

  public Game resetGame(String gameId, ResetGameRequest req) {
    Game currentGame = games.get(gameId);
    if (currentGame == null) throw new NotFoundException("Game not found: " + gameId);
    if (req == null || req.getConfirmed() == null || !req.getConfirmed()) {
      throw new BadRequestException("Reset not confirmed");
    }
    initializeGame(currentGame);
    return currentGame;
  }

  public Game recordRoll(String gameId, int frameNumber, RecordRollRequest req) {
    Game currentGame = games.get(gameId);
    if (currentGame == null) throw new NotFoundException("Game not found: " + gameId);
    if (frameNumber < FIRST_FRAME || frameNumber > TOTAL_FRAMES) {
      throw new BadRequestException("Invalid frame number");
    }
    if (req == null || req.getPinsKnockedDown() == null) {
      throw new BadRequestException("pinsKnockedDown required");
    }
    Frame frame = currentGame.getFrames().get(frameNumber - 1);
    if (currentGame.getCurrentFrameNumber() == null || frameNumber != currentGame.getCurrentFrameNumber()) {
      throw new ConflictException("Cannot record roll for non-current frame");
    }

    Roll roll = validateInputAndCreateNewRoll(frameNumber, req, frame);
    frame.getRolls().add(roll);

    updateFrameStatus(frame);

    if (frame.getRollsComplete()) {
      if (currentGame.getCurrentFrameNumber() < TOTAL_FRAMES) {
        currentGame.setCurrentFrameNumber(currentGame.getCurrentFrameNumber() + 1);
        currentGame.setCurrentRollNumber(FIRST_ROLL);
      } else {
        currentGame.setGameComplete(true);
      }
    } else {
      currentGame.setCurrentRollNumber(frame.getRolls().size() + 1);
    }

    currentGame.setUpdatedAt(OffsetDateTime.now());
    scoreService.computeScores(currentGame);
    return currentGame;
  }

  private static @NonNull Roll validateInputAndCreateNewRoll(int frameNumber, RecordRollRequest req, Frame frame) {
    int knockedPins = req.getPinsKnockedDown();
    if (knockedPins < MIN_PINS || knockedPins > MAX_PINS) {
      throw new BadRequestException("pinsKnockedDown out of range");
    }

    // Validate sum of pins in frame for frames 1-9
    if (frameNumber < TOTAL_FRAMES) {
      int knockedPinsFromPreviousRolls = 0;
      for (Roll r : frame.getRolls()) knockedPinsFromPreviousRolls += r.getPinsKnockedDown();
      if (knockedPinsFromPreviousRolls + knockedPins > MAX_PINS) {
        throw new BadRequestException("Total pins in frame cannot exceed " + MAX_PINS);
      }
    } else {
      // final frame validation
      if (frame.getRolls().size() == 1 && frame.getRolls().get(0).getPinsKnockedDown() != MAX_PINS) {
        int knockedPinsFromPreviousRolls = frame.getRolls().get(0).getPinsKnockedDown();
        if (knockedPinsFromPreviousRolls + knockedPins > MAX_PINS) {
          throw new BadRequestException("Total pins in final frame first two rolls cannot exceed " + MAX_PINS + " unless first was strike");
        }
      }
    }

    Roll roll = new Roll();
    roll.setRollNumber(frame.getRolls().size() + 1);
    roll.setPinsKnockedDown(knockedPins);
    return roll;
  }

  public Frame getFrame(String gameId, int frameNumber) {
    Game currentGame = games.get(gameId);
    if (currentGame == null) throw new NotFoundException("Game not found: " + gameId);
    if (frameNumber < FIRST_FRAME || frameNumber > TOTAL_FRAMES) throw new BadRequestException("Invalid frame number");
    return currentGame.getFrames().get(frameNumber - 1);
  }

  /**
   * Updates the frame state after a roll, including strike/spare status and completion status.
   * This refactor computes boolean variables throughout the logic flow and assigns the
   * fields on the frame (strike, spare, rollsComplete) only at the end.
   *
   * @param frame the frame to update with new state
   */
  private void updateFrameStatus(Frame frame) {
    int num = frame.getFrameNumber();
    List<Roll> rolls = frame.getRolls();

    boolean strike = false;
    boolean spare = false;
    boolean rollsComplete = false;

    if (num < TOTAL_FRAMES) {
      // Frames 1-9: max 2 rolls allowed. A third roll only happens in bonus scenarios.
      if (!rolls.isEmpty() && rolls.get(0).getPinsKnockedDown() == MAX_PINS) {
        // Strike: all 10 pins knocked down on first roll
        strike = true;
        rollsComplete = true;
      } else if (rolls.size() >= DEFAULT_ALLOWED_ROLLS) {
        // Two rolls recorded: check if it's a spare (total of 10 pins)
        int first = rolls.get(0).getPinsKnockedDown();
        int second = rolls.get(1).getPinsKnockedDown();
        spare = (first + second) == MAX_PINS;
        rollsComplete = true;
      } else {
        // Only one roll recorded: frame not yet complete, waiting for second roll
        rollsComplete = false;
      }
    } else {
      // Final frame (10): special rules - up to 3 rolls if strike or spare was scored
      if (rolls.size() == FINAL_FRAME_ALLOWED_ROLLS) {
        // All 3 rolls completed
        rollsComplete = true;
      } else if (rolls.size() == DEFAULT_ALLOWED_ROLLS) {
        int firstRollKnocked = rolls.get(0).getPinsKnockedDown();
        int secondRollKnocked = rolls.get(1).getPinsKnockedDown();
        // Grant a third roll if first roll was a strike OR first two rolls sum to 10 (spare)
        if (firstRollKnocked == MAX_PINS || firstRollKnocked + secondRollKnocked == MAX_PINS) {
          rollsComplete = false;
        } else {
          // No strike or spare: frame complete with 2 rolls
          rollsComplete = true;
        }
      } else {
        // Only one roll recorded: waiting for more rolls
        rollsComplete = false;
      }
      // Determine strike status for final frame
      strike = !rolls.isEmpty() && rolls.get(0).getPinsKnockedDown() == MAX_PINS;
      // Determine spare status: second roll + first roll = 10, but first wasn't a strike
      spare = rolls.size() >= DEFAULT_ALLOWED_ROLLS && (rolls.get(0).getPinsKnockedDown() + rolls.get(1).getPinsKnockedDown() == MAX_PINS) && rolls.get(0).getPinsKnockedDown() != MAX_PINS;
    }

    // assign computed values at the very end
    frame.setStrike(strike);
    frame.setSpare(spare);
    frame.setRollsComplete(rollsComplete);
  }

  private void initializeGame(Game currentGame) {
    currentGame.setCreatedAt(OffsetDateTime.now());
    currentGame.setUpdatedAt(OffsetDateTime.now());
    currentGame.setTotalScore(0);
    currentGame.setGameComplete(false);
    List<Frame> frames = new ArrayList<>();
    for (int i = FIRST_FRAME; i <= TOTAL_FRAMES; i++) {
      Frame frame = new Frame();
      frame.setFrameNumber(i);
      frame.setAllowedRolls(i == TOTAL_FRAMES ? FINAL_FRAME_ALLOWED_ROLLS : DEFAULT_ALLOWED_ROLLS);
      frame.setRolls(new ArrayList<>());
      frame.setRollsComplete(false);
      frame.setScorePending(false);
      frame.setBaseScore(0);
      frames.add(frame);
    }
    currentGame.setFrames(frames);
    currentGame.setCurrentFrameNumber(FIRST_FRAME);
    currentGame.setCurrentRollNumber(FIRST_ROLL);
    scoreService.computeScores(currentGame);
  }
}

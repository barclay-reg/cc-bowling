package net.kleinschmager.bowling.service;

import net.kleinschmager.bowling.api.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GameService Tests")
class GameServiceTest {

  private GameService gameService;
  private ScoreService scoreService;

  @BeforeEach
  void setUp() {
    scoreService = new ScoreService();
    gameService = new GameService(scoreService);
  }

  @Test
  @DisplayName("Should create a new game with all frames initialized")
  void testStartNewGame() {

    // WHEN
    Game game = gameService.startNewGame();

    // THEN
    assertNotNull(game);
    assertNotNull(game.getGameId());
    assertTrue(game.getGameId().startsWith("game-"));
    assertEquals(GameConstants.FIRST_FRAME, game.getCurrentFrameNumber());
    assertEquals(GameConstants.FIRST_FRAME, game.getCurrentRollNumber());
    assertEquals(0, game.getTotalScore());
    assertFalse(game.getGameComplete());
    assertNotNull(game.getCreatedAt());
    assertNotNull(game.getUpdatedAt());
    assertEquals(GameConstants.TOTAL_FRAMES, game.getFrames().size());

    for (int i = 0; i < GameConstants.TOTAL_FRAMES; i++) {
      Frame frame = game.getFrames().get(i);
      assertEquals(i + 1, frame.getFrameNumber());
      assertEquals(0, frame.getRolls().size());
      assertFalse(frame.getRollsComplete());
      assertFalse(frame.getScorePending());
      assertEquals(0, frame.getBaseScore());
    }
  }

  @Test
  @DisplayName("Should retrieve an existing game")
  void testGetGame() {
    // GIVEN
    Game created = gameService.startNewGame();

    // WHEN
    Game retrieved = gameService.getGame(created.getGameId());

    // THEN
    assertNotNull(retrieved);
    assertEquals(created, retrieved);
  }

  @Test
  @DisplayName("Should throw NotFoundException when game does not exist")
  void testGetGameNotFound() {
    assertThrows(NotFoundException.class, () -> gameService.getGame("nonexistent-id"));
  }

  @Test
  @DisplayName("Should reset a game to initial state")
  void testResetGame() {
    // GIVEN
    Game game = gameService.startNewGame();
    String gameId = game.getGameId();

    // Record a roll first
    RecordRollRequest rollRequest = new RecordRollRequest();
    rollRequest.setPinsKnockedDown(5);
    gameService.recordRoll(gameId, 1, rollRequest);

    // Reset the game
    ResetGameRequest resetRequest = new ResetGameRequest();
    resetRequest.setConfirmed(true);

    // WHEN
    Game reset = gameService.resetGame(gameId, resetRequest);

    // THEN
    assertEquals(GameConstants.FIRST_FRAME, reset.getCurrentFrameNumber());
    assertEquals(GameConstants.FIRST_ROLL, reset.getCurrentRollNumber());
    assertEquals(0, reset.getTotalScore());
    assertEquals(0, reset.getFrames().get(0).getRolls().size());
  }

  @Test
  @DisplayName("Should throw BadRequestException when reset is not confirmed")
  void testResetGameNotConfirmed() {
    // GIVEN
    Game game = gameService.startNewGame();
    ResetGameRequest resetRequest = new ResetGameRequest();
    resetRequest.setConfirmed(false);

    // WHEN / THEN
    assertThrows(BadRequestException.class, () -> gameService.resetGame(game.getGameId(), resetRequest));
  }

  @Test
  @DisplayName("Should throw BadRequestException when reset confirmation is null")
  void testResetGameNullConfirmation() {
    // GIVEN
    Game game = gameService.startNewGame();
    ResetGameRequest resetRequest = new ResetGameRequest();
    resetRequest.setConfirmed(null);

    // WHEN / THEN
    assertThrows(BadRequestException.class, () -> gameService.resetGame(game.getGameId(), resetRequest));
  }

  @Test
  @DisplayName("Should record a roll successfully")
  void testRecordRoll() {
    // GIVEN
    Game game = gameService.startNewGame();
    RecordRollRequest request = new RecordRollRequest();
    request.setPinsKnockedDown(7);

    // WHEN
    Game updated = gameService.recordRoll(game.getGameId(), 1, request);

    // THEN
    assertEquals(1, updated.getFrames().get(0).getRolls().size());
    assertEquals(7, updated.getFrames().get(0).getRolls().get(0).getPinsKnockedDown());
    assertEquals(2, updated.getCurrentRollNumber());
  }

  @Test
  @DisplayName("Should complete frame after second roll when not strike")
  void testFrameCompletionAfterSecondRoll() {
    // GIVEN
    Game game = gameService.startNewGame();

    // First roll
    RecordRollRequest request1 = new RecordRollRequest();
    request1.setPinsKnockedDown(3);
    gameService.recordRoll(game.getGameId(), 1, request1);

    // Second roll
    RecordRollRequest request2 = new RecordRollRequest();
    request2.setPinsKnockedDown(4);

    // WHEN
    Game updated = gameService.recordRoll(game.getGameId(), 1, request2);

    // THEN
    Frame frame1 = updated.getFrames().get(0);
    assertTrue(frame1.getRollsComplete());
    assertEquals(2, updated.getCurrentFrameNumber());
    assertEquals(1, updated.getCurrentRollNumber());
  }

  @Test
  @DisplayName("Should complete frame after first roll when strike")
  void testFrameCompletionWithStrike() {
    // GIVEN
    Game game = gameService.startNewGame();

    RecordRollRequest request = new RecordRollRequest();
    request.setPinsKnockedDown(10);

    // WHEN
    Game updated = gameService.recordRoll(game.getGameId(), 1, request);

    // THEN
    Frame frame1 = updated.getFrames().get(0);
    assertTrue(frame1.getRollsComplete());
    assertTrue(frame1.getStrike());
    assertEquals(2, updated.getCurrentFrameNumber());
  }

  @Test
  @DisplayName("Should identify spare correctly")
  void testSpareDetection() {
    // GIVEN
    Game game = gameService.startNewGame();

    RecordRollRequest request1 = new RecordRollRequest();
    request1.setPinsKnockedDown(3);
    gameService.recordRoll(game.getGameId(), 1, request1);

    RecordRollRequest request2 = new RecordRollRequest();
    request2.setPinsKnockedDown(7);

    // WHEN
    Game updated = gameService.recordRoll(game.getGameId(), 1, request2);

    // THEN
    Frame frame1 = updated.getFrames().get(0);
    assertTrue(frame1.getSpare());
    assertFalse(frame1.getStrike());
  }

  @Test
  @DisplayName("Should throw BadRequestException when pins exceed 10 in a frame")
  void testPinLimitExceeded() {
    // GIVEN
    Game game = gameService.startNewGame();

    RecordRollRequest request1 = new RecordRollRequest();
    request1.setPinsKnockedDown(7);
    gameService.recordRoll(game.getGameId(), 1, request1);

    RecordRollRequest request2 = new RecordRollRequest();
    request2.setPinsKnockedDown(5);

    // WHEN / THEN
    assertThrows(BadRequestException.class, () -> gameService.recordRoll(game.getGameId(), 1, request2));
  }

  @ParameterizedTest
  @ValueSource(ints = {-1, 11, 100})
  @DisplayName("Should throw BadRequestException for invalid pin counts")
  void testInvalidPinCounts(int pins) {
    // GIVEN
    Game game = gameService.startNewGame();
    RecordRollRequest request = new RecordRollRequest();
    request.setPinsKnockedDown(pins);

    // WHEN / THEN
    assertThrows(BadRequestException.class, () -> gameService.recordRoll(game.getGameId(), 1, request));
  }

  @ParameterizedTest
  @ValueSource(ints = {0, 11, -5})
  @DisplayName("Should throw BadRequestException for invalid frame numbers")
  void testInvalidFrameNumbers(int frameNumber) {
    // GIVEN
    Game game = gameService.startNewGame();
    RecordRollRequest request = new RecordRollRequest();
    request.setPinsKnockedDown(5);

    // WHEN / THEN
    assertThrows(BadRequestException.class, () -> gameService.recordRoll(game.getGameId(), frameNumber, request));
  }

  @Test
  @DisplayName("Should throw ConflictException when recording roll for non-current frame")
  void testRecordRollForNonCurrentFrame() {
    // GVEN
    Game game = gameService.startNewGame();
    RecordRollRequest request = new RecordRollRequest();
    request.setPinsKnockedDown(5);

    // WHEN / THEN
    assertThrows(ConflictException.class, () -> gameService.recordRoll(game.getGameId(), 3, request));
  }

  @Test
  @DisplayName("Should throw BadRequestException when pinsKnockedDown is null")
  void testRecordRollNullPins() {
    // GVEN
    Game game = gameService.startNewGame();
    RecordRollRequest request = new RecordRollRequest();
    request.setPinsKnockedDown(null);

    // WHEN / THEN
    assertThrows(BadRequestException.class, () -> gameService.recordRoll(game.getGameId(), 1, request));
  }

  @Test
  @DisplayName("Should retrieve a specific frame")
  void testGetFrame() {
    // GVEN
    Game game = gameService.startNewGame();

    // WHEN
    Frame frame = gameService.getFrame(game.getGameId(), 1);

    // THEN
    assertNotNull(frame);
    assertEquals(1, frame.getFrameNumber());
  }

  @Test
  @DisplayName("Should throw NotFoundException when frame does not exist")
  void testGetFrameNotFound() {
    // GVEN

    Game game = gameService.startNewGame();
    assertThrows(NotFoundException.class, () -> gameService.getFrame("nonexistent-id", 1));
  }

  @Test
  @DisplayName("Should throw BadRequestException for invalid frame number in getFrame")
  void testGetFrameInvalidFrameNumber() {
    // GVEN
    Game game = gameService.startNewGame();

    // WHEN / THEN
    assertThrows(BadRequestException.class, () -> gameService.getFrame(game.getGameId(), 11));
  }

  @Test
  @DisplayName("Should handle a complete game with all strikes (perfect game)")
  void testPerfectGame() {
    // GVEN
    Game game = gameService.startNewGame();
    String gameId = game.getGameId();

    for (int frame = 1; frame < GameConstants.TOTAL_FRAMES; frame++) {
      RecordRollRequest request = new RecordRollRequest();
      request.setPinsKnockedDown(10);
      game = gameService.recordRoll(gameId, frame, request);
    }

    // WHEN
    RecordRollRequest req1 = new RecordRollRequest();
    req1.setPinsKnockedDown(10);
    gameService.recordRoll(gameId, GameConstants.TOTAL_FRAMES, req1);

    RecordRollRequest req2 = new RecordRollRequest();
    req2.setPinsKnockedDown(10);
    gameService.recordRoll(gameId, GameConstants.TOTAL_FRAMES, req2);

    RecordRollRequest req3 = new RecordRollRequest();
    req3.setPinsKnockedDown(10);
    gameService.recordRoll(gameId, GameConstants.TOTAL_FRAMES, req3);

    // THEN
    assertTrue(game.getGameComplete());
    assertEquals(300, game.getTotalScore());
  }

  @Test
  @DisplayName("Should handle a complete game nearly all strikes")
  void testNearlyPerfectGame() {
    // GVEN
    Game game = gameService.startNewGame();
    String gameId = game.getGameId();

    for (int frame = 1; frame < GameConstants.TOTAL_FRAMES; frame++) {
      RecordRollRequest request = new RecordRollRequest();
      request.setPinsKnockedDown(10);
      game = gameService.recordRoll(gameId, frame, request);
    }

    // WHEN
    // 10th frame: need 3 rolls for a strike
    RecordRollRequest req1 = new RecordRollRequest();
    req1.setPinsKnockedDown(9);
    gameService.recordRoll(gameId, GameConstants.TOTAL_FRAMES, req1);

    RecordRollRequest req2 = new RecordRollRequest();
    req2.setPinsKnockedDown(1);
    gameService.recordRoll(gameId, GameConstants.TOTAL_FRAMES, req2);

    RecordRollRequest req3 = new RecordRollRequest();
    req3.setPinsKnockedDown(10);
    game = gameService.recordRoll(gameId, GameConstants.TOTAL_FRAMES, req3);

    assertTrue(game.getGameComplete());
    assertEquals(279, game.getTotalScore());
  }

  @Test
  @DisplayName("Should handle a complete game with all gutter balls")
  void testAllGutterBalls() {
    // GVEN
    Game game = gameService.startNewGame();
    String gameId = game.getGameId();

    // WHEN
    for (int frame = 1; frame <= GameConstants.TOTAL_FRAMES; frame++) {
      RecordRollRequest request1 = new RecordRollRequest();
      request1.setPinsKnockedDown(0);
      gameService.recordRoll(gameId, frame, request1);

      RecordRollRequest request2 = new RecordRollRequest();
      request2.setPinsKnockedDown(0);
      game = gameService.recordRoll(gameId, frame, request2);
    }

    // THEN
    assertTrue(game.getGameComplete());
    assertEquals(0, game.getTotalScore());
  }

  @Test
  @DisplayName("Should advance to next frame after completing current frame")
  void testFrameAdvancement() {
    // GVEN
    Game game = gameService.startNewGame();

    RecordRollRequest request1 = new RecordRollRequest();
    request1.setPinsKnockedDown(5);
    gameService.recordRoll(game.getGameId(), 1, request1);

    RecordRollRequest request2 = new RecordRollRequest();
    request2.setPinsKnockedDown(3);

    // WHEN
    game = gameService.recordRoll(game.getGameId(), 1, request2);

    // THEN
    assertEquals(2, game.getCurrentFrameNumber());
    assertEquals(1, game.getCurrentRollNumber());
  }
}

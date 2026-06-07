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
        Game game = gameService.startNewGame();

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
        Game created = gameService.startNewGame();
        Game retrieved = gameService.getGame(created.getGameId());

        assertNotNull(retrieved);
        assertEquals(created.getGameId(), retrieved.getGameId());
    }

    @Test
    @DisplayName("Should throw NotFoundException when game does not exist")
    void testGetGameNotFound() {
        assertThrows(NotFoundException.class, () -> gameService.getGame("nonexistent-id"));
    }

    @Test
    @DisplayName("Should reset a game to initial state")
    void testResetGame() {
        Game game = gameService.startNewGame();
        String gameId = game.getGameId();

        // Record a roll first
        RecordRollRequest rollRequest = new RecordRollRequest();
        rollRequest.setPinsKnockedDown(5);
        gameService.recordRoll(gameId, 1, rollRequest);

        // Reset the game
        ResetGameRequest resetRequest = new ResetGameRequest();
        resetRequest.setConfirmed(true);
        Game reset = gameService.resetGame(gameId, resetRequest);

        assertEquals(GameConstants.FIRST_FRAME, reset.getCurrentFrameNumber());
        assertEquals(GameConstants.FIRST_FRAME, reset.getCurrentRollNumber());
        assertEquals(0, reset.getTotalScore());
        assertEquals(0, reset.getFrames().get(0).getRolls().size());
    }

    @Test
    @DisplayName("Should throw BadRequestException when reset is not confirmed")
    void testResetGameNotConfirmed() {
        Game game = gameService.startNewGame();
        ResetGameRequest resetRequest = new ResetGameRequest();
        resetRequest.setConfirmed(false);

        assertThrows(BadRequestException.class, () -> gameService.resetGame(game.getGameId(), resetRequest));
    }

    @Test
    @DisplayName("Should throw BadRequestException when reset confirmation is null")
    void testResetGameNullConfirmation() {
        Game game = gameService.startNewGame();
        ResetGameRequest resetRequest = new ResetGameRequest();
        resetRequest.setConfirmed(null);

        assertThrows(BadRequestException.class, () -> gameService.resetGame(game.getGameId(), resetRequest));
    }

    @Test
    @DisplayName("Should record a roll successfully")
    void testRecordRoll() {
        Game game = gameService.startNewGame();
        RecordRollRequest request = new RecordRollRequest();
        request.setPinsKnockedDown(7);

        Game updated = gameService.recordRoll(game.getGameId(), 1, request);

        assertEquals(1, updated.getFrames().get(0).getRolls().size());
        assertEquals(7, updated.getFrames().get(0).getRolls().get(0).getPinsKnockedDown());
        assertEquals(2, updated.getCurrentRollNumber());
    }

    @Test
    @DisplayName("Should complete frame after second roll when not strike")
    void testFrameCompletionAfterSecondRoll() {
        Game game = gameService.startNewGame();

        // First roll
        RecordRollRequest request1 = new RecordRollRequest();
        request1.setPinsKnockedDown(3);
        gameService.recordRoll(game.getGameId(), 1, request1);

        // Second roll
        RecordRollRequest request2 = new RecordRollRequest();
        request2.setPinsKnockedDown(4);
        Game updated = gameService.recordRoll(game.getGameId(), 1, request2);

        Frame frame1 = updated.getFrames().get(0);
        assertTrue(frame1.getRollsComplete());
        assertEquals(2, updated.getCurrentFrameNumber());
        assertEquals(1, updated.getCurrentRollNumber());
    }

    @Test
    @DisplayName("Should complete frame after first roll when strike")
    void testFrameCompletionWithStrike() {
        Game game = gameService.startNewGame();

        RecordRollRequest request = new RecordRollRequest();
        request.setPinsKnockedDown(10);
        Game updated = gameService.recordRoll(game.getGameId(), 1, request);

        Frame frame1 = updated.getFrames().get(0);
        assertTrue(frame1.getRollsComplete());
        assertTrue(frame1.getStrike());
        assertEquals(2, updated.getCurrentFrameNumber());
    }

    @Test
    @DisplayName("Should identify spare correctly")
    void testSpareDetection() {
        Game game = gameService.startNewGame();

        RecordRollRequest request1 = new RecordRollRequest();
        request1.setPinsKnockedDown(3);
        gameService.recordRoll(game.getGameId(), 1, request1);

        RecordRollRequest request2 = new RecordRollRequest();
        request2.setPinsKnockedDown(7);
        Game updated = gameService.recordRoll(game.getGameId(), 1, request2);

        Frame frame1 = updated.getFrames().get(0);
        assertTrue(frame1.getSpare());
        assertFalse(frame1.getStrike());
    }

    @Test
    @DisplayName("Should throw BadRequestException when pins exceed 10 in a frame")
    void testPinLimitExceeded() {
        Game game = gameService.startNewGame();

        RecordRollRequest request1 = new RecordRollRequest();
        request1.setPinsKnockedDown(7);
        gameService.recordRoll(game.getGameId(), 1, request1);

        RecordRollRequest request2 = new RecordRollRequest();
        request2.setPinsKnockedDown(5);

        assertThrows(BadRequestException.class, () -> gameService.recordRoll(game.getGameId(), 1, request2));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 11, 100})
    @DisplayName("Should throw BadRequestException for invalid pin counts")
    void testInvalidPinCounts(int pins) {
        Game game = gameService.startNewGame();
        RecordRollRequest request = new RecordRollRequest();
        request.setPinsKnockedDown(pins);

        assertThrows(BadRequestException.class, () -> gameService.recordRoll(game.getGameId(), 1, request));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 11, -5})
    @DisplayName("Should throw BadRequestException for invalid frame numbers")
    void testInvalidFrameNumbers(int frameNumber) {
        Game game = gameService.startNewGame();
        RecordRollRequest request = new RecordRollRequest();
        request.setPinsKnockedDown(5);

        assertThrows(BadRequestException.class, () -> gameService.recordRoll(game.getGameId(), frameNumber, request));
    }

    @Test
    @DisplayName("Should throw ConflictException when recording roll for non-current frame")
    void testRecordRollForNonCurrentFrame() {
        Game game = gameService.startNewGame();
        RecordRollRequest request = new RecordRollRequest();
        request.setPinsKnockedDown(5);

        assertThrows(ConflictException.class, () -> gameService.recordRoll(game.getGameId(), 3, request));
    }

    @Test
    @DisplayName("Should throw BadRequestException when pinsKnockedDown is null")
    void testRecordRollNullPins() {
        Game game = gameService.startNewGame();
        RecordRollRequest request = new RecordRollRequest();
        request.setPinsKnockedDown(null);

        assertThrows(BadRequestException.class, () -> gameService.recordRoll(game.getGameId(), 1, request));
    }

    @Test
    @DisplayName("Should retrieve a specific frame")
    void testGetFrame() {
        Game game = gameService.startNewGame();
        Frame frame = gameService.getFrame(game.getGameId(), 1);

        assertNotNull(frame);
        assertEquals(1, frame.getFrameNumber());
    }

    @Test
    @DisplayName("Should throw NotFoundException when frame does not exist")
    void testGetFrameNotFound() {
        Game game = gameService.startNewGame();
        assertThrows(NotFoundException.class, () -> gameService.getFrame("nonexistent-id", 1));
    }

    @Test
    @DisplayName("Should throw BadRequestException for invalid frame number in getFrame")
    void testGetFrameInvalidFrameNumber() {
        Game game = gameService.startNewGame();
        assertThrows(BadRequestException.class, () -> gameService.getFrame(game.getGameId(), 11));
    }

    @Test
    @DisplayName("Should handle a complete game with all strikes (perfect game)")
    void testPerfectGame() {
        Game game = gameService.startNewGame();
        String gameId = game.getGameId();

        for (int frame = 1; frame < GameConstants.TOTAL_FRAMES; frame++) {
            RecordRollRequest request = new RecordRollRequest();
            request.setPinsKnockedDown(10);
            game = gameService.recordRoll(gameId, frame, request);
        }
        
        // 10th frame: need 3 rolls for a strike
        RecordRollRequest req1 = new RecordRollRequest();
        req1.setPinsKnockedDown(10);
        gameService.recordRoll(gameId, GameConstants.TOTAL_FRAMES, req1);
        
        RecordRollRequest req2 = new RecordRollRequest();
        req2.setPinsKnockedDown(10);
        gameService.recordRoll(gameId, GameConstants.TOTAL_FRAMES, req2);
        
        RecordRollRequest req3 = new RecordRollRequest();
        req3.setPinsKnockedDown(10);
        game = gameService.recordRoll(gameId, GameConstants.TOTAL_FRAMES, req3);

        assertTrue(game.getGameComplete());
        assertEquals(320, game.getTotalScore());
    }

    @Test
    @DisplayName("Should handle a complete game with all gutter balls")
    void testAllGutterBalls() {
        Game game = gameService.startNewGame();
        String gameId = game.getGameId();

        for (int frame = 1; frame <= GameConstants.TOTAL_FRAMES; frame++) {
            RecordRollRequest request1 = new RecordRollRequest();
            request1.setPinsKnockedDown(0);
            gameService.recordRoll(gameId, frame, request1);

            RecordRollRequest request2 = new RecordRollRequest();
            request2.setPinsKnockedDown(0);
            game = gameService.recordRoll(gameId, frame, request2);
        }

        assertTrue(game.getGameComplete());
        assertEquals(0, game.getTotalScore());
    }

    @Test
    @DisplayName("Should advance to next frame after completing current frame")
    void testFrameAdvancement() {
        Game game = gameService.startNewGame();

        RecordRollRequest request1 = new RecordRollRequest();
        request1.setPinsKnockedDown(5);
        gameService.recordRoll(game.getGameId(), 1, request1);

        RecordRollRequest request2 = new RecordRollRequest();
        request2.setPinsKnockedDown(3);
        game = gameService.recordRoll(game.getGameId(), 1, request2);

        assertEquals(2, game.getCurrentFrameNumber());
        assertEquals(1, game.getCurrentRollNumber());
    }
}

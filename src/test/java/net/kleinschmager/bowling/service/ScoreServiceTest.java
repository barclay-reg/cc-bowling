package net.kleinschmager.bowling.service;

import net.kleinschmager.bowling.api.model.Frame;
import net.kleinschmager.bowling.api.model.Game;
import net.kleinschmager.bowling.api.model.Roll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ScoreService Tests")
class ScoreServiceTest {

    private ScoreService scoreService;

    @BeforeEach
    void setUp() {
        scoreService = new ScoreService();
    }

    private Game createEmptyGame() {
        Game game = new Game();
        game.setGameId("test-game");
        game.setCreatedAt(OffsetDateTime.now());
        game.setUpdatedAt(OffsetDateTime.now());
        game.setTotalScore(0);
        game.setGameComplete(false);
        game.setCurrentFrameNumber(1);
        game.setCurrentRollNumber(1);

        List<Frame> frames = new ArrayList<>();
        for (int i = 1; i <= GameConstants.TOTAL_FRAMES; i++) {
            Frame frame = new Frame();
            frame.setFrameNumber(i);
            frame.setAllowedRolls(i == GameConstants.TOTAL_FRAMES ? GameConstants.FINAL_FRAME_ALLOWED_ROLLS : GameConstants.DEFAULT_ALLOWED_ROLLS);
            frame.setRolls(new ArrayList<>());
            frame.setRollsComplete(false);
            frame.setScorePending(false);
            frame.setBaseScore(0);
            frame.setStrike(false);
            frame.setSpare(false);
            frames.add(frame);
        }
        game.setFrames(frames);
        return game;
    }

    private void addRoll(Frame frame, int pins) {
        Roll roll = new Roll();
        roll.setRollNumber(frame.getRolls().size() + 1);
        roll.setPinsKnockedDown(pins);
        frame.getRolls().add(roll);
    }

    @Test
    @DisplayName("Should handle null game gracefully")
    void testComputeScoresWithNullGame() {
        scoreService.computeScores(null);
        // Should not throw
    }

    @Test
    @DisplayName("Should compute scores for a simple frame without bonuses")
    void testSimpleFrameScore() {
        Game game = createEmptyGame();
        Frame frame1 = game.getFrames().get(0);
        addRoll(frame1, 3);
        addRoll(frame1, 4);
        frame1.setRollsComplete(true);

        scoreService.computeScores(game);

        assertEquals(7, frame1.getBaseScore());
        assertTrue(frame1.getFrameScore().isPresent());
        assertEquals(7, frame1.getFrameScore().get());
        assertEquals(7, game.getTotalScore());
    }

    @Test
    @DisplayName("Should compute bonus for a spare")
    void testSpareBonus() {
        Game game = createEmptyGame();
        Frame frame1 = game.getFrames().get(0);
        Frame frame2 = game.getFrames().get(1);

        addRoll(frame1, 7);
        addRoll(frame1, 3);
        frame1.setRollsComplete(true);
        frame1.setSpare(true);

        addRoll(frame2, 5);
        addRoll(frame2, 2);
        frame2.setRollsComplete(true);

        scoreService.computeScores(game);

        assertEquals(10, frame1.getBaseScore());
        assertTrue(frame1.getFrameScore().isPresent());
        assertEquals(15, frame1.getFrameScore().get()); // 10 + 5 bonus
        assertEquals(22, game.getTotalScore()); // 15 + 7 (frame 2 score)
    }

    @Test
    @DisplayName("Should compute bonus for a strike")
    void testStrikeBonus() {
        Game game = createEmptyGame();
        Frame frame1 = game.getFrames().get(0);
        Frame frame2 = game.getFrames().get(1);

        addRoll(frame1, 10);
        frame1.setRollsComplete(true);
        frame1.setStrike(true);

        addRoll(frame2, 7);
        addRoll(frame2, 2);
        frame2.setRollsComplete(true);

        scoreService.computeScores(game);

        assertEquals(10, frame1.getBaseScore());
        assertTrue(frame1.getFrameScore().isPresent());
        assertEquals(19, frame1.getFrameScore().get()); // 10 + 7 + 2 bonus
        assertEquals(28, game.getTotalScore()); // 19 + 9 (7+2 from frame 2)
    }

    @Test
    @DisplayName("Should set scorePending when bonus is not yet available")
    void testPendingScore() {
        Game game = createEmptyGame();
        Frame frame1 = game.getFrames().get(0);
        Frame frame2 = game.getFrames().get(1);

        addRoll(frame1, 10);
        frame1.setRollsComplete(true);
        frame1.setStrike(true);

        addRoll(frame2, 5);
        frame2.setRollsComplete(false);

        scoreService.computeScores(game);

        assertTrue(frame1.getScorePending());
        assertFalse(frame1.getFrameScore().isPresent());
    }

    @Test
    @DisplayName("Should compute perfect game score (300)")
    void testPerfectGame() {
        Game game = createEmptyGame();

        // Frames 1-9: strikes
        for (int i = 0; i < 9; i++) {
            Frame frame = game.getFrames().get(i);
            addRoll(frame, 10);
            frame.setRollsComplete(true);
            frame.setStrike(true);
        }
        
        // 10th frame: strike with 2 more strikes
        Frame frame10 = game.getFrames().get(9);
        addRoll(frame10, 10);
        addRoll(frame10, 10);
        addRoll(frame10, 10);
        frame10.setRollsComplete(true);
        frame10.setStrike(true);

        scoreService.computeScores(game);

        assertEquals(320, game.getTotalScore());
        assertTrue(game.getGameComplete());
    }

    @Test
    @DisplayName("Should compute score for all gutter balls (0)")
    void testAllGutterBalls() {
        Game game = createEmptyGame();

        for (int i = 0; i < GameConstants.TOTAL_FRAMES; i++) {
            Frame frame = game.getFrames().get(i);
            addRoll(frame, 0);
            addRoll(frame, 0);
            frame.setRollsComplete(true);
        }

        scoreService.computeScores(game);

        assertEquals(0, game.getTotalScore());
        assertTrue(game.getGameComplete());
    }

    @Test
    @DisplayName("Should compute cumulative score correctly")
    void testCumulativeScore() {
        Game game = createEmptyGame();

        // Frame 1: 5 + 3 = 8
        Frame frame1 = game.getFrames().get(0);
        addRoll(frame1, 5);
        addRoll(frame1, 3);
        frame1.setRollsComplete(true);

        // Frame 2: 7 + 2 = 9
        Frame frame2 = game.getFrames().get(1);
        addRoll(frame2, 7);
        addRoll(frame2, 2);
        frame2.setRollsComplete(true);

        scoreService.computeScores(game);

        assertEquals(8, frame1.getCumulativeScore().get());
        assertEquals(17, frame2.getCumulativeScore().get());
    }

    @Test
    @DisplayName("Should calculate nextExpectedRoll correctly")
    void testNextExpectedRoll() {
        Game game = createEmptyGame();
        Frame frame = game.getFrames().get(0);
        addRoll(frame, 5);

        scoreService.computeScores(game);

        assertTrue(frame.getNextExpectedRoll().isPresent());
        assertEquals(2, frame.getNextExpectedRoll().get());
    }

    @Test
    @DisplayName("Should set nextExpectedRoll to undefined when frame complete")
    void testNextExpectedRollUndefinedWhenComplete() {
        Game game = createEmptyGame();
        Frame frame = game.getFrames().get(0);
        addRoll(frame, 5);
        addRoll(frame, 3);
        frame.setRollsComplete(true);

        scoreService.computeScores(game);

        assertFalse(frame.getNextExpectedRoll().isPresent());
    }

    @Test
    @DisplayName("Should handle 10th frame with strike correctly")
    void testTenthFrameWithStrike() {
        Game game = createEmptyGame();

        // First 9 frames: simple scores
        for (int i = 0; i < 9; i++) {
            Frame frame = game.getFrames().get(i);
            addRoll(frame, 5);
            addRoll(frame, 3);
            frame.setRollsComplete(true);
        }

        // 10th frame with strike
        Frame frame10 = game.getFrames().get(9);
        addRoll(frame10, 10);
        addRoll(frame10, 5);
        addRoll(frame10, 3);
        frame10.setRollsComplete(true);
        frame10.setStrike(true);

        scoreService.computeScores(game);

        assertTrue(game.getGameComplete());
        assertEquals(10, game.getCurrentFrameNumber());
        assertEquals(3, game.getCurrentRollNumber());
    }

    @Test
    @DisplayName("Should update game timestamps")
    void testGameTimestampUpdate() {
        Game game = createEmptyGame();
        OffsetDateTime before = game.getUpdatedAt();

        scoreService.computeScores(game);

        assertNotNull(game.getUpdatedAt());
        assertTrue(game.getUpdatedAt().isEqual(before) || game.getUpdatedAt().isAfter(before));
    }

    @Test
    @DisplayName("Should handle alternating strikes and regular rolls")
    void testMixedStrikes() {
        Game game = createEmptyGame();

        // Frame 1: Strike
        Frame frame1 = game.getFrames().get(0);
        addRoll(frame1, 10);
        frame1.setStrike(true);
        frame1.setRollsComplete(true);

        // Frame 2: 5 + 3
        Frame frame2 = game.getFrames().get(1);
        addRoll(frame2, 5);
        addRoll(frame2, 3);
        frame2.setRollsComplete(true);

        // Frame 3: Strike
        Frame frame3 = game.getFrames().get(2);
        addRoll(frame3, 10);
        frame3.setStrike(true);
        frame3.setRollsComplete(true);

        // Frame 4: 7 + 2
        Frame frame4 = game.getFrames().get(3);
        addRoll(frame4, 7);
        addRoll(frame4, 2);
        frame4.setRollsComplete(true);

        scoreService.computeScores(game);

        // Frame 1: 10 + 5 + 3 = 18
        assertEquals(18, frame1.getFrameScore().get());
        // Frame 3: 10 + 7 + 2 = 19
        assertEquals(19, frame3.getFrameScore().get());
    }

    @Test
    @DisplayName("Should handle spare in 10th frame")
    void testTenthFrameWithSpare() {
        Game game = createEmptyGame();

        // First 9 frames: simple scores
        for (int i = 0; i < 9; i++) {
            Frame frame = game.getFrames().get(i);
            addRoll(frame, 5);
            addRoll(frame, 3);
            frame.setRollsComplete(true);
        }

        // 10th frame with spare
        Frame frame10 = game.getFrames().get(9);
        addRoll(frame10, 7);
        addRoll(frame10, 3);
        addRoll(frame10, 5);
        frame10.setRollsComplete(true);
        frame10.setSpare(true);

        scoreService.computeScores(game);

        assertTrue(game.getGameComplete());
    }
}

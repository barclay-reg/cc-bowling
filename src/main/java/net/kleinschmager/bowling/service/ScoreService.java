package net.kleinschmager.bowling.service;

import net.kleinschmager.bowling.api.model.Frame;
import net.kleinschmager.bowling.api.model.Game;
import net.kleinschmager.bowling.api.model.Roll;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ScoreService {

    public void computeScores(Game currentGame) {
        if (currentGame == null) return;
        List<Frame> frames = currentGame.getFrames();
        for (Frame f : frames) {
            int base = 0;
            for (Roll r : f.getRolls()) base += r.getPinsKnockedDown();
            f.setBaseScore(base);
            f.setBonus(JsonNullable.<Integer>undefined());
            f.setFrameScore(JsonNullable.<Integer>undefined());
            f.setCumulativeScore(JsonNullable.<Integer>undefined());
            f.setScorePending(false);
            if (f.getRollsComplete()) f.setNextExpectedRoll(JsonNullable.<Integer>undefined());
            else f.setNextExpectedRoll(JsonNullable.of(f.getRolls().size() + 1));
        }

        List<Integer> sequentialPins = new ArrayList<>();
        for (Frame f : frames) {
            for (Roll r : f.getRolls()) sequentialPins.add(r.getPinsKnockedDown());
        }

        int cumulative = 0;
        int pinIndex = 0;
        for (int i = 0; i < frames.size(); i++) {
            Frame f = frames.get(i);
            boolean isStrike = f.getRolls().size() > 0 && f.getRolls().get(0).getPinsKnockedDown() == GameConstants.MAX_PINS;
            f.setStrike(isStrike);
            boolean isSpare = !isStrike && f.getRolls().size() >= GameConstants.DEFAULT_ALLOWED_ROLLS && (f.getRolls().get(0).getPinsKnockedDown() + f.getRolls().get(1).getPinsKnockedDown() == GameConstants.MAX_PINS);
            if (!isStrike) f.setSpare(isSpare);

            Integer bonusVal = null;
            if (isStrike) {
                if (pinIndex + 1 < sequentialPins.size()) {
                    int next1 = sequentialPins.get(pinIndex + 1);
                    Integer next2 = (pinIndex + 2 < sequentialPins.size()) ? sequentialPins.get(pinIndex + 2) : null;
                    if (next2 != null) {
                        bonusVal = next1 + next2;
                    }
                }
            } else if (isSpare) {
                if (pinIndex + 2 < sequentialPins.size()) {
                    bonusVal = sequentialPins.get(pinIndex + 2);
                }
            }

            if (bonusVal != null) {
                f.setBonus(JsonNullable.of(bonusVal));
            } else {
                f.setBonus(JsonNullable.<Integer>undefined());
            }

            if (bonusVal != null) {
                f.setFrameScore(JsonNullable.of(f.getBaseScore() + bonusVal));
            } else if (!f.getScorePending()) {
                if (!isStrike && !isSpare) {
                    f.setFrameScore(JsonNullable.of(f.getBaseScore()));
                } else {
                    f.setFrameScore(JsonNullable.<Integer>undefined());
                }
            } else {
                f.setFrameScore(JsonNullable.<Integer>undefined());
            }

            if ((isStrike || isSpare) && bonusVal == null) f.setScorePending(true);

            if (f.getFrameScore() != null && f.getFrameScore().isPresent()) {
                cumulative += f.getFrameScore().get();
                f.setCumulativeScore(JsonNullable.of(cumulative));
            } else {
                f.setCumulativeScore(JsonNullable.<Integer>undefined());
            }
            pinIndex += f.getRolls().size();
        }

        currentGame.setTotalScore(cumulative);
        currentGame.setUpdatedAt(OffsetDateTime.now());

        int nextFrame = GameConstants.FIRST_FRAME;
        int nextRoll = GameConstants.FIRST_ROLL;
        boolean allComplete = true;
        for (Frame f : frames) {
            if (!f.getRollsComplete()) {
                nextFrame = f.getFrameNumber();
                nextRoll = f.getRolls().size() + 1;
                allComplete = false;
                break;
            }
        }
        if (allComplete) {
            currentGame.setCurrentFrameNumber(GameConstants.TOTAL_FRAMES);
            currentGame.setCurrentRollNumber(GameConstants.FINAL_FRAME_ALLOWED_ROLLS);
            currentGame.setGameComplete(true);
        } else {
            currentGame.setCurrentFrameNumber(nextFrame);
            currentGame.setCurrentRollNumber(nextRoll);
            currentGame.setGameComplete(false);
        }
    }
}

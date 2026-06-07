package net.kleinschmager.bowling.controller;

import net.kleinschmager.bowling.api.GamesApi;
import net.kleinschmager.bowling.api.model.Frame;
import net.kleinschmager.bowling.api.model.Game;
import net.kleinschmager.bowling.api.model.RecordRollRequest;
import net.kleinschmager.bowling.api.model.ResetGameRequest;
import net.kleinschmager.bowling.service.GameService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * Controller, for all Game REST Endpoints.
 */
public class GamesApiController implements GamesApi {

  private final GameService gameService;

  public GamesApiController(GameService gameService) {
    this.gameService = gameService;
  }

  @Override
  public ResponseEntity<Game> startNewGame() {
    Game g = gameService.startNewGame();
    return ResponseEntity.status(HttpStatus.CREATED).body(g);
  }

  @Override
  public ResponseEntity<Game> getGameById(String gameId) {
    Game g = gameService.getGame(gameId);
    return ResponseEntity.ok(g);
  }

  @Override
  public ResponseEntity<Game> recordRoll(String gameId, Integer frameNumber, RecordRollRequest recordRollRequest) {
    Game g = gameService.recordRoll(gameId, frameNumber, recordRollRequest);
    return ResponseEntity.ok(g);
  }

  @Override
  public ResponseEntity<Game> resetGame(String gameId, ResetGameRequest resetGameRequest) {
    Game g = gameService.resetGame(gameId, resetGameRequest);
    return ResponseEntity.ok(g);
  }

  @Override
  public ResponseEntity<Frame> getFrameDetails(String gameId, Integer frameNumber) {
    Frame f = gameService.getFrame(gameId, frameNumber);
    return ResponseEntity.ok(f);
  }
}

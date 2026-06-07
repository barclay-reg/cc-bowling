package net.kleinschmager.bowling;

import net.kleinschmager.bowling.controller.GamesApiController;
import net.kleinschmager.bowling.controller.GlobalExceptionHandler;
import net.kleinschmager.bowling.service.GameService;
import net.kleinschmager.bowling.service.ScoreService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({GamesApiController.class, GlobalExceptionHandler.class, GameService.class, ScoreService.class})
public class BowlingApplication {

  public static void main(String[] args) {
    SpringApplication.run(BowlingApplication.class, args);
  }

}

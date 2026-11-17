package com.petitbac.petitbac_v2.service;

import com.petitbac.petitbac_v2.model.GameRoom;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ScoreServiceTest {

    @Autowired
    private ScoreService scoreService;

    private GameRoom salle(char lettre, Map<String, String> reponsesA, Map<String, String> reponsesB) {
        GameRoom room = new GameRoom("ABC123", 2);
        room.addJoueur("Alice");
        room.addJoueur("Bob");
        room.setLettre(lettre);
        room.submitReponses("Alice", reponsesA);
        room.submitReponses("Bob", reponsesB);
        return room;
    }

    @Test
    void lesLettresRaresValentPlus() {
        assertEquals(3, scoreService.getPointsForLetter('K'));
        assertEquals(2, scoreService.getPointsForLetter('b'));
        assertEquals(1, scoreService.getPointsForLetter('A'));
        assertEquals(12, scoreService.calculateSoloScore('K', 4));
    }

    @Test
    void unMotUniqueRapporteLaValeurDeLaLettreEtUnMotPartageUnPoint() {
        // Lettre B (2 pts) : "Banane" unique pour Alice, "Brocoli" commun (avec et sans majuscule)
        GameRoom room = salle('B',
                Map.of("fruit", "Banane", "legume", "Brocoli"),
                Map.of("fruit", "", "legume", "brocoli"));
        Map<String, Integer> scores = scoreService.calculateMultiScores(room, List.of("fruit", "legume"), null);
        assertEquals(2 + 1, scores.get("Alice"));
        assertEquals(1, scores.get("Bob"));
    }

    @Test
    void leJoueurQuiCrieBacEstPenaliseSurUnMotInvalide() {
        GameRoom room = salle('C', Map.of("fruit", "Cailloux"), Map.of("fruit", "Cerise"));
        Map<String, Integer> scores = scoreService.calculateMultiScores(room, List.of("fruit"), "Alice");
        assertEquals(ScoreService.PENALITE, scores.get("Alice"));
        assertEquals(1, scores.get("Bob"));
    }

    @Test
    void uneContestationGagneeRapporteUnPoint() {
        GameRoom room = salle('C', Map.of("fruit", "Clementinette"), Map.of("fruit", ""));
        Map<String, Integer> scores = scoreService.calculateMultiScores(room, List.of("fruit"), null,
                Set.of(GameRoom.challengeKey("Alice", "fruit")));
        assertEquals(1, scores.get("Alice"));
    }
}

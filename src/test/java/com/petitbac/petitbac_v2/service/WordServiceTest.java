package com.petitbac.petitbac_v2.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class WordServiceTest {

    @Autowired
    private WordService wordService;

    @Test
    void unMotAccentueCompteBienPourSaLettre() {
        assertTrue(wordService.isValid("prenom", "Élodie", 'E'));
        assertTrue(wordService.isValid("metier", "ébéniste", 'e'));
    }

    @Test
    void laPremiereLettreDoitCorrespondre() {
        assertFalse(wordService.isValid("animal", "Chat", 'D'));
        assertFalse(wordService.isValid("animal", "", 'C'));
        assertFalse(wordService.isValid("animal", null, 'C'));
    }
}

package com.petitbac.petitbac_v2.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class WordRepositoryTest {

    @Autowired
    private WordRepository wordRepository;

    @Test
    void normaliseAccentsCasseEtTirets() {
        assertEquals("elephant", WordRepository.normaliser("Éléphant"));
        assertEquals("pomme de terre", WordRepository.normaliser("  Pomme-de-Terre "));
        assertEquals("boeuf", WordRepository.normaliser("Bœuf"));
        assertEquals("cote d ivoire", WordRepository.normaliser("Côte d'Ivoire"));
    }

    @Test
    void trouveUnMotQuelleQueSoitSaGraphie() {
        assertTrue(wordRepository.exists("animal", "elephant"));
        assertTrue(wordRepository.exists("animal", "ÉLÉPHANT"));
        assertTrue(wordRepository.exists("legume", "pomme de terre"));
        assertTrue(wordRepository.exists("capital_pays", "cote d'ivoire"));
    }

    @Test
    void refuseUnMotInconnuOuHorsCategorie() {
        assertFalse(wordRepository.exists("fruit", "Krokmou"));
        assertFalse(wordRepository.exists("fruit", "Carotte"));
        assertFalse(wordRepository.exists("categorie_inconnue", "Pomme"));
        assertFalse(wordRepository.exists("fruit", "   "));
    }

    @Test
    void chaqueCategorieEstChargee() {
        for (String categorie : WordRepository.CATEGORIES) {
            assertFalse(wordRepository.findByCategorie(categorie).isEmpty(), categorie);
        }
    }
}

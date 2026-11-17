package com.petitbac.petitbac_v2.repository;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.text.Normalizer;
import java.util.*;

/**
 * Dictionnaire des mots acceptés, par catégorie.
 *
 * Sources :
 * 1. les fichiers embarqués {@code resources/dictionnaire/<categorie>.txt} (un mot par ligne) ;
 * 2. la table {@code mots(categorie, valeur)} de la base SQLite, si elle existe.
 *
 * Les mots sont comparés sous forme normalisée : sans accents, sans casse,
 * tirets et apostrophes assimilés à des espaces ("Éléphant" = "elephant").
 */
@Repository
public class WordRepository {

    private static final Logger log = LoggerFactory.getLogger(WordRepository.class);

    public static final List<String> CATEGORIES = List.of(
            "prenom", "animal", "fruit", "legume", "capital_pays", "metier");

    @Value("${app.db.path}")
    private String dbPath;

    /** categorie -> (mot normalisé -> forme d'affichage) */
    private final Map<String, Map<String, String>> dictionnaire = new HashMap<>();

    @PostConstruct
    void charger() {
        int total = 0;
        for (String categorie : CATEGORIES) {
            Map<String, String> mots = new TreeMap<>();
            ClassPathResource fichier = new ClassPathResource("dictionnaire/" + categorie + ".txt");
            if (fichier.exists()) {
                try (BufferedReader in = new BufferedReader(
                        new InputStreamReader(fichier.getInputStream(), StandardCharsets.UTF_8))) {
                    String ligne;
                    while ((ligne = in.readLine()) != null) {
                        ligne = ligne.strip();
                        if (!ligne.isEmpty() && !ligne.startsWith("#")) {
                            mots.putIfAbsent(normaliser(ligne), ligne);
                        }
                    }
                } catch (IOException e) {
                    log.warn("Lecture impossible de {} : {}", fichier.getPath(), e.getMessage());
                }
            }
            dictionnaire.put(categorie, mots);
        }
        chargerDepuisBase();
        for (Map<String, String> mots : dictionnaire.values()) total += mots.size();
        log.info("Dictionnaire chargé : {} mots dans {} catégories", total, dictionnaire.size());
    }

    /** Ajoute les mots de la table "mots" si une base de l'équipe est présente. */
    private void chargerDepuisBase() {
        String url = "jdbc:sqlite:" + System.getProperty("user.dir") + "/" + dbPath;
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT categorie, valeur FROM mots")) {
            while (rs.next()) {
                String categorie = rs.getString("categorie").strip().toLowerCase(Locale.ROOT);
                String valeur = rs.getString("valeur");
                if (valeur != null && !valeur.isBlank()) {
                    dictionnaire.computeIfAbsent(categorie, c -> new TreeMap<>())
                            .putIfAbsent(normaliser(valeur), valeur.strip());
                }
            }
        } catch (SQLException e) {
            // Pas de table "mots" : le dictionnaire embarqué suffit.
        }
    }

    /** Vérifie si un mot existe dans la catégorie donnée. */
    public boolean exists(String categorie, String mot) {
        if (categorie == null || mot == null || mot.isBlank()) return false;
        Map<String, String> mots = dictionnaire.get(categorie.strip().toLowerCase(Locale.ROOT));
        return mots != null && mots.containsKey(normaliser(mot));
    }

    /** Retourne tous les mots d'une catégorie, triés. */
    public List<String> findByCategorie(String categorie) {
        Map<String, String> mots = dictionnaire.get(categorie.strip().toLowerCase(Locale.ROOT));
        return mots == null ? List.of() : new ArrayList<>(mots.values());
    }

    /** "Pomme-de-Terre " -> "pomme de terre", "Éléphant" -> "elephant", "Bœuf" -> "boeuf". */
    public static String normaliser(String mot) {
        String s = mot.strip().toLowerCase(Locale.ROOT)
                .replace("œ", "oe").replace("æ", "ae");
        s = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return s.replaceAll("[-'’\\s]+", " ").strip();
    }
}

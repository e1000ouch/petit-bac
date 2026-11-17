package com.petitbac.petitbac_v2.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Affiche les adresses du jeu au démarrage et, si {@code app.open-browser=true}
 * (version Windows "double-clic"), ouvre le navigateur sur la page d'accueil.
 */
@Component
public class StartupBanner {

    private static final Logger log = LoggerFactory.getLogger(StartupBanner.class);

    @Value("${server.port:8080}")
    private int port;

    @Value("${app.open-browser:false}")
    private boolean openBrowser;

    private final MdnsService mdnsService;

    public StartupBanner(MdnsService mdnsService) {
        this.mdnsService = mdnsService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        String local = "http://localhost:" + port;
        String lan;
        try {
            lan = "http://" + MdnsService.resolveLanAddress().getHostAddress() + ":" + port;
        } catch (Exception e) {
            lan = "(adresse réseau indisponible)";
        }

        StringBuilder sb = new StringBuilder("\n")
                .append("  ====================================================\n")
                .append("   Petit Bac est prêt !\n")
                .append("   Sur ce PC          : ").append(local).append('\n')
                .append("   Sur le réseau local : ").append(lan).append('\n');
        if (!mdnsService.getUrl().isEmpty()) {
            sb.append("                         ").append(mdnsService.getUrl()).append('\n');
        }
        sb.append("   Fermez cette fenêtre pour arrêter le serveur.\n")
                .append("  ====================================================");
        log.info(sb.toString());

        if (openBrowser) {
            ouvrirNavigateur(local);
        }
    }

    private void ouvrirNavigateur(String url) {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        String[] commande = os.contains("win") ? new String[]{"rundll32", "url.dll,FileProtocolHandler", url}
                : os.contains("mac") ? new String[]{"open", url}
                : new String[]{"xdg-open", url};
        try {
            new ProcessBuilder(commande).start();
        } catch (Exception e) {
            log.info("Ouvrez {} dans votre navigateur.", url);
        }
    }
}

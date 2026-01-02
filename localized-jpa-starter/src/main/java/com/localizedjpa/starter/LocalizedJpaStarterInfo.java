package com.localizedjpa.starter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Displays LocalizedJPA startup information when the Spring Boot application starts.
 * 
 * <p>This component logs a banner and version information to confirm that
 * LocalizedJPA is properly configured and active in the application.
 *
 * @since 0.1.2
 */
@Component
public class LocalizedJpaStarterInfo {

    private static final Logger log = LoggerFactory.getLogger(LocalizedJpaStarterInfo.class);

    /**
     * Logs LocalizedJPA startup banner when application is ready.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        String version = getVersion();
        
        log.info("");
        log.info("  _                    _ _             _ _ ____   _    ");
        log.info(" | |    ___   ___ __ _| (_)_______  __| | |  _ \\ / \\   ");
        log.info(" | |   / _ \\ / __/ _` | | |_  / _ \\/ _` | | |_) / _ \\  ");
        log.info(" | |__| (_) | (_| (_| | | |/ /  __/ (_| |_|  __/ ___ \\ ");
        log.info(" |_____\\___/ \\___\\__,_|_|_/___\\___|\\__,_(_)_| /_/   \\_\\");
        log.info("");
        log.info(" :: LocalizedJPA :: (v{})", version);
        log.info(" :: Multi-language JPA entity support with compile-time code generation");
        log.info("");
    }

    /**
     * Returns the current LocalizedJPA version from the JAR manifest.
     * Falls back to "dev" if running from IDE without packaged JAR.
     *
     * @return version string
     */
    public String getVersion() {
        Package pkg = getClass().getPackage();
        String version = pkg != null ? pkg.getImplementationVersion() : null;
        return version != null ? version : "dev";
    }
}


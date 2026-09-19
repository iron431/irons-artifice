package io.redspace.irons_artifice.gametest;

import net.minecraft.gametest.framework.GlobalTestReporter;
import net.minecraft.gametest.framework.JUnitLikeTestReporter;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.gametest.GameTestHooks;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;

/**
 * Installs the JUnit-style reporter that writes the suite's XML report, since the game test server has no
 * {@code --report} option. The server calls {@code GlobalTestReporter.finish()} once the run is done, and that
 * call writes the file.
 */
public final class GameTestReport {
    public static final String DESTINATION_PROPERTY = "irons_artifice.gameTestReport";
    private static final String DEFAULT_DESTINATION = "build/gametest-report.xml";

    public static void onRegisterGameTests(RegisterGameTestsEvent event) {
        if (!GameTestHooks.isGametestServer()) {
            return;
        }
        File destination = new File(System.getProperty(DESTINATION_PROPERTY, DEFAULT_DESTINATION));
        File directory = destination.getParentFile();
        if (directory != null) {
            directory.mkdirs();
        }
        try {
            GlobalTestReporter.replaceWith(new JUnitLikeTestReporter(destination));
        } catch (ParserConfigurationException exception) {
            throw new IllegalStateException("could not open the game test report at " + destination, exception);
        }
    }

    private GameTestReport() {
    }
}

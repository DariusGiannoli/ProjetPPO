// src/test/java/ch/epfl/gui_debug/TestSummaryUI.java
package ch.epfl.gui_debug;

import ch.epfl.rechor.gui.SummaryUI;
import ch.epfl.rechor.journey.Journey;
import ch.epfl.rechor.journey.JourneyExtractor;
import ch.epfl.rechor.journey.Router;
import ch.epfl.rechor.timetable.CachedTimeTable;
import ch.epfl.rechor.timetable.mapped.FileTimeTable;
import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.Stations;
import javafx.application.Application;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.util.List;

/**
 * 4. Tests – application JavaFX minimale pour tester SummaryUI.
 * Inspiré de TestDetailUI.
 */
public final class TestSummaryUI extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    /** Recherche l’ID d’une gare par son nom exact. */
    static int stationId(Stations stations, String stationName) {
        for (int i = 0; i < stations.size(); i++) {
            if (stations.name(i).equals(stationName)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Station inconnue : " + stationName);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // 1) Charger l’horaire depuis le dossier "timetable4"
        TimeTable tt = new CachedTimeTable(
                FileTimeTable.in(Path.of("timetable18"))
        );
        Stations stations = tt.stations();

        // 2) Choisir une date et deux gares
        LocalDate date = LocalDate.of(2025, Month.APRIL, 29);
        int depId = stationId(stations, "Ecublens VD, EPFL");
        int arrId = stationId(stations, "Gruyères");

        // 3) Extraire tous les voyages
        Router router = new Router(tt);
        var profile = router.profile(date, arrId);
        List<Journey> journeys = JourneyExtractor.journeys(profile, depId);

        // 4) Créer SummaryUI
        ObservableValue<List<Journey>> journeysO =
                new SimpleObjectProperty<>(journeys);
        ObservableValue<LocalTime> depTimeO =
                new SimpleObjectProperty<>(LocalTime.of(16, 0));
        SummaryUI summaryUI = SummaryUI.create(journeysO, depTimeO);

        // 5) Afficher dans une scène
        Pane root = new BorderPane(summaryUI.rootNode());
        primaryStage.setScene(new Scene(root));
        primaryStage.setMinWidth(400);
        primaryStage.setMinHeight(600);
        primaryStage.setTitle("Test SummaryUI");
        primaryStage.show();
    }
}

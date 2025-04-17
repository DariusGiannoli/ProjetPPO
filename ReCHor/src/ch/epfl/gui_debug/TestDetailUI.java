// src/test/java/ch/epfl/rechor/gui/TestDetailUI.java
package ch.epfl.gui_debug;

import ch.epfl.rechor.gui.DetailUI;
import ch.epfl.rechor.journey.Journey;
import ch.epfl.rechor.journey.JourneyExtractor;
import ch.epfl.rechor.journey.Router;
import ch.epfl.rechor.timetable.CachedTimeTable;
import ch.epfl.rechor.timetable.mapped.FileTimeTable;
import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.Stations;
import javafx.application.Application;
import javafx.beans.value.ObservableValue;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

/**
 * 4. Tests – application JavaFX minimale pour tester DetailUI.
 * D’après l’exemple de la section 4 du PDF. :contentReference[oaicite:0]{index=0}&#8203;:contentReference[oaicite:1]{index=1}
 */
public final class TestDetailUI extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Recherche l’ID d’une gare par son nom exact.
     */
    static int stationId(Stations stations, String stationName) {
        for (int i = 0; i < stations.size(); i++) {
            if (stations.name(i).equals(stationName)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Station inconnue : " + stationName);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // 1) Charger l’horaire depuis le dossier "timetable"
        TimeTable tt = new CachedTimeTable(
                FileTimeTable.in(Path.of("timetable4"))
        );
        Stations stations = tt.stations();

        // 2) Choisir une date et deux gares
        LocalDate date = LocalDate.of(2025, Month.APRIL, 15);
        int depId = stationId(stations, "Ecublens VD, EPFL");
        int arrId = stationId(stations, "Gruyères");

        // 3) Extraire un voyage
        Router router = new Router(tt);
        var profile  = router.profile(date, arrId);
        List<Journey> js = JourneyExtractor.journeys(profile, depId);
        Journey journey = js.get(32);  // 33ᵉ voyage (index 32)

        // 4) Créer DetailUI
        ObservableValue<Journey> journeyO = new SimpleObjectProperty<>(journey);
        DetailUI detailUI = DetailUI.create(journeyO);

        // 5) Afficher dans une scène
        Pane root = new BorderPane(detailUI.rootNode());
        primaryStage.setScene(new Scene(root));
        primaryStage.setMinWidth(400);
        primaryStage.setMinHeight(600);
        primaryStage.setTitle("Test DetailUI");
        primaryStage.show();
    }
}

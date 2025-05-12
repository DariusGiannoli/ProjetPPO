package ch.epfl.rechor.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;            // SplitPane est dans javafx.scene.control
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import ch.epfl.rechor.StopIndex;
import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.mapped.FileTimeTable;
import ch.epfl.rechor.journey.Router;
import ch.epfl.rechor.journey.Profile;
import ch.epfl.rechor.journey.JourneyExtractor;
import ch.epfl.rechor.journey.Journey;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.IntStream;
import java.util.stream.Collectors;

public class Main extends Application {

    /**
     * On conserve le binding dans un attribut pour qu’il ne soit pas
     * ramassé par le GC (JavaFX utilise des références faibles).
     */
    private ObservableValue<List<Journey>> journeysO;

    @Override
    public void start(Stage stage) throws Exception {
        // 1) Charger les données horaires depuis ./timetable
        TimeTable tt = FileTimeTable.in(Path.of("timetable"));

        // 2) Construire la liste des noms principaux depuis tt.stations()
        final List<String> mainNames = IntStream.range(0, tt.stations().size())
                .mapToObj(tt.stations()::name)
                .collect(Collectors.toList());

        // 3) Construire la map alias → nomPrincipal depuis tt.stationAliases()
        final Map<String,String> altToMain = new HashMap<>();
        for (int i = 0; i < tt.stationAliases().size(); i++) {
            altToMain.put(
                    tt.stationAliases().alias(i),
                    tt.stationAliases().stationName(i)
            );
        }

        // 4) Créer l'index pour l’autocomplétion
        StopIndex index = new StopIndex(mainNames, altToMain);

        // 5) Créer l’UI de requête
        QueryUI queryUI = QueryUI.create(index);

        // 6) Préparer le routeur
        Router router = new Router(tt);

        // 7) Créer le binding qui génère la liste de Journey à chaque changement
        journeysO = Bindings.createObjectBinding(
                () -> {
                    String depName = queryUI.depStopO().getValue();
                    String arrName = queryUI.arrStopO().getValue();
                    LocalDate date = queryUI.dateO().getValue();

                    if (depName.isEmpty() || arrName.isEmpty())
                        return List.of();

                    int depId = mainNames.indexOf(depName);
                    int arrId = mainNames.indexOf(arrName);
                    if (depId < 0 || arrId < 0)
                        return List.of();

                    // 7a) Calculer le profil pour la date et la station d’arrivée
                    Profile profile = router.profile(date, arrId);

                    // 7b) Extraire les objets Journey depuis ce profil et la station de départ
                    return JourneyExtractor.journeys(profile, depId);
                },
                queryUI.depStopO(),
                queryUI.arrStopO(),
                queryUI.dateO()
        );

        // 8) Construire le résumé et le détail
        SummaryUI summaryUI = SummaryUI.create(journeysO, queryUI.timeO());
        DetailUI  detailUI  = DetailUI.create(summaryUI.selectedJourneyO());

        // 9) Mettre résumé et détail dans un SplitPane
        SplitPane split = new SplitPane(summaryUI.rootNode(),
                detailUI .rootNode());

        // 10) Assembler le tout dans un BorderPane
        BorderPane root = new BorderPane();
        root.setTop(   queryUI.rootNode());
        root.setCenter(split);

        // 11) Configurer et afficher la scène
        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.setMinWidth(800);
        stage.setMinHeight(600);
        stage.setTitle("ReCHor");
        stage.show();

        // 12) Focus initial sur le champ de départ
        Platform.runLater(() -> scene.lookup("#depStop").requestFocus());
    }

    public static void main(String[] args) {
        launch(args);
    }
}

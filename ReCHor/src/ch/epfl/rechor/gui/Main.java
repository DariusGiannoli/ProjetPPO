package ch.epfl.rechor.gui;

import ch.epfl.rechor.timetable.CachedTimeTable;
import ch.epfl.rechor.timetable.StationAliases;
import ch.epfl.rechor.timetable.Stations;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import ch.epfl.rechor.StopIndex;
import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.mapped.FileTimeTable;
import ch.epfl.rechor.journey.*;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;

/**
 * Classe principale, qui permet de lancer le programme.
 *
 * @author Antoine Lepin (390950)
 * @author Darius Giannoli (380759)
 */
public class Main extends Application {
    // Constantes de l'interface
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final String DEP_STOP_ID = "#depStop";
    private static final List<Journey> EMPTY_JOURNEY_LIST = List.of();

    // Map pour les lookups des indices de stations
    private Map<String, Integer> stationNameToIndex;

    // Cache de profils pour optimiser les recherches répétées
    private ObjectProperty<Profile> cacheProfile;
    private ObjectProperty<String> cacheStop;
    private ObjectProperty<LocalDate> cacheDate;

    /**
     * Charge les données horaires présentes dans la TimeTable, et construire l'interface graphique
     * principale en combinant les parties créées par les classes DetailUI, SummaryUI et QueryUI.
     *
     * @param stage la fenêtre principale pour l'application, sur laquelle la scene est mise.
     * @throws Exception lance une exception s'il y a un problème de lecture des fichiers de la
     * TimeTable dans la methode in.
     */
    @Override
    public void start(Stage stage) throws Exception {
        //Chargement de la timetable
        CachedTimeTable timeTable = new CachedTimeTable(FileTimeTable.in(Path.of("timetable")));

        // Extraction des données des stations
        StopIndex stopIndex = createStopIndex(timeTable);

        // Création de l'UI et du routeur
        QueryUI queryUI = QueryUI.create(stopIndex);
        Router router = new Router(timeTable);

        // Initialisation du cache
        cacheProfile = new SimpleObjectProperty<>();
        cacheStop = new SimpleObjectProperty<>();
        cacheDate = new SimpleObjectProperty<>();

        // Création du binding pour les trajets
        ObservableValue<List<Journey>> journeysO = Bindings.createObjectBinding(
                () -> calculateJourneys(queryUI, router),
                queryUI.depStopO(),
                queryUI.arrStopO(),
                queryUI.dateO()
        );

        // Construction et affichage de l'interface
        SummaryUI summaryUI = SummaryUI.create(journeysO, queryUI.timeO());
        DetailUI detailUI = DetailUI.create(summaryUI.selectedJourneyO());

        // Assemblage de l'interface
        SplitPane split = new SplitPane(summaryUI.rootNode(), detailUI.rootNode());
        BorderPane root = new BorderPane(split,queryUI.rootNode(),null,null,null);

        // Configuration de la scène
        Scene scene = new Scene(root, WIDTH, HEIGHT);
        stage.setScene(scene);
        stage.setMinWidth(WIDTH);
        stage.setMinHeight(HEIGHT);
        stage.setTitle("ReCHor");
        stage.show();

        // Focus initial
        Platform.runLater(() -> scene.lookup(DEP_STOP_ID).requestFocus());
    }

    /**
     * Crée l'index des arrêts à partir de la table horaire.
     */
    private StopIndex createStopIndex(TimeTable timeTable) {
        Stations stations = timeTable.stations();
        StationAliases stationAliases = timeTable.stationAliases();

        // Construction directe
        List<String> allNamesForStopIndex = new ArrayList<>();
        stationNameToIndex = new HashMap<>();
        LinkedHashMap<String, String> aliasToMain = new LinkedHashMap<>();

        // Ajout des noms principaux
        for (int i = 0; i < stations.size(); i++) {
            String stationName = stations.name(i);
            allNamesForStopIndex.add(stationName);
            stationNameToIndex.put(stationName, i);
        }

        // Ajout des alias avec référence aux noms principaux
        for (int i = 0; i < stationAliases.size(); i++) {
            String alias = stationAliases.alias(i);
            String mainName = stationAliases.stationName(i);

            aliasToMain.put(alias, mainName);
            allNamesForStopIndex.add(alias);

            // Alias pointent vers l'index de leur station principale
            Integer mainIndex = stationNameToIndex.get(mainName);
            if (mainIndex != null) {
                stationNameToIndex.put(alias, mainIndex);
            }
        }
        return new StopIndex(allNamesForStopIndex, aliasToMain);
    }

    /**
     * Calcule les trajets en fonction des paramètres de recherche.
     */
    private List<Journey> calculateJourneys(QueryUI queryUI, Router router) {
        String depName = queryUI.depStopO().getValue();
        String arrName = queryUI.arrStopO().getValue();
        LocalDate date = queryUI.dateO().getValue();

        // Retour anticipé pour les entrées vides
        if (depName.isEmpty() || arrName.isEmpty())
            return EMPTY_JOURNEY_LIST;

        // Lookup
        Integer depId = stationNameToIndex.get(depName);
        Integer arrId = stationNameToIndex.get(arrName);

        if (depId == null || arrId == null)
            return EMPTY_JOURNEY_LIST;

        updateCacheIfNeeded(date, arrName, arrId, router);
        return JourneyExtractor.journeys(cacheProfile.get(), depId);
    }

    /**
     * Met à jour le cache de profil si nécessaire.
     */
    private void updateCacheIfNeeded(LocalDate date, String arrName, int arrId, Router router) {
        // Cache invalide si les valeurs ne correspondent pas
        boolean cacheInvalid = !Objects.equals(date, cacheDate.getValue()) ||
                !Objects.equals(arrName, cacheStop.getValue());

        if (cacheInvalid) {
            cacheProfile.set(router.profile(date, arrId));
            cacheDate.set(date);
            cacheStop.set(arrName);
        }
    }

    /**
     * Point d'entrée de l'application.
     *
     * @param args Arguments de la ligne de commande
     */
    public static void main(String[] args) {
        launch(args);
    }
}
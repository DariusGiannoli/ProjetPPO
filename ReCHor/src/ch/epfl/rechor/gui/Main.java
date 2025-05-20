package ch.epfl.rechor.gui;

import ch.epfl.rechor.timetable.CachedTimeTable;
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
import java.util.stream.IntStream;
import java.util.stream.Collectors;

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
    private static final String APP_TITLE = "ReCHor";
    private static final String DEP_STOP_ID = "#depStop";
    private static final String TIMETABLE_PATH = "timetable";

    // Attributs nécessaires pour le binding des données
    private ObservableValue<List<Journey>> journeysO;
    private List<String> mainNames;

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
        CachedTimeTable timeTable = new CachedTimeTable(FileTimeTable.in(Path.of(TIMETABLE_PATH)));

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
        journeysO = Bindings.createObjectBinding(
                () -> calculateJourneys(queryUI, router),
                queryUI.depStopO(),
                queryUI.arrStopO(),
                queryUI.dateO()
        );

        // Construction et affichage de l'interface
        // Construction des composants d'UI
        SummaryUI summaryUI = SummaryUI.create(journeysO, queryUI.timeO());
        DetailUI detailUI = DetailUI.create(summaryUI.selectedJourneyO());

        // Assemblage de l'interface
        SplitPane split = new SplitPane(summaryUI.rootNode(), detailUI.rootNode());
        BorderPane root = new BorderPane(split, queryUI.rootNode(), null, null, null);

        // Configuration de la scène
        Scene scene = new Scene(root, WIDTH, HEIGHT);

        stage.setScene(scene);
        stage.setMinWidth(WIDTH);
        stage.setMinHeight(HEIGHT);
        stage.setTitle(APP_TITLE);
        stage.show();

        // Focus initial
        Platform.runLater(() -> scene.lookup(DEP_STOP_ID).requestFocus());
    }

    /**
     * Crée l'index des arrêts à partir de la table horaire.
     *
     * @param timeTable La table horaire contenant les informations sur les stations
     * @return L'index des arrêts configuré
     */
    private StopIndex createStopIndex(TimeTable timeTable) {
        // Extraction des noms principaux des stations

        mainNames = IntStream.range(0, timeTable.stations().size())
                .mapToObj(timeTable.stations()::name)
                .collect(Collectors.toList());


        // Construction de la map des alias vers les noms principaux
        TreeMap<String, String> altToMain = new TreeMap<>();
        for (int i = 0; i < timeTable.stationAliases().size(); i++) {
            altToMain.put(
                    timeTable.stationAliases().alias(i),
                    timeTable.stationAliases().stationName(i)
            );
        }
        return new StopIndex(mainNames, altToMain);
    }

    /**
     * Calcule les trajets en fonction des paramètres de recherche.
     *
     * @param queryUI L'interface de requête contenant les paramètres
     * @param router Le routeur pour calculer les trajets
     * @return La liste des trajets calculés
     */
    private List<Journey> calculateJourneys(QueryUI queryUI, Router router) {
        String depName = queryUI.depStopO().getValue();
        String arrName = queryUI.arrStopO().getValue();
        LocalDate date = queryUI.dateO().getValue();

        // Retour anticipé pour les entrées vides
        if (depName.isEmpty() || arrName.isEmpty())
            return List.of();

        int depId = mainNames.indexOf(depName);
        int arrId = mainNames.indexOf(arrName);
        if (depId < 0 || arrId < 0)
            return List.of();

        updateCacheIfNeeded(date, arrName, arrId, router);
        return JourneyExtractor.journeys(cacheProfile.get(), depId);
    }

    /**
     * Met à jour le cache de profil si nécessaire.
     *
     * @param date Date de recherche
     * @param arrName Nom de la station d'arrivée
     * @param arrId ID de la station d'arrivée
     * @param router Routeur pour calculer les profils
     */
    private void updateCacheIfNeeded(LocalDate date, String arrName, int arrId, Router router) {
        boolean cacheValid = cacheDate.getValue() != null
                && cacheStop.getValue() != null
                && date.equals(cacheDate.getValue())
                && arrName.equals(cacheStop.getValue());

        if (!cacheValid) {
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
package ch.epfl.rechor.gui;

import ch.epfl.rechor.journey.*;
import ch.epfl.rechor.journey.Journey.Leg;
import ch.epfl.rechor.FormatterFr;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.util.Pair;
import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import static java.awt.Desktop.getDesktop;

/**
 * DetailUI représente la partie de l'interface graphique qui montre les détails d'un voyage.
 * Cette interface affiche les segments de voyage avec une représentation visuelle des étapes
 * et propose l'export vers une carte ou un calendrier.
 *
 * @param rootNode le nœud JavaFX à la racine de son graphe de scène.
 *
 * @author Antoine Lepin (390950)
 * @author Darius Giannoli (380759)
 */
public record DetailUI(Node rootNode) {
    // Dimensions et mesures visuelles
    private static final double CIRCLE_RADIUS = 3.0;
    private static final double LINE_WIDTH = 2.0;
    private static final int ICON_SIZE = 31;
    private static final int GAP_SMALL = 5;
    private static final int GAP_MEDIUM = 10;

    // Constantes de mise en page
    private static final int ICON_ROWS_WITH_INTERMEDIATES = 2;
    private static final int ICON_ROWS_WITHOUT_INTERMEDIATES = 1;

    // Textes d'interface et libellés
    private static final String CALENDAR_TAB_LABEL = "Calendrier";
    private static final String MAP_TAB_LABEL = "Carte";

    // Identifiants de types d'événements
    private static final String DEPARTURE_EVENT_TYPE = "departure";
    private static final String INTERMEDIATE_STOPS_IDENTIFIER = "intermediate-stops";

    // Formats et modèles de chaînes
    private static final String VOYAGE_CALENDAR_FILENAME_TEMPLATE = "voyage_%s.ics";
    private static final String STOPS_DURATION_FORMAT = "%d arrêts, %d min";

    // Identifiants des éléments d'interface
    private static final String BUTTONS_CONTAINER_ID = "buttons";
    private static final String DETAIL_PANEL_ID = "detail";
    private static final String EMPTY_JOURNEY_CONTAINER_ID = "no-journey";
    private static final String JOURNEY_ANNOTATIONS_LAYER_ID = "annotations";
    private static final String JOURNEY_SEGMENTS_GRID_ID = "legs";

    /**
     * Crée le graphe de scène et retourne une instance de DetailUI
     * contenant une référence à sa racine.
     *
     * @param journeyO valeur observable contenant le voyage dont les détails doivent être affichés
     *                 dans l'interface graphique.
     * @return retourne une instance de DetailUI contenant une référence à sa racine.
     */
    public static DetailUI create(ObservableValue<Journey> journeyO) {
        // Crée un panneau défilant pour contenir tous les éléments
        ScrollPane scroll = new ScrollPane();
        scroll.setId(DETAIL_PANEL_ID);
        scroll.getStylesheets().add("detail.css");
        scroll.setFitToWidth(true);

        // Message affiché quand aucun itinéraire n'est sélectionné
        VBox noJourney = new VBox(new Text("Aucun voyage"));
        noJourney.setId(EMPTY_JOURNEY_CONTAINER_ID);
        noJourney.setAlignment(Pos.CENTER);

        // Crée la grille pour afficher les segments de l'itinéraire
        Pane annotations = new Pane();
        annotations.setId(JOURNEY_ANNOTATIONS_LAYER_ID);
        DetailGridPane legsGrid = new DetailGridPane(annotations);
        legsGrid.setId(JOURNEY_SEGMENTS_GRID_ID);

        // Création des boutons d'export
        HBox buttons = createExportButtons(journeyO);

        // Assemblage des composants
        VBox detailBox = new VBox(GAP_SMALL, new StackPane(annotations, legsGrid), buttons);
        scroll.setContent(new StackPane(noJourney, detailBox));

        // Configure les réactions aux changements d'itinéraire
        journeyO.subscribe(journey -> {
            boolean hasJourney = journey != null;
            noJourney.setVisible(!hasJourney);
            detailBox.setVisible(hasJourney);
            legsGrid.updateLegs(journey);
        });

        return new DetailUI(scroll);
    }

    /**
     * Crée et configure les boutons pour l'export de l'itinéraire.
     *
     * @param journeyO valeur observable contenant le voyage
     * @return un HBox contenant les boutons configurés
     */
    private static HBox createExportButtons(ObservableValue<Journey> journeyO) {
        // Crée un bouton pour afficher l'itinéraire sur une carte
        Button btnMap = new Button(MAP_TAB_LABEL);
        btnMap.setId(MAP_TAB_LABEL);
        btnMap.setOnAction(e -> {
            Journey j = journeyO.getValue();
            if (j != null) {
                try {
                    // Ouvre l'itinéraire dans le navigateur avec umap.osm.ch
                    getDesktop().browse(
                            new URI("https", "umap.osm.ch", "/fr/map",
                                    "data=" + JourneyGeoJsonConverter.toGeoJson(j),
                                    null));
                } catch (Exception ignored) {}
            }
        });

        // Créer un bouton pour exporter l'itinéraire au format calendrier
        Button btnCalendar = new Button(CALENDAR_TAB_LABEL);
        btnCalendar.setId(CALENDAR_TAB_LABEL);
        btnCalendar.setOnAction(e -> {
            Journey j = journeyO.getValue();
            if (j != null) {
                try {
                    // Affiche un dialogue pour enregistrer le fichier calendrier
                    FileChooser fc = new FileChooser();
                    fc.setInitialFileName(String.format(VOYAGE_CALENDAR_FILENAME_TEMPLATE,
                            j.depTime().toLocalDate()));

                    File file = fc.showSaveDialog(null);
                    if (file != null) {
                        Files.writeString(file.toPath(), JourneyIcalConverter.toIcalendar(j));
                    }
                } catch (Exception ignored) {}
            }
        });

        // Conteneur pour les boutons
        HBox buttons = new HBox(GAP_MEDIUM, btnMap, btnCalendar);
        buttons.setId(BUTTONS_CONTAINER_ID);
        buttons.setAlignment(Pos.CENTER);

        return buttons;
    }

    /**
     * GridPane pour dessiner les points et les lignes rouges entre le départ et l'arrivée de chaque
     * étape en transport.
     */
    private static class DetailGridPane extends GridPane {
        private final List<Pair<Circle, Circle>> circlePairs = new ArrayList<>();
        private final List<Line> connectionLines = new ArrayList<>();
        private final Pane annotations;
        private Journey lastJourney;
        private boolean needsLineUpdate = true;

        // Positions des colonnes
        private static final int COL_TIME = 0;
        private static final int COL_CIRCLE = 1;
        private static final int COL_STATION = 2;
        private static final int COL_PLATFORM = 3;

        // Etendues des colonnes
        private static final int FOOT_LEG_COLSPAN = 2;
        private static final int DESTINATION_COLSPAN = 2;
        private static final int ACCORDION_COLSPAN = 2;

        // Incréments des lignes
        private static final int ROW_INCREMENT = 1;

        /**
         * Constructeur de DetailGridPane, qui configure les colonnes de ce GridPane.
         *
         * @param annotations le Pane qui doit contenir les lignes rouges qui relient les cercles.
         */
        DetailGridPane(Pane annotations) {
            this.annotations = annotations;
            setVgap(GAP_SMALL);
            setHgap(GAP_SMALL);
        }

        /**
         * Actualise les étapes présentes dans le GridPane selon le voyage donné en argument.
         *
         * @param journey voyage qui soit être affiché dans le DetailGridPane.
         */
        void updateLegs(Journey journey) {
            // Recalcule seulement si le voyage a changé
            if (!Objects.equals(journey, lastJourney)) {
                getChildren().clear();
                circlePairs.clear();
                needsLineUpdate = true;
                lastJourney = journey;

                if (journey != null) {
                    int row = 0;
                    for (Leg leg : journey.legs()) {
                        if (leg instanceof Leg.Foot foot) {
                            // Ajoute un segment de marche
                            add(new Text(FormatterFr.formatLeg(foot)), COL_STATION, row,
                                    FOOT_LEG_COLSPAN, ROW_INCREMENT);
                        } else {
                            // Ajoute un segment de tx (bus, train, etc.)
                            Leg.Transport tx = (Leg.Transport) leg;

                            // Cache les valeurs utilisées plusieurs fois pour optimiser les performances
                            List<Leg.IntermediateStop> intermediateStops = tx.intermediateStops();
                            boolean hasIntermediates = !intermediateStops.isEmpty();

                            // Crée des cercles pour représenter le départ et l'arrivée
                            Circle depCircle = new Circle(CIRCLE_RADIUS, Color.BLACK);
                            Circle arrCircle = new Circle(CIRCLE_RADIUS, Color.BLACK);
                            circlePairs.add(new Pair<>(depCircle, arrCircle));

                            // Ajoute la ligne de départ avec heure, cercle, nom de station et quai
                            addStopRow(tx.depTime(), depCircle, tx.depStop(), DEPARTURE_EVENT_TYPE, row);
                            row += ROW_INCREMENT;

                            // Ajoute l'icône du véhicule et la destination
                            ImageView icon = new ImageView(VehicleIcons.iconFor(tx.vehicle()));
                            icon.setFitWidth(ICON_SIZE);
                            icon.setFitHeight(ICON_SIZE);
                            icon.setPreserveRatio(true);

                            // Détermine la hauteur de l'icône selon la présence d'arrêts intermédiaires
                            int iconRowSpan = hasIntermediates
                                    ? ICON_ROWS_WITH_INTERMEDIATES
                                    : ICON_ROWS_WITHOUT_INTERMEDIATES;

                            // Ajuste la taille verticale de l'icône
                            add(icon, COL_TIME, row, ROW_INCREMENT, iconRowSpan);
                            add(new Text(FormatterFr.formatRouteDestination(tx)),
                                    COL_STATION, row, DESTINATION_COLSPAN, ROW_INCREMENT);
                            row += ROW_INCREMENT;

                            // Ajoute les arrêts intermédiaires si présents
                            if (hasIntermediates) {
                                row = addIntermediateStops(tx, intermediateStops, row);
                            }

                            // Ajoute la ligne d'arrivée
                            addStopRow(tx.arrTime(), arrCircle, tx.arrStop(), null, row);
                        }
                        row += ROW_INCREMENT; // Espace entre les segments
                    }
                }
            }
        }

        /**
         * Ajoute une ligne représentant un arrêt (départ ou arrivée) dans la grille.
         */
        private void addStopRow(LocalDateTime time, Circle circle, Stop stop,
                                String styleClass, int row) {
            // Ajoute l'heure formatée
            addStyledText(FormatterFr.formatTime(time), styleClass, COL_TIME, row);

            // Ajoute le cercle de visualisation
            add(circle, COL_CIRCLE, row);

            // Ajoute le nom de la station
            addStyledText(stop.name(), null, COL_STATION, row);

            // Ajoute le quai si disponible
            String platform = FormatterFr.formatPlatformName(stop);
            if (!platform.isEmpty()) {
                addStyledText(platform, styleClass, COL_PLATFORM, row);
            }
        }

        /**
         * Crée un Text avec style optionnel et l'ajoute à la grille.
         */
        private void addStyledText(String content, String styleClass, int col, int row) {
            Text text = new Text(content);
            if (styleClass != null) {
                text.getStyleClass().add(styleClass);
            }
            add(text, col, row);
        }

        /**
         * Crée et ajoute un accordéon contenant les arrêts intermédiaires
         * d'un segment de transport.
         */
        private int addIntermediateStops(Leg.Transport tx,
                                         List<Leg.IntermediateStop> intermediateStops, int row) {
            // Crée une grille pour les arrêts intermédiaires
            GridPane stopsGrid = new GridPane();
            stopsGrid.setId(INTERMEDIATE_STOPS_IDENTIFIER);
            stopsGrid.getStyleClass().add(INTERMEDIATE_STOPS_IDENTIFIER);
            stopsGrid.setHgap(GAP_SMALL);

            // Ajoute chaque arrêt intermédiaire à la grille
            for (int i = 0; i < intermediateStops.size(); i++) {
                Leg.IntermediateStop stop = intermediateStops.get(i);
                stopsGrid.add(new Text(FormatterFr.formatTime(stop.arrTime())), COL_TIME, i);
                stopsGrid.add(new Text(FormatterFr.formatTime(stop.depTime())), COL_CIRCLE, i);
                stopsGrid.add(new Text(stop.stop().name()), COL_STATION, i);
            }

            // Crée un panneau avec titre indiquant le nombre d'arrêts et la durée totale
            long totalDurationMinutes = tx.duration().toMinutes();
            String accordionTitle = String.format(STOPS_DURATION_FORMAT, intermediateStops.size(),
                    totalDurationMinutes);
            TitledPane stopsPane = new TitledPane(accordionTitle, stopsGrid);

            // Ajoute le panneau dans un accordéon
            Accordion accordion = new Accordion();
            accordion.setId("intermediate");
            accordion.getPanes().add(stopsPane);
            add(accordion, COL_STATION, row, ACCORDION_COLSPAN, ROW_INCREMENT);

            return row + ROW_INCREMENT;
        }

        /**
         * Redéfinition de layoutChildren, qui utiliser la position des cercles pour déterminer
         * celle des lignes rouges, et créer des lignes reliant les centres des cercles.
         */
        @Override
        protected void layoutChildren() {
            super.layoutChildren();

            if (needsLineUpdate) {
                connectionLines.clear();
                for (Pair<Circle, Circle> pair : circlePairs) {
                    Circle dep = pair.getKey();
                    Circle arr = pair.getValue();
                    Bounds depBounds = dep.getBoundsInParent();
                    Bounds arrBounds = arr.getBoundsInParent();

                    Line line = new Line(depBounds.getCenterX(), depBounds.getCenterY(),
                            arrBounds.getCenterX(), arrBounds.getCenterY());
                    line.setStrokeWidth(LINE_WIDTH);
                    line.setStroke(Color.RED);
                    connectionLines.add(line);
                }
                annotations.getChildren().setAll(connectionLines);
                needsLineUpdate = false;
            }
        }

    }
}
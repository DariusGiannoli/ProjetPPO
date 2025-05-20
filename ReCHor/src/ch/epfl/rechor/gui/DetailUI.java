package ch.epfl.rechor.gui;

import ch.epfl.rechor.journey.*;
import ch.epfl.rechor.journey.Journey.Leg;
import ch.epfl.rechor.FormatterFr;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.geometry.HPos;
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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import static java.awt.Desktop.getDesktop;

/**
 * DetailUI représente la partie de l'interface graphique qui montre les détails d'un voyage.
 *
 * @author Antoine Lepin (390950)
 * @author Darius Giannoli (380759)
 */
public record DetailUI(Node rootNode) {
    // Constantes pour l'interface graphique
    private static final double CIRCLE_RADIUS = 3.0;
    private static final double LINE_WIDTH = 2.0;
    private static final int ICON_SIZE = 31;
    private static final int GAP = 5;

    /**
     * Crée le graphe de scène et retourne une instance de DetailUI
     * contenant une référence à sa racine.
     *
     * @param journeyO valeur observable contenant le voyage dont les détails doivent être affichés
     *                 dans l'interface graphique.
     * @return retourne une instance de DetailUI contenant une référence à sa racine.
     */
    public static DetailUI create(ObservableValue<Journey> journeyO) {
        // ScrollPane
        ScrollPane scroll = new ScrollPane();
        scroll.setId("detail");
        scroll.getStylesheets().add("detail.css");
        scroll.setFitToWidth(true);

        // "No journey" message
        VBox noJourney = new VBox(new Text("Aucun voyage"));
        noJourney.setId("no-journey");
        noJourney.setAlignment(Pos.CENTER);

        // Grid for journey
        Pane annotations = new Pane();
        annotations.setId("annotations");
        DetailGridPane legsGrid = new DetailGridPane(annotations);
        legsGrid.setId("legs");

        // Buttons
        Button btnMap = new Button("Carte");
        btnMap.setId("Carte");
        btnMap.setOnAction(e -> {
            Journey j = journeyO.getValue();
            if (j != null) {
                try {
                    getDesktop().browse(new URI("https", "umap.osm.ch", "/fr/map",
                            "data=" + JourneyGeoJsonConverter.toGeoJson(j), null));
                } catch (Exception ignored) {}
            }
        });

        Button btnCalendar = new Button("Calendrier");
        btnCalendar.setId("Calendrier");
        btnCalendar.setOnAction(e -> {
            Journey j = journeyO.getValue();
            if (j != null) {
                try {
                    FileChooser fc = new FileChooser();
                    fc.setInitialFileName("voyage_" + j.depTime().toLocalDate() + ".ics");
                    File file = fc.showSaveDialog(null);
                    if (file != null) {
                        Files.writeString(file.toPath(), JourneyIcalConverter.toIcalendar(j));
                    }
                } catch (Exception ignored) {}
            }
        });

        HBox buttons = new HBox(10, btnMap, btnCalendar);
        buttons.setId("buttons");
        buttons.setAlignment(Pos.CENTER);

        // Assembly
        VBox detailBox = new VBox(GAP, new StackPane(annotations, legsGrid), buttons);
        scroll.setContent(new StackPane(noJourney, detailBox));

        // Bindings
        journeyO.subscribe(journey -> {
            boolean hasJourney = journey != null;
            noJourney.setVisible(!hasJourney);
            detailBox.setVisible(hasJourney);
            legsGrid.updateLegs(journey);
        });

        return new DetailUI(scroll);
    }

    /**
     * GridPane pour dessiner les points et les lignes rouges entre le départ et l'arrivée de chaque
     * étape en transport.
     */
    private static class DetailGridPane extends GridPane {
        private final List<Pair<Circle, Circle>> circlePairs = new ArrayList<>();
        private final Pane annotations;

        /**
         * Constructeur de DetailGridPane, qui configure les colonnes de ce GridPane.
         *
         * @param annotations le Pane qui doit contenir les lignes rouges qui relient les cercles.
         */
        DetailGridPane(Pane annotations) {
            this.annotations = annotations;
            setVgap(GAP);
            setHgap(GAP);

        }

        /**
         * Actualise les étapes présentes dans le GridPane selon le voyage donné en argument.
         *
         * @param journey voyage qui soit être affiché dans le DetailGridPane.
         */
        void updateLegs(Journey journey) {
            getChildren().clear();
            circlePairs.clear();
            if(journey != null) {
                int row = 0;
                for (Leg leg : journey.legs()) {
                    if (leg instanceof Leg.Foot foot) {
                        // Add foot leg
                        add(new Text(FormatterFr.formatLeg(foot)), 2, row, 2, 1);
                        row++;
                    } else {
                        // Add transport leg
                        Leg.Transport tx = (Leg.Transport) leg;

                        // Create circles for departure and arrival
                        Circle depCircle = new Circle(CIRCLE_RADIUS, Color.BLACK);
                        Circle arrCircle = new Circle(CIRCLE_RADIUS, Color.BLACK);
                        circlePairs.add(new Pair<>(depCircle, arrCircle));

                        // Add departure stop
                        Text depTimeText = new Text(FormatterFr.formatTime(tx.depTime()));
                        depTimeText.getStyleClass().add("departure");
                        add(depTimeText, 0, row);
                        add(depCircle, 1, row);

                        // Station name in column 2
                        Text depStationText = new Text(tx.depStop().name());
                        depStationText.getStyleClass().add("departure");
                        add(depStationText, 2, row);

                        // Platform in column 3
                        String depPlatform = FormatterFr.formatPlatformName(tx.depStop());
                        if (!depPlatform.isEmpty()) {
                            Text depPlatformText = new Text(depPlatform);
                            depPlatformText.getStyleClass().add("departure");
                            add(depPlatformText, 3, row);
                        }
                        row++;

                        // Add vehicle icon and destination
                        ImageView icon = new ImageView(VehicleIcons.iconFor(tx.vehicle()));
                        icon.setFitWidth(ICON_SIZE);
                        icon.setFitHeight(ICON_SIZE);
                        icon.setPreserveRatio(true);

                        boolean hasIntermediates = !tx.intermediateStops().isEmpty();
                        add(icon, 0, row, 1, hasIntermediates ? 2 : 1);
                        add(new Text(FormatterFr.formatRouteDestination(tx)), 2, row, 2, 1);
                        row++;

                        // Add intermediate stops if any
                        if (hasIntermediates) {
                            // Build the grid for intermediate stops
                            GridPane stopsGrid = new GridPane();
                            stopsGrid.setId("intermediate-stops");
                            stopsGrid.getStyleClass().add("intermediate-stops");
                            stopsGrid.setHgap(GAP);

                            IntStream.range(0, tx.intermediateStops().size()).forEach(i -> {
                                Leg.IntermediateStop stop = tx.intermediateStops().get(i);
                                stopsGrid.add(new Text(FormatterFr.formatTime(stop.arrTime())), 0, i);
                                stopsGrid.add(new Text(FormatterFr.formatTime(stop.depTime())), 1, i);
                                stopsGrid.add(new Text(stop.stop().name()), 2, i);
                            });

                            TitledPane stopsPane = new TitledPane(
                                    String.format("%d arrêts, %d min",
                                            tx.intermediateStops().size(), tx.duration().toMinutes()),
                                    stopsGrid);

                            Accordion accordion = new Accordion();
                            accordion.setId("intermediate");
                            accordion.getPanes().add(stopsPane);
                            add(accordion, 2, row, 2, 1);
                            row++;
                        }

                        // Add arrival stop
                        add(new Text(FormatterFr.formatTime(tx.arrTime())), 0, row);
                        add(arrCircle, 1, row);
                        add(new Text(tx.arrStop().name()), 2, row);

                        // Platform in column 3
                        String arrPlatform = FormatterFr.formatPlatformName(tx.arrStop());
                        if (!arrPlatform.isEmpty()) {
                            add(new Text(arrPlatform), 3, row);
                        }
                        row++;
                    }
                }
            }
        }

        /**
         * Redéfinition de layoutChildren, qui utiliser la position des cercles pour déterminer
         * celle des lignes rouges, et créer des lignes reliant les centres des cercles.
         */
        @Override
        protected void layoutChildren() {
            super.layoutChildren();

            // Create connecting lines between circle pairs
            List<Line> lines = circlePairs.stream().map(pair -> {
                Circle dep = pair.getKey();
                Circle arr = pair.getValue();
                Bounds depBounds = dep.getBoundsInParent();
                Bounds arrBounds = arr.getBoundsInParent();

                Line line = new Line(
                        depBounds.getCenterX(), depBounds.getCenterY(),
                        arrBounds.getCenterX(), arrBounds.getCenterY());
                line.setStrokeWidth(LINE_WIDTH);
                line.setStroke(Color.RED);
                return line;
            }).toList();

            annotations.getChildren().setAll(lines);
            annotations.getParent().requestLayout();
        }
    }
}
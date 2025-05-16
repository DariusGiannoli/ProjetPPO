package ch.epfl.rechor.gui;

import ch.epfl.rechor.journey.*;
import ch.epfl.rechor.journey.Journey.Leg;
import ch.epfl.rechor.journey.Journey.Leg.Foot;
import ch.epfl.rechor.journey.Journey.Leg.Transport;
import ch.epfl.rechor.FormatterFr;
import javafx.beans.value.ObservableValue;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static java.awt.Desktop.getDesktop;

/**
 * Represents the detailed view of a journey.
 *
 * @author Antoine Lepin (390950)
 * @author Darius Giannoli (380759)
 */
public record DetailUI(Node rootNode) {
    private static final double CIRCLE_RADIUS = 3.0;
    private static final double LINE_WIDTH = 2.0;
    private static final int ICON_SIZE = 31;

    private static final int COL_TIME     = 0;
    private static final int COL_CIRCLE   = 1;
    private static final int COL_STATION  = 2;
    private static final int COL_PLATFORM = 3;

    private static Button makeButton(String text, String id) {
        Button b = new Button(text);
        b.setId(id);
        return b;
    }

    // 1) Helper générique, static et private
    private static <T extends Node> T with(T node, Consumer<T> cfg) {
        cfg.accept(node);
        return node;
    }

    /**
     * Creates the scene graph and returns a DetailUI instance with a reference to its root.
     *
     * @param journeyO Observable value containing the journey to display.
     * @return A DetailUI instance.
     */
    public static DetailUI create(ObservableValue<Journey> journeyO) {
        // 1) ScrollPane
        var scroll = with(new ScrollPane(), s -> {
            s.setId("detail");
            s.getStylesheets().add("detail.css");
            s.setFitToWidth(true);
        });

        // 2) “Aucun voyage”
        var noJourney = with(new VBox(new Text("Aucun voyage")), v -> {
            v.setId("no-journey");
            v.setFillWidth(true);
            v.setAlignment(Pos.CENTER);
        });

        // 3) Annotations + grille
        var annotations = with(new Pane(),   p -> p.setId("annotations"));
        var legsGrid    = with(new DetailGridPane(annotations), dg -> dg.setId("legs"));
        var stepsPane   = new StackPane(annotations, legsGrid);

        // 4) Boutons
        var btnMap      = makeButton("Carte",      "Carte");
        var btnCalendar = makeButton("Calendrier", "Calendrier");
        var buttons = with(new HBox(10, btnMap, btnCalendar), hb -> {
            hb.setId("buttons");
            hb.setAlignment(Pos.CENTER);
        });

        // 5) Assemblage
        var detailBox = new VBox(5, stepsPane, buttons);
        var rootStack = new StackPane(noJourney, detailBox);
        scroll.setContent(rootStack);

        var ui = new DetailUI(scroll);

        // 6) Binding sur le voyage
        Runnable update = () -> {
            Journey j = journeyO.getValue();
            noJourney.setVisible(j == null);
            detailBox.setVisible(j != null);
            legsGrid.updateLegs(j);
        };
        journeyO.addListener((obs, o, n) -> update.run());
        update.run();

        // 7) Actions
        btnMap.setOnAction(e -> openMap(journeyO.getValue()));
        btnCalendar.setOnAction(e -> exportCalendar(journeyO.getValue()));

        return ui;
    }

    private static void openMap(Journey journey) {
        if (journey == null) return;
        try {
            String geo = JourneyGeoJsonConverter.toGeoJson(journey);
            URI uri = new URI("https", "umap.osm.ch", "/fr/map", "data=" + geo, null);
            getDesktop().browse(uri);
        } catch (Exception ignored) {
            // Ignore exceptions as per original behavior
        }
    }

    private static void exportCalendar(Journey journey) {
        if (journey == null) return;
        try {
            FileChooser fc = new FileChooser();
            LocalDate date = journey.depTime().toLocalDate();
            fc.setInitialFileName("voyage_" + date + ".ics");
            File file = fc.showSaveDialog(null);
            if (file != null) {
                String ical = JourneyIcalConverter.toIcalendar(journey);
                Files.writeString(file.toPath(), ical);
            }
        } catch (Exception ignored) {
            // Ignore exceptions as per original behavior
        }
    }

    /**
     * GridPane that draws circles and red lines between journey legs.
     */
    private static class DetailGridPane extends GridPane {
        private final List<Pair<Circle, Circle>> circlePairs = new ArrayList<>();
        private final Pane annotations;

        DetailGridPane(Pane annotations) {
            this.annotations = annotations;
            configureColumns();
            setVgap(5);
            setHgap(5);
        }

        private void configureColumns() {
            ColumnConstraints col0 = new ColumnConstraints();
            col0.setHalignment(HPos.RIGHT);
            ColumnConstraints col1 = new ColumnConstraints(10);
            col1.setHalignment(HPos.CENTER);
            ColumnConstraints col23 = new ColumnConstraints();
            col23.setHgrow(Priority.ALWAYS);
            getColumnConstraints().addAll(col0, col1, col23, col23);
        }

        void updateLegs(Journey journey) {
            getChildren().clear();
            circlePairs.clear();
            if (journey == null) return;

            int row = 0;
            for (Leg leg : journey.legs()) {
                row = leg instanceof Foot foot
                        ? addFootLeg(foot, row)
                        : addTransportLeg((Transport) leg, row);
            }
        }

        private int addFootLeg(Foot foot, int row) {
            Text text = new Text(FormatterFr.formatLeg(foot));
            add(text, 2, row, 2, 1);
            return row + 1;
        }

        private Circle createCircle() {
            return new Circle(CIRCLE_RADIUS, Color.BLACK);
        }


        private int addStopRow(Transport tx,
                               boolean isDeparture,
                               Circle circle,
                               int row)
        {
            // pick the right time and stop
            LocalDateTime time = isDeparture ? tx.depTime() : tx.arrTime();
            Stop         stop = isDeparture ? tx.depStop() : tx.arrStop();

            // time cell
            Text timeText = new Text(FormatterFr.formatTime(time));
            if (isDeparture) timeText.getStyleClass().add("departure");
            add(timeText, COL_TIME, row);

            // circle cell
            add(circle, COL_CIRCLE, row);

            // station name
            add(new Text(stop.name()), COL_STATION, row);

            // optional platform
            String platform = FormatterFr.formatPlatformName(stop);
            if (!platform.isEmpty()) {
                Text p = new Text(platform);
                if (isDeparture) p.getStyleClass().add("departure");
                add(p, COL_PLATFORM, row);
            }

            return row + 1;
        }

        private int addTransportLeg(Transport tx, int row) {
            // departure
            Circle depCircle = createCircle();
            row = addStopRow(tx, true,  depCircle, row);

            // icon + destination
            ImageView icon = createVehicleIcon(tx.vehicle());
            add(icon, 0, row, 1, tx.intermediateStops().isEmpty() ? 1 : 2);
            add(new Text(FormatterFr.formatRouteDestination(tx)), 2, row, 2, 1);
            row++;

            // intermediates
            row = addIntermediateStops(tx, row);

            // arrival
            Circle arrCircle = createCircle();
            row = addStopRow(tx, false, arrCircle, row);

            circlePairs.add(new Pair<>(depCircle, arrCircle));
            return row;
        }


        private int addIntermediateStops(Transport tx, int row) {
            if (tx.intermediateStops().isEmpty()) return row;

            int stopCount = tx.intermediateStops().size();
            long duration = tx.duration().toMinutes();
            Accordion accordion = new Accordion();
            accordion.setId("intermediate");
            TitledPane pane = new TitledPane(
                    stopCount + " arrêts, " + duration + " min",
                    buildIntermediateGrid(tx.intermediateStops())
            );
            accordion.getPanes().add(pane);
            add(accordion, 2, row, 2, 1);
            return row + 1;
        }

        private ImageView createVehicleIcon(Vehicle v) {
            // 1) délégation du load à VehicleIcons
            Image image = VehicleIcons.iconFor(v);

            // 2) création + sizing en un point unique
            ImageView iv = new ImageView(image);
            iv.setFitWidth(ICON_SIZE);
            iv.setFitHeight(ICON_SIZE);
            iv.setPreserveRatio(true);
            iv.setSmooth(true);
            return iv;
        }

        private GridPane buildIntermediateGrid(List<Leg.IntermediateStop> stops) {
            GridPane grid = new GridPane();
            grid.setId("intermediate-stops");
            grid.getStyleClass().add("intermediate-stops");
            grid.setHgap(5);

            int row = 0;
            for (var stop : stops) {
                grid.add(new Text(FormatterFr.formatTime(stop.arrTime())), 0, row);
                grid.add(new Text(FormatterFr.formatTime(stop.depTime())), 1, row);
                grid.add(new Text(stop.stop().name()), 2, row);
                row++;
            }
            return grid;
        }

        @Override
        protected void layoutChildren() {
            super.layoutChildren();
            List<Line> lines = circlePairs.stream().map(p -> {
                Circle dep = p.getKey();
                Circle arr = p.getValue();
                var depBounds = dep.getBoundsInParent();
                var arrBounds = arr.getBoundsInParent();

                Line line = new Line(
                        depBounds.getCenterX(), depBounds.getMinY() + depBounds.getHeight() / 2,
                        arrBounds.getCenterX(), arrBounds.getMinY() + arrBounds.getHeight() / 2
                );
                line.setStrokeWidth(LINE_WIDTH);
                line.setStroke(Color.RED);
                return line;
            }).toList();

            annotations.getChildren().setAll(lines);
            annotations.getParent().requestLayout();
        }
    }
}
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
 * Interface graphique pour afficher les détails d'un voyage.
 */
public record DetailUI(Node rootNode) {
    private static final double CIRCLE_RADIUS = 3.0;
    private static final double LINE_WIDTH = 2.0;
    private static final int ICON_SIZE = 31;

    private static final int COL_TIME = 0;
    private static final int COL_CIRCLE = 1;
    private static final int COL_STATION = 2;
    private static final int COL_PLATFORM = 3;

    private static final int GAP = 5;



    // Helper pour configurer un nœud
    private static <T> T with(T obj, Consumer<T> cfg) {
        cfg.accept(obj);
        return obj;
    }

    // Crée un bouton avec texte et ID
    private static Button makeButton(String text, String id) {
        return with(new Button(text), b -> b.setId(id));
    }

    /**
     * Crée l'interface graphique pour afficher les détails d'un voyage.
     *
     * @param journeyO Observable contenant le voyage à afficher.
     * @return Instance de DetailUI.
     */
    public static DetailUI create(ObservableValue<Journey> journeyO) {
        // ScrollPane principal
        var scroll = with(new ScrollPane(), s -> {
            s.setId("detail");
            s.getStylesheets().add("detail.css");
            s.setFitToWidth(true);
        });

        // Message "Aucun voyage"
        var noJourney = with(new VBox(new Text("Aucun voyage")), v -> {
            v.setId("no-journey");
            v.setAlignment(Pos.CENTER);
        });

        // Grille et annotations
        var annotations = with(new Pane(), p -> p.setId("annotations"));
        var legsGrid = with(new DetailGridPane(annotations), g -> g.setId("legs"));
        var stepsPane = new StackPane(annotations, legsGrid);

        // Boutons
        var buttons = with(new HBox(10,
                makeButton("Carte", "Carte"),
                makeButton("Calendrier", "Calendrier")
        ), hb -> {
            hb.setId("buttons");
            hb.setAlignment(Pos.CENTER);
        });

        // Assemblage
        var detailBox = new VBox(5, stepsPane, buttons);
        var rootStack = new StackPane(noJourney, detailBox);
        scroll.setContent(rootStack);

        var ui = new DetailUI(scroll);

        // Mise à jour selon le voyage
        Runnable update = () -> {
            Journey j = journeyO.getValue();
            noJourney.setVisible(j == null);
            detailBox.setVisible(j != null);
            legsGrid.updateLegs(j);
        };
        journeyO.subscribe(n -> update.run());
        update.run();

        // Actions des boutons
        buttons.getChildren().forEach(node -> {
            Button btn = (Button) node; // Cast en Button
            if (btn.getId().equals("Carte")) {
                btn.setOnAction(e -> openMap(journeyO.getValue()));
            } else {
                btn.setOnAction(e -> exportCalendar(journeyO.getValue()));
            }
        });

        return ui;
    }

    // Ouvre la carte dans le navigateur
    private static void openMap(Journey journey) {
        if (journey == null) return;
        try {
            String geo = JourneyGeoJsonConverter.toGeoJson(journey);
            URI uri = new URI("https", "umap.osm.ch", "/fr/map", "data=" + geo, null);
            getDesktop().browse(uri);
        } catch (Exception ignored) {
        }
    }

    // Exporte le voyage en iCalendar
    private static void exportCalendar(Journey journey) {
        if (journey == null) return;
        try {
            FileChooser fc = new FileChooser();
            fc.setInitialFileName("voyage_" + journey.depTime().toLocalDate() + ".ics");
            File file = fc.showSaveDialog(null);
            if (file != null) {
                Files.writeString(file.toPath(), JourneyIcalConverter.toIcalendar(journey));
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * Grille pour afficher les étapes du voyage avec cercles et lignes.
     */
    private static class DetailGridPane extends GridPane {
        private final List<Pair<Circle, Circle>> circlePairs = new ArrayList<>();
        private final Pane annotations;

        DetailGridPane(Pane annotations) {
            this.annotations = annotations;
            setVgap(GAP);
            setHgap(GAP);
            configureColumns();
        }

        private void configureColumns() {
            getColumnConstraints().addAll(
                    with(new ColumnConstraints(), c -> c.setHalignment(HPos.RIGHT)),
                    with(new ColumnConstraints(10), c -> c.setHalignment(HPos.CENTER)),
                    with(new ColumnConstraints(), c -> c.setHgrow(Priority.ALWAYS)),
                    with(new ColumnConstraints(), c -> c.setHgrow(Priority.ALWAYS))
            );
        }

        void updateLegs(Journey journey) {
            getChildren().clear();
            circlePairs.clear();
            if (journey == null) return;

            int row = 0;
            for (Leg leg : journey.legs()) {
                row = leg instanceof Foot foot ? addFootLeg(foot, row) : addTransportLeg((Transport) leg, row);
            }
        }

        private int addFootLeg(Foot foot, int row) {
            add(new Text(FormatterFr.formatLeg(foot)), COL_STATION, row, 2, 1);
            return row + 1;
        }

        private Circle createCircle() {
            return new Circle(CIRCLE_RADIUS, Color.BLACK);
        }

        private int addStopRow(Transport tx, boolean isDeparture, Circle circle, int row) {
            LocalDateTime time = isDeparture ? tx.depTime() : tx.arrTime();
            Stop stop = isDeparture ? tx.depStop() : tx.arrStop();

            Text timeText = with(new Text(FormatterFr.formatTime(time)),
                    t -> {
                        if (isDeparture) t.getStyleClass().add("departure");
                    });
            add(timeText, COL_TIME, row);
            add(circle, COL_CIRCLE, row);
            add(new Text(stop.name()), COL_STATION, row);

            String platform = FormatterFr.formatPlatformName(stop);
            if (!platform.isEmpty()) {
                Text p = with(new Text(platform),
                        t -> {
                            if (isDeparture) t.getStyleClass().add("departure");
                        });
                add(p, COL_PLATFORM, row);
            }

            return row + 1;
        }

        private int addTransportLeg(Transport tx, int row) {
            // Départ
            Circle depCircle = createCircle();
            row = addStopRow(tx, true, depCircle, row);

            // Icône et destination
            ImageView icon = createVehicleIcon(tx.vehicle());
            add(icon, COL_TIME, row, 1, tx.intermediateStops().isEmpty() ? 1 : 2);
            add(new Text(FormatterFr.formatRouteDestination(tx)), COL_STATION, row, 2, 1);
            row++;

            // Arrêts intermédiaires
            row = addIntermediateStops(tx, row);

            // Arrivée
            Circle arrCircle = createCircle();
            row = addStopRow(tx, false, arrCircle, row);

            circlePairs.add(new Pair<>(depCircle, arrCircle));
            return row;
        }

        private int addIntermediateStops(Transport tx, int row) {
            if (tx.intermediateStops().isEmpty()) return row;

            var accordion = with(new Accordion(), a -> a.setId("intermediate"));
            accordion.getPanes().add(new TitledPane(
                    tx.intermediateStops().size() + " arrêts, " + tx.duration().toMinutes() + " min",
                    buildIntermediateGrid(tx.intermediateStops())
            ));
            add(accordion, COL_STATION, row, 2, 1);
            return row + 1;
        }

        private ImageView createVehicleIcon(Vehicle v) {
            return with(new ImageView(VehicleIcons.iconFor(v)), iv -> {
                iv.setFitWidth(ICON_SIZE);
                iv.setFitHeight(ICON_SIZE);
                iv.setPreserveRatio(true);
                iv.setSmooth(true);
            });
        }

        private GridPane buildIntermediateGrid(List<Leg.IntermediateStop> stops) {
            var grid = with(new GridPane(), g -> {
                g.setId("intermediate-stops");
                g.getStyleClass().add("intermediate-stops");
                g.setHgap(5);
            });

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
            var lines = circlePairs.stream().map(p -> {
                var dep = p.getKey().getBoundsInParent();
                var arr = p.getValue().getBoundsInParent();
                return with(new Line(
                        dep.getCenterX(), dep.getCenterY(),
                        arr.getCenterX(), arr.getCenterY()
                ), line -> {
                    line.setStrokeWidth(LINE_WIDTH);
                    line.setStroke(Color.RED);
                });
            }).toList();

            annotations.getChildren().setAll(lines);
            annotations.getParent().requestLayout();
        }
    }
}
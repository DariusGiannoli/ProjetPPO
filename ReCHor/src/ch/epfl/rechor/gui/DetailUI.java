package ch.epfl.rechor.gui;

import ch.epfl.rechor.journey.Journey;
import ch.epfl.rechor.journey.Journey.Leg;
import ch.epfl.rechor.journey.Journey.Leg.Foot;
import ch.epfl.rechor.journey.Journey.Leg.Transport;
import ch.epfl.rechor.FormatterFr;
import ch.epfl.rechor.journey.JourneyGeoJsonConverter;
import ch.epfl.rechor.journey.JourneyIcalConverter;
import javafx.beans.value.ObservableValue;
import javafx.geometry.HPos;
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

import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 3.3 Enregistrement DetailUI – représente la vue détaillée d’un voyage.
 *
 */
public record DetailUI(Node rootNode) {

    public static DetailUI create(ObservableValue<Journey> journeyO) {
        // 1) racine scrollable
        ScrollPane scroll = new ScrollPane();
        scroll.setId("detail");
        scroll.getStylesheets().add("detail.css");
        scroll.setFitToWidth(true);

        // 2) aucun voyage sélectionné
        VBox noJourney = new VBox();
        noJourney.setId("no-journey");
        noJourney.setFillWidth(true);
        noJourney.setAlignment(javafx.geometry.Pos.CENTER);
        noJourney.getChildren().add(new Text("Aucun voyage"));

        // 3) zone d'annotations (cercles + lignes)
        Pane annotations = new Pane();
        annotations.setId("annotations");

        // 4) grille des étapes
        DetailGridPane legsGrid = new DetailGridPane();
        legsGrid.setId("legs");

        // On empile d'abord `annotations` puis `legsGrid` (le dernier enfant est au sommet)
        StackPane stepsPane = new StackPane(annotations, legsGrid);


        // 5) boutons Carte / Calendrier
        Button btnMap      = new Button("Carte");      btnMap.setId("Carte");
        Button btnCalendar = new Button("Calendrier"); btnCalendar.setId("Calendrier");
        HBox buttons = new HBox(10, btnMap, btnCalendar);
        buttons.setId("buttons");
        buttons.setAlignment(javafx.geometry.Pos.CENTER);

        // 6) détail (grille + boutons)
        VBox detailBox = new VBox(5, stepsPane, buttons);
        detailBox.setVisible(false);

        // 7) superposition "aucun voyage" / détail
        StackPane rootStack = new StackPane(noJourney, detailBox);
        scroll.setContent(rootStack);

        DetailUI ui = new DetailUI(scroll);

        // 8) mise à jour lors du changement de Journey
        Runnable update = () -> {
            Journey j = journeyO.getValue();
            if (j == null) {
                noJourney.setVisible(true);
                detailBox.setVisible(false);
            } else {
                noJourney.setVisible(false);
                detailBox.setVisible(true);
                legsGrid.updateLegs(j);
            }
        };
        journeyO.addListener((obs, o, n) -> update.run());
        update.run();

        // 9) actions des boutons
        btnMap.setOnAction(e -> {
            try {
                String geo = JourneyGeoJsonConverter.toGeoJson(journeyO.getValue());
                URI uri = new URI("https", "umap.osm.ch", "/fr/map", "data=" + geo, null);
                Desktop.getDesktop().browse(uri);
            } catch (Exception ex) { /* ignorer */ }
        });
        btnCalendar.setOnAction(e -> {
            try {
                FileChooser fc = new FileChooser();
                LocalDate d = journeyO.getValue().depTime().toLocalDate();
                fc.setInitialFileName("voyage_" + d + ".ics");
                File f = fc.showSaveDialog(null);
                if (f != null) {
                    String ical = JourneyIcalConverter.toIcalendar(journeyO.getValue());
                    Files.writeString(f.toPath(), ical);
                }
            } catch (Exception ex) { /* ignorer */ }
        });

        return ui;
    }

    /** GridPane qui dessine cercles et lignes rouges entre étapes. */
    private static class DetailGridPane extends GridPane {
        private final List<Pair<Circle,Circle>> circlePairs = new ArrayList<>();

        DetailGridPane() {
            ColumnConstraints c0 = new ColumnConstraints(); c0.setHalignment(HPos.RIGHT);
            ColumnConstraints c1 = new ColumnConstraints(10);  c1.setHalignment(HPos.CENTER);
            ColumnConstraints c23 = new ColumnConstraints();   c23.setHgrow(Priority.ALWAYS);
            getColumnConstraints().addAll(c0, c1, c23, c23);
            setVgap(5); setHgap(5);
        }

        void updateLegs(Journey journey) {
            getChildren().clear();
            circlePairs.clear();
            int row = 0;
            for (Leg leg : journey.legs()) {
                if (leg instanceof Foot foot) {
                    Text t = new Text(FormatterFr.formatLeg(foot));
                    add(t, 2, row, 2, 1);
                    row++;
                } else if (leg instanceof Transport tx) {
                    // départ
                    Text depTime = new Text(FormatterFr.formatTime(tx.depTime()));
                    depTime.getStyleClass().add("departure");
                    add(depTime, 0, row);

                    Circle cDep = new Circle(3, Color.BLACK);
                    add(cDep, 1, row);

                    add(new Text(tx.depStop().name()), 2, row);

                    String dp = FormatterFr.formatPlatformName(tx.depStop());
                    if (!dp.isEmpty()) {
                        Text platDep = new Text(dp);
                        platDep.getStyleClass().add("departure");
                        add(platDep, 3, row);
                    }
                    row++;

                    // icône + destination
                    Image img = new Image(tx.vehicle().name() + ".png");
                    ImageView icon = new ImageView(img);
                    icon.setFitWidth(31);
                    icon.setFitHeight(31);
                    icon.setPreserveRatio(true);
                    icon.setSmooth(true);
                    add(icon, 0, row, 1,
                            tx.intermediateStops().isEmpty() ? 1 : 2
                    );

                    Text routeDest = new Text(FormatterFr.formatRouteDestination(tx));
                    add(routeDest, 2, row, 2, 1);
                    row++;

                    // arrêts intermédiaires
                    if (!tx.intermediateStops().isEmpty()) {
                        int n   = tx.intermediateStops().size();
                        long dur = tx.duration().toMinutes();
                        Accordion acc = new Accordion();
                        acc.setId("intermediate");
                        TitledPane tp = new TitledPane(
                                n + " arrêts, " + dur + " min",
                                buildIntermediateGrid(tx.intermediateStops())
                        );
                        acc.getPanes().add(tp);
                        add(acc, 2, row, 2, 1);
                        row++;
                    }

                    // arrivée
                    Text arrTime = new Text(FormatterFr.formatTime(tx.arrTime()));
                    add(arrTime, 0, row);

                    Circle cArr = new Circle(3, Color.BLACK);
                    add(cArr, 1, row);

                    add(new Text(tx.arrStop().name()), 2, row);

                    String ap = FormatterFr.formatPlatformName(tx.arrStop());
                    if (!ap.isEmpty()) {
                        add(new Text(ap), 3, row);
                    }

                    circlePairs.add(new Pair<>(cDep, cArr));
                    row++;
                }
            }
        }

        private GridPane buildIntermediateGrid(
                List<Leg.IntermediateStop> stops
        ) {
            GridPane g = new GridPane();
            g.setId("intermediate-stops");
            g.getStyleClass().add("intermediate-stops");
            g.setHgap(5);
            int r = 0;
            for (var s : stops) {
                g.add(new Text(FormatterFr.formatTime(s.arrTime())), 0, r);
                g.add(new Text(FormatterFr.formatTime(s.depTime())), 1, r);
                g.add(new Text(s.stop().name()), 2, r);
                r++;
            }
            return g;
        }

        @Override
        protected void layoutChildren() {
            super.layoutChildren();
            Pane annot = (Pane)((StackPane)getParent()).getChildren().get(0);
            annot.getChildren().clear();
            for (var p : circlePairs) {
                Circle d = p.getKey(), a = p.getValue();
                var db = d.localToScene(d.getBoundsInLocal());
                var ab = a.localToScene(a.getBoundsInLocal());
                Line l = new Line(
                        db.getMinX()+db.getWidth()/2, db.getMinY()+db.getHeight()/2,
                        ab.getMinX()+ab.getWidth()/2, ab.getMinY()+ab.getHeight()/2
                );
                l.setStrokeWidth(2);
                l.setStroke(Color.RED);
                annot.getChildren().add(l);
            }
        }
    }
}

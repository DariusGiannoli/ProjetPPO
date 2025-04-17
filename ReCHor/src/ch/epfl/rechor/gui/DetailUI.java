//package gui;
//
//import ch.epfl.rechor.FormatterFr;
//import ch.epfl.rechor.journey.Journey;
//import javafx.beans.value.ObservableValue;
//import javafx.geometry.HPos;
//import javafx.geometry.VPos;
//import javafx.scene.Node;
//import javafx.scene.control.*;
//import javafx.scene.layout.*;
//import javafx.scene.shape.Circle;
//import javafx.scene.text.Text;
//import javafx.scene.image.ImageView;
//import javafx.scene.image.Image;
//import javafx.scene.paint.Color;
//import javafx.scene.shape.Line;
//import javafx.stage.FileChooser;
//import javafx.stage.Stage;
//import java.io.IOException;
//import java.net.URI;
//import java.awt.Desktop;
//import java.nio.file.Files;
//import java.nio.file.Path;
//
///**
// * UI détaillée d'un Journey.
// * rootNode contient le ScrollPane + StackPane gérant vu "aucun voyage" vs. détails.
// */
//public record DetailUI(Node rootNode) {
//
//    /**
//     * Crée l'interface des détails d'un voyage, et s'abonne à journeyObs.
//     * @param journeyObs ObservableValue<Journey> dont on affiche la valeur.
//     */
//    public static DetailUI create(ObservableValue<Journey> journeyObs) {
//        // 1) ScrollPane racine + CSS + id
//        ScrollPane scroll = new ScrollPane();
//        scroll.getStyleClass().add("detail");            // détail.css attachée via module-info
//        scroll.getStylesheets().add("detail.css");        // §3.3.1 :contentReference[oaicite:0]{index=0}&#8203;:contentReference[oaicite:1]{index=1}
//        scroll.setId("detail");
//
//        // 2) StackPane pour superposer : pas de voyage vs. détails
//        StackPane stack = new StackPane();
//        scroll.setContent(stack);
//
//        // 2.a) vue "aucun voyage"
//        VBox noJourney = new VBox(new Text("Aucun voyage"));
//        noJourney.setId("no-journey");                   // §3.3.1 :contentReference[oaicite:2]{index=2}&#8203;:contentReference[oaicite:3]{index=3}
//
//        // 2.b) conteneur des détails (sera rempli dynamiquement)
//        VBox detailBox = new VBox();
//        detailBox.setId("journey-details");
//
//        // ajouter dans l'ordre : détails dessous, puis "aucun voyage" au-dessus (visible par défaut)
//        stack.getChildren().addAll(detailBox, noJourney);
//
//        // 3) Listener sur la valeur observable
//        journeyObs.addListener((obs, oldJ, newJ) -> {
//            boolean has = newJ != null;
//            detailBox.getChildren().clear();
//            noJourney.setVisible(!has);
//            if (has) {
//                // 3.a) GridPane des étapes
//                GridPane grid = buildStepsGrid(newJ);
//                // 3.b) annotations (cercles + lignes)
//                Pane annotations = buildAnnotations(grid);
//                // 3.c) boutons bas
//                HBox buttons = buildButtons(newJ);
//
//                StackPane stepsWithAnno = new StackPane(grid, annotations);
//                detailBox.getChildren().addAll(stepsWithAnno, buttons);
//            }
//        });
//
//        // déclencher une première évaluation
//        if (journeyObs.getValue() != null) {
//            journeyObs.getValue(); // listener se lancera automatiquement
//        }
//
//        return new DetailUI(scroll);
//    }
//
//    // Construit le GridPane selon §3.3.2 (colonnes 0–3, alignements, CSS, ids…)
//    private static GridPane buildStepsGrid(Journey j) {
//        GridPane grid = new GridPane();
//        // configuration des colonnes, alignements par défaut...
//        for (int c = 0; c < 4; c++) {
//            ColumnConstraints cc = new ColumnConstraints();
//            if (c == 0) cc.setHalignment(HPos.RIGHT);
//            else if (c == 2) cc.setHalignment(HPos.LEFT);
//            grid.getColumnConstraints().add(cc);
//        }
//        // parcourt chaque étape du journey
//        int row = 0;
//        for (Journey.Leg leg : j.legs()) {
//            if (leg.isFoot()) {
//                // 1 ligne, texte formaté par FormatterFr
//                Text txt = new Text(FormatterFr.formatLeg(leg));
//                grid.add(txt, 2, row, 2, 1);
//                row++;
//            } else {
//                // 3 ou 4 lignes pour véhicule
//                // ligne 0 : heure départ, cercle, gare, quai
//                Text depTime = new Text(leg.departureTime().toString());
//                depTime.getStyleClass().add("departure");
//                Circle cDep = new Circle(3, Color.BLACK);
//                Text depStation = new Text(leg.from().name());
//                Text depTrack = new Text(leg.departureTrack());
//                depTrack.getStyleClass().add("departure");
//                grid.addRow(row, depTime, cDep, depStation, depTrack);
//
//                // ligne 1 : icône + ligne/destination
//                Image icon = VehicleIcons.iconFor(leg.vehicle());
//                ImageView iv = new ImageView(icon);
//                iv.setFitWidth(31);
//                iv.setFitHeight(31);
//                Text routeDest = new Text(FormatterFr.formatRouteDestination(leg));
//                grid.add(iv, 0, row+1);
//                grid.add(routeDest, 2, row+1, 2, 1);
//                GridPane.setValignment(iv, VPos.CENTER);
//
//                int startRow = row;
//                int nextRow = row + 2;
//
//                // ligne 2 (optionnelle) : arrêts intermédiaires
//                if (!leg.stops().isEmpty()) {
//                    Accordion acc = new Accordion();
//                    int count = leg.stops().size();
//                    String title = count + " arrêts, " + leg.duration().toMinutes() + " min";
//                    VBox stopsBox = new VBox();
//                    for (Stop s : leg.stops()) {
//                        HBox h = new HBox(
//                                new Text(s.arrival().toString()),
//                                new Text(s.departure().toString()),
//                                new Text(s.station().name())
//                        );
//                        stopsBox.getChildren().add(h);
//                    }
//                    TitledPane tp = new TitledPane(title, stopsBox);
//                    acc.getPanes().add(tp);
//                    acc.setExpanded(false);
//                    grid.add(acc, 2, nextRow, 2, 1);
//                    nextRow++;
//                }
//
//                // dernière ligne : heure arrivée, cercle, gare, quai
//                Text arrTime = new Text(leg.arrivalTime().toString());
//                Circle cArr = new Circle(3, Color.BLACK);
//                Text arrStation = new Text(leg.to().name());
//                Text arrTrack = new Text(leg.arrivalTrack());
//                grid.addRow(nextRow, arrTime, cArr, arrStation, arrTrack);
//
//                row = nextRow + 1;
//            }
//        }
//
//        grid.getStyleClass().add("legs");               // §3.3.1 :contentReference[oaicite:4]{index=4}&#8203;:contentReference[oaicite:5]{index=5}
//        grid.setId("legs");
//        return grid;
//    }
//
//    // Crée les lignes reliant les cercles après layout (§3.3.4.2)
//    private static Pane buildAnnotations(GridPane grid) {
//        return new Pane() {
//            @Override
//            protected void layoutChildren() {
//                super.layoutChildren();
//                getChildren().clear();
//                // rechercher tous les cercles de départ+arrivée
//                List<Circle> circles = grid.lookupAll(".circle")
//                        .stream()
//                        .map(n -> (Circle)n)
//                        .toList();
//                // relier par paires successives
//                for (int i = 0; i + 1 < circles.size(); i += 2) {
//                    Circle a = circles.get(i), b = circles.get(i+1);
//                    Line line = new Line();
//                    var pa = a.getBoundsInParent();
//                    var pb = b.getBoundsInParent();
//                    line.setStartX(pa.getMinX() + pa.getWidth()/2);
//                    line.setStartY(pa.getMinY() + pa.getHeight()/2);
//                    line.setEndX(pb.getMinX() + pb.getWidth()/2);
//                    line.setEndY(pb.getMinY() + pb.getHeight()/2);
//                    line.setStrokeWidth(2);
//                    line.setStroke(Color.RED);
//                    getChildren().add(line);
//                }
//            }
//        };
//    }
//
//    // HBox avec boutons « Carte » et « Calendrier » (§3.3.1)
//    private static HBox buildButtons(Journey j) {
//        Button map = new Button("Carte");
//        map.setOnAction(e -> {
//            // construction de l’URI uMap (§2.2 & §3.3.4.3)
//            String geoJson = j.toGeoJson(); // méthode à fournir dans Journey
//            URI uri = URI.create("https://umap.osm.ch/fr/map/?data=" +
//                    geoJson.replace(" ", ""));
//            Desktop.getDesktop().browse(uri);
//        });
//
//        Button cal = new Button("Calendrier");
//        cal.setOnAction(e -> {
//            FileChooser fc = new FileChooser();
//            fc.setInitialFileName("voyage_" + j.date() + ".ics");
//            Path p = fc.showSaveDialog(new Stage()).toPath();
//            try {
//                Files.writeString(p, j.toICalendar()); // méthode à fournir dans Journey
//            } catch (IOException ex) {
//                ex.printStackTrace();
//            }
//        });
//
//        HBox box = new HBox(10, map, cal);
//        box.setId("buttons");                           // §3.3.1 :contentReference[oaicite:6]{index=6}&#8203;:contentReference[oaicite:7]{index=7}
//        return box;
//    }
//}

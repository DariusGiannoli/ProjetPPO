package ch.epfl.rechor.gui;

import ch.epfl.rechor.FormatterFr;
import ch.epfl.rechor.journey.Journey;
import ch.epfl.rechor.journey.Journey.Leg;
import ch.epfl.rechor.journey.Journey.Leg.Foot;
import ch.epfl.rechor.journey.Journey.Leg.Transport;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;

import java.time.Duration;

import java.time.LocalTime;
import java.util.List;
import java.util.function.Consumer;

/**
 * SummaryUI – affiche la vue d'ensemble de tous les voyages.
 */
public record SummaryUI(Node rootNode, ObservableValue<Journey> selectedJourneyO) {

    private static final int RADIUS = 3;

    //Helper method
    private static <T> void onChange(
            ObservableValue<T> obs,
            Consumer<T> consumer
    ) {
        obs.subscribe((newVal) -> {
            if (newVal != null) {
                consumer.accept(newVal);
            }
        });
        T current = obs.getValue();
        if (current != null) {
            consumer.accept(current);
        }
    }


    /**
     * Crée la vue d'ensemble : une ListView de Journey, style summary.css,
     * sélectionne automatiquement le premier voyage à l'heure désirée.
     *
     * @param journeysO liste observable de voyages
     * @param depTimeO heure de départ désirée
     * @return SummaryUI (rootNode + selectedJourneyO)
     */
    public static SummaryUI create(ObservableValue<List<Journey>> journeysO,
                                   ObservableValue<LocalTime> depTimeO) {
        ListView<Journey> listView = new ListView<>();
        listView.setId("summary");
        listView.getStylesheets().add("summary.css");
        listView.setCellFactory(lv -> new JourneyCell());

        ObservableList<Journey> backingItems = FXCollections.observableArrayList();
        listView.setItems(backingItems);

        onChange(journeysO, newList -> {
            backingItems.setAll(newList);
            selectJourney(listView, depTimeO.getValue());
        });

        onChange(depTimeO, newTime -> selectJourney(listView, newTime));

        ObservableValue<Journey> selected = listView.getSelectionModel().selectedItemProperty();
        return new SummaryUI(listView, selected);
    }

    /**
     * Sélectionne dans la liste le premier voyage dont l'heure de départ est supérieure ou égale à
     * l'heure indiquée, ou le dernier si aucun n'est plus tard.
     * @param view instance de ListView qui affiche l'ensemble des voyages.
     * @param time l'heure de départ désirée.
     */
    private static void selectJourney(ListView<Journey> view, LocalTime time) {
        var items = view.getItems();
        if (items == null || items.isEmpty() || time == null) return;
        int idx = 0;
        while (idx < items.size() &&
                items.get(idx).depTime().toLocalTime().isBefore(time)) {
            idx++;
        }
        if (idx >= items.size()) idx = items.size() - 1;
        view.getSelectionModel().select(idx);
        view.scrollTo(idx);
    }

    //crée des cercles
    private static Circle makeCircle(String styleClass, double userData) {
        var c = new Circle(RADIUS);
        c.getStyleClass().add(styleClass);
        c.setUserData(userData);
        return c;
    }

    /**
     * Cellule personnalisée affichant un résumé de Voyage
     */
    private static class JourneyCell extends ListCell<Journey> {
        private final BorderPane root         = new BorderPane();
        private final Text departureText      = new Text();
        private final Text arrivalText        = new Text();
        private final HBox routeBox           = new HBox(4);
//      private final Group circlesGroup = new Group();
        private final Pane changePane = createChangePane();
        private final HBox durationBox        = new HBox();

        JourneyCell() {
            root.getStyleClass().add("journey");
            departureText.getStyleClass().add("departure");
            routeBox.getStyleClass().add("route");
            durationBox.getStyleClass().add("duration");

            root.setLeft(departureText);
            root.setTop(routeBox);
            root.setCenter(changePane);
            root.setRight(arrivalText);
            root.setBottom(durationBox);
        }

        @Override
        protected void updateItem(Journey journey, boolean empty) {
            super.updateItem(journey, empty);

            if (empty || journey == null) {
                setGraphic(null);
                return;
            }

            clearOldContent();

            List<Transport> transports = extractTransports(journey);
            if (transports.isEmpty()) {
                setGraphic(root);
                return;
            }

            Transport first = transports.getFirst();
            Transport last  = transports.getLast();
            Duration total  = Duration.between(first.depTime(), last.arrTime());
            double totalSec = total.toSeconds();

            updateTimes(first, last);
            updateRoute(first);
            updateChangeCircles(first, journey.legs(),journey, totalSec);
            updateDuration(total);

            setGraphic(root);
        }

        // —————— méthodes extraites pour update ——————

        private void clearOldContent() {
            routeBox.getChildren().clear();
            durationBox.getChildren().clear();
            changePane.getChildren().removeIf(n -> n instanceof Circle);
        }

        private List<Transport> extractTransports(Journey journey) {
            return journey.legs().stream()
                    .filter(l -> l instanceof Transport)
                    .map(l -> (Transport) l)
                    .toList();
        }

        private void updateTimes(Transport first, Transport last) {
            departureText.setText(FormatterFr.formatTime(first.depTime()));
            arrivalText  .setText(FormatterFr.formatTime(last.arrTime()));
        }

        private void updateRoute(Transport first) {
            var icon     = new ImageView(VehicleIcons.iconFor(first.vehicle()));
            icon.setFitWidth(20);
            icon.setFitHeight(20);
            icon.setPreserveRatio(true);

            var routeDest = new Text(FormatterFr.formatRouteDestination(first));
            routeBox.getChildren().setAll(icon, routeDest);
        }

        private void updateChangeCircles(
                Transport first, List<Leg> legs, Journey journey, double totalSec) {
            // cercle départ
            changePane.getChildren().add(makeCircle("dep-arr", 0.0));

            // cercles de changement
            for (Leg leg : legs) {
                if (leg instanceof Foot foot
                        && !foot.depStop().equals(journey.depStop())
                        && !foot.arrStop().equals(journey.arrStop())) {

                    double rel = Duration
                            .between(first.depTime().toLocalTime(), foot.depTime())
                            .toSeconds() / totalSec;
                    changePane.getChildren().add(makeCircle("transfer", rel));
                }
            }

            // cercle d'arrivée
            changePane.getChildren().add(makeCircle("dep-arr", 1.0));

        }

        private void updateDuration(Duration total) {
            var durText = new Text(FormatterFr.formatDuration(total));
            durationBox.getChildren().setAll(durText);
        }


        private static Pane createChangePane() {
            Pane pane = new Pane() {
                private final Line line = new Line();
                {
                    getChildren().add(line);
                    setPrefSize(0, 0);
                }

                /**
                 * Cette methode est redéfinie afin de dimensionner et positionner correctement
                 * la ligne et les disques dans le panneau.
                 */
                @Override
                protected void layoutChildren() {
                    double w = getWidth();
                    double h = getHeight();
                    double y = h / 2;
                    double m = 5;
                    line.setStartX(m);
                    line.setStartY(y);
                    line.setEndX(w - m);
                    line.setEndY(y);
                    // positionne les cercles réstants
                    for (var n : getChildren()) {
                        if (n instanceof Circle c) {
                            double rel = (double) c.getUserData();
                            double x = m + rel * (w - 2 * m);
                            c.setCenterX(x);
                            c.setCenterY(y);
                        }
                    }
                }
            };
            return pane;
        }
    }

}

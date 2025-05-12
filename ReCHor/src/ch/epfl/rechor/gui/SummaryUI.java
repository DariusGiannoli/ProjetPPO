package ch.epfl.rechor.gui;

import ch.epfl.rechor.FormatterFr;
import ch.epfl.rechor.journey.Journey;
import ch.epfl.rechor.journey.Journey.Leg;
import ch.epfl.rechor.journey.Journey.Leg.Foot;
import ch.epfl.rechor.journey.Journey.Leg.Transport;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
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
import java.util.stream.Collectors;

/**
 * SummaryUI – affiche la vue d'ensemble de tous les voyages.
 */
public record SummaryUI(Node rootNode, ObservableValue<Journey> selectedJourneyO) {

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
        var listView = new ListView<Journey>();
        listView.setId("summary");
        listView.getStylesheets().add("summary.css");
        listView.setCellFactory(lv -> new JourneyCell());

        // Met à jour les items quand la liste change
        journeysO.addListener((obs, oldList, newList) -> {
            listView.setItems(FXCollections.observableArrayList(newList));
            selectJourney(listView, depTimeO.getValue());
        });
        // initialisation
        if (journeysO.getValue() != null) {
            listView.setItems(FXCollections.observableArrayList(journeysO.getValue()));
        }
        // Met à jour la sélection quand l'heure change
        depTimeO.addListener((obs, oldTime, newTime) -> selectJourney(listView, newTime));
        if (depTimeO.getValue() != null) {
            selectJourney(listView, depTimeO.getValue());
        }

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

    /**
     * Cellule personnalisée affichant un résumé de Voyage
     */
    private static class JourneyCell extends ListCell<Journey> {
        private final BorderPane root = new BorderPane();
        private final Text departureText = new Text();
        private final Text arrivalText   = new Text();
        private final HBox routeBox      = new HBox(4);
        private final Pane changePane    = createChangePane();
        private final HBox durationBox   = new HBox();

        /**
         * Constructeur de JourneyCell, qui crée le graphe de scène correspondant à la cellule.
         */
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

        /**
         * Cette methode a pour but de remplir les différents éléments du graphe
         * de scène construit par le constructeur avec les données du voyage à afficher.
         * @param journey Le voyage dont on doit afficher le résumé dans la cellule.
         * @param empty   si la valeur est faux, alors le text et l'affichage seront null.
         */
        @Override
        protected void updateItem(Journey journey, boolean empty) {
            super.updateItem(journey, empty);
            if (empty || journey == null) {
                setGraphic(null);
            } else {
                // Nettoyage
                routeBox.getChildren().clear();
                durationBox.getChildren().clear();
                // Retire tous les cercles (mais pas la ligne)
                changePane.getChildren().removeIf(n -> n instanceof Circle);

                // Récupère premier et dernier transport
                var transports = journey.legs().stream()
                        .filter(l -> l instanceof Transport)
                        .map(l -> (Transport) l)
                        .toList();
                if (transports.isEmpty()) {
                    setGraphic(root);
                    return;
                }
                Transport first = transports.getFirst();
                Transport last  = transports.getLast();

                // Texte heures
                departureText.setText(FormatterFr.formatTime(first.depTime()));
                arrivalText.setText(FormatterFr.formatTime(last.arrTime()));

                // Icône + ligne/direction
                var icon = new ImageView(VehicleIcons.iconFor(first.vehicle()));
                icon.setFitWidth(20);
                icon.setFitHeight(20);
                icon.setPreserveRatio(true);
                var routeDest = new Text(FormatterFr.formatRouteDestination(first));
                routeBox.getChildren().addAll(icon, routeDest);

                // Cercles sur la ligne
                Duration total = Duration.between(first.depTime().toLocalTime(),
                        last.arrTime().toLocalTime());
                double totalSec = total.toSeconds();
                // départ
                var depCircle = new Circle(3);
                depCircle.getStyleClass().add("dep-arr");
                depCircle.setUserData(0.0);
                changePane.getChildren().add(depCircle);
                // changements (foot legs)
                for (Leg leg : journey.legs()) {
                    if (leg instanceof Foot foot && !leg.depStop().equals(journey.depStop()) && !leg.arrStop().equals(journey.arrStop())) {
                        double rel = Duration.between(first.depTime().toLocalTime(),
                                foot.depTime()).toSeconds() / totalSec;
                        var tCircle = new Circle(3);
                        tCircle.getStyleClass().add("transfer");
                        tCircle.setUserData(rel);
                        changePane.getChildren().add(tCircle);
                    }
                }
                // arrivée
                var arrCircle = new Circle(3);
                arrCircle.getStyleClass().add("dep-arr");
                arrCircle.setUserData(1.0);
                changePane.getChildren().add(arrCircle);

                // Durée
                var durText = new Text(FormatterFr.formatDuration(
                        Duration.between(first.depTime().toLocalTime(),
                                last.arrTime().toLocalTime())));
                durationBox.getChildren().add(durText);

                setGraphic(root);
            }
        }

        /**
         * Crée le panneau qui dessine la ligne représentant le voyage et
         * positionne les cercles qui représentent les changements sur cette dernière.
         * @return le panneau contenant la ligne en les cercles au format voulu.
         */
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

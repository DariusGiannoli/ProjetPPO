package ch.epfl.rechor.gui;

import ch.epfl.rechor.FormatterFr;
import ch.epfl.rechor.journey.Journey;
import ch.epfl.rechor.journey.Journey.Leg.Foot;
import ch.epfl.rechor.journey.Journey.Leg.Transport;
import ch.epfl.rechor.journey.Stop;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Group;
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
 * Représente la vue d'ensemble des voyages.
 */
public record SummaryUI(Node rootNode, ObservableValue<Journey> selectedJourneyO) {

    private static final int RADIUS = 3;
    private static final int ICON_SIZE = 20;
    private static final int ROUTE_BOX_SPACING = 4;
    private static final int TIMELINE_MARGIN = 5;

    public static SummaryUI create(ObservableValue<List<Journey>> journeysO,
                                   ObservableValue<LocalTime> depTimeO) {
        // Création de la liste avec configuration initiale
        ListView<Journey> listView = new ListView<>();
        listView.setId("summary");
        listView.getStylesheets().add("summary.css");
        listView.setCellFactory(lv -> new JourneyCell());

        // Liste backing stockée dans une variable pour éviter de la recréer
        ObservableList<Journey> backingItems = FXCollections.observableArrayList();
        listView.setItems(backingItems);

        // Création d'un seul Consumer réutilisable pour la mise à jour des voyages
        Consumer<List<Journey>> updateJourneys = newList -> {
            backingItems.setAll(newList);
            selectJourney(listView, depTimeO.getValue());
        };
        // Traitement immédiat de la valeur actuelle si disponible
        if (journeysO.getValue() != null) updateJourneys.accept(journeysO.getValue());
        // Abonnement aux changements futurs avec le même Consumer
        journeysO.subscribe(newVal -> { if (newVal != null) updateJourneys.accept(newVal); });

        // Même approche pour l'heure de départ
        Consumer<LocalTime> updateTime = newTime -> selectJourney(listView, newTime);
        if (depTimeO.getValue() != null) updateTime.accept(depTimeO.getValue());
        depTimeO.subscribe(newVal -> { if (newVal != null) updateTime.accept(newVal); });

        // Retourne directement la propriété selectedItem de la liste
        return new SummaryUI(listView, listView.getSelectionModel().selectedItemProperty());
    }

    /**
     * Sélectionne le premier voyage après l'heure indiquée
     * Optimisé pour parcourir la liste une seule fois
     */
    private static void selectJourney(ListView<Journey> view, LocalTime time) {
        ObservableList<Journey> items = view.getItems();
        if (items == null || items.isEmpty() || time == null) return;

        // Recherche optimisée en une seule passe
        int idx = 0;
        while (idx < items.size() && items.get(idx).depTime().toLocalTime().isBefore(time)) {
            idx++;
        }

        // Ajustement si pas de résultat
        if (idx >= items.size()) idx = items.size() - 1;

        // Sélection et défilement en une seule opération chacun
        view.getSelectionModel().select(idx);
        view.scrollTo(idx);
    }

    private static class JourneyCell extends ListCell<Journey> {
        // Composants d'UI
        private final BorderPane root;
        private final Text departureText;
        private final Text arrivalText;
        private final HBox routeBox ;
        private final Group circlesGroup ;
        private final Pane changePane;
        private final HBox durationBox;
        private final Text durationText ;
        private final ImageView icon;
        private final Text routeDestText;
        private final Line timelineLine;

        JourneyCell() {
            // Initialisation des objets UI
            root = new BorderPane();
            departureText = new Text();
            arrivalText = new Text();
            routeBox = new HBox(ROUTE_BOX_SPACING);
            circlesGroup = new Group();
            durationBox = new HBox();
            durationText = new Text();
            icon = new ImageView();
            routeDestText = new Text();
            timelineLine = new Line();

            // Configuration des styles
            root.getStyleClass().add("journey");
            departureText.getStyleClass().add("departure");
            routeBox.getStyleClass().add("route");
            durationBox.getStyleClass().add("duration");

            // Pré-configuration de l'icône
            icon.setFitWidth(ICON_SIZE);
            icon.setFitHeight(ICON_SIZE);
            icon.setPreserveRatio(true);

            // Ajout des composants aux conteneurs
            routeBox.getChildren().addAll(icon, routeDestText);
            durationBox.getChildren().add(durationText);

            // Création du panneau de ligne temporelle
            changePane = new Pane() {
                {
                    // Initialisation des enfants
                    getChildren().addAll(timelineLine, circlesGroup);
                    setPrefSize(0, 0);
                }

                @Override
                protected void layoutChildren() {
                    // Calcul des dimensions une seule fois par layout
                    double width = getWidth();
                    double height = getHeight();
                    double centerY = height / 2;
                    double margin = TIMELINE_MARGIN;

                    // Positionnement de la ligne avec des calculs minimaux
                    timelineLine.setStartX(margin);
                    timelineLine.setStartY(centerY);
                    timelineLine.setEndX(width - margin);
                    timelineLine.setEndY(centerY);

                    // Calcul unique de la largeur utile pour tous les cercles
                    double usableWidth = width - 2 * margin;

                    // Positionnement optimisé des cercles
                    for (Node n : circlesGroup.getChildren()) {
                        if (n instanceof Circle c) {
                            double relativePos = (double) c.getUserData();
                            c.setCenterX(margin + relativePos * usableWidth);
                            c.setCenterY(centerY);
                        }
                    }
                }
            };

            // Assemblage de la structure
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

            // Réinitialiser
            circlesGroup.getChildren().clear();

            // Extraction des transports avec méthode utilitaire
            List<Transport> transports = extractTransports(journey);
            if (transports.isEmpty()) {
                setGraphic(root);
                return;
            }

            // Calcul des données principales
            Transport first = transports.getFirst();
            Transport last = transports.getLast();
            Duration total = Duration.between(first.depTime(), last.arrTime());
            double totalSec = total.toSeconds();
            LocalTime firstDepTime = first.depTime().toLocalTime();

            // Mise à jour des composants réutilisables avec les nouvelles données
            departureText.setText(FormatterFr.formatTime(first.depTime()));
            arrivalText.setText(FormatterFr.formatTime(last.arrTime()));
            durationText.setText(FormatterFr.formatDuration(total));

            // Mise à jour de l'UI de route
            icon.setImage(VehicleIcons.iconFor(first.vehicle()));
            routeDestText.setText(FormatterFr.formatRouteDestination(first));

            // Ajout des cercles avec une seule instance par cercle
            addCircle("dep-arr", 0.0);
            // Passage des données pré-calculées
            addTransferCircles(journey, firstDepTime, totalSec);
            addCircle("dep-arr", 1.0);

            // Affectation du graphique
            setGraphic(root);
        }

        private List<Transport> extractTransports(Journey journey) {
            return journey.legs().stream()
                    .filter(Transport.class::isInstance)
                    .map(Transport.class::cast)
                    .toList();
        }

        private void addTransferCircles(Journey journey, LocalTime firstDepTime, double totalSec) {
            // Stock les valeurs
            Stop depStop = journey.depStop();
            Stop arrStop = journey.arrStop();

            journey.legs().stream()
                    .filter(Foot.class::isInstance)
                    .map(Foot.class::cast)
                    .filter(foot -> !foot.depStop().equals(depStop) &&
                            !foot.arrStop().equals(arrStop))
                    .forEach(foot -> {
                        // Calcul optimisé de la position relative
                        double relPos = Duration
                                .between(firstDepTime, foot.depTime())
                                .toSeconds() / totalSec;
                        addCircle("transfer", relPos);
                    });
        }

        private void addCircle(String styleClass, double relativePosition) {
            Circle c = new Circle(RADIUS);
            c.getStyleClass().add(styleClass);
            c.setUserData(relativePosition); // Stocke la position pour le layout
            circlesGroup.getChildren().add(c);
        }
    }
}
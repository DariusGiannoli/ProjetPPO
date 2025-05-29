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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Représente la vue d'ensemble des voyages. Affiche tous les voyages
 * dans une liste, avec pour chacun les informations essentielles : heure de
 * départ/arrivée, ligne, destination et changements.
 *
 * @param rootNode le nœud JavaFX à la racine du graphe de scène
 * @param selectedJourneyO valeur observable contenant le voyage sélectionné
 *
 * @author Antoine Lepin (390950)
 * @author Darius Giannoli (380759)
 */
public record SummaryUI(Node rootNode, ObservableValue<Journey> selectedJourneyO) {

    /**
     * Crée la vue d'ensemble : une ListView de Journey, style summary.css,
     * sélectionne automatiquement le premier voyage à l'heure désirée.
     *
     * @param journeysO liste observable de voyages
     * @param depTimeO heure de départ voulue
     * @return SummaryUI (rootNode + selectedJourneyO)
     */
    public static SummaryUI create(ObservableValue<List<Journey>> journeysO,
                                   ObservableValue<LocalTime> depTimeO) {
        // Création de la liste avec configuration initiale
        ListView<Journey> listView = new ListView<>();
        listView.setId("summary");
        listView.getStylesheets().add("summary.css");
        listView.setCellFactory(lv -> new JourneyCell()); // pb ici

        // Liste backing stockée dans une variable pour éviter de la recréer
        ObservableList<Journey> backingItems = FXCollections.observableArrayList();
        listView.setItems(backingItems);

        // Handler unifié pour les mises à jour des journeys et du temps
        Runnable updateHandler = () -> {
            List<Journey> journeys = journeysO.getValue();
            LocalTime depTime = depTimeO.getValue();
            if (journeys != null && depTime != null) {
                backingItems.setAll(journeys);
                selectJourney(listView, depTime);
            }
        };
        journeysO.subscribe(v -> updateHandler.run());
        depTimeO.subscribe(v -> updateHandler.run());
        updateHandler.run(); // Appel initial

        // Retourne directement la propriété selectedItem de la liste
        return new SummaryUI(listView, listView.getSelectionModel().selectedItemProperty());
    }

    /**
     * Sélectionne dans la liste le premier voyage dont l'heure de départ est supérieure ou égale à
     * l'heure indiquée, ou le dernier si aucun n'est plus tard.
     */
    private static void selectJourney(ListView<Journey> view, LocalTime time) {
        ObservableList<Journey> items = view.getItems();
        if (items == null || items.isEmpty()) return;

        int itemsSize = items.size();
        int selectedIndex = itemsSize- 1; // Par défaut le dernier
        for (int i = 0; i < itemsSize; i++) {
            LocalTime itemDepTime = items.get(i).depTime().toLocalTime();
            if (!itemDepTime.isBefore(time)) {
                selectedIndex = i;
                break;
            }
        }

        view.getSelectionModel().select(selectedIndex);
        view.scrollTo(selectedIndex);
    }

    /**
     * Cellule personnalisée affichant un résumé de voyage.
     */
    private static class JourneyCell extends ListCell<Journey> {
        // Constantes de style
        private static final String DEP_ARR_STYLE_CLASS = "dep-arr";
        private static final String TRANSFER_STYLE_CLASS = "transfer";

        // Constantes dimensions
        private static final int RADIUS = 3;
        private static final int ICON_SIZE = 20;
        private static final int ROUTE_BOX_SPACING = 4;
        private static final int TIMELINE_MARGIN = 5;
        private static final double START_POSITION = 0.0;
        private static final double END_POSITION = 1.0;

        // Composants d'UI
        private final BorderPane root;
        private final Text departureText, arrivalText, routeDestText, durationText;
        private final Group circlesGroup;
        private final ImageView icon;
        private final Line timelineLine;
        private final Pane timelinePane;

        private Journey pastJourney;

        /**
         * Constructeur initialisant la structure de base de la cellule.
         */
        JourneyCell() {
            // Initialisation des objets UI
            root = new BorderPane();
            departureText = new Text();
            arrivalText = new Text();
            routeDestText = new Text();
            durationText = new Text();
            circlesGroup = new Group();
            icon = new ImageView();
            timelineLine = new Line();

            // Variables locales pour les conteneurs
            HBox routeBox = new HBox(ROUTE_BOX_SPACING);
            HBox durationBox = new HBox();

            // Configuration des styles
            root.getStyleClass().add("journey");
            departureText.getStyleClass().add("departure");
            routeBox.getStyleClass().add("route");
            durationBox.getStyleClass().add("duration");

            // Configuration de l'icône
            icon.setFitWidth(ICON_SIZE);
            icon.setFitHeight(ICON_SIZE);
            icon.setPreserveRatio(true);

            // Panneau de ligne temporelle
            timelinePane = createTimelinePane();

            // Assemblage des conteneurs
            routeBox.getChildren().addAll(icon, routeDestText);
            durationBox.getChildren().add(durationText);

            // Assemblage de la structure principale
            root.setLeft(departureText);
            root.setTop(routeBox);
            root.setCenter(timelinePane);
            root.setRight(arrivalText);
            root.setBottom(durationBox);
        }

        /**
         * Crée le panneau de timeline avec positionnement dynamique
         */
        private Pane createTimelinePane() {
            Pane pane = new Pane() {
                @Override
                protected void layoutChildren() {
                    double width = getWidth();
                    double centerY = getHeight() / 2;
                    double usableWidth = width - 2 * TIMELINE_MARGIN;

                    // Configuration de la ligne en une fois
                    timelineLine.setStartX(TIMELINE_MARGIN);
                    timelineLine.setEndX(width - TIMELINE_MARGIN);
                    timelineLine.setStartY(centerY);
                    timelineLine.setEndY(centerY);

                    // Positionnement optimisé des cercles
                    circlesGroup.getChildren().stream()
                            .filter(Circle.class::isInstance)
                            .map(Circle.class::cast)
                            .forEach(circle -> {
                                double relativePos = (double) circle.getUserData();
                                circle.setCenterX(TIMELINE_MARGIN + relativePos * usableWidth);
                                circle.setCenterY(centerY);
                            });
                }
            };
            pane.setPrefSize(0, 0);
            pane.getChildren().addAll(timelineLine, circlesGroup);
            return pane;
        }

        /**
         * Méthode appelée par JavaFX pour mettre à jour le contenu de la cellule
         * quand un nouvel élément lui est assigné.
         */
        @Override
        protected void updateItem(Journey journey, boolean empty) {
            super.updateItem(journey, empty);

            if (empty || journey == null) {
                setGraphic(null);
                return;
            }

            List<Transport> transports = new ArrayList<>();
            for (Journey.Leg leg : journey.legs()) {
                if (leg instanceof Transport transport) {
                    transports.add(transport);
                }
            }

            // Pas besoin d'afficher si pas de transport
            if (transports.isEmpty()) {
                setGraphic(null);
                return;
            }

            updateJourneyDisplay(journey, transports);
            setGraphic(root);
        }

        /**
         * Met à jour l'affichage avec les informations du voyage
         */
        private void updateJourneyDisplay(Journey journey, List<Transport> transports) {
            Transport first = transports.getFirst();
            Transport last = transports.getLast();

            // Calculs de temps
            LocalDateTime firstDepTime = first.depTime();
            LocalDateTime lastArrTime = last.arrTime();
            Duration totalDuration = Duration.between(firstDepTime, lastArrTime);

            // Mise à jour directe des textes
            departureText.setText(FormatterFr.formatTime(firstDepTime));
            arrivalText.setText(FormatterFr.formatTime(lastArrTime));
            durationText.setText(FormatterFr.formatDuration(totalDuration));
            routeDestText.setText(FormatterFr.formatRouteDestination(first));

            // Icône
            icon.setImage(VehicleIcons.iconFor(first.vehicle()));

            // Régénération des cercles seulement si le voyage a changé
            if (!journey.equals(pastJourney)) {
                circlesGroup.getChildren().clear();

                double totalSeconds = totalDuration.toSeconds();
                Stop depStop = journey.depStop();
                Stop arrStop = journey.arrStop();
                LocalTime firstDepLocalTime = firstDepTime.toLocalTime();

                // Boucle pour les segments à pied (transferts)
                for (Journey.Leg leg : journey.legs()) {
                    if (leg instanceof Foot foot) {
                        Stop footDepStop = foot.depStop();
                        Stop footArrStop = foot.arrStop();

                        // Filtre les transferts (exclut départ/arrivée principaux)
                        if (!footDepStop.equals(depStop) && !footArrStop.equals(arrStop)) {
                            double secondsFromStart =
                                    Duration.between(firstDepLocalTime, foot.depTime()).toSeconds();
                            double relativePosition = secondsFromStart / totalSeconds;
                            addCircle(TRANSFER_STYLE_CLASS, relativePosition);
                        }
                    }
                }

                // Ajout des cercles de départ/arrivée et des transferts
                addCircle(DEP_ARR_STYLE_CLASS, START_POSITION);
                addCircle(DEP_ARR_STYLE_CLASS, END_POSITION);

                pastJourney = journey;
            }
        }

        /**
         * Crée un cercle avec une classe de style et le positionne à la position relative
         * passée en argument.
         */
        private void addCircle(String styleClass, double relativePosition) {
            Circle c = new Circle(RADIUS);
            c.getStyleClass().add(styleClass);
            c.setUserData(relativePosition);
            circlesGroup.getChildren().add(c);
        }
    }
}
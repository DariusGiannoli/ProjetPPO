package ch.epfl.rechor.gui;

import ch.epfl.rechor.FormatterFr;
import ch.epfl.rechor.journey.Journey;
import ch.epfl.rechor.journey.Journey.Leg;
import ch.epfl.rechor.journey.Journey.Leg.Foot;
import ch.epfl.rechor.journey.Journey.Leg.Transport;
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
 * Représente la vue d'ensemble des voyages. Affiche tous les voyages
 * dans une liste, avec pour chacun les informations essentielles: heure de
 * départ/arrivée, ligne, destination et changements.
 *
 * @param rootNode le nœud JavaFX à la racine du graphe de scène
 * @param selectedJourneyO valeur observable contenant le voyage sélectionné
 *
 * @author Antoine Lepin (390950)
 * @author Darius Giannoli (380759)
 */
public record SummaryUI(Node rootNode, ObservableValue<Journey> selectedJourneyO) {

    // Rayon des cercles représentant les arrêts et changements
    private static final int RADIUS = 3;
    // Taille des icônes de véhicule
    private static final int ICON_SIZE = 20;
    //Espacement entre les éléments dans les boîtes horizontales
    private static final int ROUTE_BOX_SPACING = 4;

    /**
     * Crée la vue d'ensemble : une ListView de Journey, style summary.css,
     * sélectionne automatiquement le premier voyage à l'heure désirée.
     *
     * @param journeysO liste observable de voyages
     * @param depTimeO heure de départ intéressée
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

        // Abonnement aux changements de la liste des voyages
        onChange(journeysO, newList -> {
            backingItems.setAll(newList);
            selectJourney(listView, depTimeO.getValue());
        });

        // Abonnement aux changements de l'heure désirée
        onChange(depTimeO, newTime -> selectJourney(listView, newTime));

        // Élément sélectionné dans la liste
        ObservableValue<Journey> selected = listView.getSelectionModel().selectedItemProperty();
        return new SummaryUI(listView, selected);
    }

    /**
     * Helper pour gérer les changements de valeur dans un ObservableValue.
     * Applique le consumer à la valeur actuelle si non-null et s'abonne aux
     * changements futurs.
     *
     * @param <T> type de la valeur observable
     * @param obs valeur observable à surveiller
     * @param consumer action à exécuter lorsque la valeur change
     */
    private static <T> void onChange(ObservableValue<T> obs, Consumer<T> consumer) {
        // S'abonner aux changements futurs
        obs.subscribe((newVal) -> {
            if (newVal != null) {
                consumer.accept(newVal);
            }
        });

        // Traiter la valeur actuelle si elle existe
        T current = obs.getValue();
        if (current != null) {
            consumer.accept(current);
        }
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
        while (idx < items.size() && items.get(idx).depTime().toLocalTime().isBefore(time)) {
            idx++;
        }

        if (idx >= items.size()) idx = items.size() - 1;
        view.getSelectionModel().select(idx);
        view.scrollTo(idx);
    }

    /**
     * Crée un cercle avec une classe de style et une position relative.
     *
     * @param styleClass la classe de style CSS à appliquer
     * @param userData la position relative (0.0 à 1.0) sur la ligne de temps
     * @return le cercle configuré
     */
    private static Circle makeCircle(String styleClass, double userData) {
        var c = new Circle(RADIUS);
        c.getStyleClass().add(styleClass);
        c.setUserData(userData);
        return c;
    }

    /**
     * Cellule personnalisée affichant un résumé de voyage.
     * Chaque voyage est présenté avec:
     * - L'heure de départ et d'arrivée
     * - Une icône du premier véhicule et sa destination
     * - Une ligne temporelle avec les points de changement
     * - La durée totale du voyage
     */
    private static class JourneyCell extends ListCell<Journey> {
        private final BorderPane root = new BorderPane();
        private final Text departureText = new Text();
        private final Text arrivalText = new Text();
        private final HBox routeBox = new HBox(ROUTE_BOX_SPACING);
        private final Group circlesGroup = new Group();
        private final Pane changePane;
        private final HBox durationBox = new HBox();

        /**
         * Constructeur initialisant la structure de base de la cellule.
         */
        JourneyCell() {
            changePane = createChangePane();

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
         * Méthode appelée par JavaFX pour mettre à jour le contenu de la cellule
         * quand un nouvel élément lui est assigné.
         *
         * @param journey le voyage à afficher
         * @param empty indique si la cellule est vide
         */
        @Override
        protected void updateItem(Journey journey, boolean empty) {
            super.updateItem(journey, empty);

            // Cas d'une cellule vide ou sans voyage
            if (empty || journey == null) {
                setGraphic(null);
                return;
            }

            clearOldContent();

            // Extraire les étapes en transport
            List<Transport> transports = extractTransports(journey);
            if (transports.isEmpty()) {
                setGraphic(root);
                return;
            }

            // Calculer les informations temporelles
            Transport first = transports.getFirst();
            Transport last = transports.getLast();
            Duration total = Duration.between(first.depTime(), last.arrTime());
            double totalSec = total.toSeconds();

            // Mettre à jour les différentes parties de l'interface
            updateTimes(first, last);
            updateRoute(first);
            updateChangeCircles(first, journey.legs(), journey, totalSec);
            updateDuration(total);

            setGraphic(root);
        }

        /**
         * Vide le contenu des composants pour éviter des duplications.
         */
        private void clearOldContent() {
            routeBox.getChildren().clear();
            durationBox.getChildren().clear();
            circlesGroup.getChildren().clear();
        }

        /**
         * Extrait les étapes en transport du voyage.
         *
         * @param journey le voyage à analyser
         * @return liste des étapes en transport
         */
        private List<Transport> extractTransports(Journey journey) {
            return journey.legs().stream()
                    .filter(l -> l instanceof Transport)
                    .map(l -> (Transport) l)
                    .toList();
        }

        /**
         * Met à jour les heures de départ et d'arrivée affichées.
         *
         * @param first première étape en transport
         * @param last dernière étape en transport
         */
        private void updateTimes(Transport first, Transport last) {
            departureText.setText(FormatterFr.formatTime(first.depTime()));
            arrivalText.setText(FormatterFr.formatTime(last.arrTime()));
        }

        /**
         * Met à jour l'affichage de l'icône et de la destination
         * du premier véhicule.
         *
         * @param first première étape en transport
         */
        private void updateRoute(Transport first) {
            ImageView icon = new ImageView(VehicleIcons.iconFor(first.vehicle()));
            icon.setFitWidth(ICON_SIZE);
            icon.setFitHeight(ICON_SIZE);
            icon.setPreserveRatio(true);

            Text routeDest = new Text(FormatterFr.formatRouteDestination(first));
            routeBox.getChildren().setAll(icon, routeDest);
        }

        /**
         * Met à jour la ligne temporelle avec les cercles de départ,
         * d'arrivée et de changement.
         *
         * @param first première étape en transport
         * @param legs toutes les étapes du voyage
         * @param journey le voyage complet
         * @param totalSec durée totale en secondes
         */
        private void updateChangeCircles(Transport first, List<Leg> legs, Journey journey,
                                         double totalSec) {
            // Ajouter cercle départ
            circlesGroup.getChildren().add(makeCircle("dep-arr", 0.0));

            // Ajouter cercles intermédiaires pour les changements
            legs.stream()
                    .filter(leg -> leg instanceof Foot)
                    .map(leg -> (Foot)leg)
                    .filter(foot -> !foot.depStop().equals(journey.depStop())
                            && !foot.arrStop().equals(journey.arrStop()))
                    .forEach(foot -> {
                        double relativePosition = Duration
                                .between(first.depTime().toLocalTime(), foot.depTime())
                                .toSeconds() / totalSec;
                        circlesGroup.getChildren().add(makeCircle("transfer", relativePosition));
                    });

            // Ajouter cercle d'arrivée
            circlesGroup.getChildren().add(makeCircle("dep-arr", 1.0));
        }

        /**
         * Met à jour l'affichage de la durée du voyage.
         *
         * @param total durée totale du voyage
         */
        private void updateDuration(Duration total) {
            Text durText = new Text(FormatterFr.formatDuration(total));
            durationBox.getChildren().setAll(durText);
        }

        /**
         * Crée le panneau contenant la ligne temporelle et les cercles.
         * Utilise une sous-classe anonyme de Pane qui gère la mise en page
         * des éléments graphiques.
         *
         * @return le panneau configuré
         */
        private Pane createChangePane() {
            return new Pane() {
                private final Line line = new Line();
                {
                    getChildren().addAll(line, circlesGroup); // Add line and circlesGroup
                    setPrefSize(0, 0);
                }

                /**
                 * Redéfinit la méthode layoutChildren pour positionner la ligne
                 * et les cercles selon la taille disponible.
                 */
                @Override
                protected void layoutChildren() {
                    double w = getWidth();
                    double h = getHeight();
                    double y = h / 2;
                    double m = 5;

                    // Positionner la ligne
                    line.setStartX(m);
                    line.setStartY(y);
                    line.setEndX(w - m);
                    line.setEndY(y);

                    // Positionner les cercles
                    for (var n : circlesGroup.getChildren()) {
                        if (n instanceof Circle c) {
                            double rel = (double) c.getUserData();
                            double x = m + rel * (w - 2 * m);
                            c.setCenterX(x);
                            c.setCenterY(y);
                        }
                    }
                }
            };
        }
    }
}
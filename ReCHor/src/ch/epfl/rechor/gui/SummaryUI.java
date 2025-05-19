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

    private static final int RADIUS = 3;
    private static final int ICON_SIZE = 20;
    private static final int ROUTE_BOX_SPACING = 4;
    private static final int TIMELINE_MARGIN = 5;

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
        listView.setCellFactory(lv -> new JourneyCell());

        // Liste backing stockée dans une variable pour éviter de la recréer
        ObservableList<Journey> backingItems = FXCollections.observableArrayList();
        listView.setItems(backingItems);

        // Handler unifié pour les mises à jour des journeys et du temps
        Consumer<Object> updateHandler = o -> {
            if (journeysO.getValue() != null && depTimeO.getValue() != null) {
                backingItems.setAll(journeysO.getValue());
                selectJourney(listView, depTimeO.getValue());
            }
        };

        // Abonnement aux deux observables avec le même handler
        journeysO.subscribe(newVal -> updateHandler.accept(null));
        depTimeO.subscribe(newVal -> updateHandler.accept(null));

        // Traitement initial si les valeurs sont disponibles
        updateHandler.accept(null);

        // Retourne directement la propriété selectedItem de la liste
        return new SummaryUI(listView, listView.getSelectionModel().selectedItemProperty());
    }

    /**
     * Sélectionne dans la liste le premier voyage dont l'heure de départ est supérieure ou égale à
     * l'heure indiquée, ou le dernier si aucun n'est plus tard.
     */
    private static void selectJourney(ListView<Journey> view, LocalTime time) {
        ObservableList<Journey> items = view.getItems();
        if (items == null || items.isEmpty() || time == null) return;

        // Recherche en une seule passe avec index direct
        int idx = 0;
        while (idx < items.size() && items.get(idx).depTime().toLocalTime().isBefore(time)) {
            idx++;
        }

        // Si aucun résultat, prendre le dernier élément
        if (idx >= items.size()) idx = items.size() - 1;

        // Sélection et défilement combinés
        view.getSelectionModel().select(idx);
        view.scrollTo(idx);
    }

    /**
     * Cellule personnalisée affichant un résumé de voyage.
     */
    private static class JourneyCell extends ListCell<Journey> {
        // Composants d'UI
        private final BorderPane root;
        private final Text departureText, arrivalText, routeDestText, durationText;
        private final HBox routeBox, durationBox;
        private final Group circlesGroup;
        private final Pane changePane;
        private final ImageView icon;
        private final Line timelineLine;

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
            routeBox = new HBox(ROUTE_BOX_SPACING);
            durationBox = new HBox();
            circlesGroup = new Group();
            icon = new ImageView();
            timelineLine = new Line();

            // Configuration des styles
            root.getStyleClass().add("journey");
            departureText.getStyleClass().add("departure");
            routeBox.getStyleClass().add("route");
            durationBox.getStyleClass().add("duration");

            // Configuration de l'icône
            icon.setFitWidth(ICON_SIZE);
            icon.setFitHeight(ICON_SIZE);
            icon.setPreserveRatio(true);

            // Assemblage des conteneurs
            routeBox.getChildren().addAll(icon, routeDestText);
            durationBox.getChildren().add(durationText);

            // Création du panneau de ligne temporelle
            changePane = new Pane() {
                {
                    getChildren().addAll(timelineLine, circlesGroup);
                    setPrefSize(0, 0);
                }

                /**
                 * Redéfinit la méthode layoutChildren pour positionner la ligne rouge
                 * et les cercles.
                 */
                @Override
                protected void layoutChildren() {
                    double width = getWidth();
                    double centerY = getHeight() / 2;

                    // Configuration de la ligne temporelle
                    timelineLine.setStartX(TIMELINE_MARGIN);
                    timelineLine.setStartY(centerY);
                    timelineLine.setEndX(width - TIMELINE_MARGIN);
                    timelineLine.setEndY(centerY);

                    // Largeur utilisable calculée une seule fois pour tous les cercles
                    double usableWidth = width - 2 * TIMELINE_MARGIN;

                    // Positionnement des cercles avec leurs positions relatives
                    circlesGroup.getChildren().forEach(n -> {
                        if (n instanceof Circle c) {
                            double relativePos = (double) c.getUserData();
                            c.setCenterX(TIMELINE_MARGIN + relativePos * usableWidth);
                            c.setCenterY(centerY);
                        }
                    });
                }
            };

            // Assemblage de la structure principale
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

            if (empty || journey == null) {
                setGraphic(null);
                return;
            }

            // Réinitialiser
            circlesGroup.getChildren().clear();

            // Récupération et validation des transports
            List<Transport> transports = journey.legs().stream()
                    .filter(Transport.class::isInstance)
                    .map(Transport.class::cast)
                    .toList();

            if (transports.isEmpty()) {
                setGraphic(root);
                return;
            }

            // Calcul des données principales
            Transport first = transports.getFirst();
            Transport last = transports.getLast();
            Duration total = Duration.between(first.depTime(), last.arrTime());
            LocalTime firstDepTime = first.depTime().toLocalTime();
            double totalSec = total.toSeconds();

            // Mise à jour des textes
            departureText.setText(FormatterFr.formatTime(first.depTime()));
            arrivalText.setText(FormatterFr.formatTime(last.arrTime()));
            durationText.setText(FormatterFr.formatDuration(total));

            // Mise à jour de l'icône et de la destination
            icon.setImage(VehicleIcons.iconFor(first.vehicle()));
            routeDestText.setText(FormatterFr.formatRouteDestination(first));

            // Ajout des cercles de départ/arrivée et des transferts
            addCircle("dep-arr", 0.0);
            Stop depStop = journey.depStop();
            Stop arrStop = journey.arrStop();

            journey.legs().stream()
                    .filter(leg -> leg instanceof Foot
                            && !(leg.depStop().equals(depStop))
                            && !(leg.arrStop().equals(arrStop)))
                    .map(Foot.class::cast)
                    .forEach(foot -> {
                        // Calcul de la position relative du cercle de transfert
                        double relPos = Duration
                                .between(firstDepTime, foot.depTime())
                                .toSeconds() / totalSec;
                        addCircle("transfer", relPos);
                    });
            addCircle("dep-arr", 1.0);

            // Affectation du graphique
            setGraphic(root);
        }

        /**
         * Crée un cercle avec une classe de style et le positionne à la position relative
         * passée en argument.
         */
        private void addCircle(String styleClass, double relativePosition) {
            Circle c = new Circle(RADIUS);
            c.getStyleClass().add(styleClass);
            c.setUserData(relativePosition); // Stocke la position pour le layout
            circlesGroup.getChildren().add(c);
        }
    }
}
package ch.epfl.rechor.gui;

import ch.epfl.rechor.journey.*;
import ch.epfl.rechor.journey.Journey.Leg;
import ch.epfl.rechor.journey.Journey.Leg.Foot;
import ch.epfl.rechor.journey.Journey.Leg.Transport;
import ch.epfl.rechor.FormatterFr;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
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
import java.util.stream.IntStream;

import static java.awt.Desktop.getDesktop;

/**
 * DetailUI représente la partie de l'interface graphique qui montre les détails d'un voyage.
 *
 * @author Antoine Lepin (390950)
 * @author Darius Giannoli (380759)
 */
public record DetailUI(Node rootNode) {

    private static final double CIRCLE_RADIUS = 3.0;
    private static final double LINE_WIDTH = 2.0;
    private static final int ICON_SIZE = 31;
    private static final int BUTTONS_SPACING = 10;
    private static final int GAP = 5;
    private static final double COLUMN_WIDTH_CIRCLE = 10.0;

    private static final int COL_TIME = 0;
    private static final int COL_CIRCLE = 1;
    private static final int COL_STATION = 2;
    private static final int COL_PLATFORM = 3;

    private static final String FORMAT_STOP_INTERMEDIATE = "%d arrêts, %d min";
    private static final String CALENDAR_FILENAME_FORMAT = "voyage_%s.ics";

    /**
     * Crée le graphe de scène et retourne une instance de DetailUI
     * contenant une référence à sa racine.
     *
     * @param journeyO valeur observable contenant le voyage dont les détails doivent être affichés
     *                 dans l'interface graphique.
     * @return retourne une instance de DetailUI contenant une référence à sa racine.
     */
    public static DetailUI create(ObservableValue<Journey> journeyO) {
        // 1) ScrollPane
        ScrollPane scroll = createScrollPane();

        // 2) “Aucun voyage”
        VBox noJourney = createNoJourneyMessage();

        // 3) Annotations + grille
        Pane annotations = createAnnotationsPane();
        DetailGridPane legsGrid = createLegsGrid(annotations);
        StackPane stepsPane = new StackPane(annotations, legsGrid);

        // 4) Boutons
        HBox buttons = createButtons(journeyO);

        // 5) Assemblage
        VBox detailBox = new VBox(GAP, stepsPane, buttons);
        scroll.setContent(new StackPane(noJourney, detailBox));

        // 6) Binding + actions
        setupBindingsAndActions(journeyO, noJourney, detailBox, legsGrid);

        return new DetailUI(scroll);
    }

    /**
     * Helper générique permettant de configurer un nœud et de le retourner en une seule expression.
     * Cela permet de créer et configurer un nœud JavaFX de manière concise et lisible.
     *
     * @param node le nœud à configurer
     * @param cfg  fonction de configuration à appliquer au nœud
     * @param <T>  type du nœud
     * @return le nœud configuré
     */
    private static <T extends Node> T with(T node, Consumer<T> cfg) {
        cfg.accept(node);
        return node;
    }

    /**
     * Crée le ScrollPane principal.
     *
     * @return le ScrollPane configuré
     */
    private static ScrollPane createScrollPane() {
        return with(new ScrollPane(), s -> {
            s.setId("detail");
            s.getStylesheets().add("detail.css");
            s.setFitToWidth(true);
        });
    }

    /**
     * Crée le message affiché quand aucun voyage n'est sélectionné.
     *
     * @return le VBox contenant le message
     */
    private static VBox createNoJourneyMessage() {
        return with(new VBox(new Text("Aucun voyage")), v -> {
            v.setId("no-journey");
            v.setFillWidth(true);
            v.setAlignment(Pos.CENTER);
        });
    }

    /**
     * Crée le panneau des annotations (lignes rouges).
     *
     * @return le Pane configuré
     */
    private static Pane createAnnotationsPane() {
        return with(new Pane(), p -> p.setId("annotations"));
    }

    /**
     * Crée la grille qui affiche les détails du voyage.
     *
     * @param annotations le panneau qui contiendra les lignes entre les étapes
     * @return la DetailGridPane configurée
     */
    private static DetailGridPane createLegsGrid(Pane annotations) {
        return with(new DetailGridPane(annotations), dg -> dg.setId("legs"));
    }

    /**
     * Crée les boutons pour la carte et le calendrier.
     *
     * @param journeyO observable contenant le voyage actuel
     * @return le HBox contenant les boutons
     */
    private static HBox createButtons(ObservableValue<Journey> journeyO) {
        Button btnMap = makeButton("Carte", "Carte");
        Button btnCalendar = makeButton("Calendrier", "Calendrier");

        btnMap.setOnAction(e -> openMap(journeyO.getValue()));
        btnCalendar.setOnAction(e -> exportCalendar(journeyO.getValue()));

        return with(new HBox(BUTTONS_SPACING, btnMap, btnCalendar), hb -> {
            hb.setId("buttons");
            hb.setAlignment(Pos.CENTER);
        });
    }

    /**
     * Configure les bindings et actions pour mettre à jour l'interface quand le voyage change.
     *
     * @param journeyO observable contenant le voyage actuel
     * @param noJourney panneau affiché quand aucun voyage n'est sélectionné
     * @param detailBox panneau contenant les détails du voyage
     * @param legsGrid grille affichant les étapes du voyage
     */
    private static void setupBindingsAndActions(ObservableValue<Journey> journeyO, VBox noJourney,
                                                VBox detailBox, DetailGridPane legsGrid) {
        Consumer<Journey> updateUI = j -> {
            boolean hasJourney = j != null;
            noJourney.setVisible(!hasJourney);
            detailBox.setVisible(hasJourney);
            if (hasJourney) legsGrid.updateLegs(j);
        };

        journeyO.subscribe(j -> updateUI.accept(j));
        updateUI.accept(journeyO.getValue());
    }

    /**
     * Méthode qui crée un bouton avec un id, contenant un texte.
     *
     * @param text texte représenté sur le bouton.
     * @param id   id du bouton.
     * @return retourne un bouton avec le texte et l'id donné en argument.
     */
    private static Button makeButton(String text, String id) {
        Button b = new Button(text);
        b.setId(id);
        return b;
    }

    /**
     * Ouvre dans un navigateur la carte avec le voyage donné en argument affiché.
     *
     * @param journey le voyage que l'on veut afficher sur la carte dans le navigateur.
     */
    private static void openMap(Journey journey) {
        if (journey == null) return;
        try {
            String geo = JourneyGeoJsonConverter.toGeoJson(journey);
            URI uri = new URI("https", "umap.osm.ch", "/fr/map",
                    "data=" + geo, null);
            getDesktop().browse(uri);
        } catch (Exception ignored) {
        }
    }

    /**
     * Exporte le voyage donné en argument au format iCalendar.
     *
     * @param journey le voyage que l'on veut exporter au format iCalendar.
     */
    private static void exportCalendar(Journey journey) {
        if (journey == null) return;
        try {
            FileChooser fc = new FileChooser();
            LocalDate date = journey.depTime().toLocalDate();
            fc.setInitialFileName(String.format(CALENDAR_FILENAME_FORMAT, date));
            File file = fc.showSaveDialog(null);
            if (file != null) {
                String ical = JourneyIcalConverter.toIcalendar(journey);
                Files.writeString(file.toPath(), ical);
            }
        } catch (Exception ignored) {}
    }

    /**
     * GridPane pour dessiner les points et les lignes rouges entre le départ et l'arrivée de chaque
     * étape en transport.
     */
    private static class DetailGridPane extends GridPane {
        private final List<Pair<Circle, Circle>> circlePairs = new ArrayList<>();
        private final Pane annotations;

        /**
         * Constructeur de DetailGridPane, qui configure les colonnes de ce GridPane.
         *
         * @param annotations le Pane qui doit contenir les lignes rouges qui relient les cercles.
         */
        DetailGridPane(Pane annotations) {
            this.annotations = annotations;
            configureColumns();
            setVgap(GAP);
            setHgap(GAP);
        }

        /**
         * Configure les différentes colonnes dans la GripPane.
         */
        private void configureColumns() {
            ColumnConstraints col0 = new ColumnConstraints();
            col0.setHalignment(HPos.RIGHT);

            ColumnConstraints col1 = new ColumnConstraints(COLUMN_WIDTH_CIRCLE);
            col1.setHalignment(HPos.CENTER);

            ColumnConstraints col2 = new ColumnConstraints();
            col2.setHgrow(Priority.ALWAYS);

            getColumnConstraints().addAll(col0, col1, col2, col2);
        }

        /**
         * Actualise les étapes présentes dans le GridPane selon le voyage donné en argument.
         *
         * @param journey voyage qui soit être affiché dans le DetailGridPane.
         */
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

        /**
         * Ajoute un trajet à pied au DetailGridPane, à la ligne souhaitée.
         *
         * @param foot le trajet à pied que l'on veut ajouter à la DetailGridPane.
         * @param row  numéro de la ligne de la DetailGridPane sur laquelle va être ajouté
         *             le trajet à pied.
         * @return retourne le numéro de la prochaine ligne du DetailGridPane.
         */
        private int addFootLeg(Foot foot, int row) {
            Text text = new Text(FormatterFr.formatLeg(foot));
            add(text, 2, row, 2, 1);
            return row + 1;
        }

        /**
         * Ajoute un arrêt de départ ou d'arrivé d'un trajet en transport au DetailGridPane, avec
         * l'heure de départ, le cercle, le nom de la station et de la plateforme s'il y en a une.
         *
         * @param tx          L'étape en transport à laquelle correspond l'arrêt que l'on veut ajouter.
         * @param isDeparture valeur booléenne pour savoir s'il s'agit de l'arrêt de départ
         *                    ou d'arrivé de l'étape en transport que l'on veut ajouter.
         * @param circle      le cercle que l'on veut ajouter dans le DetailGridPane.
         * @param row         la ligne à laquelle on ajoute cet arrêt.
         * @return retourne le numéro de la prochaine ligne.
         */
        private int addStopRow(Transport tx, boolean isDeparture, Circle circle, int row) {
            // prend le bon arrêt et la bonne date
            LocalDateTime time = isDeparture ? tx.depTime() : tx.arrTime();
            Stop stop = isDeparture ? tx.depStop() : tx.arrStop();

            add(createStopText(FormatterFr.formatTime(time), isDeparture), COL_TIME, row);
            add(circle, COL_CIRCLE, row);
            add(new Text(stop.name()), COL_STATION, row);

            String platform = FormatterFr.formatPlatformName(stop);
            if (!platform.isEmpty()) {
                add(createStopText(platform, isDeparture), COL_PLATFORM, row);
            }
            return row + 1;
        }

        /**
         * Crée le texte pour les arrêts de départ et d'arrivée des étapes en transport.
         *
         * @param content le contenu du Texte que la methode doit créer.
         * @param isDeparture valeur booléenne pour savoir s'il s'agit de l'arrêt de départ,
         *                    pour donner au texte le style voulu.
         * @return retourne le texte représentant l'arrêt.
         */
        private Text createStopText(String content, boolean isDeparture) {
            Text t = new Text(content);
            if (isDeparture) t.getStyleClass().add("departure");
            return t;
        }

        /**
         * Ajoute une étape en transport au DetailGridPane, à la ligne souhaitée.
         *
         * @param tx  l'étape en transport que l'on veut ajouter.
         * @param row la ligne du DetailGridPane à laquelle on veut ajouter cette étape.
         * @return retourne le numéro de la prochaine ligne,
         * après la dernière ligne utilisée pour ce voyage.
         */
        private int addTransportLeg(Transport tx, int row) {
            // Créer les cercles ensemble
            Pair<Circle, Circle> circles = createCirclePair();
            circlePairs.add(circles);

            // Ajouter départ, icône, arrêts intermédiaires et arrivée
            row = addStopRow(tx, true, circles.getKey(), row);
            row = addTransportInfo(tx, row);
            return addStopRow(tx, false, circles.getValue(), row);
        }

        /**
         * Crée une paire de cercles pour le départ et l'arrivée.
         *
         * @return une paire contenant le cercle de départ et d'arrivée
         */
        private Pair<Circle, Circle> createCirclePair() {
            return new Pair<>(createCircle(), createCircle());
        }

        /**
         * Ajoute les informations de transport (icône, destination, arrêts intermédiaires).
         *
         * @param tx l'étape de transport
         * @param row la ligne de départ
         * @return la prochaine ligne disponible
         */
        private int addTransportInfo(Transport tx, int row) {
            row = addIconAndDestination(tx, row);
            return addIntermediateStops(tx, row);
        }

        /**
         * Ajoute l'icon du transport utilisé pour cette étape ainsi que la direction
         * de la course empruntée.
         *
         * @param tx l'étape en transport.
         * @param row la ligne à laquelle on veut ajouter les éléments.
         * @return retourne le numéro de la ligne suivant celle utilisée.
         */
        private int addIconAndDestination(Transport tx, int row) {
            ImageView icon = createVehicleIcon(tx.vehicle());
            add(icon, 0, row, 1, tx.intermediateStops().isEmpty() ? 1 : 2);
            add(new Text(FormatterFr.formatRouteDestination(tx)),
                    2, row, 2, 1);
            return row + 1;
        }

        /**
         * Crée l'élément dépliant contenant les arrêts intermédiaires de l'étape en transport
         * donnée en argument à la ligne souhaitée.
         *
         * @param tx  l'étape en transport dont on veut les arrêts intermédiaires.
         * @param row la ligne à laquelle on veut ajouter le menu déroulant.
         * @return retourne le numéro de la ligne suivant celle utilisée pour cet élément dépliant.
         */
        private int addIntermediateStops(Transport tx, int row) {
            if (tx.intermediateStops().isEmpty()) return row;

            int stopCount = tx.intermediateStops().size();
            long duration = tx.duration().toMinutes();

            TitledPane content = new TitledPane(
                    String.format(FORMAT_STOP_INTERMEDIATE, stopCount, duration),
                    buildIntermediateGrid(tx.intermediateStops())
            );

            add(with(new Accordion(), a -> {
                a.setId("intermediate");
                a.getPanes().add(content);
            }), 2, row, 1, 1);

            return row + 1;
        }

        /**
         * Crée un ImageView avec l'icon du véhicule donné en argument
         *
         * @param v le véhicule dont on veut l'icon.
         * @return retourne une ImageView de l'icon du véhicule passé en argument.
         */
        private ImageView createVehicleIcon(Vehicle v) {
            return with(new ImageView(VehicleIcons.iconFor(v)), iv -> {
                iv.setFitWidth(ICON_SIZE);
                iv.setFitHeight(ICON_SIZE);
                iv.setPreserveRatio(true);
                iv.setSmooth(true);
            });
        }

        /**
         * Crée un cercle de rayon CIRCLE_RADIUS et de couleur noire.
         *
         * @return un cercle de rayon CIRCLE_RADIUS et de couleur noire.
         */
        private Circle createCircle() {
            return with(new Circle(CIRCLE_RADIUS), c -> c.setFill(Color.BLACK));
        }

        /**
         * Crée un GridPane qui est l'élément dépliant contenant la liste des arrêts intermédiaires
         * de l'étape en transport.
         *
         * @param stops la liste des arrêts intermédiaires de l'étape en transport
         *              que l'on veut ajouter.
         * @return retourne un GridPane qui est cet élément dépliant avec la liste
         * des arrêts intermédiaires.
         */
        private GridPane buildIntermediateGrid(List<Leg.IntermediateStop> stops) {
            GridPane grid = new GridPane();
            grid.setId("intermediate-stops");
            grid.getStyleClass().add("intermediate-stops");
            grid.setHgap(GAP);

            IntStream.range(0, stops.size()).forEach(row -> {
                Leg.IntermediateStop stop = stops.get(row);
                grid.add(new Text(FormatterFr.formatTime(stop.arrTime())), 0, row);
                grid.add(new Text(FormatterFr.formatTime(stop.depTime())), 1, row);
                grid.add(new Text(stop.stop().name()), 2, row);
            });

            return grid;
        }

        /**
         * Redéfinition de layoutChildren, qui utiliser la position des cercles pour déterminer
         * celle des lignes rouges, et créer des lignes reliant les centres des cercles.
         */
        @Override
        protected void layoutChildren() {
            super.layoutChildren();
            List<Line> lines = createConnectionLines();
            annotations.getChildren().setAll(lines);
            annotations.getParent().requestLayout();
        }

        /**
         * Crée les lignes rouges reliant les paires de cercles de départ et d'arrivée.
         * Ces lignes représentent les trajets en transport entre deux arrêts.
         *
         * @return liste des lignes à afficher dans le panneau d'annotations
         */
        private List<Line> createConnectionLines() {
            return circlePairs.stream()
                    .map(this::createConnectionLine)
                    .toList();
        }

        /**
         * Crée une ligne de connexion entre deux cercles.
         *
         * @param circlePair la paire de cercles à connecter
         * @return la ligne créée
         */
        private Line createConnectionLine(Pair<Circle, Circle> circlePair) {
            Circle dep = circlePair.getKey();
            Circle arr = circlePair.getValue();
            Bounds depBounds = dep.getBoundsInParent();
            Bounds arrBounds = arr.getBoundsInParent();

            Line line = new Line(
                    depBounds.getCenterX(), depBounds.getMinY() + depBounds.getHeight() / 2,
                    arrBounds.getCenterX(), arrBounds.getMinY() + arrBounds.getHeight() / 2
            );

            line.setStrokeWidth(LINE_WIDTH);
            line.setStroke(Color.RED);

            return line;
        }
    }
}
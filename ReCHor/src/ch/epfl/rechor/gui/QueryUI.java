package ch.epfl.rechor.gui;

import ch.epfl.rechor.StopIndex;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.converter.LocalTimeStringConverter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;


/**
 * Représente l'interface de requête, c'est à dire la partie de l'interface graphique
 * qui permet à l'utilisateur de choisir les arrêts de départ et d'arrivée,
 * et la date/heure de voyage désiré.
 *
 * @author Antoine Lepin (390950)
 * @author Darius Giannoli (380759)
 *
 * @param rootNode le nœud JavaFX à la racine de son graphe de scène.
 * @param depStopO une valeur observable contenant le nom de l'arrêt de départ.
 * @param arrStopO une valeur observable contenant le nom de l'arrêt d'arrivée.
 * @param dateO une valeur observable contenant la date de voyage.
 * @param timeO une valeur observable contenant l'heure de voyage.
 */
public record QueryUI(
        Node rootNode,
        ObservableValue<String> depStopO,
        ObservableValue<String> arrStopO,
        ObservableValue<LocalDate> dateO,
        ObservableValue<LocalTime> timeO
) {
    private static final String CSS_PATH = "query.css";
    private static final int SPACING = 5;


    /**
     * Construit le graphe de scène pour la recherche de voyages, avec les champs pour choisir
     * les arrêts de départ et d'arrivée ainsi que la date et l'heure souhaitée pour le trajet.
     *
     * @param index un StopIndex, à donner aux StopField pour proposer les arrêts.
     * @return retourne une instance de QueryUI contenant le nœud JavaFX qui se trouve à sa racine.
     */
    public static QueryUI create(StopIndex index) {
        VBox root = new VBox(SPACING);
        root.getStylesheets().add(CSS_PATH);

        // Champs d'arrêts (départ/arrivée)
        StopField depField = setupStopField(index, "depStop", "Nom de l'arrêt de départ");
        StopField arrField = setupStopField(index, "arrStop", "Nom de l'arrêt d'arrivée");

        // Bouton d'échange
        Button swapButton = createSwapButton(depField, arrField);

        HBox topo = new HBox(SPACING,
                new Label("Départ\u202f:"), depField.textField(),
                swapButton,
                new Label("Arrivée\u202f:"), arrField.textField()
        );

        // Sélecteurs de date et heure
        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setId("date");

        TextFormatter<LocalTime> timeFormatter = createTimeFormatter();
        TextField timeField = new TextField();
        timeField.setId("time");
        timeField.setTextFormatter(timeFormatter);

        HBox bottom = new HBox(SPACING,
                new Label("Date\u202f:"), datePicker,
                new Label("Heure\u202f:"), timeField
        );

        root.getChildren().addAll(topo, bottom);

        return new QueryUI(
                root,
                depField.stopO(),
                arrField.stopO(),
                datePicker.valueProperty(),
                timeFormatter.valueProperty());
    }

    /**
     * Permet de créer un StopField présent dans l'interface de requète pour entrer
     * l'arrêt de départ ou d'arrivée du voyage.
     *
     * @param index un StopIndex, à donner aux StopField pour proposer les arrêts.
     * @param id l'id du StopField.
     * @param prompt le texte à afficher par défaut dans le champ lorsqu'il est vide.
     * @return retourne un StopField avec les caractéristiques passées en argument.
     */
    private static StopField setupStopField(StopIndex index, String id, String prompt) {
        StopField field = StopField.create(index);
        field.textField().setId(id);
        field.textField().setPromptText(prompt);
        return field;
    }

    /**
     * Permet de créer le bouton pour échanger l'arrêt de départ et d'arrivée dans les StopField.
     *
     * @param dep le StopField pour l'arrêt de départ.
     * @param arr le StopField pour l'arrêt d'arrivée.
     * @return retourne le bouton pour échanger l'arrêt de départ et d'arrivée dans les StopField.
     */
    private static Button createSwapButton(StopField dep, StopField arr) {
        Button swap = new Button("⟷");
        swap.setOnAction(e -> {
            String depText = dep.textField().getText();
            String arrText = arr.textField().getText();
            arr.setTo(depText);
            dep.setTo(arrText);
        });
        return swap;
    }

    /**
     * Crée un TextFormatter pour le format de l'heure de départ voulue, entrée dans le champ.
     *
     * @return retourne une TexteFormatter pour une LocalDate qui est l'heure de départ présente
     * dans le champ prévu pour.
     */
    private static TextFormatter<LocalTime> createTimeFormatter() {
        DateTimeFormatter displayFormat = DateTimeFormatter.ofPattern("HH:mm");
        return new TextFormatter<>(
                new LocalTimeStringConverter(displayFormat, null),
                LocalTime.now()
        );
    }
}

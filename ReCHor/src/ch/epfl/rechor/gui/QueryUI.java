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
public record QueryUI(Node rootNode, ObservableValue<String> depStopO,
        ObservableValue<String> arrStopO, ObservableValue<LocalDate> dateO,
        ObservableValue<LocalTime> timeO) {
    // Field IDs
    private static final String DEP_STOP_ID = "depStop";
    private static final String ARR_STOP_ID = "arrStop";
    private static final String DATE_ID = "date";
    private static final String TIME_ID = "time";

    // Placeholder texts
    private static final String DEP_STOP_PROMPT = "Nom de l'arrêt de départ";
    private static final String ARR_STOP_PROMPT = "Nom de l'arrêt d'arrivée";

    // Labels
    private static final String DEP_LABEL = "Départ";
    private static final String ARR_LABEL = "Arrivée";
    private static final String DATE_LABEL = "Date";
    private static final String TIME_LABEL = "Heure";

    // Format patterns
    private static final String TIME_FORMAT_WITH_ZEROS = "HH:mm";
    private static final String TIME_FORMAT_WITHOUT_ZEROS = "H:mm";

    // Constantes
    private static final String SWAP_BUTTON_SYMBOL = "⟷";
    private static final String STYLESHEET_PATH = "query.css";
    private static final int SPACING = 5;
    private static final String THIN_SPACE_COLON = "\u202f:";

    /**
     * Construit le graphe de scène pour la recherche de voyages, avec les champs pour choisir
     * les arrêts de départ et d'arrivée ainsi que la date et l'heure souhaitée pour le trajet.
     *
     * @param index un StopIndex, à donner aux StopField pour proposer les arrêts.
     * @return retourne une instance de QueryUI contenant le nœud JavaFX qui se trouve à sa racine.
     */
    public static QueryUI create(StopIndex index) {
        // Création des champs d'arrêts
        StopField depField = createStopField(index, DEP_STOP_ID, DEP_STOP_PROMPT);
        StopField arrField = createStopField(index, ARR_STOP_ID, ARR_STOP_PROMPT);

        // Bouton d'échange
        Button swapButton = new Button(SWAP_BUTTON_SYMBOL);
        swapButton.setOnAction(e -> {
            String temp = arrField.textField().getText();
            arrField.setTo(depField.textField().getText());
            depField.setTo(temp);
        });

        // Configuration date et heure
        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setId(DATE_ID);

        TextFormatter<LocalTime> timeFormatter = new TextFormatter<>(
                new LocalTimeStringConverter(
                        DateTimeFormatter.ofPattern(TIME_FORMAT_WITH_ZEROS),
                        DateTimeFormatter.ofPattern(TIME_FORMAT_WITHOUT_ZEROS)),
                LocalTime.now());

        TextField timeField = new TextField();
        timeField.setId(TIME_ID);
        timeField.setTextFormatter(timeFormatter);

        // Construction de l'interface
        VBox root = new VBox(SPACING,
                new HBox(SPACING,
                        createLabeledControl(DEP_LABEL, depField.textField()),
                        swapButton,
                        createLabeledControl(ARR_LABEL, arrField.textField())),
                new HBox(SPACING,
                        createLabeledControl(DATE_LABEL, datePicker),
                        createLabeledControl(TIME_LABEL, timeField))
        );
        root.getStylesheets().add(STYLESHEET_PATH);

        return new QueryUI(root, depField.stopO(), arrField.stopO(),
                datePicker.valueProperty(), timeFormatter.valueProperty());
    }

    /**
     * Crée un champ d'arrêt configuré.
     */
    private static StopField createStopField(StopIndex index, String id, String prompt) {
        StopField field = StopField.create(index);
        field.textField().setId(id);
        field.textField().setPromptText(prompt);
        return field;
    }

    /**
     * Crée un contrôle avec label.
     */
    private static HBox createLabeledControl(String labelText, Node control) {
        return new HBox(SPACING, new Label(labelText + THIN_SPACE_COLON), control);
    }
}
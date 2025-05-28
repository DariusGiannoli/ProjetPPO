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
    // Identifiants des champs
    private static final String DEP_STOP_ID = "depStop";
    private static final String ARR_STOP_ID = "arrStop";

    // Textes d'indication
    private static final String DEP_STOP_PROMPT = "Nom de l'arrêt de départ";
    private static final String ARR_STOP_PROMPT = "Nom de l'arrêt d'arrivée";

    // Étiquettes
    private static final String THIN_SPACE_COLON = "\u202f:";
    private static final String DEP_LABEL = "Départ" + THIN_SPACE_COLON;
    private static final String ARR_LABEL = "Arrivée" + THIN_SPACE_COLON;
    private static final String DATE_LABEL = "Date" + THIN_SPACE_COLON;
    private static final String TIME_LABEL = "Heure" + THIN_SPACE_COLON;

    // Motifs de format
    private static final DateTimeFormatter TIME_FORMATTER_WITH_ZEROS =
            DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter TIME_FORMATTER_WITHOUT_ZEROS =
            DateTimeFormatter.ofPattern("H:mm");

    // Constantes
    private static final int SPACING = 5;

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
        Button swapButton = new Button("⟷");
        TextField depTextField = depField.textField();
        TextField arrTextField = arrField.textField();

        swapButton.setOnAction(e -> {
            String temp = arrTextField.getText();
            arrField.setTo(depTextField.getText());
            depField.setTo(temp);
        });

        // Configuration date et heure
        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setId("date");
        TextFormatter<LocalTime> timeFormatter = new TextFormatter<>(
                new LocalTimeStringConverter(TIME_FORMATTER_WITH_ZEROS, TIME_FORMATTER_WITHOUT_ZEROS),
                LocalTime.now());

        TextField timeField = new TextField();
        timeField.setId("time");
        timeField.setTextFormatter(timeFormatter);

        // Construction de l'interface
        VBox root = new VBox(SPACING,
                new HBox(SPACING,
                        createLabeledControl(DEP_LABEL, depTextField),
                        swapButton,
                        createLabeledControl(ARR_LABEL, arrTextField)),
                new HBox(SPACING,
                        createLabeledControl(DATE_LABEL, datePicker),
                        createLabeledControl(TIME_LABEL, timeField))
        );
        root.getStylesheets().add("query.css");

        return new QueryUI(root, depField.stopO(), arrField.stopO(),
                datePicker.valueProperty(), timeFormatter.valueProperty());
    }

    /**
     * Crée un champ d'arrêt configuré.
     */
    private static StopField createStopField(StopIndex index, String id, String prompt) {
        StopField field = StopField.create(index);
        TextField textField = field.textField();
        textField.setId(id);
        textField.setPromptText(prompt);
        return field;
    }

    /**
     * Crée un contrôle avec label.
     */
    private static HBox createLabeledControl(String labelText, Node control) {
        return new HBox(SPACING, new Label(labelText), control);
    }
}
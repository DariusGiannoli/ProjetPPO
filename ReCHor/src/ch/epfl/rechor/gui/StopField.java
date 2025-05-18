package ch.epfl.rechor.gui;

import ch.epfl.rechor.StopIndex;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Popup;

import java.util.List;

/**
 * Combinaison d'un TextField et d'un Popup pour sélectionner un arrêt.
 * La valeur observable stopO change lorsque le curseur quitte le champ textuel.
 *
 * @author Antoine Lepin (390950)
 * @author Darius Giannoli (380759)
 */
public record StopField(TextField textField, ObservableValue<String> stopO) {
    private static final int MAX_SUGGESTIONS = 30;
    private static final double LIST_MAX_HEIGHT = 240.0;

    /**
     * Crée un StopField donc le champ textuel et la fenêtre associés à l'index donné.
     *
     * @param index un StopIndex, à utiliser pour connaitre les arrêts à proposer.
     * @return retourne un StopField associé au StopIndex passé en argument.
     */
    public static StopField create(StopIndex index) {
        TextField textField = new TextField();
        ReadOnlyStringWrapper selected = new ReadOnlyStringWrapper("");

        // Configuration de la popup et de la liste d'arrêts
        ListView<String> list = new ListView<>();
        list.setFocusTraversable(false);
        list.setMaxHeight(LIST_MAX_HEIGHT);

        Popup popup = new Popup();
        popup.setHideOnEscape(false);
        popup.getContent().add(list);

        // Configuration des interactions
        configureKeyNavigation(textField, list);
        configureFocusHandling(textField, list, popup, selected, index);
        configureTextInput(textField, list, popup, index);

        return new StopField(textField, selected);
    }

    /**
     * Configure la navigation clavier avec les flèches dans la liste des arrêts proposés.
     */
    private static void configureKeyNavigation(TextField textField, ListView<String> list) {
        textField.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            var selModel = list.getSelectionModel();
            if (selModel.isEmpty()) return;

            if (e.getCode() == KeyCode.UP) {
                selModel.selectPrevious();
                list.scrollTo(selModel.getSelectedIndex());
                e.consume();
            } else if (e.getCode() == KeyCode.DOWN) {
                selModel.selectNext();
                list.scrollTo(selModel.getSelectedIndex());
                e.consume();
            }
        });
    }

    /**
     * Configure le comportement lors des changements de focus.
     */
    private static void configureFocusHandling(TextField textField, ListView<String> list,
                                               Popup popup, ReadOnlyStringWrapper selected,
                                               StopIndex index) {
        textField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (isFocused) {
                // Affiche la popup quand on gagne le focus
                updateSuggestions(list, index.stopsMatching(textField.getText(), MAX_SUGGESTIONS));
                showPopup(textField, popup);
            } else {
                // Valide la sélection quand on perd le focus
                String selectedItem = list.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    textField.setText(selectedItem);
                    selected.set(selectedItem);
                }
                popup.hide();
            }
        });
    }

    /**
     * Configure la mise à jour de la liste lors de la saisie de texte.
     */
    private static void configureTextInput(TextField textField, ListView<String> list,
                                           Popup popup, StopIndex index) {
        textField.textProperty().addListener((obs, oldText, newText) -> {
            if (popup.isShowing()) {
                updateSuggestions(list, index.stopsMatching(newText, MAX_SUGGESTIONS));
            }
        });
    }

    /**
     * Met à jour les suggestions dans la liste et sélectionne le premier élément.
     */
    private static void updateSuggestions(ListView<String> list, List<String> suggestions) {
        list.getItems().setAll(suggestions);
        if (!list.getItems().isEmpty()) {
            list.getSelectionModel().selectFirst();
            list.scrollTo(0);
        }
    }

    /**
     * Affiche la popup sous le champ de texte.
     */
    private static void showPopup(TextField textField, Popup popup) {
        var bounds = textField.localToScreen(textField.getBoundsInLocal());
        popup.setX(bounds.getMinX());
        popup.setY(bounds.getMaxY());
        if (!popup.isShowing()) {
            popup.show(textField.getScene().getWindow());
        }
    }

    /**
     * Force la valeur du TextField et de l'ObservableValue à être la chaine donnée en argument.
     *
     * @param stop la chaine à mettre dans textField et stopO.
     */
    public void setTo(String stop) {
        textField.setText(stop);
        // Nous savons que stopO est un ReadOnlyStringWrapper grâce à la factory method
        ((ReadOnlyStringWrapper) stopO).setValue(stop);
    }
}
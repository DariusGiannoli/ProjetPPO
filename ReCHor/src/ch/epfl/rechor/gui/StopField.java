package ch.epfl.rechor.gui;

import ch.epfl.rechor.StopIndex;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
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

        // Cache le modèle de sélection pour éviter des appels répétés
        MultipleSelectionModel<String> selModel = list.getSelectionModel();

        // Gestion de la navigation clavier
        textField.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (selModel.isEmpty()) return;

            KeyCode code = e.getCode();
            boolean needConsume = true;

            if (code == KeyCode.UP) {
                selModel.selectPrevious();
            } else if (code == KeyCode.DOWN) {
                selModel.selectNext();
            } else if (code == KeyCode.ENTER) {
                String selectedItem = selModel.getSelectedItem();
                if (selectedItem != null) {
                    textField.setText(selectedItem);
                    selected.set(selectedItem);
                }
                popup.hide();
            } else {
                needConsume = false;
            }

            if (needConsume) {
                list.scrollTo(selModel.getSelectedIndex());
                e.consume();
            }
        });

        // Gestion du focus
        textField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (isFocused) {
                updateSuggestions(list, index.stopsMatching(textField.getText(), MAX_SUGGESTIONS));
                showPopup(textField, popup);
            } else {
                String selectedItem = selModel.getSelectedItem();
                if (selectedItem != null) {
                    textField.setText(selectedItem);
                    selected.set(selectedItem);
                }
                popup.hide();
            }
        });

        // Mise à jour lors de la saisie
        textField.textProperty().addListener((obs, oldText, newText) -> {
            if (popup.isShowing()) {
                updateSuggestions(list, index.stopsMatching(newText, MAX_SUGGESTIONS));
                showPopup(textField, popup);
            }
        });

        return new StopField(textField, selected);
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
        Bounds bounds = textField.localToScreen(textField.getBoundsInLocal());
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
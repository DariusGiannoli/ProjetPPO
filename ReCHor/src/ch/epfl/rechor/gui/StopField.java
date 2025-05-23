package ch.epfl.rechor.gui;

import ch.epfl.rechor.StopIndex;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
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

        //Configure la navigation clavier avec les flèches dans la liste des arrêts proposés.
        textField.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            MultipleSelectionModel<String> selModel = list.getSelectionModel();
            if (selModel.isEmpty()) return;

            // Logique de navigation
            boolean handled = false;
            if (e.getCode() == KeyCode.UP) {
                selModel.selectPrevious();
                handled = true;
            } else if (e.getCode() == KeyCode.DOWN) {
                selModel.selectNext();
                handled = true;
            }

            if (handled) {
                list.scrollTo(selModel.getSelectedIndex());
                e.consume();
            }
        });

        //Configure le comportement lors des changements de focus.
        textField.focusedProperty().subscribe((isFocused) -> {
            if (isFocused) {
                // Affiche la popup avec les suggestions quand on gagne le focus
                updateAndShowSuggestions(textField, list, popup,
                        index.stopsMatching(textField.getText(), MAX_SUGGESTIONS));

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

        // Mise à jour lors de la saisie
        textField.textProperty().subscribe((newText) -> {
            // Ne met à jour que si la popup est visible pour éviter les calculs inutiles
            if (popup.isShowing()) {
                updateAndShowSuggestions(textField, list, popup,
                        index.stopsMatching(newText, MAX_SUGGESTIONS));
            }
        });

        return new StopField(textField, selected);
    }

    /**
     * Met à jour les suggestions dans la liste et affiche la popup sous le champ de texte.
     */
    private static void updateAndShowSuggestions(TextField textField, ListView<String> list,
                                                 Popup popup, List<String> suggestions) {
        // Met à jour les suggestions uniquement si elles ont changé
        ObservableList<String> listItems = list.getItems();
        if (!listItems.equals(suggestions)) {
            listItems.setAll(suggestions);
        }

        // Sélectionne le premier élément si la liste n'est pas vide
        if (!listItems.isEmpty()) {
            list.getSelectionModel().selectFirst();
            list.scrollTo(0);
        }

        // Affiche la popup uniquement si elle n'est pas déjà visible

            Bounds bounds = textField.localToScreen(textField.getBoundsInLocal());
            popup.setX(bounds.getMinX());
            popup.setY(bounds.getMaxY());
            popup.show(textField.getScene().getWindow());
    }

    /**
     * Force la valeur du TextField et de l'ObservableValue à être la chaine donnée en argument.
     *
     * @param stop la chaine à mettre dans textField et stopO.
     */
    public void setTo(String stop) {
        textField.setText(stop);
        // Cast sûr car stopO est créé comme ReadOnlyStringWrapper dans create()
        ((ReadOnlyStringWrapper) stopO).setValue(stop);
    }
}
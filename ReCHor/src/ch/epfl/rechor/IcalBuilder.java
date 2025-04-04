package ch.epfl.rechor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * Classe permettant de construire un événement au format iCalendar.
 * Ce builder permet d'ajouter des propriétés et de commencer/terminer des composants
 * (VCALENDAR, VEVENT) en respectant la norme (plis de ligne à 75 caractères max).
 *
 * @author Antoine Lepin (390950)
 * @author Darius Giannoli (380759)
 */
public final class IcalBuilder {

    /**
     * Enumération représentant les composants iCalendar utilisés.
     */
    public enum Component {
        /**L'objet iCalendar principal qui enveloppe l'ensemble de l'événement*/
        VCALENDAR,
        /**Un événement individuel contenu dans le calendrier*/
        VEVENT
    }

    /**
     * Enumération listant les noms de propriété iCalendar pris en charge.
     */
    public enum Name {
        /**Indique le début d'un composant*/
        BEGIN,
        /**Indique la fin d'un composant*/
        END,
        /**Identifiant du produit générant le fichier iCalendar*/
        PRODID,
        /**Version de la spécification iCalendar utilisée*/
        VERSION,
        /**Identifiant unique de l'événement*/
        UID,
        /**Date/heure de création ou de modification du fichier*/
        DTSTAMP,
        /**Date/heure de début d'un événement*/
        DTSTART,
        /**Date/heure de fin d'un événement*/
        DTEND,
        /**Titre ou résumé de l'événement*/
        SUMMARY,
        /**Description détaillée de l'événement*/
        DESCRIPTION
    }

    private static final String CRLF = "\r\n";
    private static final int FOLDING_OVERHEAD = 2; // Pour CRLF + espace
    private static final String CONTINUATION_SPACE = " ";
    private static final String COLON = ":";

    //Formatteur de date/heure pour iCalendar, sous la forme "yyyyMMdd'T'HHmmss"
    private static final DateTimeFormatter ICAL_DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

    // Constantes pour le pliage des lignes selon la norme iCalendar
    private static final int MAX_LINE_LENGTH = 75;
    private static final int CONTINUATION_LENGTH = 74;

    private final ArrayList<Component> begunComponents = new ArrayList<>();
    private final StringBuilder icalString = new StringBuilder();

    /**
     * Méthode dédiée au pliage d'une ligne conformément à la norme iCalendar.
     *
     * @param line la ligne à plier
     * @return la ligne pliée, chaque segment terminé par CRLF
     */
    private String foldLine(String line) {
        int lineLength = line.length();
        if (lineLength <= MAX_LINE_LENGTH) {
            return line + CRLF;
        }

        StringBuilder folded = new StringBuilder(lineLength
                + (lineLength / MAX_LINE_LENGTH + 1) * FOLDING_OVERHEAD);

        int index = 0;
        while (index < lineLength) {
            int segmentLength = (index == 0) ? MAX_LINE_LENGTH : CONTINUATION_LENGTH;
            int end = Math.min(index + segmentLength, lineLength);
            if (index > 0) {
                folded.append(CONTINUATION_SPACE);
            }
            folded.append(line, index, end).append(CRLF);
            index += segmentLength;
        }
        return folded.toString();
    }
    /**
     * Ajoute une ligne iCalendar au format "name:value",
     * en pliant la ligne si nécessaire.
     *
     * @param name le nom (propriété) iCalendar
     * @param value la valeur associée
     * @return la chaîne pliée correspondant à la propriété, terminée par CRLF
     */
    public String textAdd(String name, String value) {
        String line = name + COLON + value;
        return foldLine(line);
    }

    /**
     * Ajoute au document iCalendar une propriété dont la valeur est une chaîne de caractères.
     *
     * @param name le nom de la propriété iCalendar
     * @param value la valeur associée
     * @return this pour chaîner les appels
     */
    public IcalBuilder add(Name name, String value) {
        icalString.append(textAdd(name.toString(), value));
        return this;
    }

    /**
     * Ajoute au document iCalendar une propriété représentant
     * une date/heure au format "yyyyMMdd'T'HHmmss".
     *
     * @param name la propriété iCalendar
     * @param dateTime l'instant à formater
     * @return this pour chaîner les appels
     */
    public IcalBuilder add(Name name, LocalDateTime dateTime) {
        String formatted = dateTime.format(ICAL_DATE_TIME_FORMAT);
        icalString.append(textAdd(name.toString(), formatted));
        return this;
    }

    /**
     * Commence un nouveau composant iCalendar.
     *
     * @param component le composant iCalendar à commencer
     * @return this pour chaîner les appels
     */
    public IcalBuilder begin(Component component) {
        add(Name.BEGIN, component.toString());
        begunComponents.add(component);
        return this;
    }

    /**
     * Termine le dernier composant iCalendar ouvert.
     *
     * @return this pour chaîner les appels
     * @throws IllegalArgumentException si aucun composant n’a été préalablement ouvert
     */
    public IcalBuilder end(){
        Preconditions.checkArgument(!begunComponents.isEmpty());
        int lastIndex = begunComponents.size() - 1;
        add(Name.END, begunComponents.get(lastIndex).toString());
        begunComponents.remove(lastIndex);
        return this;
    }

    /**
     * Construit la chaîne iCalendar finale
     * représentant l’ensemble des composants et propriétés ajoutés.
     *
     * @return la représentation iCalendar complète
     * @throws IllegalArgumentException si des composants sont encore ouverts
     */
    public String build(){
        Preconditions.checkArgument(begunComponents.isEmpty());
        return icalString.toString();
    }
}
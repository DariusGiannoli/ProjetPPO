package ch.epfl.rechor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * Classe permettant de construire un événement au format iCalendar.
 * Ce builder permet d'ajouter des propriétés et de commencer/terminer des composants
 * (VCALENDAR, VEVENT) en respectant la norme (plis de ligne à 75 caractères max).
 * Exemple d'utilisation :
 * <pre>
 *   IcalBuilder builder = new IcalBuilder();
 *   builder.begin(IcalBuilder.Component.VCALENDAR)
 *          .add(IcalBuilder.Name.VERSION, "2.0")
 *          ... // autres propriétés
 *          .end();
 *   String ical = builder.build();
 * </pre>
 *
 * @author Antoine Lepin (390950)
 * @author Darius Giannoli (380759)
 */

public final class IcalBuilder {

    /**
     * Enumération représentant les composants iCalendar utilisés.
     */
    public enum Component {
        VCALENDAR,
        VEVENT
    }

    /**
     * Enumération listant les noms de propriété iCalendar pris en charge.
     */
    public enum Name {
        BEGIN,
        END,
        PRODID,
        VERSION,
        UID,
        DTSTAMP,
        DTSTART,
        DTEND,
        SUMMARY,
        DESCRIPTION
    }

    private static final String CRLF = "\r\n";
    //Formatteur de date/heure pour iCalendar, sous la forme "yyyyMMdd'T'HHmmss"
    private static final DateTimeFormatter ICAL_DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
    private final ArrayList<Component> begunComponents = new ArrayList<>();
    private final StringBuilder icalString = new StringBuilder();

    /**
     * Ajoute une ligne iCalendar au format "name:value", en pliant la ligne si elle dépasse 75 caractères.
     *
     * @param name le nom (propriété) iCalendar
     * @param value la valeur associée
     * @return la chaîne pliée correspondant à la propriété iCalendar, terminée par CRLF
     */
    public String textAdd(String name, String value) {
        String line = name + ":" + value;
        StringBuilder sb = new StringBuilder();

        for (int index = 0; index < line.length(); index += (index == 0 ? 75 : 74)) {
            int end = Math.min(index == 0 ? 75 : index + 74, line.length());
            if (index == 0) {
                sb.append(line, index, end).append(CRLF);
            } else {
                sb.append(" ").append(line, index, end).append(CRLF);
            }
        }
        return sb.toString();
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
     * Ajoute au document iCalendar une propriété représentant une date/heure au format "yyyyMMdd'T'HHmmss".
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
     * Construit la chaîne iCalendar finale représentant l’ensemble des composants et propriétés ajoutés.
     *
     * @return la représentation iCalendar complète
     * @throws IllegalArgumentException si des composants sont encore ouverts
     */
    public String build(){
        Preconditions.checkArgument(begunComponents.isEmpty());
        return icalString.toString();
    }
}
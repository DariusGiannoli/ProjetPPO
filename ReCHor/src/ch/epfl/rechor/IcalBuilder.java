package ch.epfl.rechor;

import java.security.cert.CRL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * Classe permettant de construire un événement au format iCalendar (iCalendar builder)
 *
 * @author Antoine Lepin (390950)
 * @author Darius Giannoli (380759)
 */

public final class IcalBuilder {

    /**
     * Énumération représentant les composants iCalendar de base
     * utilisés dans ce projet.
     */

    public enum Component {
        VCALENDAR,
        VEVENT;
    }

    /**
     * Énumération listant les principaux noms (propriétés) iCalendar
     * pris en charge par ce builder.
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
        DESCRIPTION;
    }

    private static final String CRLF = "\r\n";

    private ArrayList<Component> begunComponents = new ArrayList<>();

    private final StringBuilder ICalString = new StringBuilder();

    //Formatteur de date/heure pour iCalendar, sous la forme "yyyyMMdd'T'HHmmss"
    private static final DateTimeFormatter ICAL_DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

    /**
     * Ajoute une ligne iCalendar ayant le format "name:value", en pliant la ligne si elle dépasse 75 caractères
     * @param name  le nom (propriété) iCalendar
     * @param str2  la valeur associée à ce nom
     * @return la chaîne pliée correspondant à la propriété iCalendar, terminée par CRLF
     */
    public String textAdd(String name, String str2) {

        String line = name + ":" + str2;
        StringBuilder sb = new StringBuilder();

        //opérateur ternaire, en gros : si index c'est 0 ( donc première itération) on augmente de 75 et puis après que
        //de 74 car l'espace est le 75 caractère
        for (int index = 0; index < line.length(); index += (index == 0 ? 75 : 74)) {
            if (index == 0) {
                // Vérification de précaution pour éviter de dépasser
                int end = Math.min(75, line.length());
                sb.append(line.substring(index, end)).append(CRLF);
            } else {
                //idem
                int end = Math.min(index + 74, line.length());
                sb.append(" ").append(line.substring(index, end)).append(CRLF);
            }
        }
        return sb.toString();
    }

    /**
     * Ajoute au document iCalendar en cours une propriété dont la valeur est une chaîne de caractères simple
     * @param name
     * @param value
     * @return this, pour chaîner les appels
     */

    public IcalBuilder add(Name name, String value) {
        String nameString = name.toString();
        ICalString.append(textAdd(nameString, value));
        return this;
    }

    /**
     * Ajoute au document iCalendar en cours une propriété représentant
     * une date/heure, au format iCalendar "yyyyMMdd'T'HHmmss"
     * @param name  la propriété iCalendar
     * @param dateTime  l'instant à formater
     * @return  this, pour chaîner les appels
     */

    public IcalBuilder add(Name name, LocalDateTime dateTime) {
        String formatted = dateTime.format(ICAL_DATE_TIME_FORMAT);
        ICalString.append(textAdd(name.toString(), formatted));
        return this;
    }

    /**
     * Commence un nouveau composant iCalendar
     * @param component le composant iCalendar à commencer
     * @return this, pour chaîner les appels
     */
    public IcalBuilder begin(Component component) {
        String componentString = component.toString();
        //ICalString.append("BEGIN:").append(componentString).append(CRLF);
        add(Name.BEGIN, componentString);
        begunComponents.add(component);
        return this;
    }

    /**
     * Termine le dernier composant iCalendar ouvert
     * @return this, pour chaîner les appels
     * @throws IllegalArgumentException si aucun composant n’a été préalablement ouvert
     */
    public IcalBuilder end(){
        Preconditions.checkArgument(!begunComponents.isEmpty());
        int listSize = begunComponents.size();
        //ICalString.append("END:").append((begunComponents.get(listSize-1)).toString()).append(CRLF);
        add(Name.END, begunComponents.get(listSize-1).toString());
        begunComponents.remove(listSize - 1);
        return this;
    }

    /**
     * Construit la chaîne iCalendar finale représentant l’ensemble des
     * composants et propriétés ajoutés jusque-là
     * @return la représentation iCalendar complète, prête à être utilisée
     * @throws IllegalArgumentException si des composants sont encore ouverts
     */
    public String build(){
        Preconditions.checkArgument(begunComponents.isEmpty());
        return ICalString.toString();
    }


}

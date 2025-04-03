package ch.epfl.rechor;

import ch.epfl.rechor.journey.Journey;
import ch.epfl.rechor.journey.Stop;
import ch.epfl.rechor.journey.Journey.Leg.Foot;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Classe utilitaire finale pour formatter les informations relatives aux trajets.
 * Fournit des méthodes statiques pour obtenir la représentation textuelle de durées,
 * d'heures, de noms de plateformes, et d'étapes de trajet.
 *
 * @author Antoine Lepin (390950)
 * @author Darius Giannoli (380759)
 */
public final class FormatterFr {

    // Constantes pour les textes de formatage
    private static final String MIN_SUFFIX = " min";
    private static final String HOUR_SEPARATOR = " h ";
    private static final String HOUR = "h";
    private static final String VOIE_PREFIX = "voie ";
    private static final String QUAI_PREFIX = "quai ";
    private static final String CHANGEMENT = "changement";
    private static final String TRAJET_A_PIED = "trajet à pied";
    private static final String DIRECTION = " Direction ";
    private static final String ARROW = " → ";
    private static final String ARRIVAL_PREFIX = " (arr. ";
    private static final String LEFT_PARENTHESIS = " (";
    private static final String RIGHT_PARENTHESIS = ")";
    private static final String EMPTY_STRING = "";
    private static final String SPACE = " ";

    // Tailles initiales pour StringBuilder selon les différentes utilisations
    private static final int SB_CAPACITY_SMALL = 32;    // Pour formatDuration, formatTime
    private static final int SB_CAPACITY_MEDIUM = 64;   // Pour formatLeg(Foot)
    private static final int SB_CAPACITY_LARGE = 128;   // Pour formatLeg(Transport)

    // Format des minutes avec deux chiffres
    private static final String MINUTE_FORMAT = "%02d";

    /** Constructeur privé pour empêcher l'instanciation de cette classe utilitaire.*/
    private FormatterFr() {}

    /**
     * Formate une durée en chaîne de caractères.
     *
     * @param duration la durée à formatter.
     * @return une chaîne représentant la durée au format "X min" ou "Y h Z min".
     */
    public static String formatDuration(Duration duration) {
        final long totalMinutes = duration.toMinutes();
        final long hours = totalMinutes / 60;
        final long minutes = totalMinutes % 60;

        StringBuilder sb = new StringBuilder(SB_CAPACITY_SMALL);
        sb.append(hours == 0 ? minutes : hours + HOUR_SEPARATOR + minutes);

        return sb.append(MIN_SUFFIX).toString();
    }

    /**
     * Formate une heure donnée sous le format "HHhmm", où HH représente l'heure (0-23)
     * et mm représente les minutes sur deux chiffres.
     *
     * @param dateTime la date et heure à formater.
     * @return une chaîne représentant l'heure au format "HHhmm".
     */
    public static String formatTime(LocalDateTime dateTime) {
        StringBuilder sb = new StringBuilder(SB_CAPACITY_SMALL);

        return sb.append(dateTime.getHour())
                .append(HOUR)
                .append(String.format(MINUTE_FORMAT, dateTime.getMinute()))
                .toString();
    }

    /**
     * Formate le nom de la plateforme (voie ou quai) d'un arrêt.
     *
     * @param stop l'arrêt dont la plateforme doit être formatée.
     * @return une chaîne vide si aucun nom de plateforme n'est défini,
     *         sinon "voie <nom>" si le nom commence par un chiffre, ou "quai <nom>" sinon.
     */
    public static String formatPlatformName(Stop stop) {
        final String platformName = stop.platformName();

        if (platformName == null || platformName.isEmpty()) {
            return EMPTY_STRING;
        }

        String prefix = Character.isDigit(platformName.charAt(0)) ? VOIE_PREFIX : QUAI_PREFIX;
        return prefix + platformName;
    }

    /**
     * Formate une étape à pied en indiquant son type et sa durée.
     *
     * @param footLeg une étape effectuée à pied.
     * @return une chaîne décrivant l'étape,
     * par exemple "changement (5 min)" ou "trajet à pied (3 min)".
     */
    public static String formatLeg(Foot footLeg) {
        final String description = footLeg.isTransfer() ? CHANGEMENT : TRAJET_A_PIED;
        final String durationStr = formatDuration(footLeg.duration());

        StringBuilder sb = new StringBuilder(SB_CAPACITY_MEDIUM);

        return sb.append(description)
                .append(LEFT_PARENTHESIS)
                .append(durationStr)
                .append(RIGHT_PARENTHESIS)
                .toString();
    }

    /**
     * Formate une étape en transport public.
     * La chaîne résultante est construite de la manière suivante :
     * "HHhmm NomGareDepart (voie/quai Nom) → NomGareArrivee (arr. HHhmm [voie/quai Nom])".
     *
     * @param leg une étape en transport public.
     * @return une chaîne représentant l'étape en transport public.
     */
    public static String formatLeg(Journey.Leg.Transport leg) {
        StringBuilder sb = new StringBuilder(SB_CAPACITY_LARGE);

        // Ajout des informations de départ
        sb.append(formatTime(leg.depTime()))
                .append(SPACE)
                .append(leg.depStop().name());

        // Ajout de la plateforme de départ si disponible
        String depPlatform = formatPlatformName(leg.depStop());
        if (!depPlatform.isEmpty()) {
            sb.append(LEFT_PARENTHESIS)
                    .append(depPlatform)
                    .append(RIGHT_PARENTHESIS);
        }

        // Ajout de la flèche et de l'arrêt d'arrivée
        sb.append(ARROW)
                .append(leg.arrStop().name())
                .append(ARRIVAL_PREFIX)
                .append(formatTime(leg.arrTime()));

        // Ajout de la plateforme d'arrivée si disponible
        String arrPlatform = formatPlatformName(leg.arrStop());
        if (!arrPlatform.isEmpty()) {
            sb.append(SPACE)
                    .append(arrPlatform);
        }

        return sb.append(RIGHT_PARENTHESIS).toString();
    }

    /**
     * Formate la ligne et le sens de parcours d'une étape en transport public.
     * Le format résultant est "NomLigne Direction Destination".
     *
     * @param transportLeg une étape en transport public.
     * @return une chaîne représentant la ligne et la destination,
     * par exemple "IR 15 Direction Luzern".
     */
    public static String formatRouteDestination(Journey.Leg.Transport transportLeg) {
        final String route = transportLeg.route();
        final String destination = transportLeg.destination();

        StringBuilder sb = new StringBuilder(SB_CAPACITY_MEDIUM);

        return sb.append(route)
                .append(DIRECTION)
                .append(destination)
                .toString();
    }
}
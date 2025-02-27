package ch.epfl.rechor.timetable;

import java.time.LocalDate;

/**
 * @author Antoine Lepin (390950)
 * @author Darius Giannoli (380759)
 *
 * L'interface TimeTable représente un horaire de transport public.
 * Elle regroupe l'ensemble des composants indexés de l'horaire et offre des méthodes
 * pour accéder aux données actives pour une date donnée.
 */
public interface TimeTable{

    /**
     * Retourne les gares indexées de l'horaire.
     *
     * @return les stations de l'horaire
     */
    Stations stations();

    /**
     * Retourne les noms alternatifs indexés des gares de l'horaire.
     *
     * @return les alias des stations
     */
    StationAliases stationAliases();

    /**
     * Retourne les voies/quais indexées de l'horaire.
     *
     * @return les plateformes de l'horaire
     */
    Platforms platforms();

    /**
     * Retourne les lignes indexées de l'horaire.
     *
     * @return les lignes de transport public
     */
    Routes routes();

    /**
     * Retourne les changements indexés de l'horaire.
     *
     * @return les transferts de l'horaire
     */
    Transfers transfers();

    /**
     * Retourne les courses indexées de l'horaire actives le jour donné.
     *
     * @param date la date du voyage
     * @return les courses actives à la date indiquée
     */
    Trips tripsFor(LocalDate date);

    /**
     * Retourne les liaisons indexées de l'horaire actives le jour donné.
     *
     * @param date la date du voyage
     * @return les liaisons actives à la date indiquée
     */
    Connections connectionsFor(LocalDate date);

    /**
     * Retourne vrai si et seulement si l'index d'arrêt donné correspond à une gare.
     * Cela est déterminé en comparant l'index avec la taille des stations.
     *
     * @param stopId l'index d'arrêt
     * @return vrai si stopId est un index de gare
     */
    default boolean isStationId(int stopId) {
        return stopId < stations().size();
    }

    /**
     * Retourne vrai si et seulement si l'index d'arrêt donné correspond à une voie ou un quai.
     * En effet, si l'index est supérieur ou égal au nombre de gares, il désigne une voie/quai.
     *
     * @param stopId l'index d'arrêt
     * @return vrai si stopId est un index de voie ou quai
     */
    default boolean isPlatformId(int stopId) {
        return stopId >= stations().size();
    }

    /**
     * Retourne l'index de la gare correspondant à l'index d'arrêt donné.
     * Si l'arrêt correspond déjà à une gare, il est renvoyé tel quel.
     * Sinon, il faut soustraire le nombre total de gares.
     *
     * @param stopId l'index d'arrêt
     * @return l'index de la gare correspondante
     */
    default int stationId(int stopId) {

        //return isStationId(stopId) ? stopId : stopId - stations().size();
        //Voir laquelle des deux lignes fonctionne.

        return isStationId(stopId) ? stopId : platforms().stationId(stopId);
    }

    /**
     * Retourne le nom de la voie ou du quai correspondant à l'index d'arrêt donné.
     * Si l'index correspond à une gare, la méthode retourne null.
     *
     * @param stopId l'index d'arrêt
     * @return le nom de la plateforme ou null si l'arrêt est une gare
     */
    default String platformName(int stopId) {
        //return isPlatformId(stopId) ? platforms().name(stopId - stations().size()) : null;
        return isPlatformId(stopId) ? platforms().name(stopId) : null;
    }





}

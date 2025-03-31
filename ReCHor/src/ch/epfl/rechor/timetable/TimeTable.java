package ch.epfl.rechor.timetable;

import java.time.LocalDate;

/**
 * L'interface TimeTable représente un horaire de transport public.
 * Elle regroupe l'ensemble des composants indexés et offre des méthodes
 * pour accéder aux données actives pour une date donnée.
 *
 * @author Antoine Lepin (390950)
 * @author Darius Giannoli (380759)
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
     * @return les courses actives
     */
    Trips tripsFor(LocalDate date);

    /**
     * Retourne les liaisons actives pour la date donnée.
     *
     * @param date la date du voyage
     * @return les liaisons actives
     */
    Connections connectionsFor(LocalDate date);

    /**
     * Vérifie si l'index d'arrêt correspond à une gare.
     *
     * @param stopId l'index d'arrêt
     * @return true si c'est une gare, false sinon
     */
    default boolean isStationId(int stopId) {
        return stopId < stations().size();
    }

    /**
     * Vérifie si l'index d'arrêt correspond à une voie ou un quai.
     *
     * @param stopId l'index d'arrêt
     * @return true si c'est une voie/quai, false sinon
     */
    default boolean isPlatformId(int stopId) {
        return stopId >= stations().size();
    }

    /**
     * Retourne l'index de la gare correspondant à l'index d'arrêt.
     *
     * @param stopId l'index d'arrêt
     * @return l'index de la gare
     */
    default int stationId(int stopId) {
        return isStationId(stopId) ? stopId : platforms().stationId(stopId - stations().size());
    }

    /**
     * Retourne le nom de la plateforme correspondant à l'index d'arrêt.
     * Si l'index correspond à une gare, retourne null.
     *
     * @param stopId l'index d'arrêt
     * @return le nom de la plateforme ou null
     */
    default String platformName(int stopId) {
        return isPlatformId(stopId) ? platforms().name(stopId - stations().size()) : null;
    }
}
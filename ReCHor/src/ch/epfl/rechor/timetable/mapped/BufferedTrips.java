package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.timetable.Trips;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * Implémente l'interface {@code Trips} et permet d'accéder à une table de courses représentée de manière aplatie.
 * <p>
 * Utilise un buffer structuré pour lire les données des courses.
 * </p>
 *
 * @author Antoine Lepin (390950)
 * @author Darius Giannoli (380759)
 */
public final class BufferedTrips implements Trips {

    private static final int ROUTE_ID = 0;       // Champ U16 : index de la ligne (route)
    private static final int DESTINATION_ID = 1;   // Champ U16 : index du nom de la destination finale

    // Structure d'un enregistrement de course
    private static final Structure TRIP_STRUCTURE = new Structure(
            Structure.field(ROUTE_ID, Structure.FieldType.U16),
            Structure.field(DESTINATION_ID, Structure.FieldType.U16)
    );

    private final List<String> stringTable;
    private final StructuredBuffer structuredBuffer;

    /**
     * Construit une instance de {@code BufferedTrips}.
     *
     * @param stringTable la table de chaînes utilisée pour décoder les noms (destinations)
     * @param buffer      le {@code ByteBuffer} contenant les données aplaties des courses.
     */
    public BufferedTrips(List<String> stringTable, ByteBuffer buffer) {
        this.stringTable = stringTable;
        this.structuredBuffer = new StructuredBuffer(TRIP_STRUCTURE, buffer);
    }

    /**
     * Retourne l'index de la ligne (route) associée à la course d'index donné.
     *
     * @param id l'index de la course
     * @return l'index de la ligne associée
     */
    @Override
    public int routeId(int id) {
        return structuredBuffer.getU16(ROUTE_ID, id);
    }

    /**
     * Retourne le nom de la destination finale de la course d'index donné.
     *
     * @param id l'index de la course
     * @return le nom de la destination finale
     */
    @Override
    public String destination(int id) {
        int destinationIndex = structuredBuffer.getU16(DESTINATION_ID, id);
        return stringTable.get(destinationIndex);
    }

    /**
     * Retourne le nombre total de courses présentes dans le buffer.
     *
     * @return le nombre d'enregistrements (courses)
     */
    @Override
    public int size() {
        return structuredBuffer.size();
    }
}
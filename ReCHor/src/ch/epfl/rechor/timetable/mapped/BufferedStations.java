package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.timetable.Stations;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * Fournit un accès aux gares stockées de manière aplatie.
 * Implémente l'interface Stations
 * Chaque gare est représentée par un enregistrement composé de 3 champs :
 * <ul>
 *  <li>Index de chaîne pour le nom (U16)</li>
 *  <li>Longitude (S32)</li>
 *  <li>Latitude (S32)</li>
 * </ul>
 * Les longitudes et latitudes sont converties en degrés à l'aide d'un facteur défini.
 *
 * @author Antoine Lepin (390950)
 * @author Darius Giannoli (380759)
 */
public final class BufferedStations implements Stations {

    // Indices des champs dans la table principale des gares
    private static final int NAME_ID = 0; //U16 Index de chaîne du nom de la gare
    private static final int LON = 1; //S32 Longitude de la gare
    private static final int LAT = 2; //S32 Latitude de la gare

    private static final Structure STATION_STRUCTURE = new Structure(
            Structure.field(NAME_ID, Structure.FieldType.U16),
            Structure.field(LON, Structure.FieldType.S32),
            Structure.field(LAT, Structure.FieldType.S32)
    );

    //Conversion des unités
    private static final double UNIT_TO_DEGREES = Math.scalb(360.0, -32);

    private final List<String> stringTable;
    private final StructuredBuffer structuredBuffer;

    /**
     * Construit une instance d'accès aux gares aplaties.
     *
     * @param stringTable table des chaînes de caractères
     * @param buffer      tampon contenant les données aplaties des gares
     */
    public BufferedStations(List<String> stringTable, ByteBuffer buffer){
        this.stringTable = stringTable;
        this.structuredBuffer = new StructuredBuffer(STATION_STRUCTURE, buffer);
    }

    /**
     * {@inheritDoc}
     *
     * @param id l'index de la gare
     * @return  le nom de la gare
     */
    @Override
    public String name(int id) {
        int nameIndex = structuredBuffer.getU16(NAME_ID, id);
        return stringTable.get(nameIndex);
    }

    /**
     * {@inheritDoc}
     *
     * @param id l'index de la gare
     * @return  la longitude de la gare en degrés
     */
    @Override
    public double longitude(int id) {
        int rawLongitude = structuredBuffer.getS32(LON, id);
        return rawLongitude * UNIT_TO_DEGREES;
    }

    /**
     * {@inheritDoc}
     *
     * @param id l'index de la gare
     * @return  la latitude de la gare en degrés
     */
    @Override
    public double latitude(int id) {
        int rawLatitude = structuredBuffer.getS32(LAT, id);
        return rawLatitude * UNIT_TO_DEGREES;
    }

    /**
     * {@inheritDoc}
     *
     * @return  le nombre de gares
     */
    @Override
    public int size() {
        return structuredBuffer.size();
    }
}
package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.timetable.StationAliases;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Fournit un accès aux noms alternatifs de gares stockées de manière aplatie.
 * Implémente l'interface StationAliases
 * Chaque enregistrement comporte deux champs (U16) :
 * <ul>
 *   <li>Alias de la gare</li>
 *   <li>Nom de la gare d'origine</li>
 * </ul>
 *
 * @author Antoine Lepin (390950)
 * @author Darius Giannoli (380759)
 */
public final class BufferedStationAliases implements StationAliases {

    // Indices des champs dans la table principale des noms alternatifs des gares
    private static final int ALIAS_ID = 0; //U16 Index de chaîne du nom alternatif
    private static final int STATION_NAME_ID = 1; //U16 Index de chaîne du nom de la gare

    private static final Structure ALIAS_STRUCTURE = new Structure(
            Structure.field(ALIAS_ID, Structure.FieldType.U16),
            Structure.field(STATION_NAME_ID, Structure.FieldType.U16)
    );

    private final List<String> stringTable;
    private final StructuredBuffer structuredBuffer;

    /**
     * Construit une instance d'accès aux noms alternatifs des gares.
     *
     * @param stringTable table des chaînes de caractères
     * @param buffer      tampon contenant les données aplaties
     */
    public BufferedStationAliases(List<String> stringTable, ByteBuffer buffer) {
        this.stringTable = stringTable;
        this.structuredBuffer = new StructuredBuffer(ALIAS_STRUCTURE, buffer);
    }

    /**
     * {@inheritDoc}
     *
     * @param id l'index de l'alias
     * @return le nom alternatif de la gare
     */
    @Override
    public String alias(int id) {
        int aliasIndex = structuredBuffer.getU16(ALIAS_ID, id);
        return stringTable.get(aliasIndex);
    }

    /**
     * {@inheritDoc}
     *
     * @param id l'index de l'alias
     * @return le nom de la gare d'origine
     */
    @Override
    public String stationName(int id) {
        int stationNameIndex = structuredBuffer.getU16(STATION_NAME_ID, id);
        return stringTable.get(stationNameIndex);
    }

    /**
     * {@inheritDoc}
     *
     * @return le nombre d'enregistrements
     */
    @Override
    public int size() {
        return structuredBuffer.size();
    }
}
package ch.epfl.rechor.timetable.mapped;

import java.nio.ByteBuffer;
import ch.epfl.rechor.Preconditions;

/**
 * Représente un tableau d'octets structuré selon une Structure.
 * Permet d'accéder aux champs (U8, U16, S32) stockés dans un ByteBuffer.
 *
 * @author Antoine Lepin (390950)
 * @author Darius Giannoli (380759)
 */
public final class StructuredBuffer {

    private final Structure structure;
    private final ByteBuffer buffer;
    private final int elementCount;

    /**
     * Construit un StructuredBuffer à partir d'une structure et d'un ByteBuffer.
     *
     * @param structure la structure des éléments
     * @param buffer    le tampon contenant les données aplaties
     * @throws IllegalArgumentException si la capacité du buffer n'est pas un multiple
     * de la taille totale de la structure
     */
    public StructuredBuffer(Structure structure, ByteBuffer buffer){
        this.structure = structure;
        this.buffer = buffer;
        Preconditions.checkArgument(buffer.capacity() % structure.totalSize() == 0);
        this.elementCount = buffer.capacity() / structure.totalSize();
    }

    /**
     * Retourne le nombre d'éléments présents dans le buffer.
     *
     * @return le nombre d'éléments
     */
    public int size(){
        return elementCount;
    }

    /**
     * Accède à un entier non signé sur 8 bits à partir du buffer.
     *
     * @param fieldIndex   l'indice du champ
     * @param elementIndex l'indice de l'élément
     * @return la valeur U8 en entier positif
     */
    public int getU8(int fieldIndex, int elementIndex) {
        int offset = structure.offset(fieldIndex, elementIndex);
        byte b = buffer.get(offset);
        return Byte.toUnsignedInt(b);
    }

    /**
     * Accède à un entier non signé sur 16 bits à partir du buffer.
     *
     * @param fieldIndex   l'indice du champ
     * @param elementIndex l'indice de l'élément
     * @return la valeur U16 en entier positif
     */
    public int getU16(int fieldIndex, int elementIndex) {
        int offset = structure.offset(fieldIndex, elementIndex);
        short s = buffer.getShort(offset);
        return Short.toUnsignedInt(s);
    }

    /**
     * Accède à un entier signé sur 32 bits à partir du buffer.
     *
     * @param fieldIndex   l'indice du champ
     * @param elementIndex l'indice de l'élément
     * @return la valeur S32
     */
    public int getS32(int fieldIndex, int elementIndex) {
        int offset = structure.offset(fieldIndex, elementIndex);
        return buffer.getInt(offset);
    }
}
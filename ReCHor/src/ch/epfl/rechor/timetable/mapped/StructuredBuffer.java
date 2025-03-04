package ch.epfl.rechor.timetable.mapped;

import java.nio.ByteBuffer;
import ch.epfl.rechor.Preconditions;

/**
 * Représente un tableau d'octets structuré.
 * @author Antoine Lepin (390950)
 * @author Darius Giannoli (380759)
 */
public final class StructuredBuffer {

    private final Structure structure;
    private final ByteBuffer buffer;
    private final int elementCount;

    /**
     * Construit un tableau structuré dont les éléments ont la structure donnée, et dont les octets sont stockés dans le «tableau» buffer.
     * @param structure est la structure des éléments du tableau.
     * @param buffer est le tableau dans lequel on place les éléments de structure.
     */
    public StructuredBuffer(Structure structure, ByteBuffer buffer){
        this.structure = structure;
        this.buffer = buffer;
        Preconditions.checkArgument(buffer.capacity() % structure.totalSize() == 0);
        this.elementCount = buffer.capacity() / structure.totalSize();
    }

    /**
     * @return retourne le nombre d'éléments que contient le tableau buffer.
     */
    public int size(){
        return elementCount;
    }

    /**
     * @param fieldIndex est l'index du champ.
     * @param elementIndex l'index de l'élément dans le tableau.
     * @return retourne l'entier U8 correspondant au champ d'index fieldIndex de l'élément d'index elementIndex du tableau.
     */
    public int getU8(int fieldIndex, int elementIndex) {
        checkIndices(fieldIndex, elementIndex);
        int offset = structure.offset(fieldIndex, elementIndex);
        byte b = buffer.get(offset);
        return Byte.toUnsignedInt(b);
    }

    /**
     * @param fieldIndex est l'index du champ.
     * @param elementIndex l'index de l'élément dans le tableau.
     * @return retourne l'entier U16 correspondant au champ d'index fieldIndex de l'élément d'index elementIndex du tableau.
     */
    public int getU16(int fieldIndex, int elementIndex) {
        checkIndices(fieldIndex, elementIndex);
        int offset = structure.offset(fieldIndex, elementIndex);
        short s = buffer.getShort(offset);
        return Short.toUnsignedInt(s);
    }

    /**
     * @param fieldIndex est l'index du champ.
     * @param elementIndex l'index de l'élément dans le tableau.
     * @return retourne l'entier S32 correspondant au champ d'index fieldIndex de l'élément d'index elementIndex du tableau.
     */
    public int getS32(int fieldIndex, int elementIndex) {
        checkIndices(fieldIndex, elementIndex);
        int offset = structure.offset(fieldIndex, elementIndex);
        return buffer.getInt(offset);
    }

    //pour la modularisation du code : pour ne pas réecrire du code
    private void checkIndices(int fieldIndex, int elementIndex) {
        if (fieldIndex < 0 || fieldIndex >= structure.fieldCount()) {
            throw new IndexOutOfBoundsException("Index out fo bounds");
        }
        if (elementIndex < 0 || elementIndex >= elementCount) {
            throw new IndexOutOfBoundsException("Index out of bound");
        }
    }

}

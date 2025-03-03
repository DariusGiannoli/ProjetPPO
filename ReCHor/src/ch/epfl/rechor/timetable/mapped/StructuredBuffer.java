package ch.epfl.rechor.timetable.mapped;

import java.nio.ByteBuffer;
import ch.epfl.rechor.Preconditions;

public final class StructuredBuffer {

    private final Structure structure;
    private final ByteBuffer buffer;
    private final int elementCount;

    public StructuredBuffer(Structure structure, ByteBuffer buffer){
        this.structure = structure;
        this.buffer = buffer;
        Preconditions.checkArgument(buffer.capacity() % structure.totalSize() == 0);
        this.elementCount = buffer.capacity() / structure.totalSize();
    }

    public int size(){
        return elementCount;
    }

    public int getU8(int fieldIndex, int elementIndex) {
        checkIndices(fieldIndex, elementIndex);
        int offset = structure.offset(fieldIndex, elementIndex);
        byte b = buffer.get(offset);
        return Byte.toUnsignedInt(b);
    }

    public int getU16(int fieldIndex, int elementIndex) {
        checkIndices(fieldIndex, elementIndex);
        int offset = structure.offset(fieldIndex, elementIndex);
        short s = buffer.getShort(offset);
        return Short.toUnsignedInt(s);
    }

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

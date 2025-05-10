package art.emu;

public class Memory {
    private final byte[] memoryBytes;
    public static final int MAX_MEMORY = 0x10000;

    public Memory (int size) {
        memoryBytes = new byte[size];
    }

    public byte[] getMemoryBytes () {
        return this.memoryBytes;
    }

    public void writeMemory (int address, int value) {
        if (address < 0 || address >= memoryBytes.length) {
            throw new IndexOutOfBoundsException("Memory address out of bounds");
        }
        memoryBytes[address] = (byte) value;
    }

    public int readMemory (int address) {
        if (address < 0 || address >= memoryBytes.length) {
            throw new IndexOutOfBoundsException("Memory address out of bounds");
        }
        return memoryBytes[address] & 0xFF;
    }
}

package art.emu;

public class BitUtils {
    public static boolean checkParity (int num) {
        return Integer.bitCount(num & 0xFF) % 2 == 0;
    }

    public static int concatBytes (int b1, int b2) {
        return (b1 << 8) | (b2 & 0xFF);
    }

    public static int extractMSB (int num) {
        int mask = num & 0xFF;
        return (mask & 0x80) >> 7;
    }

    public static int extractLSB (int num) {
        int mask = num & 0xFF;
        return (mask & 0x01);
    }

    public static int setBit (int number, int n) {
        return number | (1 << n);
    }
}

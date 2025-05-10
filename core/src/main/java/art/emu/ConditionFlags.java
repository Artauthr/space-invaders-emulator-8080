package art.emu;

public class ConditionFlags {
    private int bits;

    public ConditionFlags () {
        bits = BitUtils.setBit(bits, 1);
    }

    public int getBits () {
        return this.bits & 0xFF;
    }

    public void setFlag (int flag, boolean value) {
        if (value) {
            bits |= flag;
        } else {
            bits &= ~flag;
        }
    }

    public boolean isFlagSet (int flag) {
        return (bits & flag) != 0;
    }

    public void setFromByte (int value) {
        setFlag(ConditionBits.SIGN, ((value & ConditionBits.SIGN) >> 7) == 1);
        setFlag(ConditionBits.ZERO, ((value & ConditionBits.ZERO) >> 6) == 1);
        setFlag(ConditionBits.AUX_CARRY, ((value & ConditionBits.AUX_CARRY) >> 4) == 1);
        setFlag(ConditionBits.PARITY, ((value & ConditionBits.PARITY) >> 2) == 1);
        setFlag(ConditionBits.CARRY, (value & ConditionBits.CARRY) == 1);
    }

    public void setFromAddition (int a1, int a2) {
        int result = a1 + a2;

        setFlag(ConditionBits.SIGN, (result & 0x80) != 0);
        setFlag(ConditionBits.CARRY, result > 0xFF);
        setFlag(ConditionBits.ZERO, (result & 0xFF) == 0);
        setFlag(ConditionBits.PARITY,  BitUtils.checkParity(result));
        setFlag(ConditionBits.AUX_CARRY, ((a1 & 0x0F) + (a2 & 0x0F)) > 0x0F);
    }

    public void setFromSubtraction (int a1, int a2) {
        int result = a1 - a2;
        setFlag(ConditionBits.SIGN, (result & 0x80) != 0);
        setFlag(ConditionBits.CARRY, a1 < a2);
        setFlag(ConditionBits.ZERO, (result & 0xFF) == 0);
        setFlag(ConditionBits.PARITY, BitUtils.checkParity(result));
        setFlag(ConditionBits.AUX_CARRY, ((a1 & 0x0F) - (a2 & 0x0F)) < 0);
    }
}

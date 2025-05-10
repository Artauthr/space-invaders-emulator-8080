package art.emu;

public class Processor {
    private final Memory memory;
    private final ConditionFlags conditionFlags;
    private final byte[] registers = new byte[8];

    private int programCounter;
    private int stackPointer;

    private boolean interruptsEnabled = false;
    private HardwareIOExecutor hardwareIOExecutor;

    public interface HardwareIOExecutor {
        void hardwareIN (Processor processor, int value);
        void hardwareOUT (Processor processor, int value);
    }

    public Processor () {
        this.memory = new Memory(Memory.MAX_MEMORY);
        this.conditionFlags = new ConditionFlags();
    }

    /**
     * @return Cycles completed by this step
     */
    public int step () {
        int opcode = memory.readMemory(programCounter);
        Instruction instruction = Instruction.fromOpcode(opcode);

        switch (instruction) {
            case IN:
                int inValue = memory.readMemory(programCounter + 1);
                hardwareIOExecutor.hardwareIN(this, inValue);
                programCounter += 2;
                return 10;
            case OUT:
                int outValue = memory.readMemory(programCounter + 1);
                hardwareIOExecutor.hardwareOUT(this, outValue);
                programCounter += 2;
                return 10;
            default:
                 return instruction.execute(this);
        }
    }

    public void pushStack (int value) {
        if (stackPointer <= 0) {
            throw new IllegalStateException("Stack overflow");
        }
        memory.writeMemory(--stackPointer, value & 0xFF);
    }

    public int popStack () {
        if (stackPointer >= Memory.MAX_MEMORY) {
            throw new IllegalStateException("Stack underflow");
        }
        return memory.readMemory(stackPointer++);
    }

    public int NOP () {
        programCounter += 1;
        return 4;
    }

    public int CALL () {
        int loAdd = memory.readMemory(programCounter + 1);
        int hiAdd = memory.readMemory(programCounter + 2);
        int targetAddress = BitUtils.concatBytes(hiAdd, loAdd);

        pushStack((programCounter + 3) >> 8);
        pushStack((programCounter + 3) & 0xFF);

        programCounter = targetAddress;
        return 17;
    }

    public int RET () {
        int lo = popStack();
        int hi = popStack();

        this.programCounter = BitUtils.concatBytes(hi, lo);
        return 10;
    }

    public int DAA () {
        int a = getRegisterValue(Registers.ACCUMULATOR);
        int correction = 0;
        boolean setCarry = false;

        if (((a & 0x0F) > 9) || isConditionBitSet(ConditionBits.AUX_CARRY)) {
            correction |= 0x06;
        }

        if ((a > 0x99) || isConditionBitSet(ConditionBits.CARRY)) {
            correction |= 0x60;
            setCarry = true;
        }

        a = a + correction;
        a = a & 0xFF;

        writeRegisterValue(Registers.ACCUMULATOR, (byte) a);


        setConditionBit(ConditionBits.CARRY, setCarry);
        setConditionBit(ConditionBits.ZERO, (a == 0));
        setConditionBit(ConditionBits.SIGN, (a & 0x80) != 0);
        setConditionBit(ConditionBits.PARITY, BitUtils.checkParity(a));

        programCounter += 1;

        return 4;
    }

    public int getM () {
        int H = getRegisterValue(Registers.H);
        int L = getRegisterValue(Registers.L);

        return (H << 8) | L;
    }

    public int getRegisterValue (int register) {
        return registers[register] & 0xFF; // treat it as unsigned
    }

    public void writeRegisterValue (int register, byte value) {
        registers[register] = value;
    }

    public void setConditionBit (int bit, boolean set) {
        conditionFlags.setFlag(bit, set);
    }

    public boolean isConditionBitSet (int bit) {
        return conditionFlags.isFlagSet(bit);
    }

    public int ADD_REGISTER (int register) {
        int registerValue = getRegisterValue(register);
        return ADD(registerValue);
    }

    private int ADD (int value) {
        int accumulatorValue = getRegisterValue(Registers.ACCUMULATOR);
        int result = accumulatorValue + value;

        setConditionBit(ConditionBits.SIGN, (result & 0x80) != 0);
        setConditionBit(ConditionBits.CARRY, result > 0xFF);
        setConditionBit(ConditionBits.ZERO, (result & 0xFF) == 0);
        setConditionBit(ConditionBits.PARITY,  BitUtils.checkParity(result));
        setConditionBit(ConditionBits.AUX_CARRY, ((accumulatorValue & 0x0F) + (value & 0x0F)) > 0x0F);

        writeRegisterValue(Registers.ACCUMULATOR, (byte) (result & 0xFF));

        programCounter += 1;

        return 4;
    }

    public int ADD_M () {
        int accumulatorValue = getRegisterValue(Registers.ACCUMULATOR);
        int mValue = memory.readMemory(getM());

        int result = accumulatorValue + mValue;

        setConditionBit(ConditionBits.SIGN, (result & 0x80) != 0);
        setConditionBit(ConditionBits.CARRY, result > 0xFF);
        setConditionBit(ConditionBits.ZERO, (result & 0xFF) == 0);
        setConditionBit(ConditionBits.PARITY,  BitUtils.checkParity(result));
        setConditionBit(ConditionBits.AUX_CARRY, ((accumulatorValue & 0x0F) + (mValue & 0x0F)) > 0x0F);

        writeRegisterValue(Registers.ACCUMULATOR, (byte) (result & 0xFF));

        programCounter += 1;
        return 7;
    }

    public int ADC_REGISTER (int register) {
        int registerValue = getRegisterValue(register);
        int addValue = isConditionBitSet(ConditionBits.CARRY) ? registerValue + 1 : registerValue;
        ADD(addValue);
        return 4;
    }

    public int ADC_M () {
        int mValue = memory.readMemory(getM());
        int addValue = isConditionBitSet(ConditionBits.CARRY) ? mValue + 1 : mValue;
        ADD(addValue);
        return 7;
    }

    private void SUB (int value) {
        int accumulator = getRegisterValue(Registers.ACCUMULATOR);
        int result = accumulator - value;

        setConditionBit(ConditionBits.SIGN, (result & 0x80) != 0);
        setConditionBit(ConditionBits.CARRY, accumulator < value);
        setConditionBit(ConditionBits.ZERO, (result & 0xFF) == 0);
        setConditionBit(ConditionBits.PARITY, BitUtils.checkParity(result));
        setConditionBit(ConditionBits.AUX_CARRY, ((accumulator & 0x0F) - (value & 0x0F)) < 0);

        writeRegisterValue(Registers.ACCUMULATOR, (byte) (result & 0xFF));

        programCounter += 1;
    }

    public int SUB_M () {
        int accumulator = getRegisterValue(Registers.ACCUMULATOR);
        int value = memory.readMemory(getM());
        int result = accumulator - value;

        setConditionBit(ConditionBits.SIGN, (result & 0x80) != 0);
        setConditionBit(ConditionBits.CARRY, accumulator < value);
        setConditionBit(ConditionBits.ZERO, (result & 0xFF) == 0);
        setConditionBit(ConditionBits.PARITY, BitUtils.checkParity(result));
        setConditionBit(ConditionBits.AUX_CARRY, ((accumulator & 0x0F) - (value & 0x0F)) < 0);

        writeRegisterValue(Registers.ACCUMULATOR, (byte) (result & 0xFF));

        programCounter += 1;
        return 7;
    }

    public int SUB_REGISTER (int register) {
        int registerValue = getRegisterValue(register);
        SUB(registerValue);

        return 4;
    }

    public int SBB_M () {
        int mValue = memory.readMemory(getM());
        int subValue = isConditionBitSet(ConditionBits.CARRY) ? mValue + 1 : mValue;
        SUB(subValue);
        return 7;
    }

    public int SBB_REGISTER (int register) {
        int registerValue = getRegisterValue(register);
        int subValue = isConditionBitSet(ConditionBits.CARRY) ? registerValue + 1 : registerValue;
        SUB(subValue);
        return 4;
    }

    private void ANA (int value) {
        int accumulatorValue = getRegisterValue(Registers.ACCUMULATOR);
        int result = accumulatorValue & value;

        setConditionBit(ConditionBits.CARRY, false);
        setConditionBit(ConditionBits.SIGN, (result & 0x80) != 0);
        setConditionBit(ConditionBits.ZERO, (result & 0xFF) == 0);
        setConditionBit(ConditionBits.PARITY, BitUtils.checkParity(result));
        setConditionBit(ConditionBits.AUX_CARRY, false);

        writeRegisterValue(Registers.ACCUMULATOR, (byte) (result & 0xFF));

        programCounter += 1;
    }

    public int ANA_M () {
        int accumulatorValue = getRegisterValue(Registers.ACCUMULATOR);
        int value = memory.readMemory(getM());
        int result = accumulatorValue & value;

        setConditionBit(ConditionBits.CARRY, false);
        setConditionBit(ConditionBits.SIGN, (result & 0x80) != 0);
        setConditionBit(ConditionBits.ZERO, (result & 0xFF) == 0);
        setConditionBit(ConditionBits.PARITY, BitUtils.checkParity(result));
        setConditionBit(ConditionBits.AUX_CARRY, false);

        writeRegisterValue(Registers.ACCUMULATOR, (byte) (result & 0xFF));

        programCounter += 1;
        return 7;
    }

    public int ANA_REGISTER (int register) {
        int registerValue = getRegisterValue(register);
        ANA(registerValue);
        return 4;
    }

    private void XRA (int value) {
        int accumulatorValue = getRegisterValue(Registers.ACCUMULATOR);
        int result = accumulatorValue ^ value;

        setConditionBit(ConditionBits.CARRY, false);
        setConditionBit(ConditionBits.SIGN, (result & 0x80) != 0);
        setConditionBit(ConditionBits.ZERO, (result & 0xFF) == 0);
        setConditionBit(ConditionBits.PARITY, BitUtils.checkParity(result));
        setConditionBit(ConditionBits.AUX_CARRY, false);

        writeRegisterValue(Registers.ACCUMULATOR, (byte) (result & 0xFF));

        programCounter += 1;
    }

    public int XRA_M () {
        int accumulatorValue = getRegisterValue(Registers.ACCUMULATOR);
        int value = memory.readMemory(getM());
        int result = accumulatorValue ^ value;

        setConditionBit(ConditionBits.CARRY, false);
        setConditionBit(ConditionBits.SIGN, (result & 0x80) != 0);
        setConditionBit(ConditionBits.ZERO, (result & 0xFF) == 0);
        setConditionBit(ConditionBits.PARITY, BitUtils.checkParity(result));
        setConditionBit(ConditionBits.AUX_CARRY, false);

        writeRegisterValue(Registers.ACCUMULATOR, (byte) (result & 0xFF));

        programCounter += 1;
        return 7;
    }

    public int XRA_REGISTER (int register) {
        int registerValue = getRegisterValue(register);
        XRA(registerValue);
        return 4;
    }

    private void ORA (int value) {
        int accumulatorValue = getRegisterValue(Registers.ACCUMULATOR);
        int result = accumulatorValue | value;

        setConditionBit(ConditionBits.CARRY, false);
        setConditionBit(ConditionBits.SIGN, (result & 0x80) != 0);
        setConditionBit(ConditionBits.ZERO, (result & 0xFF) == 0);
        setConditionBit(ConditionBits.PARITY, BitUtils.checkParity(result));
        setConditionBit(ConditionBits.AUX_CARRY, false);

        writeRegisterValue(Registers.ACCUMULATOR, (byte) (result & 0xFF));

        programCounter += 1;
    }

    public int ORA_M () {
        int accumulatorValue = getRegisterValue(Registers.ACCUMULATOR);
        int value = memory.readMemory(getM());
        int result = accumulatorValue | value;

        setConditionBit(ConditionBits.CARRY, false);
        setConditionBit(ConditionBits.SIGN, (result & 0x80) != 0);
        setConditionBit(ConditionBits.ZERO, (result & 0xFF) == 0);
        setConditionBit(ConditionBits.PARITY, BitUtils.checkParity(result));
        setConditionBit(ConditionBits.AUX_CARRY, false);

        writeRegisterValue(Registers.ACCUMULATOR, (byte) (result & 0xFF));

        programCounter += 1;
        return 7;
    }

    public int ORA_REGISTER (int register) {
        int registerValue = getRegisterValue(register);
        ORA(registerValue);
        return 4;
    }

    public void CMP (int value) {
        int accumulatorValue = getRegisterValue(Registers.ACCUMULATOR);
        int result = accumulatorValue - value;

        setConditionBit(ConditionBits.CARRY, accumulatorValue < value);
        setConditionBit(ConditionBits.ZERO, accumulatorValue == value);
        setConditionBit(ConditionBits.PARITY, BitUtils.checkParity(result & 0xFF));
        setConditionBit(ConditionBits.SIGN, (result & 0x80) != 0);
        setConditionBit(ConditionBits.AUX_CARRY, ((accumulatorValue & 0x0F) - (value & 0x0F)) < 0);

        programCounter += 1;
    }

    public int CMP_M () {
        int mValue = memory.readMemory(getM());
        int accumulatorValue = getRegisterValue(Registers.ACCUMULATOR);
        int result = accumulatorValue - mValue;

        setConditionBit(ConditionBits.CARRY, accumulatorValue < mValue);
        setConditionBit(ConditionBits.ZERO, (result & 0xFF) == 0);
        setConditionBit(ConditionBits.PARITY, BitUtils.checkParity(result & 0xFF));
        setConditionBit(ConditionBits.SIGN, (result & 0x80) != 0);
        setConditionBit(ConditionBits.AUX_CARRY, (accumulatorValue & 0x0F) < (mValue & 0x0F));

        programCounter += 1;
        return 7;
    }


    public int CMP_REGISTER (int register) {
        int registerValue = getRegisterValue(register);
        CMP(registerValue);
        return 4;
    }

    public int MOV_REG_REG(int destinationRegister, int sourceRegister) {
        writeRegisterValue(destinationRegister, (byte) getRegisterValue(sourceRegister));
        programCounter += 1;
        return 5;
    }

    public int MOV_REG_MEMORY (int destinationRegister) {
        writeRegisterValue(destinationRegister, (byte) memory.readMemory(getM()));
        programCounter += 1;
        return 7;
    }

    public int MOV_MEMORY_REG (int sourceRegister) {
        memory.writeMemory(getM(), getRegisterValue(sourceRegister));
        programCounter += 1;
        return 7;
    }

    public int HLT () {
        programCounter -= 1;
        return 7;
    }

    public int LXI_REG_DATA (int r1, int r2) {
        int lsb = memory.readMemory(programCounter + 1);
        int hsb = memory.readMemory(programCounter + 2);

        writeRegisterValue(r1, (byte) (hsb & 0xFF));
        writeRegisterValue(r2, (byte) (lsb & 0xFF));

        programCounter += 3;
        return 10;
    }

    public int LXI_SP_DATA () {
        int lsb = memory.readMemory(programCounter + 1);
        int hsb = memory.readMemory(programCounter + 2);

        stackPointer = ((hsb & 0xFF) << 8) | (lsb & 0xFF);

        programCounter += 3;
        return 10;
    }

    public int STAX_REG (int r1, int r2) {
        int accumulatorValue = getRegisterValue(Registers.ACCUMULATOR);

        int v1 = getRegisterValue(r1);
        int v2 = getRegisterValue(r2);

        int writeAddress = (v1 << 8) | v2;
        memory.writeMemory(writeAddress, accumulatorValue);

        programCounter += 1;
        return 7;
    }

    public int SHLD () {
        int valueL = getRegisterValue(Registers.L);
        int valueH = getRegisterValue(Registers.H);

        int lowAddr = memory.readMemory(programCounter + 1);
        int highAddr = memory.readMemory(programCounter + 2);

        int memoryAddress = BitUtils.concatBytes(highAddr, lowAddr);

        memory.writeMemory(memoryAddress, valueL);
        memory.writeMemory(memoryAddress + 1, valueH);

        programCounter += 3;
        return 16;
    }

    public int STA () {
        int lowAddr = memory.readMemory(programCounter + 1);
        int highAddr = memory.readMemory(programCounter + 2);

        int writeAddress = BitUtils.concatBytes(highAddr, lowAddr);

        int accumulatorValue = getRegisterValue(Registers.ACCUMULATOR);
        memory.writeMemory(writeAddress, accumulatorValue);

        programCounter += 3;
        return 13;
    }

    public int INX_REG_PAIR (int r1, int r2) {
        int v1 = getRegisterValue(r1);
        int v2 = getRegisterValue(r2);

        int combined = BitUtils.concatBytes(v1, v2);
        int result = (combined + 1) & 0xFFFF;

        writeRegisterValue(r1, (byte) (result >> 8));
        writeRegisterValue(r2, (byte) (result));

        programCounter += 1;
        return 5;
    }

    public int INX_SP () {
        stackPointer = (short) ((stackPointer + 1) & 0xFFFF);
        programCounter += 1;
        return 5;
    }

    public int INR_REG (int reg) {
        int currentValue = getRegisterValue(reg);

        int newValue = currentValue + 1;
        setConditionBit(ConditionBits.SIGN, (newValue & 0x80) != 0);
        setConditionBit(ConditionBits.ZERO, (newValue & 0xFF) == 0);
        setConditionBit(ConditionBits.PARITY, BitUtils.checkParity(newValue & 0xFF));
        setConditionBit(ConditionBits.AUX_CARRY, ((currentValue & 0x0F) + 1) > 0x0F);

        writeRegisterValue(reg, (byte) (newValue & 0xFF));

        programCounter += 1;
        return 5;
    }

    public int INR_MEM () {
        int hValue = getRegisterValue(Registers.H);
        int lValue = getRegisterValue(Registers.L);

        int address = BitUtils.concatBytes(hValue, lValue);
        int memValue = memory.readMemory(address);

        int newValue = memValue + 1;

        setConditionBit(ConditionBits.SIGN, (newValue & 0x80) != 0);
        setConditionBit(ConditionBits.ZERO, (newValue & 0xFF) == 0);
        setConditionBit(ConditionBits.PARITY, BitUtils.checkParity(newValue & 0xFF));
        setConditionBit(ConditionBits.AUX_CARRY, ((memValue & 0x0F) + 1) > 0x0F);

        memory.writeMemory(address, newValue);

        programCounter += 1;
        return 10;
    }

    public int DCR_REG (int reg) {
        int currentValue = getRegisterValue(reg);

        int newValue = currentValue - 1;
        setConditionBit(ConditionBits.SIGN, (newValue & 0x80) != 0);
        setConditionBit(ConditionBits.ZERO, (newValue & 0xFF) == 0);
        setConditionBit(ConditionBits.PARITY, BitUtils.checkParity(newValue & 0xFF));
        setConditionBit(ConditionBits.AUX_CARRY, (currentValue & 0x0F) == 0);

        writeRegisterValue(reg, (byte) (newValue & 0xFF));

        programCounter += 1;
        return 5;
    }

    public int DCR_MEM () {
        int hValue = getRegisterValue(Registers.H);
        int lValue = getRegisterValue(Registers.L);

        int address = BitUtils.concatBytes(hValue, lValue);
        int memValue = memory.readMemory(address);

        int newValue = memValue - 1;

        setConditionBit(ConditionBits.SIGN, (newValue & 0x80) != 0);
        setConditionBit(ConditionBits.ZERO, (newValue & 0xFF) == 0);
        setConditionBit(ConditionBits.PARITY, BitUtils.checkParity(newValue & 0xFF));
        setConditionBit(ConditionBits.AUX_CARRY, (memValue & 0x0F) == 0);

        memory.writeMemory(address, newValue);

        programCounter += 1;
        return 10;
    }

    public int MVI_REG (int reg) {
        int immediateValue = memory.readMemory(programCounter + 1);
        writeRegisterValue(reg, (byte) (immediateValue & 0xFF));

        programCounter += 2;
        return 7;
    }

    public int MVI_MEM () {
        int address = BitUtils.concatBytes(getRegisterValue(Registers.H), getRegisterValue(Registers.L));
        int immediateValue = memory.readMemory(programCounter + 1);
        memory.writeMemory(address, immediateValue);

        programCounter += 2;
        return 10;
    }

    public int RLC () {
        int accumulatorValue = getRegisterValue(Registers.ACCUMULATOR);

        int msb = accumulatorValue & 0x80;

        int result = ((accumulatorValue << 1) | (msb >> 7)) & 0xFF;

        setConditionBit(ConditionBits.CARRY, msb != 0);
        writeRegisterValue(Registers.ACCUMULATOR, (byte) result);

        programCounter += 1;
        return 4;
    }

    public int RRC () {
        int accumulatorValue = getRegisterValue(Registers.ACCUMULATOR);

        int lsb = accumulatorValue & 0x01;
        setConditionBit(ConditionBits.CARRY, lsb == 1);

        int result = (lsb << 7 | (accumulatorValue >> 1)) & 0xFF;
        writeRegisterValue(Registers.ACCUMULATOR, (byte) result);

        programCounter += 1;
        return 4;
    }

    public int RAL () {
        int accumulator = getRegisterValue(Registers.ACCUMULATOR);
        int carryBit = conditionFlags.isFlagSet(ConditionBits.CARRY) ? 1 : 0;

        int accumulatorMSB = BitUtils.extractMSB(accumulator);
        setConditionBit(ConditionBits.CARRY, accumulatorMSB == 1);

        int accumulatorResult = (accumulator << 1 | carryBit) & 0xFF;
        writeRegisterValue(Registers.ACCUMULATOR, (byte) accumulatorResult);

        programCounter += 1;
        return 4;
    }

    public int RAR () {
        int accumulator = getRegisterValue(Registers.ACCUMULATOR);
        int carryBit = conditionFlags.isFlagSet(ConditionBits.CARRY) ? 1 : 0;

        int accumulatorLSB = BitUtils.extractLSB(accumulator);
        setConditionBit(ConditionBits.CARRY, accumulatorLSB == 1);

        int accumulatorResult = (accumulator >> 1 | carryBit << 7) & 0xFF;
        writeRegisterValue(Registers.ACCUMULATOR, (byte) accumulatorResult);

        programCounter += 1;
        return 4;
    }

    public int STC () {
        setConditionBit(ConditionBits.CARRY, true);
        programCounter += 1;
        return 4;
    }

    public int DAD (int reg1, int reg2) {
        int v1 = getRegisterValue(reg1);
        int v2 = getRegisterValue(reg2);
        DAD(BitUtils.concatBytes(v1, v2));
        return 10;
    }

    public int DAD_SP () {
        DAD(stackPointer);
        return 10;
    }

    private void DAD (int value) {
        int hValue = getRegisterValue(Registers.H);
        int lValue = getRegisterValue(Registers.L);
        int hl = BitUtils.concatBytes(hValue, lValue);

        int sum = (value + hl);

        writeRegisterValue(Registers.H, (byte) ((sum >> 8) & 0xFF));
        writeRegisterValue(Registers.L, (byte) (sum & 0xFF));

        setConditionBit(ConditionBits.CARRY, sum > 0xFFFF);

        programCounter += 1;
    }

    public int LDAX (int reg1, int reg2) {
        int high = getRegisterValue(reg1);
        int low  = getRegisterValue(reg2);
        int address = BitUtils.concatBytes(high, low);
        int memoryValue = memory.readMemory(address);
        writeRegisterValue(Registers.ACCUMULATOR, (byte) memoryValue);

        programCounter += 1;
        return 7;
    }

    public int LHLD () {
        int loAdd = memory.readMemory(programCounter + 1);
        int hiAdd = memory.readMemory(programCounter + 2);

        int lAddress = BitUtils.concatBytes(hiAdd, loAdd);
        int hAddress = lAddress + 1;

        writeRegisterValue(Registers.L, (byte) memory.readMemory(lAddress));
        writeRegisterValue(Registers.H, (byte) memory.readMemory(hAddress));
        programCounter += 3;
        return 16;
    }

    public int LDA () {
        int loAdd = memory.readMemory(programCounter + 1);
        int hiAdd = memory.readMemory(programCounter + 2);

        int value = memory.readMemory(BitUtils.concatBytes(hiAdd, loAdd));
        writeRegisterValue(Registers.ACCUMULATOR, (byte) value);

        programCounter += 3;
        return 13;
    }

    public int CMC () {
        setConditionBit(ConditionBits.CARRY, !isConditionBitSet(ConditionBits.CARRY));
        programCounter += 1;
        return 4;
    }

    public int CMA () {
        int accumulatorValue = getRegisterValue(Registers.ACCUMULATOR);
        writeRegisterValue(Registers.ACCUMULATOR, (byte) ~accumulatorValue);
        programCounter += 1;
        return 4;
    }

    public int POP_REGS (int reg1, int reg2) {
        int v2 = memory.readMemory(stackPointer++);
        int v1 = memory.readMemory(stackPointer++);

        writeRegisterValue(reg2, (byte) v2);
        writeRegisterValue(reg1, (byte) v1);

        programCounter += 1;
        return 10;
    }

    public int POP_PSW() {
        int flagsByte = popStack();
        int accumulatorValue = popStack();

        conditionFlags.setFromByte(flagsByte);
        writeRegisterValue(Registers.ACCUMULATOR, (byte) accumulatorValue);

        programCounter += 1;
        return 10;
    }

    public int PUSH_REGS (int reg1, int reg2) {
        int v1 = getRegisterValue(reg1);
        int v2 = getRegisterValue(reg2);

        memory.writeMemory(--stackPointer, v1);
        memory.writeMemory(--stackPointer, v2);

        programCounter += 1;
        return 11;
    }

    public int PUSH_PSW () {
        int accumulatorValue = getRegisterValue(Registers.ACCUMULATOR);
        memory.writeMemory(stackPointer - 1, accumulatorValue);

        int bits = conditionFlags.getBits();
        memory.writeMemory(stackPointer - 2, bits);

        stackPointer -= 2;
        programCounter += 1;
        return 11;
    }


    public int JZ () {
        if (conditionFlags.isFlagSet(ConditionBits.ZERO)) {
            int loAdd = memory.readMemory(programCounter + 1);
            int hiAdd = memory.readMemory(programCounter + 2);
            programCounter = BitUtils.concatBytes(hiAdd, loAdd);
        } else {
            programCounter += 3;
        }

        return 10;
    }

    public int JNZ () {
        if (!conditionFlags.isFlagSet(ConditionBits.ZERO)) {
            int loAdd = memory.readMemory(programCounter + 1);
            int hiAdd = memory.readMemory(programCounter + 2);
            programCounter = BitUtils.concatBytes(hiAdd, loAdd);
        } else {
            programCounter += 3;
        }
        return 10;
    }

    public int PCHL () {
        int h = getRegisterValue(Registers.H);
        int l = getRegisterValue(Registers.L);

        programCounter = BitUtils.concatBytes(h, l);
        return 5;
    }

    public int SPHL () {
        int hValue = getRegisterValue(Registers.H);
        int lValue = getRegisterValue(Registers.L);

        stackPointer = BitUtils.concatBytes(hValue, lValue);
        programCounter += 1;
        return 5;
    }

    public int JMP () {
        int loAdd = memory.readMemory(programCounter + 1);
        int hiAdd = memory.readMemory(programCounter + 2);

        programCounter = BitUtils.concatBytes(hiAdd, loAdd);
        return 10;
    }

    public int JC () {
        if (conditionFlags.isFlagSet(ConditionBits.CARRY)) {
            int loAdd = memory.readMemory(programCounter + 1);
            int hiAdd = memory.readMemory(programCounter + 2);
            programCounter = BitUtils.concatBytes(hiAdd, loAdd);
        } else {
            programCounter += 3;
        }
        return 10;
    }

    public int JNC () {
        if (!conditionFlags.isFlagSet(ConditionBits.CARRY)) {
            int loAdd = memory.readMemory(programCounter + 1);
            int hiAdd = memory.readMemory(programCounter + 2);
            programCounter = BitUtils.concatBytes(hiAdd, loAdd);
        } else {
            programCounter += 3;
        }
        return 10;
    }

    public int JM () {
        if (conditionFlags.isFlagSet(ConditionBits.SIGN)) {
            int loAdd = memory.readMemory(programCounter + 1);
            int hiAdd = memory.readMemory(programCounter + 2);
            programCounter = BitUtils.concatBytes(hiAdd, loAdd);
        } else {
            programCounter += 3;
        }

        return 10;
    }

    public int JP () {
        if (!conditionFlags.isFlagSet(ConditionBits.SIGN)) {
            int loAdd = memory.readMemory(programCounter + 1);
            int hiAdd = memory.readMemory(programCounter + 2);
            programCounter = BitUtils.concatBytes(hiAdd, loAdd);
        } else {
            programCounter += 3;
        }
        return 10;
    }

    public int JPE () {
        if (conditionFlags.isFlagSet(ConditionBits.PARITY)) {
            int loAdd = memory.readMemory(programCounter + 1);
            int hiAdd = memory.readMemory(programCounter + 2);
            programCounter = BitUtils.concatBytes(hiAdd, loAdd);
        } else {
            programCounter += 3;
        }
        return 10;
    }

    public int JPO () {
        if (!conditionFlags.isFlagSet(ConditionBits.PARITY)) {
            int loAdd = memory.readMemory(programCounter + 1);
            int hiAdd = memory.readMemory(programCounter + 2);
            programCounter = BitUtils.concatBytes(hiAdd, loAdd);
        } else {
            programCounter += 3;
        }
        return 10;
    }

    public int CC () {
        if (conditionFlags.isFlagSet(ConditionBits.CARRY)) {
            CALL();
            return 17;
        } else {
            programCounter += 3;
            return 11;
        }
    }

    public int CNC () {
        if (!conditionFlags.isFlagSet(ConditionBits.CARRY)) {
            CALL();
            return 17;
        } else {
            programCounter += 3;
            return 11;
        }
    }

    public int CZ () {
        if (conditionFlags.isFlagSet(ConditionBits.ZERO)) {
            CALL();
            return 17;
        } else {
            programCounter += 3;
            return 11;
        }
    }

    public int CNZ () {
        if (!conditionFlags.isFlagSet(ConditionBits.ZERO)) {
            CALL();
            return 17;
        } else {
            programCounter += 3;
            return 11;
        }
    }

    public int CM () {
        if (conditionFlags.isFlagSet(ConditionBits.SIGN)) {
            CALL();
            return 17;
        } else {
            programCounter += 3;
            return 11;
        }
    }

    public int CP () {
        if (!conditionFlags.isFlagSet(ConditionBits.SIGN)) {
            CALL();
            return 17;
        } else {
            programCounter += 3;
            return 11;
        }
    }

    public int CPE () {
        if (conditionFlags.isFlagSet(ConditionBits.PARITY)) {
            CALL();
            return 17;
        } else {
            programCounter += 3;
            return 11;
        }
    }

    public int CPO () {
        if (!conditionFlags.isFlagSet(ConditionBits.PARITY)) {
            CALL();
            return 17;
        } else {
            programCounter += 3;
            return 11;
        }
    }

    public int RC () {
        if (conditionFlags.isFlagSet(ConditionBits.CARRY)) {
            RET();
            return 11;
        } else {
            programCounter += 1;
            return 5;
        }
    }

    public int RNC () {
        if (!conditionFlags.isFlagSet(ConditionBits.CARRY)) {
            RET();
            return 11;
        } else {
            programCounter += 1;
            return 5;
        }
    }

    public int RZ () {
        if (conditionFlags.isFlagSet(ConditionBits.ZERO)) {
            RET();
            return 11;
        } else {
            programCounter += 1;
            return 5;
        }
    }

    public int RNZ () {
        if (!conditionFlags.isFlagSet(ConditionBits.ZERO)) {
            RET();
            return 11;
        } else {
            programCounter += 1;
            return 5;
        }
    }

    public int RM () {
        if (conditionFlags.isFlagSet(ConditionBits.SIGN)) {
            RET();
            return 11;
        } else {
            programCounter += 1;
            return 5;
        }
    }

    public int RP () {
        if (!conditionFlags.isFlagSet(ConditionBits.SIGN)) {
            RET();
            return 11;
        } else {
            programCounter += 1;
            return 5;
        }
    }

    public int RPE () {
        if (conditionFlags.isFlagSet(ConditionBits.PARITY)) {
            RET();
            return 11;
        } else {
            programCounter += 1;
            return 5;
        }
    }

    public int RPO () {
        if (!conditionFlags.isFlagSet(ConditionBits.PARITY)) {
            RET();
            return 11;
        } else {
            programCounter += 1;
            return 5;
        }
    }

    public int RST_VALUE (int nnn) {
        pushStack((programCounter >> 8) & 0xFF);  // high byte
        pushStack(programCounter        & 0xFF);  // low  byte

        programCounter = nnn * 8;

        return 11;
    }

    public int XTHL () {
        int h = getRegisterValue(Registers.H);
        int l = getRegisterValue(Registers.L);

        int memLow = memory.readMemory(stackPointer);
        int memHigh = memory.readMemory(stackPointer + 1);

        writeRegisterValue(Registers.H, (byte) memHigh);
        writeRegisterValue(Registers.L, (byte) memLow);

        memory.writeMemory(stackPointer, l);
        memory.writeMemory(stackPointer + 1, h);

        programCounter += 1;
        return 18;
    }


    public int DI () {
        interruptsEnabled = false;
        programCounter += 1;
        return 4;
    }

    public int EI () {
        interruptsEnabled = true;
        programCounter += 1;
        return 4;
    }

    public int XCHG () {
        int hValue = getRegisterValue(Registers.H);
        int lValue = getRegisterValue(Registers.L);

        int dValue = getRegisterValue(Registers.D);
        int eValue = getRegisterValue(Registers.E);

        writeRegisterValue(Registers.H, (byte) dValue);
        writeRegisterValue(Registers.L, (byte) eValue);

        writeRegisterValue(Registers.D, (byte) hValue);
        writeRegisterValue(Registers.E, (byte) lValue);

        programCounter += 1;
        return 5;
    }

    public int ADI () {
        int immediate = memory.readMemory(programCounter + 1);
        int accumulatorValue = getRegisterValue(Registers.ACCUMULATOR);

        conditionFlags.setFromAddition(accumulatorValue, immediate);

        int result = accumulatorValue + immediate;
        writeRegisterValue(Registers.ACCUMULATOR, (byte) (result & 0xFF));

        programCounter += 2;
        return 7;
    }

    public int SUI () {
        int immediate = memory.readMemory(programCounter + 1);
        int accumulatorValue = getRegisterValue(Registers.ACCUMULATOR);

        conditionFlags.setFromSubtraction(accumulatorValue, immediate);

        int result = accumulatorValue - immediate;
        writeRegisterValue(Registers.ACCUMULATOR, (byte) (result & 0xFF));

        programCounter += 2;
        return 7;
    }

    public int SBI () {
        int immediate = memory.readMemory(programCounter + 1);
        int cBit = conditionFlags.isFlagSet(ConditionBits.CARRY) ? 1 : 0;

        int internalAdd = (immediate + cBit) & 0xFF;
        int accumulatorValue = getRegisterValue(Registers.ACCUMULATOR);

        conditionFlags.setFromSubtraction(accumulatorValue, internalAdd);

        int result = accumulatorValue - internalAdd;
        writeRegisterValue(Registers.ACCUMULATOR, (byte) (result & 0xFF));

        programCounter += 2;
        return 7;
    }

    public int ACI () {
        int immediate = memory.readMemory(programCounter + 1);
        int accumulatorValue = getRegisterValue(Registers.ACCUMULATOR);
        int carryBitValue = conditionFlags.isFlagSet(ConditionBits.CARRY) ? 1 : 0;

        int result = accumulatorValue + immediate + carryBitValue;
        writeRegisterValue(Registers.ACCUMULATOR, (byte) result);

        setConditionBit(ConditionBits.SIGN, (result & 0x80) != 0);
        setConditionBit(ConditionBits.CARRY, result > 0xFF);
        setConditionBit(ConditionBits.ZERO, (result & 0xFF) == 0);
        setConditionBit(ConditionBits.PARITY,  BitUtils.checkParity(result));
        setConditionBit(ConditionBits.AUX_CARRY, ((accumulatorValue & 0x0F) + (immediate + carryBitValue & 0x0F)) > 0x0F);

        programCounter += 2;
        return 7;
    }

    public int XRI () {
        int immediate = memory.readMemory(programCounter + 1);
        int accumulatorValue = getRegisterValue(Registers.ACCUMULATOR);

        int result = accumulatorValue ^ immediate;
        writeRegisterValue(Registers.ACCUMULATOR, (byte) result);

        setConditionBit(ConditionBits.SIGN, (result & 0x80) != 0);
        setConditionBit(ConditionBits.CARRY, false);
        setConditionBit(ConditionBits.ZERO, (result & 0xFF) == 0);
        setConditionBit(ConditionBits.PARITY,  BitUtils.checkParity(result));
        setConditionBit(ConditionBits.AUX_CARRY,  false);

        programCounter += 2;
        return 7;
    }

    public int CPI () {
        int immediate = memory.readMemory(programCounter + 1);
        int accumulatorValue = getRegisterValue(Registers.ACCUMULATOR);

        int result = accumulatorValue - immediate;

        setConditionBit(ConditionBits.ZERO, (result & 0xFF) == 0);
        setConditionBit(ConditionBits.SIGN, (result & 0x80) != 0);
        setConditionBit(ConditionBits.CARRY, immediate > accumulatorValue);
        setConditionBit(ConditionBits.PARITY, BitUtils.checkParity(result & 0xFF));
        setConditionBit(ConditionBits.AUX_CARRY, ((accumulatorValue & 0x0F) - (immediate & 0x0F)) < 0);

        programCounter += 2;
        return 7;
    }

    public int ANI () {
        int immediate = memory.readMemory(programCounter + 1);
        int accumulatorValue = getRegisterValue(Registers.ACCUMULATOR);

        int result = accumulatorValue & immediate;
        writeRegisterValue(Registers.ACCUMULATOR, (byte) (result & 0xFF));

        setConditionBit(ConditionBits.CARRY, false);
        setConditionBit(ConditionBits.AUX_CARRY, false);
        setConditionBit(ConditionBits.SIGN, (result & 0x80) != 0);
        setConditionBit(ConditionBits.ZERO, (result & 0xFF) == 0);
        setConditionBit(ConditionBits.PARITY, BitUtils.checkParity(result & 0xFF));

        programCounter += 2;
        return 7;
    }


    public int ORI () {
        int immediate = memory.readMemory(programCounter + 1);

        int accumulatorValue = getRegisterValue(Registers.ACCUMULATOR);

        int result = accumulatorValue | immediate;

        writeRegisterValue(Registers.ACCUMULATOR, (byte) result);

        setConditionBit(ConditionBits.CARRY, false);
        setConditionBit(ConditionBits.AUX_CARRY, false);
        setConditionBit(ConditionBits.SIGN, (result & 0x80) != 0);
        setConditionBit(ConditionBits.ZERO, (result & 0xFF) == 0);
        setConditionBit(ConditionBits.PARITY, BitUtils.checkParity(result));

        programCounter += 2;
        return 7;
    }

    public int DCX (int r1, int r2) {
        int value = BitUtils.concatBytes(getRegisterValue(r1), getRegisterValue(r2));
        int result = (value - 1) & 0xFFFF;

        writeRegisterValue(r1, (byte) ((result >> 8) & 0xFF));
        writeRegisterValue(r2, (byte) (result & 0xFF));

        programCounter += 1;
        return 5;
    }

    public int DCX_SP () {
        stackPointer = (stackPointer - 1) & 0xFFFF;
        programCounter += 1;
        return 5;
    }

    public Memory getMemory () {
        return memory;
    }

    public boolean isInterruptsEnabled () {
        return interruptsEnabled;
    }

    public void setHardwareIOExecutor (HardwareIOExecutor executor) {
        this.hardwareIOExecutor = executor;
    }
}




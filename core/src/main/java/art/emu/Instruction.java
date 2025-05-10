package art.emu;

import com.badlogic.gdx.utils.IntMap;

public enum Instruction {
    NOP(0x00, (Processor processor, int opcode) -> processor.NOP()),

    LXI_B(0x01,  (processor, opcode) -> processor.LXI_REG_DATA(Registers.B, Registers.C)),
    LXI_D(0x11,  (processor, opcode) -> processor.LXI_REG_DATA(Registers.D, Registers.E)),
    LXI_H(0x21,  (processor, opcode) -> processor.LXI_REG_DATA(Registers.H, Registers.L)),
    LXI_SP(0x31, (processor, opcode) -> processor.LXI_SP_DATA()),

    SHLD(0x22, (processor, opcode) -> processor.SHLD()),
    STA(0x32,  (processor, opcode) -> processor.STA()),

    INX_B(0x03,  (processor, opcode) -> processor.INX_REG_PAIR(Registers.B, Registers.C)),
    INX_D(0x13,  (processor, opcode) -> processor.INX_REG_PAIR(Registers.D, Registers.E)),
    INX_H(0x23,  (processor, opcode) -> processor.INX_REG_PAIR(Registers.H, Registers.L)),
    INX_SP(0x33, (processor, opcode) -> processor.INX_SP()),

    INR_B(0x04,  (processor, opcode) -> processor.INR_REG(Registers.B)),
    INR_D(0x14,  (processor, opcode) -> processor.INR_REG(Registers.D)),
    INR_H(0x24,  (processor, opcode) -> processor.INR_REG(Registers.H)),
    INR_C(0x0C,  (processor, opcode) -> processor.INR_REG(Registers.C)),
    INR_E(0x1C,  (processor, opcode) -> processor.INR_REG(Registers.E)),
    INR_L(0x2C,  (processor, opcode) -> processor.INR_REG(Registers.L)),
    INR_A(0x3C,  (processor, opcode) -> processor.INR_REG(Registers.ACCUMULATOR)),
    INR_M(0x34,  (processor, opcode) -> processor.INR_MEM()),

    DCR_B(0x05,  (processor, opcode) -> processor.DCR_REG(Registers.B)),
    DCR_D(0x15,  (processor, opcode) -> processor.DCR_REG(Registers.D)),
    DCR_H(0x25,  (processor, opcode) -> processor.DCR_REG(Registers.H)),
    DCR_C(0x0D,  (processor, opcode) -> processor.DCR_REG(Registers.C)),
    DCR_E(0x1D,  (processor, opcode) -> processor.DCR_REG(Registers.E)),
    DCR_L(0x2D,  (processor, opcode) -> processor.DCR_REG(Registers.L)),
    DCR_A(0x3D,  (processor, opcode) -> processor.DCR_REG(Registers.ACCUMULATOR)),
    DCR_M(0x35,  (processor, opcode) -> processor.DCR_MEM()),

    DCX_B(0x0B,  ((processor, opcode1) -> processor.DCX(Registers.B, Registers.C))),
    DCX_D(0x1B,  ((processor, opcode1) -> processor.DCX(Registers.D, Registers.E))),
    DCX_H(0x2B,  ((processor, opcode1) -> processor.DCX(Registers.H, Registers.L))),
    DCX_SP(0x3B, ((processor, opcode1) -> processor.DCX_SP())),

    MVI_B(0x06,  (processor, opcode) -> processor.MVI_REG(Registers.B)),
    MVI_D(0x16,  (processor, opcode) -> processor.MVI_REG(Registers.D)),
    MVI_H(0x26,  (processor, opcode) -> processor.MVI_REG(Registers.H)),
    MVI_C(0x0E,  (processor, opcode) -> processor.MVI_REG(Registers.C)),
    MVI_E(0x1E,  (processor, opcode) -> processor.MVI_REG(Registers.E)),
    MVI_L(0x2E,  (processor, opcode) -> processor.MVI_REG(Registers.L)),
    MVI_A(0x3E,  (processor, opcode) -> processor.MVI_REG(Registers.ACCUMULATOR)),
    MVI_M(0x36,  (processor, opcode) -> processor.MVI_MEM()),

    RLC(0x07, (processor, opcode) -> processor.RLC()),
    RAL(0x17, (processor, opcode) -> processor.RAL()),

    RRC(0x0F, (processor, opcode) -> processor.RRC()),
    RAR(0x1F, (processor, opcode) -> processor.RAR()),
    CMA(0x2F, (processor, opcode) -> processor.CMA()),
    CMC(0x3F, (processor, opcode) -> processor.CMC()),

    STC(0x37, (processor, opcode) -> processor.STC()),

    DAD_B(0x09,  (processor, opcode) -> processor.DAD(Registers.B, Registers.C)),
    DAD_D(0x19,  (processor, opcode) -> processor.DAD(Registers.D, Registers.E)),
    DAD_H(0x29,  (processor, opcode) -> processor.DAD(Registers.H, Registers.L)),
    DAD_SP(0x39, (processor, opcode) -> processor.DAD_SP()),

    LDAX_B(0x0A, (processor, opcode) -> processor.LDAX(Registers.B, Registers.C)),
    LDAX_D(0x1A, (processor, opcode) -> processor.LDAX(Registers.D, Registers.E)),

    LHLD(0x2A,  (processor, opcode) -> processor.LHLD()),
    LDA(0x3A,   (processor, opcode) -> processor.LDA()),

    STAX_B(0x02, (processor, opcode) -> processor.STAX_REG(Registers.B, Registers.C)),
    STAX_D(0x12, (processor, opcode) -> processor.STAX_REG(Registers.D, Registers.E)),

    DAA(0x27, ((processor, opcode1) -> processor.DAA())),

    ADD_B(0x80, (processor, opcode) -> processor.ADD_REGISTER(Registers.B)),
    ADD_C(0x81, (processor, opcode) -> processor.ADD_REGISTER(Registers.C)),
    ADD_D(0x82, (processor, opcode) -> processor.ADD_REGISTER(Registers.D)),
    ADD_E(0x83, (processor, opcode) -> processor.ADD_REGISTER(Registers.E)),
    ADD_H(0x84, (processor, opcode) -> processor.ADD_REGISTER(Registers.H)),
    ADD_L(0x85, (processor, opcode) -> processor.ADD_REGISTER(Registers.L)),
    ADD_A(0x87, (processor, opcode) -> processor.ADD_REGISTER(Registers.ACCUMULATOR)),
    ADD_M(0x86, (processor, opcode) -> processor.ADD_M()),

    ADC_B(0x88, (processor, opcode) -> processor.ADC_REGISTER(Registers.B)),
    ADC_C(0x89, (processor, opcode) -> processor.ADC_REGISTER(Registers.C)),
    ADC_D(0x8A, (processor, opcode) -> processor.ADC_REGISTER(Registers.D)),
    ADC_E(0x8B, (processor, opcode) -> processor.ADC_REGISTER(Registers.E)),
    ADC_H(0x8C, (processor, opcode) -> processor.ADC_REGISTER(Registers.H)),
    ADC_L(0x8D, (processor, opcode) -> processor.ADC_REGISTER(Registers.L)),
    ADC_A(0x8F, (processor, opcode) -> processor.ADC_REGISTER(Registers.ACCUMULATOR)),
    ADC_M(0x8E, (processor, opcode) -> processor.ADC_M()),

    SUB_B(0x90, (processor, opcode) -> processor.SUB_REGISTER(Registers.B)),
    SUB_C(0x91, (processor, opcode) -> processor.SUB_REGISTER(Registers.C)),
    SUB_D(0x92, (processor, opcode) -> processor.SUB_REGISTER(Registers.D)),
    SUB_E(0x93, (processor, opcode) -> processor.SUB_REGISTER(Registers.E)),
    SUB_H(0x94, (processor, opcode) -> processor.SUB_REGISTER(Registers.H)),
    SUB_L(0x95, (processor, opcode) -> processor.SUB_REGISTER(Registers.L)),
    SUB_M(0x96, (processor, opcode) -> processor.SUB_M()),
    SUB_A(0x97, (processor, opcode) -> processor.SUB_REGISTER(Registers.ACCUMULATOR)),

    SBB_B(0x98, (processor, opcode) -> processor.SBB_REGISTER(Registers.B)),
    SBB_C(0x99, (processor, opcode) -> processor.SBB_REGISTER(Registers.C)),
    SBB_D(0x9A, (processor, opcode) -> processor.SBB_REGISTER(Registers.D)),
    SBB_E(0x9B, (processor, opcode) -> processor.SBB_REGISTER(Registers.E)),
    SBB_H(0x9C, (processor, opcode) -> processor.SBB_REGISTER(Registers.H)),
    SBB_L(0x9D, (processor, opcode) -> processor.SBB_REGISTER(Registers.L)),
    SBB_A(0x9F, (processor, opcode) -> processor.SBB_REGISTER(Registers.ACCUMULATOR)),
    SBB_M(0x9E, (processor, opcode) -> processor.SBB_M()),

    ANA_B(0xA0, (processor, opcode) -> processor.ANA_REGISTER(Registers.B)),
    ANA_C(0xA1, (processor, opcode) -> processor.ANA_REGISTER(Registers.C)),
    ANA_D(0xA2, (processor, opcode) -> processor.ANA_REGISTER(Registers.D)),
    ANA_E(0xA3, (processor, opcode) -> processor.ANA_REGISTER(Registers.E)),
    ANA_H(0xA4, (processor, opcode) -> processor.ANA_REGISTER(Registers.H)),
    ANA_L(0xA5, (processor, opcode) -> processor.ANA_REGISTER(Registers.L)),
    ANA_A(0xA7, (processor, opcode) -> processor.ANA_REGISTER(Registers.ACCUMULATOR)),
    ANA_M(0xA6, (processor, opcode) -> processor.ANA_M()),

    XRA_B(0xA8, (processor, opcode) -> processor.XRA_REGISTER(Registers.B)),
    XRA_C(0xA9, (processor, opcode) -> processor.XRA_REGISTER(Registers.C)),
    XRA_D(0xAA, (processor, opcode) -> processor.XRA_REGISTER(Registers.D)),
    XRA_E(0xAB, (processor, opcode) -> processor.XRA_REGISTER(Registers.E)),
    XRA_H(0xAC, (processor, opcode) -> processor.XRA_REGISTER(Registers.H)),
    XRA_L(0xAD, (processor, opcode) -> processor.XRA_REGISTER(Registers.L)),
    XRA_A(0xAF, (processor, opcode) -> processor.XRA_REGISTER(Registers.ACCUMULATOR)),
    XRA_M(0xAE, (processor, opcode) -> processor.XRA_M()),

    ORA_B(0xB0, (processor, opcode) -> processor.ORA_REGISTER(Registers.B)),
    ORA_C(0xB1, (processor, opcode) -> processor.ORA_REGISTER(Registers.C)),
    ORA_D(0xB2, (processor, opcode) -> processor.ORA_REGISTER(Registers.D)),
    ORA_E(0xB3, (processor, opcode) -> processor.ORA_REGISTER(Registers.E)),
    ORA_H(0xB4, (processor, opcode) -> processor.ORA_REGISTER(Registers.H)),
    ORA_L(0xB5, (processor, opcode) -> processor.ORA_REGISTER(Registers.L)),
    ORA_A(0xB7, (processor, opcode) -> processor.ORA_REGISTER(Registers.ACCUMULATOR)),
    ORA_M(0xB6, (processor, opcode) -> processor.ORA_M()),

    CMP_B(0xB8, (processor, opcode) -> processor.CMP_REGISTER(Registers.B)),
    CMP_C(0xB9, (processor, opcode) -> processor.CMP_REGISTER(Registers.C)),
    CMP_D(0xBA, (processor, opcode) -> processor.CMP_REGISTER(Registers.D)),
    CMP_E(0xBB, (processor, opcode) -> processor.CMP_REGISTER(Registers.E)),
    CMP_H(0xBC, (processor, opcode) -> processor.CMP_REGISTER(Registers.H)),
    CMP_L(0xBD, (processor, opcode) -> processor.CMP_REGISTER(Registers.L)),
    CMP_A(0xBF, (processor, opcode) -> processor.CMP_REGISTER(Registers.ACCUMULATOR)),
    CMP_M(0xBE, (processor, opcode) -> processor.CMP_M()),

    MOV_B_B(0x40, (processor, opcode) -> processor.MOV_REG_REG(Registers.B, Registers.B)),
    MOV_B_C(0x41, (processor, opcode) -> processor.MOV_REG_REG(Registers.B, Registers.C)),
    MOV_B_D(0x42, (processor, opcode) -> processor.MOV_REG_REG(Registers.B, Registers.D)),
    MOV_B_E(0x43, (processor, opcode) -> processor.MOV_REG_REG(Registers.B, Registers.E)),
    MOV_B_H(0x44, (processor, opcode) -> processor.MOV_REG_REG(Registers.B, Registers.H)),
    MOV_B_L(0x45, (processor, opcode) -> processor.MOV_REG_REG(Registers.B, Registers.L)),
    MOV_B_A(0x47, (processor, opcode) -> processor.MOV_REG_REG(Registers.B, Registers.ACCUMULATOR)),
    MOV_B_M(0x46, (processor, opcode) -> processor.MOV_REG_MEMORY(Registers.B)),

    MOV_C_B(0x48, (processor, opcode) -> processor.MOV_REG_REG(Registers.C, Registers.B)),
    MOV_C_C(0x49, (processor, opcode) -> processor.MOV_REG_REG(Registers.C, Registers.C)),
    MOV_C_D(0x4A, (processor, opcode) -> processor.MOV_REG_REG(Registers.C, Registers.D)),
    MOV_C_E(0x4B, (processor, opcode) -> processor.MOV_REG_REG(Registers.C, Registers.E)),
    MOV_C_H(0x4C, (processor, opcode) -> processor.MOV_REG_REG(Registers.C, Registers.H)),
    MOV_C_L(0x4D, (processor, opcode) -> processor.MOV_REG_REG(Registers.C, Registers.L)),
    MOV_C_A(0x4F, (processor, opcode) -> processor.MOV_REG_REG(Registers.C, Registers.ACCUMULATOR)),
    MOV_C_M(0x4E, (processor, opcode) -> processor.MOV_REG_MEMORY(Registers.C)),

    MOV_D_B(0x50, (processor, opcode) -> processor.MOV_REG_REG(Registers.D, Registers.B)),
    MOV_D_C(0x51, (processor, opcode) -> processor.MOV_REG_REG(Registers.D, Registers.C)),
    MOV_D_D(0x52, (processor, opcode) -> processor.MOV_REG_REG(Registers.D, Registers.D)),
    MOV_D_E(0x53, (processor, opcode) -> processor.MOV_REG_REG(Registers.D, Registers.E)),
    MOV_D_H(0x54, (processor, opcode) -> processor.MOV_REG_REG(Registers.D, Registers.H)),
    MOV_D_L(0x55, (processor, opcode) -> processor.MOV_REG_REG(Registers.D, Registers.L)),
    MOV_D_A(0x57, (processor, opcode) -> processor.MOV_REG_REG(Registers.D, Registers.ACCUMULATOR)),
    MOV_D_M(0x56, (processor, opcode) -> processor.MOV_REG_MEMORY(Registers.D)),
    MOV_E_B(0x58, (processor, opcode) -> processor.MOV_REG_REG(Registers.E, Registers.B)),
    MOV_E_C(0x59, (processor, opcode) -> processor.MOV_REG_REG(Registers.E, Registers.C)),
    MOV_E_D(0x5A, (processor, opcode) -> processor.MOV_REG_REG(Registers.E, Registers.D)),
    MOV_E_E(0x5B, (processor, opcode) -> processor.MOV_REG_REG(Registers.E, Registers.E)),
    MOV_E_H(0x5C, (processor, opcode) -> processor.MOV_REG_REG(Registers.E, Registers.H)),
    MOV_E_L(0x5D, (processor, opcode) -> processor.MOV_REG_REG(Registers.E, Registers.L)),
    MOV_E_A(0x5F, (processor, opcode) -> processor.MOV_REG_REG(Registers.E, Registers.ACCUMULATOR)),
    MOV_E_M(0x5E, (processor, opcode) -> processor.MOV_REG_MEMORY(Registers.E)),
    MOV_H_B(0x60, (processor, opcode) -> processor.MOV_REG_REG(Registers.H, Registers.B)),
    MOV_H_C(0x61, (processor, opcode) -> processor.MOV_REG_REG(Registers.H, Registers.C)),
    MOV_H_D(0x62, (processor, opcode) -> processor.MOV_REG_REG(Registers.H, Registers.D)),
    MOV_H_E(0x63, (processor, opcode) -> processor.MOV_REG_REG(Registers.H, Registers.E)),
    MOV_H_H(0x64, (processor, opcode) -> processor.MOV_REG_REG(Registers.H, Registers.H)),
    MOV_H_L(0x65, (processor, opcode) -> processor.MOV_REG_REG(Registers.H, Registers.L)),
    MOV_H_A(0x67, (processor, opcode) -> processor.MOV_REG_REG(Registers.H, Registers.ACCUMULATOR)),
    MOV_H_M(0x66, (processor, opcode) -> processor.MOV_REG_MEMORY(Registers.H)),
    MOV_L_B(0x68, (processor, opcode) -> processor.MOV_REG_REG(Registers.L, Registers.B)),
    MOV_L_C(0x69, (processor, opcode) -> processor.MOV_REG_REG(Registers.L, Registers.C)),
    MOV_L_D(0x6A, (processor, opcode) -> processor.MOV_REG_REG(Registers.L, Registers.D)),
    MOV_L_E(0x6B, (processor, opcode) -> processor.MOV_REG_REG(Registers.L, Registers.E)),
    MOV_L_H(0x6C, (processor, opcode) -> processor.MOV_REG_REG(Registers.L, Registers.H)),
    MOV_L_L(0x6D, (processor, opcode) -> processor.MOV_REG_REG(Registers.L, Registers.L)),
    MOV_L_A(0x6F, (processor, opcode) -> processor.MOV_REG_REG(Registers.L, Registers.ACCUMULATOR)),
    MOV_L_M(0x6E, (processor, opcode) -> processor.MOV_REG_MEMORY(Registers.L)),
    MOV_M_B(0x70, (processor, opcode) -> processor.MOV_MEMORY_REG(Registers.B)),
    MOV_M_C(0x71, (processor, opcode) -> processor.MOV_MEMORY_REG(Registers.C)),
    MOV_M_D(0x72, (processor, opcode) -> processor.MOV_MEMORY_REG(Registers.D)),
    MOV_M_E(0x73, (processor, opcode) -> processor.MOV_MEMORY_REG(Registers.E)),
    MOV_M_H(0x74, (processor, opcode) -> processor.MOV_MEMORY_REG(Registers.H)),
    MOV_M_L(0x75, (processor, opcode) -> processor.MOV_MEMORY_REG(Registers.L)),
    MOV_M_A(0x77, (processor, opcode) -> processor.MOV_MEMORY_REG(Registers.ACCUMULATOR)),
    MOV_A_B(0x78, (processor, opcode) -> processor.MOV_REG_REG(Registers.ACCUMULATOR, Registers.B)),
    MOV_A_C(0x79, (processor, opcode) -> processor.MOV_REG_REG(Registers.ACCUMULATOR, Registers.C)),
    MOV_A_D(0x7A, (processor, opcode) -> processor.MOV_REG_REG(Registers.ACCUMULATOR, Registers.D)),
    MOV_A_E(0x7B, (processor, opcode) -> processor.MOV_REG_REG(Registers.ACCUMULATOR, Registers.E)),
    MOV_A_H(0x7C, (processor, opcode) -> processor.MOV_REG_REG(Registers.ACCUMULATOR, Registers.H)),
    MOV_A_L(0x7D, (processor, opcode) -> processor.MOV_REG_REG(Registers.ACCUMULATOR, Registers.L)),
    MOV_A_A(0x7F, (processor, opcode) -> processor.MOV_REG_REG(Registers.ACCUMULATOR, Registers.ACCUMULATOR)),
    MOV_A_M(0x7E, (processor, opcode) -> processor.MOV_REG_MEMORY(Registers.ACCUMULATOR)),

    HLT(0x76, (processor, opcode) -> processor.HLT()),

    POP_B(0xC1,   (processor, opcode) -> processor.POP_REGS(Registers.B, Registers.C)),
    POP_D(0xD1,   (processor, opcode) -> processor.POP_REGS(Registers.D, Registers.E)),
    POP_H(0xE1,   (processor, opcode) -> processor.POP_REGS(Registers.H, Registers.L)),
    POP_PSW(0xF1, (processor, opcode) -> processor.POP_PSW()),

    PUSH_B(0xC5,   (processor, opcode) -> processor.PUSH_REGS(Registers.B, Registers.C)),
    PUSH_D(0xD5,   (processor, opcode) -> processor.PUSH_REGS(Registers.D, Registers.E)),
    PUSH_H(0xE5,   (processor, opcode) -> processor.PUSH_REGS(Registers.H, Registers.L)),
    PUSH_PSW(0xF5, (processor, opcode) -> processor.PUSH_PSW()),

    RNZ(0xC0, (processor, opcode) -> processor.RNZ()),
    RNC(0xD0, (processor, opcode) -> processor.RNC()),
    RPO(0xE0, (processor, opcode) -> processor.RPO()),
    RP(0xF0,  (processor, opcode) -> processor.RP()),

    JNZ(0xC2, (processor, opcode) -> processor.JNZ()),
    JNC(0xD2, (processor, opcode) -> processor.JNC()),
    JPO(0xE2, (processor, opcode) -> processor.JPO()),
    JP(0xF2,  (processor, opcode) -> processor.JP()),

    JMP(0xC3,  (processor, opcode) -> processor.JMP()),
    XTHL(0xE3, (processor, opcode) -> processor.XTHL()),
    DI(0xF3,   (processor, opcode) -> processor.DI()),

    CNZ(0xC4, (processor, opcode) -> processor.CNZ()),
    CNC(0xD4, (processor, opcode) -> processor.CNC()),
    CPO(0xE4, (processor, opcode) -> processor.CPO()),
    CP(0xF4,  (processor, opcode) -> processor.CP()),

    ADI(0xC6, (processor, opcode) -> processor.ADI()),
    SUI(0xD6, (processor, opcode) -> processor.SUI()),
    ANI(0xE6, (processor, opcode) -> processor.ANI()),
    ORI(0xF6, (processor, opcode) -> processor.ORI()),

    RST_0(0xC7, (processor, opcode) -> processor.RST_VALUE(0)),
    RST_1(0xCF, (processor, opcode) -> processor.RST_VALUE(1)),
    RST_2(0xD7, (processor, opcode) -> processor.RST_VALUE(2)),
    RST_3(0xDF, (processor, opcode) -> processor.RST_VALUE(3)),
    RST_4(0xE7, (processor, opcode) -> processor.RST_VALUE(4)),
    RST_5(0xEF, (processor, opcode) -> processor.RST_VALUE(5)),
    RST_6(0xF7, (processor, opcode) -> processor.RST_VALUE(6)),
    RST_7(0xFF, (processor, opcode) -> processor.RST_VALUE(7)),

    RZ(0xC8,   (processor, opcode) -> processor.RZ()),
    RC(0xD8,   (processor, opcode) -> processor.RC()),
    RPE(0xE8,  (processor, opcode) -> processor.RPE()),
    RM(0xF8,   (processor, opcode) -> processor.RM()),

    RET(0xC9,  (processor, opcode) -> processor.RET()),
    PCHL(0xE9, (processor, opcode) -> processor.PCHL()),
    SPHL(0xF9, (processor, opcode) -> processor.SPHL()),

    JZ(0xCA,  (processor, opcode) -> processor.JZ()),
    JC(0xDA,  (processor, opcode) -> processor.JC()),
    JPE(0xEA, (processor, opcode) -> processor.JPE()),
    JM(0xFA,  (processor, opcode) -> processor.JM()),

    IN(0xDB, null),
    OUT(0xD3,null),

    XCHG(0xEB, (processor, opcode) -> processor.XCHG()),
    EI(0xFB,   (processor, opcode) -> processor.EI()),

    CZ(0xCC,  (processor, opcode) -> processor.CZ()),
    CC(0xDC,  (processor, opcode) -> processor.CC()),
    CPE(0xEC, (processor, opcode) -> processor.CPE()),
    CM(0xFC,  (processor, opcode) -> processor.CM()),

    CALL(0xCD, (processor, opcode) -> processor.CALL()),

    ACI(0xCE, (processor, opcode) -> processor.ACI()),
    SBI(0xDE, (processor, opcode) -> processor.SBI()),
    XRI(0xEE, (processor, opcode) -> processor.XRI()),
    CPI(0xFE, (processor, opcode) -> processor.CPI()),
    ;

    private final int opcode;
    private final InstructionExecutor executor;

    Instruction (int opcode, InstructionExecutor executor) {
        this.opcode = opcode;
        this.executor = executor;
    }

    private static final IntMap<Instruction> OPCODE_MAPPING = new IntMap<>();

    static {
        OPCODE_MAPPING.clear();
        for (Instruction instruction : Instruction.values()) {
            OPCODE_MAPPING.put(instruction.opcode, instruction);
        }
    }

    public static Instruction fromOpcode (int opcode) {
        return OPCODE_MAPPING.get(opcode);
    }

    public int execute (Processor processor) {
        return executor.execute(processor, opcode);
    }
}

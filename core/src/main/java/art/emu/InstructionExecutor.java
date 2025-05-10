package art.emu;

public interface InstructionExecutor {
    int execute (Processor processor, int opcode);
}

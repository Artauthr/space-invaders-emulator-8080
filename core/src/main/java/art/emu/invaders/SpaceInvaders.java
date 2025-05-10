package art.emu.invaders;

import art.emu.Memory;
import art.emu.Processor;
import art.emu.Registers;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Disposable;

public class SpaceInvaders extends InputAdapter implements Disposable {
    private final int[] ports = new int[8];

    // dedicated shift register
    private int shift0, shift1, shiftOffset;

    private final Processor processor;
    private final SpaceInvadersDisplay display;

    public static final int V_RAM_START = 0x2400, V_RAM_END = 0x3FFF;

    private static final int CPU_FREQ = 2_000_000;   // 2 MHz
    private static final int CYCLES_PER_FRAME = CPU_FREQ / 60;        // ≈33 333
    private static final int HALF_FRAME_CYCLES = CYCLES_PER_FRAME / 2; // ≈16 666

    public SpaceInvaders () {
        processor = new Processor();
        processor.setHardwareIOExecutor(new Processor.HardwareIOExecutor() {
            @Override
            public void hardwareIN (Processor processor, int port) {
                processor.writeRegisterValue(Registers.ACCUMULATOR, (byte) readPort(port));
            }
            @Override
            public void hardwareOUT (Processor processor, int port) {
                writePort(port, processor.getRegisterValue(Registers.ACCUMULATOR));
            }
        });

        display = new SpaceInvadersDisplay();
        loadGameRom(processor);
        Gdx.input.setInputProcessor(this);
    }

    private static final float FRAME_TIME = 1f/60f;
    private float accumulator = 0f;

    public void render(float delta) {
        delta = Math.min(delta, 0.25f);

        accumulator += delta;

        // accumulate to not run giga fast
        while (accumulator >= FRAME_TIME) {
            renderFrame();
            accumulator -= FRAME_TIME;
        }
    }

    private void renderFrame () {
        // we must fire 2 interrupts.
        // one in the middle of the frame, other at the end

        int halfCycles = 0;
        while (halfCycles < HALF_FRAME_CYCLES) {
            halfCycles += processor.step();
        }

        if (processor.isInterruptsEnabled()) {
            processor.RST_VALUE(1); // mid frame interrupt
        }

        int secondHalf = 0;
        while (secondHalf <= HALF_FRAME_CYCLES) {
            secondHalf += processor.step();
        }

        if (processor.isInterruptsEnabled()) {
            processor.RST_VALUE(2); // end of the frame interrupt
        }

        display.draw(processor);
    }

    public void loadGameRom (Processor processor) {
        FileHandle romH = Gdx.files.internal("invaders/invaders.h");
        FileHandle romG = Gdx.files.internal("invaders/invaders.g");
        FileHandle romF = Gdx.files.internal("invaders/invaders.f");
        FileHandle romE = Gdx.files.internal("invaders/invaders.e");

        byte[] bytesH = romH.readBytes();
        byte[] bytesG = romG.readBytes();
        byte[] bytesF = romF.readBytes();
        byte[] bytesE = romE.readBytes();

        Memory memory = processor.getMemory();

        System.arraycopy(bytesH, 0, memory.getMemoryBytes(), 0x0000, bytesH.length);
        System.arraycopy(bytesG, 0, memory.getMemoryBytes(), 0x0800, bytesG.length);
        System.arraycopy(bytesF, 0, memory.getMemoryBytes(), 0x1000, bytesF.length);
        System.arraycopy(bytesE, 0, memory.getMemoryBytes(), 0x1800, bytesE.length);
    }

    private int readPort(int port) {
        if (port == 3) {  // read shift data
            int v = (shift1 << 8) | shift0;
            return (v >> (8 - shiftOffset)) & 0xFF;
        }
        return ports[port] & 0xFF;
    }

    private void writePort (int port, int value) {
        switch (port) {
            case 2:  // write shift offset
                shiftOffset = value & 0x07;
                break;
            case 4:  // write shift register
                shift0 = shift1;
                shift1 = value & 0xFF;
                break;
            default:
                ports[port] = value & 0xFF;
        }
    }

    // input constants
    private static final int CREDIT = 1;
    private static final int START_2P = (1 << 1);
    private static final int START_1P = (1 << 2);
    private static final int SHOT_1P = (1 << 4);
    private static final int LEFT_1P = (1 << 5);
    private static final int RIGHT_1P = (1 << 6);
    private static final int TILT = (1 << 2);
    private static final int SHOT_2P = (1 << 4);
    private static final int LEFT_2P = (1 << 5);
    private static final int RIGHT_2P = (1 << 6);

    @Override
    public boolean keyDown(int key) {
        switch (key) {
            case Input.Keys.NUM_1:
                ports[1] |= START_1P;
                return true;
            case Input.Keys.C:
                ports[1] |= CREDIT;
                return true;
            case Input.Keys.T:
                ports[1] |= TILT;
                return true;
            case Input.Keys.SPACE:
                ports[1] |= SHOT_1P;
                ports[2] |= SHOT_2P;
                return true;
            case Input.Keys.D:
                ports[1] |= RIGHT_1P;
                ports[2] |= RIGHT_2P;
                return true;
            case Input.Keys.A:
                ports[1] |= LEFT_1P;
                ports[2] |= LEFT_2P;
                return true;
            case Input.Keys.NUM_2:
                ports[1] |= START_2P;
                return true;
        }
        return false;
    }

    @Override
    public boolean keyUp(int key) {
        switch (key) {
            case Input.Keys.NUM_1:
                ports[1] &= ~START_1P;
                return true;
            case Input.Keys.C:
                ports[1] &= ~CREDIT;
                return true;
            case Input.Keys.T:
                ports[1] &= ~TILT;
                return true;
            case Input.Keys.SPACE:
                ports[1] &= ~SHOT_1P;
                ports[2] &= ~SHOT_2P;
                return true;
            case Input.Keys.D:
                ports[1] &= ~RIGHT_1P;
                ports[2] &= ~RIGHT_2P;
                return true;
            case Input.Keys.A:
                ports[1] &= ~LEFT_1P;
                ports[2] &= ~LEFT_2P;
                return true;
            case Input.Keys.NUM_2:
                ports[2] &= ~START_2P;
                return true;
        }
        return false;
    }

    @Override
    public void dispose() {
        display.dispose();
    }
}

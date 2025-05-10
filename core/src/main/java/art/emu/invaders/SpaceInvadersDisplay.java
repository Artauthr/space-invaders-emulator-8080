package art.emu.invaders;

import art.emu.Processor;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class SpaceInvadersDisplay implements Disposable {
    private final SpriteBatch batch;
    private final ScreenViewport viewport;

    public static final int SCREEN_WIDTH = 256;
    public static final int SCREEN_HEIGHT = 224;

    private final Pixmap pixmap;
    private final Texture texture;

    private final ShaderProgram scanlineShader;

    public SpaceInvadersDisplay () {
        batch = new SpriteBatch();

        final OrthographicCamera camera = new OrthographicCamera(SCREEN_WIDTH, SCREEN_HEIGHT);
        camera.position.set((SCREEN_WIDTH) / 2f, (SCREEN_HEIGHT) / 2f, 0);
        camera.update();

        viewport = new ScreenViewport(camera);

        pixmap = new Pixmap(SCREEN_WIDTH, SCREEN_HEIGHT, Pixmap.Format.RGBA8888);
        texture = new Texture(pixmap);

        scanlineShader = new ShaderProgram(
            Gdx.files.internal("shaders/scanline.vert"),
            Gdx.files.internal("shaders/scanline.frag"));

        batch.setShader(scanlineShader);
    }

    public void draw(Processor processor) {
        ScreenUtils.clear(0, 0, 0, 1f);

        // clear the pixmap
        pixmap.setColor(0f, 0f, 0f, 1f);
        pixmap.fill();

        byte[] mem = processor.getMemory().getMemoryBytes();
        int index = 0;
        for (int addr = SpaceInvaders.V_RAM_START; addr <= SpaceInvaders.V_RAM_END; addr++) {
            int memByte = mem[addr] & 0xFF;

            for (int bit = 0; bit < 8; bit++, index++) {
                boolean on = ((memByte >> bit) & 1) != 0;
                int x = index % SCREEN_WIDTH;
                int y = index / SCREEN_WIDTH;

                if (on) {
                    if (x < 64) { // we use X because our screen is rotated
                        pixmap.drawPixel(x, y, 0x00FF00FF); // green
                    } else {
                        pixmap.drawPixel(x, y, 0xFFFFFFFF);
                    }
                } else {
                    pixmap.drawPixel(x, y, 0x000000FF);
                }
            }
        }

        texture.draw(pixmap, 0, 0);

        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        batch.draw(texture, 0, 0,
            SCREEN_WIDTH * 0.5f, SCREEN_HEIGHT * 0.5f,
            SCREEN_WIDTH, SCREEN_HEIGHT,
            1f, 1f,
            90f, 0, 0,
            texture.getWidth(), texture.getHeight(),
            false, false);
        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        scanlineShader.dispose();
        pixmap.dispose();
    }
}

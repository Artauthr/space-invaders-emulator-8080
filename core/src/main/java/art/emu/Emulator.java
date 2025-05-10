package art.emu;

import art.emu.invaders.SpaceInvaders;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;

public class Emulator extends ApplicationAdapter {
    private SpaceInvaders spaceInvaders;

    @Override
    public void create() {
        spaceInvaders = new SpaceInvaders();
    }

    @Override
    public void render() {
        spaceInvaders.render(Gdx.graphics.getDeltaTime());
    }

    @Override
    public void dispose() {
        spaceInvaders.dispose();
    }
}

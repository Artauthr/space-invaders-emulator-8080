package art.emu.gwt;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import art.emu.Emulator;

/** Launches the GWT application. */
public class GwtLauncher extends GwtApplication {
        @Override
        public GwtApplicationConfiguration getConfig () {
            // Resizable application, uses available space in browser with no padding:
            GwtApplicationConfiguration cfg = new GwtApplicationConfiguration(false);
            cfg.padVertical = 0;
            cfg.padHorizontal = 0;
//            cfg.s
//            return cfg;
            // If you want a fixed size application, comment out the above resizable section,
            // and uncomment below:configuration.setWindowedMode(768, 672);
            return new GwtApplicationConfiguration(768, 672);
        }

        @Override
        public ApplicationListener createApplicationListener () {
            return new Emulator();
        }
}

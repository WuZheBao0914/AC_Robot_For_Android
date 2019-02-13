package cn.edu.uestc.ac_core.app;

import com.joanzapata.iconify.IconFontDescriptor;
import com.joanzapata.iconify.Iconify;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * @author xuyang
 * @create 2019/1/21 15:05
 **/
public class Configurator {

    private static final HashMap<String, Object> AC_ROBOT_CONFIGS = new HashMap<>();
    private static final ArrayList<IconFontDescriptor> ICONS = new ArrayList<>();

    private Configurator() {
        AC_ROBOT_CONFIGS.put(ConfigType.CONFIG_READY.name(), false);
    }

    public static Configurator getInstance() {
        return HOlder.INSTANCE;
    }

    final HashMap<String, Object> getAcRobotConfigs() {
        return AC_ROBOT_CONFIGS;
    }

    private static class HOlder {
        private static final Configurator INSTANCE = new Configurator();
    }

    public final void configure() {
        initIcons();
        AC_ROBOT_CONFIGS.put(ConfigType.CONFIG_READY.name(), true);
    }

    public final Configurator withMasterUri(String uri) {
        AC_ROBOT_CONFIGS.put(ConfigType.MASTER_URI.name(), uri);
        return this;
    }

    private void initIcons() {
        if (ICONS.size() > 0) {
            final Iconify.IconifyInitializer initializer = Iconify.with(ICONS.get(0));
            for (int i = 1; i < ICONS.size(); i++) {
                initializer.with(ICONS.get(i));
            }
        }
    }

    public final Configurator withIcon(IconFontDescriptor descriptor){
        ICONS.add(descriptor);
        return this;
    }

    private void checkConfiguration() {
        final boolean isReady = (boolean) AC_ROBOT_CONFIGS.get(ConfigType.CONFIG_READY.name());
        if (!isReady) {
            throw new RuntimeException("Configuration is not ready,call configure!");
        }
    }

    @SuppressWarnings("unchecked")
    final <T> T getConfiguration(Enum<ConfigType> key) {
        checkConfiguration();
        return (T) AC_ROBOT_CONFIGS.get(key.name());
    }
}

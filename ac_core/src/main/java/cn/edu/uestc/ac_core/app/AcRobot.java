package cn.edu.uestc.ac_core.app;

import android.content.Context;

import java.util.HashMap;

/**
 * @author xuyang
 * @create 2019/1/21 15:05
 **/
public class AcRobot {

    public static Configurator init(Context context) {
        getConfigurations().put(
                ConfigType.APPLICATION_CONTEXT.name(), context.getApplicationContext());
        return Configurator.getInstance();
    }

    private static HashMap<String, Object> getConfigurations() {
        return Configurator.getInstance().getAcRobotConfigs();
    }

    public static Context getApplicationContext() {
        return (Context) getConfigurations().get(ConfigType.APPLICATION_CONTEXT.name());
    }
}

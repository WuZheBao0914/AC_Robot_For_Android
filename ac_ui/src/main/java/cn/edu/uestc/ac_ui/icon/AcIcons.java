package cn.edu.uestc.ac_ui.icon;

import com.joanzapata.iconify.Icon;

/**
 * @author xuyang
 * @create 2019/1/21 15:49
 **/
public enum AcIcons implements Icon {
    icon_acquisition('\ue605');

    private char character;

    AcIcons(char character) {
        this.character = character;
    }

    @Override
    public String key() {
        return name().replace('_', '-');
    }

    @Override
    public char character() {
        return character;
    }
}

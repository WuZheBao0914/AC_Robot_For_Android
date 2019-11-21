package cn.edu.uestc.ac_ui.icon;

import com.joanzapata.iconify.Icon;
import com.joanzapata.iconify.IconFontDescriptor;

public class FontACModule1 implements IconFontDescriptor {
    @Override
    public String ttfFileName() {
        return "iconfont1.ttf";
    }

    @Override
    public Icon[] characters() {
        return AcIcons.values();
    }
}

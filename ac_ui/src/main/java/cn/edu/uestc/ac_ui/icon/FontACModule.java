package cn.edu.uestc.ac_ui.icon;

import com.joanzapata.iconify.Icon;
import com.joanzapata.iconify.IconFontDescriptor;

/**
 * @author xuyang
 * @create 2019/1/21 15:49
 **/
public class FontACModule implements IconFontDescriptor {
    @Override
    public String ttfFileName() {
        return "iconfont.ttf";
    }

    @Override
    public Icon[] characters() {
        return AcIcons.values();
    }
}

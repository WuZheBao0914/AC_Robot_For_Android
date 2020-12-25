package cn.edu.uestc.ac_ui.icon;

import com.joanzapata.iconify.Icon;

/**
 * @author xuyang
 * @create 2019/1/21 15:49
 **/
public enum AcIcons implements Icon {
    icon_acquisition('\ue605'),
    icon_map('\ue713'),
    icon_track_barycenter('\ue609'),
    icon_track_bones('\uec5a'),
    icon_face_recognition('\ue655'),
    icon_face_detection('\ue605'),
    icon_emotion('\ue62d'),
    icon_voice('\ue773'),
    icon_settings('\ue60e'),
    icon_person('\ue630'),
    icon_info('\ue606'),
    icon_robot('\ue642'),
    icon_robot2('\ue6ac'),
    icon_robot3('\ue67b'),
    icon_about('\ue60f'),
    icon_add('\ue620'),
    icon_help('\ue600'),
    icon_pose('\ue6c1'),
    icon_object_track('\u2bd0');

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

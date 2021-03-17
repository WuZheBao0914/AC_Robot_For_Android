package cn.edu.uestc.cssl.entity;

import com.alibaba.fastjson.annotation.JSONField;

public class TrackingObjectInfo {
    @JSONField(name = "position")
    private float position[];
    @JSONField(name = "size")
    private float size[];
    @JSONField(name = "bitmap")
    private String bitmap;
    public TrackingObjectInfo(float pos[],float size[],String bit) {
        position=pos;
        this.size=size;
        bitmap=bit;
    }
    public double getmsg(){
        return this.position[0];
    }
}

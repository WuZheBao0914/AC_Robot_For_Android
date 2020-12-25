package cn.edu.uestc.cssl.entity;

import com.alibaba.fastjson.*;
import com.alibaba.fastjson.annotation.JSONField;

public class DetectedObject {
    @JSONField(name = "x1")
    private int x1;
    @JSONField(name = "y1")
    private int y1;
    @JSONField(name = "x2")
    private int x2;
    @JSONField(name = "y2")
    private int y2;
    @JSONField(name = "conf")
    private double conf;
    @JSONField(name = "cls_conf")
    private double cls_conf;
    @JSONField(name = "targetType")
    private int targetType;


    public DetectedObject() {
        super();
        this.x1 = 1;
        this.x2 = 1;
        this.y1 = 1;
        this.y2 = 1;
        this.targetType = 1;
        this.conf = 1;
        this.cls_conf = 1;
    }

    public DetectedObject(int x1, int y1, int x2, int y2, int targetType, double conf, double cls_conf) {
        super();
        this.x1 = x1;
        this.x2 = x2;
        this.y1 = y1;
        this.y2 = y2;
        this.conf = conf;
        this.cls_conf = cls_conf;
        this.targetType = targetType;

    }

    public int getX1() {
        return x1;
    }

    public void setX1(int x1) {
        this.x1 = x1;
    }

    public int getY1() {
        return y1;
    }

    public void setY1(int y1) {
        this.y1 = y1;
    }

    public int getX2() {
        return x2;
    }

    public void setX2(int x2) {
        this.x2 = x2;
    }

    public int getY2() {
        return y2;
    }

    public void setY2(int y2) {
        this.y2 = y2;
    }

    public int getTargetType() {
        return targetType;
    }

    public void setTargetType(int targetType) {
        this.targetType = targetType;
    }

    public double getConf() {
        return conf;
    }

    public void setConf(double conf) {
        this.conf = conf;
    }

    public double getCls_conf() {
        return cls_conf;
    }

    public void setCls_conf(double cls_conf) {
        this.cls_conf = cls_conf;
    }

    @Override
    public String toString() {
        String str = "Target Detected! TargetType : typename; Conf:" + cls_conf;
        return str;
    }
}

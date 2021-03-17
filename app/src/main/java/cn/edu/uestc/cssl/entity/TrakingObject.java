package cn.edu.uestc.cssl.entity;

import com.alibaba.fastjson.*;
import com.alibaba.fastjson.annotation.JSONField;

public class TrakingObject {
    @JSONField(name = "central_x")
    private double central_x;
    @JSONField(name = "central_y")
    private double central_y;
    @JSONField(name = "size_width")
    private double size_width;
    @JSONField(name = "size_height")
    private double size_height;


    public TrakingObject() {
        super();
        this.central_x = 1;
        this.central_y = 1;
        this.size_width = 1;
        this.size_height = 1;
    }

    public TrakingObject(int central_x, int central_y, int size_width, int size_height) {
        super();
        this.central_x = central_x;
        this.central_y = central_y;
        this.size_width = size_width;
        this.size_height = size_height;
    }

    public double getCentral_x() {
        return central_x;
    }

    public void setCentral_x(int central_x) {
        this.central_x = central_x;
    }

    public double getCentral_y() {
        return central_y;
    }

    public void setCentral_y(int central_y) {
        this.central_y = central_y;
    }

    public double getSize_width() {
        return size_width;
    }

    public void setSize_width(int size_width) {
        this.size_width = size_width;
    }

    public double getSize_height() {
        return size_height;
    }

    public void setSize_height(int size_height) {
        this.size_height = size_height;
    }
}

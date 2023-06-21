package cn.edu.uestc.cssl.vitalsign;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

import cn.edu.uestc.cssl.activities.R;

public class LineChart_Datasource {
    private List<Entry> yDataList;
    private static int max_length = 20;
    public LineChart_Datasource(){
        yDataList = new ArrayList<>();
    }
    public void add_NewData(float y){
        if(yDataList.size()>=max_length) {
            yDataList.remove(0);
            for (Entry x:yDataList){
                x.setX(x.getX()-1);
            }
        }
        yDataList.add(new Entry(yDataList.size(),y));
    }
    public List<Entry> get_yDataList(){
        return yDataList;
    }
}

package cn.edu.uestc.cssl.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import cn.edu.uestc.cssl.activities.R;
import cn.edu.uestc.cssl.delegates.AcDelegate;
import cn.edu.uestc.cssl.util.Utils;
import me.yokeyword.fragmentation_swipeback.SwipeBackFragment;
/**
* @Author:         xuyang
* @CreateDate:     2019/2/16 23:21
*/
public class AboutFragment extends AcDelegate {

    public static AboutFragment newInstance() {

        Bundle args = new Bundle();

        AboutFragment fragment = new AboutFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Object setLayout() {
        return R.layout.fragment_about;
    }

    @Override
    public void onBindView(@Nullable Bundle savedInstanceState, View rootView) {
        WebView webView = (WebView) rootView.findViewById(R.id.abouttxt);
        webView.loadData(Utils.readText(getActivity(), R.raw.about), "text/html", null);
    }
}

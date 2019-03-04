package cn.edu.uestc.cssl.fragments;

/**
 * Fragment containing the Setup tab of the Help Fragment.
 *
 * Created by kennethspear on 3/28/16.
 */
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import cn.edu.uestc.cssl.activities.R;
import cn.edu.uestc.cssl.util.Utils;


public class HelpUsingFragment extends Fragment {

    public static HelpUsingFragment newInstance() {

        Bundle args = new Bundle();

        HelpUsingFragment fragment = new HelpUsingFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_help_using, container, false);

        WebView webView =  view.findViewById(R.id.using_webview);
        webView.loadData(Utils.readText(getContext(), R.raw.using), "text/html", null);

        return view;
    }
}


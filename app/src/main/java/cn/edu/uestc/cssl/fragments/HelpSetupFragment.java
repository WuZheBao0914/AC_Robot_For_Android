package cn.edu.uestc.cssl.fragments;

/**
 * Fragment containing the Setup tab in the Help Fragment.
 * <p>
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

public class HelpSetupFragment extends Fragment {

    public static HelpSetupFragment newInstance() {

        Bundle args = new Bundle();

        HelpSetupFragment fragment = new HelpSetupFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_help_setup, container, false);

        WebView webView = view.findViewById(R.id.setup_webview);
        webView.loadData(Utils.readText(getContext(), R.raw.setup), "text/html", null);

        return view;
    }
}
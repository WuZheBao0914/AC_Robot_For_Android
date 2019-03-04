package cn.edu.uestc.cssl.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import cn.edu.uestc.cssl.activities.R;
import cn.edu.uestc.cssl.util.Utils;

public class HelpFaqFragment extends Fragment {


    public static HelpFaqFragment newInstance() {

        Bundle args = new Bundle();

        HelpFaqFragment fragment = new HelpFaqFragment();
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_help_faq, container, false);

        WebView webView = view.findViewById(R.id.faq_webview);
        webView.loadData(Utils.readText(getContext(), R.raw.faq), "text/html", null);

        return view;
    }
}


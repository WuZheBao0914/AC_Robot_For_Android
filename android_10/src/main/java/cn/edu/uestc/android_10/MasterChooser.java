/*
 * Copyright (C) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package cn.edu.uestc.android_10;



import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import com.google.common.base.Preconditions;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import org.ros.exception.RosRuntimeException;
import org.ros.internal.node.client.MasterClient;
import org.ros.internal.node.xmlrpc.XmlRpcTimeoutException;
import org.ros.namespace.GraphName;
import org.ros.node.NodeConfiguration;



/**
 * Allows the user to configue a master {@link URI} then it returns that
 * {@link URI} to the calling {@link AppCompatActivity}.
 * <p>
 * When this {@link AppCompatActivity} is started, the last used (or the default)
 * {@link URI} is displayed to the user.
 *
 * @author ethan.rublee@gmail.com (Ethan Rublee)
 * @author damonkohler@google.com (Damon Kohler)
 * @author munjaldesai@google.com (Munjal Desai)
 */


public class MasterChooser extends AppCompatActivity {
  private static final String PREFS_KEY_NAME = "URI_KEY";
  private static final String BAR_CODE_SCANNER_PACKAGE_NAME = "com.google.zxing.client.android.SCAN";
  private static final String CONNECTION_EXCEPTION_TEXT = "ECONNREFUSED";
  private static final String UNKNOW_HOST_TEXT = "UnknownHost";
  private static final int DEFAULT_PORT = 11311;
  private static final String RECENT_COUNT_KEY_NAME = "RECENT_MASTER_URI_COUNT";
  private static final String RECENT_PREFIX_KEY_NAME = "RECENT_MASTER_URI_";
  private static final int RECENT_MASTER_HISTORY_COUNT = 5;
  private String selectedInterface;
  private AutoCompleteTextView uriText;
  private Button connectButton;
  private LinearLayout connectionLayout;

  public MasterChooser() {
  }

  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.setContentView(R.layout.master_chooser);
    final Pattern uriPattern = MasterChooser.RosURIPattern.URI;
    this.uriText = this.findViewById(R.id.master_chooser_uri);
    this.connectButton = this.findViewById(R.id.master_chooser_ok);
    this.uriText.setThreshold(MasterChooser.RosURIPattern.HTTP_PROTOCOL_LENGTH);
    //todo 可能会有问题
    ArrayAdapter<String> uriAdapter = new ArrayAdapter(this, 17367057, this.getRecentMasterURIs());
    this.uriText.setAdapter(uriAdapter);
    this.uriText.addTextChangedListener(new TextWatcher() {
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        String uri = s.toString();
        if (!uriPattern.matcher(uri).matches()) {
          MasterChooser.this.uriText.setError("Please enter valid URI");
          MasterChooser.this.connectButton.setEnabled(false);
        } else {
          MasterChooser.this.uriText.setError((CharSequence)null);
          MasterChooser.this.connectButton.setEnabled(true);
        }

      }

      @Override
      public void afterTextChanged(Editable editable) {

      }

      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }


    });
    ListView interfacesList = this.findViewById(R.id.networkInterfaces);
    ArrayList list = new ArrayList();

    try {
      Iterator var6 = Collections.list(NetworkInterface.getNetworkInterfaces()).iterator();

      while(var6.hasNext()) {
        NetworkInterface networkInterface = (NetworkInterface)var6.next();
        if (networkInterface.isUp() && !networkInterface.isLoopback()) {
          list.add(networkInterface.getName());
        }
      }
    } catch (SocketException var8) {
      throw new RosRuntimeException(var8);
    }

    this.selectedInterface = "";
    MasterChooser.StableArrayAdapter adapter = new MasterChooser.StableArrayAdapter(this, 17367043, list);
    interfacesList.setAdapter(adapter);
    interfacesList.setOnItemClickListener(new OnItemClickListener() {


      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        MasterChooser.this.selectedInterface = parent.getItemAtPosition(position).toString();
        MasterChooser.this.toast("Using " + MasterChooser.this.selectedInterface + " interface.");
      }
    });
    String uri = this.getPreferences(0).getString("URI_KEY", NodeConfiguration.DEFAULT_MASTER_URI.toString());
    this.uriText.setText(uri);
    this.connectionLayout = this.findViewById(R.id.connection_layout);
  }

  public void onActivityResult(int requestCode, int resultCode, Intent intent) {
    if (requestCode == 0 && resultCode == -1) {
      String scanResultFormat = intent.getStringExtra("SCAN_RESULT_FORMAT");
      Preconditions.checkState(scanResultFormat.equals("TEXT_TYPE") || scanResultFormat.equals("QR_CODE"));
      String contents = intent.getStringExtra("SCAN_RESULT");
      this.uriText.setText(contents);
    }

  }

  public void onBackPressed() {
    this.moveTaskToBack(true);
  }

  public void okButtonClicked(View unused) {
    String tmpURI = this.uriText.getText().toString();
    Pattern portPattern = MasterChooser.RosURIPattern.PORT;
    if (!portPattern.matcher(tmpURI).find()) {
      tmpURI = String.format(Locale.getDefault(), "%s:%d/", tmpURI, 11311);
      this.uriText.setText(tmpURI);
    }

    this.uriText.setEnabled(false);
    this.connectButton.setEnabled(false);
    final String finalTmpURI = tmpURI;
    (new AsyncTask<Void, Void, Boolean>() {
      protected void onPreExecute() {
        MasterChooser.this.runOnUiThread(new Runnable() {
          public void run() {
            MasterChooser.this.connectionLayout.setVisibility(View.VISIBLE);
          }
        });
      }

      protected Boolean doInBackground(Void... params) {
        try {
          MasterClient masterClient = new MasterClient(new URI(finalTmpURI));
          masterClient.getUri(GraphName.of("android/master_chooser_activity"));
          MasterChooser.this.toast("Connected!");
          return true;
        } catch (URISyntaxException var4) {
          MasterChooser.this.toast("Invalid URI.");
          return false;
        } catch (XmlRpcTimeoutException var5) {
          MasterChooser.this.toast("Master unreachable!");
          return false;
        } catch (Exception var6) {
          String exceptionMessage = var6.getMessage();
          if (exceptionMessage.contains("ECONNREFUSED")) {
            MasterChooser.this.toast("Unable to communicate with master!");
          } else if (exceptionMessage.contains("UnknownHost")) {
            MasterChooser.this.toast("Unable to resolve URI hostname!");
          } else {
            MasterChooser.this.toast("Communication error!");
          }

          return false;
        }
      }

      protected void onPostExecute(Boolean result) {
        MasterChooser.this.runOnUiThread(new Runnable() {
          public void run() {
            MasterChooser.this.connectionLayout.setVisibility(View.GONE);
          }
        });
        if (result) {
          MasterChooser.this.addRecentMasterURI(finalTmpURI);
          Intent intent = MasterChooser.this.createNewMasterIntent(false, true);
          MasterChooser.this.setResult(-1, intent);
          MasterChooser.this.finish();
        } else {
          MasterChooser.this.connectButton.setEnabled(true);
          MasterChooser.this.uriText.setEnabled(true);
        }

      }
    }).execute(new Void[0]);
  }

  protected void toast(final String text) {
    this.runOnUiThread(new Runnable() {
      public void run() {
        Toast.makeText(MasterChooser.this, text, Toast.LENGTH_SHORT).show();
      }
    });
  }

  public void qrCodeButtonClicked(View unused) {
    Intent intent = new Intent("com.google.zxing.client.android.SCAN");
    intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
    if (!this.isQRCodeReaderInstalled(intent)) {
      this.startActivity(new Intent("android.intent.action.VIEW", Uri.parse("market://details?id=com.google.zxing.client.android")));
    } else {
      this.startActivityForResult(intent, 0);
    }

  }

  public void advancedCheckboxClicked(View view) {
    boolean checked = ((CheckBox)view).isChecked();
    LinearLayout advancedOptions = (LinearLayout)this.findViewById(R.id.advancedOptions);
    if (checked) {
      advancedOptions.setVisibility(View.VISIBLE);
    } else {
      advancedOptions.setVisibility(View.GONE);
    }

  }

  public Intent createNewMasterIntent(boolean newMaster, boolean isPrivate) {
    Intent intent = new Intent();
    String uri = this.uriText.getText().toString();
    intent.putExtra("ROS_MASTER_CREATE_NEW", newMaster);
    intent.putExtra("ROS_MASTER_PRIVATE", isPrivate);
    intent.putExtra("ROS_MASTER_URI", uri);
    intent.putExtra("ROS_MASTER_NETWORK_INTERFACE", this.selectedInterface);
    return intent;
  }

  public void newMasterButtonClicked(View unused) {
    this.setResult(-1, this.createNewMasterIntent(true, false));
    this.finish();
  }

  public void newPrivateMasterButtonClicked(View unused) {
    this.setResult(-1, this.createNewMasterIntent(true, true));
    this.finish();
  }

  public void cancelButtonClicked(View unused) {
    this.setResult(0);
    this.finish();
  }

  protected boolean isQRCodeReaderInstalled(Intent intent) {
    List<ResolveInfo> list = this.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
    return list.size() > 0;
  }

  private void addRecentMasterURI(String uri) {
    List<String> recentURIs = this.getRecentMasterURIs();
    if (!recentURIs.contains(uri)) {
      recentURIs.add(0, uri);
      if (recentURIs.size() > 5) {
        recentURIs = recentURIs.subList(0, 5);
      }
    }

    SharedPreferences.Editor editor = this.getPreferences(0).edit();
    editor.putString("URI_KEY", uri);

    for(int i = 0; i < recentURIs.size(); ++i) {
      editor.putString("RECENT_MASTER_URI_" + String.valueOf(i), (String)recentURIs.get(i));
    }

    editor.putInt("RECENT_MASTER_URI_COUNT", recentURIs.size());
    editor.apply();
  }

  private List<String> getRecentMasterURIs() {
    SharedPreferences prefs = this.getPreferences(0);
    int numRecent = prefs.getInt("RECENT_MASTER_URI_COUNT", 0);
    List<String> recentURIs = new ArrayList(numRecent);

    for(int i = 0; i < numRecent; ++i) {
      String uri = prefs.getString("RECENT_MASTER_URI_" + String.valueOf(i), "");
      if (!uri.isEmpty()) {
        recentURIs.add(uri);
      }
    }

    return recentURIs;
  }

  private static class RosURIPattern {
    private static final String WORD_BOUNDARY = "(?:\\b|$|^)";
    private static final String UCS_CHAR = "[ -\ud7ff豈-\ufdcfﷰ-\uffef\ud800\udc00-\ud83f\udffd\ud840\udc00-\ud87f\udffd\ud880\udc00-\ud8bf\udffd\ud8c0\udc00-\ud8ff\udffd\ud900\udc00-\ud93f\udffd\ud940\udc00-\ud97f\udffd\ud980\udc00-\ud9bf\udffd\ud9c0\udc00-\ud9ff\udffd\uda00\udc00-\uda3f\udffd\uda40\udc00-\uda7f\udffd\uda80\udc00-\udabf\udffd\udac0\udc00-\udaff\udffd\udb00\udc00-\udb3f\udffd\udb44\udc00-\udb7f\udffd&&[^ [ - ]\u2028\u2029 　]]";
    private static final String LABEL_CHAR = "a-zA-Z0-9[ -\ud7ff豈-\ufdcfﷰ-\uffef\ud800\udc00-\ud83f\udffd\ud840\udc00-\ud87f\udffd\ud880\udc00-\ud8bf\udffd\ud8c0\udc00-\ud8ff\udffd\ud900\udc00-\ud93f\udffd\ud940\udc00-\ud97f\udffd\ud980\udc00-\ud9bf\udffd\ud9c0\udc00-\ud9ff\udffd\uda00\udc00-\uda3f\udffd\uda40\udc00-\uda7f\udffd\uda80\udc00-\udabf\udffd\udac0\udc00-\udaff\udffd\udb00\udc00-\udb3f\udffd\udb44\udc00-\udb7f\udffd&&[^ [ - ]\u2028\u2029 　]]";
    private static final String IRI_LABEL = "[a-zA-Z0-9[ -\ud7ff豈-\ufdcfﷰ-\uffef\ud800\udc00-\ud83f\udffd\ud840\udc00-\ud87f\udffd\ud880\udc00-\ud8bf\udffd\ud8c0\udc00-\ud8ff\udffd\ud900\udc00-\ud93f\udffd\ud940\udc00-\ud97f\udffd\ud980\udc00-\ud9bf\udffd\ud9c0\udc00-\ud9ff\udffd\uda00\udc00-\uda3f\udffd\uda40\udc00-\uda7f\udffd\uda80\udc00-\udabf\udffd\udac0\udc00-\udaff\udffd\udb00\udc00-\udb3f\udffd\udb44\udc00-\udb7f\udffd&&[^ [ - ]\u2028\u2029 　]]](?:[a-zA-Z0-9[ -\ud7ff豈-\ufdcfﷰ-\uffef\ud800\udc00-\ud83f\udffd\ud840\udc00-\ud87f\udffd\ud880\udc00-\ud8bf\udffd\ud8c0\udc00-\ud8ff\udffd\ud900\udc00-\ud93f\udffd\ud940\udc00-\ud97f\udffd\ud980\udc00-\ud9bf\udffd\ud9c0\udc00-\ud9ff\udffd\uda00\udc00-\uda3f\udffd\uda40\udc00-\uda7f\udffd\uda80\udc00-\udabf\udffd\udac0\udc00-\udaff\udffd\udb00\udc00-\udb3f\udffd\udb44\udc00-\udb7f\udffd&&[^ [ - ]\u2028\u2029 　]]\\-]{0,61}[a-zA-Z0-9[ -\ud7ff豈-\ufdcfﷰ-\uffef\ud800\udc00-\ud83f\udffd\ud840\udc00-\ud87f\udffd\ud880\udc00-\ud8bf\udffd\ud8c0\udc00-\ud8ff\udffd\ud900\udc00-\ud93f\udffd\ud940\udc00-\ud97f\udffd\ud980\udc00-\ud9bf\udffd\ud9c0\udc00-\ud9ff\udffd\uda00\udc00-\uda3f\udffd\uda40\udc00-\uda7f\udffd\uda80\udc00-\udabf\udffd\udac0\udc00-\udaff\udffd\udb00\udc00-\udb3f\udffd\udb44\udc00-\udb7f\udffd&&[^ [ - ]\u2028\u2029 　]]]){0,1}";
    private static final Pattern IP_ADDRESS = Pattern.compile("((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[0-9]))");
    private static final String RELAXED_DOMAIN_NAME;
    private static final String HTTP_PROTOCOL = "(?i:http):\\/\\/";
    public static final int HTTP_PROTOCOL_LENGTH;
    private static final String PORT_NUMBER = "\\:\\d{1,5}\\/?";
    public static final Pattern URI;
    public static final Pattern PORT;

    private RosURIPattern() {
    }

    static {
      RELAXED_DOMAIN_NAME = "(?:(?:[a-zA-Z0-9[ -\ud7ff豈-\ufdcfﷰ-\uffef\ud800\udc00-\ud83f\udffd\ud840\udc00-\ud87f\udffd\ud880\udc00-\ud8bf\udffd\ud8c0\udc00-\ud8ff\udffd\ud900\udc00-\ud93f\udffd\ud940\udc00-\ud97f\udffd\ud980\udc00-\ud9bf\udffd\ud9c0\udc00-\ud9ff\udffd\uda00\udc00-\uda3f\udffd\uda40\udc00-\uda7f\udffd\uda80\udc00-\udabf\udffd\udac0\udc00-\udaff\udffd\udb00\udc00-\udb3f\udffd\udb44\udc00-\udb7f\udffd&&[^ [ - ]\u2028\u2029 　]]](?:[a-zA-Z0-9[ -\ud7ff豈-\ufdcfﷰ-\uffef\ud800\udc00-\ud83f\udffd\ud840\udc00-\ud87f\udffd\ud880\udc00-\ud8bf\udffd\ud8c0\udc00-\ud8ff\udffd\ud900\udc00-\ud93f\udffd\ud940\udc00-\ud97f\udffd\ud980\udc00-\ud9bf\udffd\ud9c0\udc00-\ud9ff\udffd\uda00\udc00-\uda3f\udffd\uda40\udc00-\uda7f\udffd\uda80\udc00-\udabf\udffd\udac0\udc00-\udaff\udffd\udb00\udc00-\udb3f\udffd\udb44\udc00-\udb7f\udffd&&[^ [ - ]\u2028\u2029 　]]\\-]{0,61}[a-zA-Z0-9[ -\ud7ff豈-\ufdcfﷰ-\uffef\ud800\udc00-\ud83f\udffd\ud840\udc00-\ud87f\udffd\ud880\udc00-\ud8bf\udffd\ud8c0\udc00-\ud8ff\udffd\ud900\udc00-\ud93f\udffd\ud940\udc00-\ud97f\udffd\ud980\udc00-\ud9bf\udffd\ud9c0\udc00-\ud9ff\udffd\uda00\udc00-\uda3f\udffd\uda40\udc00-\uda7f\udffd\uda80\udc00-\udabf\udffd\udac0\udc00-\udaff\udffd\udb00\udc00-\udb3f\udffd\udb44\udc00-\udb7f\udffd&&[^ [ - ]\u2028\u2029 　]]]){0,1}(?:\\.(?=\\S))?)+|" + IP_ADDRESS + ")";
      HTTP_PROTOCOL_LENGTH = "http://".length();
      URI = Pattern.compile("((?:\\b|$|^)(?:(?:(?i:http):\\/\\/)(?:" + RELAXED_DOMAIN_NAME + ")(?:" + "\\:\\d{1,5}\\/?" + ")?)" + "(?:\\b|$|^)" + ")");
      PORT = Pattern.compile("\\:\\d{1,5}\\/?");
    }
  }

  private class StableArrayAdapter extends ArrayAdapter<String> {
    HashMap<String, Integer> idMap = new HashMap();

    public StableArrayAdapter(Context context, int textViewResourceId, List<String> objects) {
      super(context, textViewResourceId, objects);

      for(int i = 0; i < objects.size(); ++i) {
        this.idMap.put(objects.get(i), i);
      }

    }

    public long getItemId(int position) {
      String item = (String)this.getItem(position);
      return (long)(Integer)this.idMap.get(item);
    }

    public boolean hasStableIds() {
      return true;
    }
  }
}


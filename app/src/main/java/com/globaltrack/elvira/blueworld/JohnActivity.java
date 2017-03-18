package com.globaltrack.elvira.blueworld;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.globaltrack.elvira.blueworld.extra.BluetoothDeviceItem;
import com.globaltrack.elvira.blueworld.extra.ISSPListenner;
import com.globaltrack.elvira.blueworld.extra.SSPClient;
import com.globaltrack.elvira.blueworld.extra.SSPServer;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class JohnActivity extends AppCompatActivity implements ISSPListenner {
    private static JohnActivity ins;

    private boolean blueWasEnabled;

    BluetoothAdapter bluetoothAdapter;

    private ListView discoveryListView;

    BlueBroadcastReceiver blueBroadcast;

    TextView loggerView;

    ScrollView loggerScroll;

    ConstraintLayout discoveryBoard;

    static SSPServer sspServer;

    private static boolean firstTime = false;

    private static int activeTab = 0;

    private boolean wasCleared;

    private ProgressBar descoverySpinner;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_john);
        accessLocationPermission();
        loggerView = (TextView) findViewById(R.id.logView);
        discoveryBoard = (ConstraintLayout) findViewById(R.id.discovery_board);
        loggerScroll = (ScrollView) findViewById(R.id.loggerScroll);
        discoveryListView = (ListView) findViewById(R.id.discovery_listview);
        descoverySpinner=(ProgressBar)findViewById(R.id.dicovery_spinner);
        descoverySpinner.setVisibility(View.INVISIBLE);
        registerForContextMenu(discoveryListView);
        discoveryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> myAdapter, View myView, int myItemInt, long mylng) {
                BluetoothDeviceItem selected = (BluetoothDeviceItem) discoveryListView.getItemAtPosition(myItemInt);
                if (selected != null) {
                    PopupWindow pop = createDiscoveryPopUp(selected);
                    pop.showAsDropDown(myView, -5, 0);
                }

            }
        });


        updateTabView();
        if (!firstTime) {
            InitialiseBluetooth();
            firstTime = true;
        }

        refresh();
    }

    private void refresh() {
        if (sspServer != null) {
            this.registerIntentAndFilter();
            sspServer.setSSPListenner(this);
        }
    }

    private void InitialiseBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null) {
            blueWasEnabled = bluetoothAdapter.isEnabled();
            if (!blueWasEnabled) {
                bluetoothAdapter.enable();
            }

            sspServer = new SSPServer(bluetoothAdapter);
            sspServer.startAccepting();
        }
    }

    private final int REQUEST_CODE_LOC = 11;

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.discovery_listview) {
            ListView lv = (ListView) v;
            AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) menuInfo;
            //YourObject obj = (YourObject) lv.getItemAtPosition(acmi.position);

            menu.add("One");
            menu.add("Two");
            menu.add("Three");
            //menu.add(obj.name);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void accessLocationPermission() {
        int accessCoarseLocation = checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION);
        int accessFineLocation = checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION);
        List<String> listRequestPermission = new ArrayList<String>();
        if (accessCoarseLocation != PackageManager.PERMISSION_GRANTED) {
            listRequestPermission.add(android.Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (accessFineLocation != PackageManager.PERMISSION_GRANTED) {
            listRequestPermission.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (!listRequestPermission.isEmpty()) {
            String[] strRequestPermission = listRequestPermission.toArray(new String[listRequestPermission.size()]);
            requestPermissions(strRequestPermission, REQUEST_CODE_LOC);
        }
    }

    public PopupWindow createDiscoveryPopUp(BluetoothDeviceItem selected) {
        String[] menus = new String[]{"Pair", "Unpair", "Connect"};
        final BluetoothDevice device = selected.getDevice();
        // initialize a pop up window type
        final PopupWindow popupWindow = new PopupWindow(this);

        // the drop down list is a list view
        final ListView popupListView = new ListView(this);
        // set our adapter and pass our pop up window contents
        popupListView.setAdapter(new ArrayAdapter<String>(this, R.layout.listview_item, menus));

        // set the item click listener
        popupListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            private void PerformAction(String action) {
                switch (action) {
                    case "Pair":
                        pairDevice(device);
                        break;
                    case "Unpair":
                        unpairDevice(device);
                        break;
                    case "Connect":
                        break;
                    case "Disconnect":
                        break;
                }
            }

            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            public void onItemClick(AdapterView<?> myAdapter, View myView, int myItemInt, long mylng) {
                // get the context and main activity to access variables
                Context mContext = myView.getContext();

                // add some animation when a list item was clicked
                Animation fadeInAnimation = AnimationUtils.loadAnimation(myView.getContext(), android.R.anim.fade_in);
                fadeInAnimation.setDuration(10);
                myView.startAnimation(fadeInAnimation);
                String StringString = (String) popupListView.getItemAtPosition(myItemInt);
                PerformAction(StringString);

                // dismiss the pop up
                popupWindow.dismiss();

                // get the text and set it as the button text

                //Toast.makeText(mContext, "Selected Positon is: " + myItemInt, 100).show();
            }
        });

        // some other visual settings
        popupWindow.setFocusable(true);
        popupWindow.setWidth(250);
        popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);

        // set the list view as pop up window content
        popupWindow.setContentView(popupListView);
        return popupWindow;
    }

    private void StartDecovery() {
        boolean clearToGo = true;
        if (bluetoothAdapter.isDiscovering()) {
            clearToGo = bluetoothAdapter.cancelDiscovery();
        }

        if (clearToGo) {
            discoveryListView.setAdapter(null);
            bluetoothAdapter.startDiscovery();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Make sure we're not doing discovery anymore
        if (bluetoothAdapter != null) {
            bluetoothAdapter.cancelDiscovery();
            // Unregister broadcast listeners
            this.unregisterReceiver(blueBroadcast);
        }
    }

    private void registerIntentAndFilter() {
        this.blueBroadcast = new BlueBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);

        registerReceiver(this.blueBroadcast, filter);
    }

    @Override
    public void OnSSPPost(SSPClient client, byte[] data) {
        final String text = new String(data);
        this.runOnUiThread(new Runnable() {
            public void run() {
                if (!wasCleared) {
                    loggerView.setText("");
                    wasCleared = true;
                }

                loggerView.append(text + System.getProperty("line.separator"));
            }
        });
    }

    private class BlueBroadcastReceiver extends BroadcastReceiver {
        private ArrayList<BluetoothDeviceItem> items = new ArrayList<BluetoothDeviceItem>();

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                descoverySpinner.setVisibility(View.VISIBLE);
                items.clear();
                ArrayAdapter adapter = (ArrayAdapter) (discoveryListView.getAdapter());
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }

                //Toast.makeText(getApplicationContext(), "We're starting", Toast.LENGTH_SHORT).show();
                discoveryListView.setAdapter(new ArrayAdapter<BluetoothDeviceItem>(getApplicationContext(), R.layout.listview_item, items));
                //discovery starts, we can show progress dialog or perform other tasks
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                descoverySpinner.setVisibility(View.INVISIBLE);
                //Toast.makeText(getApplicationContext(), "We're done", Toast.LENGTH_SHORT).show();
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //bluetooth device found
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                ////Toast.makeText(getApplicationContext(), device.getName() + " - " + device.getAddress(), Toast.LENGTH_SHORT).show();
                items.add(new BluetoothDeviceItem(device));
                ArrayAdapter adapter = (ArrayAdapter) (discoveryListView.getAdapter());
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
            } else if (BluetoothDevice.ACTION_PAIRING_REQUEST.equals(action)) {
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            } else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Toast.makeText(getApplicationContext(), device.getName() + " is now connected", Toast.LENGTH_SHORT).show();
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                switch (device.getBondState()) {
                    case BluetoothDevice.BOND_BONDED:
                        Toast.makeText(getApplicationContext(), device.getName() + " was paired successfully", Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothDevice.BOND_BONDING:
                        break;
                    case BluetoothDevice.BOND_NONE:
                        Toast.makeText(getApplicationContext(), device.getName() + " was unpaired", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void pairDevice(BluetoothDevice device) {
        byte[] pin = ByteBuffer.allocate(4).putInt(1234).array();
        try {
            Method m = device.getClass().getMethod("setPin", byte[].class);
            m.invoke(device, pin);
            try {
                device.getClass().getMethod("setPairingConfirmation", boolean.class).invoke(device, true);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            device.getClass().getMethod("createBond", (Class[]) null).invoke(device, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void unpairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("removeBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateTabView() {
        discoveryBoard.setVisibility(View.INVISIBLE);
        loggerScroll.setVisibility(View.INVISIBLE);
        switch (activeTab) {
            case 0:
                loggerScroll.setVisibility(View.VISIBLE);
                break;
            case 1:
                discoveryBoard.setVisibility(View.VISIBLE);
                break;
            default:
                activeTab = 0;
                break;
        }
    }

    public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.scan_action_button:
                ////Toast.makeText(getApplicationContext(), "Discovery Started", Toast.LENGTH_LONG).show();
                StartDecovery();
                break;
            case R.id.discovery_button:
                activeTab = 1;
                updateTabView();
                break;
            case R.id.logger_button:
                activeTab = 0;
                updateTabView();
                break;
            // even more buttons here
        }
    }
}
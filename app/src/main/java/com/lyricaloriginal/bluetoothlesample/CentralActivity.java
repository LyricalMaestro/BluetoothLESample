package com.lyricaloriginal.bluetoothlesample;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * セントラル画面
 */
public class CentralActivity extends AppCompatActivity {

    private BluetoothAdapter mAdapter;
    private ScanCallback mCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_central);
        mCallback = createScanCallback();

        BluetoothManager manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mAdapter = manager.getAdapter();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAdapter == null || mAdapter.isEnabled()) {
            mAdapter.getBluetoothLeScanner().startScan(mCallback);
        } else {
            Toast.makeText(this, "Bluetoothが有効ではありません。", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAdapter != null && mAdapter.isEnabled()) {
            mAdapter.getBluetoothLeScanner().stopScan(mCallback);
        }
    }

    private ScanCallback createScanCallback() {
        return new ScanCallback() {

            @Override
            public void onScanResult(int callbackType, final ScanResult result) {
                super.onScanResult(callbackType, result);
                Log.d(CentralActivity.class.getName(), "onScanResult");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        List<ScanResult> scanResults = new ArrayList<ScanResult>();
                        scanResults.add(result);
                        updateUi(scanResults);
                    }
                });
            }

            @Override
            public void onBatchScanResults(final List<ScanResult> results) {
                super.onBatchScanResults(results);
                Log.d(CentralActivity.class.getName(), "onBatchScanResults");
            }
        };
    }

    private void updateUi(List<ScanResult> results) {
        List<String> values = new ArrayList<>();
        for (ScanResult result : results) {
            StringBuilder sb = new StringBuilder();
            sb.append("Name : " + result.getDevice().getName() + "\r\n");
            sb.append("Address : " + result.getDevice().getAddress() + "\r\n");
            sb.append("RSSI : " + result.getRssi());
            values.add(sb.toString());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, values.toArray(new String[0]));

        ListView lv = (ListView) findViewById(android.R.id.list);
        lv.setAdapter(adapter);
    }

}

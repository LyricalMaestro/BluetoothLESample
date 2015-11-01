package com.lyricaloriginal.bluetoothlesample;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.content.Context;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import java.util.UUID;

/**
 * ペリファレルモード画面
 */
public class PeripheralActivity extends AppCompatActivity {

    //UUID
    private static final String ADVERTISE_UUID = "0000180a-0000-1000-8000-00805f9b34fb";

    //BLE
    private BluetoothAdapter mAdapter;
    private AdvertiseCallback mCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advertiser);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mCallback = createCallback();

        BluetoothManager manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mAdapter = manager.getAdapter();

        //アドバタイズを開始
        if (mAdapter != null && mAdapter.isEnabled()) {
            mAdapter.getBluetoothLeAdvertiser()
                    .startAdvertising(makeAdvertiseSetting(), makeAdvertiseData(), mCallback);
        } else {
            Toast.makeText(this, "Bluetoothが有効ではありません。", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAdapter != null && mAdapter.isEnabled()) {
            mAdapter.getBluetoothLeAdvertiser().stopAdvertising(mCallback);
        }
    }

    private AdvertiseCallback createCallback() {
        return new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                super.onStartSuccess(settingsInEffect);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(PeripheralActivity.this,
                                "アドバタイズスタート", Toast.LENGTH_LONG).show();
                    }
                });
            }
        };
    }

    //アドバタイズを設定
    private AdvertiseSettings makeAdvertiseSetting() {
        AdvertiseSettings.Builder builder = new AdvertiseSettings.Builder();

        //アドバタイズモード
        builder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY);
        //アドバタイズパワー
        builder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_ULTRA_LOW);
        //ペリフェラルへの接続を許可する
        builder.setConnectable(false);

        return builder.build();
    }

    //アドバタイズデータを作成
    private AdvertiseData makeAdvertiseData() {
        AdvertiseData.Builder builder = new AdvertiseData.Builder();
        builder.addServiceUuid(new ParcelUuid(UUID.fromString(ADVERTISE_UUID)));
        builder.setIncludeDeviceName(true);//発信デバイス名を送るか。

        return builder.build();
    }
}

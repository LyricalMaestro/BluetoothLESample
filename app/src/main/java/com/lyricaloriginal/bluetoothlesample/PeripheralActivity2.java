package com.lyricaloriginal.bluetoothlesample;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.content.Context;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

/**
 * ペリファレルモード画面その2
 * GATT通信対応
 */
public class PeripheralActivity2 extends AppCompatActivity {

    //UUID
    private static final String ADVERTISE_UUID = "0000180a-0000-1000-8000-00805f9b34fb";
    private static final String CHAR_UUID_YOU_CAN_CHANGE = "00002a29-0000-1000-8000-00805f9b34fb";

    private final String mTag = getClass().getName();

    //BLE
    private BluetoothAdapter mAdapter;
    private AdvertiseCallback mCallback;

    private BluetoothGattServer mGattServer;
    private BluetoothGattServerCallback mServerCallback;

    private TextView mMsgTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_peripheral2);
        mMsgTextView = (TextView) findViewById(R.id.msg_textview);

        mCallback = createCallback();
        mServerCallback = createGattCallback();

        BluetoothManager manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mAdapter = manager.getAdapter();

        //アドバタイズを開始
        if (mAdapter != null && mAdapter.isEnabled()) {

            mGattServer = manager.openGattServer(this, mServerCallback);
            setUuid();
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
            if (mGattServer != null) {
                mGattServer.close();
            }

            mAdapter.getBluetoothLeAdvertiser().stopAdvertising(mCallback);
        }
    }

    //UUIDを設定
    private void setUuid() {
        //serviceUUIDを設定
        BluetoothGattService service = new BluetoothGattService(
                UUID.fromString(ADVERTISE_UUID),
                BluetoothGattService.SERVICE_TYPE_PRIMARY);

        //characteristicUUIDを設定
        BluetoothGattCharacteristic characteristic = new BluetoothGattCharacteristic(
                UUID.fromString(CHAR_UUID_YOU_CAN_CHANGE),
                BluetoothGattCharacteristic.PROPERTY_READ |
                        BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_READ |
                        BluetoothGattCharacteristic.PERMISSION_WRITE);

        //characteristicUUIDをserviceUUIDにのせる
        service.addCharacteristic(characteristic);

        //serviceUUIDをサーバーにのせる
        mGattServer.addService(service);
    }

    private AdvertiseCallback createCallback() {
        return new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                super.onStartSuccess(settingsInEffect);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(PeripheralActivity2.this,
                                "アドバタイズスタート", Toast.LENGTH_LONG).show();

                    }
                });
            }

        };
    }

    private BluetoothGattServerCallback createGattCallback() {
        return new BluetoothGattServerCallback() {
            @Override
            public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
                super.onConnectionStateChange(device, status, newState);
                Log.d(mTag, "onConnectionStateChange");
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    appendText("接続開始!!    " + device.getName());
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    appendText("接続解除!!    " + device.getName());
                }
            }

            @Override
            public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset,
                                                    BluetoothGattCharacteristic characteristic) {
                super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
                Log.d(mTag, "onCharacteristicReadRequest");
                characteristic.setValue("something you want to send");
                mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset,
                        characteristic.getValue());
            }

            @Override
            public void onCharacteristicWriteRequest(BluetoothDevice device,
                                                     int requestId,
                                                     BluetoothGattCharacteristic characteristic,
                                                     boolean preparedWrite,
                                                     boolean responseNeeded,
                                                     int offset, byte[] value) {
                super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);
                Log.d(mTag, "onCharacteristicWriteRequest");
                try {
                    appendText(new String(value, "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    Log.e("ddd", e.getMessage(), e);
                }
                mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, null);
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
        builder.setConnectable(true);

        return builder.build();
    }

    //アドバタイズデータを作成
    private AdvertiseData makeAdvertiseData() {
        AdvertiseData.Builder builder = new AdvertiseData.Builder();
        builder.addServiceUuid(new ParcelUuid(UUID.fromString(ADVERTISE_UUID)));
        builder.setIncludeDeviceName(true);//発信デバイス名を送るか。
        return builder.build();
    }

    private void appendText(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mMsgTextView.append(text + "\r\n");
            }
        });
    }
}

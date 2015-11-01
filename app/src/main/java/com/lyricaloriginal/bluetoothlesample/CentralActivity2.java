package com.lyricaloriginal.bluetoothlesample;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.UUID;

/**
 * セントラル画面その2
 * GATT対応
 */
public class CentralActivity2 extends AppCompatActivity {

    // 対象のサービスUUID.
    private static final String SERVICE_UUID = "0000180a-0000-1000-8000-00805f9b34fb";
    // キャラクタリスティックUUID.
    private static final String CHARACTERISTIC_UUID = "00002a29-0000-1000-8000-00805f9b34fb";
    // キャラクタリスティック設定UUID(固定値).
    private static final String CHARACTERISTIC_CONFIG_UUID = "00002902-0000-1000-8000-00805f9b34fb";

    private BluetoothAdapter mAdapter;
    private ScanCallback mCallback;
    private BluetoothGattCallback mGattCallback;

    private BluetoothGattCharacteristic mBleCharacteristic;
    private BluetoothGatt mBleGatt;
    private boolean mIsBluetoothEnable = false;

    private Button mSendBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_central2);
        mSendBtn = (Button) findViewById(android.R.id.button1);
        mSendBtn.setOnClickListener(createOnClickBtnListener());

        mCallback = createScanCallback();
        mGattCallback = createGattCallback();

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
        setBluetoothEnable(false);
        if (mBleGatt != null) {
            mBleGatt.close();
            mBleGatt = null;
        }
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
                if (!mIsBluetoothEnable) {
                    //  GATT接続中はスルー。
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // スキャン中に見つかったデバイスに接続を試みる.第三引数には接続後に呼ばれるBluetoothGattCallbackを指定する.
                            mBleGatt = result.getDevice().connectGatt(CentralActivity2.this,
                                    false, mGattCallback);
                        }
                    });
                }
            }

            @Override
            public void onBatchScanResults(final List<ScanResult> results) {
                super.onBatchScanResults(results);
                Log.d(CentralActivity.class.getName(), "onBatchScanResults");
            }
        };
    }

    private BluetoothGattCallback createGattCallback() {
        return new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);
                // 接続状況が変化したら実行.
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    // 接続に成功したらサービスを検索する.
                    appendText("接続開始!!    " + gatt.getDevice().getName());
                    gatt.discoverServices();
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    // 接続が切れたらGATTを空にする.
                    if (mBleGatt != null) {
                        appendText("接続解除!!    " + gatt.getDevice().getName());
                        mBleGatt.close();
                        mBleGatt = null;
                    }
                    setBluetoothEnable(false);
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                // Serviceが見つかったら実行.
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    appendText("Discover");
                    // UUIDが同じかどうかを確認する.
                    BluetoothGattService service = gatt.getService(UUID.fromString(SERVICE_UUID));
                    if (service != null) {
                        appendText("Serviuce");
                        // 指定したUUIDを持つCharacteristicを確認する.
                        mBleCharacteristic = service.getCharacteristic(UUID.fromString(CHARACTERISTIC_UUID));
                        if (mBleCharacteristic != null) {
                            // Service, CharacteristicのUUIDが同じならBluetoothGattを更新する.
                            mBleGatt = gatt;
                            setBluetoothEnable(true);
                            appendText("BLEEnable!!");
                        }
                    }
                }
            }
        };
    }

    private void setBluetoothEnable(final boolean enable) {
        mIsBluetoothEnable = enable;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSendBtn.setEnabled(enable);
            }
        });
    }

    private View.OnClickListener createOnClickBtnListener() {
        return new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mIsBluetoothEnable) {
                    EditText editText = (EditText) findViewById(R.id.msg_edittext);
                    String msg = editText.getEditableText().toString();
                    if (!TextUtils.isEmpty(msg)) {
                        mBleCharacteristic.setValue(msg);
                        mBleGatt.writeCharacteristic(mBleCharacteristic);
                    }
                }
            }
        };
    }

    private void appendText(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView textView = (TextView) findViewById(android.R.id.text1);
                textView.append(text + "\r\n");
            }
        });
    }
}

package com.ysong.bluetooth_mpu9250;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import com.ysong.bluetooth_mpu9250.orientation.OrientationGLSurfaceView;

public class MainActivity extends AppCompatActivity {

	private static final long TIMEOUT = 50;

	private Toast toast = null;
	private OrientationGLSurfaceView orientationGLSurfaceView = null;
	private BluetoothSerial bluetoothSerial = null;
	private boolean threadEnabled = false;
	private Fusion fusion = null;
	private Calibration calibration = null;

	private class DataThread implements Runnable {
		@Override
		public void run() {
			bluetoothSerial.flush();
			while (threadEnabled) {
				try {
					byte[] data = bluetoothSerial.read(TIMEOUT);
					if (data == null) {
						continue;
					}
					if (data.length == 0) {
						break;
					} else {
						fusion.update(data);
						final float[] rpy = fusion.getRpyFusion();
//						Log.e("a", quaternion[0]+" "+quaternion[1]+" "+quaternion[2]+" "+quaternion[3]);
//						Log.e("b", rpy[0]+" "+rpy[1]+" "+rpy[2]);
						float[] mag = fusion.getMag();
						calibration.push(mag);
						float[] confirmCenter = calibration.getConfirmCenter();
						if (confirmCenter != null) {
							bluetoothSerial.write(getConfirmCenterPacket(confirmCenter));
						}
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								orientationGLSurfaceView.updateOrientation(rpy[0], rpy[1], rpy[2]);
							}
						});
					}
				} catch (Exception e) {
				}
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		toast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
		orientationGLSurfaceView = (OrientationGLSurfaceView)findViewById(R.id.motion_gl_surface_view);
		SeekBar zoom = (SeekBar)findViewById(R.id.zoom);
		bluetoothSerial = new BluetoothSerial(this);
		fusion = new Fusion();
		calibration = new Calibration();
		zoom.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
				orientationGLSurfaceView.zoom(1.0f - progressValue / 50.0f);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		orientationGLSurfaceView.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		orientationGLSurfaceView.onPause();
	}

	@Override
	protected void onDestroy() {
		orientationGLSurfaceView.release();
		super.onDestroy();
	}

	public void onCxnHandler(View view) {
		try {
			bluetoothSerial.connect(14);
			toastShow("Connect success");
			threadEnabled = true;
			new Thread(new DataThread()).start();
		} catch (Exception e) {
			toastShow(e.toString());
		}
	}

	public void onDxnHandler(View view) {
		threadEnabled = false;
		bluetoothSerial.poisonPill();
		try {
			bluetoothSerial.disconnect();
			toastShow("Disconnect success");
		} catch (Exception e) {
			toastShow(e.toString());
		}
	}

	private byte[] toHex16(int x) {
		byte[] hex = new byte[4];
		hex[0] = (byte) ((x & 0x0F) + 48);
		hex[1] = (byte) (((x >> 4) & 0x0F) + 48);
		hex[2] = (byte) (((x >> 8) & 0x0F) + 48);
		hex[3] = (byte) (((x >> 12) & 0x0F) + 48);
		return hex;
	}

	private byte[] getConfirmCenterPacket(float[] confirmCenter) {
		byte[] packet = new byte[14];
		packet[0] = 0;
		packet[13] = 10;
		System.arraycopy(toHex16((int) confirmCenter[0]), 0, packet, 1, 4);
		System.arraycopy(toHex16((int) confirmCenter[1]), 0, packet, 5, 4);
		System.arraycopy(toHex16((int) confirmCenter[2]), 0, packet, 9, 4);
		return packet;
	}

	private void toastShow(String str) {
		toast.setText(str);
		toast.show();
	}
}

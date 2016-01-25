package com.ysong.bluetooth_mpu9250;

public class Fusion {

	private final float ALPHA = 0.02f;

	private byte[] data = null;
	private float[] q = new float[4];
	private float[] mag = new float[3];
	private float[] rpy = new float[3];
	private float magYaw;
	private float prevYaw = 0.0f;
	private float prevYawFusion = 0.0f;

	public void update(byte[] data) {
		this.data = data;
		parseQuaternion();
		parseMag();
		getRpy();
		getMagYaw();
	}

	public float[] getRpyFusion() {
		float delta = rpy[2] - prevYaw;
		prevYaw = rpy[2];
		float yawFusion = normalizeYawFusion(prevYawFusion + delta);
		rpy[2] = (1.0f - ALPHA) * yawFusion + ALPHA * normalizeMagYaw(magYaw, yawFusion);
		prevYawFusion = rpy[2];
		return rpy;
	}

	public float[] getMag() {
		return mag;
	}

	private short byteToShort(byte lsb, byte msb) {
		return (short) ((msb & 0xFF) << 8 | (lsb & 0xFF));
	}

	private void parseQuaternion() {
		for (int i = 0; i < 4; i++) {
			q[i] = (float) byteToShort(data[i * 2], data[i * 2 + 1]);
		}
	}

	private void parseMag() {
		for (int i = 0; i < 3; i++) {
			mag[i] = (float) byteToShort(data[i * 2 + 8], data[i * 2 + 9]);
		}
	}

	private void getRpy() {
		float sum = q[0] * q[0] + q[1] * q[1] + q[2] * q[2] + q[3] * q[3];
		rpy[0] = (float) Math.atan2((q[0]*q[1]+q[2]*q[3])*2, sum-(q[1]*q[1]+q[2]*q[2])*2);
		rpy[1] = (float) Math.asin((q[0]*q[2]-q[1]*q[3])*2 / sum);
		rpy[2] = (float) Math.atan2((q[0]*q[3]+q[1]*q[2])*2, sum-(q[2]*q[2]+q[3]*q[3])*2);
	}

	private void getMagYaw() {
		float s2 = (float) Math.sin(rpy[1]);
		float c2 = (float) Math.cos(rpy[1]);
		float s3 = (float) Math.sin(rpy[0]);
		float c3 = (float) Math.cos(rpy[0]);
		float magX = c2 * mag[0] + s2 * s3 * mag[1] + s2 * c3 * mag[2];
		float magY = c3 * mag[1] - s3 * mag[2];
		magYaw = (float) Math.atan2(magX, magY);
	}

	private float normalizeYawFusion(float angle) {
		while (angle < 0.0f || angle >= Math.PI * 2.0f) {
			if (angle < 0.0f) {
				angle += Math.PI * 2.0f;
			} else {
				angle -= Math.PI * 2.0f;
			}
		}
		return angle;
	}

	private float normalizeMagYaw(float magYaw, float yawFusion) {
		while (magYaw - yawFusion >= Math.PI || yawFusion - magYaw >= Math.PI) {
			if (magYaw - yawFusion >= Math.PI) {
				magYaw -= Math.PI * 2.0f;
			} else {
				magYaw += Math.PI * 2.0f;
			}
		}
		return magYaw;
	}
}

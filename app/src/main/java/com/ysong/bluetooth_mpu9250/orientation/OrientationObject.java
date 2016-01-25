package com.ysong.bluetooth_mpu9250.orientation;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public abstract class OrientationObject {

	protected static final int BYTE_PER_SHORT = 2;
	protected static final int BYTE_PER_FLOAT = 4;
	protected static final int POSITION_SIZE = 3;
	protected static final int NORMAL_SIZE = 3;

	protected static int mMVPMatrixHandle;
	protected static int mMVMatrixHandle;
	protected static int mPositionHandle;
	protected static int mNormalHandle;
	protected static int mColorHandle;

	protected static FloatBuffer vertexBuffer;
	protected static ShortBuffer indexBuffer;
	private static int vertexBufferMaxSize = 0;
	private static int indexBufferMaxSize = 0;

	public static void init(int programHandle) {
		mMVPMatrixHandle = GLES20.glGetUniformLocation(programHandle, "uMVPMatrix");
		mMVMatrixHandle = GLES20.glGetUniformLocation(programHandle, "uMVMatrix");
		mPositionHandle = GLES20.glGetAttribLocation(programHandle, "aPosition");
		mNormalHandle = GLES20.glGetAttribLocation(programHandle, "aNormal");
		mColorHandle = GLES20.glGetUniformLocation(programHandle, "uColor");
		vertexBuffer = ByteBuffer.allocateDirect(BYTE_PER_FLOAT * vertexBufferMaxSize).order(ByteOrder.nativeOrder()).asFloatBuffer();
		indexBuffer = ByteBuffer.allocateDirect(BYTE_PER_SHORT * indexBufferMaxSize).order(ByteOrder.nativeOrder()).asShortBuffer();
	}

	public static void prismSetBufferMaxSize(int n) {
		int vertexBufferSize = n * 24;
		if (vertexBufferMaxSize < vertexBufferSize) {
			vertexBufferMaxSize = vertexBufferSize;
		}
		int indexBufferSize = n * 4;
		if (indexBufferMaxSize < indexBufferSize) {
			indexBufferMaxSize = indexBufferSize;
		}
	}

	public static void cylinderSetBufferMaxSize(int n) {
		int vertexBufferSize = n * 12;
		if (vertexBufferMaxSize < vertexBufferSize) {
			vertexBufferMaxSize = vertexBufferSize;
		}
		int indexBufferSize = n * 2 + 2;
		if (indexBufferMaxSize < indexBufferSize) {
			indexBufferMaxSize = indexBufferSize;
		}
	}

	public static void sphereSetBufferMaxSize(int n) {
		int vertexBufferSize = n * n * 12 - n * 12 + 12;
		if (vertexBufferMaxSize < vertexBufferSize) {
			vertexBufferMaxSize = vertexBufferSize;
		}
		int indexBufferSize = (n * 2 + 1) * (n - 2) * 2;
		if (indexBufferMaxSize < indexBufferSize) {
			indexBufferMaxSize = indexBufferSize;
		}
	}

	public static void releaseBuffer() {
		vertexBuffer.limit(0);
		vertexBuffer = null;
		indexBuffer.limit(0);
		indexBuffer = null;
	}

	public abstract void render(float[] mMVPMatrix, float[] mMVMatrix);

	public abstract void release();
}

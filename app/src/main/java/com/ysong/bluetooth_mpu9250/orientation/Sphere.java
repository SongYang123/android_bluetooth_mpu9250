package com.ysong.bluetooth_mpu9250.orientation;

import android.opengl.GLES20;

public class Sphere extends OrientationObject {

	private int[] vbo = new int[1];
	private int[] ibo = new int[3];
	private int n;
	private float radius;
	private float[] color;

	public Sphere(int n, float radius, float[] color) {
		this.n = n;
		this.radius = radius;
		this.color = color;
	}

	public void init() {
		float[] vertex = genVertex(n, radius);
		GLES20.glGenBuffers(vbo.length, vbo, 0);
		vertexBuffer.position(0);
		vertexBuffer.put(vertex).position(0);
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[0]);
		GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, BYTE_PER_FLOAT * vertex.length, vertexBuffer, GLES20.GL_STATIC_DRAW);
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

		short[][] index = genIndex(n);
		GLES20.glGenBuffers(ibo.length, ibo, 0);
		for (int i = 0; i < 3; i++) {
			indexBuffer.position(0);
			indexBuffer.put(index[i]).position(0);
			GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo[i]);
			GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, BYTE_PER_SHORT * index[i].length, indexBuffer, GLES20.GL_STATIC_DRAW);
			GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
		}

		GLES20.glUniform4fv(mColorHandle, 1, color, 0);
	}

	@Override
	public void render(float[] mMVPMatrix, float[] mMVMatrix) {
		GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
		GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mMVMatrix, 0);

		GLES20.glEnableVertexAttribArray(mPositionHandle);
		GLES20.glEnableVertexAttribArray(mNormalHandle);

		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[0]);
		GLES20.glVertexAttribPointer(mPositionHandle, POSITION_SIZE, GLES20.GL_FLOAT, false, BYTE_PER_FLOAT * (POSITION_SIZE + NORMAL_SIZE), 0);
		GLES20.glVertexAttribPointer(mNormalHandle, POSITION_SIZE, GLES20.GL_FLOAT, false, BYTE_PER_FLOAT * (POSITION_SIZE + NORMAL_SIZE), BYTE_PER_FLOAT * POSITION_SIZE);
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

		for (int i = 0; i < 2; i++) {
			GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo[i]);
			GLES20.glDrawElements(GLES20.GL_TRIANGLE_FAN, n * 2 + 2, GLES20.GL_UNSIGNED_SHORT, 0);
			GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
		}

		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo[2]);
		for (int i = 0; i < (n * 2 + 1) * (n - 2) * 2; i += (n * 2 + 1) * 2) {
			GLES20.glDrawElements(GLES20.GL_TRIANGLE_STRIP, (n * 2 + 1) * 2, GLES20.GL_UNSIGNED_SHORT, BYTE_PER_SHORT * i);
		}
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
	}

	@Override
	public void release() {
		GLES20.glDeleteBuffers(vbo.length, vbo, 0);
		GLES20.glDeleteBuffers(ibo.length, ibo, 0);
//		vbo[0] = 0;
//		for (int i = 0; i < 3; i++) {
//			ibo[i] = 0;
//		}
	}

	private float[] genVertex(int n, float radius) {
		float[] vertex = new float[n * n * 12 - n * 12 + 12];
		for (int i = 0; i < n - 1; i++) {
			double phi = Math.PI * (i + 1) / n;
			float r = (float) Math.sin(phi);
			float h = (float) Math.cos(phi);
			for (int j = 0; j < n * 2; j++) {
				double theta = Math.PI * j / n;
				float x = r * (float) Math.cos(theta);
				float y = r * (float) Math.sin(theta);
				int ij6 = (i * n * 2 + j) * 6;
				vertex[ij6] = radius * x;
				vertex[ij6 + 1] = radius * y;
				vertex[ij6 + 2] = radius * h;
				vertex[ij6 + 3] = x;
				vertex[ij6 + 4] = y;
				vertex[ij6 + 5] = h;
			}
			vertex[n * n * 12 - n * 12 + 2] = radius;
			vertex[n * n * 12 - n * 12 + 5] = 1.0f;
			vertex[n * n * 12 - n * 12 + 8] = -radius;
			vertex[n * n * 12 - n * 12 + 11] = -1.0f;
		}
		return vertex;
	}

	private short[][] genIndex(int n) {
		short[][] index = new short[3][];
		index[0] = new short[n * 2 + 2];
		index[1] = new short[n * 2 + 2];
		index[2] = new short[(n * 2 + 1) * (n - 2) * 2];
		/* top and bottom */
		index[0][0] = (short) (n * n * 2 - n * 2);
		index[1][0] = (short) (n * n * 2 - n * 2 + 1);
		for (int i = 0; i < n * 2 + 1; i++) {
			index[0][i + 1] = (short) (i % (n * 2));
			index[1][i + 1] = (short) (n * n * 2 - n * 2 - i % (n * 2) - 1);
		}
		/* side */
		for (int i = 0; i < n - 2; i++) {
			int in = i * (n * 2 + 1) * 2;
			for (int j = 0; j < n * 2 + 1; j++) {
				int j2 = j * 2;
				index[2][in + j2] = (short) (i * (n * 2) + j % (n * 2));
				index[2][in + j2 + 1] = (short) ((i + 1) * (n * 2) + j % (n * 2));
			}
		}
		return index;
	}
}

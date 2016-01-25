package com.ysong.bluetooth_mpu9250.orientation;

import android.opengl.GLES20;

public class Prism extends OrientationObject {

	private int[] vbo = new int[3];
	private int[] ibo = new int[3];
	private int n;
	private float radius;
	private float height;
	private float[] color;

	public Prism(int n, float radius, float height, float[] color) {
		this.n = n;
		this.radius = radius;
		this.height = height;
		this.color = color;
	}

	public void init() {
		float[][] vertex = genVertex(n, radius, height);
		GLES20.glGenBuffers(vbo.length, vbo, 0);
		for (int i = 0; i < 3; i++) {
			vertexBuffer.position(0);
			vertexBuffer.put(vertex[i]).position(0);
			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[i]);
			GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, BYTE_PER_FLOAT * vertex[i].length, vertexBuffer, GLES20.GL_STATIC_DRAW);
			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
		}

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

		for (int i = 0; i < 2; i++) {
			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[i]);
			GLES20.glVertexAttribPointer(mPositionHandle, POSITION_SIZE, GLES20.GL_FLOAT, false, BYTE_PER_FLOAT * (POSITION_SIZE + NORMAL_SIZE), 0);
			GLES20.glVertexAttribPointer(mNormalHandle, POSITION_SIZE, GLES20.GL_FLOAT, false, BYTE_PER_FLOAT * (POSITION_SIZE + NORMAL_SIZE), BYTE_PER_FLOAT * POSITION_SIZE);
			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
			GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo[i]);
			GLES20.glDrawElements(GLES20.GL_TRIANGLE_FAN, n + 2, GLES20.GL_UNSIGNED_SHORT, 0);
			GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
		}

		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[2]);
		GLES20.glVertexAttribPointer(mPositionHandle, POSITION_SIZE, GLES20.GL_FLOAT, false, BYTE_PER_FLOAT * (POSITION_SIZE + NORMAL_SIZE), 0);
		GLES20.glVertexAttribPointer(mNormalHandle, POSITION_SIZE, GLES20.GL_FLOAT, false, BYTE_PER_FLOAT * (POSITION_SIZE + NORMAL_SIZE), BYTE_PER_FLOAT * POSITION_SIZE);
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo[2]);
		for (int i = 0; i < n * 4; i += 4) {
			GLES20.glDrawElements(GLES20.GL_TRIANGLE_STRIP, 4, GLES20.GL_UNSIGNED_SHORT, BYTE_PER_SHORT * i);
		}
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
	}

	@Override
	public void release() {
		GLES20.glDeleteBuffers(vbo.length, vbo, 0);
		GLES20.glDeleteBuffers(ibo.length, ibo, 0);
//		for (int i = 0; i < 3; i++) {
//			vbo[i] = 0;
//			ibo[i] = 0;
//		}
	}

	private float[][] genVertex(int n, float radius, float height) {
		float[][] vertex = new float[3][];
		vertex[0] = new float[n * 6 + 6];
		vertex[1] = new float[n * 6 + 6];
		vertex[2] = new float[n * 24];
		height /= 2;
		/* top and bottom */
		for (int i = 0; i < n; i++) {
			double angle = Math.PI * 2 * i / n;
			float x = radius * (float) Math.cos(angle);
			float y = radius * (float) Math.sin(angle);
			int i6 = i * 6;
			vertex[0][i6] = vertex[1][i6] = x;
			vertex[0][i6 + 1] = y;
			vertex[1][i6 + 1] = -y;

		}
		for (int i = 0; i < n + 1; i++) {
			int i6 = i * 6;
			vertex[0][i6 + 2] = height;
			vertex[0][i6 + 5] = 1.0f;
			vertex[1][i6 + 2] = -height;
			vertex[1][i6 + 5] = -1.0f;
		}
		/* side */
		for (int i = 0; i < n; i++) {
			double angle = Math.PI * 2 * i / n;
			float x = radius * (float) Math.cos(angle);
			float y = radius * (float) Math.sin(angle);
			int prevTopR6 = ((i * 2 + n * 2 - 1) % (n * 2)) * 6;
			int curTopL6 = i * 2 * 6;
			int prevBtmR6 = prevTopR6 + n * 2 * 6;
			int curBtmL6 = curTopL6 + n * 2 * 6;
			vertex[2][prevTopR6] = vertex[2][curTopL6] = vertex[2][prevBtmR6] = vertex[2][curBtmL6] = x;
			vertex[2][prevTopR6 + 1] = vertex[2][curTopL6 + 1] = vertex[2][prevBtmR6 + 1] = vertex[2][curBtmL6 + 1] = y;
			vertex[2][prevTopR6 + 2] = vertex[2][curTopL6 + 2] = height;
			vertex[2][prevBtmR6 + 2] = vertex[2][curBtmL6 + 2] = -height;

		}
		for (int i = 0; i < n; i++) {
			double angle = Math.PI * (i * 2 + 1) / n;
			float x = (float) Math.cos(angle);
			float y = (float) Math.sin(angle);
			int topL6 = i * 2 * 6;
			int topR6 = (i * 2 + 1) * 6;
			int btmL6 = topL6 + n * 2 * 6;
			int btmR6 = topR6 + n * 2 * 6;
			vertex[2][topL6 + 3] = vertex[2][topR6 + 3] = vertex[2][btmL6 + 3] = vertex[2][btmR6 + 3] = x;
			vertex[2][topL6 + 4] = vertex[2][topR6 + 4] = vertex[2][btmL6 + 4] = vertex[2][btmR6 + 4] = y;
		}
		return vertex;
	}

	private short[][] genIndex(int n) {
		short[][] index = new short[3][];
		index[0] = new short[n + 2];
		index[1] = new short[n + 2];
		index[2] = new short[n * 4];
		/* top and bottom */
		for (int i = 0; i < n + 1; i++) {
			short idx = (short) ((i + n) % (n + 1));
			index[0][i] = index[1][i] = idx;
		}
		/* side */
		for (int i = 0;i < n; i++) {
			int i4 = i * 4;
			index[2][i4] = (short) (i * 2);
			index[2][i4 + 1] = (short) (i * 2 + n * 2);
			index[2][i4 + 2] = (short) (i * 2 + 1);
			index[2][i4 + 3] = (short) (i * 2 + n * 2 + 1);
		}
		return index;
	}
}

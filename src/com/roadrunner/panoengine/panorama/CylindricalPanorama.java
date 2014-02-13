package com.roadrunner.panoengine.panorama;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import javax.microedition.khronos.opengles.GL10;

import android.opengl.Matrix;

import com.roadrunner.panoengine.opengl.OpenGLTexture;

public class CylindricalPanorama extends Panorama {

	private int x;
	private int y;

	public CylindricalPanorama(int x, int y) {
		this.x = x;
		this.y = y;

		textures = new OpenGLTexture[this.x * this.y];

		ByteBuffer bb = ByteBuffer.allocateDirect(this.x * this.y * 3 * 4 * (Float.SIZE / Byte.SIZE));
		bb.order(ByteOrder.nativeOrder());
		vertexBuffer = bb.asFloatBuffer();

		float degreeStep = 360.0f / (float) this.x;
		float hStep = (R * 2.0f) / this.y;

		float[] startVector = new float[4];
		startVector[0] = 0.0f;
		startVector[1] = 0.0f;
		startVector[2] = R;
		startVector[3] = 0.0f;

		float[] rotation = new float[16];
		Matrix.setIdentityM(rotation, 0);
		Matrix.rotateM(rotation, 0, degreeStep, 0.0f, 1.0f, 0.0f);

		float[] endVector = new float[4];

		Matrix.multiplyMV(endVector, 0, rotation, 0, startVector, 0);

		for (int i = 0; i < this.x; i++) {
			float startH = -R;
			for (int j = 0; j < this.y; j++) {
				vertexBuffer.put(startVector[0]);
				vertexBuffer.put(startH + hStep);
				vertexBuffer.put(startVector[2]);

				vertexBuffer.put(startVector[0]);
				vertexBuffer.put(startH);
				vertexBuffer.put(startVector[2]);

				vertexBuffer.put(endVector[0]);
				vertexBuffer.put(startH + hStep);
				vertexBuffer.put(endVector[2]);

				vertexBuffer.put(endVector[0]);
				vertexBuffer.put(startH);
				vertexBuffer.put(endVector[2]);
				
				startH += hStep;
			}
			startVector = Arrays.copyOf(endVector, endVector.length);
			Matrix.multiplyMV(endVector, 0, rotation, 0, startVector, 0);
		}
		vertexBuffer.position(0);
		
		bb = ByteBuffer.allocateDirect(this.x * this.y * 3 * 4 * (Float.SIZE / Byte.SIZE));
		bb.order(ByteOrder.nativeOrder());
		normalBuffer = bb.asFloatBuffer();
		
		startVector[0] = 0.0f;
		startVector[1] = 0.0f;
		startVector[2] = -1.0f;
		startVector[3] = 0.0f;
		
		for(int i = 0; i < this.x; i++) {
			for(int j = 0; j < this.y; j++) {
				for(int k = 0; k < 4; k++) {
					for(int l = 0; l < 3; l++) {
						normalBuffer.put(startVector[l]);
					}
				}
			}
			Matrix.multiplyMV(startVector, 0, rotation, 0, startVector, 0);
		}
		normalBuffer.position(0);

		bb = ByteBuffer.allocateDirect(this.x * this.y * 2 * 4 * (Float.SIZE / Byte.SIZE));
		bb.order(ByteOrder.nativeOrder());
		textureCoordBuffer = bb.asFloatBuffer();

		for (int i = 0; i < this.x; i++) {
			for (int j = 0; j < this.y; j++) {
				textureCoordBuffer.put(1.0f);
				textureCoordBuffer.put(0.0f);

				textureCoordBuffer.put(1.0f);
				textureCoordBuffer.put(1.0f);

				textureCoordBuffer.put(0.0f);
				textureCoordBuffer.put(0.0f);

				textureCoordBuffer.put(0.0f);
				textureCoordBuffer.put(1.0f);
			}
		}
		textureCoordBuffer.position(0);
	}

	@Override
	public void drawPanorama(GL10 gl) {
		gl.glEnable(GL10.GL_CULL_FACE);
		gl.glCullFace(GL10.GL_BACK);
		gl.glFrontFace(GL10.GL_CW);
		
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
		gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureCoordBuffer);
		gl.glNormalPointer(GL10.GL_FLOAT, 0, normalBuffer);

		int start = 0;
		final int step = 4;

		for (int i = 0; i < x; i++) {
			for (int j = 0; j < y; j++) {
				if (bindTexture(gl, (i * y) + j)) {
					gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, start, step);
				}
				start += step;
			}
		}

		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL10.GL_NORMAL_ARRAY);
		gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		gl.glDisable(GL10.GL_CULL_FACE);
	}

	public synchronized void setTexture(OpenGLTexture texture, int x, int y) {
		int index = (x * this.y) + y;
		textures[index] = texture;
	}

}

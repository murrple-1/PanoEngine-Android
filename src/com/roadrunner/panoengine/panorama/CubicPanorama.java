package com.roadrunner.panoengine.panorama;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.microedition.khronos.opengles.GL10;

import com.roadrunner.panoengine.opengl.OpenGLTexture;

public class CubicPanorama extends Panorama {

	public enum CubeFaceOrientation {
		FRONT, BACK, RIGHT, LEFT, UP, DOWN
	};
	
	private static final CubeFaceOrientation[] FACES = CubeFaceOrientation.values();

	private int x;
	private int y;

	public CubicPanorama(int x, int y) {
		this.x = x;
		this.y = y;

		textures = new OpenGLTexture[FACES.length * this.x * this.y];

		ByteBuffer bb = ByteBuffer.allocateDirect(FACES.length * this.x * this.y * 4 * 3 * (Float.SIZE / Byte.SIZE));
		bb.order(ByteOrder.nativeOrder());
		vertexBuffer = bb.asFloatBuffer();

		float stepM = (R * 2.0f) / (float) this.x;
		float stepN = (R * 2.0f) / (float) this.y;

		for (int i = 0; i < FACES.length; i++) {
			CubeFaceOrientation face = FACES[i];
			float v[][] = new float[4][3];
			
			float start[] = new float[3];
			
			switch (face) {
			case FRONT:
				start[0] = -R;
				start[1] = -R;
				start[2] = R;
				break;
			case BACK:
				start[0] = R;
				start[1] = -R;
				start[2] = -R;
				break;
			case RIGHT:
				start[0] = R;
				start[1] = -R;
				start[2] = R;
				break;
			case LEFT:
				start[0] = -R;
				start[1] = -R;
				start[2] = -R;
				break;
			case UP:
				start[0] = -R;
				start[1] = -R;
				start[2] = -R;
				break;
			case DOWN:
				start[0] = -R;
				start[1] = R;
				start[2] = R;
				break;
			default:
				break;
			}
			
			for (int j = 0; j < this.x; j++) {
				
				for(int m = 0; m < v.length; m++) {
					for(int n = 0; n < v[m].length; n++) {
						v[m][n] = start[n];
					}
				}
				
				float tStepM = (stepM * (j + 1));
				switch (face) {
				case FRONT:
					v[1][0] += tStepM;
					v[3][0] += tStepM;
					
					v[2][1] += stepN;
					v[3][1] += stepN;
					break;
				case BACK:
					v[1][0] += tStepM;
					v[3][0] += tStepM;
					
					v[2][1] += stepN;
					v[3][1] += stepN;
					break;
				case RIGHT:
					v[1][1] += tStepM;
					v[3][1] += tStepM;
					
					v[2][2] += stepN;
					v[3][2] += stepN;
					break;
				case LEFT:
					v[1][1] += tStepM;
					v[3][1] += tStepM;
					
					v[2][2] -= stepN;
					v[3][2] -= stepN;
					break;
				case UP:
					v[1][0] += tStepM;
					v[3][0] += tStepM;
					
					v[2][2] += stepN;
					v[3][2] += stepN;
					break;
				case DOWN:
					v[1][0] += tStepM;
					v[3][0] += tStepM;
					
					v[2][2] -= stepN;
					v[3][2] -= stepN;
					break;
				default:
					break;
				}
				
				for (int k = 0; k < this.y; k++) {
					
					for(int m = 0; m < v.length; m++) {
						vertexBuffer.put(v[m]);
					}
					
					switch (face) {
					case FRONT:
						v[2][1] += stepN;
						v[3][1] += stepN;
						break;
					case BACK:
						v[2][1] += stepN;
						v[3][1] += stepN;
						break;
					case RIGHT:
						v[2][2] += stepN;
						v[3][2] += stepN;
						break;
					case LEFT:
						v[2][2] -= stepN;
						v[3][2] -= stepN;
						break;
					case UP:
						v[2][2] += stepN;
						v[3][2] += stepN;
						break;
					case DOWN:
						v[2][2] -= stepN;
						v[3][2] -= stepN;
						break;
					default:
						break;
					}
				}
			}
		}
		vertexBuffer.position(0);
		
		bb = ByteBuffer.allocateDirect(this.x * this.y * FACES.length * 3 * 4 * (Float.SIZE / Byte.SIZE));
		bb.order(ByteOrder.nativeOrder());
		normalBuffer = bb.asFloatBuffer();
		
		for(int i = 0; i < FACES.length; i++) {
			for(int j = 0; j < this.x; j++) {
				for(int k = 0; k < this.y; k++) {
					for(int l = 0; l < 4; l++){
						CubeFaceOrientation face = FACES[i];
						float nX = 0.0f;
						float nY = 0.0f;
						float nZ = 0.0f;
						switch (face) {
						case FRONT:
							nZ = 1.0f;
							break;
						case BACK:
							nZ = -1.0f;
							break;
						case RIGHT:
							nX = 1.0f;
							break;
						case LEFT:
							nX = -1.0f;
							break;
						case UP:
							nY = 1.0f;
							break;
						case DOWN:
							nY = -1.0f;
							break;
						}
						normalBuffer.put(nX);
						normalBuffer.put(nY);
						normalBuffer.put(nZ);
					}
				}
			}
		}
		normalBuffer.position(0);
		
		bb = ByteBuffer.allocateDirect(FACES.length * this.x * this.y * 2 * 4 * (Float.SIZE / Byte.SIZE));
		bb.order(ByteOrder.nativeOrder());
		textureCoordBuffer = bb.asFloatBuffer();

		for (int i = 0; i < FACES.length; i++) {
			for (int j = 0; j < this.x; j++) {
				for (int k = 0; k < this.y; k++) {
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
		for (int i = 0; i < FACES.length; i++) {
			for (int j = 0; j < (x * y); j++) {
				if (bindTexture(gl, i * (x * y) + j)) {
					gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, start, step);
				}
				start += step;
			}
		}

		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		gl.glDisable(GL10.GL_CULL_FACE);
	}

	public synchronized void setTexture(OpenGLTexture texture, CubeFaceOrientation face, int x, int y) {
		int index = (face.ordinal() * (this.x * this.y)) + (y * this.x) + x;
		textures[index] = texture;
	}
	
}

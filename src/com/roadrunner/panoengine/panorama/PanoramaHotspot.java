package com.roadrunner.panoengine.panorama;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import com.roadrunner.panoengine.opengl.OpenGLTexture;

public class PanoramaHotspot {

	public static final float R = 0.5f;
	
	private int id;
	private OpenGLTexture texture;
	private float x;
	private float y;
	private float width;
	private float height;
	private String data;
	
	private static final float[] vertexB = {
		-1.0f, 1.0f, R,
		-1.0f, -1.0f, R,
		1.0f, 1.0f, R,
		1.0f, -1.0f, R
	};
	
	private static final float[] textureCoordB = { 
		0.0f, 1.0f,
		0.0f, 0.0f,
		1.0f, 1.0f,
		1.0f, 0.0f
	};
	
	private static final float[] normalB = {
		0.0f, 0.0f, 1.0f
	};
	
	private FloatBuffer vertexBuffer;
	private FloatBuffer textureCoordBuffer;
	private FloatBuffer normalBuffer;
	
	public PanoramaHotspot(int id, OpenGLTexture texture, float x,
			float y, float width, float height, String data) {
		this.id = id;
		this.texture = texture;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.data = data;
		
		ByteBuffer bb = ByteBuffer.allocateDirect(vertexB.length * (Float.SIZE / Byte.SIZE));
		bb.order(ByteOrder.nativeOrder());
		vertexBuffer = bb.asFloatBuffer();
		vertexBuffer.put(vertexB);
		vertexBuffer.position(0);
		
		bb = ByteBuffer.allocateDirect(normalB.length * (Float.SIZE / Byte.SIZE));
		bb.order(ByteOrder.nativeOrder());
		normalBuffer = bb.asFloatBuffer();
		normalBuffer.put(normalB);
		normalBuffer.position(0);
		
		bb = ByteBuffer.allocateDirect(textureCoordB.length * (Float.SIZE / Byte.SIZE));
		bb.order(ByteOrder.nativeOrder());
		textureCoordBuffer = bb.asFloatBuffer();
		textureCoordBuffer.put(textureCoordB);
		textureCoordBuffer.position(0);
	}
	
	public void drawHotspot(GL10 gl) {
		if(bindTexture(gl)) {
			gl.glEnable(GL10.GL_CULL_FACE);
			gl.glCullFace(GL10.GL_BACK);
			gl.glFrontFace(GL10.GL_CW);
	
			gl.glEnable(GL10.GL_BLEND);
            gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
			
			gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
			gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);
			gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
			
			gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
			gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureCoordBuffer);
			gl.glNormalPointer(GL10.GL_FLOAT, 0, normalBuffer);
			
			gl.glPushMatrix();
			
			gl.glScalef(width, height, 1.0f);
			gl.glRotatef(x, 0.0f, 1.0f, 0.0f);
			gl.glRotatef(y, 1.0f, 0.0f, 0.0f);
	
			gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
			
			gl.glPopMatrix();
	
			gl.glDisable(GL10.GL_BLEND);
			
			gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
			gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
			gl.glDisable(GL10.GL_CULL_FACE);
		}
	}
	
	protected boolean bindTexture(GL10 gl) {
		if (texture != null) {
			int textureId = texture.getTextureId(gl);
			gl.glBindTexture(GL10.GL_TEXTURE_2D, textureId);
			return true;
		} else {
			return false;
		}
	}

	public int getId() {
		return id;
	}

	public OpenGLTexture getTexture() {
		return texture;
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public float getWidth() {
		return width;
	}

	public float getHeight() {
		return height;
	}

	public String getData() {
		return data;
	}
}

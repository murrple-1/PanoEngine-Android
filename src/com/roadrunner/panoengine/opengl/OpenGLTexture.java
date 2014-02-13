package com.roadrunner.panoengine.opengl;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.Bitmap;
import android.opengl.GLUtils;

public class OpenGLTexture {

	private int[] textureBuffer;
	private Bitmap bitmap;
	private boolean useMipMaps;
	
	private GL10 cacheGL;
	
	public OpenGLTexture(Bitmap bitmap, boolean useMipmaps) {
		this.bitmap = bitmap;
		this.useMipMaps = useMipmaps;
	}
	
	public OpenGLTexture(Bitmap bitmap) {
		this(bitmap, false);
	}
	
	public int getTextureId(GL10 gl) {
		if(cacheGL == null) {
			cacheGL = gl;
		} else {
			if(!cacheGL.equals(gl)) {
				cacheGL = gl;
				textureBuffer = null;
			}
		}
		if(textureBuffer == null) {
			loadTexture(cacheGL);
		}
		if(textureBuffer != null) {
			return textureBuffer[0];
		} else {
			return -1;
		}
	}
	
	private void loadTexture(GL10 gl) {
		if(bitmap != null && !bitmap.isRecycled()) {
			textureBuffer = new int[1];
			
			gl.glGenTextures(1, textureBuffer, 0);
			gl.glBindTexture(GL10.GL_TEXTURE_2D, textureBuffer[0]);
			
			gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
			gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
			
			try {
				if(useMipMaps) {
					int width = bitmap.getWidth();
					int height = bitmap.getHeight();
					
					int level = 0;
					
					Bitmap tBitmap = bitmap;
					
					while(width >= 1 || height >= 1) {
						GLUtils.texImage2D(GL10.GL_TEXTURE_2D, level++, tBitmap, 0);
						
						if(width == 1 || height == 1) {
							break;
						}
						
						width /= 2;
						height /= 2;
						
						tBitmap = Bitmap.createScaledBitmap(tBitmap, width, height, true);
					}
				} else {
					GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
				}
			} catch(Exception e) {
				textureBuffer = null;
				e.printStackTrace();
			}
		}
	}
	
	@Override
	protected void finalize() throws Throwable {
		if(cacheGL != null) {
			cacheGL.glDeleteTextures(1, textureBuffer, 0);
			cacheGL = null;
		}
		super.finalize();
	}
}

package com.roadrunner.panoengine.panorama;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collection;

import javax.microedition.khronos.opengles.GL10;

import com.roadrunner.panoengine.opengl.OpenGLTexture;

public abstract class Panorama {

	protected static final float R = 1.0f;
	
	protected OpenGLTexture[] textures;
	protected FloatBuffer vertexBuffer;
	protected FloatBuffer textureCoordBuffer;
	protected FloatBuffer normalBuffer;
	protected FloatBuffer colorBuffer;
	protected ByteBuffer indexBuffer;
	
	private Collection<PanoramaHotspot> hotspots = new ArrayList<PanoramaHotspot>();
	private Collection<PanoramaHotspotTouchListener> hotspotListeners = new ArrayList<PanoramaHotspotTouchListener>();
	
	public void drawFrame(GL10 gl) {
		drawPanorama(gl);
		
		for(PanoramaHotspot hotspot : hotspots) {
			hotspot.drawHotspot(gl);
		}
	}
	
	public abstract void drawPanorama(GL10 gl);
	
	public int getTexturesCount() {
		return textures.length;
	}
	
	public synchronized void setTexture(OpenGLTexture texture, int index) {
		textures[index] = texture;
	}
	
	protected boolean bindTexture(GL10 gl, int index) {
		OpenGLTexture texture = textures[index];
		if (texture != null) {
			int textureId = texture.getTextureId(gl);
			gl.glBindTexture(GL10.GL_TEXTURE_2D, textureId);
			return true;
		} else {
			return false;
		}
	}
	
	public void addHotspot(PanoramaHotspot hotspot) {
		hotspots.add(hotspot);
	}
	
	public void removeHotspot(PanoramaHotspot hotspot) {
		hotspots.remove(hotspot);
	}
	
	public Iterable<PanoramaHotspot> getHotspots() {
		return hotspots;
	}
	
	public void addHotspotTouchListener(PanoramaHotspotTouchListener listener) {
		hotspotListeners.add(listener);
	}
	
	public void removeHotspotTouchListener(PanoramaHotspotTouchListener listener) {
		hotspotListeners.remove(listener);
	}
	
	public void didTouchHotspot(PanoramaHotspot hotspot) {
		for(PanoramaHotspotTouchListener listener : hotspotListeners) {
			listener.onTouch(hotspot);
		}
	}
}

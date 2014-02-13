package com.roadrunner.panoengine.panorama;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;

public class PanoramaRenderer implements Renderer {

	public static final float ZNEAR = 0.01f;
	public static final float ZFAR = 100.0f;
	
	private int width;
	private int height;
	
	private PanoramaCamera camera = new PanoramaCamera(45.0f, 0.5f);
	private Panorama panorama;
	
	public Panorama getPanorama() {
		return panorama;
	}
	
	public synchronized void setPanorama(Panorama panorama) {
		this.panorama = panorama;
	}
	
	public PanoramaCamera getCamera() {
		return camera;
	}
	
	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		gl.glEnable(GL10.GL_TEXTURE_2D);
		gl.glShadeModel(GL10.GL_SMOOTH);
		gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        gl.glClearDepthf(1.0f);
        gl.glEnable(GL10.GL_DEPTH_TEST);
        gl.glDepthFunc(GL10.GL_LEQUAL);
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		this.width = width;
		this.height = height;
		
		gl.glViewport(0, 0, this.width, this.height);
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		if(panorama != null) {
			gl.glMatrixMode(GL10.GL_PROJECTION);
	        gl.glLoadIdentity();
	        GLU.gluPerspective(gl, camera.getAdjustedFOV(), (float) width / (float) height, ZNEAR, ZFAR);
	        gl.glMatrixMode(GL10.GL_MODELVIEW);
			
			gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
            gl.glLoadIdentity();
            
            float[] lookAt = camera.getLookAtVector();
	        GLU.gluLookAt(gl, camera.getPositionX(), camera.getPositionY(), camera.getPositionZ(), lookAt[0], lookAt[1], lookAt[2], 0.0f, 1.0f, 0.0f);
            
	        panorama.drawFrame(gl);
		}
	}
}

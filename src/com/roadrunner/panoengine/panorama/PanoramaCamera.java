package com.roadrunner.panoengine.panorama;

import android.opengl.Matrix;

public class PanoramaCamera {

	private float FOV;
	private float zoomFactor;
	
	private float minPitchAngle = (float) -(Math.PI * 0.5);
	private float maxPitchAngle = (float) (Math.PI * 0.5);
	
	private float minYawAngle = (float) -Math.PI;
	private float maxYawAngle = (float) Math.PI;
	
	private float pitch = 0.0f;
	private float yaw = 0.0f;
	private float[] positionVector = new float[3];
	
	public PanoramaCamera(float FOV, float zoomFactor) {
		this.FOV = FOV;
		this.zoomFactor = zoomFactor;
		
		positionVector[0] = 0.0f;
		positionVector[1] = 0.0f;
		positionVector[2] = 0.0f;
	}
	
	public float getFOV() {
		return FOV;
	}
	
	public float getZoomFactor() {
		return zoomFactor;
	}
	
	public void setZoomFactor(float zoomFactor) {
		this.zoomFactor = zoomFactor;
	}
	
	public float getAdjustedFOV() {
		return FOV / zoomFactor;
	}

	public float[] getLookAtVector() {
		float[] vector = { 0.0f, 0.0f, 1.0f, 1.0f };
		float[] matrix = new float[16];
		Matrix.setIdentityM(matrix, 0);
		Matrix.rotateM(matrix, 0, (float) Math.toDegrees(pitch), 1.0f, 0.0f, 0.0f);
		Matrix.rotateM(matrix, 0, (float) Math.toDegrees(yaw), 0.0f, 1.0f, 0.0f);
		Matrix.multiplyMV(vector, 0, matrix, 0, vector, 0);
		return vector;
 	}
	
	public float getPositionX() {
		return positionVector[0];
	}
	
	public synchronized void setPositionX(float x) {
		positionVector[0] = x;
	}
	
	public float getPositionY() {
		return positionVector[1];
	}
	
	public synchronized void setPositionY(float y) {
		positionVector[1] = y;
	}
	
	public float getPositionZ() {
		return positionVector[2];
	}
	
	public synchronized void setPositionZ(float z) {
		positionVector[2] = z;
	}
	
	public float getPitch() {
		return pitch;
	}
	
	public float getYaw() {
		return yaw;
	}
	
	public synchronized void rotatePitch(float rotateRad) {
		float tPitch = pitch + rotateRad;
		if(tPitch > maxPitchAngle) {
			pitch = maxPitchAngle;
		} else if(tPitch < minPitchAngle) {
			pitch = minPitchAngle;
		} else {
			pitch = tPitch;
		}
		
		if(pitch >= (Math.PI * 0.5)) {
			pitch = (float) ((Math.PI * 0.5) * 0.99);
		} else if(tPitch <= -(Math.PI * 0.5)) {
			pitch = (float) (-(Math.PI * 0.5) * 0.99);
		}
	}
	
	public synchronized void rotateYaw(float rotateRad) {
		final float twoPi = (float) (Math.PI * 2.0f);
		final float threshold = 0.001f;
		final float minThres = twoPi - threshold;
		final float maxThres = twoPi + threshold;
		
		float t1 = minYawAngle - maxYawAngle;
		float t2 = maxYawAngle - minYawAngle;
		
		boolean wrapAround = minYawAngle == maxYawAngle || (t1 >= minThres && t1 <= maxThres) || (t2 >= minThres && t2 <= maxThres);
		float tYaw = yaw + rotateRad;
		if(wrapAround) {
			yaw = tYaw;
		} else if(tYaw < minYawAngle) {
			yaw = minYawAngle;
		} else if(tYaw > maxYawAngle) {
			yaw = maxYawAngle;
		} else {
			yaw = tYaw;
		}
	}
	
	public synchronized void setPitchRange(float min, float max) {
		minPitchAngle = min;
		maxPitchAngle = max;
	}
	
	public synchronized void setYawRange(float min, float max) {
		minYawAngle = min;
		maxYawAngle = max;
	}
	
	public synchronized void setLookAt(float pitch, float yaw) {
		this.pitch = pitch;
		this.yaw = yaw;
	}
}

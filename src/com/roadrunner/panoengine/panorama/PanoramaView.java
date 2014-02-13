package com.roadrunner.panoengine.panorama;

import java.util.Timer;
import java.util.TimerTask;

/* Thanks to sromku for the Polygon library: https://github.com/sromku/polygon-contains-point */
import com.sromku.polygon.Point;
import com.sromku.polygon.Polygon;

import android.content.Context;
import android.graphics.PointF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class PanoramaView extends GLSurfaceView implements SensorEventListener {

	private static final long TIMER_PERIOD = 10L;
	private static final float DRAG_FACTOR = 0.0001f;
	private static final float DRAG_RADIUS = 100.0f;
	private static final float SENSOR_FACTOR = 0.1f;
	private static final float ZOOM_FACTOR = 0.0005f;
	private static final float MAX_ZOOM = 2.0f;
	private static final float MIN_ZOOM = 0.5f;
	
	private PanoramaRenderer renderer;
	
	private SensorManager sensorManager;
	private Sensor accelerometer;
	private Sensor magnetometer;
	
	private boolean sensorEnabled = false;
	private boolean touchEnabled = true;
	
	private int touchCount = 0;
	private float[] initialDrag;
	private Float initialZoomLength;
	
	private boolean didMove = false;
	
	private Float yawStep = null;
	private Float pitchStep = null;
	private Float zoomStep = null;
	
	float[] lastAccelerometer = null;
	float[] lastMagnetometer = null;
	float[] firstOrientation = null;
	float[] orientation = new float[3];
	
	private Timer timer = new Timer();
	private TimerTask timerTask;
	
	public PanoramaView(Context context) {
		this(context, null);
	}
	
	public PanoramaView(Context context, AttributeSet attrs) {
		super(context, attrs);
		renderer = new PanoramaRenderer();
		setRenderer(renderer);
		
		sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		
		timerTask = new UpdateTimer(this);
		timer.schedule(timerTask, 0L, TIMER_PERIOD);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
		sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		sensorManager.unregisterListener(this);
	}
	
	public Panorama getPanorama() {
		return renderer.getPanorama();
	}
	
	public void setPanorama(Panorama panorama) {
		renderer.setPanorama(panorama);
	}
	
	public PanoramaCamera getCamera() {
		return renderer.getCamera();
	}
	
	public void setTouchScrollingEnabled(boolean enabled) {
		touchEnabled = enabled;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(touchEnabled) {
			int action = event.getActionMasked();
			
			switch(action) {
			case MotionEvent.ACTION_DOWN:
				touchCount = 1;
				break;
			case MotionEvent.ACTION_POINTER_DOWN:
				touchCount = 2;
				yawStep = null;
				pitchStep = null;
				initialDrag = null;
				break;
			case MotionEvent.ACTION_UP:
				touchCount = 0;
				yawStep = null;
				pitchStep = null;
				initialDrag = null;
				if(!didMove) {
					float touchX = event.getX();
					float touchY = event.getY();
					PointF touchPoint = new PointF(touchX, touchY);
					
					PanoramaCamera camera = getCamera();
					
					float view[] = new float[16];
					float lookAtVec[] = camera.getLookAtVector();
					Matrix.setLookAtM(view, 0, camera.getPositionX(), camera.getPositionY(), camera.getPositionZ(), lookAtVec[0], lookAtVec[1], lookAtVec[2], 0.0f, 1.0f, 0.0f);
					
					float projection[] = new float[16];
					Matrix_perspectiveM(projection, 0, camera.getAdjustedFOV(), ((float) getWidth()) / ((float) getHeight()), PanoramaRenderer.ZNEAR, PanoramaRenderer.ZFAR);
					
					for(PanoramaHotspot hotspot : getPanorama().getHotspots()) {
						float points[][] = new float[4][4];
						points[0][0] = -1.0f; points[0][1] = -1.0f; points[0][2] = PanoramaHotspot.R; points[0][3] = 1.0f;
						points[1][0] = 1.0f; points[1][1] = -1.0f; points[1][2] = PanoramaHotspot.R; points[1][3] = 1.0f;
						points[2][0] = 1.0f; points[2][1] = 1.0f; points[2][2] = PanoramaHotspot.R; points[2][3] = 1.0f;
						points[3][0] = -1.0f; points[3][1] = 1.0f; points[3][2] = PanoramaHotspot.R; points[3][3] = 1.0f;
						
						float model[] = new float[16];
						Matrix.setIdentityM(model, 0);
						Matrix.scaleM(model, 0, hotspot.getWidth(), hotspot.getHeight(), 1.0f);
						Matrix.rotateM(model, 0, (float) Math.toDegrees(hotspot.getX()), 0.0f, 1.0f, 0.0f);
						Matrix.rotateM(model, 0, (float) Math.toDegrees(hotspot.getY()), 1.0f, 0.0f, 0.0f);
						
						float finalMat[] = new float[16];
						Matrix.multiplyMM(finalMat, 0, view, 0, model, 0);
						Matrix.multiplyMM(finalMat, 0, projection, 0, finalMat, 0);
						
						for(int i = 0; i < 4; i++) {
							Matrix.multiplyMV(points[i], 0, finalMat, 0, points[i], 0);
						}
						
						Polygon.Builder builder = Polygon.Builder();
						float width = getWidth();
						float height = getHeight();
						float halfWidth = width * 0.5f;
						float halfHeight = height * 0.5f;
						for(int i = 0; i < 4; i++) {
							float new_x = (points[i][0] * width) / (2.0f * points[i][3]) + halfWidth;
							float new_y = height - ((points[i][1] * height) / (2.0f * points[i][3]) + halfHeight);
							builder.addVertex(new Point(new_x, new_y));
						}
						Polygon polygon = builder.build();
						
						boolean contains = polygon.contains(new Point(touchPoint.x, touchPoint.y));
						
						if(contains) {
							renderer.getPanorama().didTouchHotspot(hotspot);
							break;
						}
					}
				}
				didMove = false;
				break;
			case MotionEvent.ACTION_POINTER_UP:
				touchCount = 1;
				initialZoomLength = null;
				zoomStep = null;
				break;
			case MotionEvent.ACTION_MOVE:
				didMove = true;
				if(touchCount == 1) {
					if(initialDrag != null) {
						float x = event.getX();
						float y = event.getY();
						
						float deltaX = x - initialDrag[0];
						float deltaY = y - initialDrag[1];
						
						float length = Matrix.length(deltaX, deltaY, 0.0f);
						if(length > DRAG_RADIUS) {
							deltaX = (deltaX / length) * DRAG_RADIUS;
							deltaY = (deltaY / length) * DRAG_RADIUS;
						}
						
						yawStep = Float.valueOf(deltaX * DRAG_FACTOR);
						pitchStep = Float.valueOf(-deltaY * DRAG_FACTOR);
					} else {
						float x = event.getX();
						float y = event.getY();
						
						initialDrag = new float[2];
						initialDrag[0] = x;
						initialDrag[1] = y;
					}
				} else if(touchCount == 2) {
					if(initialZoomLength != null) {
						float x1 = event.getX(0);
						float x2 = event.getX(1);
						float y1 = event.getY(0);
						float y2 = event.getY(1);
						
						float x = x1 - x2;
						float y = y1 - y2;
						float length = Matrix.length(x, y, 0.0f);
						
						float lengthDiff = length - initialZoomLength.floatValue();
						float zoomStep = lengthDiff * ZOOM_FACTOR;
						this.zoomStep = Float.valueOf(zoomStep);
						
					} else {
						float x1 = event.getX(0);
						float x2 = event.getX(1);
						float y1 = event.getY(0);
						float y2 = event.getY(1);
						
						float x = x1 - x2;
						float y = y1 - y2;
						
						float length = Matrix.length(x, y, 0.0f);
						initialZoomLength = Float.valueOf(length);
					}
				}
				break;
			}
			return true;
		}
		return super.onTouchEvent(event);
	}
	
	/* 
	 * direct copy from Matrix.perspectiveM, which is not available at API level 13
	 */
	private static void Matrix_perspectiveM(float[] m, int offset,
	          float fovy, float aspect, float zNear, float zFar) {
	        float f = 1.0f / (float) Math.tan(Math.toRadians(fovy) / 2.0f);
	        float rangeReciprocal = 1.0f / (zNear - zFar);

	        m[offset + 0] = f / aspect;
	        m[offset + 1] = 0.0f;
	        m[offset + 2] = 0.0f;
	        m[offset + 3] = 0.0f;

	        m[offset + 4] = 0.0f;
	        m[offset + 5] = f;
	        m[offset + 6] = 0.0f;
	        m[offset + 7] = 0.0f;

	        m[offset + 8] = 0.0f;
	        m[offset + 9] = 0.0f;
	        m[offset + 10] = (zFar + zNear) * rangeReciprocal;
	        m[offset + 11] = -1.0f;

	        m[offset + 12] = 0.0f;
	        m[offset + 13] = 0.0f;
	        m[offset + 14] = 2.0f * zFar * zNear * rangeReciprocal;
	        m[offset + 15] = 0.0f;
	    }
	
	public void setSensorEnabled(boolean enabled) {
		sensorEnabled = enabled;
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if(sensorEnabled) {
			if(event.sensor == accelerometer) {
				if(lastAccelerometer == null) {
					lastAccelerometer = new float[3];
				}
				System.arraycopy(event.values, 0, lastAccelerometer, 0, 3);
			} else if(event.sensor == magnetometer) {
				if(lastMagnetometer == null) {
					lastMagnetometer = new float[3];
				}
				System.arraycopy(event.values, 0, lastMagnetometer, 0, 3);
			}
			
			if(lastMagnetometer != null && lastAccelerometer != null) {
				float[] rotation = new float[9];
				SensorManager.getRotationMatrix(rotation, null, lastAccelerometer, lastMagnetometer);
				if(firstOrientation != null) {
					SensorManager.getOrientation(rotation, orientation);
					
					float deltaX = (orientation[2] - firstOrientation[2]);
					float deltaY = (orientation[1] - firstOrientation[1]);
					
					getCamera().rotateYaw(deltaX * SENSOR_FACTOR);
					getCamera().rotatePitch(deltaY * SENSOR_FACTOR);
				} else {
					firstOrientation = new float[3];
					SensorManager.getOrientation(rotation, firstOrientation);
				}
			}
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// do nothing
	}
	
	private static class UpdateTimer extends TimerTask {

		private PanoramaView view;
		
		public UpdateTimer(PanoramaView view) {
			this.view = view;
		}
		
		@Override
		public void run() {
			if(view.yawStep != null) {
				view.getCamera().rotateYaw(view.yawStep.floatValue());
			}
			if(view.pitchStep != null) {
				view.getCamera().rotatePitch(view.pitchStep.floatValue());
			}
			if(view.zoomStep != null) {
				float zoom = view.getCamera().getZoomFactor() + view.zoomStep;
				if(zoom > MAX_ZOOM) {
					zoom = MAX_ZOOM;
				} else if(zoom < MIN_ZOOM) {
					zoom = MIN_ZOOM;
				}
				view.getCamera().setZoomFactor(zoom);
			}
		}
		
	}

}

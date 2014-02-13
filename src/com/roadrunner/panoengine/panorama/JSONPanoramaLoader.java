package com.roadrunner.panoengine.panorama;

import org.json.JSONArray;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.roadrunner.panoengine.opengl.OpenGLTexture;

public class JSONPanoramaLoader extends PanoramaLoader {
	public enum PanoramaType {
		Unknown, Cubic, Cylindrical
	}

	public interface BitmapLoader {
		public Bitmap loadBitmap(String assetID);
	}
	
	private BitmapLoader bitmapLoader;
	private JSONObject json;

	public JSONPanoramaLoader(BitmapLoader bitmapLoader, JSONObject json) {
		this.bitmapLoader = bitmapLoader;
		this.json = json;
	}

	@Override
	public void load(PanoramaView view) {
		Panorama panorama = null;

		String type = json.optString("type");
		PanoramaType panoramaType = PanoramaType.Unknown;
		if (type != null) {
			try {
				if (type.equals("cubic")) {
					panoramaType = PanoramaType.Cubic;
					int subdivisionX = json.getInt("subdivisionX");
					int subdivisionY = json.getInt("subdivisionY");
					panorama = new CubicPanorama(subdivisionX, subdivisionY);
				} else if (type.equals("cylindrical")) {
					panoramaType = PanoramaType.Cylindrical;
					int subdivisionX = json.getInt("subdivisionX");
					int subdivisionY = json.getInt("subdivisionY");
					panorama = new CylindricalPanorama(subdivisionX, subdivisionY);
				} else {
					throw new RuntimeException("Panorama type is not recognized");
				}
			} catch(Exception e) {
				throw new RuntimeException("Panorama could not be created", e);
			}
		} else {
			throw new RuntimeException("type property not exists");
		}

		JSONObject images = json.optJSONObject("images");
		if (images != null) {
				if(!images.isNull("preview")) {
					try {
						String assetID = images.getString("preview");
						OpenGLTexture texture = createTexture(bitmapLoader, assetID);
						for(int i = 0; i < panorama.getTexturesCount(); i++) {
							panorama.setTexture(texture, i);
						}
					} catch(Exception e) {
						e.printStackTrace();
					}
				}

			if (panoramaType == PanoramaType.Cubic) {
				CubicPanorama cPanorama = (CubicPanorama) panorama;

				JSONArray imageArr = null;
				try {
					imageArr = images.getJSONArray("front");
					jsonForCubicPanorama(
							bitmapLoader,
							imageArr,
							CubicPanorama.CubeFaceOrientation.FRONT,
							cPanorama);
				} catch(Exception e) {
					e.printStackTrace();
				}

				try {
					imageArr = images.getJSONArray("back");
					jsonForCubicPanorama(
							bitmapLoader,
							imageArr,
							CubicPanorama.CubeFaceOrientation.BACK,
							cPanorama);
				} catch(Exception e) {
					e.printStackTrace();
				}

				try {
					imageArr = images.getJSONArray("left");
					jsonForCubicPanorama(
							bitmapLoader,
							imageArr,
							CubicPanorama.CubeFaceOrientation.LEFT,
							cPanorama);
				} catch(Exception e){
					e.printStackTrace();
				}

				try {
					imageArr = images.getJSONArray("right");
					jsonForCubicPanorama(
							bitmapLoader,
							imageArr,
							CubicPanorama.CubeFaceOrientation.RIGHT,
							cPanorama);
				} catch(Exception e) {
					e.printStackTrace();
				}

				try {
					imageArr = images.getJSONArray("up");
					jsonForCubicPanorama(
							bitmapLoader,
							imageArr,
							CubicPanorama.CubeFaceOrientation.UP,
							cPanorama);
				} catch(Exception e) {
					e.printStackTrace();
				}

				try {
					imageArr = images.getJSONArray("down");
					jsonForCubicPanorama(
							bitmapLoader,
							imageArr,
							CubicPanorama.CubeFaceOrientation.DOWN,
							cPanorama);
				} catch(Exception e) {
					e.printStackTrace();
				}

			} else if (panoramaType == PanoramaType.Cylindrical) {
				CylindricalPanorama cPanorama = (CylindricalPanorama) panorama;

				JSONArray imageArr = images.optJSONArray("images");
				if(imageArr != null) {
					for (int i = 0; i < imageArr.length(); i++) {
						try {
							JSONObject image = imageArr.getJSONObject(i);
							int divX = image.getInt("divX");
							int divY = image.getInt("divY");
							String assetID = image.getString("assetID");
							CylindrialLoader loader = new CylindrialLoader(bitmapLoader,
									cPanorama, assetID, divX, divY);
							loader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
						} catch(Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		} else {
			throw new RuntimeException("images property not exists");
		}

		JSONObject camera = json.optJSONObject("camera");
		if (camera != null) {
			PanoramaCamera currentCamera = view.getCamera();
			int athmin = camera.optInt("athmin");
			int athmax = camera.optInt("athmax");
			int atvmin = camera.optInt("atvmin");
			int atvmax = camera.optInt("atvmax");
			int hlookat = camera.optInt("hlookat");
			int vlookat = camera.optInt("vlookat");
			currentCamera.setPitchRange((float) Math.toRadians(atvmin), (float)Math.toRadians(atvmax));
			currentCamera.setYawRange((float) Math.toRadians(athmin), (float) Math.toRadians(athmax));
			currentCamera.setLookAt((float) Math.toRadians(vlookat), (float) Math.toRadians(hlookat));
		}

		JSONArray hotspots = json.optJSONArray("hotspots");
		if (hotspots != null) {
			for (int i = 0; i < hotspots.length(); i++) {
				try {
					JSONObject hotspot = hotspots.getJSONObject(i);
					String assetId = hotspot.getString("image");
					OpenGLTexture hotspotTexture = createTexture(bitmapLoader,
							assetId);
					int identifier = hotspot.getInt("id");
					int atv = hotspot.getInt("atv");
					int ath = hotspot.getInt("ath");
					float width = (float) hotspot.getDouble("width");
					float height = (float) hotspot.getDouble("height");
					String data = hotspot.getString("data");
					PanoramaHotspot currentHotspot = new PanoramaHotspot(
							identifier, hotspotTexture, (float) Math.toRadians(atv), (float) Math.toRadians(ath), width,
							height, data);
							panorama.addHotspot(currentHotspot);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		JSONObject gyro = json.optJSONObject("gyro");
		if(gyro != null) {
			view.setSensorEnabled(gyro.optBoolean("enabled", false));
		}
		
		JSONObject scrolling = json.optJSONObject("scrolling");
		if(scrolling != null) {
			view.setTouchScrollingEnabled(scrolling.optBoolean("enabled", true));
		}
		
		view.setPanorama(panorama);
	}

	private static OpenGLTexture createTexture(BitmapLoader bitmapLoader, String assetID) throws Exception {
		Bitmap bm = bitmapLoader.loadBitmap(assetID);
		OpenGLTexture texture = new OpenGLTexture(bm);
		return texture;
	}

	private static class CubicLoader extends
			AsyncTask<Void, Void, OpenGLTexture> {
		private BitmapLoader bitmapLoader;
		private CubicPanorama panorama;
		private String assetID;
		private CubicPanorama.CubeFaceOrientation face;
		private int sX;
		private int sY;

		public CubicLoader(BitmapLoader bitmapLoader, CubicPanorama panorama,
				String assetID,
				CubicPanorama.CubeFaceOrientation face, int sX, int sY) {
			this.bitmapLoader = bitmapLoader;
			this.panorama = panorama;
			this.assetID = assetID;
			this.face = face;
			this.sX = sX;
			this.sY = sY;
		}

		@Override
		protected OpenGLTexture doInBackground(Void... params) {
			try {
				return createTexture(bitmapLoader, assetID);
			} catch (Exception e) {
				return null;
			}
		}

		@Override
		protected void onPostExecute(OpenGLTexture result) {
			panorama.setTexture(result, face, sX, sY);
		}
	}

	private static void jsonForCubicPanorama(BitmapLoader bitmapLoader,
			JSONArray jsonArray, CubicPanorama.CubeFaceOrientation face,
			CubicPanorama panorama) {
		int count = jsonArray.length();
		for (int i = 0; i < count; i++) {
			JSONObject image = jsonArray.optJSONObject(i);
			if(image != null) {
				try {
					int divX = image.getInt("divX");
					int divY = image.getInt("divY");
					String assetID = image.getString("assetID");
					CubicLoader loader = new CubicLoader(bitmapLoader, panorama,
							assetID, face, divX, divY);
					loader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static class CylindrialLoader extends
			AsyncTask<Void, Void, OpenGLTexture> {
		private BitmapLoader bitmapLoader;
		private CylindricalPanorama panorama;
		private String assetID;
		private int sX;
		private int sY;

		public CylindrialLoader(BitmapLoader bitmapLoader,
				CylindricalPanorama panorama, String assetID,
				int sX, int sY) {
			this.bitmapLoader = bitmapLoader;
			this.panorama = panorama;
			this.assetID = assetID;
			this.sX = sX;
			this.sY = sY;
		}

		@Override
		protected OpenGLTexture doInBackground(Void... params) {
			try {
				return createTexture(bitmapLoader, assetID);
			}catch (Throwable t) {
				return null;
			}
		}

		@Override
		protected void onPostExecute(OpenGLTexture result) {
			if(result != null) {
				panorama.setTexture(result, sX, sY);
			}
		}
	}
}

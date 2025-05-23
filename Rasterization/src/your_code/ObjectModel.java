package your_code;

//student 1 - Omer Goldstein - 205906258
//student 2 - Shlomi Fridman - 318187002

import java.io.IOException;
import java.util.List;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.joml.Vector4i;

import app_interface.DisplayTypeEnum;
import app_interface.ExerciseEnum;
import app_interface.IntBufferWrapper;
import app_interface.OBJLoader;
import app_interface.TriangleFace;

public class ObjectModel {
	WorldModel worldModel;

	private int imageWidth;
	private int imageHeight;

	private List<VertexData> verticesData;
	private List<TriangleFace> faces;
	private IntBufferWrapper textureImageIntBufferWrapper;

	private Matrix4f modelM = new Matrix4f();
	private Matrix4f lookatM = new Matrix4f();
	private Matrix4f projectionM = new Matrix4f();
	private Matrix4f viewportM = new Matrix4f();
	private Vector3f boundingBoxDimensions;
	private Vector3f boundingBoxCenter;

	private Vector3f lightPositionEyeCoordinates = new Vector3f();
	
	public static ExerciseEnum exercise = ExerciseEnum.EX_9___Lighting;
	
	public ObjectModel(WorldModel worldModel, int imageWidth, int imageHeight) {
		this.worldModel = worldModel;
		this.imageWidth = imageWidth;
		this.imageHeight = imageHeight;
	}

	void initTransfomations() {
		this.modelM.identity();
		this.modelM.identity();
		this.lookatM.identity();
		this.projectionM.identity();
		this.viewportM.identity();
	}
	
	void setModelM(Matrix4f modelM) {
		this.modelM = modelM;
	}

	void setLookatM(Matrix4f lookatM) {
		this.lookatM = lookatM;
	}

	void setProjectionM(Matrix4f projectionM) {
		this.projectionM = projectionM;
	}

	void setViewportM(Matrix4f viewportM) {
		this.viewportM = viewportM;
	}

	public Vector3f getBoundingBoxDimensions() {
		return boundingBoxDimensions;
	}

	public Vector3f getBoundingBoxCenter() {
		return boundingBoxCenter;
	}

	public boolean load(String fileName) {
		OBJLoader objLoader = new OBJLoader();
		try {
			objLoader.loadOBJ(fileName);
			verticesData = objLoader.getVertices();
			faces = objLoader.getFaces();
			boundingBoxDimensions = objLoader.getBoundingBoxDimensions();
			boundingBoxCenter = objLoader.getBoundingBoxCenter();
			textureImageIntBufferWrapper = objLoader.getTextureImageIntBufferWrapper();
			return true;
		} catch (IOException e) {
			//System.err.println("Failed to load the OBJ file.");
			return false;
		}
	}
	
	public boolean objectHasTexture() {
		return textureImageIntBufferWrapper != null;
	}

	public void render(IntBufferWrapper intBufferWrapper) {
		exercise = worldModel.exercise;

		
		if (verticesData != null) {
			for (VertexData vertexData : verticesData) {
				vertexProcessing(intBufferWrapper, vertexData);
			}
			for (TriangleFace face : faces) {
				rasterization(intBufferWrapper,	
						verticesData.get(face.indices[0]), 
						verticesData.get(face.indices[1]), 
						verticesData.get(face.indices[2]), 
						face.color);
			}
		}
	}

	private void vertexProcessing(IntBufferWrapper intBufferWrapper, VertexData vertex) {


			// Initialize a 4D vector from the 3D vertex point
			Vector4f t = new Vector4f(vertex.pointObjectCoordinates, 1f);
	
			// Transform only model transformation
			modelM.transform(t);
			vertex.pointEyeCoordinates = new Vector3f(t.x, t.y, t.z);
			
			vertex.pointWindowCoordinates = new Vector3f(t.x, t.y, t.z);


		// transformation normal from object coordinates to eye coordinates v->normal
		///////////////////////////////////////////////////////////////////////////////////
		transformNormalFromObjectCoordToEyeCoordAndDrawIt(intBufferWrapper, vertex);


	}

	private void transformNormalFromObjectCoordToEyeCoordAndDrawIt(IntBufferWrapper intBufferWrapper, VertexData vertex) {
		// transformation normal from object coordinates to eye coordinates v->normal
		///////////////////////////////////////////////////////////////////////////////////
		// --> v->NormalEyeCoordinates
		Matrix4f modelviewM = new Matrix4f(lookatM).mul(modelM);
		Matrix3f modelviewM3x3 = new Matrix3f();
		modelviewM.get3x3(modelviewM3x3);
		vertex.normalEyeCoordinates = new Vector3f();
		modelviewM3x3.transform(vertex.normalObjectCoordinates, vertex.normalEyeCoordinates);
		if (worldModel.displayNormals) {
			// drawing normals
			Vector3f t1 = new Vector3f(vertex.normalEyeCoordinates);
			Vector4f point_plusNormal_eyeCoordinates = new Vector4f(t1.mul(0.1f).add(vertex.pointEyeCoordinates),
					1);
			Vector4f t2 = new Vector4f(point_plusNormal_eyeCoordinates);
			// modelviewM.transform(t2);
			projectionM.transform(t2);
			if (t2.w != 0) {
				t2.mul(1 / t2.w);
			} else {
				System.err.println("Division by w == 0 in vertexProcessing normal transformation");
			}
			viewportM.transform(t2);
			Vector3f point_plusNormal_screen = new Vector3f(t2.x, t2.y, t2.z);
			drawLineDDA(intBufferWrapper, vertex.pointWindowCoordinates, point_plusNormal_screen, 0, 0, 1f);
		}
		
	}
	
	
	private void rasterization(IntBufferWrapper intBufferWrapper, VertexData vertex1, VertexData vertex2, VertexData vertex3, Vector3f faceColor) {

		Vector3f faceNormal = new Vector3f(vertex2.pointEyeCoordinates).sub(vertex1.pointEyeCoordinates)
					.cross(new Vector3f(vertex3.pointEyeCoordinates).sub(vertex1.pointEyeCoordinates))
					.normalize();
		

		if (worldModel.displayType == DisplayTypeEnum.FACE_EDGES) {
			drawLineDDA(intBufferWrapper,vertex1.pointWindowCoordinates,vertex2.pointWindowCoordinates,1f,1f,1f);
			drawLineDDA(intBufferWrapper,vertex1.pointWindowCoordinates,vertex3.pointWindowCoordinates,1f,1f,1f);
			drawLineDDA(intBufferWrapper,vertex2.pointWindowCoordinates,vertex3.pointWindowCoordinates,1f,1f,1f);
			

		} else {
			Vector3f p1 = new Vector3f(vertex1.pointWindowCoordinates);
			Vector3f p2 = new Vector3f(vertex2.pointWindowCoordinates);
			Vector3f p3 = new Vector3f(vertex3.pointWindowCoordinates);
			
			Vector4i boundingBox = calcBoundingBox(p1, p2, p3, imageWidth, imageHeight);
			BarycentricCoordinates bc = new BarycentricCoordinates(p1, p2, p3);
			
			for (int y = boundingBox.z; y <= boundingBox.w; y++) {
				for (int x = boundingBox.x; x <= boundingBox.y; x++) {
					bc.calcCoordinatesForPoint(x, y);
					if (bc.isPointInside()) {
						FragmentData fragmentData = new FragmentData();
						if (worldModel.displayType == DisplayTypeEnum.FACE_COLOR) {
							fragmentData.pixelColor = new Vector3f(faceColor);
						} else if (worldModel.displayType == DisplayTypeEnum.INTERPOlATED_VERTEX_COLOR) {
							
						} else if (worldModel.displayType == DisplayTypeEnum.LIGHTING_FLAT) {
							
						} else if (worldModel.displayType == DisplayTypeEnum.LIGHTING_GOURARD) {
							
						} else if (worldModel.displayType == DisplayTypeEnum.LIGHTING_PHONG) {
							
						} else if (worldModel.displayType == DisplayTypeEnum.TEXTURE) {
							
						} else if (worldModel.displayType == DisplayTypeEnum.TEXTURE_LIGHTING) {
							
						}

						Vector3f pixelColor = fragmentProcessing(fragmentData);
						intBufferWrapper.setPixel(x, y, pixelColor);
					}
				}
			}

		}
		
	}


	private Vector3f fragmentProcessing(FragmentData fragmentData) {
		
		if (worldModel.displayType == DisplayTypeEnum.FACE_COLOR) {
			return fragmentData.pixelColor;
		} else if (worldModel.displayType == DisplayTypeEnum.INTERPOlATED_VERTEX_COLOR) {
		} else if (worldModel.displayType == DisplayTypeEnum.LIGHTING_FLAT) {
		} else if (worldModel.displayType == DisplayTypeEnum.LIGHTING_GOURARD) {
		} else if (worldModel.displayType == DisplayTypeEnum.LIGHTING_PHONG) {
		} else if (worldModel.displayType == DisplayTypeEnum.TEXTURE) {
		} else if (worldModel.displayType == DisplayTypeEnum.TEXTURE_LIGHTING) {
		}
		return new Vector3f();
		
	}

	

	static void drawLineDDA(IntBufferWrapper intBufferWrapper, Vector3f p1, Vector3f p2, float r, float g, float b) {
		int x1 = Math.round(p1.x);
		int y1 = Math.round(p1.y);
		int x2 = Math.round(p2.x);
		int y2 = Math.round(p2.y);

		int dx = x2 - x1;
		int dy = y2 - y1;

		int steps = Math.max(Math.abs(dx), Math.abs(dy));
		if (steps == 0)
			return;

		float xInc = dx / (float) steps;
		float yInc = dy / (float) steps;

		float x = x1;
		float y = y1;

		for (int i = 0; i <= steps; i++) {
			intBufferWrapper.setPixel(Math.round(x), Math.round(y), new Vector3f(r, g, b));
			x += xInc;
			y += yInc;
		}
		
	}



	static Vector4i calcBoundingBox(Vector3f p1, Vector3f p2, Vector3f p3, int imageWidth, int imageHeight) { 
		int minX,maxX,minY,maxY;
		minX = (int) Math.floor(Math.max(Math.min(p1.x, Math.min(p2.x, p3.x)), 0f));
		maxX = (int) Math.round(Math.min(Math.max(p1.x, Math.max(p2.x, p3.x)), imageWidth-1));
		minY = (int) Math.floor(Math.max(Math.min(p1.y, Math.min(p2.y, p3.y)), 0f));
		maxY = (int) Math.round(Math.min(Math.max(p1.y, Math.max(p2.y, p3.y)), imageHeight-1));

		return new Vector4i(minX, maxX, minY, maxY);

	}

	
	float lightingEquation(Vector3f point, Vector3f PointNormal, Vector3f LightPos, float Kd, float Ks, float Ka, float shininess) {

		Vector3f color = lightingEquation(point, PointNormal, LightPos, 
				                          new Vector3f(Kd), new Vector3f(Ks), new Vector3f(Ka), shininess);
		return color.get(0);
	}
	
	
	private static Vector3f lightingEquation(Vector3f point, Vector3f PointNormal, Vector3f LightPos, Vector3f Kd,
			Vector3f Ks, Vector3f Ka, float shininess) {

		Vector3f returnedColor = new Vector3f();


		return returnedColor;
	}	
}



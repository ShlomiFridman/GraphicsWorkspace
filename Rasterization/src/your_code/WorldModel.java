package your_code;

//student 1 - Omer Goldstein - 205906258
//student 2 - Shlomi Fridman - 318187002

import java.nio.IntBuffer;
import java.util.Random;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import app_interface.DisplayTypeEnum;
import app_interface.ExerciseEnum;
import app_interface.IntBufferWrapper;
import app_interface.ProjectionTypeEnum;

public class WorldModel {

	// type of rendering
	public ProjectionTypeEnum projectionType;
	public DisplayTypeEnum displayType;
	public boolean displayNormals;
	public YourSelectionEnum yourSelection;
	
	// camera location parameters
	public Vector3f cameraPos = new Vector3f();
	public Vector3f cameraLookAtCenter = new Vector3f();
	public Vector3f cameraUp = new Vector3f();
	public float horizontalFOV;

	// transformation parameters
	public float modelScale;

	// lighting parameters
	public float lighting_Diffuse;
	public float lighting_Specular;
	public float lighting_Ambient;
	public float lighting_sHininess;
	public Vector3f lightPositionWorldCoordinates = new Vector3f();
	
	public ExerciseEnum exercise;

	private int imageWidth;
	private int imageHeight;

	private ObjectModel object1;
	
	float zBuffer[][];
	
	private int counter = 0;
	
	ErrorLogger errorLogger;
	
	public WorldModel(int imageWidth, int imageHeight, ErrorLogger errorLogger) {
		this.imageWidth  = imageWidth;
		this.imageHeight = imageHeight;
		this.zBuffer = new float[imageWidth][imageHeight];
		this.errorLogger = errorLogger;
	}


	public boolean load(String fileName) {
		object1 = new ObjectModel(this, imageWidth, imageHeight);
		return object1.load(fileName);
	}
	
	public boolean modelHasTexture() {
		return object1.objectHasTexture();
	}
	
	
	public void render(IntBufferWrapper intBufferWrapper) {
		counter+=1;
		intBufferWrapper.imageClear();
		clearZbuffer();
		object1.initTransfomations();
		Matrix4f translationMatrix = new Matrix4f();
				
		if (exercise.ordinal() == ExerciseEnum.EX_3_1_Object_transformation___translation.ordinal()) {
			Random rand = new Random();
			float offset = 10f;
			float offset_x = rand.nextFloat(offset) - offset/2f;
			float offset_y = rand.nextFloat(offset) -offset/2f;
			translationMatrix.translate(offset_x, offset_y, 0f);
			object1.setModelM(translationMatrix);
		}
	
		if (exercise.ordinal() == ExerciseEnum.EX_3_2_Object_transformation___scale.ordinal()) {
			int period = 20;
		    float centerX = 300f;
		    float centerY = 300f;
		    float minScale = 0.8f;
		    float maxScale = 1.1f;
		    int frameInCycle = counter % period;
		    float scale;
		    if (frameInCycle < period / 2) {
		        scale = minScale + (maxScale - minScale) * (frameInCycle / (float)(period / 2));
		    } else 
		        scale = maxScale - (maxScale - minScale) * ((frameInCycle - period / 2) / (float)(period / 2));
		    translationMatrix.translate(centerX, centerY, 0)
		    	    .scale(scale)
		    	    .translate(-centerX, -centerY, 0);
		    object1.setModelM(translationMatrix);
		}

		if (exercise.ordinal() == ExerciseEnum.EX_3_3_Object_transformation___4_objects.ordinal()) {
			float[][] positions = {
			        {0f, 0f},
			        {600f, 0f},
			        {0f, 600f},
			        {600f, 600f}
			    };

			    for (int i = 0; i < positions.length; i++) {
			        float x = positions[i][0];
			        float y = positions[i][1];

			        Matrix4f tmp_translationMatrix = new Matrix4f();
			        tmp_translationMatrix.translate(x, y, 0)
			            .scale(0.5f)
			            .translate(-x, -y, 0);

			        object1.setModelM(tmp_translationMatrix);
		            object1.render(intBufferWrapper);
			    }
		}


			if(projectionType==ProjectionTypeEnum.ORTHOGRAPHIC) {


			}

			
			if(projectionType==ProjectionTypeEnum.PERSPECTIVE) {


			}
			
		
		object1.render(intBufferWrapper);
	}
	
	private void clearZbuffer() {
		for(int i=0; i<imageHeight; i++)
			for(int j=0; j<imageWidth; j++)
				zBuffer[i][j] = 1;
	}	
}

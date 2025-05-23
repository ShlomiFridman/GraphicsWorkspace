package app_interface;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.IntBuffer;

import javax.imageio.ImageIO;

import org.joml.Vector3f;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

class IntBufferWrapper {
    private IntBuffer intBuffer;
    private int imageWidth;
    private int imageHeight;
    private String bmpFilePath;

    IntBufferWrapper(int imageWidth, int imageHeight) {
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.intBuffer = IntBuffer.allocate(imageWidth * imageHeight);
    }
    
    IntBufferWrapper(IntBuffer intBuffer, int imageWidth, int imageHeight) {
        this.intBuffer = intBuffer;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
    }

    IntBufferWrapper(String bmpFilePath) throws IOException {
        // Load the BMP image using ImageIO
        BufferedImage image = ImageIO.read(new File(bmpFilePath));
        this.imageWidth = image.getWidth();
        this.imageHeight = image.getHeight();
        this.intBuffer = IntBuffer.allocate(imageWidth * imageHeight);

        // Load the pixel data into the IntBuffer
        for (int y = 0; y < imageHeight; y++) {
            for (int x = 0; x < imageWidth; x++) {
                int argb = image.getRGB(x, y);  // Get pixel color in ARGB format
                intBuffer.put(y * imageWidth + x, argb);
            }
        }
        
        this.bmpFilePath = bmpFilePath;
    }    
    
    IntBuffer getIntBuffer() {
    	return intBuffer;
    }
    int getImageWidth() {
		return imageWidth;
	}
	int getImageHeight() {
		return imageHeight;
	}

//	// Method to set pixel using RGB values
//    void setPixel(int x, int y, int r, int g, int b) {
//    	int y_ = imageHeight - y - 1;
//    	int argb = (255 << 24) | ( ( (r>255)?255:r ) << 16) | ( ((g>255)?255:g) << 8) | ((b>255)?255:b);
//        try {
//            intBuffer.put(y_ * imageWidth + x, argb);
//        } catch (IndexOutOfBoundsException e) {
//            System.err.println("IndexOutOfBoundsException at IntBufferWrapper.setPixel for pixel x="+x+",y="+y + " " +bmpFilePath);
//        }
//    }
	// Method to set pixel using RGB values
    void setPixel(int x, int y, float r, float g, float b) {
    	int y_ = imageHeight - y - 1;
    	int argb = (255 << 24) | 
    			( ((r>1)?255:(int)(r*255) ) << 16) | 
    			( ((g>1)?255:(int)(g*255) ) << 8) | 
    			  ((b>1)?255:(int)(b*255)   );
        try {
            intBuffer.put(y_ * imageWidth + x, argb);
        } catch (IndexOutOfBoundsException e) {
            System.err.println("IndexOutOfBoundsException at IntBufferWrapper.setPixel for pixel x="+x+",y="+y + " " +bmpFilePath);
        }
    }

//    // Method to set pixel using a color array
//    void setPixel(int x, int y, short[] color) {
//    	int y_ = imageHeight - y - 1;
//    	int argb = (255 << 24) | ( ( (color[0]>255)?255:color[0] ) << 16) | ( ((color[1]>255)?255:color[1]) << 8) | ((color[2]>255)?255:color[2]);
//        try {
//            intBuffer.put(y_ * imageWidth + x, argb);
//        } catch (IndexOutOfBoundsException e) {
//            System.err.println("IndexOutOfBoundsException at IntBufferWrapper.setPixel for pixel x="+x+",y="+y + " " +bmpFilePath);
//        }
//    }
    // Method to set pixel using a color array
    void setPixel(int x, int y, Vector3f color) {
    	int y_ = imageHeight - y - 1;
    	int argb = (255 << 24) | 
    			   ( (color.get(0)>1)?255:(int)(color.get(0)*255) ) << 16 | 
    			   ( (color.get(1)>1)?255:(int)(color.get(1)*255) ) << 8  | 
    			   ( (color.get(2)>1)?255:(int)(color.get(2)*255) )	  ;
        try {
            intBuffer.put(y_ * imageWidth + x, argb);
        } catch (IndexOutOfBoundsException e) {
            System.err.println("IndexOutOfBoundsException at IntBufferWrapper.setPixel for pixel x="+x+",y="+y + " " +bmpFilePath);
        }
    }

    
//    // Method to get pixel 
//    void getPixel(int x, int y, short[] color) {
//        try {
//        	int y_ = imageHeight - y - 1;
//        	int argb = intBuffer.get(y_ * imageWidth + x);
//            color[0] = (short) ((argb >> 16) & 0xFF); // Red component
//            color[1] = (short) ((argb >>  8) & 0xFF); // Green component
//            color[2] = (short) (argb & 0xFF);         // Blue component
//        } catch (IndexOutOfBoundsException e) {
//            System.err.println("IndexOutOfBoundsException at IntBufferWrapper.getPixel for pixel x="+x+",y="+y + " " +bmpFilePath);
//        }
//    }
    // Method to get pixel 
    Vector3f getPixel(int x, int y) {
        try {
        	int y_ = imageHeight - y - 1;
        	int argb = intBuffer.get(y_ * imageWidth + x);
        	return new Vector3f(   
        			(float)((argb >> 16) & 0xFF)/255, // Red component
        			(float)((argb >>  8) & 0xFF)/255, // Green component
        			(float)(argb & 0xFF)/255          // Blue component
        			 );
        } catch (IndexOutOfBoundsException e) {
            System.err.println("IndexOutOfBoundsException at IntBufferWrapper.getPixel for pixel x="+x+",y="+y + " " +bmpFilePath);
            return new Vector3f();
        }
    }

    
    // Method to clear the image by setting all pixels to black
    void imageClear() {
        for (int i = 0; i < imageHeight * imageWidth; i++) {
            intBuffer.put(i, -16777216); // black with full alpha (255 << 24)
        }
    }

    // Save the IntBuffer to a BMP file
    void saveToBMP(String filename) {
        Utilities.saveIntBufferAsBMP(intBuffer, imageWidth, imageHeight, filename);
    }

    // Save the IntBuffer to a text file (for debugging purposes)
    void saveToCSV(String filename) {
        Utilities.saveIntBufferAsCSV(intBuffer, imageWidth, imageHeight, filename);
    }

//    // Method to write text on IntBuffer at (x, y) with specified height and color
//    void writeText(String text, int x, int y, int textHeight, int r, int g, int b) {
//        Canvas canvas = new Canvas(imageWidth, imageHeight);
//        GraphicsContext gc = canvas.getGraphicsContext2D();
//
//        // Set the font size
//        gc.setFont(new javafx.scene.text.Font(textHeight));
//        // Set the text color
//        gc.setFill(Color.rgb(r, g, b));
//
//        // Draw the text at the specified (x, y) position
//        gc.fillText(text, x, y + textHeight);
//
//        // Create a WritableImage to extract pixel data
//        WritableImage writableImage = new WritableImage(imageWidth, imageHeight);
//        canvas.snapshot(null, writableImage);
//        PixelReader pixelReader = writableImage.getPixelReader();
//
//        // Transfer pixel data from the WritableImage to the IntBuffer
//        for (int j = 0; j < imageHeight; j++) {
//            for (int i = 0; i < imageWidth; i++) {
//                int argb = pixelReader.getArgb(i, j);
//                if(argb!=-1)
////                	intBuffer.put(j * imageWidth + i, argb);
//                    setPixel(i, imageHeight - j - 1, r, g, b);
//                	
//            }
//        }
//    }
    // Method to write text on IntBuffer at (x, y) with specified height and color
    void writeText(String text, int x, int y, int textHeight, float r, float g, float b) {
        Canvas canvas = new Canvas(imageWidth, imageHeight);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Set the font size
        gc.setFont(new javafx.scene.text.Font(textHeight));
        // Set the text color
        gc.setFill(Color.rgb((int)(r*255), (int)(g*255), (int)(b*255)));

        // Draw the text at the specified (x, y) position
        gc.fillText(text, x, y + textHeight);

        // Create a WritableImage to extract pixel data
        WritableImage writableImage = new WritableImage(imageWidth, imageHeight);
        canvas.snapshot(null, writableImage);
        PixelReader pixelReader = writableImage.getPixelReader();

        // Transfer pixel data from the WritableImage to the IntBuffer
        for (int j = 0; j < imageHeight; j++) {
            for (int i = 0; i < imageWidth; i++) {
                int argb = pixelReader.getArgb(i, j);
                if(argb!=-1)
//                	intBuffer.put(j * imageWidth + i, argb);
                    setPixel(i, imageHeight - j - 1, r, g, b);
                	
            }
        }
    }

    
//    // Method to set all pixels in the IntBuffer to a single RGB color
//    void fillImageWithColor(int r, int g, int b) {
//        int argb = (255 << 24) | (r << 16) | (g << 8) | b;  // Full opacity (ARGB)
//        for (int i = 0; i < imageWidth * imageHeight; i++) {
//            intBuffer.put(i, argb);
//        }
//    }
    // Method to set all pixels in the IntBuffer to a single RGB color
    void fillImageWithColor(float r, float g, float b) {
        int argb = (255 << 24) | 
        		((int)(r*255) << 16) | 
        		((int)(g*255) <<  8) | 
        		 (int)(b*255);  // Full opacity (ARGB)
        for (int i = 0; i < imageWidth * imageHeight; i++) {
            intBuffer.put(i, argb);
        }
    }
}


/*
package app_interface;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.nio.IntBuffer;
import java.util.List;

public class IntBufferWrapper {
    private IntBuffer intBuffer;
    private int imageWidth;
    private int imageHeight;
    private String bmpFilePath;

    public IntBufferWrapper(int imageWidth, int imageHeight) {
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.intBuffer = IntBuffer.allocate(imageWidth * imageHeight);
    }
    
    public IntBufferWrapper(IntBuffer intBuffer, int imageWidth, int imageHeight) {
        this.intBuffer = intBuffer;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
    }

    // Method to write text on IntBuffer at (x, y) with specified height and color
    public void writeText(int x, int y, int textHeight, String text, int r, int g, int b) {
        Canvas canvas = new Canvas(imageWidth, imageHeight);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Set the font size
        gc.setFont(new javafx.scene.text.Font(textHeight));
        // Set the text color
        gc.setFill(Color.rgb(r, g, b));

        // Draw the text at the specified (x, y) position
        gc.fillText(text, x, y + textHeight);

        // Create a WritableImage to extract pixel data
        WritableImage writableImage = new WritableImage(imageWidth, imageHeight);
        canvas.snapshot(null, writableImage);
        PixelReader pixelReader = writableImage.getPixelReader();

        // Transfer pixel data from the WritableImage to the IntBuffer
        for (int j = 0; j < imageHeight; j++) {
            for (int i = 0; i < imageWidth; i++) {
                int argb = pixelReader.getArgb(i, j);
                intBuffer.put(j * imageWidth + i, argb);
            }
        }
    }

    // Method to set all pixels in the IntBuffer to a single RGB color
    public void fillBufferWithColor(int r, int g, int b) {
        int argb = (255 << 24) | (r << 16) | (g << 8) | b;  // Full opacity (ARGB)
        for (int i = 0; i < imageWidth * imageHeight; i++) {
            intBuffer.put(i, argb);
        }
    }

    public IntBuffer getIntBuffer() {
        return intBuffer;
    }

    public int getImageWidth() {
        return imageWidth;
    }

    public int getImageHeight() {
        return imageHeight;
    }

    // Other existing methods...
}
*/
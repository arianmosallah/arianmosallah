import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Main extends Application {
	short cthead[][][]; // store the 3D volume data set
	short min, max; // min max value in the 3D volume data set

	@Override
	public void start(Stage stage) throws FileNotFoundException, IOException {
		stage.setTitle("CThead Viewer");
		ReadData();

		int width = 256;
		int height = 256;

		// images we can write to
		WritableImage zmedical_image = new WritableImage(width, height);
		WritableImage ymedical_image = new WritableImage(width, 113);
		WritableImage xmedical_image = new WritableImage(width, 113);
		// to view the images
		ImageView zView = new ImageView(zmedical_image);
		ImageView yView = new ImageView(ymedical_image);
		ImageView xView = new ImageView(xmedical_image);

		// to perform maximum intensity projection
		Button zMipBUTTON = new Button("TOP MIP");
		Button yMipBUTTON = new Button("FRONT MIP");
		Button xMipBUTTON = new Button("SIDE MIP");

		// sliders to step through the slices (z and y directions) (remember 113 slices
		// in z direction 0-112)
		Slider zslider = new Slider(0, 112, 0);
		Slider yslider = new Slider(0, 255, 0);
		Slider xslider = new Slider(0, 255, 0);

		// to view all the thumbnails of images
		Button zThumbnail = new Button("Thumbails");
		Button yThumbnail = new Button("Thumbails");
		Button xThumbnail = new Button("Thumbails");

		// Buttons on click will perform mip specific to each image
		zMipBUTTON.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				zMIP(zmedical_image);
			}
		});

		yMipBUTTON.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				yMIP(ymedical_image);
			}
		});

		xMipBUTTON.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				xMIP(xmedical_image);
			}
		});

		// taking value of slider, puts as parameter in each view method to find
		// particular image to show
		zslider.valueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				System.out.println(newValue.intValue());
				zView(zmedical_image, newValue.intValue());
			}
		});

		yslider.valueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				System.out.println(newValue.intValue());
				yView(ymedical_image, newValue.intValue());
			}
		});

		xslider.valueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				System.out.println(newValue.intValue());
				xView(xmedical_image, newValue.intValue());
			}
		});

		FlowPane root = new FlowPane();
		root.setVgap(8);
		root.setHgap(4);

		// 3. (referring to the 3 things we need to display an image)
		// we need to add it to the flow pane
		root.getChildren().addAll(zView, zslider, zMipBUTTON, zThumbnail);
		root.getChildren().addAll(yView, yslider, yMipBUTTON, yThumbnail);
		root.getChildren().addAll(xView, xslider, xMipBUTTON, xThumbnail);

		Scene scene = new Scene(root, 640, 480);
		stage.setScene(scene);
		stage.show();

		// An array of both writable image and Image view is created for z,y,x views
		// To store all the images accordingly
		WritableImage zthumbedit[] = new WritableImage[255];
		ImageView zthumbArray[] = new ImageView[255];
		zThumbnail.setOnMouseClicked((event) -> {
			GridPane gridPane = new GridPane(); // used instead of stackpane
			// so i can indicate exactly where to put thumbnail
			int zc = 0; // zimage column, row
			int zr = 0;
			for (int i = 0; i < 113; i++) {
				zthumbedit[i] = new WritableImage(255, 255);
				zthumbArray[i] = new ImageView(zthumbedit[i]);
				zthumbArray[i].setFitHeight(50); // make the image smaller
				zthumbArray[i].setFitWidth(50);

				zView(zthumbedit[i], i);
				if (i % 10 == 0) { // just so it goes into next row when full
					zr++;
					zc = 0;
				}
				final int k = i; // to use in event handler method
				zthumbArray[i].addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {

					@Override
					public void handle(MouseEvent event) {
						zView(zmedical_image, k); // show the image in the main window when clicked
						event.consume();
					}
				});
				gridPane.add(zthumbArray[i], zc, zr); // add them all into the view
				zc++;
			}

			Scene secondScene = new Scene(gridPane, 500, 600);
			Stage secondStage = new Stage();
			secondStage.setScene(secondScene); // set the scene
			secondStage.setTitle("TOP VIEW THUMBNAILS");
			secondStage.show();
		});

		WritableImage ythumbedit[] = new WritableImage[255];
		ImageView ythumbArray[] = new ImageView[255];
		yThumbnail.setOnMouseClicked((event) -> {
			GridPane gridPane = new GridPane();
			int yc = 0;
			int yr = 0;
			for (int i = 0; i < 255; i++) { // 255 images this time
				ythumbedit[i] = new WritableImage(255, 113);
				ythumbArray[i] = new ImageView(ythumbedit[i]);
				ythumbArray[i].setFitHeight(50);
				ythumbArray[i].setFitWidth(50);

				yView(ythumbedit[i], i);
				if (i % 16 == 0) { // just to fit the larger quantity in smaller height
					yr++;
					yc = 0;
				}
				final int k = i;
				ythumbArray[i].addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {

					@Override
					public void handle(MouseEvent event) {
						yView(ymedical_image, k);
						event.consume();
					}
				});
				gridPane.add(ythumbArray[i], yc, yr);
				yc++;
			}

			Scene secondScene = new Scene(gridPane, 800, 800);
			Stage secondStage = new Stage();
			secondStage.setScene(secondScene); // set the scene
			secondStage.setTitle("FRONT VIEW THUMBNAILS");
			secondStage.show();
		});

		WritableImage xthumbedit[] = new WritableImage[255];
		ImageView xthumbArray[] = new ImageView[255];
		xThumbnail.setOnMouseClicked((event) -> {
			GridPane gridPane = new GridPane();
			int xc = 0;
			int xr = 0;
			for (int i = 0; i < 255; i++) { // 255 images this time
				xthumbedit[i] = new WritableImage(255, 113);
				xthumbArray[i] = new ImageView(xthumbedit[i]);
				xthumbArray[i].setFitHeight(50);
				xthumbArray[i].setFitWidth(50);

				xView(xthumbedit[i], i);
				if (i % 16 == 0) {
					xr++;
					xc = 0;
				}
				final int k = i;
				xthumbArray[i].addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {

					@Override
					public void handle(MouseEvent event) {
						xView(xmedical_image, k);
						event.consume();
					}
				});
				gridPane.add(xthumbArray[i], xc, xr);
				xc++;
			}

			Scene secondScene = new Scene(gridPane, 800, 800);
			Stage secondStage = new Stage();
			secondStage.setScene(secondScene); // set the scene
			secondStage.setTitle("SIDE VIEW THUMBNAILS");
			secondStage.show();
		});

	}

	// Function to read in the cthead data set
	public void ReadData() throws IOException {
		// File name is hardcoded here - much nicer to have a dialog to select it and
		// capture the size from the user
		File file = new File("C:\\Users\\aryan\\Documents\\Computer Science\\Cthead.raw");
		// Read the data quickly via a buffer (in C++ you can just do a single fread - I
		// couldn't find if there is an equivalent in Java)
		DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));

		int i, j, k; // loop through the 3D data set

		min = Short.MAX_VALUE;
		max = Short.MIN_VALUE; // set to extreme values
		short read; // value read in
		int b1, b2; // data is wrong Endian (check wikipedia) for Java so we need to swap the bytes
					// around

		cthead = new short[113][256][256]; // allocate the memory - note this is fixed for this data set
		// loop through the data reading it in
		for (k = 0; k < 113; k++) {
			for (j = 0; j < 256; j++) {
				for (i = 0; i < 256; i++) {
					// because the Endianess is wrong, it needs to be read byte at a time and
					// swapped
					b1 = ((int) in.readByte()) & 0xff; // the 0xff is because Java does not have unsigned types
					b2 = ((int) in.readByte()) & 0xff; // the 0xff is because Java does not have unsigned types
					read = (short) ((b2 << 8) | b1); // and swizzle the bytes around
					if (read < min)
						min = read; // update the minimum
					if (read > max)
						max = read; // update the maximum
					cthead[k][j][i] = read; // put the short into memory (in C++ you can replace all this code with one
											// fread)
				}
			}
		}
		System.out.println(min + " " + max); // diagnostic - for CThead this should be -1117, 2248
		// (i.e. there are 3366 levels of grey (we are trying to display on 256 levels
		// of grey)
		// therefore histogram equalization would be a good thing
		// maybe put your histogram equalization code here to set up the mapping array
	}

	/*
	 * This function shows how to carry out an operation on an image. It obtains the
	 * dimensions of the image, and then loops through the image carrying out the
	 * copying of a slice of data into the image.
	 */
	public void zView(WritableImage image, int sliceNum) {
		// Get image dimensions, and declare loop variables
		int w = (int) image.getWidth(), h = (int) image.getHeight(), i, j;
		PixelWriter image_writer = image.getPixelWriter();

		float col;
		short datum;
		// Shows how to loop through each pixel and colour
		// Try to always use j for loops in y, and i for loops in x
		// as this makes the code more readable
		for (j = 0; j < h; j++) {
			for (i = 0; i < w; i++) {
				datum = cthead[sliceNum][j][i]; // get values from each slice "sliceNum"
				// calculate the colour by performing a mapping from [min,max] -> 0 to 1 (float)
				// Java setColor uses float values from 0 to 1 rather than 0-255 bytes for
				// colour
				col = (((float) datum - (float) min) / ((float) (max - min)));
				image_writer.setColor(i, j, Color.color(col, col, col, 1.0));
			} // column loop
		} // row loop
	}

	public void yView(WritableImage image, int sliceNum) {
		int w = (int) image.getWidth(), h = (int) image.getHeight(), i, j;
		PixelWriter image_writer = image.getPixelWriter();

		float col;
		short datum;
		for (j = 0; j < h; j++) {
			for (i = 0; i < w; i++) {
				datum = cthead[j][sliceNum][i];
				col = (((float) datum - (float) min) / ((float) (max - min)));
				image_writer.setColor(i, j, Color.color(col, col, col, 1.0));
			}
		}
	}

	public void xView(WritableImage image, int sliceNum) {
		int w = (int) image.getWidth(), h = (int) image.getHeight(), i, j;
		PixelWriter image_writer = image.getPixelWriter();

		float col;
		short datum;
		for (j = 0; j < h; j++) {
			for (i = 0; i < w; i++) {
				datum = cthead[j][i][sliceNum];
				col = (((float) datum - (float) min) / ((float) (max - min)));
				image_writer.setColor(i, j, Color.color(col, col, col, 1.0));
			}
		}
	}

	public void zMIP(WritableImage image) {
		int w = (int) image.getWidth(), h = (int) image.getHeight(), i, j, c, k;
		PixelWriter image_writer = image.getPixelWriter();

		float col;
		short datum;
		for (j = 0; j < h; j++) {
			for (i = 0; i < w; i++) {
				double m = 0.000001; // very small value

				for (k = 0; k < 113; k++) {

					m = Math.max(cthead[k][j][i], m); // take max colour value from each row
					datum = (short) m;

					col = (((float) datum - (float) min) / ((float) (max - min)));
					image_writer.setColor(i, j, Color.color(col, col, col, 1.0));
				}
			} // column loop
		} // row loop

	}

	public void yMIP(WritableImage image) {
		int w = (int) image.getWidth(), h = (int) image.getHeight(), i, j, c, k;
		PixelWriter image_writer = image.getPixelWriter();

		float col;
		short datum;

		for (j = 0; j < h; j++) {
			for (i = 0; i < w; i++) {
				double m = 0.000001;

				for (k = 0; k < 255; k++) {
					m = Math.max(cthead[j][k][i], m);
					datum = (short) m;

					col = (((float) datum - (float) min) / ((float) (max - min)));
					image_writer.setColor(i, j, Color.color(col, col, col, 1.0));
				}

			}
		}
	}

	public void xMIP(WritableImage image) {
		int w = (int) image.getWidth(), h = (int) image.getHeight(), i, j, c, k;
		PixelWriter image_writer = image.getPixelWriter();

		float col;
		short datum;

		for (j = 0; j < h; j++) {
			for (i = 0; i < w; i++) {
				double m = 0.000001;

				for (k = 0; k < 255; k++) {
					m = Math.max(cthead[j][i][k], m);
					datum = (short) m;

					col = (((float) datum - (float) min) / ((float) (max - min)));
					image_writer.setColor(i, j, Color.color(col, col, col, 1.0));
				}
			}
		}
	}

	public static void main(String[] args) {
		launch();
	}

}
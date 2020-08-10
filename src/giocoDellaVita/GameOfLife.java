package giocoDellaVita;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

class Config extends Stage {
	int minSize = 50;
	int maxSize = 10000;
	int maxSeenSize = 100;
	int sizeX = -1;
	int sizeY = -1;
	TextField tSizeX = new TextField();
	TextField tSizeY = new TextField();

	public Config() {
		Label lConfig = new Label("Finestra Configurazione");
		Label lSizeX = new Label("Larghezza");
		Label lSizeY = new Label("Altezza");
		Label lSizeXInfo = new Label("50-10000");
		Label lSizeYInfo = new Label("50-10000");
		Button bImposta = new Button("Imposta");
		Button bAnnulla = new Button("Annulla");

		GridPane griglia = new GridPane();
		griglia.add(lConfig, 0, 0);
		griglia.add(lSizeX, 0, 1);
		griglia.add(lSizeY, 0, 2);
		griglia.add(tSizeX, 1, 1);
		griglia.add(tSizeY, 1, 2);
		griglia.add(lSizeXInfo, 2, 1);
		griglia.add(lSizeYInfo, 2, 2);
		griglia.add(bImposta, 1, 3);
		griglia.add(bAnnulla, 2, 3);

		Scene scene = new Scene(griglia);
		setTitle("Finestra configurazione");
		setScene(scene);

		bImposta.setOnAction(e -> imposta());
		bAnnulla.setOnAction(e -> annulla());
	}

	private void annulla() {
		close();
	}

	private void imposta() {
		sizeX = Integer.parseInt(tSizeX.getText());
		sizeY = Integer.parseInt(tSizeY.getText());
		if (sizeX < minSize || sizeX > maxSize || sizeY < minSize || sizeY > maxSize) {
			sizeX = -1;
			sizeY = -1;
			Alert allerta = new Alert(AlertType.ERROR, "grandezze non nel limite");
		} else {
			close();
		}

	}
}

public class GameOfLife extends Application {
	int fullWidth = 50, fullHeight = 30; // in celle
	int seenWidth = 50, seenHeight = 30;
	int cellSize = 8;
	int topLeftX = (fullWidth / 2) - (seenWidth / 2), topLeftY = (fullHeight / 2) - (seenHeight / 2);
	int incremento = 10;
	Rectangle rectArr[][]; 
	boolean colorArr[][]; 
	boolean tempColorArr[][]; 
	Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(0.02), // ogni quanto va chiamata la funzione
			x -> aggiornaTimer()));
	boolean state = false;
	Stage primaryStage;

	@Override
	public void start(Stage primaryStage) {
		Config finestraConfig = new Config();
		finestraConfig.showAndWait();
		System.out.println(finestraConfig.sizeX + " " + finestraConfig.sizeY);
		if(!(finestraConfig.sizeX == -1 || finestraConfig.sizeY == -1)) {
			fullWidth = finestraConfig.sizeX;
			fullHeight = finestraConfig.sizeY;
		}
		rectArr  = new Rectangle[seenWidth][seenHeight];
		colorArr = new boolean[fullWidth][fullHeight];
		tempColorArr = new boolean[fullWidth][fullHeight];	
		Button bInizia = new Button("INIZIA/FERMA");
		Button bPulisci = new Button("PULISCI");
		Button bSalva = new Button("Salva");
		Button bCarica = new Button("Carica");
		
		GridPane ui = new GridPane();
		GridPane griglia = new GridPane();
		griglia.setVgap(1);
		griglia.setHgap(1);
		ui.add(griglia, 0, 0, 10, 1);
		for (int x = 0; x < fullWidth; x++) {
			for (int y = 0; y < fullHeight; y++) {

				colorArr[x][y] = false;
				tempColorArr[x][y] = false;

			}

		}
		for (int x = 0; x < seenWidth; x++) {
			for (int y = 0; y < seenHeight; y++) {
				rectArr[x][y] = new Rectangle();
				rectArr[x][y].setWidth(cellSize);
				rectArr[x][y].setHeight(cellSize);
				rectArr[x][y].setFill(Color.gray(0.9));
				griglia.add(rectArr[x][y], x, y);
			}
		}
		ui.add(bInizia, 0, 1);
		ui.add(bPulisci, 1, 1);
		ui.add(bSalva, 2, 1);
		ui.add(bCarica, 3, 1);
		

		bInizia.setOnAction(e -> inizia());
		bPulisci.setOnAction(e -> pulisci());
		bSalva.setOnAction(e -> {
			try {
				salva();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		});
		bCarica.setOnAction(e -> carica());
		
		Scene scene = new Scene(ui);
		scene.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> cambioStato(e));
		primaryStage.setTitle("Game of Life");
		primaryStage.setScene(scene);
		primaryStage.show();
		scene.setOnKeyPressed(e -> pigiato(e));
		timeline.setCycleCount(timeline.INDEFINITE);

	}

	private void config() {
		timeline.stop();
		
		
	}

	private void carica() {
		timeline.stop();
	}

	private void salva() throws IOException {
		timeline.stop();

		char[][] content = new char[fullHeight][fullWidth];
		for (int col = 0; col < fullWidth; col++) {
			for (int row = 0; row < fullHeight; row++) {
				System.out.println("col:" + col + "  row:" + row);
				if (colorArr[col][row]) {

					content[row][col] = '1';
				} else {
					content[row][col] = '0';
				}

			}
		}

		for (int row = 0; row < fullHeight; row++) {
			System.out.println(content[row]);
		}

		FileChooser fileChooser = new FileChooser();
		File file = fileChooser.showSaveDialog(primaryStage);
		FileWriter scrivi = new FileWriter(file);
		scrivi.write("" + fullWidth + " " + fullHeight + "\n");
		for (int row = 0; row < fullHeight; row++) {
			scrivi.write(content[row]);
			scrivi.write("\n");
		}
		scrivi.close();
	}

	private void pigiato(KeyEvent e) {
		if (e.getCode() == KeyCode.W) {
			if (topLeftY >= incremento) {
				topLeftY -= incremento;
			}
		}
		if (e.getCode() == KeyCode.A) {
			if (topLeftX >= incremento) {
				topLeftX -= incremento;
			}
		}
		if (e.getCode() == KeyCode.D) {
			if (topLeftX + seenWidth <= fullWidth - incremento) {
				topLeftX += incremento;
			}
		}
		if (e.getCode() == KeyCode.S) {
			if (topLeftY + seenHeight <= fullHeight - incremento) {
				topLeftY += incremento;
			}
		}
		System.out.println(topLeftX + " " + topLeftY);
	}

	private void pulisci() {
		for (int x = 0; x < fullWidth; x++) {
			for (int y = 0; y < fullHeight; y++) {

				colorArr[x][y] = false;
				tempColorArr[x][y] = false;
			}
		}
	}

	private void inizia() {
		if (state) {
			timeline.stop();
			state = !state;
		} else {
			timeline.play();
			state = !state;
		}
	}

	private void aggiornaTimer() {
		for (int x = 0; x < fullWidth; x++) {
			for (int y = 0; y < fullHeight; y++) {
				int neighborCount = 0;

				if (colorArr[(x - 1 + fullWidth) % fullWidth][(y - 1 + fullHeight) % fullHeight]) {
					neighborCount++;
				}
				if (colorArr[x % fullWidth][(y - 1 + fullHeight) % fullHeight]) {
					neighborCount++;
				}
				if (colorArr[(x + 1 + fullWidth) % fullWidth][(y - 1 + fullHeight) % fullHeight]) {
					neighborCount++;
				}
				if (colorArr[(x + 1 + fullWidth) % fullWidth][y % fullHeight]) {
					neighborCount++;
				}
				if (colorArr[(x + 1 + fullWidth) % fullWidth][(y + 1 + fullHeight) % fullHeight]) {
					neighborCount++;
				}
				if (colorArr[x % fullWidth][(y + 1 + fullHeight) % fullHeight]) {
					neighborCount++;
				}
				if (colorArr[(x - 1 + fullWidth) % fullWidth][(y + 1 + fullHeight) % fullHeight]) {
					neighborCount++;
				}
				if (colorArr[(x - 1 + fullWidth) % fullWidth][y % fullHeight]) {
					neighborCount++;
				}

				if (colorArr[x][y]) { // se cella viva
					if (neighborCount < 2) { // morte per isolamento :(
						tempColorArr[x][y] = false;

					} else if (neighborCount > 3) {// morte per sovrappopolazione
						tempColorArr[x][y] = false;
					} else {
						tempColorArr[x][y] = true;
					}
				} else { // se cella morta
					if (neighborCount == 3) {
						tempColorArr[x][y] = true;
					}
				}
			}
		}
		for (int x = 0; x < fullWidth; x++) {
			for (int y = 0; y < fullHeight; y++) {
				colorArr[x][y] = tempColorArr[x][y];
				tempColorArr[x][y] = false;
			}
		}

		for (int x = topLeftX; x < topLeftX + seenWidth; x++) {
			for (int y = topLeftY; y < topLeftY + seenHeight; y++) {
				if (colorArr[x][y]) {
					rectArr[x - topLeftX][y - topLeftY].setFill(Color.gray(0));
				} else {
					rectArr[x - topLeftX][y - topLeftY].setFill(Color.gray(0.9));
				}
			}
		}

	}

	private void cambioStato(MouseEvent e) {

		int x = ((int) ((int) e.getSceneX() / (cellSize + 1))) + topLeftX;
		int y = ((int) ((int) e.getSceneY() / (cellSize + 1))) + topLeftY;
		if (colorArr[x][y]) {
			rectArr[x - topLeftX][y - topLeftY].setFill(Color.gray(0.9));
			colorArr[x][y] = !colorArr[x][y];
		} else {
			rectArr[x - topLeftX][y - topLeftY].setFill(Color.gray(0));
			colorArr[x][y] = !colorArr[x][y];
		}
	}

	public static void main(String[] args) {
		launch(args);
	}

}

package com.example.floodfill.controller;

import com.example.floodfill.model.FloodFill;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class MainController {

    @FXML private ImageView imageView;
    @FXML private ColorPicker colorPicker;
    @FXML private ChoiceBox<String> structureChoiceBox;
    @FXML private Label statusLabel;
    @FXML private Button loadImageButton;
    @FXML private Button startButton;

    private BufferedImage originalImage;
    private BufferedImage currentImage;
    private int startX = -1, startY = -1;
    private File outputDir;
    private int frameCount;

    @FXML
    public void initialize() {
        colorPicker.setValue(Color.RED);
        structureChoiceBox.setItems(FXCollections.observableArrayList("Pilha", "Fila"));
        structureChoiceBox.setValue("Pilha");
        startButton.setDisable(true);
    }

    @FXML
    private void onLoadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Abrir Imagem");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imagens PNG", "*.png"));
        File file = fileChooser.showOpenDialog(imageView.getScene().getWindow());

        if (file != null) {
            try {
                originalImage = ImageIO.read(file);
                resetImage();
                statusLabel.setText("Imagem carregada. Clique na imagem para definir o ponto inicial.");
                startX = -1;
                startY = -1;
            } catch (IOException e) {
                statusLabel.setText("Erro ao carregar a imagem.");
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void onImageClicked(MouseEvent event) {
        if (currentImage == null) return;

        // Converte as coordenadas do clique para as coordenadas da imagem
        double clickX = event.getX();
        double clickY = event.getY();

        double viewWidth = imageView.getFitWidth();
        double viewHeight = imageView.getFitHeight();
        
        double imageWidth = currentImage.getWidth();
        double imageHeight = currentImage.getHeight();
        
        double scaleX = viewWidth / imageWidth;
        double scaleY = viewHeight / imageHeight;

        // Usa a menor escala para manter a proporção
        double scale = Math.min(scaleX, scaleY);
        
        double scaledWidth = imageWidth * scale;
        double scaledHeight = imageHeight * scale;
        
        // Calcula o offset para centralizar a imagem
        double offsetX = (viewWidth - scaledWidth) / 2;
        double offsetY = (viewHeight - scaledHeight) / 2;

        if (clickX >= offsetX && clickX <= offsetX + scaledWidth &&
            clickY >= offsetY && clickY <= offsetY + scaledHeight) {
            
            startX = (int) ((clickX - offsetX) / scale);
            startY = (int) ((clickY - offsetY) / scale);

            statusLabel.setText(String.format("Ponto inicial definido em: (%d, %d). Pronto para iniciar.", startX, startY));
            startButton.setDisable(false);
        }
    }

    @FXML
    private void onStartFloodFill() {
        if (currentImage == null || startX == -1) {
            statusLabel.setText("Carregue uma imagem e selecione um ponto inicial.");
            return;
        }

        prepareOutputDirectory();
        setControlsDisabled(true);
        frameCount = 0;

        Color newColor = colorPicker.getValue();
        String selectedStructure = structureChoiceBox.getValue();
        
        FloodFill floodFill = new FloodFill();

        Task<Void> fillTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                // A função de callback que atualiza a UI e salva o frame
                java.util.function.Consumer<Image> frameUpdater = (frame) -> {
                    Platform.runLater(() -> imageView.setImage(frame));
                    saveFrame(frame);
                    // REMOVIDO: O Thread.sleep(10) foi retirado para máxima performance.
                };

                if ("Pilha".equals(selectedStructure)) {
                    floodFill.fillWithStack(currentImage, startX, startY, newColor, frameUpdater);
                } else {
                    floodFill.fillWithQueue(currentImage, startX, startY, newColor, frameUpdater);
                }
                return null;
            }
        };

        fillTask.setOnSucceeded(event -> {
            statusLabel.setText("Preenchimento concluído! Imagens salvas na pasta 'output_frames'.");
            setControlsDisabled(false);
        });

        fillTask.setOnFailed(event -> {
            statusLabel.setText("Ocorreu um erro durante o preenchimento.");
            fillTask.getException().printStackTrace();
            setControlsDisabled(false);
        });

        new Thread(fillTask).start();
    }

    private void prepareOutputDirectory() {
        outputDir = new File("output_frames");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        } else {
            // Limpa a pasta para uma nova animação
            File[] files = outputDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    file.delete();
                }
            }
        }
    }
    
    private void saveFrame(Image frame) {
        try {
            BufferedImage bImage = SwingFXUtils.fromFXImage(frame, null);
            String fileName = String.format("frame_%05d.png", frameCount++);
            File outputFile = new File(outputDir, fileName);
            ImageIO.write(bImage, "png", outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setControlsDisabled(boolean disabled) {
        loadImageButton.setDisable(disabled);
        startButton.setDisable(disabled);
        colorPicker.setDisable(disabled);
        structureChoiceBox.setDisable(disabled);
    }
    
    private void resetImage() {
        currentImage = new BufferedImage(
                originalImage.getColorModel(),
                originalImage.copyData(null),
                originalImage.isAlphaPremultiplied(),
                null);
        Image fxImage = SwingFXUtils.toFXImage(currentImage, null);
        imageView.setImage(fxImage);
    }
}
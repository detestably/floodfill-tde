package com.example.floodfill.model;

import com.example.floodfill.model.structures.CustomQueue;
import com.example.floodfill.model.structures.CustomStack;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

public class FloodFill {

    public void fillWithStack(BufferedImage image, int startX, int startY, Color newColor, Consumer<Image> onFrameReady) {
        int width = image.getWidth();
        int height = image.getHeight();
        int targetColorRGB = image.getRGB(startX, startY);
        int newColorRGB = toAwtColor(newColor).getRGB();

        if (targetColorRGB == newColorRGB) return;

        CustomStack stack = new CustomStack();
        stack.push(new Pixel(startX, startY));

        // NOVO: Contador para processamento em lotes
        int pixelCounter = 0;
        final int BATCH_SIZE = 500; // Atualiza a cada 500 pixels

        while (!stack.isEmpty()) {
            Pixel current = stack.pop();
            int x = current.getX();
            int y = current.getY();

            if (x < 0 || x >= width || y < 0 || y >= height || image.getRGB(x, y) != targetColorRGB) {
                continue;
            }

            image.setRGB(x, y, newColorRGB);
            pixelCounter++;

            // ALTERADO: Atualiza a tela e salva o frame apenas a cada BATCH_SIZE pixels
            if (pixelCounter % BATCH_SIZE == 0) {
                onFrameReady.accept(convertToFxImage(image));
            }

            stack.push(new Pixel(x + 1, y));
            stack.push(new Pixel(x - 1, y));
            stack.push(new Pixel(x, y + 1));
            stack.push(new Pixel(x, y - 1));
        }
        // Garante que o último frame seja sempre exibido e salvo
        onFrameReady.accept(convertToFxImage(image));
    }

    public void fillWithQueue(BufferedImage image, int startX, int startY, Color newColor, Consumer<Image> onFrameReady) {
        int width = image.getWidth();
        int height = image.getHeight();
        int targetColorRGB = image.getRGB(startX, startY);
        int newColorRGB = toAwtColor(newColor).getRGB();

        if (targetColorRGB == newColorRGB) return;

        CustomQueue queue = new CustomQueue();
        queue.enqueue(new Pixel(startX, startY));

        // NOVO: Contador para processamento em lotes
        int pixelCounter = 0;
        final int BATCH_SIZE = 500; // Atualiza a cada 500 pixels

        while (!queue.isEmpty()) {
            Pixel current = queue.dequeue();
            int x = current.getX();
            int y = current.getY();

            if (x < 0 || x >= width || y < 0 || y >= height || image.getRGB(x, y) != targetColorRGB) {
                continue;
            }

            image.setRGB(x, y, newColorRGB);
            pixelCounter++;

            // ALTERADO: Atualiza a tela e salva o frame apenas a cada BATCH_SIZE pixels
            if (pixelCounter % BATCH_SIZE == 0) {
                onFrameReady.accept(convertToFxImage(image));
            }

            queue.enqueue(new Pixel(x + 1, y));
            queue.enqueue(new Pixel(x - 1, y));
            queue.enqueue(new Pixel(x, y + 1));
            queue.enqueue(new Pixel(x, y - 1));
        }
        // Garante que o último frame seja sempre exibido e salvo
        onFrameReady.accept(convertToFxImage(image));
    }

    private java.awt.Color toAwtColor(Color fxColor) {
        return new java.awt.Color((float) fxColor.getRed(),
                                   (float) fxColor.getGreen(),
                                   (float) fxColor.getBlue(),
                                   (float) fxColor.getOpacity());
    }

    private Image convertToFxImage(BufferedImage awtImage) {
        WritableImage writableImage = new WritableImage(awtImage.getWidth(), awtImage.getHeight());
        return SwingFXUtils.toFXImage(awtImage, writableImage);
    }
}
package ru.sagenotes.ocrservice.util;

import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@Slf4j
public class ConvertFileFormat {

    public static File convertToCommonFormat(File imageFile) {
        try {
            BufferedImage originalImage = ImageIO.read(imageFile);
            if (originalImage == null) {
                log.error("Failed to read the image file: {}", imageFile.getAbsolutePath());
                return null;
            }

            File convertedFile = new File(imageFile.getParent(),
                    imageFile.getName().substring(0, imageFile.getName().lastIndexOf('.')) + ".png");

            ImageIO.write(originalImage, "png", convertedFile);
            return convertedFile;
        } catch (IOException e) {
            log.error("IOException during image conversion: {}", e.getMessage(), e);
            return null;
        }
    }
}

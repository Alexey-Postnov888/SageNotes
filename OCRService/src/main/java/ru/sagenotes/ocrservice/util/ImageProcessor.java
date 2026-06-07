package ru.sagenotes.ocrservice.util;

import lombok.extern.slf4j.Slf4j;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_imgproc.CLAHE;

import java.io.File;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgcodecs.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

@Slf4j
public class ImageProcessor {

    public enum ImageType {
        PARAGRAPH_TEXT,
        SHORT_TEXT,
        MIXED
    }

    public record ImageProcessingResult(File file, ImageType imageType) {
    }

    public ImageProcessingResult ImageMake(File file) {
        Mat src = imread(file.getAbsolutePath());

        if (src.empty()) {
            log.error("Не удалось загрузить изображение!");
            return new ImageProcessingResult(file, ImageType.MIXED);
        }

        double scale = 1.0;
        Mat scaledSrc = new Mat();
        if (src.cols() < 1600 || src.rows() < 1200) {
            scale = Math.max(2.0, 2000.0 / Math.max(src.cols(), src.rows()));
            resize(src, scaledSrc, new Size((int)(src.cols() * scale), (int)(src.rows() * scale)), 0, 0, INTER_CUBIC);
        } else {
            scaledSrc = src.clone();
        }

        Mat srcCorrected = scaledSrc.clone();
        try {
            SkewProcessor skewProcessor = new SkewProcessor();
            srcCorrected = skewProcessor.correctSkewMethod(scaledSrc);
        } catch (Exception e) {
            log.warn("Deskew пропущен или не сработал: {}", e.getMessage());
        }

        Mat processed = processForOCR(srcCorrected, scale);

        ImageType type = detectImageType(processed, scale);

        String originalName = file.getName();
        String outputName = originalName.contains(".")
                ? "processed_" + originalName.substring(0, originalName.lastIndexOf(".")) + ".png"
                : "processed_" + originalName + ".png";

        File outputFile = new File(file.getParent(), outputName);
        imwrite(outputFile.getAbsolutePath(), processed);
        file.delete();

        src.close();
        scaledSrc.close();
        srcCorrected.close();
        processed.close();

        return new ImageProcessingResult(outputFile, type);
    }

    private Mat processForOCR(Mat src, double scale) {
        Mat gray = new Mat();
        Mat contrast = new Mat();
        Mat blurred = new Mat();
        Mat binary = new Mat();

        if (src.channels() == 3) {
            cvtColor(src, gray, CV_BGR2GRAY);
        } else {
            gray = src.clone();
        }

        CLAHE clahe = createCLAHE(3.0, new Size(12, 12));
        clahe.apply(gray, contrast);
        clahe.close();

        GaussianBlur(contrast, blurred, new Size(3, 3), 0);

        threshold(blurred, binary, 0, 255, CV_THRESH_BINARY | CV_THRESH_OTSU);

        if (isPageBackgroundDark(binary)) {
            bitwise_not(binary, binary);
        }

        Mat invNoise = new Mat();
        bitwise_not(binary, invNoise);

        MatVector noiseContours = new MatVector();
        findContours(invNoise, noiseContours, CV_RETR_EXTERNAL, CV_CHAIN_APPROX_SIMPLE);

        Scalar eraseColor = new Scalar(0);
        long totalContours = noiseContours.size();

        double minArea = 12.0 * scale * scale;
        double minDim = 3.0 * scale;

        for (long i = 0; i < totalContours; i++) {
            Mat contour = noiseContours.get(i);
            double area = contourArea(contour);
            Rect rect = boundingRect(contour);

            if (area < minArea || (rect.width() <= minDim && rect.height() <= minDim)) {
                rectangle(invNoise, rect, eraseColor, -1, 8, 0);
            }
            contour.close();
        }

        bitwise_not(invNoise, binary);

        invNoise.close();
        noiseContours.close();
        eraseColor.close();
        Mat thickKernel = getStructuringElement(MORPH_RECT, new Size(2, 2));
        erode(binary, binary, thickKernel);
        thickKernel.close();

        Mat closeKernel = getStructuringElement(MORPH_RECT, new Size(1, 1));
        morphologyEx(binary, binary, MORPH_CLOSE, closeKernel);
        closeKernel.close();

        gray.close();
        contrast.close();
        blurred.close();

        return binary;
    }

    private boolean isPageBackgroundDark(Mat binary) {
        int w = binary.cols();
        int h = binary.rows();

        long darkPixels = 0;
        long totalEdgePixels = 0;

        int inset = 10;
        if (w <= inset * 2 || h <= inset * 2) return false;

        for (int x = 0; x < w; x += 2) {
            if ((binary.ptr(inset, x).get() & 0xFF) < 128) darkPixels++;
            if ((binary.ptr(h - inset, x).get() & 0xFF) < 128) darkPixels++;
            totalEdgePixels += 2;
        }
        for (int y = inset; y < h - inset; y += 2) {
            if ((binary.ptr(y, inset).get() & 0xFF) < 128) darkPixels++;
            if ((binary.ptr(y, w - inset).get() & 0xFF) < 128) darkPixels++;
            totalEdgePixels += 2;
        }

        return ((double) darkPixels / totalEdgePixels) > 0.5;
    }

    private ImageType detectImageType(Mat cleanBinary, double scale) {
        Mat inv = new Mat();
        bitwise_not(cleanBinary, inv);

        MatVector contours = new MatVector();
        findContours(inv, contours, CV_RETR_EXTERNAL, CV_CHAIN_APPROX_SIMPLE);

        int validTextBlocks = 0;

        double minHeight = 6.0 * scale;
        double minWidth = 3.0 * scale;
        double maxHeight = 120.0 * scale;

        for (int i = 0; i < contours.size(); i++) {
            Mat contour = contours.get(i);
            Rect rect = boundingRect(contour);

            if (rect.height() > minHeight && rect.width() > minWidth && rect.height() < maxHeight) {
                double aspect = (double) rect.width() / rect.height();
                if (aspect > 0.15 && aspect < 8.0) {
                    if (rect.width() < cleanBinary.cols() * 0.8 && rect.height() < cleanBinary.rows() * 0.8) {
                        validTextBlocks++;
                    }
                }
            }
            contour.close();
        }

        inv.close();
        contours.close();

        if (scale > 3.5) {
            return validTextBlocks <= 5 ? ImageType.SHORT_TEXT : ImageType.MIXED;
        }

        if (validTextBlocks <= 15) {
            return ImageType.SHORT_TEXT;
        } else if (validTextBlocks > 65) {
            return ImageType.PARAGRAPH_TEXT;
        } else {
            return ImageType.MIXED;
        }
    }
}
package ru.sagenotes.ocrservice.util;

import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_imgproc.Vec2fVector;
import org.bytedeco.opencv.opencv_imgproc.Vec4iVector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.bytedeco.opencv.global.opencv_core.BORDER_CONSTANT;
import static org.bytedeco.opencv.global.opencv_imgproc.*;
import static org.bytedeco.opencv.global.opencv_imgproc.COLOR_BGR2GRAY;
import static org.bytedeco.opencv.global.opencv_imgproc.CV_CHAIN_APPROX_SIMPLE;
import static org.bytedeco.opencv.global.opencv_imgproc.CV_MOP_DILATE;
import static org.bytedeco.opencv.global.opencv_imgproc.CV_RETR_EXTERNAL;
import static org.bytedeco.opencv.global.opencv_imgproc.CV_SHAPE_RECT;
import static org.bytedeco.opencv.global.opencv_imgproc.CV_THRESH_BINARY_INV;
import static org.bytedeco.opencv.global.opencv_imgproc.CV_THRESH_OTSU;
import static org.bytedeco.opencv.global.opencv_imgproc.Canny;
import static org.bytedeco.opencv.global.opencv_imgproc.HoughLines;
import static org.bytedeco.opencv.global.opencv_imgproc.HoughLinesP;
import static org.bytedeco.opencv.global.opencv_imgproc.INTER_LINEAR;
import static org.bytedeco.opencv.global.opencv_imgproc.contourArea;
import static org.bytedeco.opencv.global.opencv_imgproc.cvtColor;
import static org.bytedeco.opencv.global.opencv_imgproc.findContours;
import static org.bytedeco.opencv.global.opencv_imgproc.getRotationMatrix2D;
import static org.bytedeco.opencv.global.opencv_imgproc.getStructuringElement;
import static org.bytedeco.opencv.global.opencv_imgproc.minAreaRect;
import static org.bytedeco.opencv.global.opencv_imgproc.morphologyEx;
import static org.bytedeco.opencv.global.opencv_imgproc.warpAffine;

public class SkewProcessor {

    public Mat correctSkewMethod(Mat src) {
        double angle1 = detectSkewAngle(src);

        double angle2 = detectSkewAngleHough(src);

        double angle3 = detectSkewAngleHoughProbabilistic(src);

        java.util.ArrayList<Double> validAngles = new java.util.ArrayList<>();

        if (Math.abs(angle1) > 0.5) validAngles.add(angle1);
        if (Math.abs(angle2) > 0.5) validAngles.add(angle2);
        if (Math.abs(angle3) > 0.5) validAngles.add(angle3);

        if (validAngles.isEmpty()) {
            return src.clone();
        }

        java.util.Collections.sort(validAngles);
        double finalAngle = validAngles.get(validAngles.size() / 2);

        if (Math.abs(finalAngle) < 0.5) {
            return src.clone();
        }

        Point2f center = new Point2f(src.cols() / 2f, src.rows() / 2f);
        Mat rotationMatrix = getRotationMatrix2D(center, finalAngle, 1.0);
        Mat rotated = new Mat();
        warpAffine(src, rotated, rotationMatrix, src.size(),
                INTER_LINEAR, BORDER_CONSTANT, new Scalar(255, 255, 255, 255));

        rotationMatrix.close();

        return rotated;
    }

    private double detectSkewAngle(Mat src) {
        Mat gray = new Mat();
        if (src.channels() == 3) {
            cvtColor(src, gray, CV_BGR2GRAY);
        } else {
            gray = src.clone();
        }

        Mat binary = new Mat();
        threshold(gray, binary, 0, 255, CV_THRESH_BINARY_INV | CV_THRESH_OTSU);

        Mat kernel = getStructuringElement(CV_SHAPE_RECT, new Size(3, 3));
        Mat dilated = new Mat();
        morphologyEx(binary, dilated, CV_MOP_DILATE, kernel);

        MatVector contours = new MatVector();
        findContours(dilated, contours, CV_RETR_EXTERNAL, CV_CHAIN_APPROX_SIMPLE);

        java.util.ArrayList<Double> angleList = new java.util.ArrayList<>();

        for (int i = 0; i < contours.size(); i++) {
            Mat contour = contours.get(i);
            double area = contourArea(contour);

            if (area < 100) continue;

            RotatedRect rotatedRect = minAreaRect(contour);
            double angle = rotatedRect.angle();

            if (angle < -45) {
                angle += 90;
            } else if (angle > 45) {
                angle -= 90;
            }

            angleList.add(angle);
        }

        if (angleList.isEmpty()) {
            gray.close();
            binary.close();
            dilated.close();
            return 0.0;
        }

        java.util.Collections.sort(angleList);
        double medianAngle = angleList.get(angleList.size() / 2);

        gray.close();
        binary.close();
        dilated.close();

        return medianAngle;
    }

    private double detectSkewAngleHough(Mat src) {
        Mat gray = new Mat();
        if (src.channels() == 3) {
            cvtColor(src, gray, COLOR_BGR2GRAY);
        } else {
            gray = src.clone();
        }

        Mat edges = new Mat();
        Canny(gray, edges, 50.0, 200.0, 3, false);

        Vec2fVector lines = new Vec2fVector();
        HoughLines(edges, lines, 1.0, Math.PI / 180, 150, 0, 0, 0, Math.PI);

        if (lines.size() == 0) {
            edges.close();
            gray.close();
            lines.close();
            return 0.0;
        }

        List<Double> angleList = new ArrayList<>();

        for (int i = 0; i < lines.size(); i++) {
            Point2f lineData = lines.get(i);

            double theta = lineData.get(1);

            double angle = Math.toDegrees(theta);

            if (angle > 90) {
                angle = angle - 180;
            }

            if (Math.abs(angle) < 60 && Math.abs(angle) > 0.1) {
                angleList.add(angle);
            }
        }

        if (angleList.isEmpty()) {
            edges.close();
            gray.close();
            lines.close();
            return 0.0;
        }

        Collections.sort(angleList);
        double medianAngle;
        int size = angleList.size();
        if (size % 2 == 0) {
            medianAngle = (angleList.get(size / 2 - 1) + angleList.get(size / 2)) / 2.0;
        } else {
            medianAngle = angleList.get(size / 2);
        }

        edges.close();
        gray.close();
        lines.close();

        return medianAngle;
    }

    private double detectSkewAngleHoughProbabilistic(Mat src) {
        Mat gray = new Mat();
        if (src.channels() == 3) {
            cvtColor(src, gray, COLOR_BGR2GRAY);
        } else {
            gray = src.clone();
        }

        Mat edges = new Mat();
        Canny(gray, edges, 50.0, 200.0, 3, false);

        Vec4iVector lines = new Vec4iVector();
        HoughLinesP(edges, lines, 1.0, Math.PI / 180, 80, 50, 10);

        if (lines.size() == 0) {
            edges.close();
            gray.close();
            lines.close();
            return 0.0;
        }

        List<Double> angleList = new ArrayList<>();

        for (int i = 0; i < lines.size(); i++) {
            Scalar4i lineData = lines.get(i);

            double x1 = lineData.get(0);
            double y1 = lineData.get(1);
            double x2 = lineData.get(2);
            double y2 = lineData.get(3);

            double dx = x2 - x1;
            double dy = y2 - y1;
            double angle = Math.toDegrees(Math.atan2(dy, dx));

            if (Math.abs(angle) > 90) {
                angle = angle - 180;
            }

            double length = Math.hypot(dx, dy);
            if (Math.abs(angle) < 60 && Math.abs(angle) > 0.5 && length > 50) {
                angleList.add(angle);
            }
        }

        if (angleList.isEmpty()) {
            edges.close();
            gray.close();
            lines.close();
            return 0.0;
        }

        Collections.sort(angleList);
        double medianAngle;
        int size = angleList.size();
        if (size % 2 == 0) {
            medianAngle = (angleList.get(size / 2 - 1) + angleList.get(size / 2)) / 2.0;
        } else {
            medianAngle = angleList.get(size / 2);
        }

        edges.close();
        gray.close();
        lines.close();

        return medianAngle;
    }
}

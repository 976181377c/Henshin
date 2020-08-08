package com.example.henshin;

import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class MyImage {

    public static void ImageDis(Mat mat) {

        Mat rectKernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2, 2));
        Imgproc.medianBlur(mat, mat, 1);
        Imgproc.threshold(mat, mat, 0, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
        Imgproc.erode(mat, mat, rectKernel);

        List<MatOfPoint> contours = new ArrayList<>();
        List<MatOfPoint> con = new ArrayList<>();

        Mat hierarchy = new Mat();
        Imgproc.findContours(mat, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
        int ic = 0;
        int parentIdx = -1;
        for (int i = hierarchy.cols() - 1; i >= 0; i--) {
            double[] h = hierarchy.get(0, i);
            if (h[2] == -1 && ic == 0 && h[3] != 1 && h[0] - h[1] == 0) {
                parentIdx = i;
                ic++;
            } else if (h[3] != -1 && parentIdx != -1 && h[0] - h[1] == 0) {
                ic++;
            } else if (h[3] == -1) {
                ic = 0;
                parentIdx = -1;
            }
            if (ic == 2) {
                MatOfPoint matOfPoint = contours.get(parentIdx);
                double S = Imgproc.contourArea(matOfPoint);
                MatOfPoint2f matOfPoint2f = new MatOfPoint2f(matOfPoint.toArray());
                RotatedRect rotatedRect = Imgproc.minAreaRect(matOfPoint2f);
                double ar = rotatedRect.size.height - rotatedRect.size.width;
                if (ar <= 1 && ar >= -1 && S > 100) {
                    Imgproc.boxPoints(rotatedRect, matOfPoint);
                    Mat matrix2D = Imgproc.getRotationMatrix2D(rotatedRect.center, rotatedRect.angle, 1);
                    Mat warp = new Mat();
                    Imgproc.warpAffine(mat, warp, matrix2D, new Size(mat.cols() * 2, mat.rows() * 2));
                    mat = warp;
                }
                ic = 0;
                parentIdx = -1;

            }

        }

    }

}

#include <jni.h>
#include <string>
#include <android/bitmap.h>
#include <opencv2/opencv.hpp>
#include <vector>
#include <iostream>

using namespace cv;
using std::vector;


extern "C" JNIEXPORT jstring JNICALL
Java_com_example_henshin_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
extern "C"
JNIEXPORT jint JNICALL
Java_com_example_henshin_Camera_getInt(JNIEnv *env, jobject instance, jlong mat_Addr) {

    Mat& gray = *(Mat*)mat_Addr;
    Mat rectKernel = getStructuringElement(MORPH_RECT, Size(3,3));
    medianBlur(gray, gray, 1);
    threshold(gray, gray, 0, 255, THRESH_BINARY | THRESH_OTSU);
//    morphologyEx(gray,gray,MORPH_DILATE,rectKernel);
    erode(gray, gray, rectKernel);
    //imshow("erode", gray);
    vector<vector<Point>> contours;
    vector<Vec4i> hierarchy;
    findContours(gray, contours, hierarchy, RETR_TREE, CHAIN_APPROX_SIMPLE);
    vector<RotatedRect> cont;
    int ic = 0;
    int parentIdx = -1;
    for (int i = hierarchy.size() - 1; i >= 0; i--) {
        Vec4i h = hierarchy[i];

        if (h[2] == -1 and ic == 0 and h[3] != 1 and h[0] - h[1] == 0) {
            parentIdx = i;
            ic++;
        } else if (h[3] != -1 and parentIdx != -1 and h[0] - h[1] == 0) {
            ic++;
        } else if (h[3] == -1) {
            ic = 0;
            parentIdx = -1;
        }
        if (ic == 2) {
            double S = contourArea(contours[parentIdx]);
            RotatedRect rota = minAreaRect(contours[parentIdx]);
            double ar = rota.size.width - rota.size.height;
            if (ar <= 1 && ar >= -1 && S > 100) {
                cont.push_back(rota);
            }
            ic = 0;
            parentIdx = -1;
        }
    }
    if (cont.size() == 1) {
        RotatedRect rota = cont[0];

        //移动至中心点

        Mat t_mat = Mat::zeros(2, 3, CV_32FC1);
        t_mat.at<float>(0, 0) = 1;
        t_mat.at<float>(0, 2) = gray.cols / 2 - rota.center.x;
        t_mat.at<float>(1, 1) = 1;
        t_mat.at<float>(1, 2) = gray.rows / 2 - rota.center.y;
        Mat warp;
        warpAffine(gray, warp, t_mat, Size(gray.cols, gray.rows));

        //旋转
        rota.center.x = warp.cols / 2;
        rota.center.y = warp.rows / 2;
        Mat rotm = getRotationMatrix2D(rota.center, rota.angle, 1);
        warpAffine(warp, warp, rotm, Size(gray.cols, gray.rows));
        vector<vector<Point>> point;

        //变换图形
        int w = rota.size.width / 2;
        rota.size.width *= 2;
        rota.size.height *= 10;

        //平移裁剪
        t_mat.at<float>(0, 2) = -(gray.cols / 2 - rota.size.width / 2);
        t_mat.at<float>(1, 2) = -(gray.rows / 2 - rota.size.height - w);

        warpAffine(warp, warp, t_mat, rota.size);

        threshold(warp, warp, 0, 255, THRESH_BINARY);

        int ncols = warp.cols;
        int nrows = warp.rows;
        vector<int> num;

        uchar *p = warp.data;
        int j = 0;
        int l = 0;

        //计算距离
        for( unsigned int i = 0; i < nrows; i++){
            int n = int(*(p+(i*ncols)+ncols/4));
            //cout << n << endl;
            if(n == j){
                j = 0 - (j - 255);//0,255翻转
                num.push_back(i - l);//计算距离
                l = i;
            }
        }
        //计算个数
        int num2[11] = {0};
        int n = num[1];
        int x = 0;
        int i = 0;
        j = 1;
        while(i<11){
            int S = int(float(num[j]) / n + 0.5);
            x = 0 - (x - 1);
            while (S>0){
                num2[i] = x;
                S--;
                i++;
                if(i>=11)
                    break;
            }
            j++;
        }
        int S=0;
        for (int i = 0;i<11;i++){
            S += num2[i] * exp2(i);
        }
        return S;
    }

    return 0;
    // TODO

}
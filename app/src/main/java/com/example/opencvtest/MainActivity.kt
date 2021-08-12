package com.example.opencvtest

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.provider.MediaStore
import android.view.*
import android.widget.TextView
import androidx.camera.core.ImageCapture
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.MotionEventCompat
import kotlinx.android.synthetic.main.activity_main.*
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.OpenCVLoader
import org.opencv.core.Core.*
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc



class MainActivity : AppCompatActivity()  {



    companion object {
        const val CAMERA_REQUEST_CODE = 1
        const val CAMERA_PERMISSION_REQUEST_CODE = 2
    }

    var scale = 0
    init{
        scale = 0
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)



        val version:TextView = findViewById(R.id.txtversion)
        if(OpenCVLoader.initDebug()){
            version.text = "OpenCV Version : " + OpenCVLoader.OPENCV_VERSION
            // println("print message" + version.text)
        } else {
            version.text = "OpenCV Version: Not found."
            return
        }
    }

    override fun onRestart() {
        super.onRestart()
    }
    override fun onResume() {
        super.onResume()
        if (checkCameraPermission()) {

        } else {
            grantCameraPermission()
        }

        // OpenCVライブラリ読み込み
        initCamera()

    }

    private fun checkCameraPermission() = PackageManager.PERMISSION_GRANTED ==
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.CAMERA)


    private fun grantCameraPermission() =
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST_CODE)


    private fun initCamera(){
        //リスナ設定
        camera_view.run {
            camera_view.setCvCameraViewListener(object: CameraBridgeViewBase.CvCameraViewListener2{
                override fun onCameraViewStarted(width:Int,height:Int){ }
                override fun onCameraViewStopped(){ }
                override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame?): Mat? {
                    //このメソッド内で画像処理(条件分岐)
                    var mat :Mat? = null
                    when(scale){
                        0 ->{ //RGBカラー
                            mat = inputFrame?.rgba()
                            textMode.text = "Mode : COLOR"
                        }
                        1->{ //Gray
                            mat = inputFrame?.gray()
                            textMode.text = "Mode : GRAY"
                        }
                        2->{ //RGBカラー + 反転
                            mat = inputFrame?.rgba()
                            bitwise_not(mat,mat)
                            textMode.text = "Mode : COLOR (REVERSE)"
                        }
                        3->{ //Gray + 反転
                            mat = inputFrame?.gray()
                            bitwise_not(mat,mat)
                            textMode.text = "Mode : GRAY(REVERSE)"
                        }
                        4->{ //二値化
                            mat = inputFrame?.rgba()
                            Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2GRAY)
                            Imgproc.threshold(mat, mat, 128.0, 255.0, Imgproc.THRESH_BINARY)  // 明るさが128を境に白と黒へ変換
                            textMode.text = "Mode : BINALIZATION"
                        }
                        5->{ //ガウシアンフィルタ
                            mat = inputFrame?.rgba()
                            Imgproc.GaussianBlur(mat,mat, Size(15.0,15.0),1.0,1.0)
                            textMode.text = "Mode : GAUSSIAN FILTER"
                        }
                        6->{ //メディアンフィルタ
                            mat = inputFrame?.rgba()
                            Imgproc.medianBlur(mat, mat, 41)
                            textMode.text = "Mode : MEDIAN FILTER"
                        }
                        7->{ //バイラテラルフィルタ
                            try{
                                mat = inputFrame?.rgba()
                                Imgproc.bilateralFilter(mat, mat, 3, 0.0, 0.0)
                                textMode.text = "Mode : BILATERAL FILTER"
                            } catch( e: Exception){
                            } finally{
                            }
                        }
                        8->{ //ボックスフィルタ
                            mat = inputFrame?.rgba()
                            if (mat != null) {
                                Imgproc.boxFilter(mat, mat, mat.depth(),  Size(41.0, 41.0))
                                textMode.text = "Mode : BOX FILTER"
                            }
                        }
                        else->{
                            inputFrame?.rgba()
                            textMode.text = "Mode : COLOR ( exception )"
                        }
                    }
                    return mat
                }
            })
            //プレビューを有効にする
            camera_view.setCameraPermissionGranted()
            camera_view.enableView()
        }
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        //オプションメニュー用xmlファイルをインフレクト
        menuInflater.inflate(R.menu.menu_options_menu_list,menu)
        //親クラスの同名メソッドを呼び出し、その戻り値を返却
        return super.onCreateOptionsMenu(menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            //Color を選択した場合
            R.id.menuListOptionColor ->
                scale = 0
            //Gray を選択した場合
            R.id.menuListOptionGray ->
                scale = 1
            //Color Bitwise を選択した場合
            R.id.menuListOptionColorBitwise ->
                scale = 2
            //Gray Bitwise を選択した場合
            R.id.menuListOptionGrayBitwise ->
                scale = 3
            //Binalization を選択した場合
            R.id.menuListOptionBinalization ->
                scale = 4
            //GaussianBlur を選択した場合
            R.id.menuListOptionGaussianBlur ->
                scale = 5
            //MedianBlur を選択した場合
            R.id.menuListOptionMedianBlur ->
                scale = 6
            //Bilateral Filter を選択した場合
            R.id.menuListOptionBilateralFilter ->
                scale = 7
            //Box Filter を選択した場合
            R.id.menuListOptionBoxFilter ->
                scale = 8
        }
        return super.onOptionsItemSelected(item)
    }
}
package org.chzz.qrcode.demo

import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Vibrator
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.View
import android.widget.Toast

import org.chzz.qrcode.core.QRCodeView
import org.chzz.qrcode.zxing.QRCodeDecoder
import org.chzz.qrcode.zxing.ZXingView

import java.io.File


class TestScanActivity : AppCompatActivity(), QRCodeView.Delegate {

    private var mQRCodeView: QRCodeView? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_scan)
        setSupportActionBar(findViewById(R.id.toolbar) as Toolbar)

        mQRCodeView = findViewById(R.id.zxingview) as ZXingView
        mQRCodeView!!.setDelegate(this)
    }

    override fun onStart() {
        super.onStart()
        mQRCodeView!!.startCamera()
    }

    override fun onStop() {
        mQRCodeView!!.stopCamera()
        super.onStop()
    }

    override fun onDestroy() {
        mQRCodeView!!.onDestroy()
        super.onDestroy()
    }

    private fun vibrate() {
        val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(200)
    }

    override fun onScanQRCodeSuccess(result: String) {
        Log.i(TAG, "result:" + result)
        Toast.makeText(this, result, Toast.LENGTH_SHORT).show()
        vibrate()
        mQRCodeView!!.startSpot()
    }

    override fun onScanQRCodeOpenCameraError() {
        Log.e(TAG, "打开相机出错")
    }

    fun onClick(v: View) {
        when (v.id) {
            R.id.start_spot -> mQRCodeView!!.startSpot()
            R.id.stop_spot -> mQRCodeView!!.stopSpot()
            R.id.start_spot_showrect -> mQRCodeView!!.startSpotAndShowRect()
            R.id.stop_spot_hiddenrect -> mQRCodeView!!.stopSpotAndHiddenRect()
            R.id.show_rect -> mQRCodeView!!.showScanRect()
            R.id.hidden_rect -> mQRCodeView!!.hiddenScanRect()
            R.id.start_preview -> mQRCodeView!!.startCamera()
            R.id.stop_preview -> mQRCodeView!!.stopCamera()
            R.id.open_flashlight -> mQRCodeView!!.openFlashlight()
            R.id.close_flashlight -> mQRCodeView!!.closeFlashlight()
            R.id.scan_barcode -> mQRCodeView!!.changeToScanBarcodeStyle()
            R.id.scan_qrcode -> mQRCodeView!!.changeToScanQRCodeStyle()
            R.id.choose_qrcde_from_gallery -> startActivityForResult(Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI), REQUEST_CODE_CHOOSE_QRCODE_FROM_GALLERY)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        mQRCodeView!!.showScanRect()

        if (requestCode == REQUEST_CODE_CHOOSE_QRCODE_FROM_GALLERY && resultCode == Activity.RESULT_OK && null != data) {
            var picturePath: String
            try {
                val selectedImage = data.data
                val filePathColumns = arrayOf(MediaStore.Images.Media.DATA)
                val c = contentResolver.query(selectedImage, filePathColumns, null, null, null)
                c!!.moveToFirst()
                val columnIndex = c.getColumnIndex(filePathColumns[0])
                picturePath = c.getString(columnIndex)
                c.close()
            } catch (e: Exception) {
                picturePath = data.data.path
            }

            if (File(picturePath).exists()) {
                QRCodeDecoder.decodeQRCode(BitmapFactory.decodeFile(picturePath), object : QRCodeDecoder.Delegate {
                    override fun onDecodeQRCodeSuccess(result: String) {
                        Toast.makeText(this@TestScanActivity, result, Toast.LENGTH_SHORT).show()
                    }

                    override fun onDecodeQRCodeFailure() {
                        Toast.makeText(this@TestScanActivity, "未发现二维码", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }
    }

    companion object {
        private val TAG = TestScanActivity::class.java.simpleName
        private val REQUEST_CODE_CHOOSE_QRCODE_FROM_GALLERY = 666
    }
}
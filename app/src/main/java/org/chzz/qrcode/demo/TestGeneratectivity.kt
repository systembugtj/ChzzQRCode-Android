package org.chzz.qrcode.demo

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.view.View
import android.widget.ImageView
import android.widget.Toast

import org.chzz.qrcode.core.CHZZQRCodeUtil
import org.chzz.qrcode.zxing.QRCodeDecoder
import org.chzz.qrcode.zxing.QRCodeEncoder


class TestGeneratectivity : AppCompatActivity() {
    private var mChineseIv: ImageView? = null
    private var mEnglishIv: ImageView? = null
    private var mChineseLogoIv: ImageView? = null
    private var mEnglishLogoIv: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_generate)
        setSupportActionBar(findViewById(R.id.toolbar) as Toolbar)

        initView()
        createQRCode()
    }

    private fun initView() {
        mChineseIv = findViewById(R.id.iv_chinese) as ImageView
        mChineseLogoIv = findViewById(R.id.iv_chinese_logo) as ImageView
        mEnglishIv = findViewById(R.id.iv_english) as ImageView
        mEnglishLogoIv = findViewById(R.id.iv_english_logo) as ImageView
    }

    private fun createQRCode() {
        createChineseQRCode()
        createEnglishQRCode()
        createChineseQRCodeWithLogo()
        createEnglishQRCodeWithLogo()
    }

    private fun createChineseQRCode() {
        QRCodeEncoder.encodeQRCode("二维码", CHZZQRCodeUtil.dp2px(this@TestGeneratectivity, 200f), object : QRCodeEncoder.Delegate {
            override fun onEncodeQRCodeSuccess(bitmap: Bitmap) {
                mChineseIv!!.setImageBitmap(bitmap)
            }

            override fun onEncodeQRCodeFailure() {
                Toast.makeText(this@TestGeneratectivity, "生成中文二维码失败", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun createEnglishQRCode() {
        QRCodeEncoder.encodeQRCode("chzz", CHZZQRCodeUtil.dp2px(this@TestGeneratectivity, 150f), Color.parseColor("#ff0000"), object : QRCodeEncoder.Delegate {
            override fun onEncodeQRCodeSuccess(bitmap: Bitmap) {
                mEnglishIv!!.setImageBitmap(bitmap)
            }

            override fun onEncodeQRCodeFailure() {
                Toast.makeText(this@TestGeneratectivity, "生成英文二维码失败", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun createChineseQRCodeWithLogo() {
        QRCodeEncoder.encodeQRCode("二维码", CHZZQRCodeUtil.dp2px(this@TestGeneratectivity, 150f), Color.parseColor("#795dbf"), BitmapFactory.decodeResource(this@TestGeneratectivity.resources, R.mipmap.logo), object : QRCodeEncoder.Delegate {
            override fun onEncodeQRCodeSuccess(bitmap: Bitmap) {
                mChineseLogoIv!!.setImageBitmap(bitmap)
            }

            override fun onEncodeQRCodeFailure() {
                Toast.makeText(this@TestGeneratectivity, "生成带logo的中文二维码失败", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun createEnglishQRCodeWithLogo() {
        QRCodeEncoder.encodeQRCode("chzz", CHZZQRCodeUtil.dp2px(this@TestGeneratectivity, 150f), Color.parseColor("#0000ff"), BitmapFactory.decodeResource(this@TestGeneratectivity.resources, R.mipmap.logo), object : QRCodeEncoder.Delegate {
            override fun onEncodeQRCodeSuccess(bitmap: Bitmap) {
                mEnglishLogoIv!!.setImageBitmap(bitmap)
            }

            override fun onEncodeQRCodeFailure() {
                Toast.makeText(this@TestGeneratectivity, "生成带logo的英文二维码失败", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun decodeChinese(v: View) {
        mChineseIv!!.isDrawingCacheEnabled = true
        val bitmap = mChineseIv!!.drawingCache
        decode(bitmap, "解析中文二维码失败")
    }

    fun decodeEnglish(v: View) {
        mEnglishIv!!.isDrawingCacheEnabled = true
        val bitmap = mEnglishIv!!.drawingCache
        decode(bitmap, "解析英文二维码失败")
    }

    fun decodeChineseWithLogo(v: View) {
        mChineseLogoIv!!.isDrawingCacheEnabled = true
        val bitmap = mChineseLogoIv!!.drawingCache
        decode(bitmap, "解析带logo的中文二维码失败")
    }

    fun decodeEnglishWithLogo(v: View) {
        mEnglishLogoIv!!.isDrawingCacheEnabled = true
        val bitmap = mEnglishLogoIv!!.drawingCache
        decode(bitmap, "解析带logo的英文二维码失败")
    }

    fun decodeIsbn(v: View) {
        val bitmap = BitmapFactory.decodeResource(resources, R.mipmap.test_isbn)
        decode(bitmap, "解析ISBN失败")
    }

    private fun decode(bitmap: Bitmap, errorTip: String) {
        QRCodeDecoder.decodeQRCode(bitmap, object : QRCodeDecoder.Delegate {
            override fun onDecodeQRCodeSuccess(result: String) {
                Toast.makeText(this@TestGeneratectivity, result, Toast.LENGTH_SHORT).show()
            }

            override fun onDecodeQRCodeFailure() {
                Toast.makeText(this@TestGeneratectivity, errorTip, Toast.LENGTH_SHORT).show()
            }
        })
    }
}
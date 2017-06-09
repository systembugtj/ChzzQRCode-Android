package org.chzz.qrcode.core

import android.content.Context
import android.graphics.Point
import android.hardware.Camera
import android.view.Display
import android.view.Surface
import android.view.WindowManager
import java.util.regex.Pattern

internal class CameraConfigurationManager(private val mContext: Context) {
    private var mScreenResolution: Point? = null
    private var cameraResolution: Point? = null

    fun initFromCameraParameters(camera: Camera) {
        val parameters = camera.parameters
        val manager = mContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = manager.defaultDisplay
        mScreenResolution = Point(display.width, display.height)
        val screenResolutionForCamera = Point()
        screenResolutionForCamera.x = mScreenResolution!!.x
        screenResolutionForCamera.y = mScreenResolution!!.y

        // preview size is always something like 480*320, other 320*480
        if (mScreenResolution!!.x < mScreenResolution!!.y) {
            screenResolutionForCamera.x = mScreenResolution!!.y
            screenResolutionForCamera.y = mScreenResolution!!.x
        }

        cameraResolution = getCameraResolution(parameters, screenResolutionForCamera)
    }

    fun setDesiredCameraParameters(camera: Camera) {
        val parameters = camera.parameters
        parameters.setPreviewSize(cameraResolution!!.x, cameraResolution!!.y)
        setZoom(parameters)

        camera.setDisplayOrientation(displayOrientation)
        camera.parameters = parameters
    }

    fun openFlashlight(camera: Camera) {
        doSetTorch(camera, true)
    }

    fun closeFlashlight(camera: Camera) {
        doSetTorch(camera, false)
    }

    private fun doSetTorch(camera: Camera, newSetting: Boolean) {
        val parameters = camera.parameters
        val flashMode: String?
        /** 是否支持闪光灯  */
        if (newSetting) {
            flashMode = findSettableValue(parameters.supportedFlashModes, Camera.Parameters.FLASH_MODE_TORCH, Camera.Parameters.FLASH_MODE_ON)
        } else {
            flashMode = findSettableValue(parameters.supportedFlashModes, Camera.Parameters.FLASH_MODE_OFF)
        }
        if (flashMode != null) {
            parameters.flashMode = flashMode
        }
        camera.parameters = parameters
    }

    val displayOrientation: Int
        get() {
            val info = Camera.CameraInfo()
            Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, info)
            val wm = mContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display = wm.defaultDisplay

            val rotation = display.rotation
            var degrees = 0
            when (rotation) {
                Surface.ROTATION_0 -> degrees = 0
                Surface.ROTATION_90 -> degrees = 90
                Surface.ROTATION_180 -> degrees = 180
                Surface.ROTATION_270 -> degrees = 270
            }

            var result: Int
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                result = (info.orientation + degrees) % 360
                result = (360 - result) % 360
            } else {
                result = (info.orientation - degrees + 360) % 360
            }
            return result
        }


    private fun setZoom(parameters: Camera.Parameters) {
        val zoomSupportedString = parameters.get("zoom-supported")
        if (zoomSupportedString != null && !java.lang.Boolean.parseBoolean(zoomSupportedString)) {
            return
        }

        var tenDesiredZoom = TEN_DESIRED_ZOOM

        val maxZoomString = parameters.get("max-zoom")
        if (maxZoomString != null) {
            try {
                val tenMaxZoom = (10.0 * java.lang.Double.parseDouble(maxZoomString)).toInt()
                if (tenDesiredZoom > tenMaxZoom) {
                    tenDesiredZoom = tenMaxZoom
                }
            } catch (nfe: NumberFormatException) {
            }

        }

        val takingPictureZoomMaxString = parameters.get("taking-picture-zoom-max")
        if (takingPictureZoomMaxString != null) {
            try {
                val tenMaxZoom = Integer.parseInt(takingPictureZoomMaxString)
                if (tenDesiredZoom > tenMaxZoom) {
                    tenDesiredZoom = tenMaxZoom
                }
            } catch (nfe: NumberFormatException) {
            }

        }

        val motZoomValuesString = parameters.get("mot-zoom-values")
        if (motZoomValuesString != null) {
            tenDesiredZoom = findBestMotZoomValue(motZoomValuesString, tenDesiredZoom)
        }

        val motZoomStepString = parameters.get("mot-zoom-step")
        if (motZoomStepString != null) {
            try {
                val motZoomStep = java.lang.Double.parseDouble(motZoomStepString.trim { it <= ' ' })
                val tenZoomStep = (10.0 * motZoomStep).toInt()
                if (tenZoomStep > 1) {
                    tenDesiredZoom -= tenDesiredZoom % tenZoomStep
                }
            } catch (nfe: NumberFormatException) {
                // continue
            }

        }
        if (maxZoomString != null || motZoomValuesString != null) {
            parameters.set("zoom", (tenDesiredZoom / 10.0).toString())
        }
        if (takingPictureZoomMaxString != null) {
            parameters.set("taking-picture-zoom", tenDesiredZoom)
        }
    }

    companion object {
        private val TEN_DESIRED_ZOOM = 27
        private val COMMA_PATTERN = Pattern.compile(",")

        private fun findSettableValue(supportedValues: Collection<String>?, vararg desiredValues: String): String {
            var result: String = ""
            if (supportedValues != null) {
                for (desiredValue in desiredValues) {
                    if (supportedValues.contains(desiredValue)) {
                        result = desiredValue
                        break
                    }
                }
            }
            return result
        }

        private fun getCameraResolution(parameters: Camera.Parameters, screenResolution: Point): Point {
            var previewSizeValueString: String? = parameters.get("preview-size-values")
            if (previewSizeValueString == null) {
                previewSizeValueString = parameters.get("preview-size-value")
            }
            var cameraResolution: Point? = null
            if (previewSizeValueString != null) {
                cameraResolution = findBestPreviewSizeValue(previewSizeValueString, screenResolution)
            }
            if (cameraResolution == null) {
                cameraResolution = Point(screenResolution.x shr 3 shl 3, screenResolution.y shr 3 shl 3)
            }
            return cameraResolution
        }

        private fun findBestPreviewSizeValue(previewSizeValueString: CharSequence, screenResolution: Point): Point? {
            var bestX = 0
            var bestY = 0
            var diff = Integer.MAX_VALUE
            for (rawPreviewSize in COMMA_PATTERN.split(previewSizeValueString)) {

                val previewSize = rawPreviewSize.trim { it <= ' ' }
                val dimPosition = previewSize.indexOf('x')
                if (dimPosition < 0) {
                    continue
                }

                val newX: Int
                val newY: Int
                try {
                    newX = Integer.parseInt(previewSize.substring(0, dimPosition))
                    newY = Integer.parseInt(previewSize.substring(dimPosition + 1))
                } catch (nfe: NumberFormatException) {
                    continue
                }

                val newDiff = Math.abs(newX - screenResolution.x) + Math.abs(newY - screenResolution.y)
                if (newDiff == 0) {
                    bestX = newX
                    bestY = newY
                    break
                } else if (newDiff < diff) {
                    bestX = newX
                    bestY = newY
                    diff = newDiff
                }

            }

            if (bestX > 0 && bestY > 0) {
                return Point(bestX, bestY)
            }
            return null
        }

        private fun findBestMotZoomValue(stringValues: CharSequence, tenDesiredZoom: Int): Int {
            var tenBestValue = 0
            for (rawStringValue in COMMA_PATTERN.split(stringValues)) {
                val stringValue = rawStringValue.trim { it <= ' ' }
                val value: Double
                try {
                    value = java.lang.Double.parseDouble(stringValue)
                } catch (nfe: NumberFormatException) {
                    return tenDesiredZoom
                }

                val tenValue = (10.0 * value).toInt()
                if (Math.abs(tenDesiredZoom - value) < Math.abs(tenDesiredZoom - tenBestValue)) {
                    tenBestValue = tenValue
                }
            }
            return tenBestValue
        }
    }

}
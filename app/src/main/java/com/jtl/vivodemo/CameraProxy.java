package com.jtl.vivodemo;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;
import android.util.Size;
import android.view.Surface;

import com.socks.library.KLog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * 作者:jtl
 * 日期:Created in 2019/3/18 21:23
 * 描述:
 * 更改:
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class CameraProxy {
    private static final String TAG = "CameraProxy";
    private boolean openSuccess;
    private HandlerThread mCameraThread;
    private CameraHandler mCameraHandler;
    private CameraManager mCameraManager;
    private String mCameraId = "0";
    private File mFile;
    private ImageReader mImageReader;
    private CameraDevice mCameraDevice;
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);
    /**
     * Orientation of the camera sensor
     */
    private int mSensorOrientation;
    private static final int MAX_PREVIEW_WIDTH = 1080;
    private static final int MAX_PREVIEW_HEIGHT = 2340;
    private boolean mFlashSupported;
    private CaptureRequest.Builder mPreviewRequestBuilder;
    private CameraCaptureSession mCaptureSession;
    private CaptureRequest mPreviewRequest;
    private ProxyListener mListener;

    private CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureStarted(@NonNull CameraCaptureSession session,
                                     @NonNull CaptureRequest request,
                                     long timestamp,
                                     long frameNumber) {
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                       @NonNull CaptureRequest request,
                                       @NonNull TotalCaptureResult result) {
        }
    };

    public boolean isOpenSuccess() {
        return openSuccess;
    }

    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            mCameraOpenCloseLock.release();
            mCameraDevice = camera;
            startPreview();
            KLog.e(TAG, "CameraDevice.StateCallback:onOpened");
            openSuccess = true;
        }

        @Override
        public void onClosed(@NonNull CameraDevice camera) {
            KLog.e(TAG, "CameraDevice.StateCallback:onClosed");
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            mCameraOpenCloseLock.release();
            camera.close();
            mCameraDevice = null;

            KLog.e(TAG, "CameraDevice.StateCallback:onDisconnected");
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            mCameraOpenCloseLock.release();
            camera.close();
            mCameraDevice = null;

            KLog.e(TAG, "CameraDevice.StateCallback:onError:" + error);
        }
    };
    /**
     * This a callback object for the {@link ImageReader}. "onImageAvailable" will be called when a
     * still image is ready to be saved
     */
    private ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            KLog.e(TAG, "onImageAvailable:reader:" + reader.getImageFormat() + ":" + reader.getWidth() + ":" + reader.getHeight() + "\n" + reader.acquireNextImage().getWidth() + "---" + reader.acquireNextImage().getHeight());
            // TODO: 2019/3/18 用于reader的保存回调
//            mCameraHandler.post(new ImageSaver(reader.acquireLatestImage(), mFile));
        }
    };

    public CameraProxy(Activity activity, ProxyListener listener, String cameraType) {
        mCameraId = cameraType;
        this.mListener = listener;
        mCameraManager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        mFile = new File(activity.getExternalFilesDir(null), "pic.jpg");
        startCameraThread();
    }

    public void OpenCamera(int width, int height) {
        Message message = mCameraHandler.obtainMessage();
        message.what = CameraValue.OPEN_CAMERA;
        message.arg1 = width;
        message.arg2 = height;
        mCameraHandler.sendMessage(message);
    }

    public void CloseCamera() {
        Message message = mCameraHandler.obtainMessage();
        message.what = CameraValue.CLOSE_CAMERA;
        mCameraHandler.sendMessage(message);
    }

    public void StartPreview() {
        Message message = mCameraHandler.obtainMessage();
        message.what = CameraValue.START_PREVIEW;
        mCameraHandler.sendMessage(message);
    }

    @RequiresPermission(Manifest.permission.CAMERA)
    private void openCamera(int width, int height) {
        setUpCameraOutputs(width, height);

        try {
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening");
            }
            mCameraManager.openCamera(mCameraId, mStateCallback, mCameraHandler);
        } catch (InterruptedException e) {
            KLog.e(TAG, e.toString());
        } catch (CameraAccessException e) {
            KLog.e(TAG, e.toString());
        }
    }

    private void setUpCameraOutputs(int width, int height) {
        try {
            for (String cameraId : mCameraManager.getCameraIdList()) {
                KLog.e(TAG, "cameraId:" + cameraId);
            }
            CameraCharacteristics cameraCharacteristics
                    = mCameraManager.getCameraCharacteristics(mCameraId);

            // We don't use a front facing camera in this sample.
            Integer facing = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
            if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                KLog.e(TAG, "facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT");
                return;
            }

            StreamConfigurationMap map = cameraCharacteristics.get(
                    CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            if (map == null) {
                KLog.e(TAG, "map==null");
                return;
            } else {
                Size[] size = map.getOutputSizes(ImageFormat.JPEG);
                for (Size size1 : size) {
                    KLog.e(TAG, "Size:" + size1.toString());
                }
            }

            Size largestJPEG = Collections.max(
                    Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),
                    new CompareSizesByArea());

            mImageReader = ImageReader.newInstance(width, height,
                    ImageFormat.JPEG, 2);
            KLog.e(TAG, "mImageReader:width:" + width + "---height:" + height);
            mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, null);
           /* //返回屏幕从自然方向的旋转
            int displayRotation = mActivity.getWindowManager().getDefaultDisplay().getRotation();
            //顺时针角度，输出图像需要通过该角度在原始方向上在设备屏幕上直立旋转的顺时针角度
            mSensorOrientation = cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            boolean swappedDimensions = false;
            switch (displayRotation) {
                case Surface.ROTATION_0:
                case Surface.ROTATION_180:
                    if (mSensorOrientation == 90 || mSensorOrientation == 270) {
                        swappedDimensions = true;
                    }
                    break;
                case Surface.ROTATION_90:
                case Surface.ROTATION_270:
                    if (mSensorOrientation == 0 || mSensorOrientation == 180) {
                        swappedDimensions = true;
                    }
                    break;
                default:
                    KLog.e(TAG, "Display rotation is invalid: " + displayRotation);
            }

            Point displaySize = new Point();
            mActivity.getWindowManager().getDefaultDisplay().getSize(displaySize);
            KLog.e("displaySize:"+displaySize.toString());
            int rotatedPreviewWidth = width;
            int rotatedPreviewHeight = height;
            int maxPreviewWidth = displaySize.x;
            int maxPreviewHeight = displaySize.y;

            if (swappedDimensions){
                rotatedPreviewWidth=height;
                rotatedPreviewHeight=width;
                maxPreviewWidth=displaySize.y;
                maxPreviewHeight=displaySize.x;
            }

            if (maxPreviewWidth>MAX_PREVIEW_WIDTH){
                maxPreviewWidth = MAX_PREVIEW_WIDTH;
            }
            if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) {
                maxPreviewHeight = MAX_PREVIEW_HEIGHT;
            }*/

            //check if the flash is supported
            Boolean available = cameraCharacteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
            mFlashSupported = available == null ? false : available;
        } catch (CameraAccessException e) {
            KLog.e(TAG, e.toString());
        }
    }

    private void startPreview() {
        try {
            KLog.e(TAG, "startPreview");
            //This is the output Surface we need to start preview
            Surface surface = mListener.getSurface();
            //We set up a CaptureRequest.Builder with the output Surface
            mPreviewRequestBuilder
                    = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

            mPreviewRequestBuilder.addTarget(surface);

            //create a CameraCaptureSession for Camera preview
            mCameraDevice.createCaptureSession(Arrays.asList(surface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            if (null == mCameraDevice) {
                                return;
                            }

                            mCaptureSession = session;
                            // Auto focus should be continuous for camera preview.
                            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

                            // Finally, we start displaying the camera preview.
                            mPreviewRequest = mPreviewRequestBuilder.build();
                            try {
                                mCaptureSession.setRepeatingRequest(mPreviewRequest,
                                        mCaptureCallback, mCameraHandler);
                            } catch (CameraAccessException e) {
                                KLog.e(TAG, e.toString());
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                            KLog.e(TAG, "onConfigureFailed");
                        }
                    }, null);
        } catch (CameraAccessException e) {
            KLog.e(TAG, e.toString());
        }
    }

    /**
     * Starts camera thread and its{@link Handler}
     */
    private void startCameraThread() {
        mCameraThread = new HandlerThread("CameraThread");
        mCameraThread.start();
        mCameraHandler = new CameraHandler(mCameraThread.getLooper());
    }

    private void closeCamera() {
        try {
            mCameraOpenCloseLock.acquire();
            if (mCaptureSession != null) {
                mCaptureSession.close();
                mCaptureSession = null;
            }

            if (mCameraDevice != null) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    /**
     * Stops the camera thread and its{@link Handler}
     */
    public void stopCameraThread() {
        mCameraThread.quitSafely();
        try {
            mCameraThread.join();
            mCameraThread = null;
            mCameraHandler = null;
            mListener = null;
        } catch (InterruptedException e) {
            KLog.e(TAG, e.toString());
        }
    }

    public @interface CameraValue {
        int OPEN_CAMERA = 0;
        int START_PREVIEW = 1;
        int CLOSE_CAMERA = 2;
    }

    /**
     * Compares two {@code Size}s based on their areas.
     */
    static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }
    }

    private static class ImageSaver implements Runnable {

        private final Image mImage;
        private final File mFile;

        ImageSaver(Image image, File file) {
            mImage = image;
            mFile = file;
        }

        @Override
        public void run() {
            ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            FileOutputStream output = null;
            try {
                output = new FileOutputStream(mFile);
                output.write(bytes);
            } catch (FileNotFoundException e) {
                KLog.e(TAG, e.toString());
            } catch (IOException e) {
                KLog.e(TAG, e.toString());
            } finally {
                mImage.close();
                if (null != output) {
                    try {
                        output.close();
                    } catch (IOException e) {
                        KLog.e(TAG, e.toString());
                    }
                }
            }
        }
    }

    public interface ProxyListener {
        void onError();

        Surface getSurface();

        int getOrientation(int sensor);

        void getDistance(int distance);

        void getIr(int ir);

        void getMode(int mode);

    }

    private class CameraHandler extends Handler {
        public CameraHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case CameraValue.OPEN_CAMERA:
                    openCamera(msg.arg1, msg.arg2);
                    break;
                case CameraValue.START_PREVIEW:
                    startPreview();
                    break;
                case CameraValue.CLOSE_CAMERA:
                    closeCamera();
                    break;
            }
        }
    }
}

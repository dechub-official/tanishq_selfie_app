package com.dechub.tanishq.service;

import lombok.extern.slf4j.Slf4j;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.*;
import org.bytedeco.opencv.opencv_core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgcodecs.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

/**
 * Service for adding watermark to videos using JavaCV
 * Overlays PNG watermark on video frames before S3 upload
 */
@Slf4j
@Service
public class VideoWatermarkService {

    @Value("${video.watermark.enabled:true}")
    private boolean watermarkEnabled;

    @Value("${video.watermark.image.path:classpath:static/qrwishes.png}")
    private String watermarkImagePath;

    @Value("${video.watermark.padding:30}")
    private int padding;

    @Value("${video.watermark.size.percent:15}")
    private int sizePercent;

    @Value("${video.watermark.opacity:0.75}")
    private double opacity;

    /**
     * Process video with watermark overlay
     *
     * @param videoFile Original video file from multipart upload
     * @param greetingId Greeting unique ID for temp file naming
     * @return File object of watermarked video
     * @throws IOException if processing fails
     */
    public File processVideoWithWatermark(MultipartFile videoFile, String greetingId) throws IOException {
        if (!watermarkEnabled) {
            log.info("Watermark disabled, returning original video");
            return convertMultipartToFile(videoFile);
        }

        log.info("Starting watermark processing for greeting: {}", greetingId);

        File inputFile = null;
        File outputFile = null;
        FFmpegFrameGrabber grabber = null;
        FFmpegFrameRecorder recorder = null;

        try {
            // Convert MultipartFile to temporary file
            inputFile = convertMultipartToFile(videoFile);
            log.debug("Input video saved to temp file: {}", inputFile.getAbsolutePath());

            // Create output file
            outputFile = createTempFile("watermarked_" + greetingId, getFileExtension(videoFile.getOriginalFilename()));
            log.debug("Output file path: {}", outputFile.getAbsolutePath());

            // Load watermark image
            Mat watermarkMat = loadWatermarkImage();
            if (watermarkMat == null) {
                log.warn("Failed to load watermark image, returning original video");
                return inputFile;
            }

            // Initialize frame grabber
            grabber = new FFmpegFrameGrabber(inputFile);
            grabber.start();

            int videoWidth = grabber.getImageWidth();
            int videoHeight = grabber.getImageHeight();
            double frameRate = grabber.getFrameRate();
            int audioChannels = grabber.getAudioChannels();

            log.info("Video properties - Width: {}, Height: {}, FPS: {}, Audio channels: {}",
                     videoWidth, videoHeight, frameRate, audioChannels);

            // Calculate watermark size
            int watermarkWidth = (int) (videoWidth * (sizePercent / 100.0));
            int watermarkHeight = (int) (watermarkMat.rows() * watermarkWidth / (double) watermarkMat.cols());

            // Calculate watermark position (bottom-center)
            int x = (videoWidth - watermarkWidth) / 2;
            int y = videoHeight - watermarkHeight - padding;

            log.info("Watermark position - X: {}, Y: {}, Width: {}, Height: {}", x, y, watermarkWidth, watermarkHeight);

            // Resize watermark
            Mat resizedWatermark = new Mat();
            resize(watermarkMat, resizedWatermark, new Size(watermarkWidth, watermarkHeight));

            // Initialize frame recorder with explicit configuration for RHEL/FFmpeg 7.x compatibility
            recorder = new FFmpegFrameRecorder(outputFile, videoWidth, videoHeight, audioChannels);

            // CRITICAL: Explicitly set format to mp4
            recorder.setFormat("mp4");

            // CRITICAL: Use H.264 codec explicitly (most compatible)
            recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);

            // CRITICAL: Set pixel format explicitly (YUV420P is most compatible)
            recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);

            // Set frame rate
            recorder.setFrameRate(frameRate > 0 ? frameRate : 30.0);

            // Set video bitrate (use grabber's or default)
            int videoBitrate = grabber.getVideoBitrate();
            recorder.setVideoBitrate(videoBitrate > 0 ? videoBitrate : 2000000); // 2 Mbps default

            // CRITICAL: Set video quality
            recorder.setVideoQuality(0); // 0 = use bitrate, not quality-based encoding

            // Configure audio if present
            if (audioChannels > 0) {
                // CRITICAL: Use AAC codec explicitly (most compatible with MP4)
                recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);

                int sampleRate = grabber.getSampleRate();
                recorder.setSampleRate(sampleRate > 0 ? sampleRate : 44100); // 44.1kHz default

                int audioBitrate = grabber.getAudioBitrate();
                recorder.setAudioBitrate(audioBitrate > 0 ? audioBitrate : 128000); // 128 kbps default

                recorder.setAudioChannels(audioChannels);

                log.info("Audio configured - Codec: AAC, Channels: {}, SampleRate: {}, Bitrate: {}",
                         audioChannels, recorder.getSampleRate(), recorder.getAudioBitrate());
            }

            log.info("Recorder configured - Format: mp4, VideoCodec: H264, PixelFormat: YUV420P, Bitrate: {}",
                     recorder.getVideoBitrate());

            recorder.start();

            OpenCVFrameConverter.ToMat converterToMat = new OpenCVFrameConverter.ToMat();
            Frame frame;
            int frameCount = 0;

            log.info("Starting frame processing...");

            // Process each frame
            while ((frame = grabber.grab()) != null) {
                if (frame.image != null) {
                    // Convert frame to Mat
                    Mat frameMat = converterToMat.convert(frame);

                    if (frameMat != null && !frameMat.empty()) {
                        // Apply watermark
                        overlayWatermark(frameMat, resizedWatermark, x, y);

                        // Convert back to Frame
                        Frame watermarkedFrame = converterToMat.convert(frameMat);
                        recorder.record(watermarkedFrame);

                        frameMat.release();
                    }

                    frameCount++;
                    if (frameCount % 30 == 0) {
                        log.debug("Processed {} frames", frameCount);
                    }
                } else if (frame.samples != null) {
                    // Record audio frame
                    recorder.record(frame);
                }
            }

            log.info("Completed processing {} video frames", frameCount);

            // Cleanup
            resizedWatermark.release();
            watermarkMat.release();

            return outputFile;

        } catch (Exception e) {
            log.error("Failed to process video with watermark: {}", e.getMessage(), e);

            // Cleanup output file if processing failed
            if (outputFile != null && outputFile.exists()) {
                outputFile.delete();
            }

            // Return original video file on error
            if (inputFile != null && inputFile.exists()) {
                return inputFile;
            }

            throw new IOException("Video watermark processing failed: " + e.getMessage(), e);

        } finally {
            // Close resources
            try {
                if (recorder != null) {
                    recorder.stop();
                    recorder.release();
                }
            } catch (Exception e) {
                log.warn("Error closing recorder: {}", e.getMessage());
            }

            try {
                if (grabber != null) {
                    grabber.stop();
                    grabber.release();
                }
            } catch (Exception e) {
                log.warn("Error closing grabber: {}", e.getMessage());
            }
        }
    }

    /**
     * Load watermark image from classpath or filesystem
     */
    private Mat loadWatermarkImage() {
        try {
            log.debug("Loading watermark image from: {}", watermarkImagePath);

            InputStream inputStream;
            if (watermarkImagePath.startsWith("classpath:")) {
                String path = watermarkImagePath.replace("classpath:", "");
                ClassPathResource resource = new ClassPathResource(path);
                inputStream = resource.getInputStream();
            } else {
                inputStream = Files.newInputStream(Path.of(watermarkImagePath));
            }

            // Save to temp file for OpenCV
            File tempWatermark = createTempFile("watermark", ".png");
            Files.copy(inputStream, tempWatermark.toPath(), StandardCopyOption.REPLACE_EXISTING);
            inputStream.close();

            // Load with OpenCV (with alpha channel)
            Mat watermark = imread(tempWatermark.getAbsolutePath(), IMREAD_UNCHANGED);

            // Delete temp file
            tempWatermark.delete();

            if (watermark == null || watermark.empty()) {
                log.error("Failed to load watermark image from OpenCV");
                return null;
            }

            log.info("Watermark loaded successfully - Size: {}x{}, Channels: {}",
                     watermark.cols(), watermark.rows(), watermark.channels());

            return watermark;

        } catch (Exception e) {
            log.error("Failed to load watermark image: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Overlay watermark on video frame with opacity
     */
    private void overlayWatermark(Mat frame, Mat watermark, int x, int y) {
        try {
            if (watermark.channels() == 4) {
                // Watermark has alpha channel
                overlayWithAlpha(frame, watermark, x, y);
            } else {
                // No alpha channel, use simple overlay with opacity
                overlaySimple(frame, watermark, x, y);
            }
        } catch (Exception e) {
            log.warn("Failed to overlay watermark on frame: {}", e.getMessage());
        }
    }

    /**
     * Overlay watermark with alpha channel and opacity
     */
    private void overlayWithAlpha(Mat frame, Mat watermark, int x, int y) {
        try {
            // Extract ROI (Region of Interest) from frame
            Rect roi = new Rect(x, y, watermark.cols(), watermark.rows());

            // Check bounds
            if (x < 0 || y < 0 || x + watermark.cols() > frame.cols() || y + watermark.rows() > frame.rows()) {
                log.warn("Watermark position out of bounds, skipping");
                return;
            }

            Mat frameROI = new Mat(frame, roi);

            // Split watermark into BGR and Alpha
            MatVector watermarkChannels = new MatVector();
            split(watermark, watermarkChannels);

            Mat watermarkBGR = new Mat();
            MatVector bgrChannels = new MatVector(3);
            bgrChannels.put(0, watermarkChannels.get(0));
            bgrChannels.put(1, watermarkChannels.get(1));
            bgrChannels.put(2, watermarkChannels.get(2));
            merge(bgrChannels, watermarkBGR);

            Mat alpha = watermarkChannels.get(3);

            // Apply opacity to alpha channel
            Mat alphaScaled = new Mat();
            alpha.convertTo(alphaScaled, CV_32F, opacity / 255.0, 0.0);

            // Convert frame ROI to float for blending
            Mat frameROIFloat = new Mat();
            frameROI.convertTo(frameROIFloat, CV_32F, 1.0, 0.0);

            Mat watermarkBGRFloat = new Mat();
            watermarkBGR.convertTo(watermarkBGRFloat, CV_32F, 1.0, 0.0);

            // Expand alpha to 3 channels
            MatVector alphaChannels = new MatVector(3);
            alphaChannels.put(0, alphaScaled);
            alphaChannels.put(1, alphaScaled);
            alphaChannels.put(2, alphaScaled);
            Mat alpha3 = new Mat();
            merge(alphaChannels, alpha3);

            // Create inverse alpha
            Mat invAlpha = new Mat();
            subtract(new Mat(alpha3.size(), alpha3.type(), Scalar.all(1.0)), alpha3, invAlpha);

            // Blend: result = watermark * alpha + frame * (1 - alpha)
            Mat blended = new Mat();
            Mat part1 = new Mat();
            Mat part2 = new Mat();

            multiply(watermarkBGRFloat, alpha3, part1);
            multiply(frameROIFloat, invAlpha, part2);
            add(part1, part2, blended);

            // Convert back to 8-bit
            Mat result = new Mat();
            blended.convertTo(result, CV_8U, 1.0, 0.0);
            result.copyTo(frameROI);

            // Cleanup
            result.release();
            watermarkChannels.close();
            bgrChannels.close();
            watermarkBGR.release();
            alpha.release();
            alphaScaled.release();
            frameROIFloat.release();
            watermarkBGRFloat.release();
            alphaChannels.close();
            alpha3.release();
            invAlpha.release();
            blended.release();
            part1.release();
            part2.release();
            frameROI.release();

        } catch (Exception e) {
            log.warn("Failed to overlay with alpha: {}", e.getMessage());
        }
    }

    /**
     * Simple overlay without alpha channel
     */
    private void overlaySimple(Mat frame, Mat watermark, int x, int y) {
        try {
            Rect roi = new Rect(x, y, watermark.cols(), watermark.rows());

            if (x < 0 || y < 0 || x + watermark.cols() > frame.cols() || y + watermark.rows() > frame.rows()) {
                log.warn("Watermark position out of bounds, skipping");
                return;
            }

            Mat frameROI = new Mat(frame, roi);
            addWeighted(frameROI, 1.0 - opacity, watermark, opacity, 0.0, frameROI);
            frameROI.release();

        } catch (Exception e) {
            log.warn("Failed to overlay simple: {}", e.getMessage());
        }
    }

    /**
     * Convert MultipartFile to temporary File
     */
    private File convertMultipartToFile(MultipartFile multipartFile) throws IOException {
        String extension = getFileExtension(multipartFile.getOriginalFilename());
        File tempFile = createTempFile("video_input", extension);

        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(multipartFile.getBytes());
        }

        log.debug("Converted MultipartFile to temp file: {}", tempFile.getAbsolutePath());
        return tempFile;
    }

    /**
     * Create temporary file with prefix and suffix
     */
    private File createTempFile(String prefix, String suffix) throws IOException {
        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        File tempFile = File.createTempFile(prefix + "_", suffix, tempDir);
        tempFile.deleteOnExit();
        return tempFile;
    }

    /**
     * Get file extension from filename
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return ".mp4";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    /**
     * Cleanup temporary files
     */
    public void cleanupTempFile(File file) {
        if (file != null && file.exists()) {
            try {
                if (file.delete()) {
                    log.debug("Deleted temp file: {}", file.getAbsolutePath());
                } else {
                    log.warn("Failed to delete temp file: {}", file.getAbsolutePath());
                }
            } catch (Exception e) {
                log.warn("Error deleting temp file: {}", e.getMessage());
            }
        }
    }
}


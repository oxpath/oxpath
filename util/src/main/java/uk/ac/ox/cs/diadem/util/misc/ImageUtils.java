/* 
 * COPYRIGHT (C) 2010-2015 DIADEM Team, Department of Computer Science, Oxford University. All Rights Reserved. 
 * 
 * This software is the confidential and proprietary information of the DIADEM project ("DIADEM"), Department of Computer Science, 
 * Oxford University ("Confidential Information").  You shall not disclose such Confidential Information and shall use 
 * it only in accordance with the terms of the license agreement you entered into with DIADEM.
 */
package uk.ac.ox.cs.diadem.util.misc;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;

import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Method;

/**
 * @author Giorgio Orsi (giorgio dot orsi at cs dot ox dot ac dot uk) Department of Computer Science - The University of
 *         Oxford.
 */
public class ImageUtils {

  public static void pngToJpeg(final InputStream inputImageStream, final OutputStream outputImageStream,
      final float scaleFactor, final float compressionQuality) throws IOException {

    // Load the image from imageFile.
    final BufferedImage inputImage = ImageIO.read(inputImageStream);

    // Get an image writer
    final ImageWriter imageWriter = ImageIO.getImageWritersByFormatName("jpeg").next();
    imageWriter.setOutput(ImageIO.createImageOutputStream(outputImageStream));

    pngToJpeg(inputImage, imageWriter, 1.0f, 1.0f);

    imageWriter.dispose();
  }

  /**
   * Convert an image from PNG to JPEG using the given scale factor and compression quality.
   * @param inputImageFile The input image file
   * @param outputImageFile The output image file
   * @param scaleFactor The scale factor [0,1]
   * @param compressionQuality the compression quality [0,1]
   * @throws IOException If the input file does not exist or no write permissions on output file.
   */
  public static void pngToJpeg(final File inputImageFile, final File outputImageFile, final float scaleFactor,
      final float compressionQuality, final int cropWidth, final int cropHeight) throws IOException {

    // Load the image from imageFile.
    BufferedImage inputImage = ImageIO.read(inputImageFile);

    // Get an image writer
    final ImageWriter imageWriter = ImageIO.getImageWritersByFormatName("jpeg").next();
    imageWriter.setOutput(ImageIO.createImageOutputStream(outputImageFile));

    // pngToJpeg(inputImage, imageWriter, 1.0f, 1.0f);
    // Set the scaled height and width.

    // Resize the image using Scalr
    final int realCropWidth = Math.min(cropWidth, inputImage.getWidth());
    final int realCropHeight = Math.min(cropHeight, inputImage.getHeight());
    BufferedImage scaledImage = Scalr.crop(inputImage, realCropWidth, realCropHeight);
    if (scaleFactor != 1.0f) {
      final int scaledHeight = Math.round(inputImage.getHeight() * scaleFactor);
      final int scaledWidth = Math.round(inputImage.getWidth() * scaleFactor);
      scaledImage = Scalr.resize(inputImage, Method.ULTRA_QUALITY, scaledWidth, scaledHeight, Scalr.OP_ANTIALIAS);
    }
    inputImage.flush();
    scaledImage.flush();
    inputImage.flush();
    inputImage = null;

    // Convert to JPEG.
    final BufferedImage newBufferedImage = new BufferedImage(scaledImage.getWidth(), scaledImage.getHeight(),
        BufferedImage.TYPE_INT_RGB);
    newBufferedImage.createGraphics().drawImage(scaledImage, 0, 0, Color.WHITE, null);
    scaledImage.flush();
    scaledImage = null;

    final ImageWriteParam param = imageWriter.getDefaultWriteParam();
    param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
    param.setCompressionQuality(compressionQuality);
    imageWriter.write(newBufferedImage);

    imageWriter.dispose();
  }

  /**
   * @param inputImage The input {@link BufferedImage}
   * @param imageWriter The {@link ImageWriter} to use
   * @param scaleFactor The scale factor
   * @param compressionQuality The compression quality
   * @throws IOException If unable to write using the given writer.
   */
  public static void pngToJpeg(final BufferedImage inputImage, final ImageWriter imageWriter, final float scaleFactor,
      final float compressionQuality) throws IOException {

    // Set the scaled height and width.
    final int scaledHeight = Math.round(inputImage.getHeight() * scaleFactor);
    final int scaledWidth = Math.round(inputImage.getWidth() * scaleFactor);

    // Resize the image using Scalr.
    BufferedImage scaledImage = Scalr.resize(inputImage, Method.ULTRA_QUALITY, scaledWidth, scaledHeight,
        Scalr.OP_ANTIALIAS);
    inputImage.flush();
    scaledImage.flush();

    // Convert to JPEG.
    final BufferedImage newBufferedImage = new BufferedImage(scaledImage.getWidth(), scaledImage.getHeight(),
        BufferedImage.TYPE_INT_RGB);
    newBufferedImage.createGraphics().drawImage(scaledImage, 0, 0, Color.WHITE, null);
    scaledImage.flush();
    scaledImage = null;

    final ImageWriteParam param = imageWriter.getDefaultWriteParam();
    param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
    param.setCompressionQuality(compressionQuality);
    imageWriter.write(newBufferedImage);
  }
}

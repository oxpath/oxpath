/* 
 * COPYRIGHT (C) 2010-2015 DIADEM Team, Department of Computer Science, Oxford University. All Rights Reserved. 
 * 
 * This software is the confidential and proprietary information of the DIADEM project ("DIADEM"), Department of Computer Science, 
 * Oxford University ("Confidential Information").  You shall not disclose such Confidential Information and shall use 
 * it only in accordance with the terms of the license agreement you entered into with DIADEM.
 */
package uk.ac.ox.cs.diadem.util.misc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import uk.ac.ox.cs.diadem.util.testsupport.StandardTestcase;

/**
 * @author Giorgio Orsi (giorgio dot orsi at cs dot ox dot ac dot uk) Department of Computer Science - The University of
 *         Oxford.
 */
public class ImageConversionTest extends StandardTestcase {

  @Test
  public void testImageConversionFromStreams() throws Exception {
    final Path inputFilePath = Paths.get("src/test/resources/uk/ac/ox/cs/diadem/util/misc/testImage.png");
    final InputStream is = new FileInputStream(inputFilePath.toFile());

    Files.createDirectories(Paths.get("output"));

    final FileOutputStream fos = new FileOutputStream(new File("output/convertedImage.jpg"));

    ImageUtils.pngToJpeg(is, fos, 1.0f, 1.0f);

    final Path outputFilePath = Paths.get("output/convertedImage.jpg");

    assertEquals(true, Files.exists(outputFilePath));
    assertEquals(true, Files.exists(outputFilePath));
    assertTrue((Files.size(outputFilePath) > 0) && (Files.size(outputFilePath) < Files.size(inputFilePath)));
  }

  @Test
  public void testImageConversionFromFiles() throws Exception {
    final Path inputFilePath = Paths.get("src/test/resources/uk/ac/ox/cs/diadem/util/misc/testImage.png");
    final File inputFile = new File("src/test/resources/uk/ac/ox/cs/diadem/util/misc/testImage.png");

    Files.createDirectories(Paths.get("output"));

    final File outputFile = new File("output/convertedImage.jpg");

    ImageUtils.pngToJpeg(inputFile, outputFile, 1.0f, 1.0f, 1200, 1000);

    final Path outputFilePath = Paths.get("output/convertedImage.jpg");
    assertEquals(true, Files.exists(outputFilePath));
    assertTrue((Files.size(outputFilePath) > 0) && (Files.size(outputFilePath) < Files.size(inputFilePath)));
  }
}

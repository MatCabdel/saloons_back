package com.backend_project_template.common.image;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.awt.image.BufferedImage;
import org.junit.jupiter.api.Test;

class ImageStorageServiceTest {

  private static final int RED = 0xffff0000;
  private static final int GREEN = 0xff00ff00;
  private static final int BLUE = 0xff0000ff;
  private static final int WHITE = 0xffffffff;
  private static final int BLACK = 0xff000000;
  private static final int YELLOW = 0xffffff00;

  @Test
  void keepsImageUnchangedWithoutExifOrientation() {
    BufferedImage source = createTestImage();

    assertSame(source, ImageStorageService.applyExifOrientation(source, 1));
  }

  @Test
  void appliesAllExifTransforms() {
    assertPixels(2, new int[][] {{GREEN, RED}, {WHITE, BLUE}, {YELLOW, BLACK}});
    assertPixels(3, new int[][] {{YELLOW, BLACK}, {WHITE, BLUE}, {GREEN, RED}});
    assertPixels(4, new int[][] {{BLACK, YELLOW}, {BLUE, WHITE}, {RED, GREEN}});
    assertPixels(5, new int[][] {{RED, BLUE, BLACK}, {GREEN, WHITE, YELLOW}});
    assertPixels(6, new int[][] {{BLACK, BLUE, RED}, {YELLOW, WHITE, GREEN}});
    assertPixels(7, new int[][] {{YELLOW, WHITE, GREEN}, {BLACK, BLUE, RED}});
    assertPixels(8, new int[][] {{GREEN, WHITE, YELLOW}, {RED, BLUE, BLACK}});
  }

  @Test
  void readsLittleAndBigEndianExifOrientation() {
    assertEquals(6, ImageStorageService.readExifOrientation(jpegWithExifOrientation(6, true)));
    assertEquals(8, ImageStorageService.readExifOrientation(jpegWithExifOrientation(8, false)));
  }

  @Test
  void ignoresMissingOrMalformedExifData() {
    assertEquals(1, ImageStorageService.readExifOrientation(new byte[] {1, 2, 3}));
    assertEquals(1, ImageStorageService.readExifOrientation(new byte[] {(byte) 0xff, (byte) 0xd8, 0}));
  }

  private void assertPixels(int orientation, int[][] expectedRows) {
    BufferedImage oriented = ImageStorageService.applyExifOrientation(createTestImage(), orientation);
    assertEquals(expectedRows[0].length, oriented.getWidth());
    assertEquals(expectedRows.length, oriented.getHeight());
    for (int y = 0; y < expectedRows.length; y++) {
      for (int x = 0; x < expectedRows[y].length; x++) {
        assertEquals(expectedRows[y][x], oriented.getRGB(x, y));
      }
    }
  }

  private BufferedImage createTestImage() {
    BufferedImage image = new BufferedImage(2, 3, BufferedImage.TYPE_INT_ARGB);
    image.setRGB(0, 0, RED);
    image.setRGB(1, 0, GREEN);
    image.setRGB(0, 1, BLUE);
    image.setRGB(1, 1, WHITE);
    image.setRGB(0, 2, BLACK);
    image.setRGB(1, 2, YELLOW);
    return image;
  }

  private byte[] jpegWithExifOrientation(int orientation, boolean littleEndian) {
    byte byteOrder = littleEndian ? (byte) 'I' : (byte) 'M';
    byte[] jpeg = new byte[40];
    jpeg[0] = (byte) 0xff;
    jpeg[1] = (byte) 0xd8;
    jpeg[2] = (byte) 0xff;
    jpeg[3] = (byte) 0xe1;
    jpeg[4] = 0;
    jpeg[5] = 34;
    jpeg[6] = 'E';
    jpeg[7] = 'x';
    jpeg[8] = 'i';
    jpeg[9] = 'f';
    jpeg[12] = byteOrder;
    jpeg[13] = byteOrder;
    writeShort(jpeg, 14, 42, littleEndian);
    writeInt(jpeg, 16, 8, littleEndian);
    writeShort(jpeg, 20, 1, littleEndian);
    writeShort(jpeg, 22, 0x0112, littleEndian);
    writeShort(jpeg, 24, 3, littleEndian);
    writeInt(jpeg, 26, 1, littleEndian);
    writeShort(jpeg, 30, orientation, littleEndian);
    jpeg[38] = (byte) 0xff;
    jpeg[39] = (byte) 0xd9;
    return jpeg;
  }

  private void writeShort(byte[] bytes, int offset, int value, boolean littleEndian) {
    bytes[offset] = (byte) (littleEndian ? value : value >> 8);
    bytes[offset + 1] = (byte) (littleEndian ? value >> 8 : value);
  }

  private void writeInt(byte[] bytes, int offset, int value, boolean littleEndian) {
    for (int index = 0; index < 4; index++) {
      int shift = littleEndian ? index * 8 : (3 - index) * 8;
      bytes[offset + index] = (byte) (value >> shift);
    }
  }
}

package io.playqd.util;

import io.playqd.commons.data.ArtworkSize;
import io.playqd.commons.utils.Tuple;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

@Slf4j
public abstract class ImageUtils {

  private static final Map<ArtworkSize, Tuple<Integer, Integer>> IMAGE_DIMENSIONS =
      Map.of(ArtworkSize.sm, Tuple.from(250, 250));

  public static byte[] resize(byte[] data, ArtworkSize artworkSize) {
    try {
      if (ArtworkSize.original == artworkSize) {
        return data;
      }
      var newDimensions = IMAGE_DIMENSIONS.get(artworkSize);

      if (newDimensions == null) {
        return data;
      }

      var newWidth = newDimensions.left();
      var newHeight = newDimensions.right();

      var image = ImageIO.read(new ByteArrayInputStream(data));

      if (image.getWidth() <= newWidth && image.getHeight() <= newHeight) {
        log.info("Original image size is less or equal to the new size. Resize is not required.");
        return data;
      }

      var imageType = ((image.getType() == 0) ? BufferedImage.TYPE_INT_ARGB : image.getType());
      var resizedImage = new BufferedImage(newWidth, newHeight, imageType);

      Graphics2D g2d = resizedImage.createGraphics();

      g2d.setComposite(AlphaComposite.Src);
      g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
      g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      g2d.drawImage(image, 0, 0, newWidth, newHeight, null);
      g2d.dispose();

      var out = new ByteArrayOutputStream();
      ImageIO.write(resizedImage, "jpg", out);
      return out.toByteArray();
    } catch (IOException e) {
      log.error("Resize image failed.", e);
      return new byte[]{};
    }
  }
}
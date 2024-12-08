package org.qbynet.chat.util;

import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@Component
public class CompressUtil {
    public void compressImage(InputStream inputStream, File out) throws IOException {
        BufferedImage image = ImageIO.read(inputStream);
        ImageIO.write(image, "webp", out);
    }
}

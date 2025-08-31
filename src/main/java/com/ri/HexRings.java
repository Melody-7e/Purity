package com.ri;

import static com.ri.helper.PurityMaths.roundToHex;

import com.ri.helper.Utils;
import com.ri.meta.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javax.imageio.ImageIO;

public class HexRings {
    private static final int size = 1080;
    private static final int fps = 60;
    private static final int length = 120;

    private static File imgDir;

    public static void main(String[] args) throws Exception {
        // @formatter:off
        ProjectType     type        = ProjectType.VIDEO;
        ProjectPD       pd          = ProjectPD.LEFT;
        ProjectCategory category    = ProjectCategory.SIGNATURE;
        byte            id          = (byte) 0x1c;
        String          name        = HexRings.class.getSimpleName();
        ProjectState    state       = ProjectState.SUCCESS;
        // @formatter:on

        ProjectName projectName = new ProjectName(type, pd, category, id, name, state);
        Projects.getInstance().checkName(projectName);

        System.out.print("================================ ");
        System.out.println(projectName.getFullName());

        execute(projectName);

        System.out.println();
        System.out.println("SUCCESS");
    }

    private static void execute(ProjectName projectName) throws Exception {
        imgDir = Files.createTempDirectory(HexRings.class.getSimpleName()).toFile();

        Utils.loopWithProgressParallel(HexRings::createImage, fps * length, "Writing Images");

        Utils.encodeVideo(projectName.getFile("mp4"), imgDir, fps);

        Utils.deleteDir(imgDir);
    }

    private static void createImage(int frame) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        double[] o = new double[2];

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                double x = 2.0 * i / size - 1.0;
                double y = 2.0 * j / size - 1.0;

                roundToHex(x, y, 2.5d / size, o);

                Color c = Color.getHSBColor((float) (((o[0] * o[0] + o[1] * o[1]) + 1) * frame * 25 / 24), 1, 1);
                img.setRGB(i, j, c.getRGB());
            }
        }

        try {
            ImageIO.write(img, "png", new File(imgDir, frame + ".png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
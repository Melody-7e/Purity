package com.ri;

import static com.ri.helper.PurityMaths.*;

import com.ri.helper.PurityMaths;
import com.ri.helper.Utils;
import com.ri.meta.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;

import javax.imageio.ImageIO;

// ! ABORTED
public class sM9230LAF3_6c {
    private static final float L_max = 0.75_126f;
    private static final float C_max = 0.126f;

    private static final float bps = 72 / 60f;
    private static final int musicLength = 8;

    private static final int size = 512;
    private static final int fps = 30;
    private static final int length = (int) (musicLength / bps + 1);

    private static final int vCount = 3;
    private static final float[][] color = new float[vCount][3]; // okLCH

    private static final ArrayList<Command> commands = new ArrayList<>();

    private static File imgDir;

    @SuppressWarnings("UnnecessaryLocalVariable")
    public static void main(String[] args) throws Exception {
        // @formatter:off
        String          _clazzName  = sM9230LAF3_6c.class.getSimpleName();
        ProjectType     type        = ProjectType.VIDEO;
        ProjectPD       pd          = ProjectPD.LEFT;
        ProjectCategory category    = ProjectCategory.VOID;
        byte            id          = (byte) Integer.parseInt(_clazzName.substring(_clazzName.length() - 2), 16);
        String          name        = _clazzName;
        ProjectState    state       = ProjectState.INCOMPLETE;
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
        imgDir = Files.createTempDirectory(sM9230LAF3_6c.class.getSimpleName()).toFile();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> Utils.deleteDir(imgDir)));

        for (int i = 0; i < vCount; i++) {
            color[i][0] = 1;
            color[i][1] = 1;
            color[i][2] = -i * 360f / vCount;
        }

        generateMusicData();

        Utils.loopWithProgressParallel(sM9230LAF3_6c::createImage, fps * length, "Writing Images");
        Utils.encodeVideo(projectName.getFile("mp4"), imgDir, fps);
    }

    private static void generateMusicData() {
        commands.add(new SimpleBeat(new float[vCount][3], new float[][]{{1, 1, 60}, {1, 1, 120}, {1, 1, 180}}));
        commands.add(new SimpleBeat(new float[vCount][3], new float[][]{{1, 1, 60}, {1, 1, 120}, {1, 1, 180}}));
        commands.add(new SimpleBeat(new float[vCount][3], new float[][]{{1, 1, 120}, {1, 1, 180}, {1, 1, 60}}));
        commands.add(new SimpleBeat(new float[vCount][3], new float[][]{{1, 1, 180}, {1, 1, 60}, {1, 1, 120}}));

        commands.add(new SimpleBeat(new float[vCount][3], new float[][]{{1, 1, -10}, {1, 1, 10}, {1, 1, 180}}));
        commands.add(new SimpleBeat(new float[vCount][3], new float[][]{{1, 1, 90}, {1, 1, 60}, {1, 1, 30}}));
        commands.add(new SimpleBeat(new float[vCount][3], new float[][]{{1, 1, 30}, {1, 1, 60}, {1, 1, 90}}));
        commands.add(new SimpleBeat(new float[vCount][3], new float[][]{{1, 1, 170}, {1, 1, 190}, {1, 1, 0}}));

        if (commands.size() != musicLength) throw new RuntimeException();
    }

    public static int getColorAt(float x, float y) {
        if (vCount != 3) throw new RuntimeException("Well implement this for that vCount");

        float w0 = (2.0f * x + 1.0f) / 3.0f;
        float w1 = (-x + SQRT_3 * y + 1.0f) / 3.0f;
        float w2 = (-x - SQRT_3 * y + 1.0f) / 3.0f;

        if (w0 < 0 || w1 < 0 || w2 < 0) {
            return 0;
        }

        float x0 = (float) Math.cos(Math.toRadians(color[0][2]));
        float y0 = (float) Math.sin(Math.toRadians(color[0][2]));
        float x1 = (float) Math.cos(Math.toRadians(color[1][2]));
        float y1 = (float) Math.sin(Math.toRadians(color[1][2]));
        float x2 = (float) Math.cos(Math.toRadians(color[2][2]));
        float y2 = (float) Math.sin(Math.toRadians(color[2][2]));

        float avgX = x0 * w0 + x1 * w1 + x2 * w2;
        float avgY = y0 * w0 + y1 * w1 + y2 * w2;

        float L = color[0][0] * w0 + color[1][0] * w1 + color[2][0] * w2;
        float C = color[0][1] * w0 + color[1][1] * w1 + color[2][1] * w2;
        float h = (float) Math.toDegrees(Math.atan2(avgY, avgX));

        // C *= 1.0f - (w0 * w1 * w2) * (w0 * w1 * w2) * (9 * 9 * 9);

        return oklchToSrgb(L * L_max, C * C_max, h);
    }

    private static void createImage(int frame) throws IOException {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);

        float time = (float) frame / fps * bps;
        int ind = (int) time;

        if (ind < musicLength) {
            Command command = commands.get(ind);
            command.update(time - ind);
        } else {
            color[0][0] = 0;
            color[0][1] = 0;
            color[0][2] = 0;

            color[1][0] = 0;
            color[1][1] = 0;
            color[1][2] = 0;

            color[2][0] = 0;
            color[2][1] = 0;
            color[2][2] = 0;
        }

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                float x = 2.0f * i / size - 1.0f;
                float y = 2.0f * j / size - 1.0f;

                int color = getColorAt(x, y);

                img.setRGB(i, j, 0xFF000000 | color);
            }
        }

        ImageIO.write(img, "png", new File(imgDir, frame + ".png"));
    }

    private interface Command {
        void update(float t);
    }

    private static class SimpleBeat implements Command {
        private static final float inTime = (float) (1 / Math.E);

        private final float[][] from;
        private final float[][] beat;

        private SimpleBeat(float[][] from, float[][] beat) {
            this.from = from;
            this.beat = beat;
        }

        @Override
        public void update(float t) {
            for (int i = 0; i < vCount; i++) {
                if (t < inTime) {
                    float k = curveIn(t / inTime);

                    float xF = (float) Math.cos(Math.toRadians(from[i][2]));
                    float yF = (float) Math.sin(Math.toRadians(from[i][2]));
                    float xB = (float) Math.cos(Math.toRadians(beat[i][2]));
                    float yB = (float) Math.sin(Math.toRadians(beat[i][2]));

                    float xH = PurityMaths.lerp(xF, xB, k);
                    float yH = PurityMaths.lerp(yF, yB, k);

                    float L = PurityMaths.lerp(1, beat[i][0], k); // ! there shall be from[i][0] but I can't easily change L
                    float C = PurityMaths.lerp(from[i][1], beat[i][1], k);
                    float h = (float) Math.toDegrees(Math.atan2(xH, yH));

                    color[i][0] = L;
                    color[i][1] = C;
                    color[i][2] = beat[i][2]; // ! there shall be `h` but there's also some bug
                } else {
                    float k = curveOut((t - inTime) / (1 - inTime));

                    color[i][0] = beat[i][0];
                    color[i][1] = beat[i][1] * (1 - k);
                    color[i][2] = beat[i][2];
                }
            }
        }

        private float curveOut(float x) {
            return x * x;
        }

        private float curveIn(float x) {
            return 1 - (1 - x) * (1 - x);
        }
    }
}

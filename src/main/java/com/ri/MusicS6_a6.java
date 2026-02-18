package com.ri;

import com.ri.helper.PurityMaths;
import com.ri.helper.Utils;
import com.ri.meta.*;

import java.io.ByteArrayInputStream;
import java.util.Random;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

public class MusicS6_a6 {
    @SuppressWarnings("UnnecessaryLocalVariable")
    public static void main(String[] args) throws Exception {
        // @formatter:off
        String          _clazzName  = MusicS6_a6.class.getSimpleName();
        ProjectType     type        = ProjectType.SOUND;
        ProjectPD       pd          = ProjectPD.LEFT;
        ProjectCategory category    = ProjectCategory.CLASS_T;
        byte            id          = (byte) Integer.parseInt(_clazzName.substring(_clazzName.length() - 2), 16);
        String          name        = _clazzName;
        ProjectState    state       = ProjectState.OKAY;
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
        int len = 216;
        float rate = 44100;
        float jitter = 0.173f;
        double bps = 8;


        int[] seq = new int[len];

        for (int level = 1; level < 12; level++) {
            for (int i = 0; i < seq.length; i++) {
                int div = Math.toIntExact(Math.round(Math.pow(6, level - 1)));

                int value = level * switch (i / div % 6) {
                    case 1 -> 2;
                    case 2 -> -1;
                    case 3 -> 1;
                    case 4 -> -2;
                    default -> 0;
                };

                if (level % 2 == 1) seq[i] += value;
                else seq[i] -= value;
            }
        }

        Random rand = new Random(0x7e);

        AudioFormat audioF;
        audioF = new AudioFormat(rate, 16, 1, true, false);

        int musicLen = (int) (rate * ((len / bps)));
        byte[] buf;
        buf = new byte[musicLen * 2];

        final double[] whiteLast = new double[1];
        Utils.loopWithProgress((i) -> {
            double time = (double) i / rate;

            int i1 = (int) (time * bps);
            int i2 = (int) (time * bps / 6);
            int i3 = (int) (time * bps / 6 / 6);

            double jitter1 = (1 - jitter) + jitter * ((i1 * 0.754877666246693) % 1.0);
            double jitter2 = (1 - jitter) + jitter * ((i2 * 0.796890427567314) % 1.0);
            double jitter3 = (1 - jitter) + jitter * ((i3 * 0.690859343451605) % 1.0);

            int s1 = seq[i1];
            int s2 = seq[i2];
            int s3 = seq[i3];

            double s = 0;
            s += jitter1 * (1 / 8d * 12 / 11) * getNoteAmplitude(time * bps % 1, 1 / bps, 440 * Math.pow(2, s1 / 12d));
            s += jitter2 * (1 / 2d * 12 / 11) * getNoteAmplitude(time * bps / 6 % 1, 6 / bps, 440 * Math.pow(2, s2 / 12d));
            s += jitter3 * (1 / 4d * 12 / 11) * getNoteAmplitude(time * bps / 6 / 6 % 1, 6 * 6 / bps, 440 * Math.pow(2,
                    s3 / 12d));

            double white = rand.nextDouble();
            double blue = (white - whiteLast[0]) / 2;
            whiteLast[0] = white;

            s += (1.0 - 7 / 8d * 12 / 11) * 0.07 * blue;

            s *= 0.125;

            buf[2 * i] = (byte) (s * Short.MAX_VALUE % 0xFF);
            buf[2 * i + 1] = (byte) ((short) (s * Short.MAX_VALUE) >> 8);
        }, musicLen, "Generating Sound");

        ByteArrayInputStream bis = new ByteArrayInputStream(buf);
        AudioInputStream ais = new AudioInputStream(bis, audioF, musicLen);
        AudioSystem.write(ais, AudioFileFormat.Type.WAVE, projectName.getFile("wav"));
    }


    public static double getNoteAmplitude(double x, double duration, double freq) {
        if (x < 0 || x >= 1) return 0;

        double decayTightness = PurityMaths.PHI * (1.0 + 0.06125 * Math.log(freq / 440.0));
        double attackPower = 0.25 / (1.0 + 0.037 * Math.log(freq / 110.0));

        double envelope = (1.0 - 0.0067 * Math.log(freq / 110.0)) * 1.73 *
                Math.pow(x, attackPower) * Math.pow(1.0 - Math.pow(x, 1.2), decayTightness);

        double phase = 2.0 * Math.PI * freq * x * duration;
        double signal = Math.sin(phase);
        signal += 0.43 * Math.sin(phase * 2.0);
        signal += 0.24 * Math.sin(phase * 3.0);
        signal += 0.13 * Math.sin(phase * 0.5);

        signal = Math.tanh(signal * 1.5); // Soft Saturation

        return signal * envelope / 1.8;
    }
}

package com.ri;

import com.ri.helper.PurityMaths;
import com.ri.helper.Utils;
import com.ri.meta.*;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.List;

import javax.sound.sampled.*;

public class Ius_Ymr_Ut76 {
    @SuppressWarnings("UnnecessaryLocalVariable")
    public static void main(String[] args) throws Exception {
        // @formatter:off
        String          _clazzName  = Ius_Ymr_Ut76.class.getSimpleName();
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
        List<Integer> tones = getToneArray();

        float rate = 44100;

        AudioFormat audioF;
        audioF = new AudioFormat(rate, 16, 1, true, false);

        final double bps = 0.71263;
        int len = (int) (rate * (tones.size()-1) / bps);
        byte[] buf;
        buf = new byte[len * 2];

        Utils.loopWithProgress((i) -> {
            double time = (double) i / rate;
            double angle = time * 2 * Math.PI;

            int currentTone = tones.get((int) (time * bps));

            double s = Math.sin(angle * 880 * Math.pow(2, currentTone/12f)) * PurityMaths.soundNoteCurve(time * bps % 1);

            buf[2 * i] = (byte) (s * Short.MAX_VALUE % 0xFF);
            buf[2 * i + 1] = (byte) ((short) (s * Short.MAX_VALUE) >> 8);
        }, len, "Generating Sound");

        ByteArrayInputStream bis = new ByteArrayInputStream(buf);
        AudioInputStream ais = new AudioInputStream(bis, audioF, len);
        AudioSystem.write(ais, AudioFileFormat.Type.WAVE, projectName.getFile("wav"));
    }

    private static List<Integer> getToneArray() {
        final int len = 128;
        final int skip = 4098;

        Random rand = new Random(0x7e);

        HashMap<Integer, ArrayList<Integer>> map = new HashMap<>();
        ArrayList<Integer> list = new ArrayList<>();

        list.add(0);
        for (int i = 0; i < len+skip; i++) {
            ArrayList<Integer> ps = map.computeIfAbsent(list.get(i), Ius_Ymr_Ut76::newList);

            int r = rand.nextInt(ps.size());

            int v = ps.get(r);

            ps.remove(rand.nextInt(ps.size()));
            ps.add(v);
            ps.add(v);
            list.add(v);
        }

        return list.subList(skip, len+skip);
    }

    private static ArrayList<Integer> newList(Integer i) {
        ArrayList<Integer> list = new ArrayList<>();
        for (int j = 0; j < 12; j++) {
            list.add(j);
            list.add(j);
        }
        return list;
    }
}

package com.ri.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import ws.schild.jave.Encoder;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.*;
import ws.schild.jave.encode.enums.TuneEnum;
import ws.schild.jave.info.MultimediaInfo;
import ws.schild.jave.progress.EncoderProgressListener;

public class Utils {
    private static final String MSG_FORMAT = "%-32s";
    private static final Object progressLock = new Object();
    private static final int totalLoopCalculations = 4096;
    private static final SecureRandom secureRandom = new SecureRandom();



    public static void loopWithProgress(Loopable l, int max, String msg) {
        long start = System.currentTimeMillis();

        int sl = (max + totalLoopCalculations - 1) / totalLoopCalculations;
        for (int i = 0; i < totalLoopCalculations; i++) {
            try {
                for (int j = i * sl; j < Math.min((i + 1) * sl, max); j++) {
                    l.runLoop(j);
                }
            } catch (Exception e) {
                System.out.println();
                throw new RuntimeException(e);
            }

            progressMsg(start, (float) (i + 0.5) * sl / max, msg);
        }

        finishMsg(start, msg);
    }

    public static void loopWithProgressParallel(Loopable l, int max, String msg) {
        int nThreads = Runtime.getRuntime().availableProcessors();
        try (ExecutorService executorService = Executors.newFixedThreadPool(nThreads)) {
            long start = System.currentTimeMillis();

            AtomicInteger done = new AtomicInteger();
            AtomicReference<Exception> firstException = new AtomicReference<>();
            AtomicInteger exceptionIndex = new AtomicInteger(-1);

            int sl = (max + totalLoopCalculations - 1) / totalLoopCalculations;
            for (int i = 0; i < totalLoopCalculations; i++) {
                final int finalI = i;
                executorService.submit(() -> {
                    if (firstException.get() != null) {
                        return;
                    }

                    for (int j = finalI * sl; j < Math.min((finalI + 1) * sl, max); j++) {
                        try {
                            l.runLoop(j);
                        } catch (Exception e) {
                            firstException.compareAndSet(null, e);
                            exceptionIndex.compareAndSet(-1, j);
                        } finally {
                            int doneInt = done.incrementAndGet();
                            synchronized (progressLock) {
                                progressMsg(start, (float) (doneInt + 0.5) * sl / max, msg);
                            }
                        }
                    }

                });
            }

            executorService.shutdown();
            if (!executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS)) throw new RuntimeException();

            if (firstException.get() != null) {
                throw new RuntimeException("While running loop " + exceptionIndex.get(), firstException.get());
            }

            synchronized (progressLock) {
                finishMsg(start, msg);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void runWithDuration(Runnable r, String msg) {
        System.out.printf("\r" + MSG_FORMAT + " Working....", msg);

        long start = System.currentTimeMillis();
        try {
            r.run();
        } catch (Exception e) {
            System.out.println();
            throw new RuntimeException(e);
        }

        finishMsg(start, msg);
    }

    public static void deleteDir(File file) {
        try (Stream<Path> walk = Files.walk(file.toPath())) {
            walk.sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException e) {
                            e.printStackTrace(System.err);
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void encodeVideo(File dstFile, File srcDir, int frameRate) throws Exception {
        final String msg = "Encoding Video";

        long startTime = System.currentTimeMillis();
        float duration = (float) Objects.requireNonNull(srcDir.listFiles()).length / frameRate;

        VideoAttributes videoAttrs = new VideoAttributes();
        videoAttrs.setFrameRate(frameRate);
        videoAttrs.setCrf(16);
        videoAttrs.setCodec("libx264");
        videoAttrs.setPixelFormat("yuv420p");
        videoAttrs.setTune(TuneEnum.FILM);

        EncodingAttributes attrs = new EncodingAttributes();
        attrs.setVideoAttributes(videoAttrs);
        attrs.setInputFormat("image2");
        attrs.setAudioAttributes(null);
        attrs.setDuration(duration);


        List<EncodingArgument> arguments = new ArrayList<>();
        arguments.add(new ValueArgument(ArgType.INFILE, "-r", ea -> Optional.of(Integer.toString(frameRate))));

        MultimediaObject srcObjects = new MultimediaObject(new File(srcDir, "%d.png"));
        srcObjects.setReadURLOnce(true);

        Encoder encoder = new Encoder();
        encoder.encode(Collections.singletonList(srcObjects), dstFile, attrs, new EncoderProgressListener() {
            @Override
            public void sourceInfo(MultimediaInfo info) {
            }

            @Override
            public void progress(int permil) {
                progressMsg(startTime, permil / 1000f, msg);
            }

            @Override
            public void message(String message) {
            }
        }, arguments);

        finishMsg(startTime, msg);
    }

    public static byte[] hash(String data) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return md.digest(data.getBytes(StandardCharsets.UTF_8));
    }

    public static byte[] hash(File file) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");

        try (FileInputStream fis = new FileInputStream(file)) {
            return md.digest(fis.readAllBytes());
        }
    }

    public static byte[] decrypt(byte[] data, byte[] key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(128, data, 0, 16));

        return cipher.doFinal(data, 16, data.length - 16);
    }

    public static byte[] encrypt(byte[] data, byte[] key) throws Exception {
        byte[] iv = new byte[16];
        secureRandom.nextBytes(iv);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(128, iv));

        byte[] encrypted = cipher.doFinal(data);

        byte[] full = new byte[encrypted.length + iv.length];
        System.arraycopy(iv, 0, full, 0, iv.length);
        System.arraycopy(encrypted, 0, full, iv.length, encrypted.length);

        return full;
    }

    private static void progressMsg(long start, float done, String msg) {
        float totalTime = (System.currentTimeMillis() - start) / 1000f;

        if (done > 0) {
            float remaining = (1 - done) * (totalTime / done);
            int remainingMnt = (int) (remaining / 60f);
            float remainingSec = remaining % 60f;

            System.out.printf("\r" + MSG_FORMAT + " Done = %02.1f%%,   Expected Time Remaining = %02d min %02.1f sec",
                    msg, done * 100f, remainingMnt, remainingSec);
        }
    }

    private static void finishMsg(long startTime, String msg) {
        float totalTime = (System.currentTimeMillis() - startTime) / 1000f;
        int totalMnt = (int) (totalTime / 60f);
        float totalSec = totalTime % 60f;
        System.out.printf("\r" + MSG_FORMAT + " Finished in %02d min %02.1f sec%n", msg, totalMnt, totalSec);
    }

    @FunctionalInterface
    public interface Loopable {
        void runLoop(int i) throws Exception;
    }

    @FunctionalInterface
    public interface Runnable {
        void run() throws Exception;
    }
}

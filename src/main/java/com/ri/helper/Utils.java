package com.ri.helper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import ws.schild.jave.Encoder;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.EncodingAttributes;
import ws.schild.jave.encode.VideoAttributes;
import ws.schild.jave.encode.enums.TuneEnum;
import ws.schild.jave.info.MultimediaInfo;
import ws.schild.jave.progress.EncoderProgressListener;

public class Utils {
    private static final String MSG_FORMAT = "%-32s";
    private static final Object progressLock = new Object();
    private static final int totalLoopCalculations = 4096;


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
        videoAttrs.setCodec("libx264");
        videoAttrs.setPixelFormat("yuv420p");
        videoAttrs.setTune(TuneEnum.ZEROLATENCY);
        videoAttrs.setQuality(Integer.MAX_VALUE);
        videoAttrs.setFrameRate(frameRate);

        EncodingAttributes attrs = new EncodingAttributes();
        attrs.setVideoAttributes(videoAttrs);
        attrs.setInputFormat("image2");
        attrs.setAudioAttributes(null);
        attrs.setDuration(duration);


        MultimediaObject srcObjects = new MultimediaObject(new File(srcDir, "%d.png"));
        srcObjects.setReadURLOnce(true);

        Encoder encoder = new Encoder();
        encoder.encode(srcObjects, dstFile, attrs, new EncoderProgressListener() {
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
        });

        finishMsg(startTime, msg);
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

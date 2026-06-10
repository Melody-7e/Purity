package com.ri;

import static com.ri.helper.PurityMaths.PHI;

import com.ri.helper.Utils;
import com.ri.meta.*;

import java.io.ByteArrayInputStream;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

public class Rz_Fjzw_V3e {
    private static final int SAMPLE_RATE = 44100;

    @SuppressWarnings("UnnecessaryLocalVariable")
    public static void main(String[] args)
            throws Exception
    {
        // @formatter:off
        String          _clazzName = Rz_Fjzw_V3e.class.getSimpleName();
        ProjectType     type       = ProjectType.SOUND;
        ProjectPD       pd         = ProjectPD.LEFT;
        ProjectCategory category   = ProjectCategory.COPY;
        byte            id         = (byte) Integer.parseInt(_clazzName.substring(_clazzName.length() - 2), 16);
        String          name       = _clazzName;
        ProjectState    state      = ProjectState.OKAY;
        // @formatter:on

        ProjectName projectName = new ProjectName(type, pd, category, id, name, state);
        Projects.getInstance().checkName(projectName);

        System.out.print("================================ ");
        System.out.println(projectName.getFullName());

        execute(projectName);

        System.out.println();
        System.out.println("SUCCESS");
    }

    // ~ piano
    public static void generateTone(float frequency, float durationSeconds, float volume, byte[] out, int offset) {
        int totalSamples = (int) (SAMPLE_RATE*durationSeconds);

        int     harmonicsCount = 13;
        float[] frequencies    = new float[harmonicsCount];
        float[] baseWeights    = new float[harmonicsCount];

        for (int n = 0; n < harmonicsCount; n++) {
            int harmonicNumber = n + 1;

            // fn = f * n * sqrt(1 + (phi / n))
            frequencies[n] = frequency*harmonicNumber;

            // 1 / n^phi
            baseWeights[n] = 1.0f/(float) Math.pow(harmonicNumber, PHI);
        }

        float b0             = 0.0f, b1 = 0.0f, b2 = 0.0f;
        float jitterStrength = 0.15f;

        for (int i = 0; i < totalSamples; i++) {
            float t                 = (float) i/SAMPLE_RATE;
            float sampleAccumulator = 0.0f;

            // Generate Voss-McCartney approximation of 1/f Pink Noise for phase jitter
            float white = ((float) Math.random()*2.0f) - 1.0f;
            b0 = 0.99886f*b0 + white*0.0555179f;
            b1 = 0.99332f*b1 + white*0.0750759f;
            b2 = 0.96900f*b2 + white*0.1538520f;
            float pinkNoise   = b0 + b1 + b2 + white*0.5362f;
            float phaseJitter = pinkNoise*jitterStrength;

            for (int n = 0; n < harmonicsCount; n++) {
                float decayRate        = 0.7f*(n + 1);
                float currentAmplitude = baseWeights[n]*(float) Math.exp(-t*decayRate/durationSeconds);

                // A * sin(2 * PI * f * t + phaseJitter)
                float angle = (2.0f*(float) Math.PI*frequencies[n]*t) + phaseJitter;
                sampleAccumulator += currentAmplitude*(float) Math.sin(angle);
            }

            float attack         = Math.min(1.0f, t/0.01f);
            float masterEnvelope = attack*(float) Math.exp(-t*0.4f/durationSeconds);

            float s = sampleAccumulator*masterEnvelope*0.35f*volume;

            int   l   = Byte.toUnsignedInt(out[offset + 2*i]);
            int   g   = Byte.toUnsignedInt(out[offset + 2*i + 1]);
            float pre = (float) (l | g << 8)/Short.MAX_VALUE;

            s += pre;

            out[offset + 2*i]     = (byte) ((short) (s*Short.MAX_VALUE)%0xFF);
            out[offset + 2*i + 1] = (byte) ((short) (s*Short.MAX_VALUE) >> 8);
        }
    }

    // ~ guitar
    public static void generateTone2(float frequency, float durationSeconds, float volume, byte[] out, int offset) {
        int     totalSamples = (int) (SAMPLE_RATE*durationSeconds);

        // The size of our delay line determines the pitch!
        int     delayLineSize = (int) (SAMPLE_RATE/frequency);
        float[] delayLine     = new float[delayLineSize];

        // 1. Strike the string: Fill the delay line with initial white noise chaos
        for (int i = 0; i < delayLineSize; i++) {
            delayLine[i] = ((float) Math.random()*2.0f) - 1.0f;
        }

        // 2. Let the energy vibrate and decay naturally
        int ringIndex = 0;
        for (int i = 0; i < totalSamples; i++) {
            int   l   = Byte.toUnsignedInt(out[offset + 2*i]);
            int   g   = Byte.toUnsignedInt(out[offset + 2*i + 1]);
            float pre = (float) (l | g << 8)/Short.MAX_VALUE;

            float s = pre + delayLine[ringIndex]*volume;

            out[offset + 2*i]     = (byte) ((short) (s*Short.MAX_VALUE)%0xFF);
            out[offset + 2*i + 1] = (byte) ((short) (s*Short.MAX_VALUE) >> 8);

            // The Mathematical Core: Average the current sample with the next one
            int nextIndex = (ringIndex + 1)%delayLineSize;

            // This feedback formula simulates structural acoustic dampening (loss factor ~0.996f)
            float newVibration = (delayLine[ringIndex] + delayLine[nextIndex])*0.5f*0.996f;

            // Feed it back into the loop
            delayLine[ringIndex] = newVibration;
                                   ringIndex = nextIndex;
        }
    }

    public static void generateTone2b(float frequency, float durationSeconds, float volume, byte[] out, int offset) {
        int totalSamples = (int) (SAMPLE_RATE * durationSeconds);
        float[] buffer = new float[totalSamples];

        int delayLineSize = (int) (SAMPLE_RATE / frequency);
        float[] delayLine = new float[delayLineSize];

        // 1. DYNAMIC DECAY MATH: Calculate exact feedback gain needed for this pitch
        float feedbackGain = (float) Math.exp(-1.0f / (frequency * durationSeconds));

        // 2. SOFT INITIALIZATION: Generate noise, but immediately smooth it
        float lastNoiseSample = 0.0f;
        for (int i = 0; i < delayLineSize; i++) {
            float rawNoise = ((float) Math.random() * 2.0f) - 1.0f;

            // Pre-filter the noise: Shaves off the sharp "snapping thread" transient
            float smoothNoise = 0.15f * rawNoise + 0.85f * lastNoiseSample;
            delayLine[i] = smoothNoise;
                           lastNoiseSample = smoothNoise;
        }

        // 3. Karplus-Strong Synthesis Loop
        int ringIndex = 0;
        for (int i = 0; i < totalSamples; i++) {
            buffer[i] = delayLine[ringIndex];

            int nextIndex = (ringIndex + 1) % delayLineSize;

            // Blend current sample with next sample and apply calculated decay scale
            float newVibration = (delayLine[ringIndex] + delayLine[nextIndex]) * 0.5f * feedbackGain;

            delayLine[ringIndex] = newVibration;
                                   ringIndex = nextIndex;
        }

        // 4. POST-EFFECT FILTER: Run a master Low-Pass Filter over the final wave
        float lpfHistory = 0.0f;
        float alpha = 1.0f - 0.0f; // High softness means lower alpha (more filtering)

        for (int i = 0; i < totalSamples; i++) {
            lpfHistory = (alpha * buffer[i]) + ((1.0f - alpha) * lpfHistory);
            buffer[i] = lpfHistory;
        }

        // 5. Final Soft Gain Envelope to completely mute tiny end-clicks
        for (int i = 0; i < totalSamples; i++) {
            float t = (float) i / SAMPLE_RATE;
            float release = Math.min(1.0f, (durationSeconds - t) / 0.100f);
            buffer[i] *= release * 0.4f;
        }

        // 2. PHYSICAL NORMALIZATION: Calculate current RMS energy of the generated sound
        float sumOfSquares = 0.0f;
        for (int i = 0; i < totalSamples; i++) {
            sumOfSquares += buffer[i] * buffer[i];
        }
        float rms = (float) Math.sqrt(sumOfSquares / totalSamples);
        if (rms == 0.0f) rms = 1.0f; // Prevent division by zero

        // 3. PSYCHOACOUSTIC NORMALIZATION: Calculate the Perceptual Equal-Loudness curve multiplier
        // This dampens frequencies as they approach the ultra-sensitive 1kHz-3kHz human hearing zone
        float targetReferenceFreq = 600.0f;
        float frequencyDeviation = (frequency - targetReferenceFreq) / 1500.0f;
        float perceptualGainCorrection = 1.0f / (1.0f + (frequencyDeviation * frequencyDeviation));

        // 4. MASTER COMPENSATED ALIGNMENT
        float targetPower = 0.15f; // Absolute baseline physical volume ceiling
        float masterGainScale = (targetPower / rms) * perceptualGainCorrection;

        for (int i = 0; i < totalSamples; i++) {
            buffer[i] *= masterGainScale;
        }

        for (int i = 0; i < totalSamples; i++) {
            int   l   = Byte.toUnsignedInt(out[offset + 2*i]);
            int   g   = Byte.toUnsignedInt(out[offset + 2*i + 1]);
            float pre = (float) (l | g << 8)/Short.MAX_VALUE;

            float s = pre + buffer[i] * volume;

            out[offset + 2*i]     = (byte) ((short) (s*Short.MAX_VALUE)%0xFF);
            out[offset + 2*i + 1] = (byte) ((short) (s*Short.MAX_VALUE) >> 8);
        }
    }


        // ~ annoying
    public static void generateTone3(float frequency, float durationSeconds, float volume, byte[] out, int offset) {
        int totalSamples = (int) (SAMPLE_RATE * durationSeconds);

        for (int i = 0; i < totalSamples; i++) {
            float t = (float) i / SAMPLE_RATE;

            float vibrato = (float) Math.sin(2.0f * Math.PI * 4.5f * t);

            float tremolo = 0.75f + 0.25f * (float) Math.sin(2.0f * Math.PI * 0.3f * t);

            float f1 = frequency + (vibrato * 0.3f); // Center drifts slightly
            float f2 = frequency * 1.003f;           // Drifts up
            float f3 = frequency * 0.997f;           // Drifts down

            float voice1 = (float) Math.sin(2.0f * Math.PI * f1 * t);
            float voice2 = (float) Math.sin(2.0f * Math.PI * f2 * t);
            float voice3 = (float) Math.sin(2.0f * Math.PI * f3 * t);

            float softWarmth = 0.1f * (float) Math.sin(2.0f * Math.PI * (f1 * 2.0f) * t);

            float mixedSignal = (voice1 * 0.5f) + (voice2 * 0.25f) + (voice3 * 0.25f) + softWarmth;

            float attack = Math.min(1.0f, t / 0.150f);
            float release = Math.min(1.0f, (durationSeconds - t) / 0.200f); // 200ms smooth fade-out
            float masterVolume = attack * release * tremolo * volume;

            float s = mixedSignal * masterVolume * 0.22f;

            int   l   = Byte.toUnsignedInt(out[offset + 2*i]);
            int   g   = Byte.toUnsignedInt(out[offset + 2*i + 1]);
            float pre = (float) (l | g << 8)/Short.MAX_VALUE;

            s += pre;

            out[offset + 2*i]     = (byte) ((short) (s*Short.MAX_VALUE)%0xFF);
            out[offset + 2*i + 1] = (byte) ((short) (s*Short.MAX_VALUE) >> 8);
        }
    }

    // ~ annoying 2
    public static void generateTone4(float frequency, float durationSeconds, float volume, byte[] out, int offset) {
        int totalSamples = (int) (SAMPLE_RATE * durationSeconds);
        float[] buffer = new float[totalSamples];

        // 1:1 Frequency Alignment for absolute harmonic purity
        float carrierFreq = frequency;
        float modulatorFreq = frequency;

        for (int i = 0; i < totalSamples; i++) {
            float t = (float) i / SAMPLE_RATE;

            // 1. Timbre LFO: Gently float the tone brightness up/down between 0.2 and 0.6
            // This makes the sound "breathe" without shifting its pitch alignment!
            float modulationIndex = 0.4f + 0.2f * (float) Math.sin(2.0f * Math.PI * 0.4f * t);

            // 2. Calculate the Modulator phase step
            float modulator = (float) Math.sin(2.0f * Math.PI * modulatorFreq * t);

            // 3. The FM Core Equation: Pure, single-point mathematical continuity
            float rawSample = (float) Math.sin(2.0f * Math.PI * carrierFreq * t + (modulationIndex * modulator));

            // 4. Velvet Envelope: 80ms cushion attack and 150ms soft tail release
            float attack = Math.min(1.0f, t / 0.080f);
            float release = Math.min(1.0f, (durationSeconds - t) / 0.150f);

            // Scale safely into safe head-room limits
            float s = rawSample * attack * release * volume * 0.25f;

            int   l   = Byte.toUnsignedInt(out[offset + 2*i]);
            int   g   = Byte.toUnsignedInt(out[offset + 2*i + 1]);
            float pre = (float) (l | g << 8)/Short.MAX_VALUE;

            s += pre;

            out[offset + 2*i]     = (byte) ((short) (s*Short.MAX_VALUE)%0xFF);
            out[offset + 2*i + 1] = (byte) ((short) (s*Short.MAX_VALUE) >> 8);
        }
    }

    public static float[] generateAnnoyingSound(float durationSeconds, float volume, byte[] out, int offset) {
        int     totalSamples = (int) (SAMPLE_RATE*durationSeconds);
        float[] buffer       = new float[totalSamples];

        // Target the absolute worst frequency zone for human ears
        float baseHarshFreq = 3200.0f;

        for (int i = 0; i < totalSamples; i++) {
            float t = (float) i/SAMPLE_RATE;

            float vibrato = (float) (Math.sin(2.0f * Math.PI * 4.5f * t) + Math.sin(2.0f * 12f * t) + Math.sin(2.0f*PHI*8f*t))/3.0f;

            float f1 = baseHarshFreq + (vibrato * 0.3f);
            float f2 = baseHarshFreq * 1.0015f;
            float f3 = baseHarshFreq * 0.9085f;

            float voice1 = (float) Math.sin(2.0f * Math.PI * f1 * t);
            float voice2 = (float) Math.sin(2.0f * Math.PI * f2 * t);
            float voice3 = (float) Math.sin(2.0f * Math.PI * f3 * t);

            // 4. Combine them into an un-filtered, hard-clipping square-ish profile
            float mixedSignal = voice1 + voice2 + voice3;

            // Aggressive hard-clipper to create nasty odd-harmonics
            // if (mixedSignal > 1.0f) mixedSignal = 1.0f;
            // if (mixedSignal < -1.0f) mixedSignal = -1.0f;

            // No soft attack envelope here! It snaps into existence instantly.
            float s = mixedSignal*0.25f*volume;

            int   l   = Byte.toUnsignedInt(out[offset + 2*i]);
            int   g   = Byte.toUnsignedInt(out[offset + 2*i + 1]);
            float pre = (float) (l | g << 8)/Short.MAX_VALUE;

            s += pre;

            out[offset + 2*i]     = (byte) ((short) (s*Short.MAX_VALUE)%0xFF);
            out[offset + 2*i + 1] = (byte) ((short) (s*Short.MAX_VALUE) >> 8);
        }

        return buffer;
    }

    private static void execute(ProjectName projectName)
            throws Exception
    {
        // int len = 27;
        double bps = 0.5f;

        // <|
        int[] seq = new int[]{0, 2, 3, 5,   6, 7, 2, 8,   5, 7, 2, 9,   4, 4, 7, 5, 4,   0, 2, 3, 5,  6, 7, 2, 8, 1, 1,   5, 7, 2, 9, 1, 1,   0, 2, 3, 5, 7, 8, 9,  0, 2, 5, 0, 0, 0, 5, 0,  0, 2, 3, 5,   5, 7, 2, 9,   6, 7, 2, 8,   4, 4, 7, 5, 3,   0, 2, 3, 7,  6, 7, 2, 8, 1, 1,   5, 7, 2, 9, 1, 1,   0, 2, 3, 7, 6, 8, 9,  0, 2, 5, 0, 0, 0, 5, 0};
        int[] dur = new int[]{8, 8, 8, 8,   8, 8, 8, 8,   8, 8, 8, 8,   4, 4, 8, 8, 8,   8, 8, 8, 8,  4, 8, 4, 8, 4, 12,  4, 8, 4, 8, 4, 12,  8, 8, 8, 4, 8, 8, 8,  4, 8, 8, 4, 4, 4, 4, 8,  8, 8, 8, 8,   8, 8, 8, 8,   8, 8, 8, 8,   4, 4, 8, 8, 8,   8, 8, 8, 8,  4, 8, 4, 8, 4, 12,  4, 8, 4, 8, 4, 12,  8, 8, 8, 4, 8, 8, 8,  4, 8, 8, 4, 6, 4, 4, 8};
        int[] dur2 = new int[]{24,          24,           24,           24,              24,          40,                 40,                 52,                   44,                      24,           24,           24,           24,              24,          40,                 40,                 52,                   44,                   };
        // |>
        int len = seq.length;

        AudioFormat audioF;
        audioF = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);

        int    musicLen = SAMPLE_RATE*450;
        byte[] buf;
        buf = new byte[musicLen*2];

        int[] index = new int[1];
        Utils.loopWithProgress(
                (i) -> {
                    int   i1       = i;
                    int   s1       = seq[i1];
                    float duration = dur[i1]/8f/(float) bps;
                    int   length   = (int) (SAMPLE_RATE*duration)*2;

                    generateTone((float) Math.pow(2, s1/12d)*660, duration, 0.05f, buf, index[0]);
                    generateTone3((float) Math.pow(2, s1/12d)*660, duration, 0.5f, buf, index[0]);
                    generateTone4((float) Math.pow(2, s1/12d)*660, duration, 0.3f, buf, index[0]);

                    index[0] += length;
                }, len, "Generating Melody"
        );

        index[0] = 0;
        Utils.loopWithProgress(
                (i) -> {
                    int   i1       = i%dur2.length;
                    int   s1       = seq[i1];
                    float duration = dur2[i1]/8f/(float) bps;
                    int   length   = (int) (SAMPLE_RATE*duration)*2;

                    generateTone2b((float) Math.pow(2, s1/12d)*220*1.041f, duration, 0.2f, buf, index[0]);
                    generateTone4((float) Math.pow(2, s1/12d)*220, duration, 0.4f, buf, index[0]);

                    index[0] += length;
                }, dur2.length*3, "Generating Bass"
        );

        index[0] /= 2;
        Utils.loopWithProgress(
                (i) -> {
                    int   i1       = i;
                    int   s1       = seq[i1];
                    float duration = dur[i1]/8f/(float) bps;
                    int   length   = (int) (SAMPLE_RATE*duration)*2;

                    generateTone3((float) Math.pow(2, s1/12d)*660, duration, 1.9f - ((float) i/len) * 1.8f, buf, index[0]); // i know it's out of bound
                    generateTone4((float) Math.pow(2, s1/12d)*660 * 1.0371f, duration, 1.2f - ((float) i/len) * 0.8f, buf, index[0]);

                    index[0] += length;
                }, len, "Generating Melody Again?"
        );

        Utils.runWithDuration(() -> {
            generateAnnoyingSound((float) 450, 0.4f, buf, 0);
        }, "Making it more annoying");

        ByteArrayInputStream bis = new ByteArrayInputStream(buf);
        AudioInputStream     ais = new AudioInputStream(bis, audioF, musicLen);
        AudioSystem.write(ais, AudioFileFormat.Type.WAVE, projectName.getFile("wav"));
    }
}

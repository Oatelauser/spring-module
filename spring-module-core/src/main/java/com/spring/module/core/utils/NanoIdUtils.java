package com.spring.module.core.utils;

import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * nano id
 *
 * @author DearYang
 * @date 2022-08-05
 * @since 1.0
 */
@SuppressWarnings("unused")
public final class NanoIdUtils {

    public static final SecureRandom DEFAULT_NUMBER_GENERATOR = new SecureRandom();
    public static final char[] DEFAULT_ALPHABET = "_0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    public static final int DEFAULT_SIZE = 21;

    public static String randomNanoId() {
        return randomNanoId(DEFAULT_NUMBER_GENERATOR, DEFAULT_ALPHABET, DEFAULT_SIZE);
    }

    public static String threadRandomNanoId() {
        return randomNanoId(ThreadLocalRandom.current(), DEFAULT_ALPHABET, DEFAULT_SIZE);
    }

    public static String randomNanoId(int size) {
        return randomNanoId(DEFAULT_NUMBER_GENERATOR, DEFAULT_ALPHABET, size);
    }

    public static String threadRandomNanoId(int size) {
        return randomNanoId(ThreadLocalRandom.current(), DEFAULT_ALPHABET, size);
    }

    public static String randomNanoId(Random random) {
        return randomNanoId(random, DEFAULT_ALPHABET, DEFAULT_SIZE);
    }

    public static String randomNanoId(Random random, char[] alphabet, int size) {
        if (random == null || alphabet == null) {
            throw new IllegalArgumentException("random cannot be null.");
        }
        if (alphabet.length == 0 || alphabet.length >= 256) {
            throw new IllegalArgumentException("alphabet must contain between 1 and 255 symbols.");
        }

        if (size <= 0) {
            throw new IllegalArgumentException("size must be greater than zero.");
        }

        int mask = (2 << (int) Math.floor(Math.log(alphabet.length - 1) / Math.log(2.0D))) - 1;
        int step = (int) Math.ceil(1.6D * (double) mask * (double) size / (double) alphabet.length);
        StringBuilder idBuilder = new StringBuilder();

        while (true) {
            byte[] bytes = new byte[step];
            random.nextBytes(bytes);

            for (int i = 0; i < step; ++i) {
                int alphabetIndex = bytes[i] & mask;
                if (alphabetIndex < alphabet.length) {
                    idBuilder.append(alphabet[alphabetIndex]);
                    if (idBuilder.length() == size) {
                        return idBuilder.toString();
                    }
                }
            }
        }
    }

}

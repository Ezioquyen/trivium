package org.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;
import java.util.Random;

public class Main {
    static final String fileName = "world192.txt";

    public static String textToBits(String text) {
        byte[] bytes = text.getBytes();
        StringBuilder binaryStringBuilder = new StringBuilder();
        for (byte b : bytes) {
            int value = b;
            for (int i = 0; i < 8; i++) {
                binaryStringBuilder.append((value & 128) == 0 ? 0 : 1);
                value <<= 1;
            }
        }
        return binaryStringBuilder.toString();
    }

    public static char XOR(char a, char b) {
        return (a != b) ? '1' : '0';
    }

    public static char AND(char a, char b) {
        return (a == '1' && b == '1') ? '1' : '0';
    }

    public static char[] initRegisters(char[] registerA, char[] registerB, char[] registerC, int[] key, int[] IV) {
        for (int i = 0; i < 80; i++) {
            IV[i] = IV[i] & 1;
            key[i] = key[i] & 1;
        }

        for (int i = 0; i < 80; i++) {
            registerA[i] = (char) (IV[i] + '0');
            registerB[i] = (char) (key[i] + '0');
        }

        registerC[110] = registerC[109] = registerC[108] = '1';

        for (int i = 0; i < 4 * 288; i++) {
            char t1 = XOR(registerB[77], XOR(AND(registerA[90], registerA[91]), XOR(registerA[65], registerA[92])));
            char t2 = XOR(registerC[86], XOR(AND(registerB[81], registerB[82]), XOR(registerB[68], registerB[83])));
            char t3 = XOR(registerA[68], XOR(AND(registerC[108], registerC[109]), XOR(registerC[65], registerC[110])));

            System.arraycopy(registerA, 0, registerA, 1, registerA.length - 1);
            registerA[0] = t3;
            System.arraycopy(registerB, 0, registerB, 1, registerB.length - 1);
            registerB[0] = t1;
            System.arraycopy(registerC, 0, registerC, 1, registerC.length - 1);
            registerC[0] = t2;
        }

        return registerA;
    }

    public static char[] keyGeneration(char[] registerA, char[] registerB, char[] registerC, int length) {
        char[] z = new char[length];

        for (int i = 0; i < length; i++) {
            char t1 = XOR(registerA[65], XOR(registerA[92], AND(registerA[90], registerA[91])));
            char t2 = XOR(registerB[68], XOR(registerB[83], AND(registerB[81], registerB[82])));
            char t3 = XOR(registerC[65], XOR(registerC[110], AND(registerC[108], registerC[109])));

            z[i] = XOR(t1, XOR(t2, t3));

            char temp1 = XOR(t1, registerB[77]);
            char temp2 = XOR(t2, registerC[86]);
            char temp3 = XOR(t3, registerA[68]);

            System.arraycopy(registerA, 0, registerA, 1, registerA.length - 1);
            registerA[0] = temp3;
            System.arraycopy(registerB, 0, registerB, 1, registerB.length - 1);
            registerB[0] = temp1;
            System.arraycopy(registerC, 0, registerC, 1, registerC.length - 1);
            registerC[0] = temp2;
        }

        return z;
    }

    public static char[] encryption(String plainText, int[] key, int[] IV) {
        char[] binaryText = textToBits(plainText).toCharArray();
        char[] registerA = new char[93];
        char[] registerB = new char[84];
        char[] registerC = new char[111];

        registerA = initRegisters(registerA, registerB, registerC, key, IV);
        char[] keyStream = keyGeneration(registerA, registerB, registerC, binaryText.length);
        char[] encryptedText = new char[binaryText.length];

        for (int i = 0; i < binaryText.length; i++) {
            encryptedText[i] = XOR(keyStream[i], binaryText[i]);
        }

        return encryptedText;
    }

    public static void main(String[] args) throws IOException {
        long startTime = System.currentTimeMillis();

        Random random = new Random();
        int[] IV = new int[80];
        int[] key = new int[80];
        for (int i = 0; i < 80; i++) {
            IV[i] = random.nextInt(2);
            key[i] = random.nextInt(2);
        }

        StringBuilder plainTextBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(Objects.requireNonNull(Main.class.getResource("/TestData/"+fileName)).getPath()))) {
            String line;
            while ((line = br.readLine()) != null) {
                plainTextBuilder.append(line).append('\n');
            }
        }

        String plainText = plainTextBuilder.toString();
        char[] encryptedText = encryption(plainText, key, IV);

        try (FileWriter fileWriter = new FileWriter(Objects.requireNonNull(Main.class.getResource("/TestResult/"+fileName)).getPath()))        {
            fileWriter.write(encryptedText);
        }

        System.out.println("Thời gian chạy: " + (System.currentTimeMillis() - startTime) + " milliseconds");
    }
}

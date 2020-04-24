package vis.ex.reg.mycredentialsapplication.encryption;

import android.util.Log;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

import static java.lang.Math.abs;


public class DataEncryptionSystem {

    private int[] shifts;
    private int[] pc_1;
    private int[] pc_2;
    private int[] ip;
    private int[] e_bit_table;
    private int[][] __sbox = new int[8][64];
    private int[] p_table;
    private int[] ip_1;

    public DataEncryptionSystem() {

        this.shifts = new int[] {
                1, 1, 2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 1
        };

        this.pc_1 = new int[] {
                57, 49, 41, 33, 25, 17, 9, 1, 58, 50, 42, 34, 26, 18, 10, 2,
                59, 51, 43, 35, 27, 19, 11, 3, 60, 52, 44, 36, 63, 55, 47, 39,
                31, 23, 15, 7, 62, 54, 46, 38, 30, 22, 14, 6, 61, 53, 45, 37,
                29, 21, 13, 5, 28, 20, 12, 4
        };

        this.pc_2 = new int[] {
                14, 17, 11, 24, 1, 5, 3, 28, 15, 6, 21, 10, 23, 19, 12, 4,
                26, 8, 16, 7, 27, 20, 13, 2, 41, 52, 31, 37, 47, 55, 30, 40,
                51, 45, 33, 48, 44, 49, 39, 56, 34, 53, 46, 42, 50, 36, 29, 32
        };

        this.ip = new int[]{
                58, 50, 42, 34, 26, 18, 10, 2, 60, 52, 44, 36, 28, 20, 12, 4,
                62, 54, 46, 38, 30, 22, 14, 6, 64, 56, 48, 40, 32, 24, 16, 8,
                57, 49, 41, 33, 25, 17, 9, 1, 59, 51, 43, 35, 27, 19, 11, 3,
                61, 53, 45, 37, 29, 21, 13, 5, 63, 55, 47, 39, 31, 23, 15, 7
        };

        this.e_bit_table = new int[]{
                32, 1, 2, 3, 4, 5, 4, 5, 6, 7, 8, 9, 8, 9, 10, 11, 12, 13,
                12, 13, 14, 15, 16, 17, 16, 17, 18, 19, 20, 21, 20, 21, 22,
                23, 24, 25, 24, 25, 26, 27, 28, 29, 28, 29, 30, 31, 32, 1
        };
        this.p_table = new int[]{
                16, 7, 20, 21, 29, 12, 28, 17, 1, 15, 23, 26, 5, 18, 31, 10,
                2, 8, 24, 14, 32, 27, 3, 9, 19, 13, 30, 6, 22, 11, 4, 25
        };

        this.ip_1 = new int[]{
                40, 8, 48, 16, 56, 24, 64, 32, 39, 7, 47, 15, 55, 23, 63, 31,
                38, 6, 46, 14, 54, 22, 62, 30, 37, 5, 45, 13, 53, 21, 61, 29,
                36, 4, 44, 12, 52, 20, 60, 28, 35, 3, 43, 11, 51, 19, 59, 27,
                34, 2, 42, 10, 50, 18, 58, 26, 33, 1, 41, 9, 49, 17, 57, 25
        };

        this.__sbox[0] = new int[] {
                14, 4, 13, 1, 2, 15, 11, 8, 3, 10, 6, 12, 5, 9, 0, 7, 0, 15, 7, 4, 14, 2, 13, 1, 10, 6, 12, 11, 9, 5, 3, 8,
                4, 1, 14, 8, 13, 6, 2, 11, 15, 12, 9, 7, 3, 10, 5, 0, 15, 12, 8, 2, 4, 9, 1, 7, 5, 11, 3, 14, 10, 0, 6, 13};
        this.__sbox[1] = new int[] {
                15, 1, 8, 14, 6, 11, 3, 4, 9, 7, 2, 13, 12, 0, 5, 10, 3, 13, 4, 7, 15, 2, 8, 14, 12, 0, 1, 10, 6, 9, 11, 5,
                0, 14, 7, 11, 10, 4, 13, 1, 5, 8, 12, 6, 9, 3, 2, 15, 13, 8, 10, 1, 3, 15, 4, 2, 11, 6, 7, 12, 0, 5, 14, 9};
        this.__sbox[2] = new int[]
                {10, 0, 9, 14, 6, 3, 15, 5, 1, 13, 12, 7, 11, 4, 2, 8, 13, 7, 0, 9, 3, 4, 6, 10, 2, 8, 5, 14, 12, 11, 15, 1,
                        13, 6, 4, 9, 8, 15, 3, 0, 11, 1, 2, 12, 5, 10, 14, 7, 1, 10, 13, 0, 6, 9, 8, 7, 4, 15, 14, 3, 11, 5, 2, 12};
        this.__sbox[3] = new int[]
                {7, 13, 14, 3, 0, 6, 9, 10, 1, 2, 8, 5, 11, 12, 4, 15, 13, 8, 11, 5, 6, 15, 0, 3, 4, 7, 2, 12, 1, 10, 14, 9,
                        10, 6, 9, 0, 12, 11, 7, 13, 15, 1, 3, 14, 5, 2, 8, 4, 3, 15, 0, 6, 10, 1, 13, 8, 9, 4, 5, 11, 12, 7, 2, 14};
        this.__sbox[4] = new int[]
                {2, 12, 4, 1, 7, 10, 11, 6, 8, 5, 3, 15, 13, 0, 14, 9, 14, 11, 2, 12, 4, 7, 13, 1, 5, 0, 15, 10, 3, 9, 8, 6,
                        4, 2, 1, 11, 10, 13, 7, 8, 15, 9, 12, 5, 6, 3, 0, 14, 11, 8, 12, 7, 1, 14, 2, 13, 6, 15, 0, 9, 10, 4, 5, 3};
        this.__sbox[5] = new int[]
                {12, 1, 10, 15, 9, 2, 6, 8, 0, 13, 3, 4, 14, 7, 5, 11, 10, 15, 4, 2, 7, 12, 9, 5, 6, 1, 13, 14, 0, 11, 3, 8,
                        9, 14, 15, 5, 2, 8, 12, 3, 7, 0, 4, 10, 1, 13, 11, 6, 4, 3, 2, 12, 9, 5, 15, 10, 11, 14, 1, 7, 6, 0, 8, 13};
        this.__sbox[6] = new int[]
                {4, 11, 2, 14, 15, 0, 8, 13, 3, 12, 9, 7, 5, 10, 6, 1, 13, 0, 11, 7, 4, 9, 1, 10, 14, 3, 5, 12, 2, 15, 8, 6,
                        1, 4, 11, 13, 12, 3, 7, 14, 10, 15, 6, 8, 0, 5, 9, 2, 6, 11, 13, 8, 1, 4, 10, 7, 9, 5, 0, 15, 14, 2, 3, 12};
        this.__sbox[7] = new int[]
                {13, 2, 8, 4, 6, 15, 11, 1, 10, 9, 3, 14, 5, 0, 12, 7, 1, 15, 13, 8, 10, 3, 7, 4, 12, 5, 6, 11, 0, 14, 9, 2,
                        7, 11, 4, 1, 9, 12, 14, 2, 0, 6, 10, 13, 15, 3, 5, 8, 2, 1, 14, 7, 4, 10, 8, 13, 15, 12, 9, 0, 3, 5, 6, 11};
    }

    private static byte[] CreateAsciiArray(String m) {
        /*
         * Create an array of the ASCII base 10 representation of each character in the plain text message.
         */
        byte[] array = new byte[m.length()];

        for (int x=0; x < m.length(); x++) {
            int ch = m.charAt(x);
            array[x] = (byte) ch;
        }
        return array;
    }

    private static byte[] PadMessage(byte[] array) {
        /*
         * Add '0D0A' to the end of the message indicates that the message has finished and everything after this is
         * padding. Carriage return: '0D' (13 ASCII), line feed: '0A' (10 ASCII). If required pad the message array with
         * zeros until it is a multiple of 8.
         */
        byte[] paddedArray = new byte[array.length +2 + (8 - ((array.length +2) % 8))];
        System.arraycopy(array, 0, paddedArray, 0, array.length);

        paddedArray[array.length] = (byte) 13;
        paddedArray[array.length +1] = (byte) 10;

        for (int x=array.length +2; x < paddedArray.length; x++) {
            paddedArray[x] = (byte) 0;
        }

        return paddedArray;
    }

    private static byte[] ConvertBinaryStringToByteArray(String binaryString) {
        byte[] byteArray = new byte[binaryString.length() /8];
        for (int x=0; x<byteArray.length; x++) {
            byteArray[x] = Byte.parseByte(binaryString.substring(x*8, (x*8)+8), 2);
        }
        return byteArray;
    }

    private byte[] RemovePadding(byte[] byteArray) {
        int remove = 0;
        for (int b=byteArray.length -1; b>-1; b-=1) {
            if (byteArray[b] == 0) {
                remove += 1;
            } else if (byteArray[b] == 10) {
                remove += 1;
            } else if (byteArray[b] == 13) {
                remove += 1;
                break;
            }
        }
        return Arrays.copyOfRange(byteArray, 0, byteArray.length -remove);
    }

    private static String Convert64bitByteArrayToBinaryString(byte[] array) {
        StringBuilder binaryString = new StringBuilder();
        for (byte b : array) {
            binaryString.append(KeyGenerator.PadBinaryString(Integer.toBinaryString(b), 8));
        }
        return binaryString.toString();
    }

    private static String PrepareMessage(String m) {
        byte[] byteArray = CreateAsciiArray(m);
        byte[] paddedByteArray = PadMessage(byteArray);
        return Convert64bitByteArrayToBinaryString(paddedByteArray);
    }

    private static String HexToBinary(String hexMessage) {
        StringBuilder binMessage = new StringBuilder();

        for (int i =0; i < hexMessage.length(); i++) {
            StringBuilder binString = new StringBuilder();
            binString.append(Integer.toBinaryString(Character.getNumericValue(hexMessage.charAt(i))));

            while (binString.length() < 4) {
                binString.insert(0, "0");
            }
            binMessage.append(binString.toString());
        }
        return binMessage.toString();
    }

    private static String BinaryToHex(String binaryMessage) {
        StringBuilder hexMessage = new StringBuilder();
        for (int x=0; x < binaryMessage.length(); x+=4) {

            int t = Integer.parseInt(binaryMessage.substring(x, x +4), 2);
            if (t > 9) {
                hexMessage.append((char)(t + 55));
            } else {
                hexMessage.append(t);
            }
        }
        return hexMessage.toString();
    }

    private String[] Encrypt(String message, ArrayList<Long> kys, boolean decrypt) {
        int d = (decrypt) ? 15 : 0;
        StringBuilder binaryEncryption = new StringBuilder();

        // split the message into 64 bit blocks
        for (int x=0; x < message.length(); x+=64) {
            String bit64 = message.substring(x, x+64);

            // Initial_permutation(ip) substitution
            String initial_permutation = KeyGenerator.Substitution(this.ip, bit64);

            // Divide the permuted block IP into a left and right half of 32 bits.
            long ln = 0;
            long rn = 0;
            long ln_minus_1 = Long.parseLong(initial_permutation.substring(0, 32), 2);
            long rn_minus_1 = Long.parseLong(initial_permutation.substring(32), 2);

            // 16 iterations of:Ln = Rn - 1, Rn = Ln - 1 + f(Rn - 1, Kn)
            for (int y=0; y<16; y++) {
                ln = rn_minus_1;
                rn = ln_minus_1 ^ (F.Evaluate(rn_minus_1, kys.get(abs(d - y)), this.e_bit_table, this.__sbox, this.p_table));

                ln_minus_1 = ln;
                rn_minus_1 = rn;
            }

            bit64 = KeyGenerator.PadBinaryString(Long.toBinaryString(rn), 32) + KeyGenerator.PadBinaryString(Long.toBinaryString(ln), 32);
            binaryEncryption.append(KeyGenerator.Substitution(this.ip_1, bit64));
        }
        return new String[] {binaryEncryption.toString(), BinaryToHex(binaryEncryption.toString())};
    }

    public String[] EncryptDES(String message, String key) throws IOException {
        System.out.println("\nEncrypting with DES...");
        ArrayList<Long> keys = KeyGenerator.GenerateKeys(key, this.pc_1, this.pc_2, this.shifts);
        String preparedMessage = PrepareMessage(message);

        return Encrypt(preparedMessage, keys, false);
    }

    public String DecryptDES(String encryptedMessageBinary, String key) throws IOException {
        System.out.println("\nDecrypting with DES...");
        ArrayList<Long> keys = KeyGenerator.GenerateKeys(key, this.pc_1, this.pc_2, this.shifts);
        String[] decryptedMessage = Encrypt(encryptedMessageBinary, keys, true);
        String binaryMessage = decryptedMessage[0];

        return new String(RemovePadding(ConvertBinaryStringToByteArray(binaryMessage)), StandardCharsets.UTF_8);
    }

    public String[] EncryptTripleDES(String message, String key1, String key2, String key3) throws IOException {
            System.out.println("Encrypting with triple DES...");
            ArrayList<Long> keys1 = KeyGenerator.GenerateKeys(key1, this.pc_1, this.pc_2, this.shifts);
            ArrayList<Long> keys2 = KeyGenerator.GenerateKeys(key2, this.pc_1, this.pc_2, this.shifts);
            ArrayList<Long> keys3 = KeyGenerator.GenerateKeys(key3, this.pc_1, this.pc_2, this.shifts);

            String preparedMessage = PrepareMessage(message);

            String[] encryptionStrings1 = Encrypt(preparedMessage, keys1, false);
            String[] encryptionStrings2 = Encrypt(encryptionStrings1[0], keys2, true);

            return Encrypt(encryptionStrings2[0], keys3, false);
    }

    public String DecryptTripleDES(String encryptedMessageHex, String key1, String key2, String key3) throws IOException {
        System.out.println("Decrypting with triple DES...");
        ArrayList<Long> keys1 = KeyGenerator.GenerateKeys(key1, this.pc_1, this.pc_2, this.shifts);
        ArrayList<Long> keys2 = KeyGenerator.GenerateKeys(key2, this.pc_1, this.pc_2, this.shifts);
        ArrayList<Long> keys3 = KeyGenerator.GenerateKeys(key3, this.pc_1, this.pc_2, this.shifts);

        String[] decryptionStrings1 = Encrypt(HexToBinary(encryptedMessageHex), keys3, true);
        String[] decryptionStrings2 = Encrypt(decryptionStrings1[0], keys2, false);
        String[] decryptionStrings3 = Encrypt(decryptionStrings2[0], keys1, true);

        String binaryMessage = decryptionStrings3[0];

        ConvertBinaryStringToByteArray(binaryMessage);
        return new String(RemovePadding(ConvertBinaryStringToByteArray(binaryMessage)), StandardCharsets.UTF_8);
    }
}

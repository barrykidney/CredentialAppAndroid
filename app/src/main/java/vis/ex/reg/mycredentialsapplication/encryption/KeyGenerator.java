package vis.ex.reg.mycredentialsapplication.encryption;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class KeyGenerator {

    public static ArrayList<Long> GenerateKeys(String k, int[] pc_1, int[] pc_2, int[] shifts) throws IOException {
        ArrayList<Map<String, Integer>> keyList = SplitKey(Substitution(pc_1, ConvertStringToHex(k)));
        Map<String, Integer> ck = keyList.get(0);
        Map<String, Integer> dk = keyList.get(1);

        ArrayList<Map<String, Integer>> completeKeyList = GenerateKeyBlocks(ck, dk, shifts);
        Map<String, Integer> cx = completeKeyList.get(0);
        Map<String, Integer> dx = completeKeyList.get(1);

        return SubstitutionPC2(cx, dx, pc_2);
    }

    private static int RemoveFirstBit(int numb) {
        numb -= (int)Math.pow(2, (Integer.toBinaryString(numb).length() -1));
        return numb;
    }

    private static ArrayList<Map<String, Integer>> SplitKey(String k) {
        ArrayList<Map<String, Integer>> keyList = new ArrayList<>();
        Map<String, Integer> c = new HashMap<>();
        Map<String, Integer> d = new HashMap<>();

        c.put("c0", Integer.parseInt(k.substring(0, 28), 2));
        d.put("d0", Integer.parseInt(k.substring(28), 2));

        keyList.add(c);
        keyList.add(d);

        return keyList;
    }


    protected static String Substitution(int [] subArray, String msg) {
        StringBuilder sb = new StringBuilder();
        for (int idx : subArray) {
            sb.append(msg.charAt(idx -1));
        }
        return sb.toString();
    }

    private static ArrayList<Long> SubstitutionPC2(Map<String, Integer> c, Map<String, Integer> d, int[] pc_2) {
        ArrayList<Long> keys = new ArrayList<>();

        for (int x = 0; x < 16; x++) {
            StringBuilder ky = new StringBuilder();

            String cky = PadBinaryString(Integer.toBinaryString(c.get("c" + (x + 1))), 28);
            String dky = PadBinaryString(Integer.toBinaryString(d.get("d" + (x + 1))), 28);
            String cdky = cky + dky;

            for (int pc : pc_2) {
                ky.append(cdky.charAt(pc -1)); // -1 because the array index starts at 0, but the sub box numbers start at 1
            }
            keys.add(Long.parseLong(ky.toString(), 2));
        }
        return keys;
    }

    private static String ConvertStringToHex(String strArg) throws IOException {
        /*
         * Convert each char to its ASCII base 10 number using ord() then range the results between 0 and 15. Convert
         * that number to binary using bin() then drop the leading '0b'. example: b -> B -> 66 -> 11 -> 0b1011 -> 1011.
         * If a char conversion has less than 4 bits pad with zeros. Finally concatenate each to the string 'k'.
         */
        if (strArg.length() < 1) {
            throw new IOException("KeyGenerator.GenerateKeys.ConvertStringToHex: strArg.length() < 1");
        }
        StringBuilder sb = new StringBuilder();


        for(int x = 0; x < strArg.length(); x++) {
            char ch = strArg.charAt(x);

            String ch_binary;
            int a = Character.toUpperCase(ch);

            if (a > 47 && a < 58) {
                ch_binary = Integer.toBinaryString(a - 48);
            } else if(a > 64 && a < 91) {
                ch_binary = Integer.toBinaryString(a - 55);
            } else {
                throw new IOException("Error: character " + ch + " in key is out of range.");
            }
            sb.append(PadBinaryString(ch_binary, 4));
        }
        return sb.toString();
    }

    protected static String PadBinaryString(String binaryString, int numbOfDigits) {
        StringBuilder sb = new StringBuilder(binaryString);
        while (sb.length() < numbOfDigits) {
            sb.insert(0, "0");
        }
        return sb.toString();
    }

    private static ArrayList<Map<String, Integer>> GenerateKeyBlocks(Map<String, Integer> c, Map<String, Integer> d, int[] shifts) {
        ArrayList<Map<String, Integer>> keyList = new ArrayList<>();

        for (int x=0; x < shifts.length; x++) {
            int shift = shifts[x];
            int cf = c.get("c" + x);
            int df = d.get("d" + x);

            while (shift > 0) {
                cf = cf << 1;
                if (Integer.toBinaryString(cf).length() > 28) {
                    cf += 1;
                    cf = RemoveFirstBit(cf);
                }
                df = df << 1;
                if (Integer.toBinaryString(df).length() > 28) {
                    df += 1;
                    df = RemoveFirstBit(df);
                }
                shift -=1;
            }
            c.put(String.format("c%d", x +1), cf);
            d.put(String.format("d%d", x +1), df);
        }
        keyList.add(c);
        keyList.add(d);

        return keyList;
    }
}

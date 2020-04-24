package vis.ex.reg.mycredentialsapplication.encryption;


public class F {

    public static long Evaluate(long rn_minus1, long kn, int[] e_bit_table, int[][] __sbox, int[] p_table) {
        // e_bit = substitution(e_bit_table, rn_minus1)
        StringBuilder e_bit = new StringBuilder();
        for (int e : e_bit_table) {
            e_bit.append(KeyGenerator.PadBinaryString(Long.toBinaryString(rn_minus1),32).charAt(e -1));
        }
        long xored = Long.parseLong(e_bit.toString(), 2) ^ kn;

        // sbox substitution, (input: 48 bit block, return: 32 bit block) The 48 bit data is divided into 8 blocks of 6
        // bits these are substituted with the corresponding sbox (block 1 is substituted using sbox 1, etc.) resulting
        // in 8 blocks of 4 bits, when aggregated the output is a 32 bit block.
        StringBuilder op = new StringBuilder();
        for (int x=0; x<8; x++) {

            String bit6 = KeyGenerator.PadBinaryString(Long.toBinaryString(xored), 48)
                    .substring((x * 6), (x * 6) +6);

            String row = String.format("%c%c", bit6.charAt(0), bit6.charAt(5));
            String col = String.format("%c%c%c%c", bit6.charAt(1), bit6.charAt(2), bit6.charAt(3), bit6.charAt(4));

            op.append(KeyGenerator.PadBinaryString(Long.toBinaryString(__sbox[x][(Integer.parseInt(row, 2) *16)
                    + (Integer.parseInt(col, 2))]), 4));
        }
        String op2 = KeyGenerator.Substitution(p_table, op.toString());

        return Long.parseLong(op2, 2);
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Sergey
 */
public class AES {
    private static int nb, nk, nr;
    private static byte[][] w;

    private static byte[] subWord(byte[] in)
    {
        byte[] temp = new byte[in.length];
        for (int i = 0; i < temp.length; i++)
            temp[i] = (byte) (BlackBox.sbox[in[i] & 0x000000ff] & 0xff);
        return temp;
    }

    private static byte[][] keyExpansion(byte[] key)
    {
        byte[][] tmp = new byte[nb * (nr + 1)][4];
        for(int i = 0; i < nk; i++)
        {
            tmp[i][0] = key [i * 4];
            tmp[i][1] = key[i * 4 + 1];
            tmp[i][2] = key[i * 4 + 2];
            tmp[i][3] = key[i * 4 + 3];
        }

        for (int i = nk; i < nb * (nr + 1); i++)
        {
            byte[] temp = new byte[4];

            for(int k = 0; k < 4; k++)
                temp[k] = tmp[i - 1][k];
            if (i % nk == 0)
            {
                temp = subWord(Utils.rotateWord(temp));
                temp[0] = (byte) (temp[0] ^ (BlackBox.rcon[i / nk] & 0xff));
            } else
            {
                if (nk > 6 && i % nk == 4)
                {
                    temp = subWord(temp);
                }
            }
            tmp[i] = Utils.xorArrays(tmp[i - nk], temp);
        }

        return tmp;
    }

    private static byte[][] addRoundKey(byte[][] state, byte[][] w, int round)
    {

        byte[][] temp = new byte[state.length][state[0].length];

        for (int c = 0; c < nb; c++)
        {
            for (int l = 0; l < 4; l++)
                temp[l][c] = (byte) (state[l][c] ^ w[round * nb + c][l]);
        }

        return temp;
    }

    private static byte[][] subBytes(byte[][] state)
    {
        byte[][] temp = new byte[state.length][state[0].length];
        for (int row = 0; row < 4; row++)
            for (int col = 0; col < nb; col++)
                temp[row][col] = (byte) (BlackBox.sbox[(state[row][col] & 
                        0x000000ff)] & 0xff);
        return temp;
    }

    private static byte[][] invSubBytes(byte[][] state)
    {
        for (int row = 0; row < 4; row++)
            for (int col = 0; col < nb; col++)
                state[row][col] = (byte)(BlackBox.invSbox[(state[row][col] & 
                        0x000000ff)]&0xff);
        return state;
    }

    private static byte[][] shiftRows(byte[][] state)
    {

        byte[] t = new byte[4];
        for (int r = 1; r < 4; r++)
        {
            for (int c = 0; c < nb; c++)
                t[c] = state[r][(c + r) % nb];
            for (int c = 0; c < nb; c++)
                state[r][c] = t[c];
        }
        return state;
    }

    private static byte[][] invShiftRows(byte[][] state)
    {
        byte[] t = new byte[4];
        for (int r = 1; r < 4; r++)
        {
            for (int c = 0; c < nb; c++)
                t[(c + r) % nb] = state[r][c];
            for (int c = 0; c < nb; c++)
                state[r][c] = t[c];
        }
        return state;
    }

    private static byte[][] invMixColumns(byte[][] s)
    {
        int[] sp = new int[4];
        byte b02 = (byte)0x0e, 
             b03 = (byte)0x0b, 
             b04 = (byte)0x0d, 
             b05 = (byte)0x09;
        for (int c = 0; c < 4; c++)
        {
            sp[0] = Utils.GFMultiply(b02, s[0][c]) ^ 
                    Utils.GFMultiply(b03, s[1][c]) ^ 
                    Utils.GFMultiply(b04, s[2][c]) ^ 
                    Utils.GFMultiply(b05, s[3][c]);
            
            sp[1] = Utils.GFMultiply(b05, s[0][c]) ^ 
                    Utils.GFMultiply(b02, s[1][c]) ^ 
                    Utils.GFMultiply(b03, s[2][c]) ^ 
                    Utils.GFMultiply(b04, s[3][c]);
            
            sp[2] = Utils.GFMultiply(b04, s[0][c]) ^ 
                    Utils.GFMultiply(b05, s[1][c]) ^ 
                    Utils.GFMultiply(b02, s[2][c]) ^ 
                    Utils.GFMultiply(b03, s[3][c]);
            
            sp[3] = Utils.GFMultiply(b03, s[0][c]) ^ 
                    Utils.GFMultiply(b04, s[1][c]) ^ 
                    Utils.GFMultiply(b05, s[2][c]) ^ 
                    Utils.GFMultiply(b02, s[3][c]);
            for (int i = 0; i < 4; i++)
                s[i][c] = (byte)(sp[i]);
        }

        return s;
    }

    private static byte[][] mixColumns(byte[][] s)
    {
        int[] sp = new int[4];
        byte b02 = (byte)0x02, 
             b03 = (byte)0x03;
        for (int c = 0; c < 4; c++)
        {
            sp[0] = Utils.GFMultiply(b02, s[0][c]) ^ 
                    Utils.GFMultiply(b03, s[1][c]) ^ s[2][c]  ^ s[3][c];
            
            sp[1] = s[0][c]  ^ Utils.GFMultiply(b02, s[1][c]) ^
                    Utils.GFMultiply(b03, s[2][c]) ^ s[3][c];
            
            sp[2] = s[0][c]  ^ s[1][c]  ^
                    Utils.GFMultiply(b02, s[2][c]) ^ Utils.
                            GFMultiply(b03, s[3][c]);
            
            sp[3] = Utils.GFMultiply(b03, s[0][c]) ^ s[1][c] 
                    ^ s[2][c]  ^ Utils.GFMultiply(b02, s[3][c]);
            
            for (int i = 0; i < 4; i++)
                s[i][c] = (byte)(sp[i]);
        }

        return s;
    }

    private static byte[] encProcess(byte[] in)
    {

        byte[] temp = new byte[in.length];
        byte[][] state = new byte[4][nb];

        for (int i = 0; i < in.length; i++)
            state[i / 4][i % 4] = in[i % 4 * 4 + i / 4];

        state = addRoundKey(state, w, 0);
        for (int round = 1; round < nr; round++)
        {
            state = subBytes(state);
            state = shiftRows(state);
            state = mixColumns(state);
            state = addRoundKey(state, w, round);
        }
        state = subBytes(state);
        state = shiftRows(state);
        state = addRoundKey(state, w, nr);

        for (int i = 0; i < temp.length; i++)
            temp[i % 4 * 4 + i / 4] = state[i / 4][i % 4];
        return temp;
    }

    private static byte[] invEncProcess(byte[] in)
    {
        byte[] temp = new byte[in.length];
        byte[][] state = new byte[4][nb];
        for (int i = 0; i < in.length; i++)
            state[i / 4][i % 4] = in[i % 4 * 4 + i / 4];

        state = addRoundKey(state, w, nr);

        for (int round = nr - 1; round >= 1; round--)
        {
            state = invSubBytes(state);
            state = invShiftRows(state);
            state = addRoundKey(state, w, round);
            state = invMixColumns(state);
        }
        state = invSubBytes(state);
        state = invShiftRows(state);
        state = addRoundKey(state, w, 0);

        for (int i = 0; i < temp.length; i++)
            temp[i % 4 * 4 + i / 4] = state[i / 4][i % 4];
        return temp;
    }

    public static byte[] encryptECB(byte[] in,byte[] key)
    {
        nb = 4;
        nk = key.length / 4;
        nr = nk + 6;

        int length = 16 - in.length % 16;
        byte[] padding = new byte[length];
        padding[0] = (byte) 0x80;
        for (int i = 1; i < length; i++)
            padding[i] = 0;

        byte[] tmp = new byte[in.length + length];
        byte[] bloc = new byte[16];


        w = keyExpansion(key);

        int count = 0;
        int i = 0;
        for (i = 0; i < in.length + length; i++)
        {
            if (i > 0 && i % 16 == 0)
            {
                bloc = encProcess(bloc);
                System.arraycopy(bloc, 0, tmp, i - 16, bloc.length);
            }
            if (i < in.length)
            {
                bloc[i % 16] = in[i];
            } else
            {
                bloc[i % 16] = padding[count % 16];
                count++;
            }
        }
        if(bloc.length == 16)
        {
            bloc = encProcess(bloc);
            System.arraycopy(bloc, 0, tmp, i - 16, bloc.length);
        }

        return tmp;
    }
    
        public static byte[] decryptECB(byte[] in,byte[] key)
    {
        byte[] tmp = new byte[in.length];
        byte[] bloc = new byte[16];

        nb = 4;
        nk = key.length/4;
        nr = nk + 6;
        w = keyExpansion(key);

        int i;
        for (i = 0; i < in.length; i++)
        {
            if (i > 0 && i % 16 == 0)
            {
                bloc = invEncProcess(bloc);
                System.arraycopy(bloc, 0, tmp, i - 16, bloc.length);
            }
            if (i < in.length)
                bloc[i % 16] = in[i];
        }
        bloc = invEncProcess(bloc);
        System.arraycopy(bloc, 0, tmp, i - 16, bloc.length);
        tmp = Utils.deletePadding(tmp);
        return tmp;
    }
    
    public static byte[] encryptCBC(byte[] in,byte[] key)
    {
        nb = 4;
        nk = key.length / 4;
        nr = nk + 6;

        int length = 16 - in.length % 16;
        byte[] padding = new byte[length];
        padding[0] = (byte) 0x80;
        for (int i = 1; i < length; i++)
            padding[i] = 0;

        byte[] tmp = new byte[in.length + length];
        byte[] bloc = new byte[16];


        w = keyExpansion(key);

        byte[] c = BlackBox.IV;
        
        int count = 0;
        int i = 0;
        for (i = 0; i < in.length + length; i++)
        {
            if (i > 0 && i % 16 == 0)
            {
                byte[] temp = bloc;
                bloc = encProcess(Utils.xorArrays(bloc, c));
                System.arraycopy(bloc, 0, tmp, i - 16, bloc.length);
                c = temp;
            }
            if (i < in.length)
            {
                bloc[i % 16] = in[i];
            } else
            {
                bloc[i % 16] = padding[count % 16];
                count++;
            }
        }
        if(bloc.length == 16)
        {
            bloc = encProcess(Utils.xorArrays(bloc, c));
            System.arraycopy(bloc, 0, tmp, i - 16, bloc.length);
        }

        return tmp;
    }
    
    public static byte[] decryptCBC(byte[] in,byte[] key)
    {
        byte[] tmp = new byte[in.length];
        byte[] bloc = new byte[16];

        nb = 4;
        nk = key.length/4;
        nr = nk + 6;
        w = keyExpansion(key);

        byte[] c = BlackBox.IV;
        
        int i;
        for (i = 0; i < in.length; i++)
        {
            if (i > 0 && i % 16 == 0)
            {
                c = Utils.xorArrays(c, invEncProcess(bloc));
                System.arraycopy(c, 0, tmp, i - 16, c.length);
            }
            if (i < in.length)
                bloc[i % 16] = in[i];
        }
        bloc = Utils.xorArrays(c, invEncProcess(bloc));
        System.arraycopy(bloc, 0, tmp, i - 16, bloc.length);
        tmp = Utils.deletePadding(tmp);
        return tmp;
    }
    
    public static byte[] encryptCFB(byte[] in,byte[] key)
    {
        nb = 4;
        nk = key.length / 4;
        nr = nk + 6;

        int length = 16 - in.length % 16;
        byte[] padding = new byte[length];
        padding[0] = (byte) 0x80;
        for (int i = 1; i < length; i++)
            padding[i] = 0;

        byte[] tmp = new byte[in.length + length];
        byte[] bloc = new byte[16];
            
        byte[] c = BlackBox.IV;

        w = keyExpansion(key);

        int count = 0;
        int i = 0;
        for (i = 0; i < in.length + length; i++)
        {
            if (i > 0 && i % 16 == 0)
            {
                c = Utils.xorArrays(encProcess(c), bloc);
                System.arraycopy(c, 0, tmp, i - 16, c.length);
            }
            if (i < in.length)
            {
                bloc[i % 16] = in[i];
            } else
            {
                bloc[i % 16] = padding[count % 16];
                count++;
            }
        }
        if(bloc.length == 16)
        {
            c = Utils.xorArrays(encProcess(c), bloc);
            System.arraycopy(c, 0, tmp, i - 16, c.length);
        }

        return tmp;
    }
    
    public static byte[] decryptCFB(byte[] in,byte[] key)
    {
        byte[] tmp = new byte[in.length];
        byte[] bloc = new byte[16];

        nb = 4;
        nk = key.length/4;
        nr = nk + 6;
        w = keyExpansion(key);

        byte[] c = BlackBox.IV;
        
        int i;
        for (i = 0; i < in.length; i++)
        {
            if (i > 0 && i % 16 == 0)
            {
                byte[] temp = bloc;
                bloc = Utils.xorArrays(encProcess(c), bloc);
                c = temp;
                System.arraycopy(bloc, 0, tmp, i - 16, bloc.length);
            }
            if (i < in.length)
                bloc[i % 16] = in[i];
        }
        bloc = Utils.xorArrays(encProcess(c), bloc);
        System.arraycopy(bloc, 0, tmp, i - 16, bloc.length);
        tmp = Utils.deletePadding(tmp);
        return tmp;
    }
    
    public static byte[] encryptOFB(byte[] in,byte[] key)
    {
        nb = 4;
        nk = key.length / 4;
        nr = nk + 6;

        int length = 16 - in.length % 16;
        byte[] padding = new byte[length];
        padding[0] = (byte) 0x80;
        for (int i = 1; i < length; i++)
            padding[i] = 0;

        byte[] tmp = new byte[in.length + length];
        byte[] bloc = new byte[16];

        byte[] o = BlackBox.IV;
        
        w = keyExpansion(key);

        int count = 0;
        int i = 0;
        for (i = 0; i < in.length + length; i++)
        {
            if (i > 0 && i % 16 == 0)
            {
                bloc = Utils.xorArrays(bloc, o);
                System.arraycopy(bloc, 0, tmp, i - 16, bloc.length);
                o = encProcess(o);
            }
            if (i < in.length)
            {
                bloc[i % 16] = in[i];
            } else
            {
                bloc[i % 16] = padding[count % 16];
                count++;
            }
        }
        if(bloc.length == 16)
        {
            bloc = Utils.xorArrays(bloc, o);
            System.arraycopy(bloc, 0, tmp, i - 16, bloc.length);
        }

        return tmp;
    }
    
        public static byte[] decryptOFB(byte[] in,byte[] key)
    {
        byte[] tmp = new byte[in.length];
        byte[] bloc = new byte[16];

        nb = 4;
        nk = key.length/4;
        nr = nk + 6;
        w = keyExpansion(key);
        byte[] o = BlackBox.IV;

        int i;
        for (i = 0; i < in.length; i++)
        {
            if (i > 0 && i % 16 == 0)
            {
                bloc = Utils.xorArrays(bloc, o);
                System.arraycopy(bloc, 0, tmp, i - 16, bloc.length);
                o = encProcess(o);
            }
            if (i < in.length)
                bloc[i % 16] = in[i];
        }
        bloc = Utils.xorArrays(bloc, o);
        System.arraycopy(bloc, 0, tmp, i - 16, bloc.length);
        tmp = Utils.deletePadding(tmp);
        return tmp;
    }
}

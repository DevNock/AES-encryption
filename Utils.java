/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Sergey
 */
public class Utils {
     public static byte[] xorArrays(byte[] a, byte[] b)
    {
        byte[] out = new byte[a.length];
        for (int i = 0; i < a.length; i++)
        {
            out[i] = (byte) (a[i] ^ b[i]);
        }
        return out;
    }

    public static byte GFMultiply(byte a, byte b)
    {
        byte editedA = a, editedB = b;
        byte result = 0;
        byte temp;
        while (editedA != 0)
        {
            if ((editedA & 1) != 0)
            {
                result = (byte) (result ^ editedB);
            }
            temp = (byte) (editedB & 0x80);
            editedB = (byte) (editedB << 1);
            if (temp != 0)
                editedB = (byte) (editedB ^ 0x1b);
            editedA = (byte) ((editedA & 0xff) >> 1);
        }
        return result; 
    }

    public static byte[] deletePadding(byte[] text)
    {
        int count = 0;
        int i = text.length - 1;
        while (text[i] == 0)
        {
            count++;
            i--;
        }
        byte[] temp = new byte[text.length - count - 1];
        System.arraycopy(text, 0, temp, 0, temp.length);
        return temp;
    }


    public static byte[] rotateWord(byte[] input)
    {
        byte[] temp = new byte[input.length];
        temp[0] = input[1];
        temp[1] = input[2];
        temp[2] = input[3];
        temp[3] = input[0];
        return temp;
    }
}

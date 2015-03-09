/**************************************************************************
 *
 *  Copyright 2014, Roger Brown
 *
 *  This file is part of Roger Brown's Toolkit.
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU Lesser General Public License as published by the
 *  Free Software Foundation, either version 3 of the License, or (at your
 *  option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 *  more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 */

/*
 * $Id: Hex.java 1 2014-06-07 22:37:15Z rhubarb-geek-nz $
 */

package com.gauravtproject.android.emvreader.app.emvtools;

/**
 * Re-Created by Gaurav Tandon on 8/03/2015.
 */
/**
 * hex conversion routines
 * @author rogerb
 * @version $Id: Hex.java 1 2014-06-07 22:37:15Z rhubarb-geek-nz $
 */
public class Hex
{
    static final char [] map={
            '0','1','2','3',
            '4','5','6','7',
            '8','9','A','B',
            'C','D','E','F'
    };

    public static String encode(byte[] a, int offset, int length)
    {
        char []sb= new char[length * 2];
        int i=0;

        while (0 != length--)
        {
            byte b = a[offset++];

            sb[i++]=map[(b >> 4) & 0xf];
            sb[i++]=map[b & 0xf];
        }

        return String.valueOf(sb,0,i);
    }

    public static byte[] decode(String s)
    {
        byte[] a = new byte[s.length() >> 1];
        int i = 0;
        int o = 0;

        while (i < s.length())
        {
            byte b = nybble(s.charAt(i++));
            if (b >= 0)
            {
                if (0==(o & 1))
                {
                    a[o>>1]|=(byte)(b<<4);
                }
                else
                {
                    a[o>>1]|=b;
                }
                o++;
            }
        }

        if (0==(o & 1))
        {
            o>>=1;

            if (o != a.length)
            {
                byte[]c=new byte[o];
                System.arraycopy(a,0,c,0,o);
                return c;
            }

            return a;
        }

        return null;
    }

    static byte nybble(char c)
    {
        if (c > 'F')
        {
            c -= 32;
        }

        if (c > '9')
        {
            c -= 7;
        }

        if ((c >= '0') && (c < ('0' + 16)))
        {
            return (byte)(c-'0');
        }

        return -1;
    }
}

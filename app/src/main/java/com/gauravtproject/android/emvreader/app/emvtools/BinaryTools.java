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
 * $Id: BinaryTools.java 1 2014-06-07 22:37:15Z rhubarb-geek-nz $
 */

package com.gauravtproject.android.emvreader.app.emvtools;

/**
 * Re-Created by Gaurav Tandon on 8/03/2015.
 */
/**
 * Binary tools for byte array manipulation
 * @author rogerb
 * @version $Id: BinaryTools.java 1 2014-06-07 22:37:15Z rhubarb-geek-nz $
 */

public class BinaryTools
{
    /**
     * convert a series of bytes into a long value
     * @param b buffer
     * @param off offset
     * @param len length
     * @return long value
     */
    public static long readLong(byte[]b,int off,int len)
    {
        long val=0;

        while (0!=(len--))
        {
            int x=(0xff & b[off++]);
            val=(val << 8)|x;
        }

        return val;
    }

    /**
     * convert a series of bytes into an int value
     * @param b buffer
     * @param off offset
     * @param len length
     * @return int value
     */
    public static int readInt(byte[]b,int off,int len)
    {
        return (int)readLong(b,off,len);
    }

    /**
     * get a subarray of bytes
     * @param data parent array
     * @param from offset
     * @param len number of bytes
     * @return subarray
     */
    public static byte[] bytesFrom(byte [] data,int from,int len)
    {
        byte [] res=new byte[len];
        System.arraycopy(data, from, res,0, len);
        return res;
    }

    /**
     * compare two subarrays of bytes
     * @param mac array one
     * @param x offset one
     * @param mac2 array two
     * @param y offset two
     * @param len number of bytes
     * @return true of match
     */
    public static boolean compareBytes(byte[] mac,int x,byte[] mac2,int y,int len)
    {
        while (0!=(len--))
        {
            if (mac[x++]!=mac2[y++])
            {
                return false;
            }
        }

        return true;
    }

    /**
     * concatenate a series of arrays
     * @param a array of arrays
     * @return single combined array
     */

    public static byte [] catenate(byte [][] a)
    {
        int i=0,j=0;

        while (i < a.length)
        {
            j+=a[i++].length;
        }

        byte [] r=new byte[j];

        i=0;
        j=0;

        while (i < a.length)
        {
            byte []b=a[i++];
            System.arraycopy(b,0,r,j,b.length);
            j+=b.length;
        }

        return r;
    }

    /**
     * write an int byte into a binary byte array
     * @param ba byte array
     * @param offset offset
     * @param length length of data
     * @param value value to write
     */
    public static void writeInt(byte[] ba, int offset, int length, int value)
    {
        offset+=length;

        while (0!=length--)
        {
            ba[--offset]=(byte)value;
            value>>=8;
        }
    }

    /**
     * write a long byte into a binary byte array
     * @param ba byte array
     * @param offset offset
     * @param length length of data
     * @param value value to write
     */
    public static void writeLong(byte[] ba, int offset, int length, long value)
    {
        offset+=length;

        while (0!=length--)
        {
            ba[--offset]=(byte)value;
            value>>=8;
        }
    }
}

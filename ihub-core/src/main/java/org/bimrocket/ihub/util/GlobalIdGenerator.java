/*
 * BIMROCKET
 *
 * Copyright (C) 2022, Ajuntament de Sant Feliu de Llobregat
 *
 * This program is licensed and may be used, modified and redistributed under
 * the terms of the European Public License (EUPL), either version 1.1 or (at
 * your option) any later version as soon as they are approved by the European
 * Commission.
 *
 * Alternatively, you may redistribute and/or modify this program under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either  version 3 of the License, or (at your option)
 * any later version.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the licenses for the specific language governing permissions, limitations
 * and more details.
 *
 * You should have received a copy of the EUPL1.1 and the LGPLv3 licenses along
 * with this program; if not, you may find them at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * http://www.gnu.org/licenses/
 * and
 * https://www.gnu.org/licenses/lgpl.txt
 */
package org.bimrocket.ihub.util;

import java.util.Base64;
import java.util.UUID;

/**
 *
 * @author realor
 */
public class GlobalIdGenerator
{
  static final String BASE64 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
  static final String IFCB64 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz_$";
  static final byte[] toIFCTable = new byte[128];
  static final byte[] fromIFCTable = new byte[128];

  static
  {
    for (int i = 0; i < 64; i++)
    {
      toIFCTable[(byte) BASE64.charAt(i)] = (byte) IFCB64.charAt(i);
      fromIFCTable[(byte) IFCB64.charAt(i)] = (byte) BASE64.charAt(i);
    }
  }

  public static String compress(String uuid)
  {
    byte[] base64 = hexStringToByteArray("0" + uuid.replaceAll("-", "") + "0");

    base64 = Base64.getEncoder().encode(base64);
    for (int i = 0; i < 22; i++)
    {
      base64[i] = toIFCTable[base64[i]];
    }
    return new String(base64, 0, 22);
  }

  public static String expand(String globalId)
  {
    byte[] base64 = globalId.getBytes();
    byte[] array = new byte[23];

    for (int i = 0; i < 22; i++)
    {
      array[i] = fromIFCTable[base64[i]];
    }
    array[22] = '0';
    base64 = Base64.getDecoder().decode(array);
    return byteArrayToHexString(base64).substring(1, 33);
  }

  public static String randomGlobalId()
  {
    return compress(UUID.randomUUID().toString());
  }

  public static byte[] hexStringToByteArray(String s)
  {
    int len = s.length();
    byte[] data = new byte[(len / 2)];
    for (int i = 0; i < len; i += 2)
    {
      data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
          + Character.digit(s.charAt(i + 1), 16));
    }
    return data;
  }

  public static String byteArrayToHexString(byte[] array)
  {
    StringBuilder sb = new StringBuilder(array.length * 2);
    for (byte b : array)
    {
      sb.append(String.format("%02x", b));
    }
    return sb.toString();
  }

  public static void main(String[] args)
  {
    System.out.println(GlobalIdGenerator.randomGlobalId());
  }
}

package fr.strada.screens.home

import java.util.Arrays

internal class ServerVersion {
    companion object {
        val CARDTYPE_DRIVER: Byte = 1
        val FILE_IC = 5
        val FILE_ICC = 2
        val FILE_TACHO_Application_Identifier = 1281
        val FILE_TACHO_CA_Certificate = 49416
        val FILE_TACHO_Card_Certificate = 49408
        val FILE_TACHO_Card_Download = 1294
        val FILE_TACHO_Control_Activity_Data = 1288
        val FILE_TACHO_Current_Usage = 1287
        val FILE_TACHO_Driver_Activity_Data = 1284
        val FILE_TACHO_Driving_Licence_Info = 1313
        val FILE_TACHO_Events_Data = 1282
        val FILE_TACHO_Faults_Data = 1283
        val FILE_TACHO_Identification = 1312
        val FILE_TACHO_Places = 1286
        val FILE_TACHO_Specific_Conditions = 1314
        val FILE_TACHO_Vehicles_Used = 1285

        

        fun ChangeDirectory(paramString: String): ByteArray {
            val arrayOfByte = ByteArray(paramString.length + 6)
            arrayOfByte[0] = 0
            arrayOfByte[1] = -92
            arrayOfByte[2] = 4
            arrayOfByte[3] = 12
            arrayOfByte[4] = Convert.ToByte(paramString.length + 1)
            arrayOfByte[5] = -1
            arrayOfByte[6] = 84
            arrayOfByte[7] = 65
            arrayOfByte[8] = 67
            arrayOfByte[9] = 72
            arrayOfByte[10] = 79
            return arrayOfByte
        }

        fun ComputeDigitalSignature(): ByteArray {
            return byteArrayOf(0, 42, -98, -102, -128)
        }

        fun Info(
            paramArrayOfByte: ByteArray,
            paramHolder1: Holder,
            paramHolder2: Holder,
            paramHolder3: Holder
        ): Boolean {
            val i = paramArrayOfByte.size
            if (i >= 2) {
                val j = i - 2
                paramHolder2.Put(paramArrayOfByte[j])
                paramHolder3.Put(paramArrayOfByte[i - 1])
                paramHolder1.Put(Arrays.copyOfRange(paramArrayOfByte, 0, j))
                return true
            }
            return false
        }

        fun PerformHashOfFile(): ByteArray {
            return byteArrayOf(-128, 42, -112, 0, 0)
        }

        fun ReadFile(paramInteger: Int, paramByte: Byte): ByteArray {
            return byteArrayOf(
                0,
                -80,
                Convert.ToByte((paramInteger and 0xFF00).ushr(8)),
                Convert.ToByte(paramInteger and 0xFF),
                paramByte
            )
        }

        fun SelectFile(paramInteger: Int): ByteArray {
            return byteArrayOf(
                0,
                -92,
                2,
                12,
                2,
                Convert.ToByte((paramInteger and 0xFF00).ushr(8)),
                Convert.ToByte(paramInteger and 0xFF)
            )
        }
    }

    /* static byte[] UpdateBinary(long paramLong)
  {
    return new byte[] { 0, -42, 0, 0, 4, (byte)(int)((0xFFFFFFFFFF000000 & paramLong) >>> 24), (byte)(int)((0xFF0000 & paramLong) >>> 16), (byte)(int)((0xFF00 & paramLong) >>> 8), (byte)(int)(paramLong & 0xFF) };
  }*/
}

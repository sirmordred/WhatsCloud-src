package app.mordred.whatscloud.util

import android.content.Context
import java.security.Security
import java.io.*
import java.lang.Exception
import java.nio.charset.StandardCharsets
import java.util.zip.Inflater
import java.util.zip.InflaterInputStream
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


class DbDecrypter(ctx: Context) {

    private var context: Context = ctx

    companion object {
        val scProvider: Int = Security.insertProviderAt(org.spongycastle.jce.provider.BouncyCastleProvider(), 1)
    }

    fun decrypt(keyFilePath: String, wpDbFilePath: String): Boolean {
        if (scProvider == -1) {
            return false
        }

        if (!File(keyFilePath).isFile) {
            return false
        } else if (File(keyFilePath).length() != 158L) {
            return false
        } else if (!File(wpDbFilePath).isFile) {
            return false
        }

        val keyIn = FileInputStream(keyFilePath)
        val wpDbIn = BufferedInputStream(FileInputStream(wpDbFilePath))

        val keyData = ByteArray(158)
        keyIn.read(keyData)
        val t1 = ByteArray(32)
        System.arraycopy(keyData, 30, t1, 0, 32)
        val key = ByteArray(32)
        System.arraycopy(keyData, 126, key, 0, 32)
        keyIn.close()

        val c12Data = ByteArray(67)
        wpDbIn.read(c12Data)
        val t2 = ByteArray(32)
        System.arraycopy(c12Data, 3, t2, 0, 32)
        val iv = ByteArray(16)
        System.arraycopy(c12Data, 51, iv, 0, 16)

        if (String(t1, StandardCharsets.US_ASCII) != String(t2, StandardCharsets.US_ASCII)) {
            return false
        }

        val wpDbFileLength = wpDbIn.available()

        val temporaryFilePath: String = context.filesDir.absolutePath + File.separator + (System.currentTimeMillis() / 1000L).toInt().toString() + "-msgstore.enc"
        val temporaryFile = File(temporaryFilePath)

        val tempRafFile = RandomAccessFile(temporaryFile, "rw")


        val tempBuffer = ByteArray(1024)
        var buf: Int
        do {
            buf = wpDbIn.read(tempBuffer)
            if (buf == -1) {
                break
            }
            tempRafFile.write(tempBuffer, 0, buf)
        } while(true)

        tempRafFile.setLength((wpDbFileLength - 20).toLong())
        tempRafFile.close()
        wpDbIn.close()

        val pdbSt = BufferedInputStream(FileInputStream(temporaryFile))

        val cipher: Cipher = Cipher.getInstance("AES/GCM/NoPadding", "SC")

        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"), IvParameterSpec(iv))
        val cipherStream = CipherInputStream(pdbSt, cipher)

        val cryptOutput = InflaterInputStream(cipherStream, Inflater(false))

        val decryptedDbFilePath: String = context.filesDir.absolutePath + File.separator + "msgstore.db"
        val inflateBuffer = FileOutputStream(decryptedDbFilePath)

        try {

            cryptOutput.use { input ->
                inflateBuffer.use { output ->
                    input.copyTo(output)
                }
            }

            inflateBuffer.flush()
            inflateBuffer.close()

            cipherStream.close()
        } catch (ex: Exception) {
            ex.printStackTrace()
            return false
        }

        temporaryFile.delete()

        val sqlDb = FileInputStream(decryptedDbFilePath)
        val sqlData = ByteArray(6)
        sqlDb.read(sqlData)
        val ms = ByteArray(6)
        System.arraycopy(sqlData, 0, ms, 0, 6)
        sqlDb.close()

        if (String(ms, StandardCharsets.US_ASCII).toLowerCase() != "sqlite") {
            File(decryptedDbFilePath).delete()
            return false
        }

        return true
    }
}
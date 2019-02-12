package app.mordred.whatscloud

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import java.io.File
import java.io.FileOutputStream

class DbReader(ctx: Context, dbFName: String) {

    private var dbFileName: String = ""
    private var dbFilePath: String = ""

    companion object {
        private val msgQuery: String = "SELECT data FROM 'messages' WHERE key_from_me = 1 AND data IS NOT NULL"
        private var mDataBase: SQLiteDatabase? = null

    }

    private fun connectToDatabase() {
        if (File(dbFilePath).isFile && (mDataBase == null || !mDataBase!!.isOpen)) {
            mDataBase = SQLiteDatabase.openDatabase(dbFilePath, null, SQLiteDatabase.OPEN_READONLY);
        }
    }

    fun closeDatabase() {
        if (mDataBase != null && mDataBase!!.isOpen) {
            mDataBase!!.close()
        }
    }

    init {
        dbFileName = dbFName
        dbFilePath = ctx.filesDir.absolutePath + File.separator + dbFileName
        copyDatabaseFromAsset(ctx)
        connectToDatabase()
    }

    fun getMessages(optQuery: String?): MutableList<String> {
        val list: MutableList<String> = ArrayList()
        if (mDataBase != null && mDataBase!!.isOpen) {
            val result = mDataBase!!.rawQuery(optQuery ?: msgQuery, null)
            if (result != null) {
                while (result.moveToNext()) {
                    list.add(result.getString(0))
                    System.out.println("MSG: " + result.getString(0))
                }
                result.close()
            }
        }
        return list
    }

    private fun copyDatabaseFromAsset(ctx: Context) {
        val inputStream = ctx.assets.open(dbFileName)
        val outputStream = FileOutputStream(dbFilePath)

        inputStream.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }

        outputStream.flush()
        outputStream.close()
        inputStream.close()
    }
}
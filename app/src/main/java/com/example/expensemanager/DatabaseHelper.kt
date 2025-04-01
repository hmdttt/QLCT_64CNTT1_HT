package com.example.expensemanager

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_NAME = "expense_manager.db"
        private const val DATABASE_VERSION = 1

        private const val TABLE_TRANSACTIONS = "transactions"
        private const val COLUMN_ID = "id"
        private const val COLUMN_AMOUNT = "amount"
        private const val COLUMN_CATEGORY = "category"
        private const val COLUMN_TYPE = "type" // "income" hoặc "expense"
        private const val COLUMN_DATE = "date"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableSQL = "CREATE TABLE $TABLE_TRANSACTIONS (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COLUMN_AMOUNT REAL," +
                "$COLUMN_CATEGORY TEXT," +
                "$COLUMN_TYPE TEXT," +
                "$COLUMN_DATE TEXT" +
                ")"
        db.execSQL(createTableSQL)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS expense")
        onCreate(db)
    }

    fun adTransaction(amount: Double, category: String, type: String, date: String): Boolean {
        val db = writableDatabase
        val values = ContentValues()
        values.put(COLUMN_AMOUNT, amount)
        values.put(COLUMN_CATEGORY, category)
        values.put(COLUMN_TYPE, type)
        values.put(COLUMN_DATE, date)

        val result = db.insert(TABLE_TRANSACTIONS, null, values)
        db.close()
        return result != -1L
    }
}
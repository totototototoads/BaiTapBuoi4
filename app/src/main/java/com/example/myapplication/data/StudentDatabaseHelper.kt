package com.example.myapplication.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class StudentDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "student_management.db"
        private const val DATABASE_VERSION = 1

        private const val TABLE_STUDENTS = "students"
        private const val COLUMN_ID = "id"
        private const val COLUMN_STUDENT_ID = "student_id"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_EMAIL = "email"
        private const val COLUMN_AVATAR = "avatar"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_STUDENTS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_STUDENT_ID TEXT NOT NULL,
                $COLUMN_NAME TEXT NOT NULL,
                $COLUMN_EMAIL TEXT NOT NULL,
                $COLUMN_AVATAR TEXT
            )
        """.trimIndent()
        db.execSQL(createTable)

        seedData(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_STUDENTS")
        onCreate(db)
    }

    private fun seedData(db: SQLiteDatabase) {
        val sampleStudents = listOf(
            Student(studentId = "SV001", name = "Nguyễn Văn A", email = "nguyenvana@gmail.com", avatar = "https://picsum.photos/200/200?random=1"),
            Student(studentId = "SV002", name = "Trần Thị B", email = "tranthib@gmail.com", avatar = "https://picsum.photos/200/200?random=2"),
            Student(studentId = "SV003", name = "Lê Hoàng C", email = "lehoangc@gmail.com", avatar = "https://picsum.photos/200/200?random=3")
        )

        for (student in sampleStudents) {
            val values = ContentValues().apply {
                put(COLUMN_STUDENT_ID, student.studentId)
                put(COLUMN_NAME, student.name)
                put(COLUMN_EMAIL, student.email)
                put(COLUMN_AVATAR, student.avatar)
            }
            db.insert(TABLE_STUDENTS, null, values)
        }
    }

    fun getAllStudents(): List<Student> {
        val list = mutableListOf<Student>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_STUDENTS,
            null, null, null, null, null,
            "$COLUMN_ID DESC"
        )

        cursor.use {
            if (it.moveToFirst()) {
                val idIndex = it.getColumnIndexOrThrow(COLUMN_ID)
                val studentIdIndex = it.getColumnIndexOrThrow(COLUMN_STUDENT_ID)
                val nameIndex = it.getColumnIndexOrThrow(COLUMN_NAME)
                val emailIndex = it.getColumnIndexOrThrow(COLUMN_EMAIL)
                val avatarIndex = it.getColumnIndexOrThrow(COLUMN_AVATAR)

                do {
                    val student = Student(
                        id = it.getLong(idIndex),
                        studentId = it.getString(studentIdIndex) ?: "",
                        name = it.getString(nameIndex) ?: "",
                        email = it.getString(emailIndex) ?: "",
                        avatar = it.getString(avatarIndex) ?: ""
                    )
                    list.add(student)
                } while (it.moveToNext())
            }
        }
        return list
    }

    fun searchStudents(query: String): List<Student> {
        if (query.isBlank()) return getAllStudents()

        val list = mutableListOf<Student>()
        val db = readableDatabase
        val selection = "$COLUMN_NAME LIKE ? OR $COLUMN_STUDENT_ID LIKE ?"
        val selectionArgs = arrayOf("%$query%", "%$query%")

        val cursor = db.query(
            TABLE_STUDENTS,
            null,
            selection,
            selectionArgs,
            null, null,
            "$COLUMN_ID DESC"
        )

        cursor.use {
            if (it.moveToFirst()) {
                val idIndex = it.getColumnIndexOrThrow(COLUMN_ID)
                val studentIdIndex = it.getColumnIndexOrThrow(COLUMN_STUDENT_ID)
                val nameIndex = it.getColumnIndexOrThrow(COLUMN_NAME)
                val emailIndex = it.getColumnIndexOrThrow(COLUMN_EMAIL)
                val avatarIndex = it.getColumnIndexOrThrow(COLUMN_AVATAR)

                do {
                    val student = Student(
                        id = it.getLong(idIndex),
                        studentId = it.getString(studentIdIndex) ?: "",
                        name = it.getString(nameIndex) ?: "",
                        email = it.getString(emailIndex) ?: "",
                        avatar = it.getString(avatarIndex) ?: ""
                    )
                    list.add(student)
                } while (it.moveToNext())
            }
        }
        return list
    }

    fun getStudentCount(): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_STUDENTS", null)
        var count = 0
        cursor.use {
            if (it.moveToFirst()) {
                count = it.getInt(0)
            }
        }
        return count
    }

    fun getStudentById(id: Long): Student? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_STUDENTS,
            null,
            "$COLUMN_ID = ?",
            arrayOf(id.toString()),
            null, null, null
        )

        cursor.use {
            if (it.moveToFirst()) {
                return Student(
                    id = it.getLong(it.getColumnIndexOrThrow(COLUMN_ID)),
                    studentId = it.getString(it.getColumnIndexOrThrow(COLUMN_STUDENT_ID)) ?: "",
                    name = it.getString(it.getColumnIndexOrThrow(COLUMN_NAME)) ?: "",
                    email = it.getString(it.getColumnIndexOrThrow(COLUMN_EMAIL)) ?: "",
                    avatar = it.getString(it.getColumnIndexOrThrow(COLUMN_AVATAR)) ?: ""
                )
            }
        }
        return null
    }

    fun insertStudent(student: Student): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_STUDENT_ID, student.studentId)
            put(COLUMN_NAME, student.name)
            put(COLUMN_EMAIL, student.email)
            put(COLUMN_AVATAR, student.avatar)
        }
        return db.insert(TABLE_STUDENTS, null, values)
    }

    fun updateStudent(student: Student): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_STUDENT_ID, student.studentId)
            put(COLUMN_NAME, student.name)
            put(COLUMN_EMAIL, student.email)
            put(COLUMN_AVATAR, student.avatar)
        }
        return db.update(TABLE_STUDENTS, values, "$COLUMN_ID = ?", arrayOf(student.id.toString()))
    }

    fun deleteStudent(id: Long): Int {
        val db = writableDatabase
        return db.delete(TABLE_STUDENTS, "$COLUMN_ID = ?", arrayOf(id.toString()))
    }
}

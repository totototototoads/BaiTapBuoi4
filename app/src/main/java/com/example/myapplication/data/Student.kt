package com.example.myapplication.data

import java.io.Serializable

data class Student(
    val id: Long = 0,
    val studentId: String,
    val name: String,
    val email: String,
    val avatar: String
) : Serializable

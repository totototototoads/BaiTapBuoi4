package com.example.myapplication.ui

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.ViewGroup
import android.view.Window
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.data.Student
import com.example.myapplication.databinding.DialogStudentDetailBinding

class StudentDetailDialog(
    context: Context,
    private val student: Student,
    private val onEditClicked: (Student) -> Unit
) : Dialog(context) {

    private lateinit var binding: DialogStudentDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = DialogStudentDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window?.setLayout(
            (context.resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding.tvDetailName.text = student.name
        binding.tvDetailStudentId.text = "${context.getString(R.string.label_student_id)}: ${student.studentId}"
        binding.tvDetailEmail.text = if (student.email.isNotBlank()) student.email else "N/A"

        if (student.avatar.isNotBlank()) {
            Glide.with(context)
                .load(student.avatar)
                .placeholder(R.drawable.ic_default_avatar)
                .error(R.drawable.ic_default_avatar)
                .into(binding.imgDetailAvatar)
        } else {
            binding.imgDetailAvatar.setImageResource(R.drawable.ic_default_avatar)
        }

        binding.btnDetailClose.setOnClickListener {
            dismiss()
        }

        binding.btnDetailEdit.setOnClickListener {
            dismiss()
            onEditClicked(student)
        }
    }
}

package com.example.myapplication.ui

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.data.Student
import com.example.myapplication.data.StudentDatabaseHelper
import com.example.myapplication.databinding.ActivityAddEditStudentBinding

class AddEditStudentActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_STUDENT = "extra_student"
    }

    private lateinit var binding: ActivityAddEditStudentBinding
    private lateinit var dbHelper: StudentDatabaseHelper
    private var currentStudent: Student? = null

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            try {
                contentResolver.takePersistableUriPermission(
                    it,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
            binding.etAvatar.setText(it.toString())
            loadAvatarPreview(it.toString())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditStudentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = StudentDatabaseHelper(this)

        @Suppress("DEPRECATION")
        currentStudent = intent.getSerializableExtra(EXTRA_STUDENT) as? Student

        setupToolbar()
        populateData()
        setupListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = if (currentStudent == null) {
            getString(R.string.title_add_student)
        } else {
            getString(R.string.title_edit_student)
        }
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun populateData() {
        currentStudent?.let { student ->
            binding.etName.setText(student.name)
            binding.etStudentId.setText(student.studentId)
            binding.etEmail.setText(student.email)
            binding.etAvatar.setText(student.avatar)
            loadAvatarPreview(student.avatar)
        }
    }

    private fun setupListeners() {
        binding.btnPickImage.setOnClickListener {
            getContent.launch("image/*")
        }

        binding.etAvatar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                loadAvatarPreview(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnSave.setOnClickListener {
            validateAndSave()
        }
    }

    private fun loadAvatarPreview(urlOrUri: String) {
        if (urlOrUri.isNotBlank()) {
            Glide.with(this)
                .load(urlOrUri)
                .placeholder(R.drawable.ic_default_avatar)
                .error(R.drawable.ic_default_avatar)
                .into(binding.imgAvatarPreview)
        } else {
            binding.imgAvatarPreview.setImageResource(R.drawable.ic_default_avatar)
        }
    }

    private fun validateAndSave() {
        val name = binding.etName.text.toString().trim()
        val studentId = binding.etStudentId.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val avatar = binding.etAvatar.text.toString().trim()

        if (name.isEmpty() || studentId.isEmpty()) {
            Toast.makeText(this, getString(R.string.msg_fill_required), Toast.LENGTH_SHORT).show()
            return
        }

        val isEdit = currentStudent != null
        val dialogTitle = if (isEdit) getString(R.string.confirm_edit_title) else getString(R.string.confirm_add_title)
        val dialogMessage = if (isEdit) getString(R.string.confirm_edit_message) else getString(R.string.confirm_add_message)

        AlertDialog.Builder(this)
            .setTitle(dialogTitle)
            .setMessage(dialogMessage)
            .setPositiveButton(getString(R.string.btn_yes)) { _, _ ->
                saveToDatabase(name, studentId, email, avatar)
            }
            .setNegativeButton(getString(R.string.btn_no), null)
            .show()
    }

    private fun saveToDatabase(name: String, studentId: String, email: String, avatar: String) {
        val student = Student(
            id = currentStudent?.id ?: 0,
            studentId = studentId,
            name = name,
            email = email,
            avatar = avatar
        )

        if (currentStudent == null) {
            dbHelper.insertStudent(student)
        } else {
            dbHelper.updateStudent(student)
        }

        Toast.makeText(this, getString(R.string.msg_student_saved), Toast.LENGTH_SHORT).show()
        setResult(RESULT_OK)
        finish()
    }
}

package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.adapter.StudentAdapter
import com.example.myapplication.data.Student
import com.example.myapplication.data.StudentDatabaseHelper
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.ui.AddEditStudentActivity
import com.example.myapplication.ui.StudentDetailDialog

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var dbHelper: StudentDatabaseHelper
    private lateinit var adapter: StudentAdapter

    private val addEditLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            loadStudents()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = StudentDatabaseHelper(this)

        setupRecyclerView()
        setupListeners()
        loadStudents()
    }

    private fun setupRecyclerView() {
        adapter = StudentAdapter(
            students = emptyList(),
            onItemClick = { student ->
                showStudentDetailDialog(student)
            },
            onEditClick = { student ->
                openEditStudent(student)
            },
            onDeleteClick = { student ->
                showDeleteConfirmationDialog(student)
            }
        )

        binding.rvStudents.layoutManager = LinearLayoutManager(this)
        binding.rvStudents.adapter = adapter
    }

    private fun setupListeners() {
        binding.fabAdd.setOnClickListener {
            val intent = Intent(this, AddEditStudentActivity::class.java)
            addEditLauncher.launch(intent)
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterStudents(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadStudents() {
        val count = dbHelper.getStudentCount()
        binding.tvStudentCount.text = getString(R.string.student_count, count)

        val query = binding.etSearch.text.toString().trim()
        filterStudents(query)
    }

    private fun filterStudents(query: String) {
        val count = dbHelper.getStudentCount()
        binding.tvStudentCount.text = getString(R.string.student_count, count)

        val students = if (query.isBlank()) {
            dbHelper.getAllStudents()
        } else {
            dbHelper.searchStudents(query)
        }

        adapter.updateData(students)

        if (students.isEmpty()) {
            binding.tvEmpty.visibility = View.VISIBLE
            binding.rvStudents.visibility = View.GONE
        } else {
            binding.tvEmpty.visibility = View.GONE
            binding.rvStudents.visibility = View.VISIBLE
        }
    }

    private fun showStudentDetailDialog(student: Student) {
        val dialog = StudentDetailDialog(this, student) { selectedStudent ->
            openEditStudent(selectedStudent)
        }
        dialog.show()
    }

    private fun openEditStudent(student: Student) {
        val intent = Intent(this, AddEditStudentActivity::class.java).apply {
            putExtra(AddEditStudentActivity.EXTRA_STUDENT, student)
        }
        addEditLauncher.launch(intent)
    }

    private fun showDeleteConfirmationDialog(student: Student) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.confirm_delete_title))
            .setMessage(getString(R.string.confirm_delete_message))
            .setPositiveButton(getString(R.string.btn_yes)) { _, _ ->
                dbHelper.deleteStudent(student.id)
                Toast.makeText(this, getString(R.string.msg_student_deleted), Toast.LENGTH_SHORT).show()
                loadStudents()
            }
            .setNegativeButton(getString(R.string.btn_no), null)
            .show()
    }
}

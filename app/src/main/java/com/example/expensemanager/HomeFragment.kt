package com.example.expensemanager

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.expensemanager.models.Transaction
import com.google.firebase.firestore.FirebaseFirestore

import android.app.DatePickerDialog
import android.widget.ImageButton
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private var selectedCategory: String? = null
    private var selectedType: String? = null // Mặc định là chi


    private lateinit var btnTienThu: Button
    private lateinit var btnTienChi: Button
    private lateinit var layoutTienThu: LinearLayout
    private lateinit var layoutTienChi: LinearLayout

    private lateinit var btnPrevDate: ImageButton
    private lateinit var btnNextDate: ImageButton
    private var selectedCalendar: Calendar = Calendar.getInstance()

    private lateinit var edtSoTien: EditText
    private lateinit var edtNote: EditText
    private lateinit var edtDate: EditText

    private lateinit var btnAnUong: LinearLayout
    private lateinit var btnPhiAnChoi: LinearLayout
    private lateinit var btnGiaDung: LinearLayout
    private lateinit var btnYTe: LinearLayout
    private lateinit var btnMyPham: LinearLayout
    private lateinit var btnGiaoDuc: LinearLayout
    private lateinit var btnTienNha: LinearLayout
    private lateinit var btnLienLac: LinearLayout
    private lateinit var btnTietKiem: LinearLayout
    private lateinit var btnDiLai: LinearLayout
    private lateinit var btnQuanAo: LinearLayout

    private lateinit var btnLuong: LinearLayout
    private lateinit var btnThuong: LinearLayout
    private lateinit var btnPhuCap: LinearLayout

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =  inflater.inflate(R.layout.fragment_home, container, false)

        // Khởi tạo DatabaseHelper

        // Ánh xạ view
        edtSoTien = view.findViewById(R.id.etAmount)
        edtNote = view.findViewById(R.id.etNote)
        edtDate = view.findViewById(R.id.etDate)
        updateDateEditText()


        btnPrevDate = view.findViewById(R.id.btnPreviousDate)
        btnNextDate = view.findViewById(R.id.btnNextDate)


        btnAnUong = view.findViewById(R.id.btnAnuong)
        btnDiLai = view.findViewById(R.id.btnDilai)
        btnQuanAo = view.findViewById(R.id.btnQuanao)
        btnPhiAnChoi = view.findViewById(R.id.btnPhianchoi)
        btnGiaDung = view.findViewById(R.id.btnGiadung)
        btnYTe = view.findViewById(R.id.btnYte)
        btnMyPham = view.findViewById(R.id.btnMypham)
        btnGiaoDuc = view.findViewById(R.id.btnGiaoduc)
        btnTienNha = view.findViewById(R.id.btnTiennha)
        btnLienLac = view.findViewById(R.id.btnLienlac)
        btnTietKiem = view.findViewById(R.id.btnTietkiem)

        btnLuong = view.findViewById(R.id.btnTienluong)
        btnThuong = view.findViewById(R.id.btnTienthuong)
        btnPhuCap = view.findViewById(R.id.btnPhucap)

        btnTienThu = view.findViewById(R.id.btnTienthu)
        btnTienChi = view.findViewById(R.id.btnTienchi)
        layoutTienThu = view.findViewById(R.id.LinearLayoutThu)
        layoutTienChi = view.findViewById(R.id.LinearLayoutChi)

        layoutTienChi.visibility = View.VISIBLE
        layoutTienThu.visibility = View.GONE

        edtDate.setOnClickListener {
            val year = selectedCalendar.get(Calendar.YEAR)
            val month = selectedCalendar.get(Calendar.MONTH)
            val day = selectedCalendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(requireContext(), { _, y, m, d ->
                selectedCalendar.set(y, m, d)
                updateDateEditText()
            }, year, month, day).show()
        }

        btnPrevDate.setOnClickListener {
            selectedCalendar.add(Calendar.DAY_OF_MONTH, -1)
            updateDateEditText()
        }

        btnNextDate.setOnClickListener {
            selectedCalendar.add(Calendar.DAY_OF_MONTH, 1)
            updateDateEditText()
        }


        btnTienThu.setOnClickListener {
            layoutTienThu.visibility = View.VISIBLE
            layoutTienChi.visibility = View.GONE
        }

        btnTienChi.setOnClickListener {
            layoutTienThu.visibility = View.GONE
            layoutTienChi.visibility = View.VISIBLE
        }

        btnAnUong.setOnClickListener() {
            resetCategoryViews()
            selectedCategory = "Ăn uống"
            selectedType = "expense"
            btnAnUong.isSelected = true
        }
        btnDiLai.setOnClickListener() {
            resetCategoryViews()
            selectedCategory = "Đi lại"
            selectedType = "expense"
            btnDiLai.isSelected = true
        }
        btnQuanAo.setOnClickListener() {
            resetCategoryViews()
            selectedCategory = "Quần áo"
            selectedType = "expense"
            btnQuanAo.isSelected = true
        }
        btnPhiAnChoi.setOnClickListener() {
            resetCategoryViews()
            selectedCategory = "Phí ăn chơi"
            selectedType = "expense"
            btnPhiAnChoi.isSelected = true
        }
        btnGiaDung.setOnClickListener() {
            resetCategoryViews()
            selectedCategory = "Gia dụng"
            selectedType = "expense"
            btnGiaDung.isSelected = true
        }
        btnYTe.setOnClickListener() {
            resetCategoryViews()
            selectedCategory = "Y tế"
            selectedType = "expense"
            btnYTe.isSelected = true
        }
        btnMyPham.setOnClickListener() {
            resetCategoryViews()
            selectedCategory = "Mỹ phẩm"
            selectedType = "expense"
            btnMyPham.isSelected = true
        }
        btnGiaoDuc.setOnClickListener() {
            resetCategoryViews()
            selectedCategory = "Giáo dục"
            selectedType = "expense"
            btnGiaoDuc.isSelected = true
        }
        btnTienNha.setOnClickListener() {
            resetCategoryViews()
            selectedCategory = "Tiền nhà"
            selectedType = "expense"
            btnTienNha.isSelected = true
        }
        btnLienLac.setOnClickListener() {
            resetCategoryViews()
            selectedCategory = "Liên lạc"
            selectedType = "expense"
            btnLienLac.isSelected = true
        }
        btnTietKiem.setOnClickListener() {
            resetCategoryViews()
            selectedCategory = "Tiết kiệm"
            selectedType = "expense"
            btnTietKiem.isSelected = true
        }
        btnLuong.setOnClickListener() {
            resetCategoryViews()
            selectedCategory = "Lương"
            selectedType = "income"
            btnLuong.isSelected = true
        }
        btnThuong.setOnClickListener() {
            resetCategoryViews()
            selectedCategory = "Thưởng"
            selectedType = "income"
            btnThuong.isSelected = true
        }
        btnPhuCap.setOnClickListener() {
            resetCategoryViews()
            selectedCategory = "Phụ cấp"
            selectedType = "income"
            btnPhuCap.isSelected = true
        }

        val btnAddExpense = view.findViewById<Button>(R.id.btnAddExpense)
        val btnAddIncome = view.findViewById<Button>(R.id.btnAddIncome)

        btnAddExpense.setOnClickListener {
            val amount = edtSoTien.text.toString().toIntOrNull()
            val note = edtNote.text.toString()
            val date = edtDate.text.toString()

            if (amount == null || selectedCategory == null || date.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            saveTransaction(amount, note, date, selectedType!!, selectedCategory!!)
        }

        btnAddIncome.setOnClickListener {
            val amount = edtSoTien.text.toString().toIntOrNull()
            val note = edtNote.text.toString()
            val date = edtDate.text.toString()

            if (amount == null || selectedCategory == null || date.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            saveTransaction(amount, note, date, selectedType!!, selectedCategory!!)
        }



        return view
    }

    private fun saveTransaction(amount: Int, note: String, date: String, type: String, category: String) {
        Log.d("FirestoreDebug", "Saving transaction: amount=$amount, note=$note, date=$date, type=$type, category=$category")

        val db = FirebaseFirestore.getInstance()
        val transaction = Transaction(amount, note, date, type, category)

        db.collection("transactions")
            .add(transaction)
            .addOnSuccessListener {
                Log.d("FirestoreDebug", "Transaction saved successfully!")
                Toast.makeText(requireContext(), "Đã lưu giao dịch!", Toast.LENGTH_SHORT).show()
                resetInput()
            }
            .addOnFailureListener {
                Log.e("FirestoreDebug", "Error saving transaction: ${it.message}")
                Toast.makeText(requireContext(), "Lỗi khi lưu dữ liệu: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun resetCategoryViews() {
        val categoryViews = listOf(
            btnAnUong, btnDiLai, btnQuanAo, btnGiaDung, btnMyPham,
            btnPhiAnChoi, btnYTe, btnGiaoDuc, btnTienNha, btnLienLac, btnTietKiem,
            btnLuong, btnThuong, btnPhuCap
        )
        for (view in categoryViews) {
            view.isSelected = false
        }
    }


    private fun resetInput() {
        edtSoTien.setText("")
        edtNote.setText("")
        edtDate.setText("")
        selectedCategory = null
        resetCategoryViews()
    }

    private fun updateDateEditText() {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        edtDate.setText(sdf.format(selectedCalendar.time))
    }


}
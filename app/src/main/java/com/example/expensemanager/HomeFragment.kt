package com.example.expensemanager

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

class HomeFragment : Fragment() {

    private var selectedCategory: String? = null
    private var selectedType: String? = null // Mặc định là chi


    private lateinit var btnTienThu: Button
    private lateinit var btnTienChi: Button
    private lateinit var layoutTienThu: LinearLayout
    private lateinit var layoutTienChi: LinearLayout


    private lateinit var edtSoTien: EditText
    private lateinit var edtNote: EditText
    private lateinit var edtDate: EditText

    private lateinit var btnAnUong: Button
    private lateinit var btnPhiAnChoi: Button
    private lateinit var btnGiaDung: Button
    private lateinit var btnYTe: Button
    private lateinit var btnMyPham: Button
    private lateinit var btnGiaoDuc: Button
    private lateinit var btnTienNha: Button
    private lateinit var btnLienLac: Button
    private lateinit var btnTietKiem: Button
    private lateinit var btnDiLai: Button
    private lateinit var btnQuanAo: Button

    private lateinit var btnLuong: Button
    private lateinit var btnThuong: Button
    private lateinit var btnPhuCap: Button

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

        btnTienThu.setOnClickListener {
            layoutTienThu.visibility = View.VISIBLE
            layoutTienChi.visibility = View.GONE
        }

        btnTienChi.setOnClickListener {
            layoutTienThu.visibility = View.GONE
            layoutTienChi.visibility = View.VISIBLE
        }

        btnAnUong.setOnClickListener() {
            resetCategoryButtons()
            selectedCategory = "Ăn uống"
            selectedType = "expense"
            btnAnUong.setBackgroundColor(resources.getColor(android.R.color.holo_blue_light)) // Màu nổi bật
            Toast.makeText(requireContext(), "Đã chọn: $selectedCategory", Toast.LENGTH_SHORT).show()
        }
        btnDiLai.setOnClickListener() {
            resetCategoryButtons()
            selectedCategory = "Đi lại"
            selectedType = "expense"
            btnDiLai.setBackgroundColor(resources.getColor(android.R.color.holo_blue_light)) // Màu nổi bật
            Toast.makeText(requireContext(), "Đã chọn: $selectedCategory", Toast.LENGTH_SHORT).show()
        }
        btnQuanAo.setOnClickListener() {
            resetCategoryButtons()
            selectedCategory = "Quần áo"
            selectedType = "expense"
            btnQuanAo.setBackgroundColor(resources.getColor(android.R.color.holo_blue_light)) // Màu nổi bật
            Toast.makeText(requireContext(), "Đã chọn: $selectedCategory", Toast.LENGTH_SHORT).show()
        }
        btnPhiAnChoi.setOnClickListener() {
            resetCategoryButtons()
            selectedCategory = "Phí ăn chơi"
            selectedType = "expense"
            btnPhiAnChoi.setBackgroundColor(resources.getColor(android.R.color.holo_blue_light)) // Màu nổi bật
            Toast.makeText(requireContext(), "Đã chọn: $selectedCategory", Toast.LENGTH_SHORT).show()
        }
        btnGiaDung.setOnClickListener() {
            resetCategoryButtons()
            selectedCategory = "Gia dụng"
            selectedType = "expense"
            btnGiaDung.setBackgroundColor(resources.getColor(android.R.color.holo_blue_light)) // Màu nổi bật
            Toast.makeText(requireContext(), "Đã chọn: $selectedCategory", Toast.LENGTH_SHORT).show()
        }
        btnYTe.setOnClickListener() {
            resetCategoryButtons()
            selectedCategory = "Y tế"
            selectedType = "expense"
            btnYTe.setBackgroundColor(resources.getColor(android.R.color.holo_blue_light)) // Màu nổi bật
            Toast.makeText(requireContext(), "Đã chọn: $selectedCategory", Toast.LENGTH_SHORT).show()
        }
        btnMyPham.setOnClickListener() {
            resetCategoryButtons()
            selectedCategory = "Mỹ phẩm"
            selectedType = "expense"
            btnMyPham.setBackgroundColor(resources.getColor(android.R.color.holo_blue_light)) // Màu nổi bật
            Toast.makeText(requireContext(), "Đã chọn: $selectedCategory", Toast.LENGTH_SHORT).show()
        }
        btnGiaoDuc.setOnClickListener() {
            resetCategoryButtons()
            selectedCategory = "Giáo dục"
            selectedType = "expense"
            btnGiaoDuc.setBackgroundColor(resources.getColor(android.R.color.holo_blue_light)) // Màu nổi bật
            Toast.makeText(requireContext(), "Đã chọn: $selectedCategory", Toast.LENGTH_SHORT).show()
        }
        btnTienNha.setOnClickListener() {
            resetCategoryButtons()
            selectedCategory = "Tiền nhà"
            selectedType = "expense"
            btnTienNha.setBackgroundColor(resources.getColor(android.R.color.holo_blue_light)) // Màu nổi bật
            Toast.makeText(requireContext(), "Đã chọn: $selectedCategory", Toast.LENGTH_SHORT).show()
        }
        btnLienLac.setOnClickListener() {
            resetCategoryButtons()
            selectedCategory = "Liên lạc"
            selectedType = "expense"
            btnLienLac.setBackgroundColor(resources.getColor(android.R.color.holo_blue_light)) // Màu nổi bật
            Toast.makeText(requireContext(), "Đã chọn: $selectedCategory", Toast.LENGTH_SHORT).show()
        }
        btnTietKiem.setOnClickListener() {
            resetCategoryButtons()
            selectedCategory = "Tiết kiệm"
            selectedType = "expense"
            btnTietKiem.setBackgroundColor(resources.getColor(android.R.color.holo_blue_light)) // Màu nổi bật
            Toast.makeText(requireContext(), "Đã chọn: $selectedCategory", Toast.LENGTH_SHORT).show()
        }
        btnLuong.setOnClickListener() {
            resetCategoryButtons()
            selectedCategory = "Lương"
            selectedType = "income"
            btnLuong.setBackgroundColor(resources.getColor(android.R.color.holo_blue_light)) // Màu nổi bật
            Toast.makeText(requireContext(), "Đã chọn: $selectedCategory", Toast.LENGTH_SHORT).show()
        }
        btnThuong.setOnClickListener() {
            resetCategoryButtons()
            selectedCategory = "Thưởng"
            selectedType = "income"
            btnThuong.setBackgroundColor(resources.getColor(android.R.color.holo_blue_light)) // Màu nổi bật
            Toast.makeText(requireContext(), "Đã chọn: $selectedCategory", Toast.LENGTH_SHORT).show()
        }
        btnPhuCap.setOnClickListener() {
            resetCategoryButtons()
            selectedCategory = "Phụ cấp"
            selectedType = "income"
            btnPhuCap.setBackgroundColor(resources.getColor(android.R.color.holo_blue_light)) // Màu nổi bật
            Toast.makeText(requireContext(), "Đã chọn: $selectedCategory", Toast.LENGTH_SHORT).show()
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


    private fun resetCategoryButtons() {
        val buttons = listOf(
            btnAnUong, btnDiLai, btnQuanAo, btnPhiAnChoi, btnGiaDung,
            btnYTe, btnMyPham, btnGiaoDuc, btnTienNha, btnLienLac, btnTietKiem,
            btnLuong, btnThuong, btnPhuCap
        )

        for (btn in buttons) {
            btn.setBackgroundResource(android.R.drawable.btn_default) // Reset về mặc định
        }
    }

    private fun resetInput() {
        edtSoTien.setText("")
        edtNote.setText("")
        edtDate.setText("")
        selectedCategory = null
        resetCategoryButtons()
    }



}
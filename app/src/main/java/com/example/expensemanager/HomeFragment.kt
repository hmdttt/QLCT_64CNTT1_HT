package com.example.expensemanager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment

class HomeFragment : Fragment() {

    private lateinit var btnTienThu: Button
    private lateinit var btnTienChi: Button
    private lateinit var layoutTienThu: GridLayout
    private lateinit var layoutTienChi: GridLayout

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var edtSoTien: EditText
    private lateinit var btnAnUong: Button
    private lateinit var btnDiLai: Button
    private lateinit var btnQuanAo: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =  inflater.inflate(R.layout.fragment_home, container, false)

        // Khởi tạo DatabaseHelper
        dbHelper = DatabaseHelper(requireContext())

        // Ánh xạ view
        edtSoTien = view.findViewById(R.id.etAmount)
        btnAnUong = view.findViewById(R.id.btnAnuong)
        btnDiLai = view.findViewById(R.id.btnDilai)
        btnQuanAo = view.findViewById(R.id.btnQuanao)

        // Xử lý sự kiện thêm khoản chi
        btnAnUong.setOnClickListener { themKhoan("Ăn uống", "expense") }
        btnDiLai.setOnClickListener { themKhoan("Đi lại", "expense") }
        btnQuanAo.setOnClickListener { themKhoan("Quần áo", "expense") }


        btnTienThu = view.findViewById(R.id.btnTienthu)
        btnTienChi = view.findViewById(R.id.btnTienchi)
        layoutTienThu = view.findViewById(R.id.gridLayoutTienthu)
        layoutTienChi = view.findViewById(R.id.gridLayoutTienChi)

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

        return view
    }

    private fun themKhoan(danhMuc: String, loai: String) {
        val soTienStr = edtSoTien.text.toString().trim()

        if (soTienStr.isEmpty()) {
            edtSoTien.error = "Vui lòng nhập số tiền"
            return
        }
        val soTien = soTienStr.toDouble()
        val date = "2021-10-10"

        val isSuccess = dbHelper.adTransaction(soTien, danhMuc, loai, date)
        if (isSuccess) {
            Toast.makeText(requireContext(), "Thêm khoản thành công", Toast.LENGTH_SHORT).show()
            edtSoTien.text.clear()
        } else {
            Toast.makeText(requireContext(), "Thêm khoản thất bại", Toast.LENGTH_SHORT).show()
        }

    }
}
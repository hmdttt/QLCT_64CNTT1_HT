package com.example.expensemanager

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

class TransactionSearchFragment : Fragment() {

    private lateinit var editTextSearch: EditText
    private lateinit var textIncome: TextView
    private lateinit var textExpense: TextView
    private lateinit var textTotal: TextView
    private lateinit var recyclerView: RecyclerView

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Gắn layout XML với fragment
        val view = inflater.inflate(R.layout.fragment_transaction_search, container, false)

        // Ánh xạ các view
        editTextSearch = view.findViewById(R.id.editTextSearch)
        textIncome = view.findViewById(R.id.textIncome)
        textExpense = view.findViewById(R.id.textExpense)
        textTotal = view.findViewById(R.id.textTotal)
        recyclerView = view.findViewById(R.id.recyclerViewResults)

        // Tạm ẩn RecyclerView ban đầu
        recyclerView.visibility = View.GONE

        // Bắt sự kiện người dùng nhập vào ô tìm kiếm
        editTextSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val keyword = s.toString()
                searchTransactions(keyword)
            }
        })

        return view
    }

    // Hàm xử lý tìm kiếm
    private fun searchTransactions(keyword: String) {
        // Tạm thời dùng số liệu giả
        val income = 1000000 // Giả lập thu nhập từ các giao dịch khớp
        val expense = 400000 // Giả lập chi phí từ các giao dịch khớp
        val total = income - expense

        // Cập nhật giao diện
        textIncome.text = "Thu nhập\n${income}đ"
        textExpense.text = "Chi phí\n${expense}đ"
        textTotal.text = "Tổng\n${if (total >= 0) "+" else "-"}${abs(total)}đ"

        // (Tuỳ chọn) nếu có kết quả thì hiện danh sách, ở bước sau bạn sẽ thêm dữ liệu thật
        recyclerView.visibility = View.VISIBLE
    }
}


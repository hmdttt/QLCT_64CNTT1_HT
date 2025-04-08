package com.example.expensemanager

import android.icu.util.Calendar
import android.os.Bundle
import android.text.SpannableStringBuilder
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ListView
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import com.example.expensemanager.models.Transaction
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.firestore.FirebaseFirestore
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import java.text.NumberFormat


class ReportFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var pieChart: PieChart
    private lateinit var expenseList: ListView
    private lateinit var edtDate: EditText
    private var isYearly: Boolean = false
    private var isExpense: Boolean = true
    private var transactionList = mutableListOf<Transaction>()
    private var selectedCalendar: Calendar = Calendar.getInstance()
    private lateinit var tvTotalIncome: TextView
    private lateinit var tvTotalExpense: TextView
    private lateinit var tvBalance: TextView



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_report, container, false)

        // Ánh xạ
        db = FirebaseFirestore.getInstance()
        pieChart = view.findViewById(R.id.pieChart)
        expenseList = view.findViewById(R.id.expenseList)
        edtDate = view.findViewById(R.id.edtDate)
        tvTotalIncome = view.findViewById(R.id.tvTotalIncome)
        tvTotalExpense = view.findViewById(R.id.tvTotalExpense)
        tvBalance = view.findViewById(R.id.tvBalance)



        val btnThunhap = view.findViewById<Button>(R.id.btnThunhap)
        val btnChitieu = view.findViewById<Button>(R.id.btnChitieu)
        val btnHangthang = view.findViewById<Button>(R.id.btnHangthang)
        val btnHangnam = view.findViewById<Button>(R.id.btnHangnam)
        val btnPrev = view.findViewById<ImageButton>(R.id.btnLeft)
        val btnNext = view.findViewById<ImageButton>(R.id.btnRight)


        // Xử lý chọn loại báo cáo
        btnHangthang.setOnClickListener {
            isYearly = false
            val month = selectedCalendar.get(Calendar.MONTH) + 1
            val year = selectedCalendar.get(Calendar.YEAR)
            edtDate.setText(String.format("%02d/%d", month, year))
            fetchData()
        }

        btnHangnam.setOnClickListener {
            isYearly = true
            val year = selectedCalendar.get(Calendar.YEAR)
            edtDate.setText(year.toString())
            fetchData()
        }

        btnPrev.setOnClickListener {
            if (isYearly) {
                selectedCalendar.add(Calendar.YEAR, -1)
            } else {
                selectedCalendar.add(Calendar.MONTH, -1)
            }
            updateDateDisplay()
            fetchData()
        }

        btnNext.setOnClickListener {
            if (isYearly) {
                selectedCalendar.add(Calendar.YEAR, 1)
            } else {
                selectedCalendar.add(Calendar.MONTH, 1)
            }
            updateDateDisplay()
            fetchData()
        }


        btnChitieu.setOnClickListener {
            isExpense = true
            displayFilteredData()
        }

        btnThunhap.setOnClickListener {
            isExpense = false
            displayFilteredData()
        }

        edtDate.setOnClickListener {
            val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_month_year_picker, null)
            val monthPicker = dialogView.findViewById<NumberPicker>(R.id.monthPicker)
            val yearPicker = dialogView.findViewById<NumberPicker>(R.id.yearPicker)

            yearPicker.minValue = 2000
            yearPicker.maxValue = 2100
            yearPicker.value = selectedCalendar.get(Calendar.YEAR)

            if (isYearly) {
                // Ẩn monthPicker nếu chọn năm
                monthPicker.visibility = View.GONE
            } else {
                monthPicker.minValue = 1
                monthPicker.maxValue = 12
                monthPicker.value = selectedCalendar.get(Calendar.MONTH) + 1
                monthPicker.visibility = View.VISIBLE
            }

            val dialog = android.app.AlertDialog.Builder(requireContext())
                .setTitle("Chọn thời gian")
                .setView(dialogView)
                .setPositiveButton("OK") { _, _ ->
                    val selectedYear = yearPicker.value
                    val selectedMonth = monthPicker.value - 1

                    selectedCalendar.set(Calendar.YEAR, selectedYear)
                    selectedCalendar.set(Calendar.MONTH, selectedMonth)

                    val formatted = if (isYearly) {
                        "$selectedYear"
                    } else {
                        String.format("%02d/%d", selectedMonth + 1, selectedYear)
                    }

                    edtDate.setText(formatted)
                    fetchData()
                }
                .setNegativeButton("Hủy", null)
                .create()

            dialog.show()
        }


        // Lấy dữ liệu từ Firestore
        fetchData()
        updateDateDisplay()


        return view
    }

    private fun fetchData() {
        db.collection("transactions").get()
            .addOnSuccessListener { result ->
                transactionList.clear()
                for (doc in result) {
                    val item = doc.toObject(Transaction::class.java)
                    transactionList.add(item)
                }
                displayFilteredData()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show()
            }
    }

    private fun displayFilteredData() {
        val filtered = transactionList.filter {
            (it.type == if (isExpense) "expense" else "income") &&
                    (if (isYearly) {
                        it.date.endsWith("/${selectedCalendar.get(Calendar.YEAR)}")
                    } else {
                        val monthStr = String.format("%02d", selectedCalendar.get(Calendar.MONTH) + 1)
                        it.date.contains("$monthStr/${selectedCalendar.get(Calendar.YEAR)}")
                    })
        }

        // Tính tổng tiền
        val totalAmount = filtered.sumOf { it.amount }
        val allFiltered = transactionList.filter {
            if (isYearly) {
                it.date.endsWith("/${selectedCalendar.get(Calendar.YEAR)}")
            } else {
                val monthStr = String.format("%02d", selectedCalendar.get(Calendar.MONTH) + 1)
                it.date.contains("$monthStr/${selectedCalendar.get(Calendar.YEAR)}")
            }
        }
        val totalIncome = allFiltered.filter { it.type == "income" }.sumOf { it.amount }
        val totalExpense = allFiltered.filter { it.type == "expense" }.sumOf { it.amount }
        val balance = totalIncome - totalExpense

// Định dạng và cập nhật giao diện
        tvTotalIncome.text = "+${NumberFormat.getInstance().format(totalIncome)}đ"
        tvTotalExpense.text = "-${NumberFormat.getInstance().format(totalExpense)}đ"

        val formattedBalance = "${if (balance >= 0) "+" else "-"}${NumberFormat.getInstance().format(kotlin.math.abs(balance))}đ"
        tvBalance.text = formattedBalance
        tvBalance.setTextColor(
            ContextCompat.getColor(requireContext(),
                if (balance >= 0) android.R.color.holo_blue_light else android.R.color.holo_red_light)
        )


        // Gom nhóm theo danh mục
        val categoryMap = filtered.groupBy { it.category }.mapValues {
            it.value.sumOf { tran -> tran.amount }
        }

        // Tạo danh sách Pair<Category, Pair<Amount, Percent>>
        val reportData = categoryMap.map { entry ->
            val category = entry.key
            val amount = entry.value
            val percent = if (totalAmount != 0) (amount.toFloat() / totalAmount * 100) else 0f
            Pair(category, Pair(amount, percent))
        }

        // Hiển thị pie chart
        showPieChart(categoryMap)

        // Hiển thị danh sách
        val adapter = object : ArrayAdapter<Pair<String, Pair<Int, Float>>>(requireContext(), R.layout.item_report, reportData) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val row = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_report, parent, false)

                val tvCategory = row.findViewById<TextView>(R.id.tvCategory)
                val tvAmountPercent = row.findViewById<TextView>(R.id.tvAmountPercent)

                val (category, valuePair) = getItem(position)!!
                val (amount, percent) = valuePair

                tvCategory.text = category

                val spannable = SpannableStringBuilder()

                val amountStr = "${amount}đ"
                val percentStr = " ${String.format("%.1f", percent)}%"

// Append số tiền in đậm, màu đen
                spannable.append(amountStr)
                spannable.setSpan(
                    StyleSpan(Typeface.BOLD),
                    0, amountStr.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                spannable.setSpan(
                    ForegroundColorSpan(ContextCompat.getColor(requireContext(), android.R.color.black)),
                    0, amountStr.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )

// Append phần trăm màu xám nhạt
                spannable.append(percentStr)
                spannable.setSpan(
                    ForegroundColorSpan(ContextCompat.getColor(requireContext(), android.R.color.darker_gray)),
                    amountStr.length, spannable.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                tvAmountPercent.text = spannable



                return row
            }
        }

        expenseList.adapter = adapter
    }



    private fun showPieChart(data: Map<String, Int>) {
        val entries = ArrayList<PieEntry>()
        data.forEach { (category, amount) ->
            entries.add(PieEntry(amount.toFloat(), category))
        }

        val dataSet = PieDataSet(entries, "")
        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
        val pieData = PieData(dataSet)

        pieChart.data = pieData
        pieChart.invalidate()
    }

    private fun updateDateDisplay() {
        val formatted = if (isYearly) {
            "${selectedCalendar.get(Calendar.YEAR)}"
        } else {
            val monthStr = String.format("%02d", selectedCalendar.get(Calendar.MONTH) + 1)
            "$monthStr/${selectedCalendar.get(Calendar.YEAR)}"
        }
        edtDate.setText(formatted)
    }
    inner class ReportAdapter(
        private val items: List<Triple<String, Int, Double>> // category, amount, percent
    ) : ArrayAdapter<Triple<String, Int, Double>>(requireContext(), 0, items) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(context)
                .inflate(R.layout.item_report, parent, false)

            val (category, amount, percent) = items[position]

            val tvCategory = view.findViewById<TextView>(R.id.tvCategory)
            val tvAmountPercent = view.findViewById<TextView>(R.id.tvAmountPercent)

            tvCategory.text = category
            tvAmountPercent.text = "$amount (${String.format("%.1f", percent)}%)"

            return view
        }
    }



}

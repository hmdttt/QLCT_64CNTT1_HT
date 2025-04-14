package com.example.expensemanager

import android.graphics.Color
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
import com.example.expensemanager.models.Transaction
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.firebase.firestore.FirebaseFirestore
import android.graphics.Typeface
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.widget.ArrayAdapter
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
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

    private lateinit var btnChitieu: Button
    private lateinit var btnThunhap: Button
    private lateinit var btnHangthang: Button
    private lateinit var btnHangnam: Button



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



        btnChitieu = view.findViewById(R.id.btnChitieu)
        btnThunhap = view.findViewById(R.id.btnThunhap)
        btnHangthang = view.findViewById(R.id.btnHangthang)
        btnHangnam = view.findViewById(R.id.btnHangnam)
        val btnPrev = view.findViewById<ImageButton>(R.id.btnLeft)
        val btnNext = view.findViewById<ImageButton>(R.id.btnRight)


        // Xử lý chọn loại báo cáo
        btnHangthang.setOnClickListener {
            isYearly = false
            val month = selectedCalendar.get(Calendar.MONTH) + 1
            val year = selectedCalendar.get(Calendar.YEAR)
            edtDate.setText(String.format("%02d/%d", month, year))
            fetchData()
            updateButtons2(true)
        }

        btnHangnam.setOnClickListener {
            isYearly = true
            val year = selectedCalendar.get(Calendar.YEAR)
            edtDate.setText(year.toString())
            fetchData()
            updateButtons2(false)
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
            updateButtons(true)
        }

        btnThunhap.setOnClickListener {
            isExpense = false
            displayFilteredData()
            updateButtons(false)
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

        updateButtons(true)
        updateButtons2(true)
        return view
    }

    fun updateButtons(isChi: Boolean) {
        btnChitieu.isSelected = isChi
        btnThunhap.isSelected = !isChi
    }
    fun updateButtons2(isChi: Boolean) {
        btnHangthang.isSelected = isChi
        btnHangnam.isSelected = !isChi
    }

    private fun fetchData() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        db.collection("transactions").whereEqualTo("userId",currentUserId)
            .get()
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

                val imgCategoryIcon = row.findViewById<ImageView>(R.id.imgCategoryIcon)
                val tvCategory = row.findViewById<TextView>(R.id.tvCategory)
                val tvAmountPercent = row.findViewById<TextView>(R.id.tvAmountPercent)

                val (category, valuePair) = getItem(position)!!
                val (amount, percent) = valuePair

                tvCategory.text = category

                val spannable = SpannableStringBuilder()

                val amountStr = "${amount}đ"
                val percentStr = " ${String.format("%.1f", percent)}%"

                val iconRes = when (category.lowercase()) {
                    "ăn uống"     -> R.drawable.icons8_hamburger48
                    "đi lại"      -> R.drawable.icons8_car48
                    "quần áo"     -> R.drawable.icons8_shirt
                    "phí ăn chơi" -> R.drawable.icons8_play
                    "gia dụng"    -> R.drawable.icons_8home48
                    "y tế"        -> R.drawable.icons8_health48
                    "mỹ phẩm"     -> R.drawable.icons8_cosmetics_48
                    "giáo dục"    -> R.drawable.icons8_education
                    "tiền nhà"    -> R.drawable.icons8_tiennha
                    "liên lạc"    -> R.drawable.icons8_phone
                    "tiết kiệm"   -> R.drawable.icons8_pig
                    "lương"       -> R.drawable.icons8_money48
                    "thưởng"      -> R.drawable.icons8_thuong
                    "phụ cấp"     -> R.drawable.icons8_dautu
                    else          -> R.drawable.icons8_pencil_50
                }
                imgCategoryIcon.setImageResource(iconRes)


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

        expenseList.setOnItemClickListener { _, _, position, _ ->
            val selectedCategory = reportData[position].first
            val fragment = CategoryDetailFragment()
            val bundle = Bundle()
            bundle.putString("category_name", selectedCategory)
            bundle.putString("transaction_type", if (isExpense) "expense" else "income")

            fragment.arguments = bundle

            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_container, fragment) // ID của FrameLayout chứa Fragment
                .addToBackStack(null)
                .commit()
        }

    }

    private fun showPieChart(data: Map<String, Int>) {
        val entries = ArrayList<PieEntry>()
        for ((category: String, amount: Int) in data) {
            entries.add(PieEntry(amount.toFloat(), category))
        }

        val dataSet = PieDataSet(entries, "")

        val customColors = listOf(
            Color.parseColor("#2196F3"), // Xanh dương (Blue)
            Color.parseColor("#F44336"), // Đỏ (Red)
            Color.parseColor("#9C27B0"), // Tím (Purple)
            Color.parseColor("#FFEB3B"), // Vàng (Yellow)
            Color.parseColor("#FF9800"), // Cam (Orange)
            Color.parseColor("#4CAF50"), // Xanh lá (Green)
            Color.parseColor("#E91E63")  // Hồng (Pink)
        )
        dataSet.colors = customColors
        dataSet.sliceSpace = 2f // hoặc 2f nếu bạn muốn viền mỏng hơn

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
}

package com.example.expensemanager

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.expensemanager.databinding.FragmentCalendarBinding
import com.example.expensemanager.models.Transaction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private val calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        binding.btnPrevMonth.setOnClickListener {
            calendar.add(Calendar.MONTH, -1)
            loadTransactionsAndDrawCalendar()
        }

        binding.btnNextMonth.setOnClickListener {
            calendar.add(Calendar.MONTH, 1)
            loadTransactionsAndDrawCalendar()
        }

        loadTransactionsAndDrawCalendar()
    }

    private fun loadTransactionsAndDrawCalendar() {
        val user = auth.currentUser ?: return
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)

        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        val startCal = Calendar.getInstance().apply { set(year, month, 1, 0, 0, 0) }
        val endCal = Calendar.getInstance().apply {
            set(year, month, 1)
            add(Calendar.MONTH, 1)
        }

        Log.d("DEBUG", "Đang load dữ liệu cho UID: ${user.uid}")
        Log.d("DEBUG", "Thời gian từ: ${sdf.format(startCal.time)} đến ${sdf.format(endCal.time)}")

        db.collection("transactions")
            //.whereEqualTo("userId", user.uid)
            .get()
            .addOnSuccessListener { snapshot ->
                Log.d("DEBUG", "Tổng số bản ghi Firestore: ${snapshot.size()}")

                val transactions = snapshot.documents.mapNotNull {
                    it.toObject(Transaction::class.java)
                }

                val filtered = transactions.filter {
                    val transDate = try {
                        sdf.parse(it.date)
                    } catch (e: Exception) {
                        Log.e("DEBUG", "Lỗi parse ngày: ${it.date}")
                        null
                    }

                    val cal = Calendar.getInstance().apply { time = transDate ?: Date(0) }
                    val result = cal.get(Calendar.MONTH) == month && cal.get(Calendar.YEAR) == year

                    Log.d("DEBUG", "⏱ ${it.date} → ${transDate?.toString()} | match = $result")

                    result
                }

                Log.d("DEBUG", "Số giao dịch khớp với tháng $month/$year: ${filtered.size}")

                val grouped = filtered.groupBy {
                    val cal = Calendar.getInstance()
                    cal.time = sdf.parse(it.date) ?: Date()
                    cal.get(Calendar.DAY_OF_MONTH)
                }

                drawCalendar(year, month, grouped)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show()
                Log.e("DEBUG", "Lỗi Firestore: ${it.message}")
            }
    }
    private fun formatMoney(amount: Int): String {
        val formatter = java.text.DecimalFormat("#,###")
        return if (amount >= 0) "+${formatter.format(amount)}đ" else "-${formatter.format(-amount)}đ"
    }


    private fun drawCalendar(year: Int, month: Int, transactionsByDate: Map<Int, List<Transaction>>) {
        binding.tvMonthYear.text = SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(calendar.time)

        val cal = Calendar.getInstance().apply { set(year, month, 1) }
        val firstDayOfWeek = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        val prevMonth = Calendar.getInstance().apply {
            set(year, month, 1)
            add(Calendar.MONTH, -1)
        }
        val lastDayPrevMonth = prevMonth.getActualMaximum(Calendar.DAY_OF_MONTH)

        binding.calendarTable.removeAllViews()
        var dayCounter = 1
        var nextMonthDay = 1

        // Tính tổng thu/chi
        var income = 0
        var expense = 0

        for (list in transactionsByDate.values) {
            for (txn in list) {
                if (txn.type == "income") income += txn.amount
                else expense += txn.amount
            }
        }


        // Hiển thị tổng
        binding.tvIncome.text = formatMoney(income)
        binding.tvExpense.text = formatMoney(-expense)
        binding.tvBalance.text = formatMoney(income - expense)

        for (i in 0 until 6) {
            val row = TableRow(requireContext())
            for (j in 0 until 7) {
                val cellView = layoutInflater.inflate(R.layout.item_calendar_day, row, false)
                val tvDay = cellView.findViewById<TextView>(R.id.tvDay)
                val tvAmount = cellView.findViewById<TextView>(R.id.tvAmount)

                when {
                    i == 0 && j < firstDayOfWeek -> {
                        val day = lastDayPrevMonth - (firstDayOfWeek - j - 1)
                        tvDay.text = day.toString()
                        tvDay.setTextColor(Color.DKGRAY)
                        tvAmount.text = ""
                    }
                    dayCounter > daysInMonth -> {
                        tvDay.text = nextMonthDay.toString()
                        tvDay.setTextColor(Color.DKGRAY)
                        tvAmount.text = ""
                        nextMonthDay++
                    }
                    else -> {
                        val list = transactionsByDate[dayCounter]
                        tvDay.text = dayCounter.toString()

                        if (!list.isNullOrEmpty()) {
                            val total = list.sumOf {
                                if (it.type == "income") it.amount else -it.amount
                            }
                            tvAmount.text = formatMoney(total)
                            tvAmount.setTextColor(if (total >= 0) Color.BLUE else Color.RED)

                            // Gán sự kiện click để hiển thị chi tiết ngày đó
                            cellView.setOnClickListener {
                                showTransactionsForDay(list)
                            }
                        } else {
                            tvAmount.text = "..."
                            tvAmount.setTextColor(Color.GRAY)

                            cellView.setOnClickListener {
                                showTransactionsForDay(emptyList())
                            }
                        }
                        dayCounter++
                    }
                }
                row.addView(cellView)
            }
            binding.calendarTable.addView(row)
        }
        val allTransactions = transactionsByDate.values.flatten()
        showTransactionsForDay(allTransactions)
    }


    private fun showTransactionsForDay(transactions: List<Transaction>) {
        binding.transactionListLayout.removeAllViews()

        if (transactions.isEmpty()) {
            val emptyView = TextView(requireContext())
            emptyView.text = "Không có giao dịch"
            emptyView.setPadding(8, 8, 8, 8)
            binding.transactionListLayout.addView(emptyView)
            return
        }

        // Group by date
        val groupedByDate = transactions.groupBy { it.date }

        for ((date, txnsOnDate) in groupedByDate) {
            // Hiển thị ngày
            val dateText = TextView(requireContext())
            dateText.text = "Ngày $date"
            dateText.setTextColor(Color.DKGRAY)
            dateText.setPadding(16, 16, 16, 8)
            dateText.textSize = 16f
            binding.transactionListLayout.addView(dateText)

            for (txn in txnsOnDate) {
                val view = layoutInflater.inflate(R.layout.item_transaction_calendar, binding.transactionListLayout, false)
                val tvName = view.findViewById<TextView>(R.id.tvName)
                val tvAmount = view.findViewById<TextView>(R.id.tvAmount)
                val img = view.findViewById<ImageView>(R.id.iconCategory)

                tvName.text = txn.category
                tvAmount.text = formatMoney(if (txn.type == "income") txn.amount else -txn.amount)
                tvAmount.setTextColor(if (txn.type == "income") Color.BLUE else Color.RED)
                img.setImageResource(getCategoryIconRes(txn.category))

                binding.transactionListLayout.addView(view)
            }
        }
    }

    private fun getCategoryIconRes(category: String): Int {
        return when (category.lowercase(Locale.ROOT)) {
            "ăn uống" -> R.drawable.icons8_hamburger48
            "giải trí", "phí ăn chơi" -> R.drawable.icons8_play
            "quần áo", "mua sắm" -> R.drawable.icons8_shirt
            "mỹ phẩm" -> R.drawable.icons8_cosmetics_48
            "gia dụng" -> R.drawable.icons_8home48
            "y tế" -> R.drawable.icons8_health48
            "giáo dục" -> R.drawable.icons8_education
            "đi lại" -> R.drawable.icons8_car48
            "tiền nhà" -> R.drawable.icons8_tiennha
            "liên lạc" -> R.drawable.icons8_phone
            "tiết kiệm" -> R.drawable.icons8_pig
            "lương", "thưởng", "phụ cấp" -> R.drawable.icons8_money48
            else -> R.drawable.icons8_pencil_50
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

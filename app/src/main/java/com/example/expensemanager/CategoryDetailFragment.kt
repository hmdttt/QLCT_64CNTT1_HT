package com.example.expensemanager

import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.expensemanager.models.Transaction
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.firestore.FirebaseFirestore
import java.text.NumberFormat

// ✅ Class mở rộng để lưu ID của giao dịch Firestore
data class TransactionWithId(
    val id: String,
    val transaction: Transaction
)

class CategoryDetailFragment : Fragment() {

    private lateinit var barChart: BarChart
    private lateinit var listView: ListView
    private lateinit var tvTitle: TextView
    private var categoryName: String? = null
    private var transactionType: String? = null
    private val transactionList = mutableListOf<TransactionWithId>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_category_detail, container, false)

        barChart = view.findViewById(R.id.barChart)
        listView = view.findViewById(R.id.transactionListView)
        tvTitle = view.findViewById(R.id.tvTitle)
        categoryName = arguments?.getString("category_name")
        transactionType = arguments?.getString("transaction_type") ?: "expense"

        loadAndDisplayData()

        return view
    }

    private fun loadAndDisplayData() {
        FirebaseFirestore.getInstance().collection("transactions")
            .get()
            .addOnSuccessListener { result ->
                transactionList.clear()

                for (doc in result) {
                    val transaction = doc.toObject(Transaction::class.java)
                    if (transaction.category == categoryName && transaction.type == transactionType) {
                        transactionList.add(TransactionWithId(doc.id, transaction))
                    }
                }

                showBarChart()
                showListView()
            }
    }

    private fun showBarChart() {
        val grouped = transactionList.map { it.transaction }.groupBy {
            it.date.takeLast(7) // "MM/yyyy"
        }.mapValues { entry ->
            entry.value.sumOf { it.amount }
        }.toSortedMap()

        val entries = grouped.entries.mapIndexed { index, (month, total) ->
            BarEntry(index.toFloat(), total.toFloat())
        }

        val dataSet = BarDataSet(entries, "Chi tiêu $categoryName")
        dataSet.color = Color.parseColor("#FF9800")
        dataSet.valueTextSize = 12f
        dataSet.setDrawValues(true)

        val barData = BarData(dataSet)
        barChart.data = barData

        val monthLabels = grouped.keys.map {
            try {
                val parts = it.split("/")
                val monthNum = parts[0].toInt()
                "T$monthNum"
            } catch (e: Exception) {
                "T?"
            }
        }

        barChart.xAxis.valueFormatter = IndexAxisValueFormatter(monthLabels)
        barChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        barChart.xAxis.granularity = 1f
        barChart.xAxis.setDrawGridLines(false)

        barChart.axisRight.isEnabled = false
        barChart.description.isEnabled = false
        barChart.setVisibleXRangeMaximum(4f)
        barChart.moveViewToX(entries.size.toFloat())
        barChart.invalidate()

        val latestMonth = grouped.keys.lastOrNull() ?: ""
        val typeLabel = if (transactionType == "income") "Thu nhập" else "Chi tiêu"
        val formattedTitle = "$categoryName ($typeLabel) - Tháng $latestMonth"

        tvTitle.text = formattedTitle
    }

    private fun showListView() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            transactionList.map {
                val t = it.transaction
                "${t.category} | ${t.date} | ${t.note}: ${NumberFormat.getInstance().format(t.amount)}đ"
            }
        )
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            val item = transactionList[position]
            val transaction = item.transaction

            val fragment = HomeFragment()
            val bundle = Bundle().apply {
                putString("edit_mode", "true")
                putString("transaction_id", item.id)
                putString("amount", transaction.amount.toString())
                putString("note", transaction.note)
                putString("date", transaction.date)
                putString("type", transaction.type)
                putString("category", transaction.category)
            }
            fragment.arguments = bundle

            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_container, fragment) // đổi theo ID layout bạn dùng
                .addToBackStack(null)
                .commit()
        }
    }
}

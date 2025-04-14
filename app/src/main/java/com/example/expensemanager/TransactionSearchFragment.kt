
package com.example.expensemanager

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.expensemanager.databinding.FragmentTransactionSearchBinding
import com.example.expensemanager.models.Transaction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.DecimalFormat
import java.util.*

class TransactionSearchFragment : Fragment() {
    private var _binding: FragmentTransactionSearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTransactionSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        binding.tvBack.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_container, SettingFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.editTextSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val keyword = s.toString().trim()
                searchTransactions(keyword)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        searchTransactions("")
    }

    private fun searchTransactions(keyword: String) {
        val user = auth.currentUser ?: return
        db.collection("transactions").whereEqualTo("userId", user.uid)
            .get()
            .addOnSuccessListener { snapshot ->
                val txns = snapshot.documents.mapNotNull { doc ->
                    val txn = doc.toObject(Transaction::class.java)
                    txn?.let { TransactionWithId(doc.id, it) }
                }
                val filtered = txns.filter {
                    it.transaction.category.contains(keyword, ignoreCase = true)
                }
                updateSummary(filtered)
                showResults(filtered)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Lỗi khi tìm kiếm", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateSummary(results: List<TransactionWithId>) {
        var income = 0
        var expense = 0
        for (txn in results) {
            if (txn.transaction.type == "income") income += txn.transaction.amount
            else expense += txn.transaction.amount
        }
        binding.textIncome.text = "Thu nhập\n${formatMoney(income)}"
        binding.textExpense.text = "Chi phí\n${formatMoney(-expense)}"
        binding.textTotal.text = "Tổng\n${formatMoney(income - expense)}"
    }

    private fun showResults(results: List<TransactionWithId>) {
        binding.resultLayout.removeAllViews()

        if (results.isEmpty()) {
            val emptyView = TextView(requireContext())
            emptyView.text = "Không có giao dịch nào"
            emptyView.setPadding(16, 16, 16, 16)
            binding.resultLayout.addView(emptyView)
            return
        }

        val groupedByDate = results.groupBy { it.transaction.date }
        for ((date, txns) in groupedByDate) {
            val dateTitle = TextView(requireContext())
            dateTitle.text = "Ngày $date"
            dateTitle.setTextColor(Color.DKGRAY)
            dateTitle.textSize = 16f
            dateTitle.setPadding(16, 16, 16, 8)
            binding.resultLayout.addView(dateTitle)

            for (txn in txns) {
                val view = layoutInflater.inflate(R.layout.item_transaction_calendar, binding.resultLayout, false)
                val tvName = view.findViewById<TextView>(R.id.tvName)
                val tvAmount = view.findViewById<TextView>(R.id.tvAmount)
                val icon = view.findViewById<ImageView>(R.id.iconCategory)

                val t = txn.transaction
                tvName.text = t.category
                tvAmount.text = formatMoney(if (t.type == "income") t.amount else -t.amount)
                tvAmount.setTextColor(if (t.type == "income") Color.BLUE else Color.RED)
                icon.setImageResource(getCategoryIconRes(t.category))

                view.setOnClickListener { openEditTransaction(txn) }
                binding.resultLayout.addView(view)
            }
        }
    }

    private fun openEditTransaction(txn: TransactionWithId) {
        val t = txn.transaction
        val fragment = HomeFragment()
        val bundle = Bundle().apply {
            putString("edit_mode", "true")
            putString("transaction_id", txn.id)
            putString("amount", t.amount.toString())
            putString("note", t.note)
            putString("date", t.date)
            putString("type", t.type)
            putString("category", t.category)
        }
        fragment.arguments = bundle

        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun formatMoney(amount: Int): String {
        return DecimalFormat("#,###").format(amount) + "đ"
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



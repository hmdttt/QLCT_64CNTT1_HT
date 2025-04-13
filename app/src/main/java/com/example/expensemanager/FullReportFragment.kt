package com.example.expensemanager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.database.*

class FullReportFragment : Fragment() {

    private lateinit var tvIncome: TextView
    private lateinit var tvExpense: TextView
    private lateinit var tvTotal: TextView

    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_full_report, container, false)

        tvIncome = view.findViewById(R.id.tvIncome)
        tvExpense = view.findViewById(R.id.tvExpense)
        tvTotal = view.findViewById(R.id.tvTotal)

        database = FirebaseDatabase.getInstance().reference.child("transactions")
        loadReportFromFirebase()

        return view
    }

    private fun loadReportFromFirebase() {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var totalIncome = 0L
                var totalExpense = 0L

                for (transactionSnapshot in snapshot.children) {
                    val amount = transactionSnapshot.child("amount").getValue(Long::class.java) ?: 0
                    val type = transactionSnapshot.child("type").getValue(String::class.java) ?: ""

                    if (type == "income") {
                        totalIncome += amount
                    } else if (type == "expense") {
                        totalExpense += amount
                    }
                }

                val balance = totalIncome - totalExpense

                tvIncome.text = "${totalIncome}đ"
                tvExpense.text = "${totalExpense}đ"
                tvTotal.text = "${balance}đ"
            }

            override fun onCancelled(error: DatabaseError) {
                tvIncome.text = "Lỗi"
                tvExpense.text = "Lỗi"
                tvTotal.text = "Lỗi"
            }
        })
    }
}

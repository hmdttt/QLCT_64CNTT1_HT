package com.example.expensemanager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.expensemanager.databinding.FragmentFullReportBinding
import com.example.expensemanager.models.Transaction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.DecimalFormat

class FullReportFragment : Fragment() {

    private lateinit var tvIncome: TextView
    private lateinit var tvExpense: TextView
    private lateinit var tvTotal: TextView
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentFullReportBinding.inflate(inflater, container, false)
        tvIncome = binding.tvIncome
        tvExpense = binding.tvExpense
        tvTotal = binding.tvTotal

        db = FirebaseFirestore.getInstance()

        binding.tvBack.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_container, SettingFragment())
                .addToBackStack(null)
                .commit()
        }

        loadReportData()

        return binding.root
    }


    private fun loadReportData() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

         db.collection("transactions").whereEqualTo("userId", user.uid)

            .get()
            .addOnSuccessListener { snapshot ->
                var totalIncome = 0
                var totalExpense = 0

                // Lặp qua các document trong Firestore
                for (doc in snapshot.documents) {
                    val txn = doc.toObject(Transaction::class.java)
                    if (txn != null) {
                        if (txn.type == "income") {
                            totalIncome += txn.amount
                        } else if (txn.type == "expense") {
                            totalExpense += txn.amount
                        }
                    }
                }

                // Tính toán và cập nhật thông tin lên giao diện
                val balance = totalIncome - totalExpense
                tvIncome.text = "+${formatMoney(totalIncome)}đ"
                tvExpense.text = "-${formatMoney(totalExpense)}đ"
                tvTotal.text = "${formatMoney(balance)}đ"
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error loading data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun formatMoney(amount: Int): String {
        return DecimalFormat("#,###").format(amount)
    }
}


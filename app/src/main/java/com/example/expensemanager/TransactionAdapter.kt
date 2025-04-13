package com.example.expensemanager

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.expensemanager.R
import com.example.expensemanager.models.Transaction
import java.text.NumberFormat
import java.util.*

class TransactionAdapter(private val transactions: List<Transaction>) :
    RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    class TransactionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNote: TextView = view.findViewById(R.id.tvNote)
        val tvAmount: TextView = view.findViewById(R.id.tvAmount)
        val imgIcon: ImageView = view.findViewById(R.id.imgtIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.i, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]

        holder.tvNote.text = transaction.note
        holder.tvDate.text = transaction.date
        holder.tvAmount.text = NumberFormat.getNumberInstance(Locale("vi", "VN")).format(transaction.amount) + "đ"

        if (transaction.type == "income") {
            holder.imgIcon.setImageResource(R.drawable.ic_income)
            holder.tvAmount.setTextColor(Color.parseColor("#03A9F4"))
        } else {
            holder.imgIcon.setImageResource(R.drawable.ic_expense)
            holder.tvAmount.setTextColor(Color.parseColor("#F44336"))
        }
    }

    override fun getItemCount(): Int = transactions.size
}

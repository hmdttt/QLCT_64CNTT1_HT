import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.expensemanager.R
import com.example.expensemanager.models.Transaction

class TransactionAdapter(private val transactionList: List<Transaction>) : RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.tem_transaction, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction = transactionList[position]
        holder.tvDate.text = transaction.date
        holder.tvAmount.text = formatMoney(transaction.amount)
        // Set icon for income or expense
        if (transaction.type == "income") {
            holder.icIncome.visibility = View.VISIBLE
            holder.icExpense.visibility = View.GONE
        } else {
            holder.icIncome.visibility = View.GONE
            holder.icExpense.visibility = View.VISIBLE
        }
    }

    override fun getItemCount(): Int {
        return transactionList.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvAmount: TextView = view.findViewById(R.id.tvAmount)
        val icIncome: ImageView = view.findViewById(R.id.imgtIcon)
        val icExpense: ImageView = view.findViewById(R.id.ic_expense)
    }

    private fun formatMoney(amount: Int): String {
        return "$${amount}"
    }
}


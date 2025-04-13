package com.example.expensemanager.helpers


import com.example.expensemanager.models.Transaction
import com.google.firebase.firestore.FirebaseFirestore

class TransactionHelper {
    private val db = FirebaseFirestore.getInstance()

    fun addTransaction(transaction: Transaction, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("transactions").add(transaction)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }
}
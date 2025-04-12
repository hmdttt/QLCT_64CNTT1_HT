package com.example.expensemanager.models

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Transaction(
    var amount: Int = 0,
    var note: String = "",
    var date: String = "",
    var type: String = "",
    var category: String = "",
    var userId: String = ""
)


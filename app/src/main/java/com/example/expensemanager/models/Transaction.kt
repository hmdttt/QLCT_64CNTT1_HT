package com.example.expensemanager.models

class Transaction() {
    var amount: Int = 0
    var note: String = ""
    var date: String = ""
    var type: String = ""
    var category: String = ""

    constructor(amount: Int, note: String, date: String, type: String, category: String) : this() {
        this.amount = amount
        this.note = note
        this.date = date
        this.type = type
        this.category = category
    }
}

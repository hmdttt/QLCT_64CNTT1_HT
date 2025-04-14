package com.example.expensemanager

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.expensemanager.models.Transaction
import com.google.firebase.firestore.FirebaseFirestore
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.expensemanager.helpers.GPTHelper
import com.google.firebase.auth.FirebaseAuth
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private var selectedCategory: String? = null
    private var selectedType: String? = null // Mặc định là chi
    private var editTransactionId: String? = null


    private lateinit var btnTienThu: Button
    private lateinit var btnTienChi: Button
    private lateinit var layoutTienThu: LinearLayout
    private lateinit var layoutTienChi: LinearLayout

    private lateinit var gridLayoutChi: GridLayout
    private lateinit var gridLayoutThu: GridLayout
    private lateinit var btnAddCategoryExpense: LinearLayout
    private lateinit var btnAddCategoryIncome: LinearLayout


    private lateinit var btnPrevDate: ImageButton
    private lateinit var btnNextDate: ImageButton
    private var selectedCalendar: Calendar = Calendar.getInstance()

    private lateinit var edtSoTien: EditText
    private lateinit var edtNote: EditText
    private lateinit var edtDate: EditText

    private lateinit var btnAnUong: LinearLayout
    private lateinit var btnPhiAnChoi: LinearLayout
    private lateinit var btnGiaDung: LinearLayout
    private lateinit var btnYTe: LinearLayout
    private lateinit var btnMyPham: LinearLayout
    private lateinit var btnGiaoDuc: LinearLayout
    private lateinit var btnTienNha: LinearLayout
    private lateinit var btnLienLac: LinearLayout
    private lateinit var btnTietKiem: LinearLayout
    private lateinit var btnDiLai: LinearLayout
    private lateinit var btnQuanAo: LinearLayout

    private lateinit var btnLuong: LinearLayout
    private lateinit var btnThuong: LinearLayout
    private lateinit var btnPhuCap: LinearLayout

    private lateinit var layoutEditButtons: LinearLayout
    private lateinit var btnUpdateTransaction: Button
    private lateinit var btnDeleteTransaction: Button

    private val CAMERA_PERMISSION = android.Manifest.permission.CAMERA
    private val CAMERA_REQUEST_CODE = 101
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        bitmap?.let {

            // ✅ Gọi helper mới
            GPTHelper.recognizeTextFromImage(requireContext(), bitmap) { content ->
                requireActivity().runOnUiThread {
                    showTransactionPreviewDialog(content) // Bạn đã có hàm này
                }
            }
        }
    }
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, it)
            GPTHelper.recognizeTextFromImage(requireContext(), bitmap) { content ->
                requireActivity().runOnUiThread {
                    showTransactionPreviewDialog(content)
                }
            }
        }
    }


    private fun showTransactionPreviewDialog(jsonString: String) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_transaction_preview, null)

        val listView = dialogView.findViewById<ListView>(R.id.lvPreview)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btnConfirmSave)

        val transactionList = mutableListOf<JSONObject>()

        try {
            val array = JSONArray(jsonString)
            for (i in 0 until array.length()) {
                transactionList.add(array.getJSONObject(i))
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Lỗi phân tích JSON", Toast.LENGTH_SHORT).show()
            return
        }

        val adapter = object : ArrayAdapter<JSONObject>(requireContext(), R.layout.item_transaction_preview, transactionList) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val itemView = convertView ?: LayoutInflater.from(context)
                    .inflate(R.layout.item_transaction_preview, parent, false)

                val obj = getItem(position)!!
                val edtCategory = itemView.findViewById<EditText>(R.id.edtCategory)
                val edtAmount = itemView.findViewById<EditText>(R.id.edtAmount)
                val edtNote = itemView.findViewById<EditText>(R.id.edtNote)
                val imgCategory = itemView.findViewById<ImageView>(R.id.imgCategory)

                // Đặt giá trị ban đầu
                edtCategory.setText(obj.optString("category"))
                edtAmount.setText(obj.optInt("amount").toString())
                edtNote.setText(obj.optString("note"))

                // Gán icon danh mục
                imgCategory.setImageResource(getCategoryIconRes(obj.optString("category")))

                // Cập nhật dữ liệu nếu người dùng chỉnh sửa
                edtCategory.addTextChangedListener(createTextWatcher { text ->
                    obj.put("category", text)
                    imgCategory.setImageResource(getCategoryIconRes(text))
                })

                edtAmount.addTextChangedListener(createTextWatcher { text ->
                    obj.put("amount", text.toIntOrNull() ?: 0)
                })

                edtNote.addTextChangedListener(createTextWatcher { text ->
                    obj.put("note", text)
                })

                return itemView
            }
        }

        listView.adapter = adapter

        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle("Xác nhận và chỉnh sửa")
            .create()

        btnConfirm.setOnClickListener {
            for (obj in transactionList) {
                val category = obj.optString("category")
                val amount = obj.optInt("amount")
                val note = obj.optString("note")
                val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

                saveTransaction(amount, note, date, "expense", category)
            }

            dialog.dismiss()
            Toast.makeText(requireContext(), "Đã lưu các khoản chi!", Toast.LENGTH_SHORT).show()
        }
        dialog.show()
    }
    private fun createTextWatcher(onChange: (String) -> Unit): TextWatcher {
        return object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                onChange(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
    }

    private fun getCategoryIconRes(category: String): Int {
        return when (category.lowercase(Locale.ROOT)) {
            "ăn uống" -> R.drawable.icons8_hamburger48
            "gia dụng" -> R.drawable.icons_8home48
            "quần áo" -> R.drawable.icons8_shirt
            "mỹ phẩm" -> R.drawable.icons8_cosmetics_48
            "phí ăn chơi" -> R.drawable.icons8_play
            "y tế" -> R.drawable.icons8_health48
            "giáo dục" -> R.drawable.icons8_education
            "đi lại" -> R.drawable.icons8_car48
            "tiền nhà" -> R.drawable.icons8_tiennha
            "liên lạc" -> R.drawable.icons8_phone
            "tiết kiệm" -> R.drawable.icons8_pig
            else -> R.drawable.icons8_pencil_50
        }
    }




    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireContext(), "Đã cấp quyền Camera", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Bạn cần cấp quyền Camera để chụp hóa đơn", Toast.LENGTH_LONG).show()
            }
        }
    }


    @SuppressLint("MissingInflatedId")
    override fun onCreateView(

        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =  inflater.inflate(R.layout.fragment_home, container, false)
        Log.d("DEBUG_HOME", "🧩 onCreateView đã chạy")


        view.findViewById<Button>(R.id.btnCaptureReceipt).setOnClickListener {
            val options = arrayOf("Chụp ảnh", "Chọn từ thư viện")
            val builder = android.app.AlertDialog.Builder(requireContext())
            builder.setTitle("Chọn nguồn ảnh")
            builder.setItems(options) { _, which ->
                when (which) {
                    0 -> cameraLauncher.launch(null)        // Mở camera
                    1 -> galleryLauncher.launch("image/*")  // Mở thư viện
                }
            }
            builder.show()
        }

        if (ContextCompat.checkSelfPermission(requireContext(), CAMERA_PERMISSION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(CAMERA_PERMISSION), CAMERA_REQUEST_CODE)
        }


        // Khởi tạo DatabaseHelper

        // Ánh xạ view
        edtSoTien = view.findViewById(R.id.etAmount)
        edtNote = view.findViewById(R.id.etNote)
        edtDate = view.findViewById(R.id.etDate)
        updateDateEditText()

        gridLayoutChi = view.findViewById(R.id.gridLayoutTienChi)
        gridLayoutThu = view.findViewById(R.id.gridLayoutTienthu)
        btnAddCategoryExpense = view.findViewById(R.id.btnAddCategoryExpense)
        btnAddCategoryIncome = view.findViewById(R.id.btnAddCategoryIncome)


        btnPrevDate = view.findViewById(R.id.btnPreviousDate)
        btnNextDate = view.findViewById(R.id.btnNextDate)


        btnAnUong = view.findViewById(R.id.btnAnuong)
        btnDiLai = view.findViewById(R.id.btnDilai)
        btnQuanAo = view.findViewById(R.id.btnQuanao)
        btnPhiAnChoi = view.findViewById(R.id.btnPhianchoi)
        btnGiaDung = view.findViewById(R.id.btnGiadung)
        btnYTe = view.findViewById(R.id.btnYte)
        btnMyPham = view.findViewById(R.id.btnMypham)
        btnGiaoDuc = view.findViewById(R.id.btnGiaoduc)
        btnTienNha = view.findViewById(R.id.btnTiennha)
        btnLienLac = view.findViewById(R.id.btnLienlac)
        btnTietKiem = view.findViewById(R.id.btnTietkiem)

        btnLuong = view.findViewById(R.id.btnTienluong)
        btnThuong = view.findViewById(R.id.btnTienthuong)
        btnPhuCap = view.findViewById(R.id.btnPhucap)

        btnTienThu = view.findViewById(R.id.btnTienthu)
        btnTienChi = view.findViewById(R.id.btnTienchi)
        layoutTienThu = view.findViewById(R.id.LinearLayoutThu)
        layoutTienChi = view.findViewById(R.id.LinearLayoutChi)

        layoutEditButtons = view.findViewById(R.id.layoutEditButtons)
        btnUpdateTransaction = view.findViewById(R.id.btnUpdateTransaction)
        btnDeleteTransaction = view.findViewById(R.id.btnDeleteTransaction)


        layoutTienChi.visibility = View.VISIBLE
        layoutTienThu.visibility = View.GONE

        edtDate.setOnClickListener {
            val year = selectedCalendar.get(Calendar.YEAR)
            val month = selectedCalendar.get(Calendar.MONTH)
            val day = selectedCalendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(requireContext(), { _, y, m, d ->
                selectedCalendar.set(y, m, d)
                updateDateEditText()
            }, year, month, day).show()
        }

        btnPrevDate.setOnClickListener {
            selectedCalendar.add(Calendar.DAY_OF_MONTH, -1)
            updateDateEditText()
        }

        btnNextDate.setOnClickListener {
            selectedCalendar.add(Calendar.DAY_OF_MONTH, 1)
            updateDateEditText()
        }


        btnTienThu.setOnClickListener {
            layoutTienThu.visibility = View.VISIBLE
            layoutTienChi.visibility = View.GONE
            updateThuChiButtons(false)
        }

        btnTienChi.setOnClickListener {
            layoutTienThu.visibility = View.GONE
            layoutTienChi.visibility = View.VISIBLE
            updateThuChiButtons(true)
        }

        btnAnUong.setOnClickListener() {
            resetCategoryViews()
            selectedCategory = "Ăn uống"
            selectedType = "expense"
            btnAnUong.isSelected = true
        }
        btnDiLai.setOnClickListener() {
            resetCategoryViews()
            selectedCategory = "Đi lại"
            selectedType = "expense"
            btnDiLai.isSelected = true
        }
        btnQuanAo.setOnClickListener() {
            resetCategoryViews()
            selectedCategory = "Quần áo"
            selectedType = "expense"
            btnQuanAo.isSelected = true
        }
        btnPhiAnChoi.setOnClickListener() {
            resetCategoryViews()
            selectedCategory = "Phí ăn chơi"
            selectedType = "expense"
            btnPhiAnChoi.isSelected = true
        }
        btnGiaDung.setOnClickListener() {
            resetCategoryViews()
            selectedCategory = "Gia dụng"
            selectedType = "expense"
            btnGiaDung.isSelected = true
        }
        btnYTe.setOnClickListener() {
            resetCategoryViews()
            selectedCategory = "Y tế"
            selectedType = "expense"
            btnYTe.isSelected = true
        }
        btnMyPham.setOnClickListener() {
            resetCategoryViews()
            selectedCategory = "Mỹ phẩm"
            selectedType = "expense"
            btnMyPham.isSelected = true
        }
        btnGiaoDuc.setOnClickListener() {
            resetCategoryViews()
            selectedCategory = "Giáo dục"
            selectedType = "expense"
            btnGiaoDuc.isSelected = true
        }
        btnTienNha.setOnClickListener() {
            resetCategoryViews()
            selectedCategory = "Tiền nhà"
            selectedType = "expense"
            btnTienNha.isSelected = true
        }
        btnLienLac.setOnClickListener() {
            resetCategoryViews()
            selectedCategory = "Liên lạc"
            selectedType = "expense"
            btnLienLac.isSelected = true
        }
        btnTietKiem.setOnClickListener() {
            resetCategoryViews()
            selectedCategory = "Tiết kiệm"
            selectedType = "expense"
            btnTietKiem.isSelected = true
        }
        btnLuong.setOnClickListener() {
            resetCategoryViews()
            selectedCategory = "Lương"
            selectedType = "income"
            btnLuong.isSelected = true
        }
        btnThuong.setOnClickListener() {
            resetCategoryViews()
            selectedCategory = "Thưởng"
            selectedType = "income"
            btnThuong.isSelected = true
        }
        btnPhuCap.setOnClickListener() {
            resetCategoryViews()
            selectedCategory = "Phụ cấp"
            selectedType = "income"
            btnPhuCap.isSelected = true
        }
        btnAddCategoryExpense.setOnClickListener {
            showAddCategoryDialog(isExpense = true)
        }

        btnAddCategoryIncome.setOnClickListener {
            showAddCategoryDialog(isExpense = false)
        }


        val btnAddExpense = view.findViewById<Button>(R.id.btnAddExpense)
        val btnAddIncome = view.findViewById<Button>(R.id.btnAddIncome)

        btnAddExpense.setOnClickListener {
            val amount = edtSoTien.text.toString().toIntOrNull()
            val note = edtNote.text.toString()
            val date = edtDate.text.toString()

            if (amount == null || selectedCategory == null || date.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

            saveTransaction(amount, note, date, selectedType!!, selectedCategory!!, userId)
        }

        btnAddIncome.setOnClickListener {
            val amount = edtSoTien.text.toString().toIntOrNull()
            val note = edtNote.text.toString()
            val date = edtDate.text.toString()

            if (amount == null || selectedCategory == null || date.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

            saveTransaction(amount, note, date, selectedType!!, selectedCategory!!, userId)

        }
        arguments?.let {
            if (it.getString("edit_mode") == "true") {
                Log.d("DEBUG_HOME", "✏️ Dữ liệu truyền vào chỉnh sửa:")
                Log.d("DEBUG_HOME", "transaction_id = ${it.getString("transaction_id")}")
                Log.d("DEBUG_HOME", "amount = ${it.getString("amount")}")
                Log.d("DEBUG_HOME", "note = ${it.getString("note")}")
                Log.d("DEBUG_HOME", "date = ${it.getString("date")}")
                Log.d("DEBUG_HOME", "type = ${it.getString("type")}")
                Log.d("DEBUG_HOME", "category = ${it.getString("category")}")

                val amount = it.getString("amount")
                val note = it.getString("note")
                val date = it.getString("date")
                val type = it.getString("type")
                val category = it.getString("category")
                editTransactionId = it.getString("transaction_id")


                edtSoTien.setText(amount)
                edtNote.setText(note)
                edtDate.setText(date)
                selectedType = type
                selectedCategory = category

                when (type) {
                    "income" -> {
                        btnTienThu.performClick()
                    }
                    "expense" -> {
                        btnTienChi.performClick()
                    }
                }

                when (category) {
                    "Ăn uống" -> btnAnUong.performClick()
                    "Đi lại" -> btnDiLai.performClick()
                    "Quần áo" -> btnQuanAo.performClick()
                    "Phí ăn chơi" -> btnPhiAnChoi.performClick()
                    "Gia dụng" -> btnGiaDung.performClick()
                    "Y tế" -> btnYTe.performClick()
                    "Mỹ phẩm" -> btnMyPham.performClick()
                    "Giáo dục" -> btnGiaoDuc.performClick()
                    "Tiền nhà" -> btnTienNha.performClick()
                    "Liên lạc" -> btnLienLac.performClick()
                    "Tiết kiệm" -> btnTietKiem.performClick()
                    "Lương" -> btnLuong.performClick()
                    "Thưởng" -> btnThuong.performClick()
                    "Phụ cấp" -> btnPhuCap.performClick()
                }
                // ✨ Chuyển sang chế độ chỉnh sửa
                layoutEditButtons.visibility = View.VISIBLE
                btnAddExpense.visibility = View.GONE
                btnAddIncome.visibility = View.GONE
            }
        }

        btnUpdateTransaction.setOnClickListener {
            val amount = edtSoTien.text.toString().toIntOrNull()
            val note = edtNote.text.toString()
            val date = edtDate.text.toString()

            if (amount == null || selectedCategory == null || date.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updated = mapOf(
                "amount" to amount,
                "note" to note,
                "date" to date,
                "type" to selectedType,
                "category" to selectedCategory
            )

            editTransactionId?.let { id ->
                FirebaseFirestore.getInstance().collection("transactions")
                    .document(id)
                    .update(updated)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Đã cập nhật!", Toast.LENGTH_SHORT).show()
                        parentFragmentManager.popBackStack() // quay lại màn hình trước
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Lỗi khi cập nhật!", Toast.LENGTH_SHORT).show()
                    }
            }
        }
        btnDeleteTransaction.setOnClickListener {
            editTransactionId?.let { id ->
                FirebaseFirestore.getInstance().collection("transactions")
                    .document(id)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Đã xoá giao dịch!", Toast.LENGTH_SHORT).show()
                        parentFragmentManager.popBackStack()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Lỗi khi xoá!", Toast.LENGTH_SHORT).show()
                    }
            }
        }





        return view
    }
    fun updateThuChiButtons(isChi: Boolean) {
        btnTienChi.isSelected = isChi
        btnTienThu.isSelected = !isChi
    }


    private fun saveTransaction(amount: Int, note: String, date: String, type: String, category: String, userId: String = "") {
        Log.d("FirestoreDebug", "Saving transaction: amount=$amount, note=$note, date=$date, type=$type, category=$category")

        val db = FirebaseFirestore.getInstance()
        val transaction = Transaction(amount, note, date, type, category, userId)

        db.collection("transactions")
            .add(transaction)
            .addOnSuccessListener {
                Log.d("FirestoreDebug", "Transaction saved successfully!")
                Toast.makeText(requireContext(), "Đã lưu giao dịch!", Toast.LENGTH_SHORT).show()
                resetInput()
            }
            .addOnFailureListener {
                Log.e("FirestoreDebug", "Error saving transaction: ${it.message}")
                Toast.makeText(requireContext(), "Lỗi khi lưu dữ liệu: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun resetCategoryViews() {
        val categoryViews = listOf(
            btnAnUong, btnDiLai, btnQuanAo, btnGiaDung, btnMyPham,
            btnPhiAnChoi, btnYTe, btnGiaoDuc, btnTienNha, btnLienLac, btnTietKiem,
            btnLuong, btnThuong, btnPhuCap
        )
        for (view in categoryViews) {
            view.isSelected = false
        }
    }


    private fun resetInput() {
        edtSoTien.setText("")
        edtNote.setText("")
        edtDate.setText("")
        selectedCategory = null
        resetCategoryViews()
    }

    private fun updateDateEditText() {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        edtDate.setText(sdf.format(selectedCalendar.time))
    }
    private fun showAddCategoryDialog(isExpense: Boolean) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_category, null)
        val edtCategoryName = dialogView.findViewById<EditText>(R.id.edtCategoryName)

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Thêm danh mục mới")
            .setView(dialogView)
            .setPositiveButton("Thêm") { _, _ ->
                val name = edtCategoryName.text.toString().trim()
                if (name.isNotEmpty()) {
                    addCategoryButton(name, isExpense, isCustom = true)
                    selectedCategory = name
                    selectedType = if (isExpense) "expense" else "income"
                    Toast.makeText(requireContext(), "Đã chọn danh mục mới: $name", Toast.LENGTH_SHORT).show()
                }

            }
            .setNegativeButton("Hủy", null)
            .show()
    }
    private fun addCategoryButton(categoryName: String, isExpense: Boolean, isCustom: Boolean = false) {
        val layout = LayoutInflater.from(requireContext()).inflate(R.layout.item_category_button, null) as LinearLayout
        val text = layout.findViewById<TextView>(R.id.tvCategoryName)
        val image = layout.findViewById<ImageView>(R.id.ivCategoryIcon)

        text.text = categoryName
        image.setImageResource(R.drawable.icons8_pencil_50) // icon danh mục riêng

        layout.setOnClickListener {
            resetCategoryViews()
            selectedCategory = categoryName
            selectedType = if (isExpense) "expense" else "income"
            layout.isSelected = true
        }

        val params = GridLayout.LayoutParams().apply {
            width = 0
            height = GridLayout.LayoutParams.WRAP_CONTENT
            columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            setMargins(8, 8, 8, 8)
        }
        layout.layoutParams = params

        if (isExpense) {
            gridLayoutChi.addView(layout, gridLayoutChi.childCount - 1)
        } else {
            gridLayoutThu.addView(layout, gridLayoutThu.childCount - 1)
        }
    }

}
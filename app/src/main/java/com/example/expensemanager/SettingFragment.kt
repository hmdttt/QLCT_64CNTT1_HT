package com.example.expensemanager

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

class SettingFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_setting, container, false)

        val btnTimKiem = view.findViewById<Button>(R.id.btn_tim_kiem)
        val btnBaoCao = view.findViewById<Button>(R.id.btn_bao_cao_toan_ky)

        btnTimKiem.setOnClickListener {
            val fragment = TransactionSearchFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_container, fragment)
                .addToBackStack(null)
                .commit()
        }

        btnBaoCao.setOnClickListener {
            val fragment = FullReportFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_container, fragment)
                .addToBackStack(null)
                .commit()
        }

        return view
    }
}



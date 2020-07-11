package com.interpause.webcamdroid

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.Navigation


class AboutFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_about, container, false)
        layout.findViewById<Button>(R.id.button2).setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_aboutFragment_to_controlsFragment)
        }
        return layout
    }

}
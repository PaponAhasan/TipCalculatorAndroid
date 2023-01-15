package com.example.tippytip.ui.home

import android.animation.ArgbEvaluator
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.tippytip.R
import com.example.tippytip.databinding.FragmentHomeBinding
import kotlin.math.roundToInt

private const val TAG = "HomeFragment"
private const val INITIAL_TIP_PERCENT = 15
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        ViewModelProvider(this)[HomeViewModel::class.java]

        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        binding.seekBarTip.progress = INITIAL_TIP_PERCENT
        binding.tvTipPercentLabel.text = "$INITIAL_TIP_PERCENT%"
        updateTipDescription(INITIAL_TIP_PERCENT)
        binding.seekBarTip.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, progress: Int, fromUser: Boolean) {
                Log.e(TAG, "onProgressChanged $progress")
                binding.tvTipPercentLabel.text = "$progress%"
                computeTipAndTotal()
                updateTipDescription(progress)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}

            override fun onStopTrackingTouch(p0: SeekBar?) {}

        })
        binding.etBaseAmount.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(p0: Editable?) {
                Log.e(TAG, "afterTextChanged $")
                computeTipAndTotal()
            }

        })
        binding.etNumberOfPerson.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val inputFilter = InputFilter { source, start, end, dest, dstart, dend ->
                    if (dstart == 0 && source.startsWith("0")) {
                        ""
                    } else source
                }
                binding.etNumberOfPerson.filters = arrayOf(inputFilter)
            }

            override fun afterTextChanged(p0: Editable?) {
                computeTipAndTotal()
            }

        })
        binding.checkBoxRoundAmount.setOnCheckedChangeListener{buttonView, isChecked ->
            if (isChecked) {
                // The checkbox is checked
                computeRoundTipAndTotal()
                Toast.makeText(context, "Round up to tip and amount", Toast.LENGTH_SHORT).show()
            } else {
                // The checkbox is not checked
                computeTipAndTotal()
                Toast.makeText(context, "Unchecked", Toast.LENGTH_SHORT).show()
            }
        }
        binding.btnSave.setOnClickListener{
            if(binding.tvTipAmount.text.isNotEmpty()){
                Toast.makeText(context, "Saved your result", Toast.LENGTH_SHORT).show()
            }
        }
        return binding.root
    }

    private fun computeRoundTipAndTotal() {
        binding.tvTipAmount.text = binding.tvTipAmount.text.toString().toDouble().roundToInt().toString()
        binding.tvTotalAmount.text = binding.tvTotalAmount.text.toString().toDouble().roundToInt().toString()
    }

    private fun updateTipDescription(tipPercent: Int) {
        binding.tvTipDescription.text = when(tipPercent){
            in 0..9 -> "Poor"
            in 10..14 -> "Acceptable"
            in 15..19 -> "Good"
            in 20..24 -> "Great"
            else -> "Amazing"
        }
        // update the color based on the tipPercent
        val color = ArgbEvaluator().evaluate(
            //Progress bar range
            tipPercent.toFloat()/binding.seekBarTip.max,
            // start value
            ContextCompat.getColor(requireContext(), R.color.color_worst_tip),
            // end value
            ContextCompat.getColor(requireContext(), R.color.color_best_tip)
        ) as Int
        binding.tvTipDescription.setTextColor(color)
    }

    private fun computeTipAndTotal() {
        // Solved first issue
        if(binding.etBaseAmount.text.isEmpty() or binding.etNumberOfPerson.text.isEmpty()){
            binding.tvTipAmount.text = ""
            binding.tvTotalAmount.text = ""
            return
        }
        // 1. Get the value of the base and tip percent
        val baseAmount = binding.etBaseAmount.text.toString().toDouble()
        val numberOfPerson = binding.etNumberOfPerson.text.toString().toInt()
        val tipPercent = binding.seekBarTip.progress
        // 2. Compute the tip and total
        val tipAmount = (baseAmount * tipPercent / 100) / numberOfPerson
        val totalAmount = (baseAmount + tipAmount) / numberOfPerson
        // 3. Update the UI
        binding.tvTipAmount.text = "%.2f".format(tipAmount)
        binding.tvTotalAmount.text = "%.2f".format(totalAmount)

        /*
          Two issue improvement in this code
          01. bug : backspace on base amount(editText empty : try convert empty string to double) then
          app crashed (NumberFormatException: empty String[line 66])

          02. Formatting the output of the tip and total amount, when base amount decimal (100.9) total
           amount very long and unwieldy
         */

        /*
            Answer:
            Tip Each Person:	$ 1.51
            Total Each Person:	$ 51.96
            Tip Total:	$ 3.03
            Total (Bill + Tip):	$ 103.93
         */
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
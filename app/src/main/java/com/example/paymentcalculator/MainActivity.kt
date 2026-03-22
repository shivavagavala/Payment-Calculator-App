package com.example.paymentcalculator

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    lateinit var inputField: EditText
    lateinit var sendButton: ImageButton
    lateinit var plusButton: ImageButton
    lateinit var imageButton: ImageButton

    lateinit var emptyText: TextView
    lateinit var chatContainer: LinearLayout
    lateinit var peopleCount: EditText

    lateinit var resetButton: Button
    lateinit var splitButton: Button
    lateinit var calcDisplay: TextView
    // ✅ Calculator variables (MISSING BEFORE)
    var calcCurrent = ""
    var calcOperator = ""
    var calcFirst = 0.0

    lateinit var calcDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ FIRST load layout
        setContentView(R.layout.activity_main)

        // ✅ THEN find views
        emptyText = findViewById(R.id.emptyText)
        chatContainer = findViewById(R.id.chatContainer)
        inputField = findViewById(R.id.inputField)
        sendButton = findViewById(R.id.sendButton)
        plusButton = findViewById(R.id.plusButton)
        imageButton = findViewById(R.id.imageButton)

        sendButton.setOnClickListener {

            val text = inputField.text.toString().replace("₹", "").trim()

            if (text.isEmpty()) {
                Toast.makeText(this, "Enter amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val amount = text.toDoubleOrNull()

            if (amount == null) {
                Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            addPaymentToHistory(text)
            showSuccessPopup(text)
            inputField.setText("")
        }

        plusButton.setOnClickListener {
            showPopupMenu()
        }

        imageButton.setOnClickListener {
            Toast.makeText(this, "Attach image", Toast.LENGTH_SHORT).show()
        }
    }

    fun onCalcDecimal(view: View) {

        // Prevent multiple dots
        if (calcCurrent.contains(".")) return

        // If empty, start with 0.
        if (calcCurrent.isEmpty()) {
            calcCurrent = "0."
        } else {
            calcCurrent += "."
        }

        calcDisplay.text = calcCurrent
    }
    fun addPaymentToHistory(amount: String) {

        emptyText.visibility = View.GONE

        val row = LinearLayout(this)
        row.orientation = LinearLayout.HORIZONTAL
        row.setPadding(10, 10, 10, 10)

        val rowParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        row.layoutParams = rowParams

        // 👤 Avatar
        val avatar = TextView(this)
        avatar.text = "S"
        avatar.textSize = 16f
        avatar.setTextColor(android.graphics.Color.WHITE)
        avatar.gravity = android.view.Gravity.CENTER

        avatar.setBackgroundResource(R.drawable.avatar_bg)

// Size (important)
        val avatarParams = LinearLayout.LayoutParams(100, 100)
        avatarParams.setMargins(0, 0, 20, 0)
        avatar.layoutParams = avatarParams
        avatar.setPadding(0, 0, 0, 0)
        // 💳 Card
        val card = LinearLayout(this)
        card.orientation = LinearLayout.VERTICAL
        card.setPadding(30, 30, 30, 30)

        val params = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.weight = 1f
        card.layoutParams = params

        // Gradient background
        val gradient = android.graphics.drawable.GradientDrawable(
            android.graphics.drawable.GradientDrawable.Orientation.LEFT_RIGHT,
            intArrayOf(
                android.graphics.Color.parseColor("#FF6F00"),
                android.graphics.Color.parseColor("#FFA000")
            )
        )
        gradient.cornerRadius = 30f
        card.background = gradient

        // Amount
        val amountText = TextView(this)
        amountText.text = "₹$amount"
        amountText.textSize = 20f
        amountText.setTextColor(android.graphics.Color.WHITE)

        // Status
        val status = TextView(this)
        status.text = "PAID"
        status.setTextColor(android.graphics.Color.parseColor("#C8E6C9"))

        // Time
        val time = TextView(this)
        val currentTime = java.text.SimpleDateFormat("hh:mm a").format(java.util.Date())
        time.text = currentTime
        time.setTextColor(android.graphics.Color.parseColor("#EEEEEE"))

        card.addView(amountText)
        card.addView(status)
        card.addView(time)

        row.addView(avatar)
        row.addView(card)

        chatContainer.addView(row)
    }

    fun showSuccessPopup(amount: String) {

        val dialog = Dialog(this)
        dialog.setContentView(R.layout.success_popup)

        val message = dialog.findViewById<TextView>(R.id.successMessage)
        message.text = "₹$amount Sent Successfully ✅"

        dialog.show()
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.7).toInt(),
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )
        // Auto close after 2 seconds
        android.os.Handler().postDelayed({
            dialog.dismiss()
        }, 2000)
    }

    fun showPopupMenu() {

        val view = layoutInflater.inflate(R.layout.popup_menu, null)

        val popup = PopupWindow(
            view,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        )

        popup.showAsDropDown(plusButton, -100, -200)

        val calcOption = view.findViewById<LinearLayout>(R.id.calculatorOption)

        calcOption.setOnClickListener {
            popup.dismiss()
            showSmallCalculator()
        }
    }

    fun showSmallCalculator() {

        // ✅ Step 1: Create dialog
        calcDialog = Dialog(this)
        calcDialog.setContentView(R.layout.small_calculator)

        // ✅ Step 2: Initialize views
        calcDisplay = calcDialog.findViewById(R.id.calcDisplay)
        peopleCount = calcDialog.findViewById(R.id.peopleCount)
        splitButton = calcDialog.findViewById(R.id.splitButton)
        resetButton = calcDialog.findViewById(R.id.resetButton)

        // ✅ Step 3: Reset on open
        resetCalculator()

        // ✅ Step 4: Button listeners
        resetButton.setOnClickListener {
            resetCalculator()
        }

        splitButton.setOnClickListener {

            val peopleText = peopleCount.text.toString()

            if (calcCurrent.isEmpty() || peopleText.isEmpty()) {
                calcDisplay.text = "Enter values"
                return@setOnClickListener
            }

            val amount = calcCurrent.toDoubleOrNull()
            val people = peopleText.toIntOrNull()

            if (amount == null || people == null || people == 0) {
                calcDisplay.text = "Invalid input"
                return@setOnClickListener
            }

            val splitAmount = amount / people
            val rounded = String.format("%.2f", splitAmount)

            calcCurrent = rounded
            calcDisplay.text = "Each: ₹$rounded"
        }

        // ✅ Step 5: Show dialog FIRST
        calcDialog.show()

        // ✅ Step 6: Then adjust size
        calcDialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    fun resetCalculator() {
        calcCurrent = ""
        calcOperator = ""
        calcFirst = 0.0

        calcDisplay.text = "0"

        if (::peopleCount.isInitialized) {
            peopleCount.setText("")
        }
    }

    fun onCalcNumber(view: View) {
        val btn = view as Button

        if (calcCurrent == "0") {
            calcCurrent = btn.text.toString()
        } else {
            calcCurrent += btn.text.toString()
        }

        calcDisplay.text = calcCurrent
    }

    fun onCalcOperator(view: android.view.View) {
        val btn = view as android.widget.Button
        val value = calcCurrent.toDoubleOrNull() ?: 0.0

        if (calcOperator.isNotEmpty()) {
            calcFirst = calculate(calcFirst, value, calcOperator)
        } else {
            calcFirst = value
        }

        calcOperator = btn.text.toString()
        calcCurrent = ""
    }

    fun calculate(a: Double, b: Double, operator: String): Double {
        return when (operator) {
            "+" -> a + b
            "-" -> a - b
            "*" -> a * b
            "/" -> if (b != 0.0) a / b else 0.0
            else -> b
        }
    }

    fun onCalcEqual(view: android.view.View) {
        val second = calcCurrent.toDoubleOrNull() ?: 0.0

        val result = calculate(calcFirst, second, calcOperator)

        val rounded = String.format("%.2f", result)

        calcCurrent = rounded
        calcDisplay.text = rounded

        calcOperator = ""
    }

    fun onCalcClear(view: View) {
        calcCurrent = ""
        calcDisplay.text = "0"
    }

    fun useCalcResult(view: View) {
        inputField.setText("₹$calcCurrent")
        calcDialog.dismiss()
    }
}
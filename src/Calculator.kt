import java.awt.*
import javax.swing.*
//https://www.youtube.com/watch?v=DSus4EW_DiA&t=204s
//https://www.youtube.com/@BroCodez
//https://www.tutorialspoint.com/swing/index.htm
class Calculator {
    // Properties
    private val jFrame = JFrame()
    private val jTextField = JTextField()
    private var firstNumber: Double? = null
    private var currentOperator: String? = null
    private var resetTextField = false

    // Button constants
    private val buttonLabels = arrayOf(
        "C", "±", "%", "÷",
        "7", "8", "9", "×",
        "4", "5", "6", "-",
        "1", "2", "3", "+",
        "0", ".", "="
    )

    private val buttons = mutableListOf<JButton>()

    init {
        initializeFrame()
        initializeTextField()
        initializeButtons()
    }

    private fun initializeFrame() {
        with(jFrame) {
            title = "Simple Calculator"
            isResizable = false
            defaultCloseOperation = JFrame.EXIT_ON_CLOSE
            setSize(400, 600)
            layout = null
            setLocationRelativeTo(null)
            background = Color(240, 240, 240)
        }
    }

    private fun initializeTextField() {
        val textFieldFont = Font("Arial", Font.BOLD, 32)
        with(jTextField) {
            setBounds(20, 20, 340, 70)
            font = textFieldFont
            horizontalAlignment = SwingConstants.RIGHT
            isEditable = false
            border = BorderFactory.createLineBorder(Color(180, 180, 180), 2)
            text = "0"
            background = Color(255, 255, 255)
            foreground = Color(50, 50, 50)
            isFocusable = false
        }

        jFrame.add(jTextField)
    }

    private fun initializeButtons() {
        val buttonWidth = 80
        val buttonHeight = 70
        var x = 20
        var y = 110

        for ((index, label) in buttonLabels.withIndex()) {
            val button = createButton(label)

            // Handle special button sizing for "0"
            if (label == "0") {
                button.setBounds(x, y, buttonWidth * 2 + 10, buttonHeight)
            } else {
                button.setBounds(x, y, buttonWidth, buttonHeight)
            }

            // Add action listener
            button.addActionListener { handleButtonClick(label) }

            buttons.add(button)
            jFrame.add(button)

            // Update position for next button
            x += if (label == "0") buttonWidth * 2 + 10 else buttonWidth + 10

            // Move to next row
            if ((index + 1) % 4 == 0 && label != "0") {
                x = 20
                y += buttonHeight + 10
            }
        }

        // Position the last row buttons (. and =)
        buttons.find { it.text == "." }?.setBounds(180, y, buttonWidth, buttonHeight)
        buttons.find { it.text == "=" }?.setBounds(270, y, buttonWidth, buttonHeight)
    }

    private fun createButton(label: String): JButton {
        val button = JButton(label)

        // Set button colors based on type
        when (label) {
            in listOf("÷", "×", "-", "+", "=") -> {
                // Operation buttons (orange)
                button.background = Color(255, 149, 0)
                button.foreground = Color.WHITE
            }
            in listOf("C", "±", "%") -> {
                // Function buttons (light gray)
                button.background = Color(200, 200, 200)
                button.foreground = Color.BLACK
            }
            else -> {
                // Number buttons (dark gray)
                button.background = Color(50, 50, 50)
                button.foreground = Color.WHITE
            }
        }

        button.font = Font("Arial", Font.BOLD, 20)
        button.border = BorderFactory.createRaisedBevelBorder()
        button.isFocusPainted = false

        // Add hover effect
        button.addMouseListener(object : java.awt.event.MouseAdapter() {
            override fun mouseEntered(e: java.awt.event.MouseEvent) {
                button.background = button.background.brighter()
            }

            override fun mouseExited(e: java.awt.event.MouseEvent) {
                // Restore original color based on type
                when {
                    label in listOf("÷", "×", "-", "+", "=") ->
                        button.background = Color(255, 149, 0)
                    label in listOf("C", "±", "%") ->
                        button.background = Color(200, 200, 200)
                    else ->
                        button.background = Color(50, 50, 50)
                }
            }
        })

        return button
    }

    private fun handleButtonClick(label: String) {
        when {
            label in listOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9") ->
                handleNumberClick(label)

            label == "." -> handleDecimalClick()

            label in listOf("÷", "×", "-", "+") ->
                handleOperatorClick(label)

            label == "=" -> calculateResult()

            label == "C" -> clearCalculator()

            label == "±" -> toggleSign()

            label == "%" -> calculatePercentage()
        }
    }

    private fun handleNumberClick(number: String) {
        if (resetTextField || jTextField.text == "0") {
            jTextField.text = number
            resetTextField = false
        } else {
            jTextField.text += number
        }
    }

    private fun handleDecimalClick() {
        if (resetTextField) {
            jTextField.text = "0."
            resetTextField = false
        } else if (!jTextField.text.contains(".")) {
            jTextField.text += "."
        }
    }

    private fun handleOperatorClick(operator: String) {
        try {
            val currentValue = jTextField.text.toDouble()

            if (firstNumber == null) {
                firstNumber = currentValue
            } else if (!resetTextField && currentOperator != null) {
                calculateResult()
                firstNumber = jTextField.text.toDouble()
            }

            currentOperator = operator
            resetTextField = true
        } catch (e: NumberFormatException) {
            jTextField.text = "Error"
            resetTextField = true
        }
    }

    private fun calculateResult() {
        if (firstNumber == null || currentOperator == null || resetTextField) {
            return
        }

        try {
            val secondNumber = jTextField.text.toDouble()
            val result = when (currentOperator) {
                "÷" -> if (secondNumber != 0.0) firstNumber!! / secondNumber else Double.NaN
                "×" -> firstNumber!! * secondNumber
                "-" -> firstNumber!! - secondNumber
                "+" -> firstNumber!! + secondNumber
                else -> secondNumber
            }

            if (result.isNaN()) {
                jTextField.text = "Error"
            } else {
                // Remove trailing .0 if it's an integer
                jTextField.text = if (result % 1 == 0.0) {
                    result.toLong().toString()
                } else {
                    String.format("%.8f", result).trimEnd('0').trimEnd('.')
                }
            }

            firstNumber = null
            currentOperator = null
            resetTextField = true
        } catch (e: Exception) {
            jTextField.text = "Error"
            resetTextField = true
            firstNumber = null
            currentOperator = null
        }
    }

    private fun clearCalculator() {
        jTextField.text = "0"
        firstNumber = null
        currentOperator = null
        resetTextField = false
    }

    private fun toggleSign() {
        try {
            val currentValue = jTextField.text.toDouble()
            jTextField.text = if (currentValue % 1 == 0.0) {
                (-currentValue).toLong().toString()
            } else {
                (-currentValue).toString()
            }
        } catch (e: NumberFormatException) {
            jTextField.text = "Error"
        }
    }

    private fun calculatePercentage() {
        try {
            val currentValue = jTextField.text.toDouble()
            jTextField.text = (currentValue / 100).toString()
        } catch (e: NumberFormatException) {
            jTextField.text = "Error"
        }
    }

    fun show() {
        jFrame.isVisible = true
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SwingUtilities.invokeLater {
                Calculator().show()
            }
        }
    }
}
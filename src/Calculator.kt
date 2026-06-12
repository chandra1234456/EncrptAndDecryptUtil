import java.awt.*
import javax.swing.*
import javax.swing.border.EmptyBorder

class Calculator {

    private val jFrame = JFrame()
    private val jTextField = JTextField()
    private val historyLabel = JLabel(" ")
    private var firstNumber: Double? = null
    private var currentOperator: String? = null
    private var resetTextField = false

    private val bgColor = Color(28, 28, 30)
    private val displayBg = Color(28, 28, 30)
    private val numColor = Color(64, 64, 66)
    private val numColorPressed = Color(90, 90, 92)
    private val funcColor = Color(165, 165, 165)
    private val funcColorPressed = Color(200, 200, 200)
    private val opColor = Color(255, 149, 0)
    private val opColorPressed = Color(255, 180, 80)
    private val opColorActive = Color.WHITE

    private val buttonLabels = arrayOf(
        "C", "±", "%", "÷",
        "7", "8", "9", "×",
        "4", "5", "6", "-",
        "1", "2", "3", "+",
        "0", ".", "="
    )

    private val buttons = mutableMapOf<String, JButton>()
    private var activeOperatorButton: JButton? = null

    init {
        initializeFrame()
        initializeDisplay()
        initializeButtons()
    }

    private fun initializeFrame() {
        with(jFrame) {
            title = "Calculator"
            isResizable = false
            defaultCloseOperation = JFrame.EXIT_ON_CLOSE
            setSize(400, 650)
            layout = null
            setLocationRelativeTo(null)
            contentPane.background = bgColor
        }
    }

    private fun initializeDisplay() {
        historyLabel.setBounds(20, 30, 320, 25)
        historyLabel.font = Font("Arial", Font.PLAIN, 18)
        historyLabel.foreground = Color(150, 150, 150)
        historyLabel.horizontalAlignment = SwingConstants.RIGHT
        jFrame.add(historyLabel)

        with(jTextField) {
            setBounds(20, 60, 320, 90)
            font = Font("Arial", Font.BOLD, 48)
            horizontalAlignment = SwingConstants.RIGHT
            isEditable = false
            border = EmptyBorder(0, 0, 0, 10)
            text = "0"
            background = displayBg
            foreground = Color.WHITE
            isFocusable = false
        }
        jFrame.add(jTextField)
    }

    private fun initializeButtons() {
        val buttonSize = 75
        val gap = 12
        var x = 20
        var y = 180

        for ((index, label) in buttonLabels.withIndex()) {
            val button = createButton(label)

            if (label == "0") {
                button.setBounds(x, y, buttonSize * 2 + gap, buttonSize)
            } else {
                button.setBounds(x, y, buttonSize, buttonSize)
            }

            button.addActionListener { handleButtonClick(label) }

            buttons[label] = button
            jFrame.add(button)

            x += if (label == "0") buttonSize * 2 + gap else buttonSize + gap

            if ((index + 1) % 4 == 0 && label != "0") {
                x = 20
                y += buttonSize + gap
            }
        }

        buttons["."]?.setBounds(180, y, buttonSize, buttonSize)
        buttons["="]?.setBounds(272, y, buttonSize, buttonSize)
    }

    private fun createButton(label: String): JButton {
        val button = JButton(label)

        val (bg, fg) = when (label) {
            in listOf("÷", "×", "-", "+", "=") -> opColor to Color.WHITE
            in listOf("C", "±", "%") -> funcColor to Color.BLACK
            else -> numColor to Color.WHITE
        }

        button.background = bg
        button.foreground = fg
        button.font = Font("Arial", Font.PLAIN, 26)
        button.isFocusPainted = false
        button.isBorderPainted = false
        button.isOpaque = true
        button.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)

        // Make circular by overriding paint
        button.putClientProperty("flatLook", true)

        button.addMouseListener(object : java.awt.event.MouseAdapter() {
            override fun mouseEntered(e: java.awt.event.MouseEvent) {
                if (button == activeOperatorButton) return
                button.background = when (label) {
                    in listOf("÷", "×", "-", "+", "=") -> opColorPressed
                    in listOf("C", "±", "%") -> funcColorPressed
                    else -> numColorPressed
                }
            }

            override fun mouseExited(e: java.awt.event.MouseEvent) {
                if (button == activeOperatorButton) return
                button.background = when (label) {
                    in listOf("÷", "×", "-", "+", "=") -> opColor
                    in listOf("C", "±", "%") -> funcColor
                    else -> numColor
                }
            }
        })

        // Circular shape
        button.border = null
        button.isContentAreaFilled = false
        button.addPropertyChangeListener { jFrame.repaint() }

        return RoundButton(label, bg, fg).also { rb ->
            rb.addActionListener { handleButtonClick(label) }
        }
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
        highlightOperator()
    }

    private fun handleNumberClick(number: String) {
        if (resetTextField || jTextField.text == "0") {
            jTextField.text = number
            resetTextField = false
        } else {
            if (jTextField.text.replace("-", "").replace(".", "").length >= 15) return
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
            historyLabel.text = "${formatNumber(firstNumber!!)} $operator"
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

            historyLabel.text = "${formatNumber(firstNumber!!)} $currentOperator ${formatNumber(secondNumber)} ="

            jTextField.text = if (result.isNaN()) "Error" else formatNumber(result)

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

    private fun formatNumber(value: Double): String {
        return if (value % 1 == 0.0 && Math.abs(value) < 1e15) {
            value.toLong().toString()
        } else {
            String.format("%.8f", value).trimEnd('0').trimEnd('.')
        }
    }

    private fun clearCalculator() {
        jTextField.text = "0"
        historyLabel.text = " "
        firstNumber = null
        currentOperator = null
        resetTextField = false
    }

    private fun toggleSign() {
        try {
            val currentValue = jTextField.text.toDouble()
            jTextField.text = formatNumber(-currentValue)
        } catch (e: NumberFormatException) {
            jTextField.text = "Error"
        }
    }

    private fun calculatePercentage() {
        try {
            val currentValue = jTextField.text.toDouble()
            jTextField.text = formatNumber(currentValue / 100)
        } catch (e: NumberFormatException) {
            jTextField.text = "Error"
        }
    }

    private fun highlightOperator() {
        activeOperatorButton?.let {
            (it as? RoundButton)?.resetColor()
        }
        if (currentOperator != null && resetTextField) {
            val btn = buttons[currentOperator] as? RoundButton
            btn?.setActive(true)
            activeOperatorButton = btn
        } else {
            activeOperatorButton = null
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

/**
 * A custom JButton painted as a filled circle with hover/active states.
 */
class RoundButton(text: String, private val baseColor: Color, private val baseFg: Color) : JButton(text) {

    private var hovered = false
    private var active = false
    private val activeColor = Color.WHITE
    private val activeFg = baseColor
    private val hoverColor = baseColor.brighter()

    init {
        isContentAreaFilled = false
        isFocusPainted = false
        isBorderPainted = false
        cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        font = Font("Arial", Font.PLAIN, 26)
        foreground = baseFg

        addMouseListener(object : java.awt.event.MouseAdapter() {
            override fun mouseEntered(e: java.awt.event.MouseEvent) {
                hovered = true
                repaint()
            }
            override fun mouseExited(e: java.awt.event.MouseEvent) {
                hovered = false
                repaint()
            }
        })
    }

    fun setActive(value: Boolean) {
        active = value
        foreground = if (active) activeFg else baseFg
        repaint()
    }

    fun resetColor() {
        active = false
        foreground = baseFg
        repaint()
    }

    override fun paintComponent(g: Graphics) {
        val g2 = g.create() as Graphics2D
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        val color = when {
            active -> activeColor
            hovered -> hoverColor
            else -> baseColor
        }

        g2.color = color
        val size = minOf(width, height)
        val x = (width - size) / 2
        val y = (height - size) / 2

        if (size == width) {
            // wide "0" button -> rounded rectangle
            g2.fillRoundRect(0, 0, width, height, height, height)
        } else {
            g2.fillOval(x, y, size, size)
        }

        g2.dispose()
        super.paintComponent(g)
    }

    override fun contains(x: Int, y: Int): Boolean {
        return if (width != height) {
            super.contains(x, y)
        } else {
            val center = width / 2.0
            val dx = x - center
            val dy = y - center
            dx * dx + dy * dy <= center * center
        }
    }
}
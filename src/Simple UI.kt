import util.formatJson
import util.isValidJson
import util.showToast
import java.awt.Component
import java.awt.Dimension
import javax.swing.*


class SimpleUI {
    //private val secretKey = AESUtils.generateKey()  // ✅ ONE KEY

    private val jFrame = JFrame()
    private val inputArea = JTextArea(5, 20)
    private val outPutArea = JTextArea(5, 20)
    private val encryptButton = JButton("Encrypt")
    private val decryptButton = JButton("Decrypt")
    private val clearButton = JButton("Clear")
    private val inPutJsonBeautifier = JButton("Json Beautify")
    private val inPutXmlBeautifier = JButton("XML Beautify")
    private val outPutJsonBeautifier = JButton("Json Beautify")
    private val outPutXmlBeautifier = JButton("XML Beautify")

    init {
        initializeFrame()
        initializeTextAreas()
        initializeButtons()
        addFrames()
        addButtonListeners()
    }

    private fun initializeFrame() {
        with(jFrame) {
            title = "Simple UI"
            defaultCloseOperation = JFrame.EXIT_ON_CLOSE
            setSize(700, 300)
            setLocationRelativeTo(null)
        }
    }

    private fun initializeTextAreas() {
        // Wrap lines and make output read-only
        inputArea.lineWrap = true
        inputArea.wrapStyleWord = true

        outPutArea.lineWrap = true
        outPutArea.wrapStyleWord = true
        outPutArea.isEditable = false

        // Limit maximum width so horizontal gap doesn’t grow
        inputArea.maximumSize = Dimension(250, Int.MAX_VALUE)
        outPutArea.maximumSize = Dimension(250, Int.MAX_VALUE)
    }

    private fun initializeButtons() {
        encryptButton.alignmentX = Component.CENTER_ALIGNMENT
        decryptButton.alignmentX = Component.CENTER_ALIGNMENT
        clearButton.alignmentX = Component.CENTER_ALIGNMENT

        // Limit button panel width
        encryptButton.maximumSize = Dimension(120, 50)
        decryptButton.maximumSize = Dimension(120, 50)
        clearButton.maximumSize = Dimension(120, 50)
    }

    private fun addButtonListeners() {
        encryptButton.addActionListener {
            val value = inputArea.text.trim()
            if (value.isNotBlank()){
            val password = "MyStrongPassword123!"
            val encrypted = AESUtils.encrypt(inputArea.text.trim(), password)
            println("Encrypted: $encrypted")
            // DO NOT modify Base64 string
            outPutArea.text = encrypted
            }else{
                JOptionPane.showMessageDialog(jFrame,
                    "While Encrypting Input Filed should not be empty",
                    "Simple UI",
                    JOptionPane.ERROR_MESSAGE)
            }
        }

        decryptButton.addActionListener {
            val value = inputArea.text.trim()
            if (value.isNotBlank()) {
                val password = "MyStrongPassword123!"
                val decrypted = AESUtils.decrypt(inputArea.text.trim(), password)
                println("Decrypted: $decrypted")
                // You can lowercase only for display, NOT for decryption
                outPutArea.text = decrypted
            } else {
                JOptionPane.showMessageDialog(
                    jFrame,
                    "While Decrypting Input Filed should not be empty",
                    "Simple UI",
                    JOptionPane.ERROR_MESSAGE
                )
            }
        }
        clearButton.addActionListener {
            outPutArea.text = ""
            inputArea.text = ""
            showToast(jFrame, "Data Cleared", 2500)
        }

        inPutJsonBeautifier.addActionListener {
            val json = inputArea.text.toString()
            if (!json.isNullOrBlank()){
              val actualJson =  isValidJson(json)
                if (actualJson) {
                    val output = formatJson(json)
                    inputArea.text = output
                }

            }else{
                showToast(jFrame, "Please Add Json", 2500)
            }
        }

    }
    private fun addFrames() {
        val mainPanel = JPanel()
        mainPanel.layout = BoxLayout(mainPanel, BoxLayout.X_AXIS)
        mainPanel.border = BorderFactory.createEmptyBorder(20, 20, 20, 20)

        /* =========================
           INPUT COLUMN
           ========================= */
        val inputColumn = JPanel()
        inputColumn.layout = BoxLayout(inputColumn, BoxLayout.Y_AXIS)
        inputColumn.alignmentX = Component.RIGHT_ALIGNMENT

        // Set text area properties BEFORE adding to scroll pane
        inputArea.preferredSize = Dimension(400, 300)
        inputArea.minimumSize = Dimension(200, 200)
        inputArea.maximumSize = Dimension(Int.MAX_VALUE, Int.MAX_VALUE)

        val inputScrollPane = JScrollPane(inputArea)
        inputScrollPane.preferredSize = Dimension(400, 300)
        inputScrollPane.minimumSize = Dimension(200, 200)
        inputScrollPane.maximumSize = Dimension(Int.MAX_VALUE, Int.MAX_VALUE)
        inputScrollPane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
        inputScrollPane.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED

        inputColumn.add(inputScrollPane)
        inputColumn.add(Box.createRigidArea(Dimension(0, 10))) // Better than glue for spacing

        val inputBeautifierPanel = JPanel()
        inputBeautifierPanel.layout = BoxLayout(inputBeautifierPanel, BoxLayout.X_AXIS)
        inputBeautifierPanel.maximumSize = Dimension(Int.MAX_VALUE, 40) // Limit height

        inputBeautifierPanel.add(Box.createHorizontalGlue())
        inputBeautifierPanel.add(inPutXmlBeautifier)
        inputBeautifierPanel.add(Box.createRigidArea(Dimension(10, 0)))
        inputBeautifierPanel.add(inPutJsonBeautifier)
        inputBeautifierPanel.add(Box.createHorizontalGlue())

        inputColumn.add(inputBeautifierPanel)

        /* =========================
           BUTTON COLUMN
           ========================= */
        val buttonColumn = JPanel()
        buttonColumn.layout = BoxLayout(buttonColumn, BoxLayout.Y_AXIS)
        buttonColumn.alignmentX = Component.CENTER_ALIGNMENT

        // Set button sizes
        encryptButton.preferredSize = Dimension(120, 35)
        decryptButton.preferredSize = Dimension(120, 35)
        clearButton.preferredSize = Dimension(120, 35)

        buttonColumn.add(Box.createVerticalGlue())

        encryptButton.alignmentX = Component.CENTER_ALIGNMENT
        decryptButton.alignmentX = Component.CENTER_ALIGNMENT
        clearButton.alignmentX = Component.CENTER_ALIGNMENT

        buttonColumn.add(encryptButton)
        buttonColumn.add(Box.createRigidArea(Dimension(0, 10)))
        buttonColumn.add(decryptButton)
        buttonColumn.add(Box.createRigidArea(Dimension(0, 10)))
        buttonColumn.add(clearButton)

        buttonColumn.add(Box.createVerticalGlue())

        /* =========================
           OUTPUT COLUMN
           ========================= */
        val outputColumn = JPanel()
        outputColumn.layout = BoxLayout(outputColumn, BoxLayout.Y_AXIS)
        outputColumn.alignmentX = Component.LEFT_ALIGNMENT

        // Set output area properties
        outPutArea.preferredSize = Dimension(400, 300)
        outPutArea.minimumSize = Dimension(200, 200)
        outPutArea.maximumSize = Dimension(Int.MAX_VALUE, Int.MAX_VALUE)

        val outputScrollPane = JScrollPane(outPutArea)
        outputScrollPane.preferredSize = Dimension(400, 300)
        outputScrollPane.minimumSize = Dimension(200, 200)
        outputScrollPane.maximumSize = Dimension(Int.MAX_VALUE, Int.MAX_VALUE)
        outputScrollPane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
        outputScrollPane.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED

        outputColumn.add(outputScrollPane)
        outputColumn.add(Box.createRigidArea(Dimension(0, 10)))

        val outputBeautifierPanel = JPanel()
        outputBeautifierPanel.layout = BoxLayout(outputBeautifierPanel, BoxLayout.X_AXIS)
        outputBeautifierPanel.maximumSize = Dimension(Int.MAX_VALUE, 40)

        outputBeautifierPanel.add(Box.createHorizontalGlue())
        outputBeautifierPanel.add(outPutXmlBeautifier)
        outputBeautifierPanel.add(Box.createRigidArea(Dimension(10, 0)))
        outputBeautifierPanel.add(outPutJsonBeautifier)
        outputBeautifierPanel.add(Box.createHorizontalGlue())

        outputColumn.add(outputBeautifierPanel)

        /* =========================
           ADD TO MAIN PANEL
           ========================= */
        mainPanel.add(inputColumn)
        mainPanel.add(Box.createRigidArea(Dimension(15, 0)))
        mainPanel.add(buttonColumn)
        mainPanel.add(Box.createRigidArea(Dimension(15, 0)))
        mainPanel.add(outputColumn)

        jFrame.contentPane.add(mainPanel)

        // Add this for proper sizing
        jFrame.pack()
        jFrame.minimumSize = Dimension(900, 500) // Set minimum window size
    }

    private fun expandVertically(panel: JComponent) {
        panel.maximumSize = Dimension(Int.MAX_VALUE, Int.MAX_VALUE)
    }


    fun show() {
        jFrame.isVisible = true
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SwingUtilities.invokeLater {
                SimpleUI().show()
            }
        }
    }
}

import util.colorJsonInTextPane
import util.getDecryptValue
import util.getEncryptValue
import util.isValidJson
import util.showToast
import java.awt.*
import javax.swing.JButton
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*
import javax.swing.border.AbstractBorder
import javax.swing.border.EmptyBorder

// ─────────────────────────────────────────────
//  THEME CONSTANTS
// ─────────────────────────────────────────────
object Theme {
    val BG_DEEP        = Color(0x0D, 0x11, 0x17)
    val BG_PANEL       = Color(0x13, 0x19, 0x22)
    val BG_INPUT       = Color(0x0A, 0x0E, 0x14)
    val BORDER_SUBTLE  = Color(0x1E, 0x28, 0x38)
    val ACCENT_CYAN    = Color(0x00, 0xD4, 0xFF)
    val ACCENT_GREEN   = Color(0x00, 0xFF, 0x9F)
    val ACCENT_ORANGE  = Color(0xFF, 0x6B, 0x35)
    val ACCENT_PURPLE  = Color(0xBD, 0x93, 0xF9)
    val TEXT_PRIMARY   = Color(0xE8, 0xF4, 0xFF)
    val TEXT_SECONDARY = Color(0x6A, 0x83, 0xA0)
    val TEXT_MONO      = Color(0xAB, 0xDB, 0xFF)

    val FONT_MONO = Font("JetBrains Mono", Font.PLAIN, 13).let { requested ->
        val families = GraphicsEnvironment.getLocalGraphicsEnvironment().availableFontFamilyNames.toSet()
        when {
            "JetBrains Mono" in families -> requested
            "Cascadia Code"  in families -> Font("Cascadia Code", Font.PLAIN, 13)
            "Fira Code"      in families -> Font("Fira Code",     Font.PLAIN, 13)
            "Consolas"       in families -> Font("Consolas",      Font.PLAIN, 13)
            else                         -> Font(Font.MONOSPACED,  Font.PLAIN, 13)
        }
    }
    val FONT_UI_BOLD = Font("Segoe UI", Font.BOLD, 12)
    val FONT_UI      = Font("Segoe UI", Font.PLAIN, 12)
    val FONT_LABEL   = Font("Segoe UI", Font.BOLD, 11)
}

// ─────────────────────────────────────────────
//  ROUNDED BORDER UTILITY
// ─────────────────────────────────────────────
class RoundedBorder(
    private val radius: Int,
    private val color: Color,
    private val thickness: Float = 1f
) : AbstractBorder() {
    override fun paintBorder(c: Component, g: Graphics, x: Int, y: Int, width: Int, height: Int) {
        val g2 = g.create() as Graphics2D
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2.color = color
        g2.stroke = BasicStroke(thickness)
        g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius)
        g2.dispose()
    }
    override fun getBorderInsets(c: Component) = Insets(8, 12, 8, 12)
    override fun getBorderInsets(c: Component, insets: Insets): Insets {
        insets.set(8, 12, 8, 12); return insets
    }
}

// ─────────────────────────────────────────────
//  STYLED BUTTON
// ─────────────────────────────────────────────
class StyledButton(
    text: String,
    private val accent: Color,
    private val icon: String = ""
) : JButton(if (icon.isNotEmpty()) "$icon  $text" else text) {

    private var hovered = false
    private var pressed = false

    init {
        isContentAreaFilled = false
        isFocusPainted      = false
        isBorderPainted     = false
        isOpaque            = false
        foreground          = accent
        font                = Theme.FONT_UI_BOLD
        cursor              = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)

        addMouseListener(object : MouseAdapter() {
            override fun mouseEntered(e: MouseEvent)  { hovered = true;  repaint() }
            override fun mouseExited(e: MouseEvent)   { hovered = false; repaint() }
            override fun mousePressed(e: MouseEvent)  { pressed = true;  repaint() }
            override fun mouseReleased(e: MouseEvent) { pressed = false; repaint() }
        })
    }

    override fun paintComponent(g: Graphics) {
        val g2 = g.create() as Graphics2D
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        val arc = 10

        // FIX: blend toward BG_PANEL instead of black so hover is visible on dark theme
        val bgColor = when {
            pressed -> blendToward(accent, Theme.BG_PANEL, 0.30)
            hovered -> blendToward(accent, Theme.BG_PANEL, 0.18)
            else    -> blendToward(accent, Theme.BG_PANEL, 0.10)
        }

        g2.color = bgColor
        g2.fillRoundRect(0, 0, width, height, arc, arc)

        g2.color  = accent
        g2.stroke = BasicStroke(if (pressed) 2f else 1.2f)
        g2.drawRoundRect(1, 1, width - 2, height - 2, arc, arc)
        g2.dispose()

        super.paintComponent(g)
    }

    /**
     * Blend [base] toward [target] by [factor] (0.0 = pure target, 1.0 = pure base).
     * Replaces the old blend-to-black helper so buttons stay visible on dark backgrounds.
     */
    private fun blendToward(base: Color, target: Color, factor: Double): Color {
        val f = factor.coerceIn(0.0, 1.0)
        return Color(
            (base.red   * f + target.red   * (1 - f)).toInt().coerceIn(0, 255),
            (base.green * f + target.green * (1 - f)).toInt().coerceIn(0, 255),
            (base.blue  * f + target.blue  * (1 - f)).toInt().coerceIn(0, 255)
        )
    }
}

// ─────────────────────────────────────────────
//  STYLED SCROLL PANE
// ─────────────────────────────────────────────
fun styledScrollPane(view: Component, accentColor: Color): JScrollPane {
    return JScrollPane(view).apply {
        border     = RoundedBorder(10, accentColor, 1f)
        background = Theme.BG_INPUT
        viewport.background = Theme.BG_INPUT

        verticalScrollBar.apply {
            preferredSize = Dimension(6, 0)
            isOpaque      = false
            setUI(object : javax.swing.plaf.basic.BasicScrollBarUI() {
                override fun configureScrollBarColors() {
                    thumbColor = Color(accentColor.red, accentColor.green, accentColor.blue, 90)
                    trackColor = Theme.BG_INPUT
                }
                override fun createDecreaseButton(o: Int) = JButton().apply { preferredSize = Dimension(0, 0) }
                override fun createIncreaseButton(o: Int) = JButton().apply { preferredSize = Dimension(0, 0) }
            })
        }
        horizontalScrollBar.preferredSize    = Dimension(0, 5)
        verticalScrollBarPolicy              = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
        horizontalScrollBarPolicy            = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
    }
}

// ─────────────────────────────────────────────
//  SECTION LABEL
// ─────────────────────────────────────────────
fun sectionLabel(text: String, dot: Color): JPanel {
    return JPanel().apply {
        layout  = BoxLayout(this, BoxLayout.X_AXIS)
        isOpaque = false
        add(JLabel("●").apply {
            font      = Font(Font.SANS_SERIF, Font.PLAIN, 8)
            foreground = dot
        })
        add(JLabel("  $text").apply {
            font      = Theme.FONT_LABEL
            foreground = Theme.TEXT_SECONDARY
        })
        add(Box.createHorizontalGlue())
    }
}

// ─────────────────────────────────────────────
//  MAIN UI CLASS
// ─────────────────────────────────────────────
class SimpleUI {

    private val jFrame               = JFrame()
    private val inputArea            = JTextPane()
    private val outPutArea           = JTextPane()
    private val installationKeyField = JTextField(10)

    // FIX: LogPanel promoted to class field so it is accessible from both
    //      addButtonListeners() and addFrames() without being re-created.
    private val logPanel = LogPanel()

    private val encryptButton        = StyledButton("Encrypt", Theme.ACCENT_PURPLE, "")
    private val decryptButton        = StyledButton("Decrypt", Theme.ACCENT_PURPLE, "")
    private val clearButton          = StyledButton("Clear",   Theme.ACCENT_PURPLE, "X")
    private val inPutJsonBeautifier  = StyledButton("JSON",    Theme.ACCENT_PURPLE, "{}")
    private val inPutXmlBeautifier   = StyledButton("XML",     Theme.ACCENT_PURPLE, "</>")
    private val outPutJsonBeautifier = StyledButton("JSON",    Theme.ACCENT_PURPLE, "{}")
    private val outPutXmlBeautifier  = StyledButton("XML",     Theme.ACCENT_PURPLE, "</>")

    // FIX: logsBtn and logBadge promoted to fields so addFrames() can embed them
    //      in the title bar without orphaned add() calls inside addButtonListeners().
    private val logsBtn = StyledButton("Logs", Theme.ACCENT_CYAN, "ⓘ").apply {
        preferredSize = Dimension(90, 30)
        maximumSize   = Dimension(90, 30)
    }
    private val logBadge = JLabel().apply {
        font       = Font(Font.SANS_SERIF, Font.BOLD, 9)
        foreground = Color.WHITE
        background = Color(0xE5, 0x39, 0x35)
        isOpaque   = true
        border     = EmptyBorder(1, 5, 1, 5)
    }

    init {
        setGlobalUIDefaults()
        initializeFrame()
        initializeTextAreas()
        initializeButtons()
        addFrames()           // build UI structure first
        addButtonListeners()  // then wire up listeners
        syncLogBadge()
    }

    // ── Keeps the badge label in sync with the LogPanel's unread count ──
    private fun syncLogBadge() {
        val count = logPanel.getBadgeLabel().text
        logBadge.text      = count
        logBadge.isVisible = count.isNotBlank() && count != "0"
    }

    private fun setGlobalUIDefaults() {
        UIManager.put("OptionPane.background",        Theme.BG_PANEL)
        UIManager.put("Panel.background",             Theme.BG_PANEL)
        UIManager.put("OptionPane.messageForeground", Theme.TEXT_PRIMARY)
        UIManager.put("Button.background",            Theme.BG_PANEL)
        UIManager.put("Button.foreground",            Theme.TEXT_PRIMARY)
    }

    private fun initializeFrame() {
        with(jFrame) {
            title                  = "  ⬡  CryptoTool  —  AES Encrypt / Decrypt"
            defaultCloseOperation  = JFrame.EXIT_ON_CLOSE
            setSize(1000, 580)
            setLocationRelativeTo(null)
            contentPane.background = Theme.BG_DEEP
        }
    }

    private fun initializeTextAreas() {
        inputArea.apply {
            background = Theme.BG_INPUT
            foreground = Theme.TEXT_MONO
            caretColor = Theme.ACCENT_CYAN
            font       = Theme.FONT_MONO
            border     = EmptyBorder(12, 14, 12, 14)
        }
        outPutArea.apply {
            background = Theme.BG_INPUT
            foreground = Theme.ACCENT_GREEN
            caretColor = Theme.ACCENT_GREEN
            font       = Theme.FONT_MONO
            isEditable = false
            border     = EmptyBorder(12, 14, 12, 14)
        }
        installationKeyField.apply {
            background = Theme.BG_INPUT
            foreground = Theme.TEXT_PRIMARY
            caretColor = Theme.ACCENT_CYAN
            font       = Theme.FONT_MONO
            border     = RoundedBorder(8, Theme.ACCENT_CYAN)
        }
    }

    private fun initializeButtons() {
        val btnSize  = Dimension(130, 38)
        val smallBtn = Dimension(90, 30)

        for (b in listOf(encryptButton, decryptButton, clearButton)) {
            b.preferredSize = btnSize
            b.maximumSize   = btnSize
            b.alignmentX    = Component.CENTER_ALIGNMENT
        }
        for (b in listOf(inPutJsonBeautifier, inPutXmlBeautifier, outPutJsonBeautifier, outPutXmlBeautifier)) {
            b.preferredSize = smallBtn
            b.maximumSize   = smallBtn
        }
    }

    private fun addButtonListeners() {
        // ── Encrypt ──────────────────────────────────────────────────
        encryptButton.addActionListener {
            val value = inputArea.text.trim()
            if (value.isNotBlank()) {
                try {
                    val encrypted = getEncryptValue(value, installationKeyField.text.trim())
                    outPutArea.text = encrypted
                    logPanel.info("Encrypt successful (${value.length} chars)")
                } catch (e: Exception) {
                    logPanel.error("Encrypt failed: ${e.message}", e)
                    showStyledDialog("Encryption error: ${e.message}")
                }
                syncLogBadge()
            } else {
                showStyledDialog("Input field must not be empty before encrypting.")
            }
        }

        // ── Decrypt ──────────────────────────────────────────────────
        decryptButton.addActionListener {
            val value = inputArea.text.trim()
            if (value.isNotBlank()) {
                try {
                    val decrypted = getDecryptValue(value, installationKeyField.text.trim())
                    outPutArea.text = decrypted
                    logPanel.info("Decrypt successful")
                } catch (e: Exception) {
                    logPanel.error("Decrypt failed: ${e.message}", e)
                    showStyledDialog("Decryption error: ${e.message}")
                }
                syncLogBadge()
            } else {
                showStyledDialog("Input field must not be empty before decrypting.")
            }
        }

        // ── Clear ────────────────────────────────────────────────────
        clearButton.addActionListener {
            outPutArea.text = ""
            inputArea.text  = ""
            showToast(jFrame, "Data Cleared", 2500)
        }

        // ── Logs toggle ──────────────────────────────────────────────
        logsBtn.addActionListener {
            logPanel.toggle()
        }

        // ── Input JSON beautifier ────────────────────────────────────
        inPutJsonBeautifier.addActionListener {
            val json = inputArea.text
            when {
                json.isNullOrBlank() -> showToast(jFrame, "Please add JSON", 2500)
                isValidJson(json)    -> colorJsonInTextPane(inputArea, json)
                else                 -> showToast(jFrame, "Invalid JSON", 2500)
            }
        }

        // ── Input XML beautifier ─────────────────────────────────────
        // FIX: was missing entirely in original code
        inPutXmlBeautifier.addActionListener {
            val xml = inputArea.text
            if (xml.isNullOrBlank()) {
                showToast(jFrame, "Please add XML", 2500)
            } else {
                try {
                    val formatted = formatXml(xml)
                    inputArea.text = formatted
                } catch (e: Exception) {
                    showToast(jFrame, "Invalid XML", 2500)
                }
            }
        }

        // ── Output JSON beautifier ───────────────────────────────────
        outPutJsonBeautifier.addActionListener {
            val json = outPutArea.text
            when {
                json.isNullOrBlank() -> showToast(jFrame, "Please add JSON", 2500)
                isValidJson(json)    -> colorJsonInTextPane(outPutArea, json)
                else                 -> showToast(jFrame, "Invalid JSON", 2500)
            }
        }

        // ── Output XML beautifier ────────────────────────────────────
        // FIX: was missing entirely in original code
        outPutXmlBeautifier.addActionListener {
            val xml = outPutArea.text
            if (xml.isNullOrBlank()) {
                showToast(jFrame, "Please add XML", 2500)
            } else {
                try {
                    val formatted = formatXml(xml)
                    outPutArea.text = formatted
                } catch (e: Exception) {
                    showToast(jFrame, "Invalid XML", 2500)
                }
            }
        }
    }

    /**
     * Minimal XML pretty-printer using the JDK's built-in Transformer.
     * No external dependency required.
     */
    private fun formatXml(xml: String): String {
        val factory = javax.xml.transform.TransformerFactory.newInstance()
        val transformer = factory.newTransformer().apply {
            setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes")
            setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")
            setOutputProperty(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "yes")
        }
        val source = javax.xml.transform.stream.StreamSource(java.io.StringReader(xml))
        val result = javax.xml.transform.stream.StreamResult(java.io.StringWriter())
        transformer.transform(source, result)
        return result.writer.toString().trim()
    }

    private fun showStyledDialog(message: String) {
        JOptionPane.showMessageDialog(jFrame, message, "CryptoTool", JOptionPane.ERROR_MESSAGE)
    }

    private fun addFrames() {
        // ── Title Bar ─────────────────────────────────────────────────
        val titleBar = JPanel().apply {
            layout      = BoxLayout(this, BoxLayout.X_AXIS)
            background  = Theme.BG_PANEL
            border      = EmptyBorder(10, 20, 10, 20)
            maximumSize = Dimension(Int.MAX_VALUE, 44)

            add(JLabel("⬡").apply {
                font      = Font(Font.SANS_SERIF, Font.BOLD, 18)
                foreground = Theme.ACCENT_CYAN
            })
            add(JLabel("  CryptoTool").apply {
                font      = Font("Segoe UI", Font.BOLD, 14)
                foreground = Theme.TEXT_PRIMARY
            })
            add(JLabel("  v1.0  ·  AES-256").apply {
                font      = Theme.FONT_LABEL
                foreground = Theme.TEXT_SECONDARY
            })
            add(Box.createHorizontalGlue())

            // FIX: Logs button and badge are now properly added to titleBar here,
            //      instead of being orphaned inside addButtonListeners().
            add(logsBtn)
            add(Box.createRigidArea(Dimension(4, 0)))
            add(logBadge)
            add(Box.createRigidArea(Dimension(16, 0)))

            // Traffic-light status dots (decorative)
            for ((col, tip) in listOf(
                Theme.ACCENT_ORANGE       to "Close",
                Color(0xF1, 0xC4, 0x0F)  to "Minimise",
                Theme.ACCENT_GREEN        to "Full Screen"
            )) {
                add(JLabel("●").apply {
                    font        = Font(Font.SANS_SERIF, Font.PLAIN, 14)
                    foreground  = col
                    toolTipText = tip
                })
                add(Box.createRigidArea(Dimension(6, 0)))
            }
        }

        // ── Separator ─────────────────────────────────────────────────
        val sep = JSeparator().apply {
            foreground  = Theme.BORDER_SUBTLE
            background  = Theme.BORDER_SUBTLE
            maximumSize = Dimension(Int.MAX_VALUE, 1)
        }

        // ── Main Panel ────────────────────────────────────────────────
        val mainPanel = JPanel().apply {
            layout     = BoxLayout(this, BoxLayout.X_AXIS)
            background = Theme.BG_DEEP
            border     = EmptyBorder(20, 24, 24, 24)
        }

        /* ── INPUT COLUMN ── */
        val inputColumn = JPanel().apply {
            layout     = BoxLayout(this, BoxLayout.Y_AXIS)
            background = Theme.BG_DEEP
        }
        inputColumn.add(sectionLabel("INPUT", Theme.ACCENT_CYAN))
        inputColumn.add(Box.createRigidArea(Dimension(0, 6)))
        inputColumn.add(styledScrollPane(inputArea, Theme.ACCENT_CYAN).apply {
            preferredSize = Dimension(400, 300)
            minimumSize   = Dimension(200, 150)
        })
        inputColumn.add(Box.createRigidArea(Dimension(0, 10)))

        // Key field row
        inputColumn.add(JPanel().apply {
            layout     = BoxLayout(this, BoxLayout.X_AXIS)
            background = Theme.BG_DEEP
            // FIX: maximumSize height derived AFTER font/border are applied in initializeTextAreas()
            installationKeyField.maximumSize = Dimension(
                Int.MAX_VALUE,
                installationKeyField.preferredSize.height.coerceAtLeast(32)
            )
            add(JLabel("Key:").apply {
                foreground = Theme.TEXT_PRIMARY
                font       = Theme.FONT_UI_BOLD
            })
            add(Box.createRigidArea(Dimension(4, 0)))
            add(installationKeyField)
        })
        inputColumn.add(Box.createRigidArea(Dimension(0, 8)))

        // Input beautifier row
        inputColumn.add(JPanel().apply {
            layout      = BoxLayout(this, BoxLayout.X_AXIS)
            background  = Theme.BG_DEEP
            maximumSize = Dimension(Int.MAX_VALUE, 40)
            add(Box.createHorizontalGlue())
            add(inPutXmlBeautifier)
            add(Box.createRigidArea(Dimension(8, 0)))
            add(inPutJsonBeautifier)
            add(Box.createHorizontalGlue())
        })

        /* ── BUTTON COLUMN ── */
        val buttonColumn = JPanel().apply {
            layout     = BoxLayout(this, BoxLayout.Y_AXIS)
            background = Theme.BG_DEEP
            alignmentX = Component.CENTER_ALIGNMENT
        }
        buttonColumn.add(Box.createVerticalGlue())
        buttonColumn.add(encryptButton)
        buttonColumn.add(Box.createRigidArea(Dimension(0, 12)))
        buttonColumn.add(decryptButton)
        buttonColumn.add(Box.createRigidArea(Dimension(0, 12)))
        // Thin divider between action buttons and clear
        buttonColumn.add(JPanel().apply {
            background    = Theme.BORDER_SUBTLE
            maximumSize   = Dimension(80, 1)
            preferredSize = Dimension(80, 1)
            alignmentX    = Component.CENTER_ALIGNMENT
        })
        buttonColumn.add(Box.createRigidArea(Dimension(0, 12)))
        buttonColumn.add(clearButton)
        buttonColumn.add(Box.createVerticalGlue())

        /* ── OUTPUT COLUMN ── */
        val outputColumn = JPanel().apply {
            layout     = BoxLayout(this, BoxLayout.Y_AXIS)
            background = Theme.BG_DEEP
        }
        outputColumn.add(sectionLabel("OUTPUT", Theme.ACCENT_GREEN))
        outputColumn.add(Box.createRigidArea(Dimension(0, 6)))
        outputColumn.add(styledScrollPane(outPutArea, Theme.ACCENT_GREEN).apply {
            preferredSize = Dimension(400, 300)
            minimumSize   = Dimension(200, 150)
        })
        outputColumn.add(Box.createRigidArea(Dimension(0, 10)))

        // Output beautifier row
        outputColumn.add(JPanel().apply {
            layout      = BoxLayout(this, BoxLayout.X_AXIS)
            background  = Theme.BG_DEEP
            maximumSize = Dimension(Int.MAX_VALUE, 40)
            add(Box.createHorizontalGlue())
            add(outPutXmlBeautifier)
            add(Box.createRigidArea(Dimension(8, 0)))
            add(outPutJsonBeautifier)
            add(Box.createHorizontalGlue())
        })

        /* ── ASSEMBLE MAIN PANEL ── */
        mainPanel.add(inputColumn)
        mainPanel.add(Box.createRigidArea(Dimension(18, 0)))
        mainPanel.add(buttonColumn)
        mainPanel.add(Box.createRigidArea(Dimension(18, 0)))
        mainPanel.add(outputColumn)

        // ── Status Bar ────────────────────────────────────────────────
        val statusBar = JPanel().apply {
            layout      = BoxLayout(this, BoxLayout.X_AXIS)
            background  = Theme.BG_PANEL
            border      = EmptyBorder(6, 20, 6, 20)
            maximumSize = Dimension(Int.MAX_VALUE, 32)

            add(JLabel("●  Ready").apply {
                font      = Theme.FONT_LABEL
                foreground = Theme.ACCENT_GREEN
            })
            add(Box.createHorizontalGlue())
            add(JLabel("AES-256-CBC  ·  Base64 encoded").apply {
                font      = Theme.FONT_LABEL
                foreground = Theme.TEXT_SECONDARY
            })
        }

        // ── Root Layout ───────────────────────────────────────────────
        val root = JPanel().apply {
            layout     = BoxLayout(this, BoxLayout.Y_AXIS)
            background = Theme.BG_DEEP
            add(titleBar)
            add(sep)
            add(mainPanel)
            add(statusBar)
        }

        jFrame.contentPane.add(root)
        jFrame.pack()
        jFrame.minimumSize = Dimension(900, 520)
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
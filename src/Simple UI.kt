import util.NewAESUtil
import util.NewAESUtil.SECRET_KEY
import util.colorJsonInTextPane
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
    val BG_DEEP        = Color(0x0D, 0x11, 0x17)   // near-black navy
    val BG_PANEL       = Color(0x13, 0x19, 0x22)   // panel surface
    val BG_INPUT       = Color(0x0A, 0x0E, 0x14)   // input well
    val BORDER_SUBTLE  = Color(0x1E, 0x28, 0x38)   // subtle border
    val ACCENT_CYAN    = Color(0x00, 0xD4, 0xFF)   // electric cyan
    val ACCENT_GREEN   = Color(0x00, 0xFF, 0x9F)   // neon green
    val ACCENT_ORANGE  = Color(0xFF, 0x6B, 0x35)   // coral orange
    val ACCENT_PURPLE  = Color(0xBD, 0x93, 0xF9)   // lavender
    val TEXT_PRIMARY   = Color(0xE8, 0xF4, 0xFF)   // bright white-blue
    val TEXT_SECONDARY = Color(0x6A, 0x83, 0xA0)   // muted slate
    val TEXT_MONO      = Color(0xAB, 0xDB, 0xFF)   // code text

    val FONT_MONO = Font("JetBrains Mono", Font.PLAIN, 13).let { requested ->
        // fallback chain for monospace
        val families = GraphicsEnvironment.getLocalGraphicsEnvironment().availableFontFamilyNames.toSet()
        when {
            "JetBrains Mono" in families -> requested
            "Cascadia Code"  in families -> Font("Cascadia Code", Font.PLAIN, 13)
            "Fira Code"      in families -> Font("Fira Code",     Font.PLAIN, 13)
            "Consolas"       in families -> Font("Consolas",      Font.PLAIN, 13)
            else                         -> Font(Font.MONOSPACED,  Font.PLAIN, 13)
        }
    }
    val FONT_UI_BOLD  = Font("Segoe UI", Font.BOLD, 12)
    val FONT_UI       = Font("Segoe UI", Font.PLAIN, 12)
    val FONT_LABEL    = Font("Segoe UI", Font.BOLD, 11)
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
        isFocusPainted = false
        isBorderPainted = false
        isOpaque = false   // ✅ IMPORTANT FIX

        foreground = accent
        font = Theme.FONT_UI_BOLD
        cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)

        addMouseListener(object : MouseAdapter() {
            override fun mouseEntered(e: MouseEvent) {
                hovered = true
                repaint()
            }

            override fun mouseExited(e: MouseEvent) {
                hovered = false
                repaint()
            }

            override fun mousePressed(e: MouseEvent) {
                pressed = true
                repaint()
            }

            override fun mouseReleased(e: MouseEvent) {
                pressed = false
                repaint()
            }
        })
    }

    override fun paintComponent(g: Graphics) {
        val g2 = g.create() as Graphics2D

        g2.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON
        )

        val arc = 10

        // background states (stable colors instead of alpha issues)
        val bgColor = when {
            pressed -> blend(accent, 0.20)
            hovered -> blend(accent, 0.12)
            else -> blend(accent, 0.08)
        }

        g2.color = bgColor
        g2.fillRoundRect(0, 0, width, height, arc, arc)

        // border
        g2.color = accent
        g2.stroke = BasicStroke(if (pressed) 2f else 1.2f)
        g2.drawRoundRect(1, 1, width - 2, height - 2, arc, arc)

        g2.dispose()

        // IMPORTANT: let Swing draw text AFTER background
        super.paintComponent(g)
    }

    private fun blend(color: Color, factor: Double): Color {
        return Color(
            (color.red * factor).toInt().coerceIn(0, 255),
            (color.green * factor).toInt().coerceIn(0, 255),
            (color.blue * factor).toInt().coerceIn(0, 255)
        )
    }
}
// ─────────────────────────────────────────────
//  STYLED SCROLL PANE
// ─────────────────────────────────────────────
fun styledScrollPane(view: Component, accentColor: Color): JScrollPane {
    return JScrollPane(view).apply {
        border = RoundedBorder(10, accentColor, 1f)
        background = Theme.BG_INPUT
        viewport.background = Theme.BG_INPUT

        verticalScrollBar.apply {
            preferredSize = Dimension(6, 0)
            isOpaque = false
            setUI(object : javax.swing.plaf.basic.BasicScrollBarUI() {
                override fun configureScrollBarColors() {
                    thumbColor = Color(accentColor.red, accentColor.green, accentColor.blue, 90)
                    trackColor = Theme.BG_INPUT
                }
                override fun createDecreaseButton(o: Int) = JButton().apply { preferredSize = Dimension(0,0) }
                override fun createIncreaseButton(o: Int) = JButton().apply { preferredSize = Dimension(0,0) }
            })
        }
        horizontalScrollBar.preferredSize = Dimension(0, 5)
        verticalScrollBarPolicy   = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
        horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
    }
}

// ─────────────────────────────────────────────
//  SECTION LABEL
// ─────────────────────────────────────────────
fun sectionLabel(text: String, dot: Color): JPanel {
    return JPanel().apply {
        layout = BoxLayout(this, BoxLayout.X_AXIS)
        isOpaque = false
        val dotLabel = JLabel("●").apply {
            font = Font(Font.SANS_SERIF, Font.PLAIN, 8)
            foreground = dot
        }
        val textLabel = JLabel("  $text").apply {
            font = Theme.FONT_LABEL
            foreground = Theme.TEXT_SECONDARY
        }
        add(dotLabel)
        add(textLabel)
        add(Box.createHorizontalGlue())
    }
}

// ─────────────────────────────────────────────
//  MAIN UI CLASS
// ─────────────────────────────────────────────
class SimpleUI {

    private val jFrame = JFrame()
    val inputArea = JTextPane()
    private val outPutArea = JTextArea(5, 20)

    // Buttons — same names, same functionality
    private val encryptButton        = StyledButton("Encrypt",      Theme.ACCENT_PURPLE,   "")
    private val decryptButton        = StyledButton("Decrypt",      Theme.ACCENT_PURPLE,  "")
    private val clearButton          = StyledButton("Clear",        Theme.ACCENT_PURPLE, "X")
    private val inPutJsonBeautifier  = StyledButton("JSON",         Theme.ACCENT_PURPLE, "{}")
    private val inPutXmlBeautifier   = StyledButton("XML",          Theme.ACCENT_PURPLE, "</>")
    private val outPutJsonBeautifier = StyledButton("JSON",         Theme.ACCENT_PURPLE, "{}")
    private val outPutXmlBeautifier  = StyledButton("XML",          Theme.ACCENT_PURPLE, "</>")

    init {
        setGlobalUIDefaults()
        initializeFrame()
        initializeTextAreas()
        initializeButtons()
        addFrames()
        addButtonListeners()
    }

    private fun setGlobalUIDefaults() {
        UIManager.put("OptionPane.background",          Theme.BG_PANEL)
        UIManager.put("Panel.background",               Theme.BG_PANEL)
        UIManager.put("OptionPane.messageForeground",   Theme.TEXT_PRIMARY)
        UIManager.put("Button.background",              Theme.BG_PANEL)
        UIManager.put("Button.foreground",              Theme.TEXT_PRIMARY)
    }

    private fun initializeFrame() {
        with(jFrame) {
            title = "  ⬡  CryptoTool  —  AES Encrypt / Decrypt"
            defaultCloseOperation = JFrame.EXIT_ON_CLOSE
            setSize(1000, 580)
            setLocationRelativeTo(null)
            contentPane.background = Theme.BG_DEEP
        }
    }

    private fun initializeTextAreas() {
        // Input
        inputArea.apply {
            background  = Theme.BG_INPUT
            foreground  = Theme.TEXT_MONO
            caretColor  = Theme.ACCENT_CYAN
            font        = Theme.FONT_MONO
            border      = EmptyBorder(12, 14, 12, 14)
            preferredSize = Dimension(400, 300)
            minimumSize   = Dimension(200, 200)
            maximumSize   = Dimension(Int.MAX_VALUE, Int.MAX_VALUE)
        }

        // Output
        outPutArea.apply {
            background  = Theme.BG_INPUT
            foreground  = Theme.ACCENT_GREEN
            caretColor  = Theme.ACCENT_GREEN
            font        = Theme.FONT_MONO
            lineWrap    = true
            wrapStyleWord = true
            isEditable  = false
            border      = EmptyBorder(12, 14, 12, 14)
            preferredSize = Dimension(400, 300)
            minimumSize   = Dimension(200, 200)
            maximumSize   = Dimension(Int.MAX_VALUE, Int.MAX_VALUE)
        }
    }

    private fun initializeButtons() {
        val btnSize = Dimension(130, 38)
        encryptButton.preferredSize = btnSize
        encryptButton.maximumSize   = btnSize
        decryptButton.preferredSize = btnSize
        decryptButton.maximumSize   = btnSize
        clearButton.preferredSize   = btnSize
        clearButton.maximumSize     = btnSize

        encryptButton.alignmentX = Component.CENTER_ALIGNMENT
        decryptButton.alignmentX = Component.CENTER_ALIGNMENT
        clearButton.alignmentX   = Component.CENTER_ALIGNMENT

        val smallBtn = Dimension(90, 30)
        for (b in listOf(inPutJsonBeautifier, inPutXmlBeautifier, outPutJsonBeautifier, outPutXmlBeautifier)) {
            b.preferredSize = smallBtn
            b.maximumSize   = smallBtn
        }
    }

    private fun addButtonListeners() {
        encryptButton.addActionListener {
            val value = inputArea.text.trim()
            if (value.isNotBlank()) {
                val encrypted = NewAESUtil.encrypt(inputArea.text.trim(), SECRET_KEY)
                println("Encrypted: $encrypted")
                outPutArea.text = encrypted
            } else {
                showStyledDialog("Input field must not be empty before encrypting.")
            }
        }

        decryptButton.addActionListener {
            val value = inputArea.text.trim()
            if (value.isNotBlank()) {
                val decrypted = NewAESUtil.decrypt(inputArea.text.trim(), SECRET_KEY)
                println("Decrypted: $decrypted")
                outPutArea.text = decrypted
            } else {
                showStyledDialog("Input field must not be empty before decrypting.")
            }
        }

        clearButton.addActionListener {
            outPutArea.text = ""
            inputArea.text  = ""
            showToast(jFrame, "Data Cleared", 2500)
        }

        inPutJsonBeautifier.addActionListener {
            val json = inputArea.text
            if (!json.isNullOrBlank()) {
                if (isValidJson(json)) {
                    colorJsonInTextPane(inputArea, json)
                } else {
                    showToast(jFrame, "Invalid JSON", 2500)
                }
            } else {
                showToast(jFrame, "Please add JSON", 2500)
            }
        }
    }

    private fun showStyledDialog(message: String) {
        JOptionPane.showMessageDialog(
            jFrame, message, "CryptoTool", JOptionPane.ERROR_MESSAGE
        )
    }

    private fun addFrames() {
        // ── Title Bar Strip ────────────────────────────────────────────
        val titleBar = JPanel().apply {
            layout     = BoxLayout(this, BoxLayout.X_AXIS)
            background = Theme.BG_PANEL
            border     = EmptyBorder(10, 20, 10, 20)
            maximumSize = Dimension(Int.MAX_VALUE, 44)

            val appIcon = JLabel("⬡").apply {
                font      = Font(Font.SANS_SERIF, Font.BOLD, 18)
                foreground = Theme.ACCENT_CYAN
            }
            val appName = JLabel("  CryptoTool").apply {
                font      = Font("Segoe UI", Font.BOLD, 14)
                foreground = Theme.TEXT_PRIMARY
            }
            val version = JLabel("  v1.0  ·  AES-256").apply {
                font      = Theme.FONT_LABEL
                foreground = Theme.TEXT_SECONDARY
            }

            add(appIcon)
            add(appName)
            add(version)
            add(Box.createHorizontalGlue())

            // Status dots (decorative)
            for ((col, tip) in listOf(Theme.ACCENT_ORANGE to "Close", Color(0xF1, 0xC4, 0x0F) to "Minimise", Theme.ACCENT_GREEN to "Full Screen")) {
                val dot = JLabel("●").apply {
                    font      = Font(Font.SANS_SERIF, Font.PLAIN, 14)
                    foreground = col
                    toolTipText = tip
                }
                add(dot)
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

        val inputScrollPane = styledScrollPane(inputArea, Theme.ACCENT_CYAN).apply {
            preferredSize = Dimension(400, 300)
            minimumSize   = Dimension(200, 200)
            maximumSize   = Dimension(Int.MAX_VALUE, Int.MAX_VALUE)
        }
        inputColumn.add(inputScrollPane)
        inputColumn.add(Box.createRigidArea(Dimension(0, 10)))

        val inputBeautifierPanel = JPanel().apply {
            layout     = BoxLayout(this, BoxLayout.X_AXIS)
            background = Theme.BG_DEEP
            maximumSize = Dimension(Int.MAX_VALUE, 40)
            add(Box.createHorizontalGlue())
            add(inPutXmlBeautifier)
            add(Box.createRigidArea(Dimension(8, 0)))
            add(inPutJsonBeautifier)
            add(Box.createHorizontalGlue())
        }
        inputColumn.add(inputBeautifierPanel)

        /* ── BUTTON COLUMN ── */
        val buttonColumn = JPanel().apply {
            layout     = BoxLayout(this, BoxLayout.Y_AXIS)
            background = Theme.BG_DEEP
            alignmentX = Component.CENTER_ALIGNMENT
        }

        buttonColumn.add(Box.createVerticalGlue())

        // Divider line above buttons
        val divTop = JSeparator(SwingConstants.VERTICAL).apply {
            foreground  = Theme.BORDER_SUBTLE
            maximumSize = Dimension(1, 40)
        }

        encryptButton.alignmentX = Component.CENTER_ALIGNMENT
        decryptButton.alignmentX = Component.CENTER_ALIGNMENT
        clearButton.alignmentX   = Component.CENTER_ALIGNMENT

        buttonColumn.add(encryptButton)
        buttonColumn.add(Box.createRigidArea(Dimension(0, 12)))
        buttonColumn.add(decryptButton)
        buttonColumn.add(Box.createRigidArea(Dimension(0, 12)))

        // Thin divider between action + clear
        val divider = JPanel().apply {
            background  = Theme.BORDER_SUBTLE
            maximumSize = Dimension(80, 1)
            preferredSize = Dimension(80, 1)
            alignmentX  = Component.CENTER_ALIGNMENT
        }
        buttonColumn.add(divider)
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

        val outputScrollPane = styledScrollPane(outPutArea, Theme.ACCENT_GREEN).apply {
            preferredSize = Dimension(400, 300)
            minimumSize   = Dimension(200, 200)
            maximumSize   = Dimension(Int.MAX_VALUE, Int.MAX_VALUE)
        }
        outputColumn.add(outputScrollPane)
        outputColumn.add(Box.createRigidArea(Dimension(0, 10)))

        val outputBeautifierPanel = JPanel().apply {
            layout     = BoxLayout(this, BoxLayout.X_AXIS)
            background = Theme.BG_DEEP
            maximumSize = Dimension(Int.MAX_VALUE, 40)
            add(Box.createHorizontalGlue())
            add(outPutXmlBeautifier)
            add(Box.createRigidArea(Dimension(8, 0)))
            add(outPutJsonBeautifier)
            add(Box.createHorizontalGlue())
        }
        outputColumn.add(outputBeautifierPanel)

        /* ── ASSEMBLE ── */
        mainPanel.add(inputColumn)
        mainPanel.add(Box.createRigidArea(Dimension(18, 0)))
        mainPanel.add(buttonColumn)
        mainPanel.add(Box.createRigidArea(Dimension(18, 0)))
        mainPanel.add(outputColumn)

        // ── Status Bar ────────────────────────────────────────────────
        val statusBar = JPanel().apply {
            layout     = BoxLayout(this, BoxLayout.X_AXIS)
            background = Theme.BG_PANEL
            border     = EmptyBorder(6, 20, 6, 20)
            maximumSize = Dimension(Int.MAX_VALUE, 32)

            val statusDot = JLabel("●  Ready").apply {
                font      = Theme.FONT_LABEL
                foreground = Theme.ACCENT_GREEN
            }
            val algo = JLabel("AES-256-CBC  ·  Base64 encoded").apply {
                font      = Theme.FONT_LABEL
                foreground = Theme.TEXT_SECONDARY
            }

            add(statusDot)
            add(Box.createHorizontalGlue())
            add(algo)
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
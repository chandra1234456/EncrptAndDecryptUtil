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

/**
 * Run Below Command For .exe File in Intellij Terminal
 */
//powershell -ExecutionPolicy Bypass -File build.ps1
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

    // Font.family returns "Dialog" when the face is missing — far cheaper than enumerating all installed fonts at startup.
    val FONT_MONO: Font = listOf("JetBrains Mono", "Cascadia Code", "Fira Code", "Consolas")
        .map { name -> name to Font(name, Font.PLAIN, 13) }
        .firstOrNull { (name, f) -> f.family.equals(name, ignoreCase = true) }?.second
        ?: Font(Font.MONOSPACED, Font.PLAIN, 13)
    val FONT_UI_BOLD = Font("Segoe UI", Font.BOLD, 12)
    val FONT_UI      = Font("Segoe UI", Font.PLAIN, 12)
    val FONT_LABEL   = Font("Segoe UI", Font.BOLD, 11)
}

// ─────────────────────────────────────────────
//  APP ICON (drawn in code, shared with the .exe icon generator)
// ─────────────────────────────────────────────
object AppIcon {
    fun render(size: Int): java.awt.image.BufferedImage {
        val img = java.awt.image.BufferedImage(size, size, java.awt.image.BufferedImage.TYPE_INT_ARGB)
        val g = img.createGraphics()
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE)
        val s = size.toFloat()
        g.paint = GradientPaint(0f, 0f, Color(0x1B, 0x25, 0x38), s, s, Theme.BG_DEEP)
        g.fillRoundRect(0, 0, size, size, (s * 0.22f).toInt(), (s * 0.22f).toInt())
        // hexagon
        val cx = s / 2; val cy = s / 2; val r = s * 0.34f
        val hex = java.awt.geom.Path2D.Float()
        for (i in 0 until 6) {
            val ang = Math.toRadians(60.0 * i - 90)
            val x = (cx + r * Math.cos(ang)).toFloat(); val y = (cy + r * Math.sin(ang)).toFloat()
            if (i == 0) hex.moveTo(x, y) else hex.lineTo(x, y)
        }
        hex.closePath()
        g.stroke = BasicStroke(s * 0.06f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
        g.paint = GradientPaint(0f, 0f, Theme.ACCENT_CYAN, s, s, Theme.ACCENT_GREEN)
        g.draw(hex)
        // keyhole
        val kr = s * 0.08f
        g.fill(java.awt.geom.Ellipse2D.Float(cx - kr, cy - kr * 1.6f, kr * 2, kr * 2))
        g.fill(java.awt.geom.RoundRectangle2D.Float(cx - kr * 0.45f, cy - kr * 0.2f, kr * 0.9f, kr * 2.4f, kr * 0.4f, kr * 0.4f))
        g.dispose()
        return img
    }

    @JvmStatic
    fun main(args: Array<String>) {   // used by build script: writes PNGs for .ico
        for (sz in listOf(16, 32, 48, 64, 128, 256))
            javax.imageio.ImageIO.write(render(sz), "png", java.io.File(args[0], "icon$sz.png"))
    }
}

// ─────────────────────────────────────────────
//  THEMED MESSAGE DIALOG
// ─────────────────────────────────────────────
object StyledDialog {
    fun show(owner: JFrame, title: String, message: String, accent: Color, glyph: String) {
        val dialog = JDialog(owner, "CryptoTool", true)
        dialog.isUndecorated = true
        dialog.iconImages = owner.iconImages

        val badge = object : JLabel(glyph, SwingConstants.CENTER) {
            override fun paintComponent(g: Graphics) {
                val g2 = g.create() as Graphics2D
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                g2.color = Color(accent.red, accent.green, accent.blue, 40)
                g2.fillOval(0, 0, width - 1, height - 1)
                g2.color = accent
                g2.drawOval(0, 0, width - 1, height - 1)
                g2.dispose()
                super.paintComponent(g)
            }
        }.apply {
            font = Font("Segoe UI", Font.BOLD, 20); foreground = accent
            preferredSize = Dimension(44, 44); minimumSize = preferredSize; maximumSize = preferredSize
        }

        val text = JTextArea(message).apply {
            lineWrap = true; wrapStyleWord = true; isEditable = false; isOpaque = false
            font = Theme.FONT_UI.deriveFont(13f); foreground = Theme.TEXT_PRIMARY
            border = EmptyBorder(0, 0, 0, 0)
            columns = 28
            alignmentX = Component.LEFT_ALIGNMENT
        }
        val heading = JLabel(title).apply {
            font = Font("Segoe UI", Font.BOLD, 15); foreground = Theme.TEXT_PRIMARY
            border = EmptyBorder(2, 0, 6, 0)
            alignmentX = Component.LEFT_ALIGNMENT
        }
        val body = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS); isOpaque = false
            alignmentX = Component.LEFT_ALIGNMENT
            add(heading); add(text)
        }
        val ok = StyledButton("OK", accent).apply {
            preferredSize = Dimension(90, 32)
            addActionListener { dialog.dispose() }
        }
        val buttons = JPanel(FlowLayout(FlowLayout.RIGHT, 0, 0)).apply { isOpaque = false; add(ok) }

        val content = JPanel(BorderLayout(16, 16)).apply {
            background = Theme.BG_PANEL
            border = javax.swing.BorderFactory.createCompoundBorder(
                RoundedBorder(0, accent, 1.5f), EmptyBorder(8, 14, 8, 14))
            add(JPanel(BorderLayout()).apply { isOpaque = false; add(badge, BorderLayout.NORTH) }, BorderLayout.WEST)
            add(body, BorderLayout.CENTER)
            add(buttons, BorderLayout.SOUTH)
        }
        dialog.contentPane = content
        dialog.rootPane.defaultButton = ok
        dialog.rootPane.registerKeyboardAction({ dialog.dispose() },
            KeyStroke.getKeyStroke("ESCAPE"), JComponent.WHEN_IN_FOCUSED_WINDOW)
        dialog.pack()
        dialog.setLocationRelativeTo(owner)
        dialog.isVisible = true
    }
}

// ─────────────────────────────────────────────
//  WRAPPING EDITOR KIT (breaks long unbroken strings such as Base64)
// ─────────────────────────────────────────────
class WrapEditorKit : javax.swing.text.StyledEditorKit() {
    private val factory = object : javax.swing.text.ViewFactory {
        private val base = super@WrapEditorKit.getViewFactory()
        override fun create(elem: javax.swing.text.Element): javax.swing.text.View {
            return when (elem.name) {
                javax.swing.text.AbstractDocument.ContentElementName ->
                    object : javax.swing.text.LabelView(elem) {
                        override fun getMinimumSpan(axis: Int): Float =
                            if (axis == X_AXIS) 0f else super.getMinimumSpan(axis)
                    }
                javax.swing.text.AbstractDocument.ParagraphElementName ->
                    object : javax.swing.text.ParagraphView(elem) {
                        override fun calculateMinorAxisRequirements(axis: Int, r: SizeRequirements?): SizeRequirements {
                            val sr = super.calculateMinorAxisRequirements(axis, r)
                            sr.minimum = 0
                            return sr
                        }
                    }
                else -> base.create(elem)
            }
        }
    }
    override fun getViewFactory() = factory
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
        horizontalScrollBarPolicy            = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
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
    private val statusLabel = JLabel("●  Ready").apply {
        font       = Theme.FONT_LABEL
        foreground = Theme.ACCENT_GREEN
    }

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

    private fun setBusy(busy: Boolean, activeLabel: String = "") {
        encryptButton.isEnabled = !busy
        decryptButton.isEnabled = !busy
        clearButton.isEnabled   = !busy
        statusLabel.text = if (busy) "●  $activeLabel…" else "●  Ready"
        statusLabel.foreground = if (busy) Theme.ACCENT_ORANGE else Theme.ACCENT_GREEN
        jFrame.cursor = if (busy) Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) else Cursor.getDefaultCursor()
    }

    private fun runCryptoTask(
        activeLabel: String,
        work: () -> String,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        setBusy(true, activeLabel)
        object : SwingWorker<String, Unit>() {
            override fun doInBackground(): String = work()
            override fun done() {
                setBusy(false)
                try {
                    onSuccess(get())
                } catch (e: Exception) {
                    onFailure((e.cause as? Exception) ?: e)
                }
            }
        }.execute()
    }

    private fun initializeFrame() {
        with(jFrame) {
            title                  = "  ⬡  CryptoTool  —  AES Encrypt / Decrypt"
            defaultCloseOperation  = JFrame.EXIT_ON_CLOSE
            setSize(1000, 580)
            setLocationRelativeTo(null)
            contentPane.background = Theme.BG_DEEP
            iconImages             = listOf(16, 32, 48, 64, 128, 256).map { AppIcon.render(it) }
        }
    }

    private fun initializeTextAreas() {
        inputArea.editorKit  = WrapEditorKit()
        outPutArea.editorKit = WrapEditorKit()
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
            val key   = installationKeyField.text.trim()
            if (value.isNotBlank()) {
                runCryptoTask(
                    activeLabel = "Encrypting",
                    work = { getEncryptValue(value, key) },
                    onSuccess = { encrypted ->
                        outPutArea.text = encrypted
                        logPanel.info("Encrypt successful (${value.length} chars)")
                        syncLogBadge()
                    },
                    onFailure = { e ->
                        logPanel.error("Encrypt failed: ${e.message}", e)
                        syncLogBadge()
                        showStyledDialog("Encryption error: ${e.message}")
                    }
                )
            } else {
                showStyledDialog("Input field must not be empty before encrypting.")
            }
        }


        // ── Decrypt ──────────────────────────────────────────────────
        decryptButton.addActionListener {
            val value = inputArea.text.trim()
            val key   = installationKeyField.text.trim()
            if (value.isNotBlank()) {
                runCryptoTask(
                    activeLabel = "Decrypting",
                    work = { getDecryptValue(value, key) },
                    onSuccess = { decrypted ->
                        outPutArea.text = decrypted
                        logPanel.info("Decrypt successful")
                        syncLogBadge()
                    },
                    onFailure = { e ->
                        logPanel.error("Decrypt failed: ${e.message}", e)
                        syncLogBadge()
                        showStyledDialog("Decryption error: ${e.message}")
                    }
                )
            } else {
                showStyledDialog("Input field must not be empty before decrypting.")
            }
        }

        jFrame.rootPane.registerKeyboardAction(
            { encryptButton.doClick() },
            KeyStroke.getKeyStroke("control ENTER"), JComponent.WHEN_IN_FOCUSED_WINDOW
        )
        encryptButton.toolTipText = "Ctrl+Enter"

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
                json.isNullOrBlank() -> showToast(jFrame, "Please add JSON", 2500, Theme.ACCENT_ORANGE)
                isValidJson(json)    -> colorJsonInTextPane(inputArea, json)
                else                 -> showToast(jFrame, "Invalid JSON", 2500, Theme.ACCENT_ORANGE)
            }
        }

        // ── Input XML beautifier ─────────────────────────────────────
        // FIX: was missing entirely in original code
        inPutXmlBeautifier.addActionListener {
            val xml = inputArea.text
            if (xml.isNullOrBlank()) {
                showToast(jFrame, "Please add XML", 2500, Theme.ACCENT_ORANGE)
            } else {
                try {
                    val formatted = formatXml(xml)
                    inputArea.text = formatted
                } catch (e: Exception) {
                    showToast(jFrame, "Invalid XML", 2500, Theme.ACCENT_ORANGE)
                }
            }
        }

        // ── Output JSON beautifier ───────────────────────────────────
        outPutJsonBeautifier.addActionListener {
            val json = outPutArea.text
            when {
                json.isNullOrBlank() -> showToast(jFrame, "Please add JSON", 2500, Theme.ACCENT_ORANGE)
                isValidJson(json)    -> colorJsonInTextPane(outPutArea, json)
                else                 -> showToast(jFrame, "Invalid JSON", 2500, Theme.ACCENT_ORANGE)
            }
        }

        // ── Output XML beautifier ────────────────────────────────────
        // FIX: was missing entirely in original code
        outPutXmlBeautifier.addActionListener {
            val xml = outPutArea.text
            if (xml.isNullOrBlank()) {
                showToast(jFrame, "Please add XML", 2500, Theme.ACCENT_ORANGE)
            } else {
                try {
                    val formatted = formatXml(xml)
                    outPutArea.text = formatted
                } catch (e: Exception) {
                    showToast(jFrame, "Invalid XML", 2500, Theme.ACCENT_ORANGE)
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
        StyledDialog.show(jFrame, "Error", message, Theme.ACCENT_ORANGE, "!")
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
        mainPanel.layout = GridBagLayout()
        val gc = GridBagConstraints().apply { fill = GridBagConstraints.BOTH; weighty = 1.0; gridy = 0 }
        mainPanel.add(inputColumn,  gc.apply { gridx = 0; weightx = 1.0; insets = Insets(0, 0, 0, 18) })
        mainPanel.add(buttonColumn, gc.apply { gridx = 1; weightx = 0.0; insets = Insets(0, 0, 0, 18) })
        mainPanel.add(outputColumn, gc.apply { gridx = 2; weightx = 1.0; insets = Insets(0, 0, 0, 0) })

        // ── Status Bar ────────────────────────────────────────────────
        val statusBar = JPanel().apply {
            layout      = BoxLayout(this, BoxLayout.X_AXIS)
            background  = Theme.BG_PANEL
            border      = EmptyBorder(6, 20, 6, 20)
            maximumSize = Dimension(Int.MAX_VALUE, 32)

            add(statusLabel)
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

        root.layout = BoxLayout(root, BoxLayout.Y_AXIS)
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
            System.setProperty("awt.useSystemAAFontSettings", "on")
            System.setProperty("swing.aatext", "true")
            System.setProperty("sun.java2d.d3d", "true")
            SwingUtilities.invokeLater {
                SimpleUI().show()
            }
        }
    }
}
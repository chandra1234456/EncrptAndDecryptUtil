import java.awt.*
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.text.*

class LogPanel {

    enum class Level { INFO, WARN, ERROR }

    private val frame = JFrame()
    private val logPane = JTextPane()
    private val doc get() = logPane.styledDocument
    private val countLabel = JLabel("0 entries")
    private var entryCount = 0
    private val badgeLabel = JLabel("0").apply { isVisible = false }

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

    init {
        buildUI()
        installGlobalExceptionHandler()
    }

    // ── Global uncaught exception hook ───────────────────────────────
    private fun installGlobalExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            SwingUtilities.invokeLater {
                appendLog(Level.ERROR, "[${thread.name}] ${throwable::class.simpleName}: ${throwable.message}", throwable)
            }
        }
    }

    // ── Public: log manually from anywhere ───────────────────────────
    fun info(msg: String)  = SwingUtilities.invokeLater { appendLog(Level.INFO,  msg) }
    fun warn(msg: String)  = SwingUtilities.invokeLater { appendLog(Level.WARN,  msg) }
    fun error(msg: String, t: Throwable? = null) = SwingUtilities.invokeLater { appendLog(Level.ERROR, msg, t) }

    fun getBadgeLabel() = badgeLabel

    // ── Append a log entry ────────────────────────────────────────────
    private fun appendLog(level: Level, message: String, throwable: Throwable? = null) {
        entryCount++
        val time = LocalTime.now().format(timeFormatter)

        // timestamp
        append("  $time  ", style("timestamp"))
        // badge text
        val (badgeText, badgeStyle) = when (level) {
            Level.INFO  -> "INFO " to style("info")
            Level.WARN  -> "WARN " to style("warn")
            Level.ERROR -> "ERROR" to style("error")
        }
        append(" $badgeText ", badgeStyle)
        append("  $message\n", style("message"))

        // stack trace if present
        throwable?.let { t ->
            val stack = buildString {
                appendLine("  ${t.javaClass.name}: ${t.message}")
                t.stackTrace.take(8).forEach { appendLine("      at $it") }
            }
            append(stack + "\n", style("stack"))
        }

        // scroll to bottom
        logPane.caretPosition = doc.length

        // update counters
        countLabel.text = "$entryCount ${if (entryCount == 1) "entry" else "entries"}"
        badgeLabel.text = entryCount.toString()
        badgeLabel.isVisible = true
    }

    // ── Style helpers ─────────────────────────────────────────────────
    private fun style(name: String): Style {
        val s = logPane.addStyle(name + System.nanoTime(), null)
        when (name) {
            "timestamp" -> {
                StyleConstants.setForeground(s, Color(0x6A, 0x83, 0xA0))
                StyleConstants.setFontFamily(s, Theme.FONT_MONO.family)
                StyleConstants.setFontSize(s, 12)
            }
            "info" -> {
                StyleConstants.setForeground(s, Color(0x00, 0xD4, 0xFF))
                StyleConstants.setBold(s, true)
                StyleConstants.setFontFamily(s, Theme.FONT_MONO.family)
                StyleConstants.setFontSize(s, 12)
            }
            "warn" -> {
                StyleConstants.setForeground(s, Color(0xFF, 0xB3, 0x47))
                StyleConstants.setBold(s, true)
                StyleConstants.setFontFamily(s, Theme.FONT_MONO.family)
                StyleConstants.setFontSize(s, 12)
            }
            "error" -> {
                StyleConstants.setForeground(s, Color(0xFF, 0x6B, 0x6B))
                StyleConstants.setBold(s, true)
                StyleConstants.setFontFamily(s, Theme.FONT_MONO.family)
                StyleConstants.setFontSize(s, 12)
            }
            "message" -> {
                StyleConstants.setForeground(s, Color(0xAB, 0xDB, 0xFF))
                StyleConstants.setFontFamily(s, Theme.FONT_MONO.family)
                StyleConstants.setFontSize(s, 12)
            }
            "stack" -> {
                StyleConstants.setForeground(s, Color(0x6A, 0x83, 0xA0))
                StyleConstants.setFontFamily(s, Theme.FONT_MONO.family)
                StyleConstants.setFontSize(s, 11)
                StyleConstants.setLeftIndent(s, 20f)
            }
        }
        return s
    }

    private fun append(text: String, style: Style) {
        doc.insertString(doc.length, text, style)
    }

    // ── Build the window UI ───────────────────────────────────────────
    private fun buildUI() {
        logPane.apply {
            isEditable = false
            background = Color(0x0A, 0x0E, 0x14)
            border = EmptyBorder(8, 6, 8, 6)
        }

        val scrollPane = styledScrollPane(logPane, Theme.ACCENT_CYAN).apply {
            preferredSize = Dimension(760, 400)
        }

        // toolbar
        val toolbar = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            background = Theme.BG_PANEL
            border = EmptyBorder(8, 16, 8, 16)

            countLabel.apply {
                font = Theme.FONT_LABEL
                foreground = Theme.TEXT_SECONDARY
            }

            val clearBtn = StyledButton("Clear Logs", Theme.ACCENT_ORANGE, "✕")
            clearBtn.preferredSize = Dimension(130, 32)
            clearBtn.maximumSize   = Dimension(130, 32)
            clearBtn.addActionListener { clearLogs() }

            add(countLabel)
            add(Box.createHorizontalGlue())
            add(clearBtn)
        }

        // title bar
        val titleBar = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            background = Theme.BG_PANEL
            border = EmptyBorder(10, 16, 10, 16)

            add(JLabel("⬡  ").apply { font = Font(Font.SANS_SERIF, Font.BOLD, 16); foreground = Theme.ACCENT_CYAN })
            add(JLabel("CryptoTool — Logs & Diagnostics").apply {
                font = Font("Segoe UI", Font.BOLD, 13); foreground = Theme.TEXT_PRIMARY
            })
            add(Box.createHorizontalGlue())
        }

        val sep = JSeparator().apply {
            foreground = Theme.BORDER_SUBTLE; background = Theme.BORDER_SUBTLE
            maximumSize = Dimension(Int.MAX_VALUE, 1)
        }

        val root = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            background = Theme.BG_DEEP
            add(titleBar); add(sep); add(toolbar); add(scrollPane)
        }

        frame.apply {
            title = "CryptoTool — Logs"
            defaultCloseOperation = JFrame.HIDE_ON_CLOSE
            contentPane.background = Theme.BG_DEEP
            contentPane.add(root)
            setSize(800, 520)
            setLocationRelativeTo(null)
        }
    }

    private fun clearLogs() {
        try { doc.remove(0, doc.length) } catch (_: Exception) {}
        entryCount = 0
        countLabel.text = "0 entries"
        badgeLabel.text = "0"
        badgeLabel.isVisible = false
    }

    fun show() { frame.isVisible = true }
    fun hide() { frame.isVisible = false }
    fun toggle() { frame.isVisible = !frame.isVisible }
}
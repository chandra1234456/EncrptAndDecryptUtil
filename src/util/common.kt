package util

import com.google.gson.JsonParser
import java.awt.Color
import java.awt.Font
import java.awt.Point
import javax.swing.JLabel
import javax.swing.JWindow
import javax.swing.Timer
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException


import javax.swing.JTextPane
import javax.swing.text.AttributeSet
import javax.swing.text.StyleConstants
import javax.swing.text.StyledDocument
import javax.swing.text.SimpleAttributeSet

fun isValidJson(jsonString: String): Boolean {
    return try {
        JsonParser.parseString(jsonString) // parses both objects and arrays
        true
    } catch (e: JsonSyntaxException) {
        false
    }
}

fun formatJson(jsonString: String): String? {
    return try {
        // Parse the JSON string
        val jsonElement = JsonParser.parseString(jsonString)

        // Pretty-print / standardize JSON output
        val gson = GsonBuilder().setPrettyPrinting().create()
        gson.toJson(jsonElement)
    } catch (e: JsonSyntaxException) {
        null // Return null if not valid JSON
    }
}



/**
 * Shows a toast message in a Swing application.
 *
 * @param parent The parent JFrame or component
 * @param message The message to display
 * @param durationMs How long the toast shows (milliseconds)
 */
fun showToast(
    parent: javax.swing.JFrame,
    message: String,
    durationMs: Int = 2000,
    accent: Color = Color(0x00, 0xD4, 0xFF)
) {
    val toast = JWindow(parent)
    toast.focusableWindowState = false
    toast.background = Color(0, 0, 0, 0)

    val panel = object : javax.swing.JPanel(java.awt.BorderLayout(10, 0)) {
        override fun paintComponent(g: java.awt.Graphics) {
            val g2 = g.create() as java.awt.Graphics2D
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON)
            g2.color = Color(0x13, 0x19, 0x22, 235)
            g2.fillRoundRect(0, 0, width - 1, height - 1, 14, 14)
            g2.color = accent
            g2.stroke = java.awt.BasicStroke(1.2f)
            g2.drawRoundRect(0, 0, width - 1, height - 1, 14, 14)
            g2.fillRoundRect(0, 0, 5, height - 1, 5, 5)   // accent bar on the left
            g2.dispose()
        }
    }.apply {
        isOpaque = false
        border = javax.swing.BorderFactory.createEmptyBorder(11, 20, 11, 22)
    }
    panel.add(JLabel("✓").apply { font = Font("Segoe UI", Font.BOLD, 15); foreground = accent }, java.awt.BorderLayout.WEST)
    panel.add(JLabel(message).apply { font = Font("Segoe UI", Font.BOLD, 13); foreground = Color(0xE8, 0xF4, 0xFF) }, java.awt.BorderLayout.CENTER)

    toast.contentPane = panel
    toast.pack()

    // Bottom-centre of the parent, using on-screen position so it also works when the window is moved/maximised
    val loc = parent.locationOnScreen
    toast.location = Point(loc.x + (parent.width - toast.width) / 2, loc.y + parent.height - toast.height - 60)
    toast.opacity = 0f
    toast.isVisible = true

    // fade in -> hold -> fade out
    var opacity = 0f
    var fadingIn = true
    val start = System.currentTimeMillis()
    Timer(16, null).apply {
        addActionListener {
            val elapsed = System.currentTimeMillis() - start
            if (fadingIn) {
                opacity = (opacity + 0.12f).coerceAtMost(1f)
                if (opacity >= 1f) fadingIn = false
            } else if (elapsed > durationMs) {
                opacity = (opacity - 0.08f).coerceAtLeast(0f)
                if (opacity <= 0f) { stop(); toast.dispose(); return@addActionListener }
            }
            toast.opacity = opacity
        }
        start()
    }
}


fun colorJsonInTextPane(textPane: JTextPane, jsonString: String) {
    val doc: StyledDocument = textPane.styledDocument
    doc.remove(0, doc.length)

    // 🎨 Color styles
    val keyAttr = SimpleAttributeSet().apply {
        StyleConstants.setForeground(this, Color(86, 156, 214)) // blue
        StyleConstants.setBold(this, true)
    }

    val stringAttr = SimpleAttributeSet().apply {
        StyleConstants.setForeground(this, Color(106, 153, 85)) // green
    }

    val numberAttr = SimpleAttributeSet().apply {
        StyleConstants.setForeground(this, Color(181, 206, 168)) // light green/orange mix
    }

    val booleanAttr = SimpleAttributeSet().apply {
        StyleConstants.setForeground(this, Color(197, 134, 192)) // purple
        StyleConstants.setBold(this, true)
    }

    val nullAttr = SimpleAttributeSet().apply {
        StyleConstants.setForeground(this, Color(128, 128, 128)) // gray
        StyleConstants.setItalic(this, true)
    }

    val braceAttr = SimpleAttributeSet().apply {
        StyleConstants.setForeground(this, Color(212, 212, 212)) // light gray
    }

    val colonAttr = SimpleAttributeSet().apply {
        StyleConstants.setForeground(this, Color.WHITE)
    }

    // 🔍 Regex patterns
    val keyPattern = Regex("\"(\\w+)\"(?=\\s*:)")
    val stringPattern = Regex(":\\s*\"([^\"]*)\"")
    val numberPattern = Regex(":\\s*(-?\\d+(\\.\\d+)?)")
    val booleanPattern = Regex(":\\s*(true|false)")
    val nullPattern = Regex(":\\s*(null)")
    val bracePattern = Regex("[{}\\[\\]]")
    val colonPattern = Regex(":")

    val gson = GsonBuilder().setPrettyPrinting().create()
    val prettyJson = gson.toJson(JsonParser.parseString(jsonString))

    var i = 0

    while (i < prettyJson.length) {
        var matched = false

        fun match(pattern: Regex, attr: AttributeSet) {
            val m = pattern.find(prettyJson, i)
            if (m != null && m.range.first == i) {
                doc.insertString(doc.length, m.value, attr)
                i = m.range.last + 1
                matched = true
            }
        }

        match(bracePattern, braceAttr)
        if (matched) continue

        match(keyPattern, keyAttr)
        if (matched) continue

        match(stringPattern, stringAttr)
        if (matched) continue

        match(numberPattern, numberAttr)
        if (matched) continue

        match(booleanPattern, booleanAttr)
        if (matched) continue

        match(nullPattern, nullAttr)
        if (matched) continue

        match(colonPattern, colonAttr)
        if (matched) continue

        // default text
        doc.insertString(doc.length, prettyJson[i].toString(), null)
        i++
    }
}

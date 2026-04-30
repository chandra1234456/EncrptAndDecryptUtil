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
fun showToast(parent: javax.swing.JFrame, message: String, durationMs: Int = 2000) {
    val toast = JWindow(parent)

    // Create label for the message
    val label = JLabel(message)
    label.font = Font("Arial", Font.BOLD, 14)
    label.background = Color(0, 0, 0, 170) // semi-transparent black
    label.foreground = Color.WHITE
    label.isOpaque = true
    label.horizontalAlignment = JLabel.CENTER
    label.verticalAlignment = JLabel.CENTER
    label.border = javax.swing.BorderFactory.createEmptyBorder(10, 20, 10, 20)

    toast.contentPane.add(label)
    toast.pack()

    // Position the toast at the bottom center of the parent frame
    val parentLocation = parent.location
    val x = parentLocation.x + (parent.width - toast.width) / 2
    val y = parentLocation.y + parent.height - toast.height - 50
    toast.location = Point(x, y)

    // Show the toast
    toast.isVisible = true

    // Hide it automatically after durationMs
    Timer(durationMs) { toast.dispose() }.start()
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

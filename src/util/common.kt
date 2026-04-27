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
    doc.remove(0, doc.length) // clear existing text

    val keyAttr = SimpleAttributeSet().apply { StyleConstants.setForeground(this, java.awt.Color.BLUE) }
    val stringAttr = SimpleAttributeSet().apply { StyleConstants.setForeground(this, java.awt.Color(0,128,0)) } // green
    val braceAttr = SimpleAttributeSet().apply { StyleConstants.setForeground(this, java.awt.Color.RED) }

    val keyPattern = Regex("\"(\\w+)\"(?=\\s*:)")       // keys
    val stringPattern = Regex(":\\s*\"([^\"]*)\"")      // string values
    val bracePattern = Regex("[{}\\[\\]]")             // braces

    var currentIndex = 0

    val gson = GsonBuilder().setPrettyPrinting().create()
    val prettyJson = gson.toJson(JsonParser.parseString(jsonString))

    while (currentIndex < prettyJson.length) {
        var matched = false

        // Check braces
        bracePattern.find(prettyJson, currentIndex)?.takeIf { it.range.first == currentIndex }?.let {
            doc.insertString(doc.length, it.value, braceAttr)
            currentIndex = it.range.last + 1
            matched = true
        }

        // Check keys
        keyPattern.find(prettyJson, currentIndex)?.takeIf { it.range.first == currentIndex }?.let {
            doc.insertString(doc.length, it.value, keyAttr)
            currentIndex = it.range.last + 1
            matched = true
        }

        // Check string values
        stringPattern.find(prettyJson, currentIndex)?.takeIf { it.range.first == currentIndex }?.let {
            doc.insertString(doc.length, it.value, stringAttr)
            currentIndex = it.range.last + 1
            matched = true
        }

        // If no match, insert one char as default
        if (!matched) {
            doc.insertString(doc.length, prettyJson[currentIndex].toString(), null)
            currentIndex++
        }
    }
}


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

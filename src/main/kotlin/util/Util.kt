package com.tinyfoxes.translationhelper.util

import com.tinyfoxes.translationhelper.model.TranslationString
import java.io.File
import java.io.FileInputStream
import java.nio.file.Files
import java.util.prefs.Preferences

object Util {

    fun getPreferences(): Preferences = Preferences.userNodeForPackage(Util::class.java)

    fun printListOfTranslationString(list: List<TranslationString>) {
        list.forEach { item: TranslationString ->
            println("L${item.linenumber} [${item.section}] ${item.key}=${item.translation}")
        }
    }

    fun loadTranslationStrings(text: String): List<TranslationString> {
        val listOfTranslationStrings = mutableListOf<TranslationString>()
        var latestSection: String? = null
        var linenumber = 0
        val lines = text.split("\n")
        lines.forEach { line ->
            linenumber++
            if (line.startsWith(";") || line.isBlank()) {
                //comment line or empty line, skip
                return@forEach
            }
            if (line.startsWith("[")) {
                //section header
                latestSection = line.removePrefix("[").removeSuffix("]")
                return@forEach
            }
            //translation string
            if (line.contains("=")) {
                if (line.last() == '=') {
                    //Key without value
                    listOfTranslationStrings.add(TranslationString(latestSection, line.removeSuffix("="), "", linenumber))
                    return@forEach
                }
                //Key with value
                val (key, value) = line.split("=")
                listOfTranslationStrings.add(TranslationString(latestSection, key, value, linenumber))
            }
        }

        return listOfTranslationStrings
    }

    fun loadTranslationStrings(rootFolder: File, subFolder: String, langCode: String): List<TranslationString> {
        val listOfTranslationStrings = mutableListOf<TranslationString>()
        val file = File(rootFolder, "${subFolder}${System.getProperty("file.separator")}${langCode}.ini")
        val fis = FileInputStream(file)
        val br = fis.bufferedReader()
        val streamOfString = br.lines()
        var latestSection: String? = null
        var linenumber = 0
        streamOfString.forEach { line ->
            linenumber++
            if (line.startsWith(";") || line.isBlank()) {
                //comment line or empty line, skip
                return@forEach
            }
            if (line.startsWith("[")) {
                //section header
                latestSection = line.removePrefix("[").removeSuffix("]")
                return@forEach
            }
            //translation string
            if (line.contains("=")) {
                if (line.last() == '=') {
                    //Key without value
                    listOfTranslationStrings.add(TranslationString(latestSection, line.removeSuffix("="), "", linenumber))
                    return@forEach
                }
                //Key with value
                val (key, value) = line.split("=")
                listOfTranslationStrings.add(TranslationString(latestSection, key, value, linenumber))
            }
        }
        return listOfTranslationStrings
    }

    fun saveTranslationStrings(rootFolder: File, subFolder: String, langCode: String, updatedStrings: List<TranslationString>) {
        //Try to only update lines that are different
        //Find changed strings
        val ogStrings = loadTranslationStrings(rootFolder, subFolder, langCode)
        val changedStrings = mutableListOf<TranslationString>()
        updatedStrings.forEach { updated ->
            val og = ogStrings.firstOrNull { ogTs -> ogTs.toId() == updated.toId() }

            if (og == null || updated.linenumber == -1) {
                //This one is added
                changedStrings.add(updated)
                return@forEach
            }

            if (og.translation != updated.translation) {
                //This one is changed
                changedStrings.add(updated)
            }
        }
        //
        val file = File(rootFolder, "${subFolder}${System.getProperty("file.separator")}${langCode}.ini")
        val fileLines = Files.readAllLines(file.toPath())
        changedStrings.forEach { ts ->
            if (ts.linenumber == -1 || ts.linenumber > fileLines.size - 1) {
                //Added one
                fileLines.add("${ts.key}=${ts.translation}")
            } else {
                //Updated one
                // array is zero based, line numbers are one based.
                fileLines[(ts.linenumber-1)] = "${ts.key}=${ts.translation}"
            }
        }
        //Files.write(file.toPath(), fileLines)
    }
}
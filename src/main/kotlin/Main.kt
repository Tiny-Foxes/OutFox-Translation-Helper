package com.tinyfoxes.translationhelper

import com.tinyfoxes.translationhelper.util.Util
import com.tinyfoxes.translationhelper.model.TranslationString
import com.tinyfoxes.translationhelper.ui.UiMain
import com.tinyfoxes.translationhelper.util.AppPreferences
import com.tinyfoxes.translationhelper.util.initTranslations
import com.tinyfoxes.translationhelper.util.s
import java.io.File
import kotlin.system.exitProcess

//Preferences
var appLanguage: String = "en" //en, nl, or pt-br
var rootFolder: File? = null //File("/Users/frankbouwens/priv/Outfox/Tiny-Foxes/OutFox-NL/")
var subFolder: String? = null //"_fallback" or "default"
var sourceLangCode: String? = null //"en"
var targetLangCode: String? = null //"nl"

fun main(args: Array<String>) {
    println(s("[general]Translation Helper for Project OutFox; by FrankkieNL"))

    initAppPreferences()

    while (true) {
        startMenu()
    }
}

fun initAppPreferences() {
    val prefs = Util.getPreferences()
    appLanguage = prefs.get(AppPreferences.APP_LANGUAGE, "en")
    val rootFolderString = prefs.get(AppPreferences.ROOT_FOLDER, null)
    if (rootFolderString != null && rootFolderString.isNotBlank()) {
        rootFolder = File(rootFolderString)
    }
    subFolder = prefs.get(AppPreferences.SUB_FOLDER, null)
    sourceLangCode = prefs.get(AppPreferences.SOURCE_LANG_CODE, null)
    targetLangCode = prefs.get(AppPreferences.TARGET_LANG_CODE, null)
}

fun startMenu() {
    println(
        """     
        ${s("[cli]Menu:")}
        0) ${s("[cli]Exit")}
        1) ${s("[cli]Check for missing keys")}
        2) ${s("[cli]Check for non-translated keys")}
        3) ${s("[cli]Check app settings")}
        4) ${s("[cli]Edit app settings")}
        5) ${s("[cli]Open UI")}
    """.trimIndent()
    )
    val input = readln()
    if (input.toIntOrNull() != null) {
        when (input.toInt()) {
            0 -> exitProcess(0)
            1 -> checkForMissingKeys()
            2 -> checkForNonTranslatedKeys()
            3 -> checkAppSettings()
            4 -> editAppSettings()
            5 -> openUI()
            else -> println(s("[cli]Number not found"))
        }
    }
}

private fun openUI() {
    UiMain()
}

fun askRootFolder(): File? {
    println(s("[cli]Please provide the root folder for the translations: (full path) (e.g. /~/Tiny-Foxes/OutFox-NL/ )"))
    val input = readln()
    val tempFile = File(input)
    if (!tempFile.exists()) {
        println(s("[cli]File does not exist"))
        return null
    }
    if (!tempFile.canRead()) {
        println(s("[cli]File cannot be read"))
        return null
    }
    if (!tempFile.isDirectory) {
        println(s("[cli]File is not a directory"))
        return null
    }
    //All ok
    return tempFile
}

fun askLangCode(prompt: String): String? {
    println(prompt)
    val langCode = readln()
    if (!"\\w\\w".toRegex().matches(langCode) && !"\\w\\w-\\w\\w".toRegex().matches(langCode)) {
        println(String.format(s("[cli]That is not a valid letter language code %s."), langCode))
        return null
    }
    return langCode
}

fun askAppLanguage(): String? {
    println("[cli]Please select a language;")
    println("[cli]Fill in one of the following languages codes:")
    println(
        """
        en
        nl
        pt-br
    """.trimIndent()
    )
    println("[cli]Or leave empty to cancel")
    val langCode = readln().lowercase()
    if (!"(en|nl|pt\\-br)".toRegex().matches(langCode)) {
        println(String.format(s("[cli]That is not a valid letter language code %s."), langCode))
        return null
    }
    return langCode
}

fun askSubFolder(): String {
    println(s("[cli]Please enter sub-folder (leave blank for \"_fallback\")"))
    val input = readln()
    if (input.isBlank()) {
        return "_fallback"
    }
    return input
}

/**
 * Returns true when all is well
 */
fun checkSettings(): Boolean {
    if (rootFolder == null) {
        rootFolder = askRootFolder() ?: return false
    }
    val safeRootFolder = rootFolder ?: return false
    println(String.format(s("[cli]Set as root folder: %s"), safeRootFolder.absolutePath))
    Util.getPreferences().put(AppPreferences.ROOT_FOLDER, safeRootFolder.absolutePath)

    if (subFolder == null) {
        subFolder = askSubFolder()
    }
    val safeSubFolder = subFolder ?: return false
    println(String.format(s("[cli]Set as sub folder: %s"), safeSubFolder))
    Util.getPreferences().put(AppPreferences.SUB_FOLDER, safeSubFolder)

    if (sourceLangCode == null) {
        sourceLangCode = askLangCode(s("[cli]Please enter source language (as language code, e.g. 'en')")) ?: return false
    }
    val safeSourceLangCode = sourceLangCode ?: return false
    Util.getPreferences().put(AppPreferences.SOURCE_LANG_CODE, safeSourceLangCode)

    if (targetLangCode == null) {
        targetLangCode = askLangCode(s("[cli]Please enter target language (as language code, e.g. 'nl')")) ?: return false
    }
    val safeTargetLangCode = targetLangCode ?: return false
    Util.getPreferences().put(AppPreferences.TARGET_LANG_CODE, safeTargetLangCode)

    return true
}

fun checkForMissingKeys() {
    if (!checkSettings()) {
        return
    }
    val safeRootFolder = rootFolder ?: return
    val safeSubFolder = subFolder ?: return
    val safeSourceLangCode = sourceLangCode ?: return
    val safeTargetLangCode = targetLangCode ?: return

    //
    val sourceStrings = Util.loadTranslationStrings(safeRootFolder, safeSubFolder, safeSourceLangCode)
    val targetStrings = Util.loadTranslationStrings(safeRootFolder, safeSubFolder, safeTargetLangCode)

    var numMissingSourceLang = 0
    var numMissingTargetLang = 0
    println(s("Checking for missing keys..."))
    println(String.format(s("[cli]%s strings missing in the %s strings: "), safeSourceLangCode, safeTargetLangCode))
    sourceStrings.forEach { sourceString: TranslationString ->
        if (!targetStrings.contains(sourceString)) {
            println("L${sourceString.linenumber} [${sourceString.section}] ${sourceString.key}=${sourceString.translation}")
            numMissingSourceLang++
        }
    }
    if (numMissingSourceLang == 0) {
        println(s("[cli]No missing strings"))
    }

    println(String.format(s("[cli]%s strings missing in the %s strings: "), safeTargetLangCode, safeSourceLangCode))
    targetStrings.forEach { targetString: TranslationString ->
        if (!sourceStrings.contains(targetString)) {
            println("L${targetString.linenumber} [${targetString.section}] ${targetString.key}=${targetString.translation}")
            numMissingTargetLang++
        }
    }
    if (numMissingTargetLang == 0) {
        println(s("[cli]No missing strings"))
    }

    println(String.format(s("[cli]Done. Missing %s: %d; Missing %s: %d"), safeSourceLangCode, numMissingSourceLang, safeTargetLangCode, numMissingTargetLang))
    println(String.format(s("[cli]Total strings: %s"), sourceStrings.size))
}

private fun checkForNonTranslatedKeys() {
    if (!checkSettings()) {
        return
    }
    val safeRootFolder = rootFolder ?: return
    val safeSubFolder = subFolder ?: return
    val safeSourceLangCode = sourceLangCode ?: return
    val safeTargetLangCode = targetLangCode ?: return

    val sourceStrings = Util.loadTranslationStrings(safeRootFolder, safeSubFolder, safeSourceLangCode)
    val targetStrings = Util.loadTranslationStrings(safeRootFolder, safeSubFolder, safeTargetLangCode)

    var sameTranslations = 0
    println(s("[cli]Translations that are the same (possibly not translated):"))
    sourceStrings.forEach { sourceString ->
        //ignore translations that consists only out of numbers (e.g. "Dance_Double=8")
        if ("\\d+".toRegex().matches(sourceString.translation)) {
            return@forEach
        }
        val targetString = targetStrings.firstOrNull { it.key == sourceString.key } ?: return@forEach
        if (sourceString.translation == targetString.translation) {
            println("L${sourceString.linenumber} [${sourceString.section}] ${sourceString.key}=${sourceString.translation}")
            sameTranslations++
        }
    }
    println(String.format(s("[cli]Number of same translations: %s"), sameTranslations))
}

private fun checkAppSettings() {
    val prefs = Util.getPreferences()
    println(
        """
        ${AppPreferences.APP_LANGUAGE}=${prefs.get(AppPreferences.APP_LANGUAGE, "null")}
        ${AppPreferences.ROOT_FOLDER}=${prefs.get(AppPreferences.ROOT_FOLDER, "null")}
        ${AppPreferences.SUB_FOLDER}=${prefs.get(AppPreferences.SUB_FOLDER, "null")}
        ${AppPreferences.SOURCE_LANG_CODE}=${prefs.get(AppPreferences.SOURCE_LANG_CODE, "null")}
        ${AppPreferences.TARGET_LANG_CODE}=${prefs.get(AppPreferences.TARGET_LANG_CODE, "null")}      
    """.trimIndent()
    )
}

private fun editAppSettings() {
    println(
        """
        ${s("[cli]Menu:")}
        0) ${s("[cli]Cancel")}
        1) ${AppPreferences.APP_LANGUAGE}
        2) ${AppPreferences.ROOT_FOLDER}
        3) ${AppPreferences.SUB_FOLDER}
        4) ${AppPreferences.SOURCE_LANG_CODE}
        5) ${AppPreferences.TARGET_LANG_CODE}
    """.trimIndent()
    )


    val input1 = readln()
    if (input1.toIntOrNull() == null) return
    when (input1.toInt()) {
        -1 -> {
            //Clear all settings
            Util.getPreferences().clear()
        }

        0 -> {
            return
        }

        1 -> {
            val tempAppLanguage = askAppLanguage()
            if (tempAppLanguage != null) {
                appLanguage = tempAppLanguage
                println(String.format(s("[cli]Set as app language: %s"), appLanguage))
                initTranslations()
                Util.getPreferences().put(AppPreferences.APP_LANGUAGE, appLanguage)
            }
        }

        2 -> {
            rootFolder = askRootFolder()
            rootFolder?.let { safeRootFolder ->
                println(String.format(s("[cli]Set as root folder: %s"), safeRootFolder.absolutePath))
                Util.getPreferences().put(AppPreferences.ROOT_FOLDER, safeRootFolder.absolutePath)
            }
        }

        3 -> {
            subFolder = askSubFolder()
            subFolder?.let { safeSubFolder ->
                println(String.format(s("[cli]Set as sub folder: %s"), safeSubFolder))
                Util.getPreferences().put(AppPreferences.SUB_FOLDER, safeSubFolder)
            }
        }

        4 -> {
            sourceLangCode = askLangCode(s("[cli]Please enter source language (as language code, e.g. 'en')")) ?: return
            Util.getPreferences().put(AppPreferences.SOURCE_LANG_CODE, sourceLangCode)
        }

        5 -> {
            targetLangCode = askLangCode(s("[cli]Please enter target language (as language code, e.g. 'nl')")) ?: return
            Util.getPreferences().put(AppPreferences.TARGET_LANG_CODE, targetLangCode)

        }

        else -> println(s("[cli]Number not found"))
    }
}
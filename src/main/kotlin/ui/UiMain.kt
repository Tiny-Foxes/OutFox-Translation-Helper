package com.tinyfoxes.translationhelper.ui

import com.tinyfoxes.translationhelper.model.TranslationString
import com.tinyfoxes.translationhelper.rootFolder
import com.tinyfoxes.translationhelper.sourceLangCode
import com.tinyfoxes.translationhelper.subFolder
import com.tinyfoxes.translationhelper.targetLangCode
import com.tinyfoxes.translationhelper.util.Util
import com.tinyfoxes.translationhelper.util.s
import java.awt.Dimension
import javax.swing.JFileChooser
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JMenuItem
import javax.swing.JOptionPane
import javax.swing.JScrollPane
import javax.swing.JTable
import javax.swing.UIManager
import javax.swing.event.TableModelListener
import javax.swing.table.*
import kotlin.system.exitProcess

class UiMain {
    lateinit var frame: JFrame
    lateinit var table: JTable

    init {
        initUI()
    }

    private fun initUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName())
        } catch (e: Exception) {
            //ignore
        }
        frame = JFrame()
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.size = Dimension(1200, 800)
        initMenu()
        initBody()
        frame.isVisible = true
    }

    //<editor-fold desc="MENU">
    private fun initMenu() {
        val menuBar = JMenuBar()
        //
        val fileMenu = JMenu(s("[ui]File"))
        val fileOpenFolderItem = JMenuItem(s("[ui]Open root folder"))
        fileOpenFolderItem.addActionListener { openFolder() }
        fileMenu.add(fileOpenFolderItem)
        //
        val fileSaveItem = JMenuItem(s("[ui]Save"))
        fileSaveItem.addActionListener { save() }
        fileMenu.add(fileSaveItem)
        //
        val filePreferencesItem = JMenuItem(s("[ui]Preferences"))
        filePreferencesItem.addActionListener { preferences() }
        fileMenu.add(filePreferencesItem)
        //
        val fileExitItem = JMenuItem(s("[ui]Exit"))
        fileExitItem.addActionListener { exit() }
        fileMenu.add(fileExitItem)
        menuBar.add(fileMenu)
        //
        frame.jMenuBar = menuBar
    }

    private fun openFolder() {
        val fileChooser = JFileChooser()
        fileChooser.dialogTitle = s("[ui]Open root folder")
        fileChooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
        val answer = fileChooser.showOpenDialog(null)
        if (answer == JFileChooser.APPROVE_OPTION) {
            val folderToOpen = fileChooser.selectedFile
            if (folderToOpen.exists() && folderToOpen.isDirectory) {
                rootFolder = folderToOpen
                JOptionPane.showMessageDialog(null, s("[ui]Root folder opened:") + "\n" + folderToOpen.absolutePath)

                if (subFolder == null) {
                    openSubFolder()
                }

                if (sourceLangCode == null) {
                    setSourceLangCode()
                }

                if (targetLangCode == null) {
                    setTargetLangCode()
                }
            }
        }
    }

    private fun openSubFolder() {
        val fileChooser = JFileChooser()
        fileChooser.dialogTitle = s("[ui]Open sub folder")
        fileChooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
        val answer = fileChooser.showOpenDialog(null)
        if (answer == JFileChooser.APPROVE_OPTION) {
            val folderToOpen = fileChooser.selectedFile
            if (folderToOpen.exists() && folderToOpen.isDirectory) {
                subFolder = folderToOpen.name
                JOptionPane.showMessageDialog(null, s("[ui]Sub folder opened:") + "\n" + folderToOpen.name)
            }
        }
    }

    private fun setSourceLangCode() {
        val input = JOptionPane.showInputDialog(s("[ui]Set source language code (e.g. 'en')"))
        //TODO: check if input is valid?
        sourceLangCode = input
    }

    private fun setTargetLangCode() {
        val input = JOptionPane.showInputDialog(s("[ui]Set target language code (e.g. 'nl')"))
        //TODO: check if input is valid?
        targetLangCode = input
    }

    private fun save() {
        val answer = JOptionPane.showConfirmDialog(null, s("[ui]Save? (this will overwrite existing data)"))
        if (answer != JOptionPane.YES_OPTION) {
            return
        }
        val safeRootFolder = rootFolder ?: return
        val safeSubFolder = subFolder ?: return
        val safeTargetLangCode = targetLangCode ?: return
        //
        val myTableModel = table.model as MyTableModel
        val updatedTranslations = myTableModel.getUpdatedTargetTranslationStrings()
        //
        Util.saveTranslationStrings(safeRootFolder, safeSubFolder, safeTargetLangCode, updatedTranslations)
    }

    private fun preferences() {
        JOptionPane.showMessageDialog(null, s("[ui]Change preferences via CLI"))
    }

    private fun exit() {
        val choice = JOptionPane.showConfirmDialog(null, s("[ui]Are you sure you want to exit?"))
        if (choice == JOptionPane.YES_OPTION) {
            exitProcess(0)
        }
    }
    //</editor-fold>

    private fun initBody() {
        if (rootFolder == null || subFolder == null) {
            initEmptyBody()
        } else {
            initTranslationBody()
        }
    }

    private fun initEmptyBody() {
        val label = JLabel(s("[ui]Open root folder first!"))
        label.isOpaque = true
        frame.contentPane = label
    }

    private fun initTranslationBody() {
        if (rootFolder == null
            || subFolder == null
            || sourceLangCode == null
            || targetLangCode == null
        ) {
            JOptionPane.showMessageDialog(null, s("[ui]Error: one of the following isn't set: Root folder, sub folder, source language code or target language code. Please fix that first."))
            return
        }

        val table = createTable()
        val scrollPane = JScrollPane(table)
        scrollPane.isOpaque = true
        frame.contentPane = scrollPane
    }

    private fun createTable(): JTable? {
        val safeRootFolder = rootFolder ?: return null
        val safeSubFolder = subFolder ?: return null
        val safeSourceLangCode = sourceLangCode ?: return null
        val safeTargetLangCode = targetLangCode ?: return null
        //
        val sourceStrings = Util.loadTranslationStrings(safeRootFolder, safeSubFolder, safeSourceLangCode)
        val targetStrings = Util.loadTranslationStrings(safeRootFolder, safeSubFolder, safeTargetLangCode)
        //
        table = JTable()
        val myTableModel = MyTableModel(sourceStrings, targetStrings)
        table.model = myTableModel
        //
        table.preferredScrollableViewportSize = Dimension(1200, 800)
        table.fillsViewportHeight = true
        return table
    }

    class MyTableModel(
        val sourceStrings: List<TranslationString>,
        val targetStrings: List<TranslationString>
    ) : TableModel {
        private val columnNames = arrayOf(s("[ui]Keys"), "${s("[ui]Source")} $sourceLangCode", "${s("[ui]Target")} $targetLangCode")
        private var updatedTargetStrings = targetStrings.toMutableList()

        fun getUpdatedTargetTranslationStrings(): List<TranslationString> {
            return updatedTargetStrings
        }

        override fun getRowCount(): Int {
            return sourceStrings.size
        }

        override fun getColumnCount(): Int {
            return columnNames.size
        }

        override fun getColumnName(columnIndex: Int): String {
            return columnNames[columnIndex]
        }

        override fun getColumnClass(columnIndex: Int): Class<*> {
            return String::class.java
        }

        override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean {
            return (columnIndex == 2) //only target lang is editable
        }

        override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
            return when (columnIndex) {
                0 -> sourceStrings[rowIndex].toId()
                1 -> sourceStrings[rowIndex].translation
                2 -> updatedTargetStrings.firstOrNull { ts -> ts.toId() == sourceStrings[rowIndex].toId() }?.translation ?: ""
                else -> ""
            }
        }

        override fun setValueAt(aValue: Any?, rowIndex: Int, columnIndex: Int) {
            //TranslationStrings are immutable, so remove and add new one.
            val og = updatedTargetStrings.firstOrNull { ts -> ts.toId() == sourceStrings[rowIndex].toId() }
            if (og == null) {
                val temp = TranslationString.idToSectionAndKey(sourceStrings[rowIndex].toId()) ?: return
                val added = TranslationString(temp[0], temp[1], aValue?.toString() ?: "", -1)
                updatedTargetStrings.add(added)
            } else {
                updatedTargetStrings.remove(og)
                val updated = TranslationString(og.section, og.key, aValue.toString(), og.linenumber)
                updatedTargetStrings.add(updated)
            }
        }

        override fun addTableModelListener(l: TableModelListener?) {
            //TODO("Not yet implemented")
        }

        override fun removeTableModelListener(l: TableModelListener?) {
            //TODO("Not yet implemented")
        }
    }
}
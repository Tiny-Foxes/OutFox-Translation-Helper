package com.tinyfoxes.translationhelper.ui

import com.tinyfoxes.translationhelper.rootFolder
import com.tinyfoxes.translationhelper.sourceLangCode
import com.tinyfoxes.translationhelper.subFolder
import com.tinyfoxes.translationhelper.targetLangCode
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
import javax.swing.table.*
import kotlin.system.exitProcess

class UiMain {
    lateinit var frame: JFrame

    init {
        initUI()
    }

    private fun initUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
        } catch (e: Exception) {
            //ignore
        }
        frame = JFrame()
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        initMenu()
        initBody()
        frame.isVisible = true
    }

    //<editor-fold desc="MENU">
    private fun initMenu() {
        val menuBar = JMenuBar()
        //
        val fileMenu = JMenu()
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
        frame.size = Dimension(600, 400)
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
                JOptionPane.showMessageDialog(null, s("[ui]Root folder opened:\n") + folderToOpen.absolutePath)

                if (subFolder == null) {
                    openSubFolder()
                }

                if (sourceLangCode == null) {
                    setSourceLangCode()
                }

                if (targetLangCode == null) {
                    setTargetLangCode()
                }
                //
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
                JOptionPane.showMessageDialog(null, s("[ui]Sub folder opened:\n") + folderToOpen.name)
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
        if (rootFolder == null || subFolder == null) return
        //TODO: save
    }

    private fun preferences() {
        //TODO: preferences
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
        frame.contentPane.removeAll()
        frame.contentPane.add(label)
    }

    private fun initTranslationBody() {
        if (rootFolder == null
            || subFolder == null
            || sourceLangCode == null
            || targetLangCode == null
        ) {
            JOptionPane.showMessageDialog(null, s("Error: one of the following isn't set: Root folder, sub folder, source language code or target language code. Please fix that first."))
            return
        }
        val scrollPane = JScrollPane()
        val table = createTable()
        scrollPane.add(table)
        frame.contentPane.removeAll()
        frame.contentPane.add(scrollPane)
    }

    private fun createTable(): JTable {
        if (rootFolder == null
            || subFolder == null
            || sourceLangCode == null
            || targetLangCode == null
        ) {
            throw IllegalArgumentException()
        }
        val table = JTable()
        //Columns
//        val columnModel = DefaultTableColumnModel()
//        val columnKeys = TableColumn()
//        columnKeys.headerValue = s("[ui]Keys")
//        columnModel.addColumn(columnKeys)
//        val columnSourceLang = TableColumn()
//        columnSourceLang.headerValue = sourceLangCode
//        columnModel.addColumn(columnSourceLang)
//        val columnTargetLang = TableColumn()
//        columnTargetLang.headerValue = targetLangCode
//        columnModel.addColumn(columnTargetLang)
//        for (column in columnModel.columns) {
//            column.minWidth = 50
//        }
//        table.columnModel = columnModel
        //Rows
        val tableModel = DefaultTableModel()
        tableModel.addColumn(s("[ui]Keys"))
        tableModel.addColumn(sourceLangCode)
        tableModel.addColumn(targetLangCode)
        table.model = tableModel
        //
        table.fillsViewportHeight = true
        return table
    }
}
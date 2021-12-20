package com.blogspot.kotlinstudy.lognote

import java.awt.*
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.StringSelection
import java.awt.event.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import javax.swing.*
import javax.swing.event.PopupMenuEvent
import javax.swing.event.PopupMenuListener
import javax.swing.plaf.basic.BasicScrollBarUI
import javax.swing.text.JTextComponent
import kotlin.system.exitProcess


class MainUI(title: String) : JFrame() {
    private lateinit var mMenuBar: JMenuBar
    private lateinit var mMenuFile: JMenu
    private lateinit var mItemFileOpen: JMenuItem
    private lateinit var mItemFileOpenFiles: JMenuItem
    private lateinit var mItemFileAppendFiles: JMenuItem
//    private lateinit var mItemFileOpenRecents: JMenu
    private lateinit var mItemFileExit: JMenuItem
    private lateinit var mMenuView: JMenu
    private lateinit var mItemFull: JCheckBoxMenuItem
    private lateinit var mMenuSettings: JMenu
    private lateinit var mItemAdb: JMenuItem
    private lateinit var mItemLogFile: JMenuItem
    private lateinit var mItemFont: JMenuItem
    private lateinit var mItemFilterIncremental: JCheckBoxMenuItem
    private lateinit var mMenuLogLevel: JMenu
    private lateinit var mLogLevelGroup: ButtonGroup
    private lateinit var mMenuHelp: JMenu
    private lateinit var mItemHelp: JMenuItem
    private lateinit var mItemAbout: JMenuItem

    private lateinit var mFilterPanel: JPanel
    private lateinit var mFilterLeftPanel: JPanel

    private lateinit var mLogToolBar: JPanel
    private lateinit var mStartBtn: ColorButton
    private lateinit var mStopBtn: ColorButton
    private lateinit var mPauseBtn: ColorButton
    private lateinit var mClearBtn: ColorButton
    private lateinit var mClearSaveBtn: ColorButton
    private lateinit var mRotationBtn: ColorButton
    private lateinit var mFiltersBtn: ColorButton
    private val SPLIT_WEIGHT = 0.7

    private lateinit var mLogPanel: JPanel
    private lateinit var mShowLogPanel: JPanel
    private lateinit var mMatchCaseBtn: ColorToggleButton
    private lateinit var mShowLogCombo: ColorComboBox<String>
    private lateinit var mShowLogToggle: ColorToggleButton
    private lateinit var mShowLogTogglePanel: JPanel

    private lateinit var mBoldLogPanel: JPanel
    private lateinit var mBoldLogCombo: ColorComboBox<String>
    private lateinit var mBoldLogToggle: ColorToggleButton
    private lateinit var mBoldLogTogglePanel: JPanel

    private lateinit var mShowTagPanel: JPanel
    private lateinit var mShowTagCombo: ColorComboBox<String>
    private lateinit var mShowTagToggle: ColorToggleButton
    private lateinit var mShowTagTogglePanel: JPanel

    private lateinit var mShowPidPanel: JPanel
    private lateinit var mShowPidCombo: ColorComboBox<String>
    private lateinit var mShowPidToggle: ColorToggleButton
    private lateinit var mShowPidTogglePanel: JPanel

    private lateinit var mShowTidPanel: JPanel
    private lateinit var mShowTidCombo: ColorComboBox<String>
    private lateinit var mShowTidToggle: ColorToggleButton
    private lateinit var mShowTidTogglePanel: JPanel

    private lateinit var mDeviceCombo: ColorComboBox<String>
    private lateinit var mDeviceStatus: JLabel
    private lateinit var mAdbConnectBtn: ColorButton
    private lateinit var mAdbRefreshBtn: ColorButton
    private lateinit var mAdbDisconnectBtn: ColorButton

    private lateinit var mScrollbackLabel: JLabel
    private lateinit var mScrollbackTF: JTextField
    private lateinit var mScrollbackSplitFileCheck: JCheckBox
    private lateinit var mScrollbackApplyBtn: ColorButton
    private lateinit var mScrollbackKeepToggle: ColorToggleButton

    lateinit var mFilteredTableModel: LogTableModel
        private set

    private lateinit var mFullTableModel: LogTableModel

    private lateinit var mLogSplitPane: JSplitPane

    private lateinit var mFilteredLogPanel: LogPanel
    private lateinit var mFullLogPanel: LogPanel
    private var mSelectedLine = 0

    private lateinit var mStatusBar: JPanel
    private lateinit var mStatusTF: JTextField

    private val mFrameMouseListener = FrameMouseListener(this)
    private val mKeyHandler = KeyHandler()
    private val mItemHandler = ItemHandler()
    private val mLevelItemHandler = LevelItemHandler()
    private val mActionHandler = ActionHandler()
    private val mPopupMenuHandler = PopupMenuHandler()
    private val mMouseHandler = MouseHandler()
    private val mComponentHandler = ComponentHandler()

    private val mConfigManager = ConfigManager()
    private val mColorManager = ColorManager.getInstance()
    private var mIsCreatingUI = true

    private val mAdbManager = AdbManager.getInstance()
    private val mFiltersManager = FiltersManager(this, mConfigManager)

    private var mFrameX = 0
    private var mFrameY = 0
    private var mFrameWidth = 1280
    private var mFrameHeight = 720
    private var mFrameExtendedState = Frame.MAXIMIZED_BOTH

    private val ROTATION_LEFT_RIGHT = 0
    private val ROTATION_TOP_BOTTOM = 1
    private val ROTATION_RIGHT_LEFT = 2
    private val ROTATION_BOTTOM_TOP = 3
    private val ROTATION_MAX = ROTATION_BOTTOM_TOP
    private var mRotationStatus = ROTATION_LEFT_RIGHT

    private val VERBOSE = "Verbose"
    private val DEBUG = "Debug"
    private val INFO = "Info"
    private val WARNING = "Warning"
    private val ERROR = "Error"
    private val FATAL = "Fatal"

    var mFont: Font = Font("Dialog", Font.PLAIN, 12)
        set(value) {
            field = value
            if (mIsCreatingUI == false) {
                mFilteredLogPanel.mFont = value
                mFullLogPanel.mFont = value
            }
        }

    init {
        mConfigManager.loadConfig()
        val cmd = mConfigManager.mProperties.get(mConfigManager.ITEM_ADB_CMD) as? String
        if (!cmd.isNullOrEmpty()) {
            mAdbManager.mAdbCmd = cmd
        } else {
            val os = System.getProperty("os.name")
            System.out.println("OS : " + os)
            if (os.lowercase().contains("windows")) {
                mAdbManager.mAdbCmd = "adb.exe"
            } else {
                mAdbManager.mAdbCmd = "adb"
            }
        }
        mAdbManager.addEventListener(AdbHandler())
        mAdbManager.mLogSavePath = mConfigManager.mProperties.get(mConfigManager.ITEM_ADB_LOG_SAVE_PATH) as? String
        if (mAdbManager.mLogSavePath.isNullOrEmpty()) {
            mAdbManager.mLogSavePath = "."
        }

        val prefix = mConfigManager.mProperties.get(mConfigManager.ITEM_ADB_PREFIX) as? String
        if (prefix.isNullOrEmpty()) {
            mAdbManager.mPrefix = ""
        } else {
            mAdbManager.mPrefix = prefix
        }

        var prop = mConfigManager.mProperties.get(mConfigManager.ITEM_FRAME_X) as? String
        if (!prop.isNullOrEmpty()) {
            mFrameX = prop.toInt()
        }
        prop = mConfigManager.mProperties.get(mConfigManager.ITEM_FRAME_Y) as? String
        if (!prop.isNullOrEmpty()) {
            mFrameY = prop.toInt()
        }
        prop = mConfigManager.mProperties.get(mConfigManager.ITEM_FRAME_WIDTH) as? String
        if (!prop.isNullOrEmpty()) {
            mFrameWidth = prop.toInt()
        }
        prop = mConfigManager.mProperties.get(mConfigManager.ITEM_FRAME_HEIGHT) as? String
        if (!prop.isNullOrEmpty()) {
            mFrameHeight = prop.toInt()
        }
        prop = mConfigManager.mProperties.get(mConfigManager.ITEM_FRAME_EXTENDED_STATE) as? String
        if (!prop.isNullOrEmpty()) {
            mFrameExtendedState = prop.toInt()
        }
        prop = mConfigManager.mProperties.get(mConfigManager.ITEM_ROTATION) as? String
        if (!prop.isNullOrEmpty()) {
            mRotationStatus = prop.toInt()
        }

        prop = mConfigManager.mProperties.get(mConfigManager.ITEM_LANG) as? String
        if (!prop.isNullOrEmpty()) {
            Strings.lang = prop.toInt()
        }
        else {
            Strings.lang = Strings.EN
        }

        createUI(title)
        mAdbManager.getDevices()
    }

    private fun exit() {
        mConfigManager.saveConfig()
        mFilteredTableModel.stopScan()
        mFullTableModel.stopScan()
        mAdbManager.stop()
        exitProcess(0)
    }

    private fun createUI(title: String) {
        setTitle(title)

        val img = ImageIcon(this.javaClass.getResource("/images/logo.png"))
        iconImage = img.image

        defaultCloseOperation = EXIT_ON_CLOSE
        setLocation(mFrameX, mFrameY)
        setSize(mFrameWidth, mFrameHeight)
        extendedState = mFrameExtendedState
        addComponentListener(mComponentHandler)

        mMenuBar = JMenuBar()
        mMenuFile = JMenu(Strings.FILE)

        mItemFileOpen = JMenuItem(Strings.OPEN)
        mItemFileOpen.addActionListener(mActionHandler)
        mMenuFile.add(mItemFileOpen)

        mItemFileOpenFiles = JMenuItem(Strings.OPEN_FILES)
        mItemFileOpenFiles.addActionListener(mActionHandler)
        mMenuFile.add(mItemFileOpenFiles)

        mItemFileAppendFiles = JMenuItem(Strings.APPEND_FILES)
        mItemFileAppendFiles.addActionListener(mActionHandler)
        mMenuFile.add(mItemFileAppendFiles)

//        mItemFileOpenRecents = JMenu(Strings.OPEN_RECENTS)
//        mItemFileOpenRecents.addActionListener(mActionHandler)
//        mMenuFile.add(mItemFileOpenRecents)
        mMenuFile.addSeparator()

        mItemFileExit = JMenuItem(Strings.EXIT)
        mItemFileExit.addActionListener(mActionHandler)
        mMenuFile.add(mItemFileExit)
        mMenuBar.add(mMenuFile)

        mMenuView = JMenu(Strings.VIEW)

        mItemFull = JCheckBoxMenuItem(Strings.VIEW_FULL)
        mItemFull.addActionListener(mActionHandler)
        mMenuView.add(mItemFull)
        mMenuBar.add(mMenuView)

        mMenuSettings = JMenu(Strings.SETTING)

        mItemAdb = JMenuItem(Strings.ADB)
        mItemAdb.addActionListener(mActionHandler)
        mMenuSettings.add(mItemAdb)
        mItemLogFile = JMenuItem(Strings.LOGFILE)
        mItemLogFile.addActionListener(mActionHandler)
        mMenuSettings.add(mItemLogFile)

        mMenuSettings.addSeparator()

        mItemFont = JMenuItem(Strings.FONT + " & " + Strings.COLOR)
//        mItemFont = JMenuItem(Strings.FONT)
        mItemFont.addActionListener(mActionHandler)
        mMenuSettings.add(mItemFont)

        mMenuSettings.addSeparator()

        mItemFilterIncremental = JCheckBoxMenuItem(Strings.FILTER + "-" + Strings.INCREMENTAL)
        mMenuSettings.add(mItemFilterIncremental)

        mMenuSettings.addSeparator()

        mMenuLogLevel = JMenu(Strings.LOGLEVEL)
        mMenuLogLevel.addActionListener(mActionHandler)
        mMenuSettings.add(mMenuLogLevel)

        mLogLevelGroup = ButtonGroup()

        var menuItem = JRadioButtonMenuItem(VERBOSE)
        mLogLevelGroup.add(menuItem)
        mMenuLogLevel.add(menuItem)
        menuItem.isSelected = true
        menuItem.addItemListener(mLevelItemHandler)

        menuItem = JRadioButtonMenuItem(DEBUG)
        mLogLevelGroup.add(menuItem)
        mMenuLogLevel.add(menuItem)
        menuItem.addItemListener(mLevelItemHandler)

        menuItem = JRadioButtonMenuItem(INFO)
        mLogLevelGroup.add(menuItem)
        mMenuLogLevel.add(menuItem)
        menuItem.addItemListener(mLevelItemHandler)

        menuItem = JRadioButtonMenuItem(WARNING)
        mLogLevelGroup.add(menuItem)
        mMenuLogLevel.add(menuItem)
        menuItem.addItemListener(mLevelItemHandler)

        menuItem = JRadioButtonMenuItem(ERROR)
        mLogLevelGroup.add(menuItem)
        mMenuLogLevel.add(menuItem)
        menuItem.addItemListener(mLevelItemHandler)

        menuItem = JRadioButtonMenuItem(FATAL)
        mLogLevelGroup.add(menuItem)
        mMenuLogLevel.add(menuItem)
        menuItem.addItemListener(mLevelItemHandler)

        mMenuBar.add(mMenuSettings)

        mMenuHelp = JMenu(Strings.HELP)

        mItemHelp = JMenuItem(Strings.HELP)
        mItemHelp.addActionListener(mActionHandler)
        mMenuHelp.add(mItemHelp)

        mMenuHelp.addSeparator()

        mItemAbout = JMenuItem(Strings.ABOUT)
        mItemAbout.addActionListener(mActionHandler)
        mMenuHelp.add(mItemAbout)
        mMenuBar.add(mMenuHelp)

        jMenuBar = mMenuBar

        addMouseListener(mFrameMouseListener)
        addMouseMotionListener(mFrameMouseListener)
        contentPane.addMouseListener(mFrameMouseListener)

        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                exit()
            }
        })

        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(object : KeyEventDispatcher {
            override fun dispatchKeyEvent(p0: KeyEvent?): Boolean {
                if (p0?.keyCode == KeyEvent.VK_PAGE_DOWN && (p0.modifiers and KeyEvent.CTRL_MASK) != 0) {
                    mFilteredLogPanel.goToLast()
                    mFullLogPanel.goToLast()
                } else if (p0?.keyCode == KeyEvent.VK_PAGE_UP && (p0.modifiers and KeyEvent.CTRL_MASK) != 0) {
                    mFilteredLogPanel.goToFirst()
                    mFullLogPanel.goToFirst()
//                } else if (p0?.keyCode == KeyEvent.VK_N && (p0.modifiers and KeyEvent.CTRL_MASK) != 0) {

                } else if (p0?.keyCode == KeyEvent.VK_L && (p0.modifiers and KeyEvent.CTRL_MASK) != 0) {
                    mDeviceCombo.requestFocus()
                } else if (p0?.keyCode == KeyEvent.VK_R && (p0.modifiers and KeyEvent.CTRL_MASK) != 0) {
                    reconnectAdb()
                } else if (p0?.keyCode == KeyEvent.VK_G && (p0.modifiers and KeyEvent.CTRL_MASK) != 0) {
                    val goToDialog = GoToDialog(this@MainUI)
                    goToDialog.setLocationRelativeTo(this@MainUI)
                    goToDialog.setVisible(true)
                    if (goToDialog.line != -1) {
                        goToLine(goToDialog.line)
                    } else {
                        println("Cancel Goto Line")
                    }
                }

                return false
            }
        })

        mFilterPanel = JPanel()
        mFilterLeftPanel = JPanel()

        mLogToolBar = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0))
        mLogToolBar.border = BorderFactory.createEmptyBorder(3, 3, 3, 3)
        mLogToolBar.addMouseListener(mMouseHandler)

//        mLogToolBar = JPanel()
//        mLogToolBar.background = Color(0xE5, 0xE5, 0xE5)
        mStartBtn = ColorButton(Strings.START)
        mStartBtn.toolTipText = TooltipStrings.START_BTN
        mStartBtn.addActionListener(mActionHandler)
        mStartBtn.addMouseListener(mMouseHandler)
        mStopBtn = ColorButton(Strings.STOP)
        mStopBtn.toolTipText = TooltipStrings.STOP_BTN
        mStopBtn.addActionListener(mActionHandler)
        mStopBtn.addMouseListener(mMouseHandler)
        mPauseBtn = ColorButton(Strings.PAUSE)
        mPauseBtn.addActionListener(mActionHandler)
        mPauseBtn.addMouseListener(mMouseHandler)
        mClearBtn = ColorButton(Strings.CLEAR)
        mClearBtn.toolTipText = TooltipStrings.CLEAR_BTN
        mClearBtn.addActionListener(mActionHandler)
        mClearBtn.addMouseListener(mMouseHandler)
        mClearSaveBtn = ColorButton(Strings.CLEAR_SAVE)
        mClearSaveBtn.toolTipText = TooltipStrings.CLEAR_SAVE_BTN
        mClearSaveBtn.addActionListener(mActionHandler)
        mClearSaveBtn.addMouseListener(mMouseHandler)
        mRotationBtn = ColorButton(Strings.ROTATION)
        mRotationBtn.toolTipText = TooltipStrings.ROTATION_BTN
        mRotationBtn.addActionListener(mActionHandler)
        mRotationBtn.addMouseListener(mMouseHandler)
        mFiltersBtn = ColorButton(Strings.FILTERS)
        mFiltersBtn.toolTipText = TooltipStrings.FILTER_LIST_BTN
        mFiltersBtn.addActionListener(mActionHandler)
        mFiltersBtn.addMouseListener(mMouseHandler)

        mLogPanel = JPanel()
        mShowLogPanel = JPanel()
        mShowLogCombo = ColorComboBox<String>()
        mShowLogCombo.toolTipText = TooltipStrings.LOG_COMBO
        mShowLogCombo.isEditable = true
        mShowLogCombo.renderer = ColorComboBox.ComboBoxRenderer()
        mShowLogCombo.editor.editorComponent.addKeyListener(mKeyHandler)
        mShowLogCombo.addItemListener(mItemHandler)
        mShowLogCombo.addPopupMenuListener(mPopupMenuHandler)
        mShowLogCombo.editor.editorComponent.addMouseListener(mMouseHandler)
        mShowLogToggle = ColorToggleButton(Strings.LOG)
        mShowLogToggle.toolTipText = TooltipStrings.LOG_TOGGLE
        mShowLogToggle.margin = Insets(0, 0, 0, 0)
        mShowLogTogglePanel = JPanel(GridLayout(1, 1))
        mShowLogTogglePanel.add(mShowLogToggle)
        mShowLogTogglePanel.border = BorderFactory.createEmptyBorder(3,3,3,3)
        mShowLogToggle.addItemListener(mItemHandler)

        mBoldLogPanel = JPanel()
        mBoldLogCombo = ColorComboBox<String>()
        mBoldLogCombo.toolTipText = TooltipStrings.BOLD_COMBO
        mBoldLogCombo.isEditable = true
        mBoldLogCombo.renderer = ColorComboBox.ComboBoxRenderer()
        mBoldLogCombo.editor.editorComponent.addKeyListener(mKeyHandler)
        mBoldLogCombo.addItemListener(mItemHandler)
        mBoldLogCombo.editor.editorComponent.addMouseListener(mMouseHandler)
        mBoldLogToggle = ColorToggleButton(Strings.BOLD)
        mBoldLogToggle.toolTipText = TooltipStrings.BOLD_TOGGLE
        mBoldLogToggle.margin = Insets(0, 0, 0, 0)
        mBoldLogTogglePanel = JPanel(GridLayout(1, 1))
        mBoldLogTogglePanel.add(mBoldLogToggle)
        mBoldLogTogglePanel.border = BorderFactory.createEmptyBorder(3,3,3,3)
        mBoldLogToggle.addItemListener(mItemHandler)

        mShowTagPanel = JPanel()
        mShowTagCombo = ColorComboBox<String>()
        mShowTagCombo.toolTipText = TooltipStrings.TAG_COMBO
        mShowTagCombo.isEditable = true
        mShowTagCombo.renderer = ColorComboBox.ComboBoxRenderer()
        mShowTagCombo.editor.editorComponent.addKeyListener(mKeyHandler)
        mShowTagCombo.addItemListener(mItemHandler)
        mShowTagCombo.editor.editorComponent.addMouseListener(mMouseHandler)
        mShowTagToggle = ColorToggleButton(Strings.TAG)
        mShowTagToggle.toolTipText = TooltipStrings.TAG_TOGGLE
        mShowTagToggle.margin = Insets(0, 0, 0, 0)
        mShowTagTogglePanel = JPanel(GridLayout(1, 1))
        mShowTagTogglePanel.add(mShowTagToggle)
        mShowTagTogglePanel.border = BorderFactory.createEmptyBorder(3,3,3,3)
        mShowTagToggle.addItemListener(mItemHandler)

        mShowPidPanel = JPanel()
        mShowPidCombo = ColorComboBox<String>()
        mShowPidCombo.toolTipText = TooltipStrings.PID_COMBO
        mShowPidCombo.isEditable = true
        mShowPidCombo.renderer = ColorComboBox.ComboBoxRenderer()
        mShowPidCombo.editor.editorComponent.addKeyListener(mKeyHandler)
        mShowPidCombo.addItemListener(mItemHandler)
        mShowPidCombo.editor.editorComponent.addMouseListener(mMouseHandler)
        mShowPidToggle = ColorToggleButton(Strings.PID)
        mShowPidToggle.toolTipText = TooltipStrings.PID_TOGGLE
        mShowPidToggle.margin = Insets(0, 0, 0, 0)
        mShowPidTogglePanel = JPanel(GridLayout(1, 1))
        mShowPidTogglePanel.add(mShowPidToggle)
        mShowPidTogglePanel.border = BorderFactory.createEmptyBorder(3,3,3,3)
        mShowPidToggle.addItemListener(mItemHandler)

        mShowTidPanel = JPanel()
        mShowTidCombo = ColorComboBox<String>()
        mShowTidCombo.toolTipText = TooltipStrings.TID_COMBO
        mShowTidCombo.isEditable = true
        mShowTidCombo.renderer = ColorComboBox.ComboBoxRenderer()
        mShowTidCombo.editor.editorComponent.addKeyListener(mKeyHandler)
        mShowTidCombo.addItemListener(mItemHandler)
        mShowTidCombo.editor.editorComponent.addMouseListener(mMouseHandler)
        mShowTidToggle = ColorToggleButton(Strings.TID)
        mShowTidToggle.toolTipText = TooltipStrings.TID_TOGGLE
        mShowTidToggle.margin = Insets(0, 0, 0, 0)
        mShowTidTogglePanel = JPanel(GridLayout(1, 1))
        mShowTidTogglePanel.add(mShowTidToggle)
        mShowTidTogglePanel.border = BorderFactory.createEmptyBorder(3,3,3,3)
        mShowTidToggle.addItemListener(mItemHandler)

        mDeviceStatus = JLabel("None", JLabel.LEFT)
        mDeviceStatus.isEnabled = false
        mDeviceCombo = ColorComboBox<String>()
        mDeviceCombo.toolTipText = TooltipStrings.DEVICES_COMBO
        mDeviceCombo.isEditable = true
        mDeviceCombo.renderer = ColorComboBox.ComboBoxRenderer()
        mDeviceCombo.editor.editorComponent.addKeyListener(mKeyHandler)
        mDeviceCombo.addItemListener(mItemHandler)
        mDeviceCombo.editor.editorComponent.addMouseListener(mMouseHandler)
        mAdbConnectBtn = ColorButton(Strings.CONNECT)
        mAdbConnectBtn.toolTipText = TooltipStrings.CONNECT_BTN
        mAdbConnectBtn.addActionListener(mActionHandler)
        mAdbRefreshBtn = ColorButton(Strings.REFRESH)
        mAdbRefreshBtn.addActionListener(mActionHandler)
        mAdbRefreshBtn.toolTipText = TooltipStrings.REFRESH_BTN
        mAdbDisconnectBtn = ColorButton(Strings.DISCONNECT)
        mAdbDisconnectBtn.addActionListener(mActionHandler)
        mAdbDisconnectBtn.toolTipText = TooltipStrings.DISCONNECT_BTN

        mMatchCaseBtn = ColorToggleButton("Aa")
        mMatchCaseBtn.toolTipText = TooltipStrings.CASE_TOGGLE
        mMatchCaseBtn.addItemListener(mItemHandler)

        mShowLogPanel.layout = BorderLayout()
        mShowLogPanel.add(mShowLogTogglePanel, BorderLayout.WEST)
        mShowLogCombo.border = BorderFactory.createEmptyBorder(3, 0, 3, 3)
        mShowLogPanel.add(mShowLogCombo, BorderLayout.CENTER)

        mBoldLogPanel.layout = BorderLayout()
        mBoldLogPanel.add(mBoldLogTogglePanel, BorderLayout.WEST)
        mBoldLogCombo.border = BorderFactory.createEmptyBorder(3, 0, 3, 3)
        mBoldLogCombo.preferredSize = Dimension(170, mBoldLogCombo.preferredSize.height)
        mBoldLogPanel.add(mBoldLogCombo, BorderLayout.CENTER)
//        mBoldPanel.add(mBoldLogPanel)

        mShowTagPanel.layout = BorderLayout()
        mShowTagPanel.add(mShowTagTogglePanel, BorderLayout.WEST)
        mShowTagCombo.border = BorderFactory.createEmptyBorder(3, 0, 3, 3)
        mShowTagCombo.preferredSize = Dimension(250, mShowTagCombo.preferredSize.height)
        mShowTagPanel.add(mShowTagCombo, BorderLayout.CENTER)
//        mTagPanel.add(mShowTagPanel)

        mShowPidPanel.layout = BorderLayout()
        mShowPidPanel.add(mShowPidTogglePanel, BorderLayout.WEST)
        mShowPidCombo.border = BorderFactory.createEmptyBorder(3, 0, 3, 3)
        mShowPidCombo.preferredSize = Dimension(120, mShowPidCombo.preferredSize.height)
        mShowPidPanel.add(mShowPidCombo, BorderLayout.CENTER)
//        mPidPanel.add(mShowPidPanel)

        mShowTidPanel.layout = BorderLayout()
        mShowTidPanel.add(mShowTidTogglePanel, BorderLayout.WEST)
        mShowTidCombo.border = BorderFactory.createEmptyBorder(3, 0, 3, 3)
        mShowTidCombo.preferredSize = Dimension(120, mShowTidCombo.preferredSize.height)
        mShowTidPanel.add(mShowTidCombo, BorderLayout.CENTER)
//        mTidPanel.add(mShowTidPanel)

        mDeviceCombo.preferredSize = Dimension(200, 30)
        mDeviceCombo.border = BorderFactory.createEmptyBorder(3, 0, 3, 5)
        mDeviceStatus.preferredSize = Dimension(100, 30)
        mDeviceStatus.border = BorderFactory.createEmptyBorder(3, 0, 3, 0)
        mDeviceStatus.horizontalAlignment = JLabel.CENTER

        mScrollbackApplyBtn = ColorButton(Strings.APPLY)
        mScrollbackApplyBtn.toolTipText = TooltipStrings.SCROLLBACK_APPLY_BTN
        mScrollbackApplyBtn.addActionListener(mActionHandler)
        mScrollbackKeepToggle = ColorToggleButton(Strings.KEEP)
        mScrollbackKeepToggle.toolTipText = TooltipStrings.SCROLLBACK_KEEP_TOGGLE
        mScrollbackKeepToggle.mSelectedBg = Color.RED
        mScrollbackKeepToggle.mSelectedFg = Color.BLACK
        mScrollbackKeepToggle.addItemListener(mItemHandler)

        mScrollbackLabel = JLabel(Strings.SCROLLBACK_LINES)

        mScrollbackTF = JTextField()
        mScrollbackTF.toolTipText = TooltipStrings.SCROLLBACK_TF
        mScrollbackTF.preferredSize = Dimension(80, 25)
        mScrollbackTF.addKeyListener(mKeyHandler)
        mScrollbackSplitFileCheck = JCheckBox(Strings.SPLIT_FILE, false)
        mScrollbackSplitFileCheck.toolTipText = TooltipStrings.SCROLLBACK_SPLIT_CHK

        val itemFilterPanel = JPanel(FlowLayout(FlowLayout.LEADING, 0, 0))
        itemFilterPanel.border = BorderFactory.createEmptyBorder(0, 0, 0, 0)
        itemFilterPanel.add(mShowTagPanel)
        itemFilterPanel.add(mShowPidPanel)
        itemFilterPanel.add(mShowTidPanel)
        itemFilterPanel.add(mBoldLogPanel)

        mLogPanel.layout = BorderLayout()
        mLogPanel.add(mShowLogPanel, BorderLayout.CENTER)
        mLogPanel.add(itemFilterPanel, BorderLayout.EAST)
        mLogPanel.preferredSize = Dimension(mLogPanel.preferredSize.width, 30)

        mFilterLeftPanel.layout = BorderLayout()
        mFilterLeftPanel.add(mLogPanel, BorderLayout.NORTH)
        mFilterLeftPanel.border = BorderFactory.createEmptyBorder(3, 3, 3, 3)

        mFilterPanel.layout = BorderLayout()
        mFilterPanel.add(mFilterLeftPanel, BorderLayout.CENTER)
        mFilterPanel.addMouseListener(mMouseHandler)

        mLogToolBar.add(mStartBtn)
        mLogToolBar.add(mStopBtn)
        mLogToolBar.add(mClearBtn)
        mLogToolBar.add(mClearSaveBtn)

        addVSeparator(mLogToolBar)

        mLogToolBar.add(mDeviceCombo)
        mLogToolBar.add(mDeviceStatus)
        mLogToolBar.add(mAdbConnectBtn)
        mLogToolBar.add(mAdbRefreshBtn)
        mLogToolBar.add(mAdbDisconnectBtn)

        addVSeparator(mLogToolBar)

        mLogToolBar.add(mScrollbackLabel)
        mLogToolBar.add(mScrollbackTF)
        mLogToolBar.add(mScrollbackSplitFileCheck)
        mLogToolBar.add(mScrollbackApplyBtn)
        mLogToolBar.add(mScrollbackKeepToggle)

        addVSeparator(mLogToolBar)
        mLogToolBar.add(mRotationBtn)

        addVSeparator(mLogToolBar)
        mLogToolBar.add(mMatchCaseBtn)
        mLogToolBar.add(mFiltersBtn)

        val toolBarPanel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0))
        toolBarPanel.addMouseListener(mMouseHandler)
        toolBarPanel.add(mLogToolBar)

        mFilterPanel.add(toolBarPanel, BorderLayout.NORTH)

        layout = BorderLayout()

        mFullTableModel = LogTableModel(null)

        mFilteredTableModel = LogTableModel(mFullTableModel)
        mFilteredTableModel.mMainUI = this@MainUI

        mFullLogPanel = LogPanel(mFullTableModel, null)
        mFilteredLogPanel = LogPanel(mFilteredTableModel, mFullLogPanel)

        when (mRotationStatus) {
            ROTATION_LEFT_RIGHT -> {
                mLogSplitPane = JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false, mFilteredLogPanel, mFullLogPanel)
                mLogSplitPane.resizeWeight = SPLIT_WEIGHT
            }
            ROTATION_RIGHT_LEFT -> {
                mLogSplitPane = JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false, mFullLogPanel, mFilteredLogPanel)
                mLogSplitPane.resizeWeight = 1 - SPLIT_WEIGHT
            }
            ROTATION_TOP_BOTTOM -> {
                mLogSplitPane = JSplitPane(JSplitPane.VERTICAL_SPLIT, false, mFilteredLogPanel, mFullLogPanel)
                mLogSplitPane.resizeWeight = SPLIT_WEIGHT
            }
            ROTATION_BOTTOM_TOP -> {
                mLogSplitPane = JSplitPane(JSplitPane.VERTICAL_SPLIT, false, mFullLogPanel, mFilteredLogPanel)
                mLogSplitPane.resizeWeight = 1 - SPLIT_WEIGHT
            }
        }
        mLogSplitPane.dividerSize = 2
        mLogSplitPane.isOneTouchExpandable = false

        mStatusBar = JPanel(BorderLayout())
        mStatusBar.border = BorderFactory.createEmptyBorder(3, 3, 3, 3)
        mStatusTF = StatusTextField(Strings.NONE)
        mStatusTF.toolTipText = TooltipStrings.SAVED_FILE_TF
        mStatusTF.isEditable = false
        mStatusTF.border = BorderFactory.createEmptyBorder()
        mStatusBar.add(mStatusTF)

        val logLevel = mConfigManager.mProperties.get(mConfigManager.ITEM_LOG_LEVEL) as String
        for (item in mLogLevelGroup.elements) {
            if (logLevel == item.text) {
                item.isSelected = true
                break
            }
        }

        var item: String?
        for (i in 0 until mConfigManager.COUNT_SHOW_LOG) {
            item = mConfigManager.mProperties.get(mConfigManager.ITEM_SHOW_LOG + i) as? String
            if (item == null) {
                break
            }

            if (!mShowLogCombo.isExistItem(item)) {
                mShowLogCombo.addItem(item)
            }
        }
        if (mShowLogCombo.itemCount > 0) {
            mShowLogCombo.selectedIndex = 0
        }

        var check = mConfigManager.mProperties.get(mConfigManager.ITEM_SHOW_LOG_CHECK) as? String
        if (!check.isNullOrEmpty()) {
            mShowLogToggle.isSelected = check.toBoolean()
        } else {
            mShowLogToggle.isSelected = true
        }
        mShowLogCombo.isEnabled = mShowLogToggle.isSelected 
        
        for (i in 0 until mConfigManager.COUNT_SHOW_TAG) {
            item = mConfigManager.mProperties.get(mConfigManager.ITEM_SHOW_TAG + i) as? String
            if (item == null) {
                break
            }
            mShowTagCombo.insertItemAt(item, i)
            if (i == 0) {
                mShowTagCombo.selectedIndex = 0
            }
        }
        check = mConfigManager.mProperties.get(mConfigManager.ITEM_SHOW_TAG_CHECK) as? String
        if (!check.isNullOrEmpty()) {
            mShowTagToggle.isSelected = check.toBoolean()
        } else {
            mShowTagToggle.isSelected = true
        }
        mShowTagCombo.setEnabledFilter(mShowTagToggle.isSelected)

        check = mConfigManager.mProperties.get(mConfigManager.ITEM_SHOW_PID_CHECK) as? String
        if (!check.isNullOrEmpty()) {
            mShowPidToggle.isSelected = check.toBoolean()
        } else {
            mShowPidToggle.isSelected = true
        }
        mShowPidCombo.setEnabledFilter(mShowPidToggle.isSelected)

        check = mConfigManager.mProperties.get(mConfigManager.ITEM_SHOW_TID_CHECK) as? String
        if (!check.isNullOrEmpty()) {
            mShowTidToggle.isSelected = check.toBoolean()
        } else {
            mShowTidToggle.isSelected = true
        }
        mShowTidCombo.setEnabledFilter(mShowTidToggle.isSelected)

        for (i in 0 until mConfigManager.COUNT_HIGHLIGHT_LOG) {
            item = mConfigManager.mProperties.get(mConfigManager.ITEM_HIGHLIGHT_LOG + i) as? String
            if (item == null) {
                break
            }
            mBoldLogCombo.insertItemAt(item, i)
            if (i == 0) {
                mBoldLogCombo.selectedIndex = 0
            }
        }
        check = mConfigManager.mProperties.get(mConfigManager.ITEM_HIGHLIGHT_LOG_CHECK) as? String
        if (!check.isNullOrEmpty()) {
            mBoldLogToggle.isSelected = check.toBoolean()
        } else {
            mBoldLogToggle.isSelected = true
        }
        mBoldLogCombo.setEnabledFilter(mBoldLogToggle.isSelected)

        val targetDevice = mConfigManager.mProperties.get(mConfigManager.ITEM_ADB_DEVICE) as? String
        mDeviceCombo.insertItemAt(targetDevice, 0)
        mDeviceCombo.selectedIndex = 0

        if (mAdbManager.mDevices.contains(targetDevice)) {
            mDeviceStatus.text = Strings.CONNECTED
        } else {
            mDeviceStatus.text = Strings.NOT_CONNECTED
        }

        var fontName = mConfigManager.mProperties.get(mConfigManager.ITEM_FONT_NAME) as? String
        if (fontName.isNullOrEmpty()) {
            fontName = "Dialog"
        }

        var fontSize = 12
        check = mConfigManager.mProperties.get(mConfigManager.ITEM_FONT_SIZE) as? String
        if (!check.isNullOrEmpty()) {
            fontSize = check.toInt()
        }

        mFont = Font(fontName, Font.PLAIN, fontSize)
        mFilteredLogPanel.mFont = mFont
        mFullLogPanel.mFont = mFont

        var divider = mConfigManager.mProperties.get(mConfigManager.ITEM_LAST_DIVIDER_LOCATION) as? String
        if (!divider.isNullOrEmpty()) {
            mLogSplitPane.lastDividerLocation = divider.toInt()
        }

        divider = mConfigManager.mProperties.get(mConfigManager.ITEM_DIVIDER_LOCATION) as? String
        if (!divider.isNullOrEmpty() && mLogSplitPane.lastDividerLocation != -1) {
            mLogSplitPane.dividerLocation = divider.toInt()
        }

        when (logLevel) {
            VERBOSE->mFilteredTableModel.mFilterLevel = mFilteredTableModel.LEVEL_VERBOSE
            DEBUG->mFilteredTableModel.mFilterLevel = mFilteredTableModel.LEVEL_DEBUG
            INFO->mFilteredTableModel.mFilterLevel = mFilteredTableModel.LEVEL_INFO
            WARNING->mFilteredTableModel.mFilterLevel = mFilteredTableModel.LEVEL_WARNING
            ERROR->mFilteredTableModel.mFilterLevel = mFilteredTableModel.LEVEL_ERROR
            FATAL->mFilteredTableModel.mFilterLevel = mFilteredTableModel.LEVEL_FATAL
        }

        if (mShowLogToggle.isSelected && mShowLogCombo.selectedItem != null) {
            mFilteredTableModel.mFilterLog = mShowLogCombo.selectedItem!!.toString()
        } else {
            mFilteredTableModel.mFilterLog = ""
        }
        if (mBoldLogToggle.isSelected && mBoldLogCombo.selectedItem != null) {
            mFilteredTableModel.mFilterHighlightLog = mBoldLogCombo.selectedItem!!.toString()
        } else {
            mFilteredTableModel.mFilterHighlightLog = ""
        }
        if (mShowTagToggle.isSelected && mShowTagCombo.selectedItem != null) {
            mFilteredTableModel.mFilterTag = mShowTagCombo.selectedItem!!.toString()
        } else {
            mFilteredTableModel.mFilterTag = ""
        }
        if (mShowPidToggle.isSelected && mShowPidCombo.selectedItem != null) {
            mFilteredTableModel.mFilterPid = mShowPidCombo.selectedItem!!.toString()
        } else {
            mFilteredTableModel.mFilterPid = ""
        }
        if (mShowTidToggle.isSelected && mShowTidCombo.selectedItem != null) {
            mFilteredTableModel.mFilterTid = mShowTidCombo.selectedItem!!.toString()
        } else {
            mFilteredTableModel.mFilterTid = ""
        }

        check = mConfigManager.mProperties.get(mConfigManager.ITEM_VIEW_FULL) as? String
        if (!check.isNullOrEmpty()) {
            mItemFull.state = check.toBoolean()
        } else {
            mItemFull.state = true
        }
        if (mItemFull.state == false) {
            windowedModeLogPanel(mFullLogPanel)
        }

        check = mConfigManager.mProperties.get(mConfigManager.ITEM_FILTER_INCREMENTAL) as? String
        if (!check.isNullOrEmpty()) {
            mItemFilterIncremental.state = check.toBoolean()
        } else {
            mItemFilterIncremental.state = false
        }

        check = mConfigManager.mProperties.get(mConfigManager.ITEM_SCROLLBACK) as? String
        if (!check.isNullOrEmpty()) {
            mScrollbackTF.text = check
        } else {
            mScrollbackTF.text = "0"
        }
        mFilteredTableModel.mScrollback = mScrollbackTF.text.toInt()

        check = mConfigManager.mProperties.get(mConfigManager.ITEM_SCROLLBACK_SPLIT_FILE) as? String
        if (!check.isNullOrEmpty()) {
            mScrollbackSplitFileCheck.isSelected = check.toBoolean()
        } else {
            mScrollbackSplitFileCheck.isSelected = false
        }
        mFilteredTableModel.mScrollbackSplitFile = mScrollbackSplitFileCheck.isSelected

        check = mConfigManager.mProperties.get(mConfigManager.ITEM_MATCH_CASE) as? String
        if (!check.isNullOrEmpty()) {
            mMatchCaseBtn.isSelected = check.toBoolean()
        } else {
            mMatchCaseBtn.isSelected = false
        }
        mFilteredTableModel.mMatchCase = mMatchCaseBtn.isSelected

        add(mFilterPanel, BorderLayout.NORTH)
        add(mLogSplitPane, BorderLayout.CENTER)
        add(mStatusBar, BorderLayout.SOUTH)

        mIsCreatingUI = false
    }

    fun addVSeparator(panel:JPanel) {
        val separator1 = JSeparator(SwingConstants.VERTICAL)
        separator1.preferredSize = Dimension(separator1.preferredSize.width, 20)
        separator1.foreground = Color.DARK_GRAY
        separator1.background = Color.DARK_GRAY
        val separator2 = JSeparator(SwingConstants.VERTICAL)
        separator2.preferredSize = Dimension(separator2.preferredSize.width, 20)
        separator2.background = Color.DARK_GRAY
        separator2.foreground = Color.DARK_GRAY
        panel.add(Box.createHorizontalStrut(5))
        panel.add(separator1)
        panel.add(separator2)
        panel.add(Box.createHorizontalStrut(5))
    }

    inner class ConfigManager {
        val CONFIG_FILE = "lognote.xml"
        var mProperties = Properties()
        val ITEM_FRAME_X = "FRAME_X"
        val ITEM_FRAME_Y = "FRAME_Y"
        val ITEM_FRAME_WIDTH = "FRAME_WIDTH"
        val ITEM_FRAME_HEIGHT = "FRAME_HEIGHT"
        val ITEM_FRAME_EXTENDED_STATE = "FRAME_EXTENDED_STATE"
        val ITEM_ROTATION = "ROTATION"
        val ITEM_DIVIDER_LOCATION = "DIVIDER_LOCATION"
        val ITEM_LAST_DIVIDER_LOCATION = "LAST_DIVIDER_LOCATION"

        val ITEM_LANG = "LANG"

        val ITEM_SHOW_LOG = "SHOW_LOG_"
        val COUNT_SHOW_LOG = 20
        val ITEM_SHOW_TAG = "SHOW_TAG_"
        val COUNT_SHOW_TAG = 10

        val ITEM_HIGHLIGHT_LOG = "HIGHLIGHT_LOG_"
        val COUNT_HIGHLIGHT_LOG = 10

        val ITEM_SHOW_LOG_CHECK = "SHOW_LOG_CHECK"
        val ITEM_SHOW_TAG_CHECK = "SHOW_TAG_CHECK"
        val ITEM_SHOW_PID_CHECK = "SHOW_PID_CHECK"
        val ITEM_SHOW_TID_CHECK = "SHOW_TID_CHECK"

        val ITEM_HIGHLIGHT_LOG_CHECK = "HIGHLIGHT_LOG_CHECK"

        val ITEM_LOG_LEVEL = "LOG_LEVEL"

        val ITEM_ADB_DEVICE = "ADB_DEVICE"
        val ITEM_ADB_CMD = "ADB_CMD"
        val ITEM_ADB_LOG_SAVE_PATH = "ADB_LOG_SAVE_PATH"
        val ITEM_ADB_PREFIX = "ADB_PREFIX"

        val ITEM_FONT_NAME = "FONT_NAME"
        val ITEM_FONT_SIZE = "FONT_SIZE"
        val ITEM_VIEW_FULL = "VIEW_FULL"
        val ITEM_FILTER_INCREMENTAL = "FILTER_INCREMENTAL"

        val ITEM_SCROLLBACK = "SCROLLBACK"
        val ITEM_SCROLLBACK_SPLIT_FILE = "SCROLLBACK_SPLIT_FILE"
        val ITEM_MATCH_CASE = "MATCH_CASE"

        val ITEM_FILTERS_TITLE = "FILTERS_TITLE_"
        val ITEM_FILTERS_FILTER = "FILTERS_FILTER_"

        val ITEM_COLOR_MANAGER = "COLOR_MANAGER_"

        private fun setDefaultConfig() {
            mProperties.put(ITEM_LOG_LEVEL, VERBOSE)
            mProperties.put(ITEM_SHOW_LOG_CHECK, "true")
            mProperties.put(ITEM_SHOW_TAG_CHECK, "true")
            mProperties.put(ITEM_SHOW_PID_CHECK, "true")
            mProperties.put(ITEM_SHOW_TID_CHECK, "true")
            mProperties.put(ITEM_HIGHLIGHT_LOG_CHECK, "true")
        }

        fun loadConfig() {
            var fileInput: FileInputStream? = null

            try {
                fileInput = FileInputStream(CONFIG_FILE)
                mProperties.loadFromXML(fileInput)
            } catch (ex: Exception) {
                ex.printStackTrace()
                setDefaultConfig()
            } finally {
                if (null != fileInput) {
                    try {
                        fileInput.close()
                    } catch (ex: IOException) {
                    }
                }
            }
            mColorManager.getConfig(this)
            mColorManager.applyColor()
        }

        fun saveConfig() {
            var fileOutput: FileOutputStream? = null

            val location = getLocation()
            try {
                mProperties.put(ITEM_FRAME_X, location.x.toString())
            } catch (e: NullPointerException) {
                mProperties.put(ITEM_FRAME_X, "0")
            }

            try {
                mProperties.put(ITEM_FRAME_Y, location.y.toString())
            } catch (e: NullPointerException) {
                mProperties.put(ITEM_FRAME_Y, "0")
            }

            val size = getSize()
            try {
                mProperties.put(ITEM_FRAME_WIDTH, size.width.toString())
            } catch (e: NullPointerException) {
                mProperties.put(ITEM_FRAME_WIDTH, "1280")
            }

            try {
                mProperties.put(ITEM_FRAME_HEIGHT, size.height.toString())
            } catch (e: NullPointerException) {
                mProperties.put(ITEM_FRAME_HEIGHT, "720")
            }

            mProperties.put(ITEM_FRAME_EXTENDED_STATE, extendedState.toString())

            try {
                for (item in mLogLevelGroup.elements) {
                    if (item.isSelected) {
                        mProperties.put(ITEM_LOG_LEVEL, item.text)
                        break
                    }
                }
            } catch (e: NullPointerException) {
                mProperties.put(ITEM_LOG_LEVEL, VERBOSE)
            }

            var nCount = mShowLogCombo.itemCount
            if (nCount > COUNT_SHOW_LOG) {
                nCount = COUNT_SHOW_LOG
            }
            for (i in 0 until nCount) {
                mProperties.put(ITEM_SHOW_LOG + i, mShowLogCombo.getItemAt(i).toString())
            }

            for (i in nCount until COUNT_SHOW_LOG) {
                mProperties.remove(ITEM_SHOW_LOG + i)
            }
            mProperties.put(ITEM_SHOW_LOG_CHECK, mShowLogToggle.isSelected.toString())
            nCount = mShowTagCombo.itemCount
            if (nCount > COUNT_SHOW_TAG) {
                nCount = COUNT_SHOW_TAG
            }
            for (i in 0 until nCount) {
                mProperties.put(ITEM_SHOW_TAG + i, mShowTagCombo.getItemAt(i).toString())
            }
            for (i in nCount until COUNT_SHOW_TAG) {
                mProperties.remove(ITEM_SHOW_TAG + i)
            }
            mProperties.put(ITEM_SHOW_TAG_CHECK, mShowTagToggle.isSelected.toString())

            mProperties.put(ITEM_SHOW_PID_CHECK, mShowPidToggle.isSelected.toString())
            mProperties.put(ITEM_SHOW_TID_CHECK, mShowTidToggle.isSelected.toString())

            nCount = mBoldLogCombo.itemCount
            if (nCount > COUNT_HIGHLIGHT_LOG) {
                nCount = COUNT_HIGHLIGHT_LOG
            }
            for (i in 0 until nCount) {
                mProperties.put(ITEM_HIGHLIGHT_LOG + i, mBoldLogCombo.getItemAt(i).toString())
            }
            for (i in nCount until COUNT_HIGHLIGHT_LOG) {
                mProperties.remove(ITEM_HIGHLIGHT_LOG + i)
            }
            mProperties.put(ITEM_HIGHLIGHT_LOG_CHECK, mBoldLogToggle.isSelected.toString())
            try {
                mProperties.put(ITEM_ADB_DEVICE, mDeviceCombo.getItemAt(0).toString())
            } catch (e: NullPointerException) {
                mProperties.put(ITEM_ADB_DEVICE, "0.0.0.0")
            }

            mProperties.put(ITEM_ADB_CMD, mAdbManager.mAdbCmd)
            mProperties.put(ITEM_ADB_LOG_SAVE_PATH, mAdbManager.mLogSavePath)
            mProperties.put(ITEM_ADB_PREFIX, mAdbManager.mPrefix)

            mProperties.put(ITEM_ROTATION, mRotationStatus.toString())
            mProperties.put(ITEM_DIVIDER_LOCATION, mLogSplitPane.dividerLocation.toString())
            if (mLogSplitPane.lastDividerLocation != -1) {
                mProperties.put(ITEM_LAST_DIVIDER_LOCATION, mLogSplitPane.lastDividerLocation.toString())
            }

            mProperties.put(ITEM_LANG, Strings.lang.toString())

            mProperties.put(ITEM_FONT_NAME, mFont.family)
            mProperties.put(ITEM_FONT_SIZE, mFont.size.toString())

            mProperties.put(ITEM_VIEW_FULL, mItemFull.state.toString())
            mProperties.put(ITEM_FILTER_INCREMENTAL, mItemFilterIncremental.state.toString())

            mProperties.put(ITEM_SCROLLBACK, mScrollbackTF.text)
            mProperties.put(ITEM_SCROLLBACK_SPLIT_FILE, mScrollbackSplitFileCheck.isSelected.toString())
            mProperties.put(ITEM_MATCH_CASE, mMatchCaseBtn.isSelected.toString())

            mColorManager.putConfig(this)

            try {
                fileOutput = FileOutputStream(CONFIG_FILE)
                mProperties.storeToXML(fileOutput, "")
            } finally {
                if (null != fileOutput) {
                    try {
                        fileOutput.close()
                    } catch (ex: IOException) {
                    }
                }
            }
        }

        fun loadFilters() : ArrayList<FiltersManager.FilterElement> {
            val filters = ArrayList<FiltersManager.FilterElement>()

            var title: String?
            var filter: String?
            for (i in 0 until FiltersManager.MAX_FILTERS) {
                title = mProperties.get(ITEM_FILTERS_TITLE + i) as? String
                if (title == null) {
                    break
                }
                filter = mProperties.get(ITEM_FILTERS_FILTER + i) as? String
                if (filter == null) {
                    filter = "null"
                }
                filters.add(FiltersManager.FilterElement(title, filter))
            }

            return filters
        }

        fun saveFilters(filters : ArrayList<FiltersManager.FilterElement>) {
            var nCount = filters.size
            if (nCount > FiltersManager.MAX_FILTERS) {
                nCount = FiltersManager.MAX_FILTERS
            }
            for (i in 0 until nCount) {
                mProperties.put(ITEM_FILTERS_TITLE + i, filters[i].mTitle)
                mProperties.put(ITEM_FILTERS_FILTER + i, filters[i].mFilter)
            }

            saveConfig()
            return
        }
    }

    fun windowedModeLogPanel(logPanel: LogPanel) {
        if (logPanel.parent == mLogSplitPane) {
            logPanel.mIsWindowedMode = true
            mRotationBtn.isEnabled = false
            mLogSplitPane.remove(logPanel)
            if (mItemFull.state) {
                val logTableDialog = LogTableDialog(this@MainUI, logPanel)
                logTableDialog.isVisible = true
            }
        }
    }

    fun attachLogPanel(logPanel: LogPanel) {
        if (logPanel.parent != mLogSplitPane) {
            logPanel.mIsWindowedMode = false
            mRotationBtn.isEnabled = true
            mLogSplitPane.remove(mFilteredLogPanel)
            mLogSplitPane.remove(mFullLogPanel)
            when (mRotationStatus) {
                ROTATION_LEFT_RIGHT -> {
                    mLogSplitPane.orientation = JSplitPane.HORIZONTAL_SPLIT
                    mLogSplitPane.add(mFilteredLogPanel)
                    mLogSplitPane.add(mFullLogPanel)
                    mLogSplitPane.resizeWeight = SPLIT_WEIGHT
                }
                ROTATION_RIGHT_LEFT -> {
                    mLogSplitPane.orientation = JSplitPane.HORIZONTAL_SPLIT
                    mLogSplitPane.add(mFullLogPanel)
                    mLogSplitPane.add(mFilteredLogPanel)
                    mLogSplitPane.resizeWeight = 1 - SPLIT_WEIGHT
                }
                ROTATION_TOP_BOTTOM -> {
                    mLogSplitPane.orientation = JSplitPane.VERTICAL_SPLIT
                    mLogSplitPane.add(mFilteredLogPanel)
                    mLogSplitPane.add(mFullLogPanel)
                    mLogSplitPane.resizeWeight = SPLIT_WEIGHT
                }
                ROTATION_BOTTOM_TOP -> {
                    mLogSplitPane.orientation = JSplitPane.VERTICAL_SPLIT
                    mLogSplitPane.add(mFullLogPanel)
                    mLogSplitPane.add(mFilteredLogPanel)
                    mLogSplitPane.resizeWeight = 1 - SPLIT_WEIGHT
                }
            }
        }
    }

    fun openFile(path: String, isAppend: Boolean) {
        println("Opening: $path, $isAppend")
        mFilteredTableModel.stopScan()

        if (isAppend) {
            mStatusTF.text += "| $path"
        } else {
            mStatusTF.text = path
        }
        mFullTableModel.setLogFile(path)
        mFullTableModel.loadItems(isAppend)
        mFilteredTableModel.loadItems(isAppend)
        repaint()

        return
    }

    fun setSaveLogFile() {
        val dtf = DateTimeFormatter.ofPattern("yyyyMMdd_HH.mm.ss")
        var prefix = mDeviceCombo.selectedItem!!.toString()
        prefix = prefix.substringBefore(":")
        if (mAdbManager.mPrefix.length > 0) {
            prefix = mAdbManager.mPrefix
        }

        var filePath = mAdbManager.mLogSavePath + "/" + prefix + "_" + dtf.format(LocalDateTime.now()) + ".txt"
        var file = File(filePath)
        var idx = 1;
        while (file.isFile) {
            filePath = mAdbManager.mLogSavePath + "/" + prefix + "_" + dtf.format(LocalDateTime.now()) + "-" + idx + ".txt"
            file = File(filePath)
            idx++
        }

        mFilteredTableModel.setLogFile(filePath)
        mStatusTF.text = filePath
    }

    fun startAdbScan(reconnect: Boolean) {
        mFilteredTableModel.stopScan()
        setSaveLogFile()
        if (reconnect) {
            mAdbManager.mTargetDevice = mDeviceCombo.selectedItem!!.toString()
            mAdbManager.startLogcat()
        }
        mFilteredTableModel.startScan()
    }
    internal inner class ActionHandler() : ActionListener {
        override fun actionPerformed(p0: ActionEvent?) {
            if (p0?.source == mItemFileOpen) {
                val fileDialog = FileDialog(this@MainUI, Strings.FILE + " " + Strings.OPEN, FileDialog.LOAD)
                fileDialog.isMultipleMode = false
                fileDialog.directory = mFullTableModel.mLogFile?.parent
                fileDialog.setVisible(true)
                if (fileDialog.getFile() != null) {
                    val file = File(fileDialog.getDirectory() + fileDialog.getFile())
                    openFile(file.absolutePath, false)
                } else {
                    System.out.println("Cancel Open")
                }
            } else if (p0?.source == mItemFileOpenFiles) {
                val fileDialog = FileDialog(this@MainUI, Strings.FILE + " " + Strings.OPEN_FILES, FileDialog.LOAD)
                fileDialog.isMultipleMode = true
                fileDialog.directory = mFullTableModel.mLogFile?.parent
                fileDialog.setVisible(true)
                val fileList = fileDialog.getFiles()
                if (fileList != null) {
                    var isFirst = true
                    for (file in fileList) {
                        if (isFirst) {
                            openFile(file.absolutePath, false)
                            isFirst = false
                        } else {
                            openFile(file.absolutePath, true)
                        }
                    }
                } else {
                    System.out.println("Cancel Open")
                }
            } else if (p0?.source == mItemFileAppendFiles) {
                val fileDialog = FileDialog(this@MainUI, Strings.FILE + " " + Strings.APPEND_FILES, FileDialog.LOAD)
                fileDialog.isMultipleMode = true
                fileDialog.directory = mFullTableModel.mLogFile?.parent
                fileDialog.setVisible(true)
                val fileList = fileDialog.getFiles()
                if (fileList != null) {
                    for (file in fileList) {
                        openFile(file.absolutePath, true)
                    }
                } else {
                    System.out.println("Cancel Open")
                }
            } else if (p0?.source == mItemFileExit) {
                exit()
            } else if (p0?.source == mItemAdb || p0?.source == mItemLogFile) {
                val settingsDialog = AdbSettingsDialog(this@MainUI)
                settingsDialog.setLocationRelativeTo(this@MainUI)
                settingsDialog.setVisible(true)
            } else if (p0?.source == mItemFont) {
                val fontDialog = FontDialog(this@MainUI)
                fontDialog.setLocationRelativeTo(this@MainUI)
                fontDialog.setVisible(true)
            } else if (p0?.source == mItemFull) {
                if (mItemFull.state) {
                    attachLogPanel(mFullLogPanel)
                } else {
                    windowedModeLogPanel(mFullLogPanel)
                }
            } else if (p0?.source == mItemAbout) {
                val aboutDialog = AboutDialog(this@MainUI)
                aboutDialog.setLocationRelativeTo(this@MainUI)
                aboutDialog.setVisible(true)
            } else if (p0?.source == mItemHelp) {
                val helpDialog = HelpDialog(this@MainUI)
                helpDialog.setLocationRelativeTo(this@MainUI)
                helpDialog.setVisible(true)
            } else if (p0?.source == mAdbConnectBtn) {
                mFilteredTableModel.stopScan()
                mAdbManager.mTargetDevice = mDeviceCombo.selectedItem!!.toString()
                mAdbManager.connect()
            } else if (p0?.source == mAdbRefreshBtn) {
                mAdbManager.getDevices()
            } else if (p0?.source == mAdbDisconnectBtn) {
                mFilteredTableModel.stopScan()
                mAdbManager.disconnect()
            } else if (p0?.source == mScrollbackApplyBtn) {
                try {
                    mFilteredTableModel.mScrollback = mScrollbackTF.text.toString().trim().toInt()
                } catch (e: java.lang.NumberFormatException) {
                    mFilteredTableModel.mScrollback = 0
                    mScrollbackTF.text = "0"
                }
                mFilteredTableModel.mScrollbackSplitFile = mScrollbackSplitFileCheck.isSelected
            } else if (p0?.source == mStartBtn) {
                startAdbScan(true)
            } else if (p0?.source == mStopBtn) {
                mFilteredTableModel.stopScan()
                mAdbManager.stop()
//            } else if (p0?.source == mPauseBtn) {
            } else if (p0?.source == mClearBtn) {
                mFilteredTableModel.clearItems()
                repaint()
            } else if (p0?.source == mClearSaveBtn) {
                mFilteredTableModel.clearItems()
                setSaveLogFile()
                repaint()
            } else if (p0?.source == mRotationBtn) {
                mRotationStatus++

                if (mRotationStatus > ROTATION_MAX) {
                    mRotationStatus = ROTATION_LEFT_RIGHT
                }

                mLogSplitPane.remove(mFilteredLogPanel)
                mLogSplitPane.remove(mFullLogPanel)
                when (mRotationStatus) {
                    ROTATION_LEFT_RIGHT -> {
                        mLogSplitPane.orientation = JSplitPane.HORIZONTAL_SPLIT
                        mLogSplitPane.add(mFilteredLogPanel)
                        mLogSplitPane.add(mFullLogPanel)
                        mLogSplitPane.resizeWeight = SPLIT_WEIGHT
                    }
                    ROTATION_RIGHT_LEFT -> {
                        mLogSplitPane.orientation = JSplitPane.HORIZONTAL_SPLIT
                        mLogSplitPane.add(mFullLogPanel)
                        mLogSplitPane.add(mFilteredLogPanel)
                        mLogSplitPane.resizeWeight = 1 - SPLIT_WEIGHT
                    }
                    ROTATION_TOP_BOTTOM -> {
                        mLogSplitPane.orientation = JSplitPane.VERTICAL_SPLIT
                        mLogSplitPane.add(mFilteredLogPanel)
                        mLogSplitPane.add(mFullLogPanel)
                        mLogSplitPane.resizeWeight = SPLIT_WEIGHT
                    }
                    ROTATION_BOTTOM_TOP -> {
                        mLogSplitPane.orientation = JSplitPane.VERTICAL_SPLIT
                        mLogSplitPane.add(mFullLogPanel)
                        mLogSplitPane.add(mFilteredLogPanel)
                        mLogSplitPane.resizeWeight = 1 - SPLIT_WEIGHT
                    }
                }
            } else if (p0?.source == mFiltersBtn) {
                mFiltersManager.showFiltersDialog()
            }
        }
    }

    internal inner class FramePopUp() : JPopupMenu() {
        var mReconnectItem: JMenuItem
        val mActionHandler = ActionHandler()

        init {
            mReconnectItem = JMenuItem("Reconnect " + mDeviceCombo.selectedItem?.toString())
            mReconnectItem.addActionListener(mActionHandler)
            add(mReconnectItem)
        }
        internal inner class ActionHandler() : ActionListener {
            override fun actionPerformed(p0: ActionEvent?) {
                if (p0?.source == mReconnectItem) {
                    reconnectAdb()
                }
            }
        }
    }
    internal inner class FrameMouseListener(private val frame: JFrame) : MouseAdapter() {
        private var mouseDownCompCoords: Point? = null

        var popupMenu: JPopupMenu? = null
        override fun mouseReleased(e: MouseEvent) {
            mouseDownCompCoords = null

            if (SwingUtilities.isRightMouseButton(e)) {
                if (e.source == this@MainUI.contentPane) {
                    popupMenu = FramePopUp()
                    popupMenu?.show(e.component, e.x, e.y)
                }
            }
            else {
                popupMenu?.isVisible = false
            }
        }

        override fun mousePressed(e: MouseEvent) {
            mouseDownCompCoords = e.getPoint()
        }

        override fun mouseDragged(e: MouseEvent) {
            val currCoords = e.getLocationOnScreen()
            frame.setLocation(currCoords.x - mouseDownCompCoords!!.x, currCoords.y - mouseDownCompCoords!!.y)
        }
    }

    internal inner class PopUpCombobox(combo: JComboBox<String>?) : JPopupMenu() {
        var mSelectAllItem: JMenuItem
        var mCopyItem: JMenuItem
        var mPasteItem: JMenuItem
        var mReconnectItem: JMenuItem
        var mCombo: JComboBox<String>?
        val mActionHandler = ActionHandler()

        init {
            mSelectAllItem = JMenuItem("Select All")
            mSelectAllItem.addActionListener(mActionHandler)
            add(mSelectAllItem)
            mCopyItem = JMenuItem("Copy")
            mCopyItem.addActionListener(mActionHandler)
            add(mCopyItem)
            mPasteItem = JMenuItem("Paste")
            mPasteItem.addActionListener(mActionHandler)
            add(mPasteItem)
            mReconnectItem = JMenuItem("Reconnect " + mDeviceCombo.selectedItem?.toString())
            mReconnectItem.addActionListener(mActionHandler)
            add(mReconnectItem)
            mCombo = combo
        }
        internal inner class ActionHandler() : ActionListener {
            override fun actionPerformed(p0: ActionEvent?) {
                if (p0?.source == mSelectAllItem) {
                    mCombo?.editor?.selectAll()
                } else if (p0?.source == mCopyItem) {
                    val editorCom: JTextComponent = mCombo?.editor?.editorComponent as JTextField
                    editorCom.selectedText
                    val stringSelection = StringSelection(editorCom.selectedText)
                    val clipboard: Clipboard = Toolkit.getDefaultToolkit().systemClipboard
                    clipboard.setContents(stringSelection, null)
                } else if (p0?.source == mPasteItem) {
                    val editorCom: JTextComponent = mCombo?.editor?.editorComponent as JTextField
                    editorCom.paste()
                } else if (p0?.source == mReconnectItem) {
                    reconnectAdb()
                }
            }
        }
    }

    internal inner class MouseHandler : MouseAdapter() {
        override fun mouseClicked(p0: MouseEvent?) {
            super.mouseClicked(p0)
        }

        var popupMenu: JPopupMenu? = null
        override fun mouseReleased(p0: MouseEvent?) {
            if (p0 == null) {
                super.mouseReleased(p0)
                return
            }

            if (SwingUtilities.isRightMouseButton(p0)) {
                if (p0.source == mDeviceCombo.editor.editorComponent
                        || p0.source == mShowLogCombo.editor.editorComponent
                        || p0.source == mBoldLogCombo.editor.editorComponent
                        || p0.source == mShowTagCombo.editor.editorComponent
                        || p0.source == mShowPidCombo.editor.editorComponent
                        || p0.source == mShowTidCombo.editor.editorComponent) {
                    var combo: JComboBox<String>? = null
                    if (p0.source == mDeviceCombo.editor.editorComponent) {
                        combo = mDeviceCombo
                    } else if (p0.source == mShowLogCombo.editor.editorComponent) {
                        combo = mShowLogCombo
                    } else if (p0.source == mBoldLogCombo.editor.editorComponent) {
                        combo = mBoldLogCombo
                    } else if (p0.source == mShowTagCombo.editor.editorComponent) {
                        combo = mShowTagCombo
                    } else if (p0.source == mShowPidCombo.editor.editorComponent) {
                        combo = mShowPidCombo
                    } else if (p0.source == mShowTidCombo.editor.editorComponent) {
                        combo = mShowTidCombo
                    }
                    popupMenu = PopUpCombobox(combo)
                    popupMenu?.show(p0.component, p0.x, p0.y)
                }
                else {
                    val compo = p0.source as JComponent
                    val event = MouseEvent(compo.parent, p0.id, p0.`when`, p0.modifiers, p0.x + compo.x, p0.y + compo.y, p0.clickCount, p0.isPopupTrigger)

                    compo.parent.dispatchEvent(event)
                }
            }
            else {
                popupMenu?.isVisible = false
            }

            super.mouseReleased(p0)
        }
    }

    fun reconnectAdb() {
        println("Reconnect ADB")
        if (mDeviceCombo.selectedItem!!.toString().isNotEmpty()) {
            mAdbConnectBtn.doClick()
            mStopBtn.doClick()
            Thread(Runnable {
                run {
                    Thread.sleep(100)
                    mClearBtn.doClick()
                    mStartBtn.doClick()
                }
            }).start()
        }
    }

    fun startAdbLog() {
        Thread(Runnable {
            run {
                mStartBtn.doClick()
            }
        }).start()
    }

    fun stopAdbLog() {
        mStopBtn.doClick()
    }

    fun clearAdbLog() {
        Thread(Runnable {
            run {
                mClearBtn.doClick()
            }
        }).start()
    }

    fun clearSaveAdbLog() {
        Thread(Runnable {
            run {
                mClearSaveBtn.doClick()
            }
        }).start()
    }

    fun getTextShowLogCombo() : String {
        if (mShowLogCombo.selectedItem == null) {
           return "";
        }
        return mShowLogCombo.selectedItem!!.toString()
    }

    fun setTextShowLogCombo(text : String) {
        mShowLogCombo.selectedItem = text
    }

    fun applyShowLogCombo() {
        val item = mShowLogCombo.selectedItem!!.toString()
        resetComboItem(mShowLogCombo, item)
        mFilteredTableModel.mFilterLog = item
    }

    internal inner class KeyHandler : KeyAdapter() {
        override fun keyReleased(p0: KeyEvent?) {
            if (KeyEvent.VK_ENTER == p0?.keyCode) {
                if (p0.source == mShowLogCombo.editor.editorComponent && mShowLogToggle.isSelected) {
                    val combo = mShowLogCombo
                    val item = combo.selectedItem!!.toString()
                    resetComboItem(combo, item)
                    mFilteredTableModel.mFilterLog = item
                } else if (p0.source == mBoldLogCombo.editor.editorComponent && mBoldLogToggle.isSelected) {
                    val combo = mBoldLogCombo
                    val item = combo.selectedItem!!.toString()
                    resetComboItem(combo, item)
                    mFilteredTableModel.mFilterHighlightLog = item
                } else if (p0.source == mShowTagCombo.editor.editorComponent && mShowTagToggle.isSelected) {
                    val combo = mShowTagCombo
                    val item = combo.selectedItem!!.toString()
                    resetComboItem(combo, item)
                    mFilteredTableModel.mFilterTag = item
                } else if (p0.source == mShowPidCombo.editor.editorComponent && mShowPidToggle.isSelected) {
                    val combo = mShowPidCombo
                    val item = combo.selectedItem!!.toString()
                    resetComboItem(combo, item)
                    mFilteredTableModel.mFilterPid = item
                } else if (p0.source == mShowTidCombo.editor.editorComponent && mShowTidToggle.isSelected) {
                    val combo = mShowTidCombo
                    val item = combo.selectedItem!!.toString()
                    resetComboItem(combo, item)
                    mFilteredTableModel.mFilterTid = item
                } else if (p0.source == mDeviceCombo.editor.editorComponent) {
                    reconnectAdb()
                } else if (p0.source == mScrollbackTF) {
                    mScrollbackApplyBtn.doClick()
                }
            } else if (p0 != null && mItemFilterIncremental.state) {
                if (p0.source == mShowLogCombo.editor.editorComponent && mShowLogToggle.isSelected) {
                    val item = mShowLogCombo.editor.item.toString()
                    mFilteredTableModel.mFilterLog = item
                } else if (p0.source == mBoldLogCombo.editor.editorComponent && mBoldLogToggle.isSelected) {
                    val item = mBoldLogCombo.editor.item.toString()
                    mFilteredTableModel.mFilterHighlightLog = item
                } else if (p0.source == mShowTagCombo.editor.editorComponent && mShowTagToggle.isSelected) {
                    val item = mShowTagCombo.editor.item.toString()
                    mFilteredTableModel.mFilterTag = item
                } else if (p0.source == mShowPidCombo.editor.editorComponent && mShowPidToggle.isSelected) {
                    val item = mShowPidCombo.editor.item.toString()
                    mFilteredTableModel.mFilterPid = item
                } else if (p0.source == mShowTidCombo.editor.editorComponent && mShowTidToggle.isSelected) {
                    val item = mShowTidCombo.editor.item.toString()
                    mFilteredTableModel.mFilterTid = item
                }
            }
            super.keyReleased(p0)
        }
    }

    internal inner class ItemHandler : ItemListener {
        override fun itemStateChanged(p0: ItemEvent?) {
            if (p0?.source == mShowLogToggle) {
                mShowLogCombo.setEnabledFilter(mShowLogToggle.isSelected)
            } else if (p0?.source == mBoldLogToggle) {
                mBoldLogCombo.setEnabledFilter(mBoldLogToggle.isSelected)
            } else if (p0?.source == mShowTagToggle) {
                mShowTagCombo.setEnabledFilter(mShowTagToggle.isSelected)
            } else if (p0?.source == mShowPidToggle) {
                mShowPidCombo.setEnabledFilter(mShowPidToggle.isSelected)
            } else if (p0?.source == mShowTidToggle) {
                mShowTidCombo.setEnabledFilter(mShowTidToggle.isSelected)
            }

            if (mIsCreatingUI) {
                return
            }
            if (p0?.source == mShowLogToggle) {
                if (mShowLogToggle.isSelected && mShowLogCombo.selectedItem != null) {
                    mFilteredTableModel.mFilterLog = mShowLogCombo.selectedItem!!.toString()
                } else {
                    mFilteredTableModel.mFilterLog = ""
                }
            } else if (p0?.source == mBoldLogToggle) {
                if (mBoldLogToggle.isSelected && mBoldLogCombo.selectedItem != null) {
                    mFilteredTableModel.mFilterHighlightLog = mBoldLogCombo.selectedItem!!.toString()
                } else {
                    mFilteredTableModel.mFilterHighlightLog = ""
                }
            } else if (p0?.source == mShowTagToggle) {
                if (mShowTagToggle.isSelected && mShowTagCombo.selectedItem != null) {
                    mFilteredTableModel.mFilterTag = mShowTagCombo.selectedItem!!.toString()
                } else {
                    mFilteredTableModel.mFilterTag = ""
                }
            } else if (p0?.source == mShowPidToggle) {
                if (mShowPidToggle.isSelected && mShowPidCombo.selectedItem != null) {
                    mFilteredTableModel.mFilterPid = mShowPidCombo.selectedItem!!.toString()
                } else {
                    mFilteredTableModel.mFilterPid = ""
                }
            } else if (p0?.source == mShowTidToggle) {
                if (mShowTidToggle.isSelected && mShowTidCombo.selectedItem != null) {
                    mFilteredTableModel.mFilterTid = mShowTidCombo.selectedItem!!.toString()
                } else {
                    mFilteredTableModel.mFilterTid = ""
                }
            } else if (p0?.source == mMatchCaseBtn) {
                mFilteredTableModel.mMatchCase = mMatchCaseBtn.isSelected
            } else if (p0?.source == mScrollbackKeepToggle) {
                mFilteredTableModel.mScrollbackKeep = mScrollbackKeepToggle.isSelected
            }
        }
    }

    internal inner class LevelItemHandler : ItemListener {
        override fun itemStateChanged(p0: ItemEvent?) {
            val item = p0?.source as JRadioButtonMenuItem
            when (item.text) {
                VERBOSE->mFilteredTableModel.mFilterLevel = mFilteredTableModel.LEVEL_VERBOSE
                DEBUG->mFilteredTableModel.mFilterLevel = mFilteredTableModel.LEVEL_DEBUG
                INFO->mFilteredTableModel.mFilterLevel = mFilteredTableModel.LEVEL_INFO
                WARNING->mFilteredTableModel.mFilterLevel = mFilteredTableModel.LEVEL_WARNING
                ERROR->mFilteredTableModel.mFilterLevel = mFilteredTableModel.LEVEL_ERROR
                FATAL->mFilteredTableModel.mFilterLevel = mFilteredTableModel.LEVEL_FATAL
            }
        }
    }

    internal inner class AdbHandler : AdbEventListener {
        override fun changedStatus(event: AdbEvent) {
            when (event.cmd) {
                AdbManager.CMD_CONNECT -> {
                    mAdbManager.getDevices()
                }
                AdbManager.CMD_GET_DEVICES -> {
                    if (mIsCreatingUI) {
                        return
                    }
                    var selectedItem = mDeviceCombo.selectedItem
                    mDeviceCombo.removeAllItems()
                    for (item in mAdbManager.mDevices) {
                        mDeviceCombo.addItem(item)
                    }
                    if (selectedItem == null) {
                        selectedItem = ""
                    }

                    if (mAdbManager.mDevices.contains(selectedItem.toString())) {
                        mDeviceStatus.text = Strings.CONNECTED
                    } else {
                        var isExist = false
                        val deviceChk = selectedItem.toString() + ":"
                        for (device in mAdbManager.mDevices) {
                            if (device.contains(deviceChk)) {
                                isExist = true
                                selectedItem = device
                                break
                            }
                        }
                        if (isExist) {
                            mDeviceStatus.text = Strings.CONNECTED
                        } else {
                            mDeviceStatus.text = Strings.NOT_CONNECTED
                        }
                    }
                    mDeviceCombo.selectedItem = selectedItem
                }
                AdbManager.CMD_DISCONNECT -> {
                    mAdbManager.getDevices()
                }
            }
        }
    }

    internal inner class PopupMenuHandler : PopupMenuListener {
        private var mIsCanceled = false
        override fun popupMenuWillBecomeInvisible(p0: PopupMenuEvent?) {
            if (mIsCanceled) {
                mIsCanceled = false
                return
            }
            if (p0?.source == mShowLogCombo) {
                if (mShowLogCombo.selectedIndex < 0) {
                    return
                }
                val combo = mShowLogCombo
                val item = combo.selectedItem!!.toString()
                if (combo.editor.item.toString() != item) {
                    return
                }
                resetComboItem(combo, item)
                mFilteredTableModel.mFilterLog = item
            } else if (p0?.source == mBoldLogCombo) {
                if (mBoldLogCombo.selectedIndex < 0) {
                    return
                }
                val combo = mBoldLogCombo
                val item = combo.selectedItem!!.toString()
                resetComboItem(combo, item)
                mFilteredTableModel.mFilterHighlightLog = item
            } else if (p0?.source == mShowTagCombo) {
                if (mShowTagCombo.selectedIndex < 0) {
                    return
                }
                val combo = mShowTagCombo
                val item = combo.selectedItem!!.toString()
                resetComboItem(combo, item)
                mFilteredTableModel.mFilterTag = item
            } else if (p0?.source == mShowPidCombo) {
                if (mShowPidCombo.selectedIndex < 0) {
                    return
                }
                val combo = mShowPidCombo
                val item = combo.selectedItem!!.toString()
                resetComboItem(combo, item)
                mFilteredTableModel.mFilterPid = item
            } else if (p0?.source == mShowTidCombo) {
                if (mShowTidCombo.selectedIndex < 0) {
                    return
                }
                val combo = mShowTidCombo
                val item = combo.selectedItem!!.toString()
                resetComboItem(combo, item)
                mFilteredTableModel.mFilterTid = item
            }
        }

        override fun popupMenuCanceled(p0: PopupMenuEvent?) {
            mIsCanceled = true
        }

        override fun popupMenuWillBecomeVisible(p0: PopupMenuEvent?) {
            val box = p0?.getSource() as JComboBox<*>
            val comp = box.ui.getAccessibleChild(box, 0) as? JPopupMenu ?: return
            val scrollPane = comp.getComponent(0) as JScrollPane
            scrollPane.verticalScrollBar?.ui = BasicScrollBarUI()
            scrollPane.horizontalScrollBar?.ui = BasicScrollBarUI()
            mIsCanceled = false
        }
    }

    internal inner class ComponentHandler : ComponentAdapter() {
        override fun componentResized(p0: ComponentEvent?) {
            revalidate()
            super.componentResized(p0)
        }
    }

    fun resetComboItem(combo: ColorComboBox<String>, item: String) {
        if (combo.isExistItem(item)) {
            if (combo.selectedIndex == 0) {
                return
            }
            combo.removeItem(item)
        }
        combo.insertItemAt(item, 0)
        combo.selectedIndex = 0
        return
    }

    fun goToLine(line: Int) {
        println("Line : " + line)
        if (line < 0) {
            return
        }
        var num = 0
        for (idx in 0 until mFilteredTableModel.rowCount) {
            num = mFilteredTableModel.getValueAt(idx, 0).toString().trim().toInt()
            if (line <= num) {
                mFilteredLogPanel.goToRow(idx, 0)
                break
            }
        }

        if (line != num) {
            for (idx in 0 until mFullTableModel.rowCount) {
                num = mFullTableModel.getValueAt(idx, 0).toString().trim().toInt()
                if (line <= num) {
                    mFullLogPanel.goToRow(idx, 0)
                    break
                }
            }
        }
    }

    fun markLine() {
        if (mIsCreatingUI) {
            return
        }
        mSelectedLine = mFilteredLogPanel.getSelectedLine()
    }

    fun getMarkLine(): Int {
        return mSelectedLine
    }

    fun goToMarkedLine() {
        if (mIsCreatingUI) {
            return
        }
        goToLine(mSelectedLine)
    }

    internal inner class StatusTextField(text: String?) : JTextField(text) {
        var mPrevText = ""
        override fun getToolTipText(event: MouseEvent?): String? {
            val textTrimmed = text.trim()
            var tooltip = ""
            if (mPrevText != textTrimmed && textTrimmed.isNotEmpty()) {
                mPrevText = textTrimmed
                val splitData = textTrimmed.split("|")

                tooltip = "<html>"
                for (item in splitData) {
                    val itemTrimmed = item.trim()
                    if (itemTrimmed.isNotEmpty()) {
                        tooltip += "$itemTrimmed<br>"
                    }
                }
                tooltip += "</html>"
                toolTipText = tooltip
            }
            return super.getToolTipText(event)
        }
    }
}


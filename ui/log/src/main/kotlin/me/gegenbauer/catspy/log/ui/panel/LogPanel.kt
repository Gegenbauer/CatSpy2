package me.gegenbauer.catspy.log.ui.panel

import me.gegenbauer.catspy.common.configuration.GThemeChangeListener
import me.gegenbauer.catspy.common.configuration.ThemeManager
import me.gegenbauer.catspy.common.configuration.UIConfManager
import me.gegenbauer.catspy.common.configuration.isDark
import me.gegenbauer.catspy.common.support.LogColorScheme
import me.gegenbauer.catspy.common.ui.button.ColorToggleButton
import me.gegenbauer.catspy.common.ui.button.IconBarButton
import me.gegenbauer.catspy.common.ui.container.WrapablePanel
import me.gegenbauer.catspy.common.ui.icon.DayNightIcon
import me.gegenbauer.catspy.common.ui.table.PageIndicator
import me.gegenbauer.catspy.common.ui.table.RowNavigation
import me.gegenbauer.catspy.context.Context
import me.gegenbauer.catspy.context.Contexts
import me.gegenbauer.catspy.context.ServiceManager
import me.gegenbauer.catspy.databinding.bind.Bindings
import me.gegenbauer.catspy.databinding.bind.ObservableViewModelProperty
import me.gegenbauer.catspy.databinding.bind.withName
import me.gegenbauer.catspy.databinding.property.support.selectedProperty
import me.gegenbauer.catspy.iconset.GIcons
import me.gegenbauer.catspy.log.BookmarkChangeListener
import me.gegenbauer.catspy.log.BookmarkManager
import me.gegenbauer.catspy.log.ui.LogTabPanel
import me.gegenbauer.catspy.log.ui.table.LogTable
import me.gegenbauer.catspy.log.ui.table.LogTableModel
import me.gegenbauer.catspy.log.ui.table.LogTableModelListener
import me.gegenbauer.catspy.resource.strings.STRINGS
import me.gegenbauer.catspy.utils.applyTooltip
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Font
import java.awt.Insets
import java.awt.event.AdjustmentEvent
import java.awt.event.AdjustmentListener
import java.awt.event.FocusListener
import javax.swing.BoxLayout
import javax.swing.JPanel
import javax.swing.JScrollBar
import javax.swing.JScrollPane
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener
import javax.swing.event.TableModelEvent


abstract class LogPanel(
    val tableModel: LogTableModel,
    private val focusChangeListener: FocusListener,
    override val contexts: Contexts = Contexts.default,
    val table: LogTable = LogTable(tableModel)
) : JPanel(), Context, ListSelectionListener, RowNavigation by table {

    val viewModel = LogPanelViewModel()
    protected val ctrlMainPanel: WrapablePanel = WrapablePanel() withName "ctrlMainPanel"

    private val topBtn = IconBarButton(GIcons.Action.Top.get()) applyTooltip STRINGS.toolTip.viewFirstBtn
    private val bottomBtn = IconBarButton(GIcons.Action.Bottom.get()) applyTooltip STRINGS.toolTip.viewLastBtn
    private val tagBtn = ColorToggleButton(STRINGS.ui.tag) applyTooltip STRINGS.toolTip.viewTagToggle
    private val pidBtn = ColorToggleButton(STRINGS.ui.pid) applyTooltip STRINGS.toolTip.viewPidToggle
    private val tidBtn = ColorToggleButton(STRINGS.ui.tid) applyTooltip STRINGS.toolTip.viewTidToggle
    private val scrollToEndIcon = DayNightIcon(
        GIcons.State.ScrollEnd.get(24, 24),
        GIcons.State.ScrollEndDark.get(24, 24)
    )
    private val scrollToEndSelectedIcon = DayNightIcon(
        GIcons.State.ScrollEnd.selected(24, 24),
        GIcons.State.ScrollEndDark.selected(24, 24)
    )
    private val scrollToEndBtn = IconBarButton()

    private val scrollPane = JScrollPane(table)
    private val vStatusPanel = VStatusPanel()
    private val pageNavigationPanel = PageIndicator(tableModel)
    private val adjustmentHandler = AdjustmentHandler()
    private val tableModelHandler = TableModelHandler()
    private val bookmarkHandler = BookmarkHandler()

    private var lastPosition = -1

    private val themeChangeListener = GThemeChangeListener {
        scrollToEndIcon.isDarkMode = it.isDark
        scrollToEndSelectedIcon.isDarkMode = it.isDark
        scrollToEndBtn.repaint()
    }

    init {
        registerEvent()

        observeViewModelProperty()
    }

    private fun registerEvent() {
        topBtn.addActionListener { moveToFirstRow() }
        bottomBtn.addActionListener { moveToLastRow() }
        table.addFocusListener(focusChangeListener)
        tableModel.addLogTableModelListener(tableModelHandler)
        scrollPane.verticalScrollBar.addAdjustmentListener(adjustmentHandler)
        scrollToEndBtn.addActionListener {
            scrollToEndBtn.isSelected = !scrollToEndBtn.isSelected
        }
        ThemeManager.registerThemeUpdateListener(themeChangeListener)
    }

    private fun observeViewModelProperty() {
        viewModel.boldPid.addObserver {
            table.repaint()
        }
        viewModel.boldTid.addObserver {
            table.repaint()
        }
        viewModel.boldTag.addObserver {
            table.repaint()
        }
        viewModel.scrollToEnd.addObserver {
            if (it == true) {
                moveToLastRow()
            }
        }
    }

    class LogPanelViewModel {
        val scrollToEnd = ObservableViewModelProperty(false)
        val boldPid = ObservableViewModelProperty(false)
        val boldTid = ObservableViewModelProperty(false)
        val boldTag = ObservableViewModelProperty(false)
        val fullMode = ObservableViewModelProperty(false)
        val bookmarkMode = ObservableViewModelProperty(false)
    }

    protected open fun bind(viewModel: LogPanelViewModel) {
        viewModel.apply {
            Bindings.bind(selectedProperty(scrollToEndBtn), scrollToEnd)
            Bindings.bind(selectedProperty(pidBtn), boldPid)
            Bindings.bind(selectedProperty(tidBtn), boldTid)
            Bindings.bind(selectedProperty(tagBtn), boldTag)
        }
    }

    override fun configureContext(context: Context) {
        super.configureContext(context)
        table.setContexts(contexts)
        vStatusPanel.setContexts(contexts)

        contexts.getContext(LogTabPanel::class.java)?.apply {
            val bookmarkManager = ServiceManager.getContextService(this, BookmarkManager::class.java)
            bookmarkManager.addBookmarkEventListener(bookmarkHandler)
        }
    }

    protected open fun createUI() {
        layout = BorderLayout()
        scrollPane.minimumSize = Dimension(0, 0)
        Insets(2, 3, 1, 3).apply {
            topBtn.margin = this
            bottomBtn.margin = this
            scrollToEndBtn.margin = this
        }
        Insets(0, 3, 0, 3).apply {
            tagBtn.margin = this
            pidBtn.margin = this
            tidBtn.margin = this
        }
        scrollToEndBtn.isRolloverEnabled = false
        scrollToEndBtn.isContentAreaFilled = false
        scrollToEndBtn.icon = scrollToEndIcon
        scrollToEndBtn.selectedIcon = scrollToEndSelectedIcon

        table.columnSelectionAllowed = true
        table.listSelectionHandler = this
        scrollPane.verticalScrollBar.unitIncrement = 20

        val ctrlPanel = JPanel()
        ctrlPanel.layout = BoxLayout(ctrlPanel, BoxLayout.Y_AXIS)
        ctrlPanel.add(ctrlMainPanel)

        add(ctrlPanel, BorderLayout.NORTH)
        add(vStatusPanel, BorderLayout.WEST)
        add(scrollPane, BorderLayout.CENTER)
        add(pageNavigationPanel, BorderLayout.SOUTH)
    }

    open fun updateTableBar() {
        ctrlMainPanel.removeAll()
        ctrlMainPanel.add(topBtn)
        ctrlMainPanel.add(bottomBtn)
        ctrlMainPanel.add(pidBtn)
        ctrlMainPanel.add(tidBtn)
        ctrlMainPanel.add(tagBtn)
        ctrlMainPanel.add(scrollToEndBtn)
    }

    var customFont: Font = Font(
        UIConfManager.uiConf.logFontName,
        UIConfManager.uiConf.logFontStyle,
        UIConfManager.uiConf.logFontSize
    )
        set(value) {
            field = value
            table.font = value
            table.rowHeight = value.size + 4

            repaint()
        }

    override fun repaint() {
        background = LogColorScheme.logBG
        super.repaint()
    }

    fun goToLineIndex(logNum: Int) {
        table.moveToRow(logNum)
    }

    fun setGoToLast(value: Boolean) {
        viewModel.scrollToEnd.updateValue(value)
    }

    override fun moveToFirstRow() {
        table.moveToFirstRow()
        setGoToLast(false)
    }

    override fun moveToLastRow() {
        table.moveToLastRow()
    }

    internal inner class AdjustmentHandler : AdjustmentListener {
        override fun adjustmentValueChanged(event: AdjustmentEvent) {
            val currentPosition = event.value
            val isScrollingDown = currentPosition - lastPosition > 0
            takeIf { currentPosition != lastPosition } ?: return
            lastPosition = currentPosition

            val scrollBar = event.adjustable as JScrollBar
            val extent = scrollBar.model.extent
            val maximum = scrollBar.model.maximum
            false.takeUnless { isScrollingDown }?.let {
                setGoToLast(false)
            }

            val valueIsAtMaximum = (event.value + extent) >= maximum
            true.takeIf { valueIsAtMaximum }?.let {
                if (tableModel.currentPage == tableModel.pageCount - 1) {
                    setGoToLast(true)
                }
            }
            vStatusPanel.repaint()
        }
    }

    internal inner class TableModelHandler : LogTableModelListener {

        override fun onLogDataChanged(event: TableModelEvent) {
            if ((event.source as LogTableModel).rowCount == 0) {
                lastPosition = -1
            }

            if (viewModel.scrollToEnd.value == true) {
                moveToLastRow()
            }
        }
    }

    override fun valueChanged(event: ListSelectionEvent) {
        // Empty implementation
    }

    internal inner class BookmarkHandler : BookmarkChangeListener {
        override fun bookmarkChanged() {
            vStatusPanel.repaint()
            table.repaint()
        }
    }

    override fun onDestroy() {
        ThemeManager.unregisterThemeUpdateListener(themeChangeListener)
        ctrlMainPanel.onDestroy()
    }

    companion object {
        private const val TAG = "LogPanel"
    }
}

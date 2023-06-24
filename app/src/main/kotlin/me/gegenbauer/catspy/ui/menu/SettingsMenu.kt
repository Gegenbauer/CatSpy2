package me.gegenbauer.catspy.ui.menu

import com.github.weisj.darklaf.settings.ThemeSettings
import me.gegenbauer.catspy.resource.strings.STRINGS
import me.gegenbauer.catspy.ui.Menu.MENU_ITEM_ICON_SIZE
import me.gegenbauer.catspy.utils.loadDarklafThemedIcon
import java.awt.Dialog
import java.awt.event.ActionEvent
import java.awt.event.KeyEvent
import javax.swing.JCheckBoxMenuItem
import javax.swing.JMenuItem

class SettingsMenu : GMenu() {
    // TODO itemFilterIncremental has no sense
    val itemDebug = JCheckBoxMenuItem(STRINGS.ui.debug)
    private val itemThemeSettings = JMenuItem(STRINGS.ui.theme).apply {
        icon = loadDarklafThemedIcon("menu/themeSettings.svg", MENU_ITEM_ICON_SIZE)
        addActionListener { _: ActionEvent ->
            ThemeSettings.showSettingsDialog(this, Dialog.ModalityType.APPLICATION_MODAL)
        }
    }

    init {
        text = STRINGS.ui.setting
        mnemonic = KeyEvent.VK_S
        add(itemThemeSettings)
        addSeparator()
        add(itemDebug)
    }
}
package com.blogspot.kotlinstudy.lognote

import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import javax.swing.JToggleButton


class ColorToggleButton(title:String) : JToggleButton(title){
    var mSelectedFg: Color? = null
    var mSelectedBg: Color? = null
    var mUnselectedFg: Color? = null
    var mUnselectedBg: Color? = null

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)

        val graphics2D = g as Graphics2D
        graphics2D.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON
        )
        graphics2D.setRenderingHint(
            RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_ON
        )

        val thickness = 2
        if (isSelected || model.isPressed) {

            if (mSelectedBg != null) {
                graphics2D.color = mSelectedBg
            }
            else {
                graphics2D.color = Color(0x85, 0x85, 0x85)
            }
            graphics2D.fillRect(0, 0, width, height)

            graphics2D.color = Color(0x0, 0x0, 0x0)
            graphics2D.fillRect(0, 0, thickness, height)
            graphics2D.fillRect(0, 0, width, thickness)

            if (mSelectedFg != null) {
                graphics2D.color = mSelectedFg
            }
            else {
                graphics2D.color = Color(0xFF, 0xFF, 0xFF)
            }
            graphics2D.drawString(text,
                (width - graphics2D.fontMetrics.stringWidth(text)) / 2,
                (height + graphics2D.fontMetrics.ascent) / 2 - 2)

        }
        else {
            if (mUnselectedBg != null) {
                graphics2D.color = mUnselectedBg
            }
            else {
                graphics2D.color = Color(0xFA, 0xFA, 0xFF)
            }

            graphics2D.fillRect(0, 0, width, height)

            if (mUnselectedFg != null) {
                graphics2D.color = mUnselectedFg
            }
            else {
                graphics2D.color = Color(0xBB, 0x84, 0x4C)
            }
            graphics2D.drawString(text,
                (width - graphics2D.fontMetrics.stringWidth(text)) / 2,
                (height + graphics2D.fontMetrics.ascent) / 2 - 2)
        }
    }
}
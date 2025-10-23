package com.selectabletext

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.widget.FrameLayout
import android.widget.TextView
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactContext
import com.facebook.react.bridge.WritableMap
import com.facebook.react.modules.core.DeviceEventManagerModule

class SelectableTextView : FrameLayout {
  private var menuOptions: Array<String> = emptyArray()
  private var textViews: List<TextView> = emptyList()
  
  constructor(context: Context?) : super(context!!)
  constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs)
  constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
    context!!,
    attrs,
    defStyleAttr
  )
  
  fun setMenuOptions(options: Array<String>) {
    this.menuOptions = options
    setupCallbacks()
  }

  private fun collectTextViews(root: View, out: MutableList<TextView> = mutableListOf(), onFound: (TextView) -> Unit): List<TextView> {
    if (root is TextView) {
        out.add(root)
        onFound(root)
    }
    if (root is ViewGroup) {
        for (i in 0 until root.childCount) {
            collectTextViews(root.getChildAt(i), out, onFound)
        }
    }
    return out
}
  
  private fun setupCallbacks() {
    textViews = collectTextViews(this) { tv ->
        setupSelectionCallback(tv)
    }
  }
  
  private fun setupSelectionCallback(textView: TextView) {
    textView.setTextIsSelectable(true)
    textView.customSelectionActionModeCallback = object : ActionMode.Callback {
      override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        return true
      }
      
      override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        menu?.clear()
        menuOptions.forEachIndexed { index, option ->
          menu?.add(0, index, 0, option)
        }
        return true
      }
      
      override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
        val selectionStart = textView.selectionStart
        val selectionEnd = textView.selectionEnd
        val textViewIndex = textViews.indexOf(textView)
        if (textViewIndex == -1) { return false }

        var offset = 0
        for (i in 0 until textViewIndex) {
          offset += textViews[i].text.length
        }
        val adjustedStart = selectionStart + offset
        val adjustedEnd = selectionEnd + offset
        
        val selectedText = textView.text.toString().substring(selectionStart, selectionEnd)
        val chosenOption = menuOptions[item?.itemId ?: 0]
        
        // Send event to React Native
        onSelectionEvent(chosenOption, selectedText, adjustedStart, adjustedEnd)
        
        mode?.finish()
        return true
      }
      
      override fun onDestroyActionMode(mode: ActionMode?) {
        // Called when action mode is destroyed
      }
    }
  }
  
  private fun onSelectionEvent(chosenOption: String, highlightedText: String, startIndex: Int, endIndex: Int) {
    val reactContext = context as ReactContext
    val params = Arguments.createMap().apply {
      putInt("viewTag", id)
      putString("chosenOption", chosenOption)
      putString("highlightedText", highlightedText)
      putInt("startIndex", startIndex)
      putInt("endIndex", endIndex)
    }

    Log.d("SelectableTextView", "Emitting selection event: $params")
    
    reactContext
      .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
      .emit("SelectableTextSelection", params)
  }
  
  override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
    super.onLayout(changed, left, top, right, bottom)
    if (changed) {
      setupCallbacks()
    }
  }
  
  override fun onViewRemoved(child: View) {
    super.onViewRemoved(child)
    setupCallbacks()
  }

  override fun onViewAdded(child: View) {
    super.onViewAdded(child)
    setupCallbacks()
  }
}

package com.revolsys.swing.field;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JTextField;

import org.jdesktop.swingx.JXTextField;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.data.equals.EqualsRegistry;
import com.revolsys.swing.listener.WeakFocusListener;
import com.revolsys.swing.menu.PopupMenu;
import com.revolsys.swing.undo.UndoManager;
import com.revolsys.util.Property;

public class TextField extends JXTextField implements Field, FocusListener {
  public static final Color DEFAULT_BACKGROUND = new JTextField().getBackground();

  public static final Color DEFAULT_FOREGROUND = new JTextField().getForeground();

  public static final Color DEFAULT_SELECTED_FOREGROUND = new JTextField().getSelectedTextColor();

  private static final long serialVersionUID = 1L;

  private final FieldSupport support;

  public TextField(final int columns) {
    this("text");
    setColumns(columns);
  }

  public TextField(final String fieldName) {
    this(fieldName, "");
  }

  public TextField(final String fieldName, final int columns) {
    this(fieldName);
    setColumns(columns);
  }

  public TextField(String fieldName, final Object fieldValue) {
    setDocument(new PropertyChangeDocument(this));
    if (!Property.hasValue(fieldName)) {
      fieldName = "fieldValue";
    }
    final String text = StringConverterRegistry.toString(fieldValue);
    this.support = new FieldSupport(this, fieldName, text);
    setText(text);
    addFocusListener(new WeakFocusListener(this));
    PopupMenu.getPopupMenuFactory(this);
  }

  public TextField(final String fieldName, final Object fieldValue,
    final int columns) {
    this(fieldName, fieldValue);
    setColumns(columns);
  }

  @Override
  public void firePropertyChange(final String propertyName,
    final Object oldValue, final Object newValue) {
    super.firePropertyChange(propertyName, oldValue, newValue);
  }

  @Override
  public void focusGained(final FocusEvent e) {
  }

  @Override
  public void focusLost(final FocusEvent e) {
    final String text = getText();
    setFieldValue(text);
  }

  protected String getDisplayText(final Object value) {
    return StringConverterRegistry.toString(value);
  }

  @Override
  public String getFieldName() {
    return this.support.getName();
  }

  @Override
  public String getFieldValidationMessage() {
    return this.support.getErrorMessage();
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getFieldValue() {
    final String text = getText();
    return (T)text;
  }

  @Override
  public boolean isFieldValid() {
    return true;
  }

  @Override
  public void setFieldBackgroundColor(Color color) {
    if (color == null) {
      color = DEFAULT_BACKGROUND;
    }
    setBackground(color);
  }

  @Override
  public void setFieldForegroundColor(Color color) {
    if (color == null) {
      color = DEFAULT_FOREGROUND;
    }
    setForeground(color);
  }

  @Override
  public void setFieldInvalid(final String message,
    final Color foregroundColor, final Color backgroundColor) {
    this.support.setFieldInvalid(message, foregroundColor, backgroundColor);
  }

  @Override
  public void setFieldToolTip(final String toolTip) {
    setToolTipText(toolTip);
  }

  @Override
  public void setFieldValid() {
    this.support.setFieldValid();
  }

  @Override
  public void setFieldValue(final Object value) {
    final String newText = getDisplayText(value);
    final String text = getText();
    this.support.discardAllEdits();
    if (!EqualsRegistry.equal(text, newText)) {
      if (newText == null) {
        if (Property.hasValue(text)) {
          setText("");
        }
      } else {
        setText(newText);
      }
      this.support.discardAllEdits();
    }
    this.support.setValue(newText);
  }

  @Override
  public void setToolTipText(final String text) {
    if (this.support.setOriginalTooltipText(text)) {
      super.setToolTipText(text);
    }
  }

  @Override
  public void setUndoManager(final UndoManager undoManager) {
    this.support.setUndoManager(undoManager);
  }

  @Override
  public String toString() {
    return getFieldName() + "=" + getFieldValue();
  }

  @Override
  public void updateFieldValue() {
    setFieldValue(getText());
  }
}

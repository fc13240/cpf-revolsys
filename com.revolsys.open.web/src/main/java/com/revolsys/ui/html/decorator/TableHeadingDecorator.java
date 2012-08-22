package com.revolsys.ui.html.decorator;

import java.util.Iterator;

import com.revolsys.io.xml.XmlWriter;
import com.revolsys.ui.html.HtmlUtil;
import com.revolsys.ui.html.fields.Field;
import com.revolsys.ui.html.view.Element;
import com.revolsys.ui.html.view.ElementContainer;
import com.revolsys.ui.html.view.TableRow;

public class TableHeadingDecorator implements Decorator {
  public static void addRow(final ElementContainer container,
    final Element element, final String label, final String instructions) {

    final TableHeadingDecorator decorator = new TableHeadingDecorator(label,
      instructions);
    final TableRow row = new TableRow();
    row.add(element, decorator);
    container.add(row);
  }

  private String label = "";

  private String instructions = "";

  private boolean required;

  public TableHeadingDecorator(final String label) {
    this.label = label;
  }

  public TableHeadingDecorator(final String label, final String instructions) {
    this.label = label;
    this.instructions = instructions;
  }

  public String getInstructions() {
    return instructions;
  }

  public void setRequired(boolean required) {
    this.required = required;
  }

  public String getLabel() {
    return label;
  }

  @Override
  public void serialize(final XmlWriter out, final Element element) {
    out.startTag(HtmlUtil.TH);
    serializeLabel(out, element);
    out.endTag(HtmlUtil.TH);
    out.startTag(HtmlUtil.TD);
    out.startTag(HtmlUtil.DIV);
    out.attribute(HtmlUtil.ATTR_CLASS, "fieldComponent");
    serializeElement(out, element);
    serializeErrors(out, element);
    out.endTag(HtmlUtil.DIV);
    serializeInstructions(out);
    out.endTag(HtmlUtil.TD);
  }

  protected void serializeErrors(final XmlWriter out, final Element element) {
    if (element instanceof Field) {
      Field field = (Field)element;
      for (final String error : field.getValidationErrors()) {
        out.startTag(HtmlUtil.DIV);
        out.attribute(HtmlUtil.ATTR_CLASS, "errorMessage");
        out.startTag(HtmlUtil.LABEL);
        out.attribute(HtmlUtil.ATTR_FOR, field.getName());
        out.attribute(HtmlUtil.ATTR_CLASS, "error");
        out.attribute("generated", "true");

        out.text(error);
        out.endTag(HtmlUtil.LABEL);
        out.endTag(HtmlUtil.DIV);
      }
    }
  }

  protected void serializeElement(final XmlWriter out, final Element element) {
    out.startTag(HtmlUtil.DIV);
    out.attribute(HtmlUtil.ATTR_CLASS, "contents");
    element.serializeElement(out);
    out.endTag(HtmlUtil.DIV);
  }

  protected void serializeInstructions(final XmlWriter out) {
    final String instructions = getInstructions();
    if (instructions != null) {
      out.startTag(HtmlUtil.DIV);
      out.attribute(HtmlUtil.ATTR_CLASS, "instructions");
      out.text(instructions);
      out.endTag(HtmlUtil.DIV);
    }
  }

  protected void serializeLabel(final XmlWriter out, final Element element) {
    final String label = getLabel();
    if (label != null) {
      out.startTag(HtmlUtil.DIV);
      String cssClass = "label";
      if (element instanceof Field) {
        Field field = (Field)element;
        if (field.isRequired()) {
          cssClass = "label required";
        }
      } else if (required) {
        cssClass = "label required";
      }
      out.attribute(HtmlUtil.ATTR_CLASS, cssClass);
      out.startTag(HtmlUtil.LABEL);
      if (element instanceof Field) {
        Field field = (Field)element;
        out.attribute(HtmlUtil.ATTR_FOR, field.getName());
      }
      out.text(label);
      out.endTag(HtmlUtil.LABEL);
      out.endTag(HtmlUtil.DIV);
    }
  }

  public void setInstructions(final String instructions) {
    this.instructions = instructions;
  }

  public void setLabel(final String label) {
    this.label = label;
  }
}
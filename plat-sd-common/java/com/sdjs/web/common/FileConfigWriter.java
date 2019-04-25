package com.sdjs.web.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

public class FileConfigWriter
{
  /**
   * 读入一个XML文档,以Document对象实例的形式的存储在内存中,如果出错,返回null
   * 
   * @param String
   *          fXML
   * @return Element element
   * @throws PortalException
   */
  public static Element getRootElementFromXMLFile(File fXML) throws Exception
  {
    SAXBuilder saxBuilder = new SAXBuilder(true);
    saxBuilder.setValidation(false);
    Document doc = saxBuilder.build(fXML);
    Element element = doc.getRootElement();
    doc.detachRootElement();
    return element;
  }

}

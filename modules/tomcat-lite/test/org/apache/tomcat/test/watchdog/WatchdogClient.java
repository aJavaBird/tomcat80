/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tomcat.test.watchdog;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;

import org.apache.tomcat.util.DomUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class WatchdogClient {

  protected String base = "../watchdog";
        
  protected String goldenDir; 
  protected String testMatch;
  protected String file;
  protected String[] exclude = null;
  protected String[] slow = 
  { 
      "SingleModelTest" // slow  
  };  

  protected String targetMatch;
    

  Properties props = new Properties();
  
  protected void beforeSuite() {
      
  }
  
  protected void afterSuite(TestResult res) {
      
  }
  
  /** 
   * Return a test suite for running a watchdog-like 
   * test file. 
   *
   * @param base base dir for the watchdog dir
   * @param testMatch Prefix of tests to be run
   * @return
   */
  public Test getSuite() {
    TestSuite tests = new WatchdogTests();
    tests.setName(this.getClass().getSimpleName());
    
    props.setProperty("port", "8080");
    props.setProperty("host", "localhost");
    props.setProperty("wgdir", 
        goldenDir);
    
    
    try {
      Document doc = DomUtil.readXml(new FileInputStream(file));
      Element docE = doc.getDocumentElement();
      NodeList targetsL = docE.getElementsByTagName("target");
      for (int i = 0; i < targetsL.getLength(); i++) {
        Element target = (Element) targetsL.item(i);
        String targetName = target.getAttribute("name");
        if (targetMatch != null && !targetName.equals(targetMatch)) {
            continue;
        }
        
        // Tests are duplicated
        //TestSuite targetSuite = new TestSuite(targetName);
        
        NodeList watchDogL = target.getElementsByTagName("watchdog");
        for (int j = 0; j < watchDogL.getLength(); j++) {
          Element watchE = (Element) watchDogL.item(j);
          String testName = watchE.getAttribute("testName");
          if (testMatch != null) {
              if (!testName.startsWith(testMatch)) {
                  continue;
              }
          }
          if (exclude != null) {
              boolean found = false;
              for (String e: exclude) {
                  if (e.equals(testName)) {
                      found = true; 
                      break;
                  }
              }
              if (found) {
                  continue;
              }
          }
          testName = testName + ";" + this.getClass().getName();
          WatchdogTest test = new WatchdogTest(watchE, props, testName);
          tests.addTest(test);
        }
        
        //        if (targetSuite.countTestCases() > 0) { 
        //          tests.addTest(targetSuite);
        //              }
      }
      
    } catch (IOException e) {
      e.printStackTrace();
    } catch (SAXException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    } catch (ParserConfigurationException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }
    return tests;
  }
  
  // --------- Inner classes -------------

  public class WatchdogTests extends TestSuite {
      public void run(TestResult res) {
          beforeSuite();
          super.run(res);
          afterSuite(res);
      }
  }
  
}

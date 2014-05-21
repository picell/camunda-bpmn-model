/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.model.cmmn.impl;

import java.io.InputStream;
import java.net.URL;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.validation.SchemaFactory;

import org.camunda.bpm.model.cmmn.Cmmn;
import org.camunda.bpm.model.xml.ModelValidationException;
import org.camunda.bpm.model.xml.impl.ModelImpl;
import org.camunda.bpm.model.xml.impl.parser.AbstractModelParser;
import org.camunda.bpm.model.xml.impl.util.ReflectUtil;
import org.camunda.bpm.model.xml.instance.DomDocument;
import org.xml.sax.SAXException;

/**
 * @author Roman Smirnov
 *
 */
public class CmmnParser extends AbstractModelParser {

    private static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";
    private static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";

   private static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";

   public CmmnParser() {
     this.schemaFactory = SchemaFactory.newInstance(W3C_XML_SCHEMA);
     URL bpmnSchema = ReflectUtil.getResource(CmmnModelConstants.CMMN_10_SCHEMA_LOCATION, CmmnParser.class.getClassLoader());
     try {
       this.schema = schemaFactory.newSchema(bpmnSchema);
     } catch (SAXException e) {
       throw new ModelValidationException("Unable to parse schema:" + bpmnSchema);
     }
   }

   @Override
   protected void configureFactory(DocumentBuilderFactory dbf) {
     dbf.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
     dbf.setAttribute(JAXP_SCHEMA_SOURCE, ReflectUtil.getResource(CmmnModelConstants.CMMN_10_SCHEMA_LOCATION, CmmnParser.class.getClassLoader()).toString());
     super.configureFactory(dbf);
   }

   @Override
   protected CmmnModelInstanceImpl createModelInstance(DomDocument document) {
     return new CmmnModelInstanceImpl((ModelImpl) Cmmn.INSTANCE.getCmmnModel(), Cmmn.INSTANCE.getCmmnModelBuilder(), document);
   }

   @Override
   public CmmnModelInstanceImpl parseModelFromStream(InputStream inputStream) {
     return (CmmnModelInstanceImpl) super.parseModelFromStream(inputStream);
   }

   @Override
   public CmmnModelInstanceImpl getEmptyModel() {
     return (CmmnModelInstanceImpl) super.getEmptyModel();
   }

}

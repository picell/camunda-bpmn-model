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
package org.camunda.bpm.model.bpmn.impl.instance;

import org.camunda.bpm.model.bpmn.BpmnModelException;
import org.camunda.bpm.model.bpmn.Query;
import org.camunda.bpm.model.bpmn.builder.AbstractFlowNodeBuilder;
import org.camunda.bpm.model.bpmn.impl.QueryImpl;
import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.camunda.bpm.model.xml.type.ModelElementType;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.child.SequenceBuilder;
import org.camunda.bpm.model.xml.type.reference.AttributeReference;
import org.camunda.bpm.model.xml.type.reference.ElementReferenceCollection;
import org.camunda.bpm.model.xml.type.reference.Reference;

import java.util.Collection;
import java.util.HashSet;

import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.*;

/**
 * The BPMN flowNode element
 *
 * @author Sebastian Menski
 */
public abstract class FlowNodeImpl extends FlowElementImpl implements FlowNode {

  private static ElementReferenceCollection<SequenceFlow, Incoming> incomingCollection;
  private static ElementReferenceCollection<SequenceFlow, Outgoing> outgoingCollection;

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(FlowNode.class, BPMN_ELEMENT_FLOW_NODE)
      .namespaceUri(BPMN20_NS)
      .extendsType(FlowElement.class)
      .abstractType();

    SequenceBuilder sequenceBuilder = typeBuilder.sequence();

    incomingCollection = sequenceBuilder.elementCollection(Incoming.class)
      .qNameElementReferenceCollection(SequenceFlow.class)
      .build();

    outgoingCollection = sequenceBuilder.elementCollection(Outgoing.class)
      .qNameElementReferenceCollection(SequenceFlow.class)
      .build();

    typeBuilder.build();
  }

  public FlowNodeImpl(ModelTypeInstanceContext context) {
    super(context);
  }

  public AbstractFlowNodeBuilder builder() {
    throw new BpmnModelException("No builder implemented for type " + getElementType().getTypeNamespace() +":" + getElementType().getTypeName());
  }

  public void updateAfterReplacement() {
    super.updateAfterReplacement();
    // we traverse all incoming references in reverse direction
    for (Reference<?> reference : idAttribute.getIncomingReferences()) {

      ModelElementType sourceElementType = reference.getReferenceSourceElementType();
      Class<? extends ModelElementInstance> sourceInstanceType = sourceElementType.getInstanceType();

      // if the referencing element (source element) is a sequence flow element,
      // add a incoming/outgoing element if it references us
      if(SequenceFlow.class.isAssignableFrom(sourceInstanceType)) {
        for (ModelElementInstance sourceElement : reference.findReferenceSourceElements(this)) {
          String referenceIdentifier = reference.getReferenceIdentifier(sourceElement);

          // make sure it is a reference to us
          if (referenceIdentifier != null && referenceIdentifier.equals(getId()) && reference instanceof AttributeReference) {
            String attributeName = ((AttributeReference) reference).getReferenceSourceAttribute().getAttributeName();
            if (attributeName.equals(BPMN_ATTRIBUTE_SOURCE_REF)) {
              getOutgoing().add((SequenceFlow) sourceElement);
            }
            else if (attributeName.equals(BPMN_ATTRIBUTE_TARGET_REF)) {
              getIncoming().add((SequenceFlow) sourceElement);
            }
          }
        }
      }
    }
  }

  public Collection<SequenceFlow> getIncoming() {
    return incomingCollection.getReferenceTargetElements(this);
  }

  public Collection<SequenceFlow> getOutgoing() {
    return outgoingCollection.getReferenceTargetElements(this);
  }

  public Query<FlowNode> getPreviousNodes() {
    Collection<FlowNode> previousNodes = new HashSet<FlowNode>();
    for (SequenceFlow sequenceFlow : getIncoming()) {
      previousNodes.add(sequenceFlow.getSource());
    }
    return new QueryImpl<FlowNode>(previousNodes);
  }

  public Query<FlowNode> getSucceedingNodes() {
    Collection<FlowNode> succeedingNodes = new HashSet<FlowNode>();
    for (SequenceFlow sequenceFlow : getOutgoing()) {
      succeedingNodes.add(sequenceFlow.getTarget());
    }
    return new QueryImpl<FlowNode>(succeedingNodes);
  }
}

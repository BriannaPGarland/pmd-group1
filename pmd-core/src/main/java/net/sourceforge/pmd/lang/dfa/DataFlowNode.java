/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.dfa;

import static net.sourceforge.pmd.lang.dfa.AbstractDataFlowNode.DATAFLOW_KEY;

import java.util.List;

import net.sourceforge.pmd.lang.ast.Node;

public interface DataFlowNode {
    List<VariableAccess> getVariableAccess();

    int getLine();

    int getIndex();

    boolean isType(NodeType type);

    void setType(NodeType type);

    List<DataFlowNode> getChildren();

    List<DataFlowNode> getParents();

    List<DataFlowNode> getFlow();

    Node getNode();

    void setVariableAccess(List<VariableAccess> variableAccess);

    void addPathToChild(DataFlowNode child);

    boolean removePathToChild(DataFlowNode child);

    void reverseParentPathsTo(DataFlowNode destination);


    static DataFlowNode get(Node node) {
        DataFlowNode df = node.getUserMap().get(DATAFLOW_KEY);
        if (df == null && node.getParent() != null) {
            return get(node.getParent());
        } else {
            return df;
        }
    }
}

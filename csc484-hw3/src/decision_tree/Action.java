package decision_tree;

import java.util.HashMap;

public class Action extends DecisionTreeNode {

	private static final long serialVersionUID = 9060833469771762345L;

	public String behavior;
	
	public Action(String behavior, String id) {
		super(id.hashCode());
		this.behavior = behavior;
	}

	@Override
	public DecisionTreeNode makeDecision(HashMap<String, Parameter> parameters) {
		return this;
	}
}

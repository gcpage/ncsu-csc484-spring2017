package behavior_tree;

import java.util.ArrayList;
import java.util.List;

public abstract class Task implements BehaviorTreeNode{
	
	public List<BehaviorTreeNode> children;
	public int status;

	public Task() {
		children = new ArrayList<BehaviorTreeNode>();
		status = 0;
	}
}
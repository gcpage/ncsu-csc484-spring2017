package path_following;

import general.Kinematic;
import general.SteeringOutput;
import general.Vector;
import graph.AdjacencyList;
import path_finding.AStar;
import path_finding.Euclidian;

public class PathTo extends FollowPath {
	
	public AdjacencyList graph;
	public Vector destination;

	public PathTo(Kinematic character, AdjacencyList graph, Vector destination) {
		super(character);
		this.graph = graph;
		this.destination = destination;
	}

	@Override
	public SteeringOutput getSteering() {
		AStar aStar = new AStar();
		Euclidian euclidian = new Euclidian();
		path = aStar.path(graph, graph.closestTo(character.position), graph.closestTo(destination), euclidian); 
		return super.getSteering();
	}
}

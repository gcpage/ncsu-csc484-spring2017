package execution;

import java.util.HashMap;

import decision_tree.Action;
import decision_tree.Decision;
import decision_tree.DecisionTree;
import decision_tree.DecisionTreeNode;
import decision_tree.Parameter;
import general.Actor;
import general.Breadcrumbs;
import general.Vector;
import graph.AdjacencyList;
import graph.Edge;
import graph.Node;
import path_finding.AStar;
import path_finding.Euclidian;
import path_following.FollowPath;
import path_following.PathTo;
import path_following.SteeringBehavior;
import processing.core.PApplet;
import processing.core.PImage;

public class DecisionTreeView extends PApplet {

	public static final int viewWidth = 800;
	public static final int viewHeight = 800;
	public static final int characterRadius = 10;

	private static DecisionActor character;
	private static DecisionTree characterTree;
	
	private static HashMap<String, Parameter> paramDict;
	private static HashMap<String, SteeringBehavior> behaviorDict;

	private static boolean fridgePing;
	private static boolean tvPing;
	private static boolean toiletPing;

	private static Vector fridgePos;
	private static Vector tvPos;
	private static Vector toiletPos;
	private static Vector breakerPos;
	private static Vector safezonePos;

	private static Breadcrumbs breadcrumbs;

	public static PImage img;
	public static AdjacencyList tileGraph;

	public static AStar aStar;
	public static Euclidian euclidian;

	private static long timestamp;

	public static void main(String[] args) {
		fridgePing = false;
		tvPing = false;
		toiletPing = false;

		fridgePos = new Vector(145, viewHeight - 200);
		tvPos = new Vector(288, viewHeight - 350);
		toiletPos = new Vector(122, viewHeight - 390);
		breakerPos = new Vector(121, viewHeight - 292);

		character = new DecisionActor(safezonePos.x, safezonePos.y, 100);
		breadcrumbs = new Breadcrumbs(100, 0.1);

		aStar = new AStar();
		euclidian = new Euclidian();
		
		constructDictionaries();
		buildCharacterTree();
		
		PApplet.main("execution.DecisionTreeView");
	}

	private static void constructDictionaries() {
		paramDict = new HashMap<String, Parameter>();
		paramDict.put("fridge", new Parameter() {
			
			@Override
			public boolean getValue() {
				return fridgePing;
			}
		});
		paramDict.put("tv", new Parameter() {
			
			@Override
			public boolean getValue() {
				return tvPing;
			}
		});
		paramDict.put("toilet", new Parameter() {
			
			@Override
			public boolean getValue() {
				return toiletPing;
			}
		});
		
		behaviorDict = new HashMap<String, SteeringBehavior>();
		behaviorDict.put("goToFridge", new PathTo(character.getKinematic(), tileGraph, fridgePos));
		behaviorDict.put("goToTv", new PathTo(character.getKinematic(), tileGraph, tvPos));
		behaviorDict.put("goToToilet")
	}
	
	private static void buildCharacterTree() {
		SteeringBehavior goToFridge = ;
		SteeringBehavior goToTv = ;
		SteeringBehavior goToToilet = new PathTo(character.getKinematic(), tileGraph, toiletPos);
		SteeringBehavior goToBreaker = new PathTo(character.getKinematic(), tileGraph, breakerPos);
		SteeringBehavior goToSafezone = new PathTo(character.getKinematic(), tileGraph, safezonePos);
		
		characterTree = new DecisionTree(new Decision("fridge", "a"));
		characterTree.add(new Decision("tv", "a1"), "a", true);
		characterTree.add(new Action(goToBreaker, "a1b"), "a1", true);
		characterTree.add(new Decision("toilet", "a2"), "a1", false);
		characterTree.add(new Action(goToBreaker, "a2b"), "a2", true);
		characterTree.add(new Action(goToFridge, "a3"), "a2", false);
		characterTree.add(new Decision("tv", "b"), "a", false);
		characterTree.add(new Decision("toilet", "b1"), "b", true);
		characterTree.add(new Action(goToBreaker, "b1b"), "b1", true);
		characterTree.add(new Action(goToTv, "b2"), "b1", false);
		characterTree.add(new Decision("toilet", "c"), "b", false);
		characterTree.add(new Action(goToToilet, "c1"), "c", true);
		characterTree.add(new Action(goToSafezone, "d"), "c", false);
		
	}

	public void settings() {
		size(viewWidth, viewHeight);
	}

	public void setup() {
		img = loadImage("living_room.png");
		image(img, 0, 0);
		buildTileGraph();
		timestamp = System.nanoTime();
	}

	public void draw() {
		long timestampPrev = timestamp;
		timestamp = System.nanoTime();
		double dt = (timestamp - timestampPrev) / 1000000000.0;
		image(img, 0, 0);
		// renderTileGraph();

		character.update(dt);
		breadcrumbs.add(character.getKinematic().position, timestamp);
		renderBreadcrumbs(breadcrumbs);
		renderActor(character);
	}

	private void renderActor(Actor agent) {
		float x = (float) agent.getKinematic().position.x;
		float y = (float) (viewHeight - agent.getKinematic().position.y);
		float a = (float) agent.getKinematic().orientation;
		fill(0);
		triangle((float) (x - Math.sin(a) * characterRadius), (float) (y + Math.cos(a) * characterRadius),
				(float) (x + Math.sin(a) * characterRadius), (float) (y - Math.cos(a) * characterRadius),
				(float) (x + 2 * Math.cos(a) * characterRadius * 0.75),
				(float) (y + 2 * Math.sin(a) * characterRadius * 0.75));
		fill(255);
		ellipse(x, y, 2.0f * characterRadius, 2.0f * characterRadius);
	}

	private void renderBreadcrumbs(Breadcrumbs breadcrumbs) {
		fill(0);
		for (Vector crumb : breadcrumbs) {
			ellipse((float) crumb.x, (float) (viewHeight - crumb.y), 0.3f * characterRadius, 0.3f * characterRadius);
		}
	}

	private void renderTileGraph() {
		for (Node node : tileGraph.nodeList) {
			ellipse((float) node.position.x, (float) (viewHeight - node.position.y), 3f, 3f);
		}
		for (Edge edge : tileGraph.edgeList) {
			line((float) edge.origin.position.x, (float) (viewHeight - edge.origin.position.y),
					(float) edge.destination.position.x, (float) (viewHeight - edge.destination.position.y));
		}
	}

	private void buildTileGraph() {
		int tileWidth = 2 * characterRadius;
		int xtiles = viewWidth / tileWidth;
		int ytiles = viewHeight / tileWidth;
		tileGraph = new AdjacencyList();
		boolean[][] tiles = new boolean[xtiles][ytiles];

		loadPixels();
		for (int i = 0; i < viewHeight; i++) {
			for (int j = 0; j < viewWidth; j++) {
				if (pixels[i * viewWidth + j] < color(100)) {
					tiles[j / tileWidth][i / tileWidth] = true;
				}
			}
		}
		for (int i = 0; i < ytiles; i++) {
			for (int j = 0; j < xtiles; j++) {
				if (!tiles[j][i]) {
					tileGraph.addNode(new Node(i * xtiles + j, (ytiles - i - 1) * tileWidth + characterRadius,
							j * tileWidth + characterRadius));
				}
			}
		}
		for (Node node : tileGraph.nodeList) {
			int id = node.id;
			if (id % xtiles != 0)
				tileGraph.addDoubleEdge(id, id - 1);
			if (id % xtiles != xtiles - 1)
				tileGraph.addDoubleEdge(id, id + 1);
			if (id >= xtiles)
				tileGraph.addDoubleEdge(id, id - xtiles);
			if (id < xtiles * (ytiles - 1))
				tileGraph.addDoubleEdge(id, id + xtiles);
		}
	}
}

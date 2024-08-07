package org.example.Physics;

import java.util.*;

import static java.lang.Math.random;

class Node {
	private float x;
	private float y;

	public Node(int x, int y) {
		this.x = x;
		this.y = y;
	}
}

public class Graph {
	float min_distance;
	float max_distance;

	int width;
	int height;

	int node_count;
	int edge_count;

	Vector<Node> nodes = new Vector<>();
	Map<Integer,Integer> edges = new HashMap<Integer,Integer>();

	private int random_int(int a, int b) {
		return (int)(random() * (b-a) + a);
	}

	public Graph(int node_count, int edge_count, int width, int height) {
		this.node_count = node_count;
		this.edge_count = edge_count;
		this.width = width;
		this.height = height;
	}

	private void add_edge(int src_node, int dst_node) {
		if (edges.containsKey(src_node) && edges.get(src_node) == dst_node) { return; }
		if (edges.containsKey(dst_node) && edges.get(dst_node) == src_node) { return; }
		edges.put(src_node,dst_node);
		edges.put(dst_node,src_node);
	}

	public void init(World world) {
		int x;
		int y;
		for (int i = 0; i < node_count; i++) {
			x = random_int(0,width);
			y = random_int(0,height);

			nodes.add(new Node(x,y));

			world.new_object(x,y,0,2,2);
		}
		for (int i = 0; i < node_count; i++) {
			for (int j = 0; j < edge_count; j++) {
				add_edge(i,random_int(0,node_count));
			}
		}
	}
}

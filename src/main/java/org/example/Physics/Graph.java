package org.example.Physics;

import org.example.Object.Object;

import java.util.*;

import static java.lang.Math.*;

class Node {
	public float x;
	public float y;

	public int sprite_id;

	public Node(int x, int y, int sprite_id) {
		this.x = x;
		this.y = y;
		this.sprite_id = sprite_id;
	}
}

public class Graph {
	float min_distance;
	float max_distance;

	int width;
	int height;

	int node_count;
	int edge_count;

	World world;

	Vector<Node> nodes = new Vector<>();
	Map<Integer,Integer> edges = new HashMap<Integer,Integer>();
	Vector<Integer> edge_sprite_ids = new Vector<>();

	private int random_int(int a, int b) {
		return (int)(random() * (b-a) + a);
	}

	private double get_distance(float x1, float y1, float x2, float y2) {
		return sqrt((x1-x2) * (x1-x2) + (y1-y2) * (y1-y2));
	}

	private double get_angle(float x1, float y1, float x2, float y2) {
		float x = x1 - x2;
		float y = y1 - y2;
		if (x >= 0 && y >= 0)  return atan(y/x);
		if (x < 0 && y >= 0)   return PI/2 + atan(-x/y);
		if (x < 0 && y < 0)  return PI + atan(y/x);
		return 3 * PI / 2 + atan(-x/y);
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

	private void translate_node(int node_index, float x, float y, float angle) {
		Node node = this.nodes.get(node_index);
		node.x += x;
		node.y += y;
		Object object = world.objects.get(node.sprite_id);
		object.translate(x,y);
		object.rotate(angle);
	}

	public void init(World world,float cell_size) {
		this.world = world;

		int x;
		int y;
		for (int i = 0; i < node_count; i++) {
			x = random_int(0,width);
			y = random_int(0,height);

			world.new_object(x,y,0,2,2);
			nodes.add(new Node(x,y,world.objects.size()-1));

		}
		for (int i = 0; i < node_count; i++) {
			for (int j = 0; j < edge_count; j++) {
				int other_ind = random_int(0,node_count);
				add_edge(i,other_ind);

				Node src = this.nodes.get(i);
				Node dst = this.nodes.get(other_ind);

				float distance = (float)get_distance(src.x,src.y,dst.x,dst.y);
				float angle = (float)(get_angle(src.x,src.y,dst.x,dst.y));

				float centre_x = (src.x + dst.x) / 2;
				float centre_y = (src.y + dst.y) / 2;

				world.new_object(centre_x,centre_y,angle,(int)(distance / cell_size),1);
				edge_sprite_ids.add(world.objects.size()-1);
			}
		}
	}

	public void update() {
		int dst;

		Node src_node;
		Node dst_node;

		for (Integer src: this.edges.keySet()) {
			dst = this.edges.get(src);

			translate_node(src,1.0f,0.0f,0.0f);
		}

		for (Integer edge: this.edge_sprite_ids) {
			translate_node(edge,1.0f,0.0f,0.0f);
		}
	}
}

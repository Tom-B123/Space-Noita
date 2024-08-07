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

class Edge {
	public int src;
	public int dst;

	public int id;

	public Edge(int id, int src, int dst) {
		this.src = src;
		this.dst = dst;
		this.id = id;
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
	float cell_size;

	Vector<Node> nodes = new Vector<>();
	Vector<Edge> edges = new Vector<>();

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

	private void add_edge(int edge_index, int src_node, int dst_node) {
		edges.add(new Edge(edge_index, src_node, dst_node));
	}

	private void translate_node(int node_index, float x, float y, float angle) {
		Node node = this.nodes.get(node_index);
		node.x += x;
		node.y += y;
		Object object = world.objects.get(node.sprite_id);
		object.translate(x,y);
		object.rotate(angle);
	}

	private void update_edge(int edge_index, int src, int dst) {
		Node src_node = this.nodes.get(src);
		Node dst_node = this.nodes.get(dst);

		float distance = (float)get_distance(src_node.x,src_node.y,dst_node.x,dst_node.y);
		float angle = (float)(get_angle(src_node.x,src_node.y,dst_node.x,dst_node.y));

		float centre_x = (src_node.x + dst_node.x) / 2;
		float centre_y = (src_node.y + dst_node.y) / 2;

		Object object = world.objects.get(edge_index);

		float dx = (float)-object.get_transform().x + centre_x;
		float dy = (float)-object.get_transform().y + centre_y;
		float dangle = (float)-object.get_transform().angle + angle;
		float dscale = (float)(distance / this.cell_size) / object.scale_x;

		object.translate(dx,dy);
		object.rotate(dangle);
		float temp_angle = (float)object.get_transform().angle;
		object.rotate(-temp_angle);
		object.scale(dscale,1.0f);
		object.rotate(temp_angle);
	}

	public void init(World world,float cell_size,float min_distance, float max_distance) {
		this.world = world;
		this.cell_size = cell_size;

		this.min_distance = min_distance;
		this.max_distance = max_distance;

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

				Node src = this.nodes.get(i);
				Node dst = this.nodes.get(other_ind);

				float distance = (float)get_distance(src.x,src.y,dst.x,dst.y);
				float angle = (float)(get_angle(src.x,src.y,dst.x,dst.y));

				float centre_x = (src.x + dst.x) / 2;
				float centre_y = (src.y + dst.y) / 2;

				world.new_object(centre_x,centre_y,angle,(int)(distance / cell_size),1);
				add_edge(world.objects.size()-1,i,other_ind);
			}
		}
	}

	public void update(float dt) {
		Node src_node;
		Node dst_node;

		float ox;
		float oy;


		for (Edge edge : this.edges) {
			src_node = this.nodes.get(edge.src);
			dst_node = this.nodes.get(edge.dst);

			float distance = (float)get_distance(src_node.x,src_node.y,dst_node.x,dst_node.y);
			if (distance < min_distance) {

				ox = ((dst_node.x - src_node.x) / distance) * (min_distance - distance) * 0.01f * dt;
				oy = ((dst_node.y - src_node.y) / distance) * (min_distance - distance) * 0.01f * dt;

				translate_node(edge.src,ox ,oy, 0.0f);
			}
			if (distance > max_distance) {

				ox = ((dst_node.x - src_node.x) / distance) * (distance - max_distance) * 0.01f * dt;
				oy = ((dst_node.y - src_node.y) / distance) * (distance - max_distance) * 0.01f * dt;

				translate_node(edge.src,ox ,oy, 0.0f);
			}

		}
		for (Edge edge : this.edges) {
			update_edge(edge.id,edge.src,edge.dst);
		}
	}
}

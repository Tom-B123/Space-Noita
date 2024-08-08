package org.example.Physics;

import org.example.Object.Object;

import java.util.*;

import static java.lang.Float.NaN;
import static java.lang.Float.isNaN;
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

		int tries = 0;
		do { tries ++; }
		while (this.update() > 10.0f && tries < 500);
		this.get_all_intersections();
	}

	public float update() {

		float movement = 0;

		Node src_node;
		Node dst_node;

		float ox;
		float oy;

		float pull_power = 0.01f;

		for (Edge edge : this.edges) {
			src_node = this.nodes.get(edge.src);
			dst_node = this.nodes.get(edge.dst);

			float distance = (float)get_distance(src_node.x,src_node.y,dst_node.x,dst_node.y);

			if (distance == 0) { continue; }

			if (distance < min_distance) {

				ox = ((-dst_node.x + src_node.x) / distance) * (min_distance - distance) * pull_power;
				oy = ((-dst_node.y + src_node.y) / distance) * (min_distance - distance) * pull_power;

				movement += abs(ox) + abs(oy);

				translate_node(edge.src, ox, oy, 0.0f);
			}
			if (distance > max_distance) {

				ox = ((dst_node.x - src_node.x) / distance) * (distance - max_distance) * pull_power;
				oy = ((dst_node.y - src_node.y) / distance) * (distance - max_distance) * pull_power;

				movement += abs(ox) + abs(oy);

				translate_node(edge.src,ox ,oy, 0.0f);
			}

		}
		for (int i = 0; i < this.nodes.size(); i++) {
			for (int j = i+1; j < this.nodes.size(); j++) {
				src_node = this.nodes.get(i);
				dst_node = this.nodes.get(j);
				float distance = (float)get_distance(src_node.x,src_node.y,dst_node.x,dst_node.y);

				if (distance < min_distance) {

					ox = ((-dst_node.x + src_node.x) / distance) * (min_distance - distance) * pull_power;
					oy = ((-dst_node.y + src_node.y) / distance) * (min_distance - distance) * pull_power;

					movement += abs(ox) + abs(oy);

					translate_node(i,ox ,oy, 0.0f);
				}

			}
		}
		for (Edge edge : this.edges) {
			update_edge(edge.id,edge.src,edge.dst);
		}
		return movement;
	}

	private float[] line_from(float x1, float y1, float x2, float y2) {
		return new float[]{(y1-y2)/(x1-x2), y1-((y1-y2)/(x1-x2)) * x1};
	}

	private float[] min_max(float[] vals) {
		if  (vals[0] <= vals[1]) { return vals; }
		float tmp = vals[0];
		vals[0] = vals[1];
		vals[1] = tmp;
		tmp = vals[2];
		vals[2] = vals[3];
		vals[2] = tmp;
		return vals;
	}

	private float[] get_intersection(Edge edge1, Edge edge2) {
		float[] line1,line2;
		float m1,c1,m2,c2;
		boolean v1,v2;

		Node a = this.nodes.get(edge1.src);
		Node b = this.nodes.get(edge1.dst);
		Node c = this.nodes.get(edge2.src);
		Node d = this.nodes.get(edge2.dst);

		v1 = a.x != b.x;
		v2 = c.x != d.x;

		if (!v1 && !v2) { return new float[]{NaN, NaN}; }

		float[] min_max_1 = min_max(new float[] {a.x,b.x,a.y,b.y});
		float[] min_max_2 = min_max(new float[] {c.x,d.x,c.y,d.y});

		float min_x_1 = min_max_1[0];
		float max_x_1 = min_max_1[1];
		float min_y_1 = min_max_1[2];
		float max_y_1 = min_max_1[3];

		float min_x_2 = min_max_2[0];
		float max_x_2 = min_max_2[1];
		float min_y_2 = min_max_2[2];
		float max_y_2 = min_max_2[3];

		float[] starts = min_max(new float[] { min_x_1, min_x_2, min_y_1, min_y_2});

		float start_x = starts[1];
		float alt_start_y = starts[3];

		float[] ends = min_max(new float[] { max_x_1, max_x_2, max_y_1, max_y_2});

		float end_x = ends[0] ;
		float alt_end_y = ends[2];

		line1 = line_from(a.x,a.y,b.x,b.y);
		line2 = line_from(c.x,c.y,d.x,d.y);

		// Get the line M and C values for intersections
		m1 = line1[0];
		c1 = line1[1];
		m2 = line2[0];
		c2 = line2[1];

		float start_y_1 = m1 * start_x + c1;
		float start_y_2 = m2 * start_x + c2;

		float end_y_1 = m1 * end_x + c1;
		float end_y_2 = m2 * end_x + c2;

		if (!v1) { start_y_1 = alt_start_y; end_y_1 = alt_end_y; }
		if (!v2) { start_y_2 = alt_start_y; end_y_2 = alt_end_y; }

		// If the min and max are bad, return out
		if (!(
			((max_x_1 >= min_x_2 && min_x_1 <= max_x_2) ||
			(max_x_2 >= min_x_1 && min_x_2 <= max_x_1)) &&
			(start_y_2 - start_y_1)*(end_y_2 - end_y_1) < 0
		)) { return new float[]{NaN, NaN}; }

		// Standard case
		float intersection_x = (c2-c1)/(m1-m2);
		float intersection_y = m1 * intersection_x + c1;

		// Line 1 is invalid
		if (!v1) {
			intersection_x = this.nodes.get(edge1.src).x;
			intersection_y = m2 * intersection_x + c2;
		}
		// Line 2 is invalid
		if (!v2) {
			intersection_x = this.nodes.get(edge2.src).x;
			intersection_y = m1 * intersection_x + c1;
		}

		return new float[]{intersection_x,intersection_y};
	}

	public void get_all_intersections() {
		// Expect around n * edge_count ^3

		int intersection_count = 0;
		Edge edge1;
		Edge edge2;
		// For every edge combination:
		for (int i = 0; i < this.edges.size(); i++) {
			for (int j = i + 1; j < this.edges.size(); j++) {

				edge1 = this.edges.get(i);
				edge2 = this.edges.get(j);

				// Get the intersection points
				float[] intersect = get_intersection(edge1,edge2);

				// Ensure the intersection is valid
				if (!isNaN(intersect[0])) {

					intersection_count ++;
				}
			}
		}
		System.out.println("found [" + intersection_count + "] intersections!");
	}
}

package org.example.Components;

import org.example.Object.Component;

public class SpriteRenderer extends Component {
	@Override
	public void init() {
		System.out.println("inited sprite renderer");
	}

	@Override
	public void update(float dt) {

	}

	public void draw() {
		System.out.println(this.object.get_height());
	}
}

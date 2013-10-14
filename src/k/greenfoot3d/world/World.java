package k.greenfoot3d.world;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import k.greenfoot3d.actors.Actor;
import k.greenfoot3d.ui.Inform;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;

import crashcourse.k.library.lwjgl.DisplayLayer;
import crashcourse.k.library.main.KMain;

public abstract class World extends KMain {
	private List<Class<?>> actOrder = new ArrayList<Class<?>>();
	private List<Actor> actors = new ArrayList<Actor>();

	public World(int cellsX, int cellsY, int cellSize) {
		this(cellsX, cellsY, cellSize, true);
	}

	public World(int cellsX, int cellsY, int cellSize, boolean bounded) {
		try {
			DisplayLayer.initDisplay(false, cellsX * cellSize, cellsY
					* cellSize, "Greenfoot - " + getClass().getSimpleName(),
					false, new String[0], this);
		} catch (Exception e) {
			e.printStackTrace();
			Inform.userOf("Error while launching: " + e.getClass() + ": "
					+ e.getMessage());
		}
	}

	public void start() {
		while (!Display.isCloseRequested()) {
			try {
				DisplayLayer.loop(120);
			} catch (LWJGLException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onDisplayUpdate(int delta) {
		subAct(delta);
	}

	public void setActOrder(Class<?>... order) {
		List<Class<?>> order_list = Arrays.asList(order);
		actOrder.removeAll(order_list);
		order_list.addAll(actOrder);
		actOrder = order_list;
	}

	private void subAct(int delta) {
		for (Class<?> c : actOrder) {
			if (c.isInstance(this)) {
				act();
			} else {
				multiAct(c);
			}
		}
	}

	private void multiAct(Class<?> c) {
		List<Actor> actorsForClass = getObjects(c);
		for (Actor a : actorsForClass) {
			a.act();
		}
	}

	private List<Actor> getObjects(Class<?> c) {
		List<Actor> ret = new ArrayList<Actor>();
		for (Actor a : actors) {
			if (c.isInstance(a)) {
				ret.add(a);
			}
		}
		return ret;
	}

	public void act() {
	}

	@Override
	public void init(String[] args) {

	}

}
package k.greenfoot3d.actors;

import k.greenfoot3d.image.GreenfootImage;
import k.greenfoot3d.world.World;

public class Actor {
	private World worldObj;
	private GreenfootImage image = null;

	public Actor() {
		worldObj = null;

	}

	public void setLocation(int x, int y) {
		if (isInWorld()) {

		} else {
			throw new IllegalAccessError("Actor not in world");
		}
	}

	public boolean isInWorld() {
		return getWorld() != null;
	}

	public World getWorld() {
		return worldObj;
	}

	public void setImage(GreenfootImage img) {
		image = img;
	}

	public GreenfootImage getImage() {
		return image;
	}

	public void act() {
		
	}
}

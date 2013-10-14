package k.greenfoot3d.image;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import org.lwjgl.opengl.GL11;

import crashcourse.k.library.lwjgl.tex.BufferedTexture;
import crashcourse.k.library.lwjgl.tex.Texture;
import crashcourse.k.library.util.LUtils;

public class GreenfootImage extends Texture {

	public GreenfootImage(String path) {
		if (isImage(path)) {
			BufferedImage img;
			try {
				img = ImageIO.read(new File(path));
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
			makeFromAWTImage(img);
		}
	}

	public GreenfootImage(GreenfootImage copyFrom) {
		Texture texcp = copyFrom;
		buf = texcp.buf.duplicate();
		dim = new Dimension(texcp.dim);
		init();
	}

	public GreenfootImage(int width, int height) {
		buf = ByteBuffer.allocateDirect(width * height * 4);
		dim = new Dimension(width, height);
	}

	public GreenfootImage(String text, int fontSize, Color textColor,
			Color background) {
		throw new UnsupportedOperationException(
				"Text in image not supported yet!");
	}

	private boolean isImage(String path) {
		return path.matches("(png|jpeg|jpg)^");
	}

	/**
	 * Warning: The original implementation is *backed* by this, but the 3D
	 * version is not.
	 * 
	 * You will need to call makeFromAWTImage in order to change the image.
	 * 
	 * @return the BufferedImage representation of this image
	 */
	public BufferedImage getAWTImage() {
		return toBufferedImage();
	}

	public void clear() {
		kill();
	}

	public void draw(GreenfootImage img, int x, int y) {
		if (x > dim.width || y > dim.height) {
			new IllegalArgumentException(
					"x or y is larger than this images width, will not draw image.")
					.printStackTrace();
			return;
		}
		bind();
		int choppedWidth = img.dim.width, choppedHeight = img.dim.height;
		if (img.dim.width + x > dim.width) {
			choppedWidth = dim.width - x;
		}
		if (img.dim.height + y > dim.height) {
			choppedHeight = dim.height - x;
		}
		ByteBuffer choppedBuf = LUtils.chopBuffer(img.buf, choppedWidth,
				choppedHeight, 4);
		GL11.glTexSubImage2D(GL_TEXTURE_2D, 0, x, y, choppedWidth,
				choppedHeight, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, choppedBuf);
	}

	public void makeFromAWTImage(BufferedImage img) {
		BufferedTexture texcp = new BufferedTexture(img);
		buf = texcp.buf;
		dim = texcp.dim;
		setConstructingOverrideId(texcp.getID());
		init();
	}

	@Override
	public void setup() {
	}

}

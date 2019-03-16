package ajb.model;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.UUID;

import ajb.area.AreaUtils;
import ajb.colours.ColourUtils;
import ajb.geometry.GeometryUtils;
import ajb.images.ImageUtils;
import ajb.ships.ShipUtils;

public class Vessel implements Serializable {

	private static final long serialVersionUID = 6238354968702292635L;

	public transient Area halfArea = null;
	public transient Area displayArea = null;
	public transient Area bounds = null;

	public String identifier = UUID.randomUUID().toString();
	public String name = null;
	public Point2D.Double center = null;
	public boolean selected = false;
	double rotationInDegrees = 0;
	double shields = 0;
	double armour = 0;
	double hull = 0;
	Color color = ColourUtils.gray;

	public Vessel(Area area, Point2D.Double center) {

		this.halfArea = area;
		this.center = center;

		generateDisplayArea();

	}

	public void draw(Graphics2D g2d) {

		try {

			if (halfArea == null) {

				generateDisplayArea();

			}

			g2d.rotate(Math.toRadians(rotationInDegrees), center.getX(), center.getY());

			g2d.setColor(ColourUtils.makeTransparent(ColourUtils.background, 200));
			g2d.fill(displayArea.getBounds2D());

			if (selected) {
				
				g2d.setColor(color.brighter());
				
				
			} else {
				
				g2d.setColor(color);
				
			}
			
			g2d.fill(displayArea);

		} catch (Exception ex) {

			ex.printStackTrace();

		}
	}

	public void generateDisplayArea() {

		try {

			// Copy the area.
			displayArea = new Area(halfArea);

			// Mirror horizontally.
			displayArea.add(AreaUtils.mirrorAlongX((int) displayArea.getBounds2D().getMinX(), displayArea));
			displayArea = AreaUtils.translateToTopLeft(displayArea);

			// Translate to center point.
			displayArea = AreaUtils.translateToPoint(displayArea,
					new Point2D.Double(center.getX() - (displayArea.getBounds2D().getWidth() / 2),
							center.getY() - (displayArea.getBounds2D().getHeight() / 2)));

			halfArea = AreaUtils.translateToTopLeft(halfArea);

			halfArea = AreaUtils.translateToPoint(halfArea,
					new Point2D.Double(center.getX(), center.getY() - (halfArea.getBounds2D().getHeight() / 2)));

			// bounds = AreaUtils.getOutline(displayArea);

		} catch (Exception ex) {

			ex.printStackTrace();

		}

	}

	public void random() {

		try {

			this.halfArea = ShipUtils.generate();
			generateDisplayArea();

		} catch (Exception ex) {

			ex.printStackTrace();

		}

	}

	public void add(Point2D.Double mousePos) {

		try {

			if (this.halfArea.getBounds2D().contains(mousePos)) {

				GeometryUtils.addBlockAtPoint(this.halfArea, mousePos);

			} else {

				GeometryUtils.addRandomBlock(this.halfArea);

			}

			generateDisplayArea();

		} catch (Exception ex) {

			ex.printStackTrace();

		}

	}

	public void subtract(Point2D.Double mousePos) {

		try {

			if (this.halfArea.getBounds2D().contains(mousePos)) {

				GeometryUtils.subtractBlockAtPoint(this.halfArea, mousePos);

			} else {

				GeometryUtils.subtractRandomBlock(this.halfArea);

			}

			generateDisplayArea();

		} catch (Exception ex) {

			ex.printStackTrace();

		}
	}

	public void subtractRandomLine() {

		try {

			GeometryUtils.subtractRandomLine(this.halfArea);
			generateDisplayArea();

		} catch (Exception ex) {

			ex.printStackTrace();

		}
	}

	public void flip() {

		try {

			this.halfArea = AreaUtils.flipVertically(this.halfArea);
			generateDisplayArea();

		} catch (Exception ex) {

			ex.printStackTrace();

		}
	}

	public boolean containsPoint(Point2D.Double point) {

		boolean result = false;

		if (displayArea.getBounds2D().contains(point)) {

			result = true;

		}

		return result;

	}

	public void save() {

		// Copy the area.
		Area copy = new Area(displayArea);
		copy = AreaUtils.translateToTopLeft(copy);

		ImageUtils.save(ImageUtils.drawArea(copy), "png", this.getIdentifier());

	}

	public void move(Point2D.Double newCenter) {

		this.center = newCenter;
		generateDisplayArea();

	}

	public String getIdentifier() {

		String result = identifier;

		if (name != null) {

			result = name;

		}

		return result;

	}
}

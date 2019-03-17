package ajb.app;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;

import ajb.examples.helpers.LookAndFeelUtils;
import ajb.examples.helpers.Starfield;
import ajb.framework.Base2DFramework;
import ajb.game.GameLoop;
import ajb.images.ImageUtils;
import ajb.interfaces.Loop;
import ajb.model.Vessel;
import ajb.random.RandomGibberish;
import ajb.random.RandomInt;
import ajb.ships.ShipUtils;

@SuppressWarnings("serial")
public class ShipDesigner extends Base2DFramework implements Loop {

	@SuppressWarnings("unused")
	public static void main(String[] args) {

		LookAndFeelUtils.setNimbusLookAndfeel();
		ShipDesigner app = new ShipDesigner();

	}

	Starfield starfield = null;
	GameLoop loop = new GameLoop(this);
	List<Vessel> vessels = new ArrayList<Vessel>();
	Vessel selectedVessel = null;
	JFrame frame = null;
	int windowedWidth = 1024;
	int windowedHeight = 768;
	boolean displayHelp = true;
	Point2D.Double clickPoint = null;
	int scale = 1;

	public ShipDesigner() {

		super();

		setBackground(Color.decode("#242424"));

		frame = new JFrame();
		frame.setTitle("Example");
		frame.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		frame.setSize(windowedWidth, windowedHeight);
		frame.setLocationRelativeTo(null);
		frame.add(this);

		frame.addKeyListener(this);
		frame.addComponentListener(this);

		this.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		
		frame.setVisible(true);

		loop.go();

	}

	@Override
	public void doLogic(double delta) {

		if (starfield != null) {

			starfield.twinkle();

		}

	}

	@Override
	public void render() {

		this.repaint();

	}

	@Override
	public void drawBeforeTransform(Graphics2D g) {

		super.drawBeforeTransform(g);

		if (starfield != null) {

			starfield.draw(g);

		}

	}

	@Override
	public void paint(Graphics g) {
		
		super.paint(g);

		Graphics2D gr = (Graphics2D) g;

		for (Vessel vessel : vessels) {

			vessel.draw(gr);

		}

	}

	@Override
	public void keyPressed(KeyEvent e) {

		try {

			if (e.getKeyCode() == KeyEvent.VK_HOME) {

				moveToShip();

			} else if (e.getKeyCode() == KeyEvent.VK_SPACE) {

				Vessel vessel = selectedVessel;

				if (vessel != null) {

					selectedVessel.random();

				}

			} else if (e.getKeyCode() == KeyEvent.VK_F11) {

				if (!frame.isUndecorated()) {

					GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
					int width = gd.getDisplayMode().getWidth();
					int height = gd.getDisplayMode().getHeight();

					frame.dispose();
					frame.setVisible(false);
					frame.setUndecorated(true);
					frame.setSize(width, height);
					frame.setLocationRelativeTo(null);
					frame.setVisible(true);

				} else {

					frame.dispose();
					frame.setVisible(false);
					frame.setUndecorated(false);
					frame.setSize(windowedWidth, windowedHeight);
					frame.setVisible(true);

				}

			} else if (e.getKeyCode() == KeyEvent.VK_A) {

				Vessel vessel = selectedVessel;

				if (vessel != null) {

					Point mousePos = MouseInfo.getPointerInfo().getLocation();
					SwingUtilities.convertPointFromScreen(mousePos, this);
					
					vessel.add(transformPoint(new Point2D.Double(mousePos.getX(), mousePos.getY())));

				}

			} else if (e.getKeyCode() == KeyEvent.VK_S) {

				Vessel vessel = selectedVessel;

				if (vessel != null) {

					Point mousePos = MouseInfo.getPointerInfo().getLocation();
					SwingUtilities.convertPointFromScreen(mousePos, this);
					
					vessel.subtract(transformPoint(new Point2D.Double(mousePos.getX(), mousePos.getY())));

				}

			} else if (e.getKeyCode() == KeyEvent.VK_D) {

				Vessel vessel = selectedVessel;

				if (vessel != null) {

					vessel.subtractRandomLine();

				}

			} else if (e.getKeyCode() == KeyEvent.VK_F) {

				Vessel vessel = selectedVessel;

				if (vessel != null) {

					vessel.flip();

				}

			}

		} catch (

		Exception ex) {

			ex.printStackTrace();

		}

	}

	@Override
	public void mouseDragged(MouseEvent e) {

		if (SwingUtilities.isMiddleMouseButton(e)) {

			moveCamera(e);

		} else if (SwingUtilities.isLeftMouseButton(e) && selectedVessel != null) {

			try {

				selectedVessel.move(transformPoint(new Point2D.Double(e.getX(), e.getY())));

			} catch (NoninvertibleTransformException ex) {

				ex.printStackTrace();

			}
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {

		dragStartScreen = new Point2D.Double(e.getPoint().getX(), e.getPoint().getY());
		dragEndScreen = null;

		if (e.getButton() == MouseEvent.BUTTON1 || e.getButton() == MouseEvent.BUTTON3) {

			selectedVessel = null;

			try {

				clickPoint = transformPoint(new Point2D.Double(e.getX(), e.getY()));

			} catch (NoninvertibleTransformException e1) {

				e1.printStackTrace();

			}

			for (Vessel vessel : vessels) {

				if (vessel.containsPoint(clickPoint)) {

					vessel.selected = true;
					selectedVessel = vessel;

				} else {

					vessel.selected = false;

				}

			}

			if (selectedVessel != null) {

				vessels.remove(selectedVessel);
				vessels.add(selectedVessel);

			}
		}

		if (e.getButton() == MouseEvent.BUTTON3) {

			JPopupMenu myPopupMenu = new JPopupMenu();

			if (selectedVessel == null) {

				JMenuItem newVessel = new JMenuItem(new AbstractAction("Create Ship") {
					public void actionPerformed(ActionEvent ae) {

						Vessel vessel = new Vessel(ShipUtils.generate(), clickPoint);
						vessel.selected = true;
						selectedVessel = vessel;
						vessels.add(vessel);

					}
				});

				myPopupMenu.add(newVessel);

				JSeparator separator = new JSeparator();
				myPopupMenu.add(separator);

				JMenuItem saveAll = new JMenuItem(new AbstractAction("Save All") {
					public void actionPerformed(ActionEvent ae) {

						for (Vessel vessel : vessels) {

							vessel.save();

						}

					}
				});
				myPopupMenu.add(saveAll);

				JSeparator separator1 = new JSeparator();
				myPopupMenu.add(separator1);

				JMenuItem load = new JMenuItem(new AbstractAction("Load") {
					public void actionPerformed(ActionEvent ae) {

						try {

							// Create a file chooser
							final JFileChooser fc = new JFileChooser();

							fc.setCurrentDirectory(new File("."));
							fc.setMultiSelectionEnabled(true);

							// In response to a button click:
							int returnVal = fc.showOpenDialog(frame);

							if (returnVal == JFileChooser.APPROVE_OPTION) {

								for (File file : fc.getSelectedFiles()) {

									try {

										Vessel vessel = new Vessel(
												ImageUtils.createAreaFromImage(ImageUtils.getImage(file)), clickPoint);
										vessels.add(vessel);

									} catch (Exception ex) {

										ex.printStackTrace();

									}
								}
							}

						} catch (Exception ex) {

							ex.printStackTrace();

						}
					}
				});

				myPopupMenu.add(load);

			} else {

				JMenuItem identifier = new JMenuItem(selectedVessel.getIdentifier());
				myPopupMenu.add(identifier);

				JSeparator separator = new JSeparator();
				myPopupMenu.add(separator);

				JMenu nameSubMenu = new JMenu("Name");
				JMenuItem assignName = new JMenuItem(new AbstractAction("Manual") {
					public void actionPerformed(ActionEvent ae) {

						selectedVessel.name = JOptionPane.showInputDialog("Name: ");

					}
				});

				JMenuItem randomName = new JMenuItem(new AbstractAction("Random") {
					public void actionPerformed(ActionEvent ae) {

						selectedVessel.name = RandomGibberish.anyRandomGibberish(RandomInt.anyRandomIntRange(2, 3));

					}
				});

				nameSubMenu.add(assignName);
				nameSubMenu.add(randomName);

				myPopupMenu.add(nameSubMenu);

				JSeparator separator1 = new JSeparator();
				myPopupMenu.add(separator1);

				JMenuItem save = new JMenuItem(new AbstractAction("Save") {
					public void actionPerformed(ActionEvent ae) {

						selectedVessel.save();

					}
				});
				myPopupMenu.add(save);

				JSeparator separator2 = new JSeparator();
				myPopupMenu.add(separator2);

				JMenuItem deleteVessel = new JMenuItem(new AbstractAction("Delete") {
					public void actionPerformed(ActionEvent ae) {

						vessels.remove(selectedVessel);
						selectedVessel = null;

					}
				});

				myPopupMenu.add(deleteVessel);

				JSeparator separator3 = new JSeparator();
				myPopupMenu.add(separator3);

				JMenuItem cloneVessel = new JMenuItem(new AbstractAction("Clone") {
					public void actionPerformed(ActionEvent ae) {

						Vessel newVessel = new Vessel(new Area(selectedVessel.halfArea), selectedVessel.center);
						vessels.add(newVessel);

					}
				});

				myPopupMenu.add(cloneVessel);

				List<Area> intersectingAreas = intersectingVessels(selectedVessel);
				
				if (intersectingAreas.size() > 1) {
					
					JMenuItem mergeVessel = new JMenuItem(new AbstractAction("Merge") {
						public void actionPerformed(ActionEvent ae) {
	
							Area newArea = new Area();
							
							for (Area area : intersectingAreas) {
								
								newArea.add(area);
								
							}
							
							Vessel newVessel = new Vessel(newArea, selectedVessel.center);
							vessels.add(newVessel);
	
						}
					});
	
					myPopupMenu.add(mergeVessel);		
				}

			}

			myPopupMenu.show(this, e.getX(), e.getY());

		}

	}
	
	private List<Area> intersectingVessels(Vessel selectedVessel) {
		
		List<Area> result = new ArrayList<Area>();
		result.add(selectedVessel.halfArea);
		
		for (Vessel vessel : vessels) {
			
			if (!vessel.identifier.equals(selectedVessel.identifier) && vessel.halfArea.intersects(selectedVessel.halfArea.getBounds2D())) {
				
				result.add(vessel.halfArea);
				
			}
			
		}
		
		return result;
		
	}

	private void moveToShip() {

		Vessel vessel = selectedVessel;

		if (vessel != null) {

			try {

				moveToPoint(transformPoint(
						new Point2D.Double((this.getWidth() / 2) - (vessel.center.getX() * coordTransform.getScaleX()),
								(this.getHeight() / 2) - (vessel.center.getY() * coordTransform.getScaleY()))));

			} catch (NoninvertibleTransformException e1) {

				e1.printStackTrace();

			}

		}

	}

	@Override
	public void componentResized(ComponentEvent arg0) {

		starfield = new Starfield(0, 0, this.getWidth(), this.getHeight(), 100, 200);
		moveToShip();

	}

}
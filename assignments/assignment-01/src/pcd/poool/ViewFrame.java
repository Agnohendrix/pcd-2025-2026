package pcd.poool;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.*;

public class ViewFrame extends JFrame {
    
    private VisualiserPanel panel;
    private ViewModel model;
    private RenderSynch sync;
    
    private Board board;
    
    public ViewFrame(ViewModel model, Board board, int w, int h){
    	this.model = model;
    	this.board = board;
    	this.sync = new RenderSynch();
    	setTitle("Sketch 03");
        setSize(w,h + 25);
        setResizable(false);
        panel = new VisualiserPanel(w,h);
        getContentPane().add(panel);
        
        addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent event) {
				if (event.getKeyCode() == KeyEvent.VK_UP) {
					new UpCommand().execute(board);
				}
			}
		});
        
		setFocusable(true);
		setFocusTraversalKeysEnabled(false);
		requestFocusInWindow();
        
        addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent ev){
				System.exit(-1);
			}
			public void windowClosed(WindowEvent ev){
				System.exit(-1);
			}
		});
    }
     
    public void render(){
		long nf = sync.nextFrameToRender();
        panel.repaint();
		try {
			sync.waitForFrameRendered(nf);
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
    }
        
    public class VisualiserPanel extends JPanel {
        private int ox;
        private int oy;
        private int delta;
        
        public VisualiserPanel(int w, int h){
            setSize(w,h + 25);
            ox = w/2;
            oy = h/2;
            delta = Math.min(ox, oy);
        }

        public void paint(Graphics g){
    		Graphics2D g2 = (Graphics2D) g;
    		
    		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
    		          RenderingHints.VALUE_ANTIALIAS_ON);
    		g2.setRenderingHint(RenderingHints.KEY_RENDERING,
    		          RenderingHints.VALUE_RENDER_QUALITY);
    		g2.clearRect(0,0,this.getWidth(),this.getHeight());
            
    		g2.setColor(Color.LIGHT_GRAY);
		    g2.setStroke(new BasicStroke(1));
    		g2.drawLine(ox,0,ox,oy*2);
    		g2.drawLine(0,oy,ox*2,oy);
    		
    		drawHole(g2, model.getPlayerHole(), Color.BLUE);
		    drawHole(g2, model.getBotHole(), Color.RED);
		    
    		g2.setColor(Color.BLACK);
    		
    		    g2.setStroke(new BasicStroke(1));
	    		for (var b: model.getBalls()) {
	    			var p = b.pos();
	            	int x0 = (int)(ox + p.x()*delta);
	                int y0 = (int)(oy - p.y()*delta);
	                int radiusX = (int)(b.radius()*delta);
	                int radiusY = (int)(b.radius()*delta);
	                g2.drawOval(x0 - radiusX,y0 - radiusY,radiusX*2,radiusY*2);
	    		}
	
    		    g2.setStroke(new BasicStroke(3));
    		    g2.setColor(Color.BLUE);
	    		var pb = model.getPlayerBall();
	    		if (pb != null) {
					var p1 = pb.pos();
		        	int x0 = (int)(ox + p1.x()*delta);
		            int y0 = (int)(oy - p1.y()*delta);
	                int radiusX = (int)(pb.radius()*delta);
	                int radiusY = (int)(pb.radius()*delta);
	                g2.drawOval(x0 - radiusX,y0 - radiusY,radiusX*2,radiusY*2);
	    		}
	    		
	    		g2.setStroke(new BasicStroke(3));
	    		g2.setColor(Color.RED);
	    		var bb = model.getBotBall();
	    		if (bb != null) {
					var p1 = bb.pos();
		        	int x0 = (int)(ox + p1.x()*delta);
		            int y0 = (int)(oy - p1.y()*delta);
	                int radiusX = (int)(bb.radius()*delta);
	                int radiusY = (int)(bb.radius()*delta);
	                g2.drawOval(x0 - radiusX,y0 - radiusY,radiusX*2,radiusY*2);
	    		}
    		    
	    		g2.setStroke(new BasicStroke(1));
			    g2.setColor(Color.BLUE);
			    g2.drawString("Player: " + model.getPlayerScore(), 20, 200);
			    g2.setColor(Color.RED);
			    g2.drawString("Bot: " + model.getBotScore(), getWidth() - 80, 220);
			    g2.setColor(Color.BLACK);
			    g2.drawString("Num small balls: " + model.getBalls().size(), 20, 240);
			    g2.drawString("Frame per sec: " + model.getFramePerSec(), 20, 260);

	    		sync.notifyFrameRendered();
    		
        }
        
        private void drawHole(Graphics2D g2, Hole hole, Color color) {
			var p = hole.pos();
			int x = (int) (ox + p.x() * delta);
			int y = (int) (oy - p.y() * delta);
			int radius = (int) (hole.radius() * delta);
			g2.setColor(color);
			g2.fillOval(x - radius, y - radius, radius * 2, radius * 2);
		}
        
    }
}

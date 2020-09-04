package lcip;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.geometry.jts.LiteShape;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.util.AffineTransformation;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import tools.ScreenImage;
import javax.swing.JCheckBox;

/*
 * Authors: José Duarte 	(hfduarte@ua.pt)
 * 			Mark Mckenney 	(marmcke@siue.edu)
 * 
 * Version: 1.0 (2020-05-29)
 * 
 */

public class Rip extends JFrame
{
	private static final long serialVersionUID = 1L;
	
	private JPanel contentPane;
	private JPanel viz_p;
	private JFrame f;
	private JPanel jp;
	private JLabel status;
	private JPanel tools_p;
	private JLabel zoom_level;
	
	public static final double DEFAULT_ZOOM_MULTIPLICATION_FACTOR = 0.25;
	public static final double DEFAULT_MIN_ZOOM_LEVEL = 0.1;
	public static final double DEFAULT_MAX_ZOOM_LEVEL = 600;
	
    private double zoomLevel = 1.0;
    private double minZoomLevel = DEFAULT_MIN_ZOOM_LEVEL;
    private double maxZoomLevel = DEFAULT_MAX_ZOOM_LEVEL;
    private double zoomMultiplicationFactor = DEFAULT_ZOOM_MULTIPLICATION_FACTOR;
    private GeometryFactory geometryFactory;
    private WKTReader reader; 
	private int w_center;
	private int h_center;
	private double cx = 0;
	private double cy = 0;
    private JSlider zoom_slider;
    private JButton wkt_save_bt;
    private JTextField wkt_save_in_file;
    private JButton save_to_picture;
    
	private JLabel src_wkt_lb;
	private JLabel target_wkt_lb;
	private JButton clear_bt;
	
    private JTextField zoom_factor_tx;
    private JTextArea p_wkt_tx;
    private JButton show_geometries_btn;
    private JTextArea q_wkt_tx;    
    private JTextField png_filename_tx;
    private JButton center_geoms_btn;
    
    private JButton show_points_btn;
    private JTextField maker_radius_tx;
    
    private boolean show_geoms;
    private boolean show_test;
    
    private String sc_geom_wkt;
    private String tg_geom_wkt;
    private JButton show_subtitle_btn;
    private boolean show_subtitle;
    private boolean show_aligned_geoms;
    private String[] b_arr;
    private int curr_boundary_line;
	private JSlider inter_slider;
	
	private String[] geometries;
	private int geom_to_show_id;
	private int num_invalid_geoms;
	private int num_complex_geoms;
	private JTextField n_obs_tx;
	private JButton save_seq_to_picture;
	private JTextField gran_tx;
	private JButton seg_to_conc_btn;
	private JLabel n_obs_lb;
	private boolean geoms_validity;
	private JButton test_btn;
	private JCheckBox fill_cb;
	private JButton load_wkt;
	private JButton ice2_wkt;
	private int op;
	private int ice1;
	private JButton ice1_wkt;
	private JTextField op_tx;
	private JButton hole_wkt;
	private JButton rplane_wkt;
	
	public Rip() 
	{
		f = this;
				
		curr_boundary_line = 0;
		
		show_aligned_geoms = false;
		geometries = null;
		geom_to_show_id = 0;
		num_invalid_geoms = 0;
		num_complex_geoms = 0;
		
		geoms_validity = false;
		op = 0;
		ice1 = 0;
			    
	    b_arr = null;
	    		
	    show_geoms = false;
	    show_test = false;
	    
	    show_subtitle = false;
	    
	    sc_geom_wkt = null;
	    tg_geom_wkt = null;
				
	    geometryFactory = JTSFactoryFinder.getGeometryFactory(null);
	    reader = new WKTReader(geometryFactory);
			    
		draw_UI();
		
		w_center = (int) (viz_p.getWidth() / 2);
		h_center = (int) (viz_p.getHeight() / 2);
			    
		add_listeners();
		
		draw_geometry();
	}
	
	public void paint(Graphics g) 
	{
	    super.paint(g);
	    jp.repaint();
	}

	public void draw_geometry()
	{
		viz_p.setLayout(new BorderLayout());
		jp = new DrawGraphs();
		viz_p.add(jp, BorderLayout.CENTER);
	}
	
    private class DrawGraphs extends JPanel
    {
    	private int dx = 0;
    	private int dy = 0;
    	
    	private double sx = 1;
    	private double sy = 1; 	
    	   	
    	private int xx = 0;
    	private int yy = 0;
    	
    	private int _dx = 0;
    	private int _dy = 0;
    	
		private static final long serialVersionUID = 3203545490664689791L;
		
		public DrawGraphs() 
		{
			setBackground(Color.white);
			
			setAlignmentY(CENTER_ALIGNMENT);
			setAlignmentX(CENTER_ALIGNMENT);
						
	        addMouseListener(new MouseAdapter() 
	        {
	            public void mousePressed(MouseEvent e) 
	            {
	            	xx = e.getX();
	            	yy = e.getY();
	            }
	        });

	        addMouseMotionListener(new MouseAdapter() 
	        {
	            public void mouseDragged(MouseEvent e) 
	            {        	
	            	int ddx = (e.getX() - xx);
	            	int ddy = (e.getY() - yy);
	            	
	            	translate(ddx, ddy);
	            	repaint();
	            	
	            	xx = e.getX();
	            	yy = e.getY();
	            }
	        });
	        
	        addMouseWheelListener(new MouseAdapter()
	        {
	            public void mouseWheelMoved(MouseWheelEvent e) 
	            {
		            int notches = e.getWheelRotation();				
					zoomMultiplicationFactor = Double.parseDouble(zoom_factor_tx.getText());
					
		            if (notches > 0) 
		            {
		            	if (zoomLevel < maxZoomLevel) 
		            	{
		            		zoomLevel += zoomMultiplicationFactor; 
			            	scale(zoomMultiplicationFactor, zoomMultiplicationFactor);
		            	}
		            } else {
			           	if (zoomLevel > minZoomLevel) 
			           	{
			           		zoomLevel -= zoomMultiplicationFactor;
							scale(-zoomMultiplicationFactor, -zoomMultiplicationFactor);
			            }
		            }

		            if(b_arr != null)
						to_origin(true);
		            else
		            	center_geometries(true);
		            
		            repaint();
	            }
	        });       
		};
		
		@Override
        protected void paintComponent(Graphics g)
		{
			super.paintComponent(g);

	        Graphics2D gr = (Graphics2D) g;
	        gr.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	        
		    gr.setStroke(new BasicStroke(1.0f));	        
		    gr.setPaint(Color.blue);

	        AffineTransform at = new AffineTransform();

	        at.translate(dx, dy);
	        
	        zoom_level.setText("Zoom Level: " + sx);

			try 
			{	
				if(show_test)
				{
		    		Geometry geometry = null;
		    		
					AffineTransformation affine = new AffineTransformation();
					affine.scale(sx, -sy);
		    		
			    	// P
			    	geometry = reader.read("LineString (-1.25102040816326543 -0.73877551020408183, 0.93673469387755137 -0.59183673469387776)");
					geometry.apply(affine);;
			    	
					gr.setPaint(new Color(0.2f, 0.2f, 0.2f, 1.0f));
			    	gr.draw(new LiteShape(geometry, at, false));
									    	
			    	// Q
			    	//geometry = reader.read("LineString (-1.3040816326530611 -0.17551020408163276, -0.83469387755102042 -0.26938775510204094, -0.77346938775510199 -0.06938775510204098, -0.86734693877551017 0.02448979591836731, -0.94489795918367347 -0.01224489795918382, -0.97346938775510194 0.04081632653061218, -0.86734693877551017 0.1020408163265305, -0.78163265306122454 0.02857142857142847, -0.71632653061224483 0.00816326530612232, -0.67959183673469381 0.10612244897959178, -0.76530612244897966 0.19999999999999996, -1.00612244897959191 0.17551020408163254, -1.1489795918367347 0.06122448979591832, -1.15714285714285703 0.09387755102040807, -1.04285714285714293 0.19183673469387752, -0.89183673469387759 0.2326530612244897, -1.01428571428571423 0.33877551020408159, -0.9285714285714286 0.35510204081632646, -0.83061224489795915 0.23673469387755097, -0.66326530612244894 0.2204081632653061, -0.6020408163265305 0.14285714285714279, -0.51632653061224487 0.20408163265306112, -0.41428571428571415 0.30204081632653057, -0.25102040816326521 0.26938775510204072, -0.17755102040816317 0.33061224489795915, -0.0428571428571427 0.39591836734693875, 0.03061224489795933 0.25714285714285712, -0.02653061224489806 0.25306122448979584, -0.03877551020408143 0.31836734693877544, -0.15714285714285703 0.28979591836734686, -0.18571428571428572 0.19999999999999996, -0.09591836734693882 0.06122448979591832, 0.00612244897959213 -0.05714285714285716, 0.16530612244897958 0.03265306122448974, 0.21020408163265314 0.1673469387755101, 0.16938775510204085 0.30204081632653057, 0.10000000000000009 0.39999999999999991, -0.11224489795918369 0.47755102040816322, -0.29999999999999982 0.42040816326530606, -0.29591836734693877 0.47346938775510194, -0.13673469387755088 0.52244897959183667, 0.12040816326530601 0.45714285714285707, 0.27551020408163263 0.25714285714285712, 0.24285714285714288 0.01632653061224476, 0.02653061224489806 -0.1183673469387756, -0.07551020408163245 -0.21632653061224505, 0.14897959183673493 -0.33061224489795937, 0.83061224489795915 -0.05306122448979611)");
			    	//geometry = reader.read("LineString (-1.3040816326530611 -0.17551020408163276, -0.83469387755102042 -0.26938775510204094, -0.77346938775510199 -0.06938775510204098, -0.86734693877551017 0.02448979591836731, -0.94489795918367347 -0.01224489795918382, -0.97346938775510194 0.04081632653061218, -0.86734693877551017 0.1020408163265305, -0.78163265306122454 0.02857142857142847, -0.71632653061224483 0.00816326530612232, -0.67959183673469381 0.10612244897959178, -0.76530612244897966 0.19999999999999996, -0.66326530612244894 0.2204081632653061, -0.6020408163265305 0.14285714285714279, -0.51632653061224487 0.20408163265306112, -0.41428571428571415 0.30204081632653057, -0.25102040816326521 0.26938775510204072, -0.17755102040816317 0.33061224489795915, -0.15714285714285703 0.28979591836734686, -0.18571428571428572 0.19999999999999996, -0.09591836734693882 0.06122448979591832, 0.00612244897959213 -0.05714285714285716, 0.16530612244897958 0.03265306122448974, 0.24285714285714288 0.01632653061224476, 0.02653061224489806 -0.1183673469387756, -0.07551020408163245 -0.21632653061224505, 0.14897959183673493 -0.33061224489795937, 0.83061224489795915 -0.05306122448979611)");
			    	geometry = reader.read("LineString (-1.3040816326530611 -0.17551020408163276, -0.83469387755102042 -0.26938775510204094, -0.77346938775510199 -0.06938775510204098, -0.86734693877551017 0.02448979591836731, -0.94489795918367347 -0.01224489795918382, -0.97346938775510194 0.04081632653061218, -0.86734693877551017 0.1020408163265305, -0.78163265306122454 0.02857142857142847, -0.72123661799388505 0.03752937718888849, -0.67959183673469381 0.10612244897959178, -0.74519746809134857 0.19218577327251635, -0.67549317689872757 0.24446399166698207, -0.6020408163265305 0.14285714285714279, -0.51632653061224487 0.20408163265306112, -0.41628034402616809 0.25971180536536792, -0.2790500207406954 0.25317702806605974, -0.18571428571428572 0.19999999999999996, -0.09591836734693882 0.06122448979591832, -0.06775888806306307 -0.0539575050014266, -0.05904585166398535 -0.12366179619404771, -0.07551020408163245 -0.21632653061224505, 0.14897959183673493 -0.33061224489795937, 0.83061224489795915 -0.05306122448979611)");
			    	
			    	geometry.apply(affine);;
			    	
					gr.setPaint(new Color(0.75f, 0.2f, 0.2f, 1.0f));
			    	gr.draw(new LiteShape(geometry, at, false));
			    	
			    	geometry = reader.read("Polygon ((-0.65918367346938767 -0.19183673469387763, -0.65510204081632639 -0.00408163265306127, -0.59795918367346923 0.07346938775510192, -0.56530612244897949 -0.02448979591836742, -0.5244897959183672 -0.00816326530612255, -0.55306122448979589 0.1020408163265305, -0.42244897959183669 0.18775510204081625, -0.26734693877551008 0.18775510204081625, -0.15306122448979576 0.05306122448979589, -0.25102040816326521 -0.01224489795918382, -0.27142857142857135 0.06938775510204076, -0.32448979591836724 0.06938775510204076, -0.29999999999999982 -0.04897959183673484, -0.25510204081632648 -0.11020408163265305, -0.15306122448979576 -0.09387755102040818, -0.16530612244897958 -0.24081632653061247, -0.34897959183673466 -0.25306122448979607, -0.41428571428571415 -0.17142857142857149, -0.50408163265306127 -0.13877551020408174, -0.58571428571428563 -0.22448979591836737, -0.65918367346938767 -0.19183673469387763))");
					geometry.apply(affine);;
			    	
					gr.setPaint(new Color(0.75f, 0.2f, 0.2f, 1.0f));
			    	gr.draw(new LiteShape(geometry, at, false));
				}
				
				else if (show_geoms || show_aligned_geoms)
			    {			    	
			    	if(sc_geom_wkt != null && sc_geom_wkt.length() > 0 && tg_geom_wkt != null && tg_geom_wkt.length() > 0)
			    	{
			    		Geometry geometry = null;
			    		
						AffineTransformation affine = new AffineTransformation();
						affine.scale(sx, -sy);
			    		
				    	// P
				    	geometry = reader.read(sc_geom_wkt);
						geometry.apply(affine);;
				    	
						gr.setPaint(new Color(0.2f, 0.2f, 0.2f, 1.0f));
				    	gr.draw(new LiteShape(geometry, at, false));
										    	
				    	// Q
				    	geometry = reader.read(tg_geom_wkt);
						geometry.apply(affine);;
				    	
						gr.setPaint(new Color(0.75f, 0.2f, 0.2f, 1.0f));
				    	gr.draw(new LiteShape(geometry, at, false));
			    	}
			    }

			    if(geometries != null)
			    {			    	
			    	Geometry geometry = reader.read(geometries[geom_to_show_id]);
				    					    	
					AffineTransformation affine = new AffineTransformation();
					affine.scale(sx, -sy);
					geometry.apply(affine);;
				    	
					gr.setPaint(Color.black);
				    gr.draw(new LiteShape(geometry, at, false));
				    
   					if(fill_cb.isSelected())
   					{
   						//gr.setPaint(Color.blue);
   						gr.setPaint(new Color(18, 21, 67));
   						
		   		        for (int j = 0; j < geometry.getNumGeometries(); j++)
				   			gr.fill(new LiteShape(geometry.getGeometryN(j), at, false));
   					}
				    
		   		    gr.setFont(new Font("Arial", Font.BOLD, 14));
					gr.setPaint(new Color(0.75f, 0.2f, 0.2f, 1.0f));
					gr.drawString("Nº Invalid Geoms: " + String.valueOf(num_invalid_geoms), 20, 110);
					gr.drawString("Nº complex Geoms: " + String.valueOf(num_complex_geoms), 20, 130);  
					
					if(geoms_validity)
						gr.setPaint(new Color(6, 40, 94));
					else
						gr.setPaint(new Color(166, 10, 83));
							
					gr.setFont(new Font("Arial", Font.BOLD, 14));
							
					if(geoms_validity)
						gr.drawString("Valid Transformation!", 20, 90);
					else
						gr.drawString("Invalid Transformation!", 20, 90);
			    }
			    
			    if(show_subtitle)
			    {		
					gr.setFont(new Font("Arial", Font.BOLD, 14));
					
					gr.setPaint(new Color(0.2f, 0.2f, 0.2f, 1.0f));
					gr.drawString("Source", 20, 30);
					
					gr.setPaint(new Color(0.75f, 0.2f, 0.2f, 1.0f));
					gr.drawString("target", 20, 50);		
			    }
			} 
			catch (ParseException e) 
			{
				e.printStackTrace();
			}
        };
		        
        public void translate(int x, int y) 
        {
        	_dx += x;
        	_dy += y;
        	
        	dx += x;
        	dy += y;
        }
        
        public void scale(double x, double y) 
        {
        	sx += x;
        	sy += y;
        }
                
        public void center_geometries(boolean translate)
        {        	
        	if(show_test)
        	{
				try
				{				
				    Geometry g1 = reader.read("LineString (-1.25102040816326543 -0.73877551020408183, 0.93673469387755137 -0.59183673469387776)");
				    //Geometry g2 = reader.read("LineString (-1.3040816326530611 -0.17551020408163276, -0.83469387755102042 -0.26938775510204094, -0.77346938775510199 -0.06938775510204098, -0.86734693877551017 0.02448979591836731, -0.94489795918367347 -0.01224489795918382, -0.97346938775510194 0.04081632653061218, -0.86734693877551017 0.1020408163265305, -0.78163265306122454 0.02857142857142847, -0.71632653061224483 0.00816326530612232, -0.67959183673469381 0.10612244897959178, -0.76530612244897966 0.19999999999999996, -1.00612244897959191 0.17551020408163254, -1.1489795918367347 0.06122448979591832, -1.15714285714285703 0.09387755102040807, -1.04285714285714293 0.19183673469387752, -0.89183673469387759 0.2326530612244897, -1.01428571428571423 0.33877551020408159, -0.9285714285714286 0.35510204081632646, -0.83061224489795915 0.23673469387755097, -0.66326530612244894 0.2204081632653061, -0.6020408163265305 0.14285714285714279, -0.51632653061224487 0.20408163265306112, -0.41428571428571415 0.30204081632653057, -0.25102040816326521 0.26938775510204072, -0.17755102040816317 0.33061224489795915, -0.0428571428571427 0.39591836734693875, 0.03061224489795933 0.25714285714285712, -0.02653061224489806 0.25306122448979584, -0.03877551020408143 0.31836734693877544, -0.15714285714285703 0.28979591836734686, -0.18571428571428572 0.19999999999999996, -0.09591836734693882 0.06122448979591832, 0.00612244897959213 -0.05714285714285716, 0.16530612244897958 0.03265306122448974, 0.21020408163265314 0.1673469387755101, 0.16938775510204085 0.30204081632653057, 0.10000000000000009 0.39999999999999991, -0.11224489795918369 0.47755102040816322, -0.29999999999999982 0.42040816326530606, -0.29591836734693877 0.47346938775510194, -0.13673469387755088 0.52244897959183667, 0.12040816326530601 0.45714285714285707, 0.27551020408163263 0.25714285714285712, 0.24285714285714288 0.01632653061224476, 0.02653061224489806 -0.1183673469387756, -0.07551020408163245 -0.21632653061224505, 0.14897959183673493 -0.33061224489795937, 0.83061224489795915 -0.05306122448979611)");
				    //Geometry g2 = reader.read("LineString (-1.3040816326530611 -0.17551020408163276, -0.83469387755102042 -0.26938775510204094, -0.77346938775510199 -0.06938775510204098, -0.86734693877551017 0.02448979591836731, -0.94489795918367347 -0.01224489795918382, -0.97346938775510194 0.04081632653061218, -0.86734693877551017 0.1020408163265305, -0.78163265306122454 0.02857142857142847, -0.71632653061224483 0.00816326530612232, -0.67959183673469381 0.10612244897959178, -0.76530612244897966 0.19999999999999996, -0.66326530612244894 0.2204081632653061, -0.6020408163265305 0.14285714285714279, -0.51632653061224487 0.20408163265306112, -0.41428571428571415 0.30204081632653057, -0.25102040816326521 0.26938775510204072, -0.17755102040816317 0.33061224489795915, -0.15714285714285703 0.28979591836734686, -0.18571428571428572 0.19999999999999996, -0.09591836734693882 0.06122448979591832, 0.00612244897959213 -0.05714285714285716, 0.16530612244897958 0.03265306122448974, 0.24285714285714288 0.01632653061224476, 0.02653061224489806 -0.1183673469387756, -0.07551020408163245 -0.21632653061224505, 0.14897959183673493 -0.33061224489795937, 0.83061224489795915 -0.05306122448979611)");
				    Geometry g2 = reader.read("LineString (-1.3040816326530611 -0.17551020408163276, -0.83469387755102042 -0.26938775510204094, -0.77346938775510199 -0.06938775510204098, -0.86734693877551017 0.02448979591836731, -0.94489795918367347 -0.01224489795918382, -0.97346938775510194 0.04081632653061218, -0.86734693877551017 0.1020408163265305, -0.78163265306122454 0.02857142857142847, -0.72123661799388505 0.03752937718888849, -0.67959183673469381 0.10612244897959178, -0.74519746809134857 0.19218577327251635, -0.67549317689872757 0.24446399166698207, -0.6020408163265305 0.14285714285714279, -0.51632653061224487 0.20408163265306112, -0.41628034402616809 0.25971180536536792, -0.2790500207406954 0.25317702806605974, -0.18571428571428572 0.19999999999999996, -0.09591836734693882 0.06122448979591832, -0.06775888806306307 -0.0539575050014266, -0.05904585166398535 -0.12366179619404771, -0.07551020408163245 -0.21632653061224505, 0.14897959183673493 -0.33061224489795937, 0.83061224489795915 -0.05306122448979611)");
				    
					AffineTransformation affine = new AffineTransformation();
					
					/*
					 * Depending on the value of the coordinates, e.g., the y coordinates are negative, sy might be -sy.
					 */
					affine.scale(sx, sy);
					
					g1.apply(affine);
					g2.apply(affine);
				    
				    // Get centroids of the lines envelopes.
					
				    Point c1 = g1.getEnvelope().getCentroid();
				    Point c2 = g2.getEnvelope().getCentroid();
				    
					cx = (c1.getX() + c2.getX()) / 2;
					cy = (c1.getY() + c2.getY()) / 2;
				} 
				catch (ParseException e) {
					e.printStackTrace();
				}
	
				w_center = (int) (viz_p.getWidth() / 2);
				h_center = (int) (viz_p.getHeight() / 2);
				
				if(!translate)
				{
		        	_dx = 0;
		        	_dy = 0;
				}
				
				dx = (int) ((-cx + w_center)) + _dx;
				dy = (int) ((cy + h_center)) + _dy;    
        	}
        	else 
        	{
	          	if(sc_geom_wkt == null || tg_geom_wkt == null)
	        		return;
	    	    
	        	if(sc_geom_wkt.length() == 0 || tg_geom_wkt.length() == 0)
	        		return;
	        	        	
				try
				{				
				    Geometry g1 = reader.read(sc_geom_wkt);
				    Geometry g2 = reader.read(tg_geom_wkt);
					
					AffineTransformation affine = new AffineTransformation();
					
					/*
					 * Depending on the value of the coordinates, e.g., the y coordinates are negative, sy might be -sy.
					 */
					affine.scale(sx, sy);
					
					g1.apply(affine);
					g2.apply(affine);
				    
				    // Get centroids of the lines envelopes.
					
				    Point c1 = g1.getEnvelope().getCentroid();
				    Point c2 = g2.getEnvelope().getCentroid();
				    
					cx = (c1.getX() + c2.getX()) / 2;
					cy = (c1.getY() + c2.getY()) / 2;
				} 
				catch (ParseException e) {
					e.printStackTrace();
				}
	
				w_center = (int) (viz_p.getWidth() / 2);
				h_center = (int) (viz_p.getHeight() / 2);
				
				if(!translate)
				{
		        	_dx = 0;
		        	_dy = 0;
				}
				
				dx = (int) ((-cx + w_center)) + _dx;
				dy = (int) ((cy + h_center)) + _dy;      		
        	}
        }
        
        public void to_origin(boolean translate)
        {        	
        	if(b_arr == null)
        		return;
    	    
        	if(b_arr.length == 0)
        		return;
        	        	
			try
			{				
			    Geometry g1 = reader.read(b_arr[curr_boundary_line * 2]);
			    Geometry g2 = reader.read(b_arr[curr_boundary_line * 2 + 1]);
				
				AffineTransformation affine = new AffineTransformation();
				
				/*
				 * Depending on the value of the coordinates, e.g., the y coordinates are negative, sy might be -sy.
				 */
				affine.scale(sx, sy);
				
				g1.apply(affine);
				g2.apply(affine);
			    
			    // Get centroids of the lines envelopes.
				
			    Point c1 = g1.getEnvelope().getCentroid();
			    Point c2 = g2.getEnvelope().getCentroid();
			    
				cx = (c1.getX() + c2.getX()) / 2;
				cy = (c1.getY() + c2.getY()) / 2;
			} 
			catch (ParseException e) {
				e.printStackTrace();
			}

			w_center = (int) (viz_p.getWidth() / 2);
			h_center = (int) (viz_p.getHeight() / 2);
			
			if(!translate)
			{
	        	_dx = 0;
	        	_dy = 0;
			}
			
			dx = (int) ((-cx + w_center)) + _dx;
			dy = (int) ((cy + h_center)) + _dy;
        }
    }
	
	public void draw_UI() 
	{
		setTitle("Lines and Concavities Interpolation Problem");
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	    setSize(1282, 651);
	    setLocationRelativeTo(null);
	    setExtendedState(f.getExtendedState() | JFrame.MAXIMIZED_BOTH);
	    
		contentPane = new JPanel();
		contentPane.setToolTipText("");
		contentPane.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		viz_p = new JPanel();
		viz_p.setLocation(175, 10);
		
		viz_p.setSize(930, 590);
		
		viz_p.setBackground(Color.WHITE);
		viz_p.setBorder(new LineBorder(Color.black, 1));
		contentPane.add(viz_p);
		
		tools_p = new JPanel();
		tools_p.setBorder(new LineBorder(new Color(0, 0, 0)));
		tools_p.setBounds(10, 616, 1260, 36);
		contentPane.add(tools_p);
		
		zoom_factor_tx = new JTextField();
		zoom_factor_tx.setBackground(SystemColor.inactiveCaptionBorder);
		zoom_factor_tx.setForeground(SystemColor.desktop);
		zoom_factor_tx.setText("0.25");
		zoom_factor_tx.setToolTipText("Zoom factor.");
		tools_p.add(zoom_factor_tx);
		zoom_factor_tx.setColumns(3);
		
		gran_tx = new JTextField();
		gran_tx.setToolTipText("Zoom factor.");
		gran_tx.setText("10");
		gran_tx.setForeground(SystemColor.desktop);
		gran_tx.setColumns(3);
		gran_tx.setBackground(Color.LIGHT_GRAY);
		tools_p.add(gran_tx);
		
		save_seq_to_picture = new JButton("Save Sequence To PNG");
		tools_p.add(save_seq_to_picture);
		
		save_to_picture = new JButton("Save Current To PNG");
		tools_p.add(save_to_picture);
		
		png_filename_tx = new JTextField();
		png_filename_tx.setText("/home/user/");
		tools_p.add(png_filename_tx);
		png_filename_tx.setColumns(10);
		
		status = new JLabel("");
		tools_p.add(status);
		
		zoom_level = new JLabel("Zoom Level:");
		zoom_level.setBounds(1115, 10, 155, 14);
		contentPane.add(zoom_level);
		
		zoom_slider = new JSlider();
		zoom_slider.setValue(1);
		zoom_slider.setMinimum((int) minZoomLevel);
		zoom_slider.setMaximum((int) maxZoomLevel);
		zoom_slider.setBounds(1115, 38, 155, 26);
		contentPane.add(zoom_slider);
	    
	    wkt_save_in_file = new JTextField();
	    wkt_save_in_file.setEnabled(false);
	    wkt_save_in_file.setForeground(new Color(25, 25, 112));
	    wkt_save_in_file.setBackground(new Color(240, 255, 240));
	    wkt_save_in_file.setText("d:\\\\wkt.txt");
	    wkt_save_in_file.setBounds(11, 580, 155, 20);
	    contentPane.add(wkt_save_in_file);
	    wkt_save_in_file.setColumns(1);
	    
	    wkt_save_bt = new JButton("Save WKT");
	    wkt_save_bt.setEnabled(false);
	    wkt_save_bt.setToolTipText("Save to file.");
	    wkt_save_bt.setFont(new Font("Dialog", Font.BOLD, 12));
	    wkt_save_bt.setBounds(10, 551, 153, 25);
	    contentPane.add(wkt_save_bt);
			
		src_wkt_lb = new JLabel("Source WKT");
		src_wkt_lb.setHorizontalAlignment(SwingConstants.LEFT);
		src_wkt_lb.setForeground(new Color(25, 25, 112));
		src_wkt_lb.setFont(new Font("Dialog", Font.BOLD, 12));
		src_wkt_lb.setBounds(10, 67, 153, 16);
		contentPane.add(src_wkt_lb);
		
		clear_bt = new JButton("Clear");
		clear_bt.setForeground(new Color(0, 0, 128));
		clear_bt.setFont(new Font("Dialog", Font.BOLD, 12));
		clear_bt.setBounds(10, 464, 153, 23);
		contentPane.add(clear_bt);
		
		p_wkt_tx = new JTextArea();
		p_wkt_tx.setText("LineString (-0.75283631820074914 0.70959600166597325, 2.35466888796335061 0.64708871303623572)");
		p_wkt_tx.setBounds(10, 90, 153, 47);
		contentPane.add(p_wkt_tx);
		
		target_wkt_lb = new JLabel("Target WKT");
		target_wkt_lb.setHorizontalAlignment(SwingConstants.LEFT);
		target_wkt_lb.setForeground(new Color(25, 25, 112));
		target_wkt_lb.setFont(new Font("Dialog", Font.BOLD, 12));
		target_wkt_lb.setBounds(10, 149, 153, 16);
		contentPane.add(target_wkt_lb);
		
		q_wkt_tx = new JTextArea();
		q_wkt_tx.setText("LineString (-0.73497709287796686 1.13821740941274596, 0.28299875052061729 1.63827571845064712, 0.5062390670553949 1.29002082465639445, 0.24728029987505318 1.25430237401082989, 0.24728029987505318 1.42396501457726066, -0.0027488546438974 1.29002082465639445, 0.29192836318200843 1.04892128279883501, 0.72054977092878092 1.07571012078300843, 0.63125364431486997 1.45075385256143408, 0.20263223656809748 1.83472719700125109, 0.90807163681799397 1.71864223240316694, 0.88128279883382055 1.34359850062474084, 1.25632653061224664 1.1024989587671814, 2.05999167013744477 1.3793169512703054)");
		q_wkt_tx.setBounds(10, 172, 153, 47);
		contentPane.add(q_wkt_tx);
		
		show_geometries_btn = new JButton("Show Geoms");
		show_geometries_btn.setToolTipText("Show/Hide Geometries");
		show_geometries_btn.setForeground(new Color(0, 0, 128));
		show_geometries_btn.setBounds(10, 225, 153, 27);
		contentPane.add(show_geometries_btn);
		
		center_geoms_btn = new JButton("Center Geoms");
		center_geoms_btn.setToolTipText("Center Geometries in the middle of the screen.");
		center_geoms_btn.setForeground(new Color(0, 0, 128));
		center_geoms_btn.setFont(new Font("Dialog", Font.BOLD, 12));
		center_geoms_btn.setBounds(10, 255, 153, 25);
		contentPane.add(center_geoms_btn);
		
		show_points_btn = new JButton("Show Points");
		show_points_btn.setEnabled(false);
		show_points_btn.setFont(new Font("Dialog", Font.BOLD, 12));
		show_points_btn.setForeground(new Color(0, 0, 128));
		show_points_btn.setBounds(10, 490, 153, 25);
		contentPane.add(show_points_btn);
		
		maker_radius_tx = new JTextField();
		maker_radius_tx.setEnabled(false);
		maker_radius_tx.setToolTipText("Points Radius");
		maker_radius_tx.setText("1");
		maker_radius_tx.setBounds(10, 520, 44, 19);
		contentPane.add(maker_radius_tx);
		maker_radius_tx.setColumns(10);
		
		show_subtitle_btn = new JButton("Show Subtitle");
		show_subtitle_btn.setToolTipText("Show subtitle.");
		show_subtitle_btn.setForeground(SystemColor.activeCaption);
		show_subtitle_btn.setFont(new Font("Dialog", Font.BOLD, 12));
		show_subtitle_btn.setBounds(10, 439, 153, 23);
		contentPane.add(show_subtitle_btn);
		
		inter_slider = new JSlider();
		inter_slider.setValue(1);
		inter_slider.setMinimum(0);
		inter_slider.setMaximum(600);
		inter_slider.setBounds(1115, 90, 155, 26);
		contentPane.add(inter_slider);
		
		JLabel int_lb = new JLabel("Interpolation Slider");
		int_lb.setBounds(1115, 69, 155, 14);
		contentPane.add(int_lb);
		
		seg_to_conc_btn = new JButton("Seg > Conc");
		seg_to_conc_btn.setFont(new Font("Dialog", Font.BOLD, 12));
		seg_to_conc_btn.setForeground(SystemColor.activeCaption);
		seg_to_conc_btn.setBounds(10, 283, 153, 25);
		contentPane.add(seg_to_conc_btn);
		
		n_obs_lb = new JLabel("Nº Observations:");
		n_obs_lb.setForeground(new Color(0, 0, 128));
		n_obs_lb.setBounds(10, 330, 153, 20);
		contentPane.add(n_obs_lb);
		
		n_obs_tx = new JTextField();
		n_obs_tx.setText("100");
		n_obs_tx.setColumns(10);
		n_obs_tx.setBounds(11, 355, 67, 19);
		contentPane.add(n_obs_tx);
		
		test_btn = new JButton("Test");
		test_btn.setToolTipText("Show subtitle.");
		test_btn.setForeground(SystemColor.activeCaption);
		test_btn.setFont(new Font("Dialog", Font.BOLD, 12));
		test_btn.setBounds(10, 398, 153, 23);
		contentPane.add(test_btn);
		
		fill_cb = new JCheckBox("Fill");
		fill_cb.setBounds(10, 10, 129, 23);
		contentPane.add(fill_cb);
		
		load_wkt = new JButton("Test");
		load_wkt.setBounds(1117, 145, 151, 25);
		contentPane.add(load_wkt);
		
		ice2_wkt = new JButton("Ice 2");
		ice2_wkt.setBounds(1117, 172, 151, 25);
		contentPane.add(ice2_wkt);
		
		ice1_wkt = new JButton("Ice 1");
		ice1_wkt.setBounds(1117, 226, 151, 25);
		contentPane.add(ice1_wkt);
		
		op_tx = new JTextField();
		op_tx.setText("1");
		op_tx.setColumns(10);
		op_tx.setBounds(96, 355, 67, 19);
		contentPane.add(op_tx);
		
		hole_wkt = new JButton("Ice W Hole");
		hole_wkt.setBounds(1117, 283, 151, 25);
		contentPane.add(hole_wkt);
		
		rplane_wkt = new JButton("R Plane");
		rplane_wkt.setBounds(1117, 310, 151, 25);
		contentPane.add(rplane_wkt);
	}

	public void add_listeners()
	{
        addWindowListener(new WindowAdapter() 
        {
            @Override
            public void windowClosing(WindowEvent e) 
            {
                setVisible(false);
                dispose();
            }
        });
        
        
        
        rplane_wkt.addActionListener(new ActionListener() 
        {
			public void actionPerformed(ActionEvent arg0) 
			{	
				p_wkt_tx.setText("POLYGON ((-0.9290465631929048 0.25498891352549891, -1.1152993348115301 0.00221729490022171, -0.97782705099778289 -0.21951219512195119, -0.76496674057649683 -0.2860310421286032, -0.49445676274944583 -0.2372505543237251, -0.29933481152993369 -0.15299334811529941, -0.20177383592017772 -0.03769401330376931, -0.22394678492239484 0.21064301552106435, -0.41906873614190698 0.41019955654101992, -0.72949002217294923 0.42793791574279383, -0.9290465631929048 0.25498891352549891))");
				q_wkt_tx.setText("POLYGON ((-0.92017738359201784 0.29490022172948999, -1.05764966740576494 0.02660753880266076, -1.02439024390243905 -0.16629711751662979, -0.82705099778270519 -0.27937915742793795, -0.45011086474501116 -0.22616407982261644, -0.33924611973392471 -0.15521064301552112, -0.19068736141906872 0.01330376940133038, -0.23946784922394682 0.18181818181818177, -0.37028824833702889 0.40576496674057649, -0.70066518847006665 0.38802660753880264, -0.92017738359201784 0.29490022172948999))");

				op_tx.setText("3");
				
				DrawGraphs g = (DrawGraphs) jp;
				g.center_geometries(true);
				jp.repaint();
			}
		});
        
        hole_wkt.addActionListener(new ActionListener() 
        {
			public void actionPerformed(ActionEvent arg0) 
			{	
				p_wkt_tx.setText("POLYGON ((975.0420063220051361 697.0901670658090552, 968.2376237623761881 719.36675461741401705, 949.81410494875422046 719.68207562603902261, 947.52065936936753587 726.55787388350040601, 929.17309473427621924 738.01753764593581764, 929.50072981704590802 743.2562410801920123, 924.33641378236256969 741.69019505191499775, 919.67167733396092899 738.34495661057690086, 912.79134059580167104 741.61914625698705095, 901.97938286440842148 732.45141524703853975, 891.82269529855420842 721.31917044924421134, 884.61472347762571644 716.73530494426995574, 877.40675165669676971 712.15143943929570014, 873.1474955806933167 721.31917044924421134, 868.88823950468986368 730.48690145919249517, 851.64356435643549048 749.76253298153028481, 845.76780297717243684 751.56233974811925691, 839.07344697266626099 758.97235138296105106, 829.24439448958139565 769.7771772161138415, 823.67459808250009701 760.60944620616601242, 814 751.49999999999977263, 811.08910891089112738 744.69656992084424019, 802.37831770248340035 736.38044282273085628, 798 736, 769.37165356204957334 707.0652770818207955, 757.52759738478107465 695.09446750469783183, 734.55785556919863666 671.87890678845087677, 719.81427684457139549 645.35797065252859284, 721.78008734118850498 635.86282067793922579, 723.74589783780550079 621.1289672690934367, 735.40262887003530068 597.70883261223343652, 738 589, 763.06210777014428004 576.59998807791544095, 814 568, 845.62614862805583016 565.46774328012088517, 852 570, 878.71729198777438796 575.29031221935156282, 892.70495049504950202 586.13192612137208926, 901.82970297029703488 592.21108179419502449, 907.38116144205082492 597.09725493982887201, 928 614.00000000000022737, 975.0420063220051361 697.0901670658090552),(769.2153589293021696 637.42771019186307058, 774.28113639673915714 618.17775581560283626, 782.89295809138184268 617.67117806885903519, 790.99820203928095452 620.71064454932127319, 793.02451302625570406 628.81588849722038503, 788.46531330556240391 630.33562173745144719, 789.47846879904989237 636.92113244511938319, 797.58371274694889053 637.93428793860675796, 798.0902904936926916 644.01322089953112027, 789.47846879904989237 646.54610963324955719, 778.33375837068865621 650.59873160719905627, 770.22851442278954437 650.59873160719905627, 761.11011498140305775 644.51979864627480765, 758.57722624768462083 636.41455469837569581, 763.64300371512160837 630.33562173745144719, 769.2153589293021696 637.42771019186307058))");
				//q_wkt_tx.setText("POLYGON ((965 635, 963.16831683168311429 648.44327176781007438, 958.09900990099015416 658.57519788918216364, 947.96039603960400655 658.57519788918216364, 942 675, 932 679, 933.5 683.99999999999977263, 935 689, 919.29971699016812181 692.97810165621308442, 907.40594059405941607 688.97097625329820403, 897.26732673267326845 683.9050131926121594, 884.60749984128312917 681.68486384012931012, 875 680, 875.53853183207445454 694.86582765366483727, 871 710, 861.64931402949218864 728.70137194101539535, 858.34837671150717142 735.02803324825094933, 855.04743939352226789 741.35469455548673068, 851.61155289671489754 747.7221555823205108, 837.12943406741692343 742.05897758996457014, 826.29702970297034881 734.5646437994723783, 816.15841584158420119 729.49868073878633368, 810.68382577043939818 731.36186360440342469, 793.05342023912112381 721.60861261756804197, 772.90438534618635913 709.65301463370565216, 755.32673267326731548 704.16886543535611054, 741 698, 724.91089108910887262 688.97097625329820403, 704.63366336633635001 668.70712401055402552, 703 651.00000000000022737, 702.69759189111471187 637.9194267305304038, 705 626, 714.77227722772249763 602.8496042216359001, 760 570, 795.88118811881190595 552.1899736147756812, 834.29597603559773233 574.05136381673833057, 846.57425742574241667 562.32189973614777045, 878 573.00000000000022737, 887.12871287128712083 572.45382585751985971, 902.33663366336634226 582.58575197889172159, 950.7825840103793098 618.72754575643511998, 965 635),(769.34200336598814829 633.75502152797150757, 775.42093632691239691 620.45735567594954318, 781.62651372452262422 617.41788919548741887, 787.07222450201732045 620.33071123926367818, 786.56564675527363306 628.30931075047681134, 789.60511323573575737 632.7418660344841328, 790.11169098247944476 638.44086568535067272, 796.06397950671782837 639.45402117883804749, 796.44391281677565075 642.3668432226143068, 790.23833541916542345 645.02637639301860872, 781.87980259789446791 647.93919843679486803, 771.62160322633474152 648.69906505691039911, 764.40287033523713944 646.92604294330749326, 760.47689279797350537 638.06093237529285034, 764.27622589855116075 632.8685104711699978, 769.34200336598814829 633.75502152797150757))");
				q_wkt_tx.setText("POLYGON ((965 635, 963.16831683168311429 648.44327176781007438, 958.09900990099015416 658.57519788918216364, 947.96039603960400655 658.57519788918216364, 942 675, 932 679, 933.5 683.99999999999977263, 935 689, 919.29971699016812181 692.97810165621308442, 907.40594059405941607 688.97097625329820403, 897.26732673267326845 683.9050131926121594, 884.60749984128312917 681.68486384012931012, 875 680, 875.53853183207445454 694.86582765366483727, 871 710, 861.64931402949218864 728.70137194101539535, 858.34837671150717142 735.02803324825094933, 855.04743939352226789 741.35469455548673068, 851.61155289671489754 747.7221555823205108, 837.12943406741692343 742.05897758996457014, 826.29702970297034881 734.5646437994723783, 816.15841584158420119 729.49868073878633368, 810.68382577043939818 731.36186360440342469, 793.05342023912112381 721.60861261756804197, 772.90438534618635913 709.65301463370565216, 755.32673267326731548 704.16886543535611054, 741 698, 724.91089108910887262 688.97097625329820403, 704.63366336633635001 668.70712401055402552, 703 651.00000000000022737, 702.69759189111471187 637.9194267305304038, 705 626, 714.77227722772249763 602.8496042216359001, 760 570, 795.88118811881190595 552.1899736147756812, 834.29597603559773233 574.05136381673833057, 846.57425742574241667 562.32189973614777045, 878 573.00000000000022737, 887.12871287128712083 572.45382585751985971, 902.33663366336634226 582.58575197889172159, 950.7825840103793098 618.72754575643511998, 965 635),(748.95224905955444683 641.48033216581291072, 752.11835997670254983 628.18266631379094633, 756.55091526070987129 623.49682215641178118, 765.28938139203853552 624.00339990315546856, 771.74824766302060652 628.81588849722061241, 769.08871449261619091 632.3619327244264241, 771.87489209970647153 637.80764350192112033, 776.94066956714345906 636.92113244511961057, 778.20711393400267752 642.49348765930028549, 770.60844773284725306 644.13986533621732633, 762.8831370950059636 650.21879829714157495, 754.65124871042098675 654.14477583440520903, 747.55916025600936337 652.49839815748816818, 742.99996053531606321 647.55926512673715933, 743.25324940868790691 640.34053223563955726, 748.95224905955444683 641.48033216581291072))");

				op_tx.setText("2");
				
				DrawGraphs g = (DrawGraphs) jp;
				g.center_geometries(true);
				jp.repaint();
			}
		});
        
        ice1_wkt.addActionListener(new ActionListener() 
        {
			public void actionPerformed(ActionEvent arg0) 
			{	
				
				if(ice1 == 0)
				{
					p_wkt_tx.setText("POLYGON ((1052.471 986.88, 1058.423 988.196, 1088.571 1037.005, 1093.565 1050.563, 1098.567 1064.083, 1095.68 1095.429, 1100.8 1116.15, 1106.886 1119.389, 1113.881 1122.679, 1126.5 1139.4, 1137.783 1159.583, 1140.309 1173.014, 1144.975 1177.775, 1139.2 1218.179, 1144.874 1233.505, 1148.233 1251.583, 1152.844 1261.679, 1152 1280.333, 1157.914 1310.15, 1153.891 1326.158, 1145.37 1333.254, 1126.896 1333.958, 1118.152 1336.025, 1111.981 1332.17, 1092.863 1302.471, 1084.326 1294.025, 1082.379 1271.447, 1067.62 1252.054, 1059.196 1256.217, 1034.744 1241.362, 1022.587 1220.304, 1011.864 1195.958, 991.766 1138.56, 945.535 1075.708, 907.66 991.83, 882.455 911.988, 852.48 821.369, 849.92 815.474, 847.36 801.013, 864.275 789.859, 867.617 785.666, 877.298 786.069, 903.915 771.003, 923.937 781.444, 934.912 803.013, 975.914 832.002, 986.982 844.484, 996.911 858.013, 1012.192 886.493, 1032.911 942.086, 1052.471 986.88))");
					q_wkt_tx.setText("POLYGON ((1054.72 999.602, 1063.4 1004.16, 1072.64 1015.68, 1092.93 1049.072, 1104.158 1075.708, 1100.8 1106.025, 1106.676 1129.333, 1121.801 1136.467, 1131.847 1149.025, 1143.651 1168.389, 1152 1195.025, 1148.687 1211.246, 1152.886 1212.15, 1147.342 1228.043, 1152.794 1242.429, 1153.21 1252.054, 1161.18 1275.938, 1160.756 1290.668, 1166.847 1323.014, 1159.68 1331.9, 1145.925 1337.958, 1130.754 1348.621, 1123.234 1346.114, 1118.825 1337.795, 1104.797 1317.394, 1092.846 1298.226, 1084.431 1285.601, 1074.397 1270.708, 1061.92 1252.054, 1040.864 1220.409, 983.46 1134.371, 975.006 1125.956, 944.244 1074.246, 941.756 1065.36, 937.153 1063.054, 890.236 950.4, 876.465 919.945, 851.244 827.242, 847.36 817.012, 864.233 807.916, 866.726 805.416, 875.141 804.148, 901.847 787.002, 906.643 786.494, 922.844 796.022, 936.96 820.003, 943.626 822.196, 977.92 848.022, 995.84 869.272, 1013.76 899.594, 1054.72 999.602))");

					ice1++;
				}
				else if(ice1 == 1)
				{

					
					ice1++;
				}
				else if(ice1 == 2)
				{

					
					ice1++;
				}
				else if(ice1 == 3)
				{

					
					ice1++;
				}
				else if(ice1 == 4)
				{

					
					ice1++;
				}
				else if(ice1 == 5)
				{

					
					ice1++;
				}
				else if(ice1 == 6)
				{

					
					ice1++;
				}
				else if(ice1 == 7)
				{

					
					ice1++;
				}
				else if(ice1 == 8)
				{

					ice1 = 0;
				}
				
				DrawGraphs g = (DrawGraphs) jp;
				g.center_geometries(true);
				jp.repaint();
			}
		});
        
        ice2_wkt.addActionListener(new ActionListener() 
        {
			public void actionPerformed(ActionEvent arg0) 
			{	
				
				if(op == 0)
				{
					p_wkt_tx.setText("POLYGON ((964.853 691.023, 960 702.042, 954.852 713.002, 946.128 712.473, 937.403 711.945, 928.633 722.484, 915.851 724.002, 915.493 735.31, 908.65 730.051, 900.504 732.31, 886.438 718.31, 876.379 707.233, 868.364 700.244, 859.314 713.792, 854.691 720.763, 837.653 734.569, 813.853 752.003, 799.222 732.667, 790.325 722.638, 774.4 704.138, 757.61 685.561, 740.853 667.003, 723.331 641.686, 714.855 621.013, 718.269 601.271, 736 568.003, 763.807 555.792, 838.852 549.002, 859.933 560.888, 882.851 576.388, 904.851 590.013, 940.854 641.013, 964.853 691.023))");
					q_wkt_tx.setText("POLYGON ((972.8 694.023, 974.731 710.002, 963.731 722.023, 952.745 722.724, 947.81 718.965, 938.733 730.002, 928.104 733.733, 923.44 742.416, 918.51 738.657, 906.959 737.79, 897.388 729.098, 884.271 714.33, 875.56 707.666, 871.485 715.513, 864.827 725.041, 845.365 743.907, 823.053 760.667, 800.556 727.351, 791.514 727.346, 775.164 698.685, 757.484 681.31, 748.175 672.618, 733.729 651.042, 723.729 624, 733.729 599.003, 751.73 577.002, 763.264 574.138, 823.732 559.013, 860.734 566.002, 875.732 570.023, 913.734 597.002, 934.73 619.003, 972.8 694.023))\n");
					op++;
				}
				else if(op == 1)
				{
					p_wkt_tx.setText("POLYGON ((875.642 570.013, 934.645 619.013, 966.036 681.571, 973.895 709.003, 963.452 721.773, 945.518 720.165, 942.802 729.821, 925.699 735.926, 923.05 740.695, 918.213 739.492, 904.96 737.119, 892.5 721.773, 875.43 709.003, 864.642 724.023, 855.04 734.878, 821.76 759.513, 812.8 747.648, 801.279 729.128, 793.189 726.561, 782.325 712.494, 769.563 693.464, 755.133 679.321, 744.719 666.579, 736.231 655.253, 727.252 636.859, 723.71 624.849, 728.664 615.234, 729.156 606.159, 735.206 599.092, 738.059 591.916, 750.722 580.292, 769.624 570.81, 793.25 570.81, 812.157 566.098, 826.332 561.446, 855.04 558.946, 860.641 566.003, 875.642 570.013))");
					q_wkt_tx.setText("POLYGON ((928 614.003, 937.753 634.071, 974.325 686.369, 974.004 701.936, 969.021 718.773, 950.359 719.032, 944.64 727.821, 930.423 735.926, 928.538 742.205, 921.855 737.869, 911.752 740.119, 892.537 721.773, 877.319 710.234, 866.753 731.071, 849.631 750.436, 845.362 750.173, 828.877 769.76, 823.97 760.244, 814.198 751.069, 807.594 740.638, 797.751 736.119, 765.591 700.743, 754.033 692.945, 746.75 684.071, 733.421 669.263, 720.073 646.907, 722.855 627.127, 725.604 614.938, 732.084 603.852, 736.798 599.144, 736.854 586.897, 757.925 578.58, 776.567 572.244, 797.808 569.272, 844.445 564.196, 855.04 574.523, 880.725 579.627, 928 614.003))");
					op++;
				}
				else if(op == 2)
				{
					p_wkt_tx.setText("POLYGON ((975.0420063220051 697.090167065809, 968.2376237623762 719.366754617414, 949.8141049487542 719.682075626039, 947.5206593693675 726.5578738835004, 929.1730947342762 738.0175376459358, 929.5007298170459 743.256241080192, 924.3364137823626 741.690195051915, 919.6716773339609 738.3449566105769, 912.7913405958017 741.619146256987, 901.9793828644084 732.4514152470385, 891.8226952985542 721.3191704492442, 884.6147234776257 716.73530494427, 877.4067516566968 712.1514394392957, 873.1474955806933 721.3191704492442, 868.8882395046899 730.4869014591925, 851.6435643564355 749.7625329815303, 845.7678029771724 751.5623397481193, 839.0734469726663 758.972351382961, 829.2443944895814 769.7771772161138, 823.6745980825001 760.609446206166, 814 751.4999999999998, 811.0891089108911 744.6965699208442, 802.3783177024834 736.3804428227309, 798 736, 769.3716535620496 707.0652770818208, 757.5275973847811 695.0944675046978, 734.5578555691986 671.8789067884509, 719.8142768445714 645.3579706525286, 721.7800873411885 635.8628206779392, 723.7458978378055 621.1289672690934, 735.4026288700353 597.7088326122334, 738 589, 763.0621077701443 576.5999880779154, 814 568, 845.6261486280558 565.4677432801209, 852 570, 878.7172919877744 575.2903122193516, 892.7049504950495 586.1319261213721, 901.829702970297 592.211081794195, 907.3811614420508 597.0972549398289, 928 614.0000000000002, 975.0420063220051 697.090167065809))");
					q_wkt_tx.setText("POLYGON ((965 635, 963.1683168316831 648.4432717678101, 958.0990099009902 658.5751978891822, 947.960396039604 658.5751978891822, 942 675, 932 679, 933.5 683.9999999999998, 935 689, 919.2997169901681 692.9781016562131, 907.4059405940594 688.9709762532982, 897.2673267326733 683.9050131926122, 884.6074998412831 681.6848638401293, 875 680, 875.5385318320745 694.8658276536648, 871 710, 861.6493140294922 728.7013719410154, 858.3483767115072 735.028033248251, 855.0474393935223 741.3546945554867, 851.6115528967149 747.7221555823205, 837.1294340674169 742.0589775899646, 826.2970297029703 734.5646437994724, 816.1584158415842 729.4986807387863, 810.6838257704394 731.3618636044034, 793.0534202391211 721.608612617568, 772.9043853461864 709.6530146337057, 755.3267326732673 704.1688654353561, 741 698, 724.9108910891089 688.9709762532982, 704.6336633663364 668.707124010554, 703 651.0000000000002, 702.6975918911147 637.9194267305304, 705 626, 714.7722772277225 602.8496042216359, 760 570, 795.8811881188119 552.1899736147757, 834.2959760355977 574.0513638167383, 846.5742574257424 562.3218997361478, 878 573.0000000000002, 887.1287128712871 572.4538258575199, 902.3366336633663 582.5857519788917, 950.7825840103793 618.7275457564351, 965 635))");
					op++;
				}
				else if(op == 3)
				{
					p_wkt_tx.setText("POLYGON ((931.873 604.502, 953.321 620.494, 964.874 635.071, 963.873 644.119, 962.267 654.169, 944.64 660.446, 944.64 674.599, 930.418 679.321, 935.136 688.828, 922.487 694.138, 911.36 690.369, 876.866 678.84, 870.877 710.003, 851.875 748.002, 831.154 736.869, 819.001 730.166, 809.876 729.071, 794.18 723.273, 775.694 710.702, 745.063 699.148, 721.485 684.321, 705.587 671.415, 702.072 640.935, 706.186 617.782, 714.872 604.002, 759.876 570.013, 795.876 555.002, 834.876 576, 851.875 564.003, 877.876 573.013, 897.143 577.523, 907.279 582.59, 931.873 604.502))");
					q_wkt_tx.setText("POLYGON ((939.907 642.092, 941.47 649.398, 940.256 655.446, 932.661 664.24, 934.688 673.773, 927.589 673.773, 921.127 680.071, 903.391 676.59, 887.04 671.801, 876.851 673.26, 876.563 696.398, 871.481 720, 866.956 722.542, 862.446 745.847, 827.956 733.09, 817.834 734.109, 785.754 722.637, 753.036 715.994, 734.658 711.263, 720.54 703.659, 704.543 694.138, 697.595 677.83, 696.079 662.994, 697.259 646.138, 704.906 624, 738.56 587.916, 772.907 563.003, 834.909 560.003, 879.907 569.013, 952.905 610.003, 955.98 629.76, 950.907 637.84, 939.907 642.092))");
					op++;
				}
				else if(op == 4)
				{
					p_wkt_tx.setText("POLYGON ((697.681 676.415, 695.084 647.493, 714.836 620.117, 734.593 592.733, 772.527 563.013, 800.477 562.349, 839.6 561.282, 879.526 569.042, 916.101 589.532, 952.53 610.013, 956.763 630.205, 953.892 638.599, 939.398 642.023, 938.897 656.829, 931.095 663.378, 934.526 673.012, 924.618 674.667, 920.229 680.637, 909.787 679.936, 890.438 673.407, 876.516 673.781, 875.528 697.023, 871.106 721.117, 867.171 722.34, 862.85 745.224, 839.474 738.195, 827.653 733.061, 819.666 735.233, 794.432 726.839, 781.855 721.31, 735.094 711.577, 703.827 693.561, 697.681 676.415))");
					q_wkt_tx.setText("POLYGON ((706.106 705.82, 704.55 685.032, 710.33 655.417, 724.48 633.263, 746.481 600.291, 770.084 587.667, 788.026 582.339, 840.604 575.021, 868.554 577.012, 953.618 609.118, 962.56 621.811, 961.576 630.339, 949.76 642.8, 949.76 655.417, 941.277 663.811, 946.59 673.012, 940.02 675.31, 931.381 683.33, 907.728 680.637, 887.071 682.31, 892.305 699.484, 889.345 729.339, 886.113 732.33, 883.855 755.715, 865.944 752.036, 849.125 747.838, 836.506 752.048, 813.365 745.762, 773.376 743.657, 748.205 739.435, 718.763 726.839, 714.58 714.223, 706.106 705.82))");
					op++;
				}
				else if(op == 5)
				{
					p_wkt_tx.setText("POLYGON ((768.756 742.023, 721.898 728.032, 716.303 718.042, 710.708 708.013, 704.659 704.194, 705.715 694.023, 706.711 663.021, 727.258 633.032, 747.759 603.022, 808.257 590.023, 868.844 577.012, 888.256 593.023, 907.757 609.013, 897.319 638.166, 913.823 654.686, 946.758 641.023, 948.36 657.446, 941.205 665.714, 946.758 673.012, 937.619 674.541, 933.504 683.398, 899.56 680.626, 886.764 682.032, 891.884 697.031, 886.344 731.696, 883.864 755.984, 853.232 748.195, 835.737 749.977, 792.662 742.724, 768.756 742.023))");
					q_wkt_tx.setText("POLYGON ((857.709 799.022, 832.71 814.012, 809.266 808.714, 790.707 802.051, 778.706 787.561, 766.709 773.041, 752.711 732.032, 748.709 689.023, 754.708 672, 769.71 652.022, 786.062 621.807, 807.085 596.59, 844.921 575.579, 861.74 567.178, 880.644 555.686, 897.164 561.148, 902.692 583.167, 913.697 588.675, 919.325 599.657, 930.33 599.657, 924.836 605.157, 920.574 621.811, 896.71 656.022, 914.92 663.021, 952.336 698.724, 935.791 715.211, 924.786 726.196, 913.76 742.724, 886.213 770.243, 857.709 799.022))");
					op++;
				}
				else if(op == 6)
				{
					p_wkt_tx.setText("POLYGON ((904.96 750.002, 888.985 766.532, 877.744 780.84, 854.732 801.965, 832.571 814.013, 807.264 811.494, 778.915 792.54, 766.72 773.042, 763.05 757.215, 757.434 741.445, 752.573 732.071, 753.65 716.744, 749.532 690.916, 753.48 684.321, 755.492 674.599, 760.4 665.203, 770.021 649.148, 781.277 631.821, 786.118 623.273, 807.571 599.148, 835.906 580.292, 869.431 559.455, 881.452 555.398, 899.84 563.003, 900.572 583.071, 913.92 588.321, 916.002 599.148, 926.72 600.695, 921 608.55, 922.204 619.863, 911.563 632.137, 895.948 653.196, 914.571 663.003, 933.12 679.425, 954.039 698.186, 925.704 726.539, 920.967 735.926, 904.96 750.002))");
					q_wkt_tx.setText("POLYGON ((974.722 705.012, 963.462 735.926, 954.006 773.665, 935.103 806.734, 925.723 812.013, 916.054 816.117, 883.2 816.117, 867.136 809.32, 864.226 811.494, 826.45 773.665, 822.723 766.013, 813.22 751.069, 803.721 736.119, 800.136 713.541, 807.67 688.752, 802.942 674.599, 805.724 660.003, 807.528 646.388, 831.225 602.148, 845.423 570.81, 852.725 562.003, 880.725 555.002, 886.416 559.631, 892.632 570.81, 903.368 572.367, 913.164 581.811, 921.722 580.023, 922.549 586.321, 928 597.002, 922.917 614.4, 919.191 624.811, 919.172 639.369, 932.605 638.359, 945.92 640.119, 961.015 646.464, 972.97 651.012, 989.723 655.003, 974.722 705.012))");
					op++;
				}
				else if(op == 7)
				{
					p_wkt_tx.setText("POLYGON ((958.234 764.638, 933.017 810.878, 899.552 819.281, 865.876 810.878, 828.065 777.242, 802.73 735.233, 798.53 705.82, 806.942 693.233, 802.742 676.415, 806.954 647.002, 848.995 567.185, 870.042 554.59, 886.848 558.791, 895.066 571.378, 924.706 579.781, 928.876 592.397, 924.525 604.993, 920.334 638.599, 945.552 638.599, 991.8 655.417, 974.998 689.023, 975.238 705.82, 966.826 726.832, 958.234 764.638))");
					q_wkt_tx.setText("POLYGON ((832.298 726.839, 815.475 718.445, 752.417 642.8, 743.875 617.609, 760.698 592.397, 773.313 562.984, 806.946 546.195, 823.765 533.57, 844.787 550.388, 857.402 546.195, 886.831 550.388, 899.455 558.791, 907.858 571.378, 924.689 579.781, 987.504 638.599, 1004.335 668.012, 995.94 710.021, 979.139 722.638, 958.129 731.032, 911.914 743.657, 907.707 735.233, 882.75 752.044, 865.944 743.633, 832.298 726.839))");
					op++;
				}
				else if(op == 8)
				{
					p_wkt_tx.setText("POLYGON ((949.334 783.109, 932.175 810.763, 907.727 818.165, 896.991 816.117, 878.519 816.117, 868.08 808.57, 864 811.494, 826.341 773.665, 819.2 759.744, 812.441 752.272, 803.561 736.119, 800.571 722.292, 800.802 711.56, 807.302 688.869, 802.56 674.599, 809.564 642.119, 821.76 617.994, 831.267 603.857, 835.726 589.685, 845.215 570.81, 859.267 556.667, 880.564 555.002, 892.344 570.81, 908.8 576, 913.92 583.071, 921.6 580.023, 928 597.002, 920.773 617.994, 918.78 638.647, 941.55 639.378, 988.953 653.928, 983.757 673.061, 977.579 684.071, 977.636 699.648, 966.4 727.571, 956.665 762.744, 949.334 783.109))");
					q_wkt_tx.setText("POLYGON ((983.233 719.165, 949.906 734.666, 942.736 733.09, 915.946 745.398, 909.215 740.638, 905.923 732.823, 899.046 741.125, 883.209 754.791, 873.46 745.398, 858.284 742.426, 849.92 735.926, 835.462 726.561, 828.16 725.811, 835.891 717.051, 840.61 702.946, 859.489 655.724, 864.207 641.619, 864.203 622.773, 873.64 599.148, 878.358 589.685, 886.959 579.773, 884.862 573.579, 892.58 570.81, 885.556 564.272, 892.457 566.098, 897.176 556.667, 908.105 571.744, 916.801 577.311, 922.7 578.858, 935.746 591.619, 946.93 606.186, 959.655 614.55, 980.163 632.321, 996.132 649.593, 1003.52 666.744, 998.773 695.657, 983.233 719.165))");
					op = 0;
				}
				
				DrawGraphs g = (DrawGraphs) jp;
				g.center_geometries(true);
				jp.repaint();
			}
		});
        
        load_wkt.addActionListener(new ActionListener() 
        {
			public void actionPerformed(ActionEvent arg0) 
			{	
				p_wkt_tx.setText("POLYGON((-140.52934290768178016 -54.1797592312905465, -140.2037444305227325 -54.31393442792202109, -140.11608330205683615 -54.27099836499994723, -140.09103726535229839 -54.0813640870941299, -140.50966387884250253 -53.97044592454544443, -140.52934290768178016 -54.1797592312905465))\n");
				q_wkt_tx.setText("POLYGON((-140.45241579494640405 -54.18333723653405087, -140.12860632040911923 -54.16008020245126175, -140.30750658258440922 -54.02232700057627568, -140.44168177921588381 -54.02232700057627568, -140.45241579494640405 -54.18333723653405087))\n");
				
				DrawGraphs g = (DrawGraphs) jp;
				g.center_geometries(true);
				jp.repaint();
			}
		});
        
        fill_cb.addItemListener(new ItemListener() 
		{
			@Override 
			public void itemStateChanged(ItemEvent e) 
			{
				jp.repaint();
			}
		});
        
        seg_to_conc_btn.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent arg0) 
		    {
				DrawGraphs g = (DrawGraphs) jp;
								
	        	if(q_wkt_tx.getText() == null || p_wkt_tx.getText() == null)
	        		return;
	        	
	        	if(q_wkt_tx.getText().length() == 0 || p_wkt_tx.getText().length() == 0)
	        		return;
			
	        	int n_obs = Integer.valueOf(n_obs_tx.getText());
	        	
	        	geometries = null;
	        	
				// Call python script.
				
				String[] cmd;
				cmd = new String[7];
					
				for(int j = 0; j < cmd.length; j++)
					cmd[j] = "";
				
				cmd[0] = "python";
				cmd[1] = "/home/user/rip/src/rip.py";
				cmd[2] = p_wkt_tx.getText();
				cmd[3] = q_wkt_tx.getText();
				//cmd[4] = "0.0000000001";
				//cmd[4] = "0";
				cmd[4] = op_tx.getText();
				cmd[5] = String.valueOf(n_obs);
				cmd[6] = "0";

				Runtime rt = Runtime.getRuntime();
				Process pr = null;
				
				for(int i = 0; i < cmd.length; i++)
					System.out.print(cmd[i] + " ");
				
				System.out.println();
				
				try
				{
					pr = rt.exec(cmd);
				}
				catch (IOException e1) 
				{
					JOptionPane.showMessageDialog(null, e1.getMessage(), "Generate Interpolation ERR", JOptionPane.INFORMATION_MESSAGE);
					jp.repaint();
					return;
					//e1.printStackTrace();
				}
				
				BufferedReader bfr = new BufferedReader(new InputStreamReader(pr.getInputStream()));
				String line = "";

				try
				{
					// Build array of geometries in wkt representation.
					int i = 0;
					int n = 0;
					int j = 0;
					boolean err = false;
					String err_msg = "";
					
					while((line = bfr.readLine()) != null)
					{
						if(i == 0)
						{
							n = Integer.valueOf(line);
							if(n == 0)
								err = true;
							else
								geometries = new String[n];
						}
						else if(i == 1)
						{
							if(!err)
								num_invalid_geoms = Integer.valueOf(line);
						}
						else if(i == 2)
						{
							if(!err)
								num_complex_geoms = Integer.valueOf(line);
						}
						else
						{
							if(!err) 
							{
								geometries[j] = line;
								j++;								
							}
							else
								err_msg = line;

						}
						
						i++;
					}
					
					if(err)
					{
						JOptionPane.showMessageDialog(null, err_msg, "Generate Interpolation ERR", JOptionPane.INFORMATION_MESSAGE);
						jp.repaint();
						return;
					}
					
					if(geometries == null || geometries.length == 0)
					{
						JOptionPane.showMessageDialog(null, "An error occured. Check if the path to the python script is correct.", "Generate Interpolation ERR", JOptionPane.INFORMATION_MESSAGE);
						jp.repaint();
						return;
					}
					
					int n_inv_geoms = 0;
					Geometry geometry = null;
					
					for(String wkt : geometries)
					{
						geometry = reader.read(wkt);
						
						if(!geometry.isValid())
							n_inv_geoms++;
					}
					
					if(n_inv_geoms > 0)
						geoms_validity = false;
					else
						geoms_validity = true;
					
					n_obs_lb.setText("Nº Obs: " + String.valueOf(geometries.length));
					
					int max = geometries.length - 1;
					inter_slider.setValue(0);
					inter_slider.setMaximum(max);
				}
				catch (IOException e1)
				{
					JOptionPane.showMessageDialog(null, e1.getMessage(), "Generate Interpolation ERROR", JOptionPane.INFORMATION_MESSAGE);
					jp.repaint();
					return;
				}
				catch (ParseException e) {
					JOptionPane.showMessageDialog(null, e.getMessage(), "Generate Interpolation ERROR", JOptionPane.INFORMATION_MESSAGE);
					jp.repaint();
					
					return;
				}

				g.to_origin(false);
				jp.repaint();
		    }
		});
        
        test_btn.addActionListener(new ActionListener() 
        {
			public void actionPerformed(ActionEvent arg0) 
			{	
				show_test = !show_test;
							
				n_obs_lb.setText("");
				show_geoms = false;
				curr_boundary_line = 0;
				show_aligned_geoms = false;
				geometries = null;
				geom_to_show_id = 0;
			    b_arr = null;
			    show_geoms = false;
			    show_subtitle = false;			    
			    sc_geom_wkt = null;
			    tg_geom_wkt = null;
			    num_invalid_geoms = 0;
			    num_complex_geoms = 0;
			    geoms_validity = false;
			    
				DrawGraphs g = (DrawGraphs) jp;
				g.center_geometries(true);
				jp.repaint();
			}
		});
        
        clear_bt.addActionListener(new ActionListener() 
        {
			public void actionPerformed(ActionEvent arg0) 
			{	
				show_test = false;
				n_obs_lb.setText("");
				show_geoms = false;
				curr_boundary_line = 0;
				show_aligned_geoms = false;
				geometries = null;
				geom_to_show_id = 0;
			    b_arr = null;
			    show_geoms = false;
			    show_subtitle = false;			    
			    sc_geom_wkt = null;
			    tg_geom_wkt = null;
			    num_invalid_geoms = 0;
			    num_complex_geoms = 0;
			    geoms_validity = false;
			    
				jp.repaint();
			}
		});
        
        center_geoms_btn.addActionListener(new ActionListener() 
        {
			public void actionPerformed(ActionEvent arg0) 
			{
				DrawGraphs g = (DrawGraphs) jp;
				g.center_geometries(false);
				jp.repaint();
			}
		});
        
        show_subtitle_btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) 
			{
				if(show_subtitle)
					show_subtitle = false;
				else
					show_subtitle = true;
				
				DrawGraphs g = (DrawGraphs) jp;
				g.center_geometries(false);
				jp.repaint();
			}
		});
                
		save_seq_to_picture.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent arg0) 
			{
				int N = geometries.length;
				int show_granularity = Integer.valueOf(gran_tx.getText());
				
				int temp = geom_to_show_id;
				
				try
				{
					for(int i = 0; i < N; i = i + show_granularity) 
					{
						geom_to_show_id = i;
						jp.repaint();
			    		
						BufferedImage bi = ScreenImage.createImage(viz_p);
	    				ScreenImage.writeImage(bi, png_filename_tx.getText() + "_" + (N + i) + ".png");
					}
    			} 
				catch (IOException ex) {
					JOptionPane.showMessageDialog(null, ex.getMessage(), "Save Sequence ERR", JOptionPane.INFORMATION_MESSAGE);
					return;
    			}
				
				geom_to_show_id = temp;
				jp.repaint();
				JOptionPane.showMessageDialog(null, "Sequence Saved!", "Save Sequence", JOptionPane.INFORMATION_MESSAGE);	
			}
		});
        
		save_to_picture.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent arg0) 
			{				
				status.setText("Saving!");
				String filename = png_filename_tx.getText() + "_" + 1 + ".png";
				
				try
				{
					BufferedImage bi = ScreenImage.createImage(viz_p);
	    			ScreenImage.writeImage(bi, filename);
    			} 
				catch (IOException ex) {
					JOptionPane.showMessageDialog(null, ex.getMessage(), "Save to PNG ERR", JOptionPane.INFORMATION_MESSAGE);
					jp.repaint();
					return;
    				//ex.printStackTrace();
    			}
				
				jp.repaint();
				
				JOptionPane.showMessageDialog(null, "Saved to: " + filename + "!", "Save", JOptionPane.INFORMATION_MESSAGE);	
				status.setText("");
			}
		});
		
		zoom_slider.addChangeListener(new ChangeListener() 
		{
		    public void stateChanged(ChangeEvent e) 
		    {
		        JSlider s = (JSlider) e.getSource();
		        int desired_zoom_level = (int) s.getValue();
		        
		        DrawGraphs c = (DrawGraphs) jp;
		        double diff = desired_zoom_level - zoomLevel;
		        
		        if(diff > 0 && desired_zoom_level < maxZoomLevel)
		        {
		        	zoomLevel = desired_zoom_level;
            		c.scale(diff, diff);
		        }
		        else if(diff < 0 && desired_zoom_level > minZoomLevel)
		        {
		        	zoomLevel = desired_zoom_level;
            		c.scale(diff, diff);
		        }
				
		        if(b_arr != null)
		        	c.to_origin(true);
		        else
		        	c.center_geometries(true);
		        
		    	jp.repaint(); 
		    }
		});
		
		show_geometries_btn.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent arg0) 
		    {				
				if(show_geoms)
					show_geoms = false;
				else
				{
		    	    sc_geom_wkt = p_wkt_tx.getText();
		    	    tg_geom_wkt = q_wkt_tx.getText();
		    	    
					show_geoms = true;
				}				
				
				DrawGraphs g = (DrawGraphs) jp;
				g.center_geometries(false);
				jp.repaint();
		    }
		});
		
		inter_slider.addChangeListener(new ChangeListener() 
		{
		    public void stateChanged(ChangeEvent e) 
		    {
		    	if(geometries == null || geometries.length == 0)
		    		return;
		    	
		        JSlider s = (JSlider) e.getSource();
		        int i = (int) s.getValue();

		    	if(i < geometries.length)
		    		geom_to_show_id = i;
		    			
		    	jp.repaint();
		    }
		});
	}
}
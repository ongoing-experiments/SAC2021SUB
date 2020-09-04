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

public class LCIP extends JFrame
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

	public LCIP() 
	{
		f = this;
				
		curr_boundary_line = 0;
		
		show_aligned_geoms = false;
		geometries = null;
		geom_to_show_id = 0;
		num_invalid_geoms = 0;
		num_complex_geoms = 0;
		
		geoms_validity = false;
			    
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
		
		load_wkt = new JButton("New button");
		load_wkt.setBounds(1117, 145, 151, 25);
		contentPane.add(load_wkt);
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
        
        load_wkt.addActionListener(new ActionListener() 
        {
			public void actionPerformed(ActionEvent arg0) 
			{	
				p_wkt_tx.setText("LINESTRING(-140.50924455177292316 -54.26986651320897437, -140.06526900166338123 -54.25620572705175704)");
				q_wkt_tx.setText("LINESTRING(-140.38800507462761402 -54.16228782222089677, -140.33848472480769942 -54.09569148970446406, -140.39312786943656874 -54.0393407468059479, -140.34360751961665414 -54.02738755891838451, -140.30774795595397109 -54.06495472085072862, -140.20016926496589349 -54.05812432777211995, -140.16089450476388834 -54.12130546374925189, -140.10795895840468006 -54.08715349835620856, -140.07039179647230753 -54.10252188278307273, -140.13698812898874735 -54.15033463433333338, -140.07039179647233595 -54.18960939453533143)");
				
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
				cmd[1] = "/home/user/concavities/src/lcip.py";
				cmd[2] = p_wkt_tx.getText();
				cmd[3] = q_wkt_tx.getText();
				//cmd[4] = "0.0000000001";
				cmd[4] = "0";
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
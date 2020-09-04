package gui;

import java.awt.EventQueue;

/*
 * Authors: José Duarte 	(hfduarte@ua.pt)
 * 			Mark Mckenney 	(marmcke@siue.edu)
 * 
 * Version: 1.0 (2020-05-29)
 * 
 */

public class Gui 
{
	public static void main(String[] args) 
    {
        //Main ex = new Main();
        
		EventQueue.invokeLater(new Runnable() 
		{
			public void run() {
				try {
					MainWindow main = new MainWindow(/*ex*/);
					main.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
     }
}

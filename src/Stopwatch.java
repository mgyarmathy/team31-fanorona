import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.*;

public class Stopwatch extends JPanel implements Runnable {
		private boolean count=false;
		private double time;
		private int interval;
		
		private boolean kill_thread=false;
		private boolean timeUp=false;
		
		public GamePanel board;
		
		public Stopwatch(int _interval){
			this.time = _interval/1000;
			this.interval = _interval;
			
			setPreferredSize(new Dimension(0, 75));
			setBackground(new Color(240,240,240));
			setBorder(BorderFactory.createLineBorder(Color.BLACK,1));
			setVisible(true);
		}
		
		public void addboard(GamePanel b){
			board = b;
		}
		
		public void run(){
			while(!kill_thread){
				while(count && interval != 0){
					for(;time>0; time=time-0.1){
						if(count == false || interval == 0) { timeReset(); break;}
						try{
							Thread.sleep(100);
						}
						catch(InterruptedException e){}
					}
					
					if (Math.round(time) == 0){
						timeUp=true;
						count = false;
						board.countPieces();
					}
				}
				try{Thread.sleep(500);}
				catch(InterruptedException e){}
			}
		}
		
		@Override
		public void paintComponent(Graphics g) {
			//draw the information here
			super.paintComponent(g);
			g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, getHeight()/2 ));
			if(interval != 0) g.drawString(Double.toString(Math.round(time*100.0)/100.0)+" Seconds Left For Turn ", 20, 45);
			repaint();
		 }
		
		public void timeStart(){
			count = true;
		}
		
		public void timeStop(){
			count = false;
		}
		
		public void timeReset(){
			count = false;
			time = interval/1000;
			timeUp = false;
		}
		
		public boolean running(){
			return count;
		}
		
		public boolean isTimeUp(){
			return timeUp;
		}
		
		public void setInterval(int _interval){
			interval = _interval;
			timeReset();
		}
		
		public double getTime(){
			return time;
		}
		
		public void killStopwatch(){
			kill_thread = true;
		}
}

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;



public class Grid extends JPanel{
	
	private JButton A1,A2,A3,A4;
	
	public InfoPanel infopan;
	
	ClickListener cl = new ClickListener();
	
	public Grid(InfoPanel i1){ 
		//setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
		
		infopan = i1;
		setPreferredSize(new Dimension(200, 300));
		setVisible(true);
		makegrid();
	}
	
	public void makegrid(){
		setLayout(new GridLayout(5,9));
		
		A1 = new JButton("A1");
		A1.addActionListener(cl);
		add(A1);
		
		A2 = new JButton("A2");
		A2.addActionListener(cl);
		add(A2);
		add(new JButton("A3"));
		add(new JButton("A4"));
		add(new JButton("A5"));
		add(new JButton("A6"));
		add(new JButton("A7"));
		add(new JButton("A8"));
		add(new JButton("A9"));
		
		add(new JButton("B1"));
		add(new JButton("B2"));
		add(new JButton("B3"));
		add(new JButton("B4"));
		add(new JButton("B5"));
		add(new JButton("B6"));
		add(new JButton("B7"));
		add(new JButton("B8"));
		add(new JButton("B9"));
		
		add(new JButton("C1"));
		add(new JButton("C2"));
		add(new JButton("C3"));
		add(new JButton("C4"));
		add(new JButton("C5"));
		add(new JButton("C6"));
		add(new JButton("C7"));
		add(new JButton("C8"));
		add(new JButton("C9"));
		
		add(new JButton("D1"));
		add(new JButton("D2"));
		add(new JButton("D3"));
		add(new JButton("D4"));
		add(new JButton("D5"));
		add(new JButton("D6"));
		add(new JButton("D7"));
		add(new JButton("D8"));
		add(new JButton("D9"));
		
		add(new JButton("E1"));
		add(new JButton("E2"));
		add(new JButton("E3"));
		add(new JButton("E4"));
		add(new JButton("E5"));
		add(new JButton("E6"));
		add(new JButton("E7"));
		add(new JButton("E8"));
		add(new JButton("E9"));
	}
	
	@Override
	public void paintComponent(Graphics g) {
	 }


	private class ClickListener implements ActionListener{
		public void actionPerformed(ActionEvent e){
			if(e.getSource() == A1){
				infopan.write("A1 was pressed");
			}
			if(e.getSource() == A1){
				infopan.write("A1 was pressed");
			}
			if(e.getSource() == A2){
				infopan.write("A2 was pressed");
			}
			
		}
	}
}
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;



public class Grid extends JPanel{
	
	private JButton A[]= new JButton[9];
	private JButton B[]= new JButton[9];
	private JButton C[]= new JButton[9];
	private JButton D[]= new JButton[9];
	private JButton E[]= new JButton[9];
	
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
		
		//make buttons
		for(int i=0; i<9; i++){ A[i] = new JButton("A"+Integer.toString(i+1)); A[i].addActionListener(cl); add(A[i]);
		}
		for(int i=0; i<9; i++){ B[i] = new JButton("B"+Integer.toString(i+1)); B[i].addActionListener(cl); add(B[i]);
		}
		for(int i=0; i<9; i++){ C[i] = new JButton("C"+Integer.toString(i+1)); C[i].addActionListener(cl); add(C[i]);
		}
		for(int i=0; i<9; i++){ D[i] = new JButton("D"+Integer.toString(i+1)); D[i].addActionListener(cl); add(D[i]);
		}
		for(int i=0; i<9; i++){ E[i] = new JButton("E"+Integer.toString(i+1)); E[i].addActionListener(cl); add(E[i]);
		}
		
	}
	
	@Override
	public void paintComponent(Graphics g) {
	 }


	private class ClickListener implements ActionListener{
		public void actionPerformed(ActionEvent e){
			if(e.getSource() == A[0]){ infopan.write("A1 was pressed"); }
			else if(e.getSource() == A[1]){ infopan.write("A2 was pressed"); }
			else if(e.getSource() == A[2]){ infopan.write("A3 was pressed"); }
			else if(e.getSource() == A[3]){ infopan.write("A4 was pressed"); }
			else if(e.getSource() == A[4]){ infopan.write("A5 was pressed"); }
			else if(e.getSource() == A[5]){ infopan.write("A6 was pressed"); }
			else if(e.getSource() == A[6]){ infopan.write("A7 was pressed"); }
			else if(e.getSource() == A[7]){ infopan.write("A8 was pressed"); }
			else if(e.getSource() == A[8]){ infopan.write("A9 was pressed"); }
			
			else if(e.getSource() == B[0]){ infopan.write("B1 was pressed"); }
			else if(e.getSource() == B[1]){ infopan.write("B2 was pressed"); }
			else if(e.getSource() == B[2]){ infopan.write("B3 was pressed"); }
			else if(e.getSource() == B[3]){ infopan.write("B4 was pressed"); }
			else if(e.getSource() == B[4]){ infopan.write("B5 was pressed"); }
			else if(e.getSource() == B[5]){ infopan.write("B6 was pressed"); }
			else if(e.getSource() == B[6]){ infopan.write("B7 was pressed"); }
			else if(e.getSource() == B[7]){ infopan.write("B8 was pressed"); }
			else if(e.getSource() == B[8]){ infopan.write("B9 was pressed"); }
			
			else if(e.getSource() == C[0]){ infopan.write("C1 was pressed"); }
			else if(e.getSource() == C[1]){ infopan.write("C2 was pressed"); }
			else if(e.getSource() == C[2]){ infopan.write("C3 was pressed"); }
			else if(e.getSource() == C[3]){ infopan.write("C4 was pressed"); }
			else if(e.getSource() == C[4]){ infopan.write("C5 was pressed"); }
			else if(e.getSource() == C[5]){ infopan.write("C6 was pressed"); }
			else if(e.getSource() == C[6]){ infopan.write("C7 was pressed"); }
			else if(e.getSource() == C[7]){ infopan.write("C8 was pressed"); }
			else if(e.getSource() == C[8]){ infopan.write("C9 was pressed"); }
			
			else if(e.getSource() == D[0]){ infopan.write("D1 was pressed"); }
			else if(e.getSource() == D[1]){ infopan.write("D2 was pressed"); }
			else if(e.getSource() == D[2]){ infopan.write("D3 was pressed"); }
			else if(e.getSource() == D[3]){ infopan.write("D4 was pressed"); }
			else if(e.getSource() == D[4]){ infopan.write("D5 was pressed"); }
			else if(e.getSource() == D[5]){ infopan.write("D6 was pressed"); }
			else if(e.getSource() == D[6]){ infopan.write("D7 was pressed"); }
			else if(e.getSource() == D[7]){ infopan.write("D8 was pressed"); }
			else if(e.getSource() == D[8]){ infopan.write("D9 was pressed"); }
			
			else if(e.getSource() == E[0]){ infopan.write("E1 was pressed"); }
			else if(e.getSource() == E[1]){ infopan.write("E2 was pressed"); }
			else if(e.getSource() == E[2]){ infopan.write("E3 was pressed"); }
			else if(e.getSource() == E[3]){ infopan.write("E4 was pressed"); }
			else if(e.getSource() == E[4]){ infopan.write("E5 was pressed"); }
			else if(e.getSource() == E[5]){ infopan.write("E6 was pressed"); }
			else if(e.getSource() == E[6]){ infopan.write("E7 was pressed"); }
			else if(e.getSource() == E[7]){ infopan.write("E8 was pressed"); }
			else if(e.getSource() == E[8]){ infopan.write("E9 was pressed"); }		
		}
	}
}
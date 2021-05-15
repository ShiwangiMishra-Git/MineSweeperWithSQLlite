package MineSweeperPackage;

import javax.swing.*;
import java.awt.*;




public class Timer extends JLabel {

	private static final long serialVersionUID = 1L;
	private boolean keepRunning;
    private Runnable timertask;
	private long totaltime = 1000;

	public long getTotaltime() {
		return totaltime;
	}

	public void setTotaltime(long totaltime) {
		this.totaltime = totaltime;
	}
	
	private long stopTime=0;
   
    public long getStopTime() {
		return stopTime;
	}

	public void setStopTime(long stopTime) {
		this.stopTime = stopTime;
	}

	public Timer() {
    	
        super("1000", SwingConstants.CENTER);
        setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        setFont(new Font(Font.MONOSPACED, Font.BOLD, 20));
        timertask = new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                
                }
                while (keepRunning) {
                	if(Integer.valueOf(getText())==stopTime)
                	{
                		stopCounting();
                	}
                	else {
                    setText(String.valueOf(Integer.valueOf(getText()) - 1));
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                  
                    }
                    }

                }
            }
        };
        keepRunning = false;
    }


    public void startCounting() {
        keepRunning = true;
        new Thread(timertask).start();
    }


    public void stopCounting() {
        keepRunning = false;
    }

    public void resetCounting() {
        keepRunning = false;
        setText(String.valueOf(totaltime));
    }
}

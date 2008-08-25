package net.sf.jxpilot.test.graphics;

import net.sf.jxpilot.test.*;

import java.io.*;

public class GraphicsTest {

	private static final File mapsFile = new File("maps");
	
	
	private static void produceMap() throws IOException
	{
		if (!mapsFile.exists()) mapsFile.createNewFile();
		
		Client client;
		BlockMap map;
		
		client = new Client();
		client.runClient(UDPTest.LKRAUSS_ADDRESS, UDPTest.SERVER_MAIN_PORT);
		
		map = client.getMap();
		
		File mapFile = new File(mapsFile.getName()+"/"+map.getSetup().getName());
		
		if (mapFile.exists())
		{
			mapFile.createNewFile();
		}
		
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(mapFile));
		
		out.writeObject(map);
	}
	
	private static BlockMap getMap() throws Exception
	{
		File[] mapFiles = mapsFile.listFiles();
		
		if (mapFiles.length>0)
		{
			File mapFile = mapFiles[0];
			
			return (BlockMap) (new ObjectInputStream(new FileInputStream(mapFile)).readObject());
		}
		return null;
		
	}
	
	private static TestMapFrame frame;
	
	public static void main(String[] args) throws Exception
	{
		//produceMap();
		
		BlockMap map = getMap();
		
		if (map==null)
		{
			System.out.println("No maps in directory!");
			return;
		}
		
		frame = new TestMapFrame(map);
		
		frame.setVisible(true);
		moveLoop(frame);
	}
	
	private static final int FPS = 20;
	private static final int SLEEP_TIME = 1000/FPS;
	private static final int NUM_LOOPS = 10000;
	
	private static void moveLoop(TestMapFrame frame)
	{
		for (int i = 0;i<NUM_LOOPS;i++)
		{
			frame.moveView(0.1, 0.1);
			frame.activeRender();
			//frame.repaint();
			try
			{
				Thread.sleep(SLEEP_TIME);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}
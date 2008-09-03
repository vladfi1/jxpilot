package net.sf.jxpilot.test.graphics;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import net.sf.jxpilot.test.BlockMap;
import net.sf.jxpilot.test.Client;
import net.sf.jxpilot.test.UDPTest;

public class GraphicsTest {

	private static final File mapsFile = new File("maps");
	
	
	private static void produceMap() throws IOException
	{
		if (!mapsFile.exists()) mapsFile.createNewFile();
		
		Client client;
		BlockMap map;
		
		client = new Client(null);
		client.runClient(UDPTest.LKRAUSS_ADDRESS, UDPTest.SERVER_MAIN_PORT);
		
		map = client.getMap();
		
		File mapFile = new File(mapsFile.getName()+"/"+map.getSetup().getName());
		
		if (!mapFile.exists())
		{
			mapFile.createNewFile();
		}
		else return;
		
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(mapFile));
		
		out.writeObject(map);
	}
	
	private static BlockMap getMap() throws Exception
	{
        File[] mapFiles = mapsFile.listFiles();

        if (mapFiles.length > 0) {
            File mapFile = mapFiles[0];

            if (mapFile.isFile() && mapFile.canRead()) {
                BlockMap map = null;

                try {
                    map = (BlockMap) (new ObjectInputStream(
                            new FileInputStream(mapFile)).readObject());
                }
                catch (Exception e) {
                    // On errors with reading file - like problems with
                    // deserialising object from file(@see
                    // ObjectInputStream#readObject() or IOException.
                }

                if (map != null)
                    return map;
            }
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
			frame.moveView(0.2, 0.2);
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
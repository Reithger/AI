import java.util.Random;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.imageio.ImageIO;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.*;

public class Assgt5KMeans {

	private static final String IMAGE_PATH_1 = "test1.png";
	private static final String IMAGE_PATH_2 = "test2.png";
	
	private static final String IMAGE_OUT_1 = "C:\\Users\\Reithger\\Documents\\School\\AI\\AI Experiment Results 5\\out_test1_";
	private static final String IMAGE_OUT_2 = "C:\\Users\\Reithger\\Documents\\School\\AI\\AI Experiment Results 5\\out_test2_";
	
	public static void main(String[] args) throws Exception{
		KMeanImage obj = null;
		for(int i = 0; i < 4; i++) {
			
			for(int j = 1; j <= 10; j++) {
				obj = new KMeanImage(IMAGE_PATH_1, i);
				
				obj.performKMeans(j);
				
				obj.outputClusterImage(IMAGE_OUT_1 + i + "_" + j);
				
				obj = new KMeanImage(IMAGE_PATH_2, i);
				
				obj.performKMeans(j);
				
				obj.outputClusterImage(IMAGE_OUT_2 + i + "_" + j);
			}
		}
	}
	
}

class KMeanImage {
	
	PixelBright[][] imageRep;
	ArrayList<ClusterCenter> kClusters;
	int distVersion;
	
	public KMeanImage(String imgFilePath, int distCalc) throws Exception{
		File f = new File(imgFilePath);
		BufferedImage img = null;
		img = ImageIO.read(f);
		distVersion = distCalc;
		fillImageRep(img);
	}
	
	private void fillImageRep(BufferedImage img) {
		int wid = img.getWidth();
		int hei = img.getHeight();
		imageRep = new PixelBright[wid][hei];
		for(int i = 0; i < wid; i++) {
			for(int j = 0; j < hei; j++) {
				imageRep[i][j] = new PixelBright(i, j, new Color(img.getRGB(i,  j)), wid, hei, distVersion);
			}
		}
	}
	
	private void assignClusters(int numCluster) {
		Random rand = new Random();
		kClusters = new ArrayList<ClusterCenter>();
		for(int i = 0; i < numCluster; i++) {
			kClusters.add(new ClusterCenter(rand.nextDouble(), rand.nextDouble(), rand.nextDouble()));
		}
	}
	
	public void performKMeans(int numCluster) {
		assignClusters(numCluster);
		
		boolean[] results = new boolean[kClusters.size()];
		
		while(!queryNoChanges(results)) {
			for(ClusterCenter c : kClusters) {
				c.clearNearest();
			}
			for(int i = 0; i < imageRep.length; i++) {
				for(int j = 0; j < imageRep[i].length; j++) {
					imageRep[i][j].assignSelf(kClusters);
				}
			}
			for(int i = 0; i < kClusters.size(); i++) {
				results[i] = kClusters.get(i).averageValues();
			}
		}
		
	}
	
	private boolean queryNoChanges(boolean[] in) {
		for(boolean b : in) {
			if(b == false)
				return false;
		}
		return true;
	}
	
	public void outputClusterImage(String filePath) throws Exception{
		HashMap<ClusterCenter, Integer> colorMap = new HashMap<ClusterCenter, Integer>();
		for(int i = 0; i < kClusters.size(); i++) {
			colorMap.put(kClusters.get(i), (int)(((double)i / (double)kClusters.size()) * 255));
		}
		
		File f = new File(filePath + ".png");
		f.delete();
		int[] outImg = new int[imageRep.length * imageRep[0].length * 4];
		for(int i = 0; i < imageRep.length; i++) {
			for(int j = 0; j < imageRep[i].length; j++) {
				int val = colorMap.get(imageRep[i][j].getCluster());
				for(int k = 0; k < 4; k++)
					outImg[(j * imageRep[j].length + i) * 4 + k] = (k == 3 ? 255 : val);
			}
		}
		BufferedImage out = new BufferedImage(imageRep.length, imageRep[0].length, BufferedImage.TYPE_INT_ARGB);
		WritableRaster raster = (WritableRaster)out.getRaster();
		raster.setPixels(0, 0, imageRep.length, imageRep[0].length, outImg);
		ImageIO.write(out, "png", f);
	}
	
}

class PixelBright {
	
	double x;
	double y;
	double bright;
	ClusterCenter closestCluster;
	int version;
	
	public PixelBright(double inX, double inY, Color col, int width, int height, int vers) {
		x = inX / width;
		y = inY / height;
		double r = col.getRed() / 255.0 * .299;
		double g = col.getGreen() / 255.0 * .587;
		double b = col.getBlue() / 255.0 * .114;
		bright = r + g + b;
		version = vers;
	}
	
	public double getX() {
		return x;
	}
	
	public double getY() {
		return y;
	}
	
	public double getBright() {
		return bright;
	}
	
	public ClusterCenter getCluster() {
		return closestCluster;
	}
	
	public void assignSelf(ArrayList<ClusterCenter> clusters) {
		double dist = 10;
		ClusterCenter closest = null;
		for(ClusterCenter clus : clusters) {
			double val = clus.calcDistance(this, version);
			if(val < dist || closest == null) {
				dist = val;
				closest = clus;
			}
		}
		closest.addPixelBright(this);
		closestCluster = closest;
	}
	
}

class ClusterCenter {
	
	double x;
	double y;
	double bright;
	ArrayList<PixelBright> nearest;
	
	public ClusterCenter(double inX, double inY, double inBright) {
		x = inX;
		y = inY;
		bright = inBright;
		nearest = new ArrayList<PixelBright>();
	}
	
	public boolean averageValues() {
		if(nearest.size() == 0) {
			Random rand = new Random();
			x = rand.nextDouble();
			y = rand.nextDouble();
			bright = rand.nextDouble();
			return false;
		}
		
		double oldX = x;
		double oldY = y;
		double oldBright = bright;
		
		double avX = 0;
		double avY = 0;
		double avBright = 0;
		for(PixelBright pB : nearest) {
			avX += pB.getX();
			avY += pB.getY();
			avBright += pB.getBright();
		}
		x = avX / nearest.size();
		y = avY / nearest.size();
		bright = avBright / nearest.size();
		if(x == oldX && y == oldY && bright == oldBright) {
			return true;
		}
		else {
			return false;
		}
	}

	public void clearNearest() {
		nearest.clear();
	}

	public void addPixelBright(PixelBright pB) {
		nearest.add(pB);
	}
	
	public double calcDistance(PixelBright pB, int vers) {
		switch(vers) {
			case 0: return Math.sqrt(Math.pow(x - pB.getX(), 2) + Math.pow(y - pB.getY(), 2) + Math.pow(bright - pB.getBright(), 2));
			case 1: return Math.sqrt(Math.pow(bright - pB.getBright(), 2));
			case 2: return Math.sqrt(Math.pow(x - pB.getX(), 2) + Math.pow(y - pB.getY(), 2) + 2 * Math.pow(bright - pB.getBright(), 2));
			case 3: return Math.sqrt(2 * Math.pow(x - pB.getX(), 2) + 2 * Math.pow(y - pB.getY(), 2) + Math.pow(bright - pB.getBright(), 2));
			default: return 0;
		}
	}
	
	public void print(String in) {
		System.out.println(in);
	}
	
}
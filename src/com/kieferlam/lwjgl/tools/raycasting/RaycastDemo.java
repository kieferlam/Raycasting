package com.kieferlam.lwjgl.tools.raycasting;

import com.kieferlam.lwjgl.tools.raycasting.Vec2f;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.system.MemoryUtil;

import java.io.File;
import java.util.*;

/**
 * Created by Kiefer on 19/08/2015.
 */
public class RaycastDemo {

	private static boolean launched = false;

	public static GLFWErrorCallback errorCallback;
	public static GLFWCursorPosCallback mousePositionCallback;
	public static GLFWMouseButtonCallback mouseButtonCallback;
	public static GLFWKeyCallback keyCallback;
	public static long windowId;



	public static int windowWidth = 854;
	public static int windowHeight = 480;

	public static void main(String[] args){

		if(System.getProperty("os.arch").contains("64")){
			System.setProperty("org.lwjgl.librarypath", new File(System.getProperty("user.dir") + "/natives/lwjgl3/x64/").getAbsolutePath());
		}else{
			System.setProperty("org.lwjgl.librarypath", new File(System.getProperty("user.dir") + "/natives/lwjgl3/x86/").getAbsolutePath());
		}

		launch(args);

	}

	public static void launch(String... args){
		if(launched) return;
		launched = true;
		createWindow();
		initialiseGL();
		launchLoop();
		destroy();
	}

	private static void createWindow(){
		//Setting up the error callback
		errorCallback = Callbacks.errorCallbackPrint(System.err);
		GLFW.glfwSetErrorCallback(errorCallback);

		//Inititalise GLFW or produce an error
		if(GLFW.glfwInit() != GL11.GL_TRUE){
			System.err.println("Error initialising GLFW.");
			destroy();
			showErrorDialog();
			System.exit(1);
		}

		//Setup window
		windowId = GLFW.glfwCreateWindow(windowWidth,windowHeight, "William and Callum's Puzzle Game", 0, 0);
		if(windowId == MemoryUtil.NULL){
			System.err.println("Failed to create GLFW window.");
			destroy();
			showErrorDialog();
			System.exit(1);
		}

		//Creating GL Context
		GLFW.glfwMakeContextCurrent(windowId); //Makes the window that we just created the window we are using for OpenGL
		GLContext.createFromCurrent(); //Creates GL Context from the current window

		//Setting input callbacks
		mousePositionCallback = new GLFWCursorPosCallback() {
			@Override
			public void invoke(long window, double xpos, double ypos) {
				if(launched){
//					loop.mousePositionCall(window, xpos - windowWidth/2, (windowHeight+1) - ypos - windowHeight/2);
					castPoint.set((float)xpos - windowWidth*0.5f, windowHeight*0.5f - (float)ypos);
				}
			}
		};
		mouseButtonCallback = new GLFWMouseButtonCallback() {
			@Override
			public void invoke(long window, int button, int action, int mods) {
				if(launched){
//					loop.mouseButtonCall(window, button, action, mods);
				}
			}
		};
		keyCallback = new GLFWKeyCallback() {
			@Override
			public void invoke(long window, int key, int scancode, int action, int mods) {
				if(launched){
//					loop.keyCall(window, key, scancode, action, mods);
				}
			}
		};
		GLFW.glfwSetCursorPosCallback(windowId, mousePositionCallback);
		GLFW.glfwSetMouseButtonCallback(windowId, mouseButtonCallback);
		GLFW.glfwSetKeyCallback(windowId, keyCallback);
	}

	private static void initialiseGL(){
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();		GL11.glOrtho(-windowWidth*0.5f, windowWidth*0.5f, -windowHeight*0.5f, windowHeight*0.5f, -1.0f, 1.0f);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);

		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
	}

	private static void launchLoop(){

		double oldTimeSeconds = GLFW.glfwGetTime();

		double framerate;

		boxCoords.add(new Vec2f(windowWidth*0.5f*0.8f, windowHeight*0.5f*0.8f));
		boxCoords.add(new Vec2f(windowWidth*0.5f*-0.5f, windowHeight*0.5f*0.4f));
		boxCoords.add(new Vec2f(windowWidth*0.5f*-0.5f, windowHeight*0.5f*-0.8f));
		boxCoords.add(new Vec2f(windowWidth*0.5f*-0.2f, windowHeight*0.5f*0.8f));
		boxCoords.add(new Vec2f(0, 0));
		boxCoords.add(new Vec2f(-10, -10));

		collisions = new CollisionRule[boxCoords.size()];
		for(int i = 0; i <collisions.length; ++i){
			final int finalI = i;
			collisions[i] = (center, x, y) -> {
				if((aabb(boxCoords.get(finalI).x - 25, boxCoords.get(finalI).y - 25, 50, 50, x, y, 0, 0))||
						   (aabb(boxCoords.get(finalI).x - 25, boxCoords.get(finalI).y - 25, 50, 50, x, y, 0, 0))||
						   (aabb(boxCoords.get(finalI).x - 25, boxCoords.get(finalI).y - 25, 50, 50, x, y, 0, 0))||
						   (aabb(boxCoords.get(finalI).x - 25, boxCoords.get(finalI).y - 25, 50, 50, x, y, 0, 0))){
					return true;
				}
				return false;
			};
		}

		while(!(GLFW.glfwWindowShouldClose(windowId) == GL11.GL_TRUE)){
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

			double currentTimeSeconds = GLFW.glfwGetTime();
			double deltaTimeSeconds = currentTimeSeconds - oldTimeSeconds;
			oldTimeSeconds = currentTimeSeconds;

			framerate = 1.0 / deltaTimeSeconds;
			GLFW.glfwSetWindowTitle(windowId, framerate + "");

			manageSleep(deltaTimeSeconds);

			logic(deltaTimeSeconds);
			render();

			GLFW.glfwPollEvents();
			GLFW.glfwSwapBuffers(windowId);
		}
	}

	public static ArrayList<Vec2f> boxCoords = new ArrayList<>();
	public static Vec2f castPoint = new Vec2f(-50, -50);
	public static CollisionRule[] collisions;

	private static void logic(double deltaSecs){
//		for(Vec2f pos : boxCoords){
//			pos.addLocal((float) (Math.cos(GLFW.glfwGetTime()) * deltaSecs * 5), (float) (Math.sin(GLFW.glfwGetTime()) * deltaSecs * 5));
//		}
	}

	private static void render(){

		float multiCastDistance = 10;

//		ArrayList<Double> angles = new ArrayList<>(360/45 + boxCoords.size() * 4*3);
//		for(float deg = 0; deg < 360; deg += 45f){
//			angles.add(Math.toRadians(deg));
//		}
//		for(int i = 0; i < boxCoords.size(); ++i){
//			Vec2f boxPos = boxCoords.get(i);
//			double boxAngle = angleBetweenVectors(castPoint, boxPos.add(-25.0f, -25.0f));
//			double jitterOffset = 0.02;
//			if(boxAngle < 0) boxAngle += Math.PI*2;
//			angles.add(boxAngle);
//			angles.add(boxAngle + jitterOffset);
//			angles.add(boxAngle - jitterOffset);
//			boxAngle = angleBetweenVectors(castPoint, boxPos.add(25.0f, -25.0f));
//			if(boxAngle < 0) boxAngle += Math.PI*2;
//			angles.add(boxAngle);
//			angles.add(boxAngle + jitterOffset);
//			angles.add(boxAngle - jitterOffset);
//			boxAngle = angleBetweenVectors(castPoint, boxPos.add(25.0f, 25.0f));
//			if(boxAngle < 0) boxAngle += Math.PI*2;
//			angles.add(boxAngle);
//			angles.add(boxAngle + jitterOffset);
//			angles.add(boxAngle - jitterOffset);
//			boxAngle = angleBetweenVectors(castPoint, boxPos.add(-25.0f, 25.0f));
//			if(boxAngle < 0) boxAngle += Math.PI*2;
//			angles.add(boxAngle);
//			angles.add(boxAngle + jitterOffset);
//			angles.add(boxAngle - jitterOffset);
//		}
//
//		Collections.sort(angles);

		ArrayList<Vec2f> boxVertices = new ArrayList<>(boxCoords.size()*4);
		for(Vec2f boxPos : boxCoords){
			boxVertices.add(boxPos.add(-25,-25));
			boxVertices.add(boxPos.add(25,-25));
			boxVertices.add(boxPos.add(25,25));
			boxVertices.add(boxPos.add(-25,25));
		}

//		Vec2f[][] raycasts = new Vec2f[9][];
//		for(int i = 0; i < raycasts.length-1; ++i){
//			raycasts[i] = RaycastEngine.castAll(castPoint.add((float)Math.cos(((Math.PI*2) / raycasts.length-1) * i) * multiCastDistance, (float)Math.sin(((Math.PI * 2) / raycasts.length - 1) * i) * multiCastDistance ), 400, boxCoords.toArray(new Vec2f[0]), 0.01f, collisions);
//		}
//		raycasts[8] = RaycastEngine.castAll(castPoint, 400, boxCoords.toArray(new Vec2f[0]), 0.01f, collisions);

//		Vec2f[][] raycasts = {RaycastEngine.castAll(castPoint, 400, boxCoords.toArray(new Vec2f[0]), 0.01f, collisions)};

//		ArrayList<Vec2f> rayPoints = new ArrayList<>();
//		for(Double angle : angles){
//			rayPoints.add(RaycastEngine.castRay(castPoint, angle, 400, collisions));
//		}

		float jitter = 0.02f;

		ArrayList<ArrayList<Vec2f>> raycasts = new ArrayList<>(360/45 + 1);
		ArrayList<Vec2f> castPoints = new ArrayList<>();
		for(int i = 0; i < 8; ++i){
			raycasts.add(new ArrayList<>());
			castPoints.add(castPoint.add((float)Math.cos(i * Math.toRadians(360/8)) * multiCastDistance, (float)Math.sin(i * Math.toRadians(360/8)) * multiCastDistance));
			Collections.addAll(raycasts.get(i), RaycastEngine.castAll(castPoints.get(i), 400, boxVertices.toArray(new Vec2f[0]), jitter, collisions));
		}
		raycasts.add(new ArrayList<>());
		castPoints.add(castPoint);
		Collections.addAll(raycasts.get(raycasts.size()-1), RaycastEngine.castAll(castPoint, 400, boxVertices.toArray(new Vec2f[0]), jitter, collisions));

		for(int cast = 0; cast < raycasts.size(); ++cast){

			ArrayList<Vec2f> rayPoints = raycasts.get(cast);

			Vec2f correctedCastPoint = castPoints.get(cast);

			GL11.glBegin(GL11.GL_TRIANGLES);
			for(int i = 0; i < rayPoints.size(); ++i){
				Vec2f point = rayPoints.get(i);
				float brightness = correctedCastPoint.distance(point) / 400.0f;
				if(i == rayPoints.size()-1){
					GL11.glColor4f(1.0f - brightness, 1.0f - brightness, 1.0f - brightness, 0.1f);
					GL11.glVertex2f(point.x, point.y);
					brightness = correctedCastPoint.distance(rayPoints.get(0)) / 400.0f;
					GL11.glColor4f(1.0f - brightness, 1.0f - brightness, 1.0f - brightness, 0.1f);
					GL11.glVertex2f(rayPoints.get(0).x, rayPoints.get(0).y);
					GL11.glColor4f(1.0f, 1.0f, 1.0f, 0.1f);
					GL11.glVertex2f(correctedCastPoint.x, correctedCastPoint.y);
				}else{
					Vec2f nextPoint = rayPoints.get(i+1);
					GL11.glColor4f(1.0f - brightness, 1.0f - brightness, 1.0f - brightness, 0.1f);
					GL11.glVertex2f(point.x, point.y);
					brightness = correctedCastPoint.distance(nextPoint) / 400.0f;
					GL11.glColor4f(1.0f - brightness, 1.0f - brightness, 1.0f - brightness, 0.1f);
					GL11.glVertex2f(nextPoint.x, nextPoint.y);
					GL11.glColor4f(1.0f, 1.0f, 1.0f, 0.1f);
					GL11.glVertex2f(correctedCastPoint.x, correctedCastPoint.y);
				}
			}
			GL11.glEnd();

		}


		for(Vec2f boxPos : boxCoords){
			GL11.glColor4f(0.6f, 0.4f, 0.45f, 1.0f);
			GL11.glTranslatef(boxPos.x, boxPos.y, 0.0f);
			GL11.glBegin(GL11.GL_QUADS);
			GL11.glColor4f(0.6f, 0.4f, 0.45f, 1.0f - castPoint.distance(boxPos.add(-25.0f, -25.0f)) / 400.0f);
			GL11.glVertex2f(-25.0f, -25.0f);
			GL11.glColor4f(0.6f, 0.4f, 0.45f, 1.0f - castPoint.distance(boxPos.add(25.0f, -25.0f)) / 400.0f);
			GL11.glVertex2f(25.0f, -25.0f);
			GL11.glColor4f(0.6f, 0.4f, 0.45f, 1.0f - castPoint.distance(boxPos.add(25.0f, 25.0f)) / 400.0f);
			GL11.glVertex2f(25.0f, 25.0f);
			GL11.glColor4f(0.6f, 0.4f, 0.45f, 1.0f - castPoint.distance(boxPos.add(-25.0f, 25.0f)) / 400.0f);
			GL11.glVertex2f(-25.0f, 25.0f);
			GL11.glEnd();
			GL11.glTranslatef(-boxPos.x, -boxPos.y, 0.0f);
		}

	}

	public static double[] doubleArrayListToPrim(ArrayList<Double> doubles){
		double[] doublesPrim = new double[doubles.size()];
		for(int i = 0; i < doubles.size(); ++i){
			doublesPrim[i] = doubles.get(i);
		}
		return doublesPrim;
	}

	public static double angleBetweenVectors(Vec2f origin, Vec2f target){
		return Math.atan2((target.y - origin.y), (target.x - origin.x));
	}

	public static Vec2f castRay(Vec2f center, double angle, float cutoff, ArrayList<Vec2f> boxes){
		double radians = angle;

		float dx = (float) (Math.cos(radians) * 2.0f);
		float dy = (float) (Math.sin(radians) * 2.0f);

		float accumulatedDistance = 0.0f;

		float oldx = center.x;
		float oldy = center.y;
		while(accumulatedDistance < cutoff){
			accumulatedDistance += Math.sqrt(dx*dx + dy*dy);

			oldx += dx;
			oldy += dy;

			for(Vec2f box : boxes){
				if(aabb(oldx, oldy, 0, 0, box.x - 25, box.y - 25, 50 ,50)){
					return new Vec2f(oldx, oldy);
				}
			}
		}

		return new Vec2f((float)Math.cos(radians) * cutoff + center.x, (float)Math.sin(radians) * cutoff + center.y);
	}

	public static Vec2f nearestVec2f(ArrayList<Vec2f> coords){
		int lowestIndexSoFar = 0;
		float shortestDistanceSoFar = Float.MAX_VALUE;
		for(int index = 0; index < coords.size(); ++index){
			float distance = coords.get(index).distance(castPoint);
			if(distance < shortestDistanceSoFar){
				lowestIndexSoFar = index;
				shortestDistanceSoFar = distance;
			}
		}
		return coords.get(lowestIndexSoFar);
	}

	public static boolean aabb(float x, float y, float w, float h, float x2, float y2, float w2, float h2){
		if(x + w <= x2 || x >= x2 + w2) return false;
		if(y + h <= y2 || y >= y2 + h2) return false;

		return true;
	}

	private static void manageSleep(double deltaTimeSeconds){
		double sleepTimeForDesiredFramerateSeconds = 1.0 / 60.0;
		if(deltaTimeSeconds < sleepTimeForDesiredFramerateSeconds){
			try {
				Thread.sleep((long) (sleepTimeForDesiredFramerateSeconds - deltaTimeSeconds) * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public static void showErrorDialog(){
		//TODO ERROR DIALOG
	}

	public static void destroy(){
		if(!launched) return;
		launched = false;
		if(windowId != MemoryUtil.NULL){
			GLFW.glfwDestroyWindow(windowId);
		}
		mousePositionCallback.release();
		mouseButtonCallback.release();
		keyCallback.release();
		GLFW.glfwTerminate();
		errorCallback.release();
	}

}

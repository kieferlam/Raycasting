package com.kieferlam.lwjgl.tools.raycasting;

import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Kiefer on 21/08/2015.
 *
 * This is the main class for raycasting. The most simple way to use this is to just use castAll()
 *
 */
public enum RaycastEngine {
	INSTANCE;

	private static ArrayList<Float> defaultAngles;
	static {
		defaultAngles = new ArrayList<>();
		for(int angle = 45; angle < 360; angle+=90)defaultAngles.add((float) Math.toRadians(angle));
	}


	/**
	 *
	 * Gives you an array of the points of contact from each ray.
	 * You should provide (in order):
	 *
	 * • Vec2f - Center, where the rays come out of
	 * • float - Cutoff, the distance a ray travels before it ends (without any contact)
	 * • Vec2f[] - Array of vertices of all objects that cast shadows. (Note: angles 45 degrees [each corner] to the castPoint are already added)
	 * • float - Jitter; multiple rays are casted at each vertice with this offset to reach any walls or ensure contact with the object
	 * • CollisionRule... - List of CollisionRule(s) to determine whether the ray has made contact
	 *
	 * @param castPoint
	 * @param cutoff
	 * @param points
	 * @param jitter
	 * @param collidables
	 * @return vec2f[]
	 */

	public static Vec2f[] castAll(Vec2f castPoint, float cutoff, Vec2f[] points, float jitter, CollisionRule... collidables) {
		ArrayList<Float> angles = new ArrayList<>(points.length * 5 + defaultAngles.size());
		angles.addAll(defaultAngles);
		for (Vec2f point : points) {
			float angle = (float) angleBetweenVectors(castPoint, point);
			if(angle < 0) angle += Math.PI*2;
			angles.add(angle);
			angles.add(angle + jitter/4);
			angles.add(angle - jitter/4);
			angles.add(angle + jitter);
			angles.add(angle - jitter);
		}
		return castAll(castPoint, cutoff, floatArrayListToPrim(angles), collidables);
	}


	/**
	 *
	 * Gives you an array of the points of contact from each ray.
	 * You should provide (in order):
	 *
	 * • Vec2f - Center, where the rays come out of
	 * • float - Cutoff, the distance a ray travels before it ends (without any contact)
	 * • float - stepDistance, the distance a ray travels per iteration
	 * • Vec2f[] - Array of vertices of all objects that cast shadows. (Note: angles 45 degrees [each corner] to the castPoint are already added)
	 * • float - Jitter; multiple rays are casted at each vertice with this offset to reach any walls or ensure contact with the object
	 * • CollisionRule... - List of CollisionRule(s) to determine whether the ray has made contact
	 *
	 * @param castPoint
	 * @param cutoff
	 * @param stepDistance
	 * @param points
	 * @param jitter
	 * @param collidables
	 * @return vec2f[]
	 */
	public static Vec2f[] castAll(Vec2f castPoint, float cutoff, float stepDistance, Vec2f[] points, float jitter, CollisionRule... collidables) {
		ArrayList<Float> angles = new ArrayList<>(points.length * 5 + defaultAngles.size());
		angles.addAll(angles);
		for (Vec2f point : points) {
			float angle = (float) angleBetweenVectors(castPoint, point);
			angles.add(angle);
			angles.add(angle + jitter/4);
			angles.add(angle - jitter/4);
			angles.add(angle + jitter);
			angles.add(angle - jitter);
		}
		return castAll(castPoint, cutoff, stepDistance, floatArrayListToPrim(angles), collidables);
	}

	/**
	 *
	 * Gives you an array of the points of contact from each ray.
	 * You should provide (in order):
	 *
	 * • Vec2f - Center, where the rays come out of
	 * • float - Cutoff, the distance a ray travels before it ends (without any contact)
	 * • float - stepDistance, the distance a ray travels per iteration
	 * • float[] - Angles, the angles (in radians) for each ray to cast (Note: the angles will be sorted automatically)
	 * • CollisionRule... - List of CollisionRule(s) to determine whether the ray has made contact
	 *
	 * @param castPoint
	 * @param cutoff
	 * @param stepDistance
	 * @param angles
	 * @param collidables
	 * @return vec2f[]
	 */
	public static Vec2f[] castAll(Vec2f castPoint, float cutoff, float stepDistance, float[] angles, CollisionRule... collidables){
		Vec2f[] results = new Vec2f[angles.length];

		angles = sortAngles(angles);

		for(int i = 0; i < results.length; ++i){
			results[i] = castRay(castPoint, angles[i], cutoff, stepDistance, collidables);
		}

		return results;
	}


	/**
	 *
	 * Gives you an array of the points of contact from each ray.
	 * You should provide (in order):
	 *
	 * • Vec2f - Center, where the rays come out of
	 * • float - Cutoff, the distance a ray travels before it ends (without any contact)
	 * • float[] - Angles, the angles (in radians) for each ray to cast (Note: the angles will be sorted automatically)
	 * • CollisionRule... - List of CollisionRule(s) to determine whether the ray has made contact
	 *
	 * @param castPoint
	 * @param cutoff
	 * @param angles
	 * @param collidables
	 * @return vec2f[]
	 */
	public static Vec2f[] castAll(Vec2f castPoint, float cutoff, float[] angles, CollisionRule... collidables){
		Vec2f[] results = new Vec2f[angles.length];

		angles = sortAngles(angles);

		for(int i = 0; i < results.length; ++i){
			results[i] = castRay(castPoint, angles[i], cutoff, collidables);
		}

		return results;
	}

	private static float[] sortAngles(float[] angles){
		Arrays.sort(angles);
		return angles;
	}

	/**
	 *
	 * Use this to cast a ray at a specific angle (in radians) without using castAll().
	 * This gives you a single Vec2f where the ray makes contact with the given CollisionRule(s).
	 *
	 * @param center
	 * @param radians
	 * @param cutoff
	 * @param collidables
	 * @return contactPoint
	 */
	public static Vec2f castRay(Vec2f center, double radians, float cutoff, CollisionRule... collidables){

		float rayStepX = (float) (Math.cos(radians)) * 2.0f;
		float rayStepY = (float) (Math.sin(radians)) * 2.0f;
		float stepDistance = (float) Math.sqrt(rayStepX*rayStepX + rayStepY*rayStepY);

		float rayX = center.x, rayY = center.y;
		for(float accumulatedDistance = 0.0f; accumulatedDistance < cutoff; accumulatedDistance += stepDistance){

			rayX += rayStepX;
			rayY += rayStepY;

			for(CollisionRule collisionRule : collidables){
				if(collisionRule.collision(center, rayX, rayY)){
					return new Vec2f(rayX, rayY);
				}
			}
		}

		return center.add((float) Math.cos(radians) * cutoff, (float) Math.sin(radians) * cutoff);
	}

	/**
	 *
	 * Use this to cast a ray at a specific angle (in radians) without using castAll().
	 * This gives you a single Vec2f where the ray makes contact with the given CollisionRule(s).
	 *
	 * @param center
	 * @param radians
	 * @param cutoff
	 * @param stepDistance
	 * @param collidables
	 * @return contactPoint
	 */
	public static Vec2f castRay(Vec2f center, double radians, float cutoff, float stepDistance, CollisionRule... collidables){

		float rayStepX = (float) (Math.cos(radians)) * stepDistance;
		float rayStepY = (float) (Math.sin(radians)) * stepDistance;
		float stepLength = (float) Math.sqrt(rayStepX*rayStepX + rayStepY*rayStepY);

		float rayX = center.x, rayY = center.y;
		for(float accumulatedDistance = 0; accumulatedDistance < cutoff; accumulatedDistance += stepLength){

			rayX += rayStepX;
			rayY += rayStepY;

			for(CollisionRule collisionRule : collidables){
				if(collisionRule.collision(center, rayX, rayY)){
					return new Vec2f(rayX, rayY);
				}
			}
		}

		return center.add((float)Math.cos(radians) * cutoff, (float)Math.sin(radians) * cutoff);
	}

	private static double angleBetweenVectors(Vec2f origin, Vec2f target){
		return Math.atan2((target.y - origin.y), (target.x - origin.x));
	}

	private static float[] floatArrayListToPrim(ArrayList<Float> arrayList){
		float[] array = new float[arrayList.size()];
		for (int i = 0 ; i < arrayList.size(); ++i){
			array[i] = arrayList.get(i);
		}
		return array;
	}

}

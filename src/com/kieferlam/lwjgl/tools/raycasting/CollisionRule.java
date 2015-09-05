package com.kieferlam.lwjgl.tools.raycasting;

/**
 * Created by Kiefer on 21/08/2015.
 *
 * Interface created to allow you to do your own collision detection.
 *
 */
public interface CollisionRule {

	boolean collision(Vec2f center, float rayX, float rayY);

}

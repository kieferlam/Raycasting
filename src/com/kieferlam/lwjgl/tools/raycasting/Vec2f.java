package com.kieferlam.lwjgl.tools.raycasting;

/**
 * Created by Kiefer on 20/08/2015.
 */
public class Vec2f{

	public float x,y;

	public Vec2f(float x, float y){
		this.x = x;
		this.y = y;
	}

	public Vec2f(Vec2f v) {
		this(v.x, v.y);
	}

	public Vec2f add(Vec2f v){
		return add(v.x, v.y);
	}
	public Vec2f add(float x2, float y2){
		return new Vec2f(x + x2, y + y2);
	}
	public Vec2f addLocal(Vec2f v){
		return addLocal(v.x, v.y);
	}
	public Vec2f addLocal(float x2, float y2){
		this.x += x2;
		this.y += y2;
		return this;
	}

	public Vec2f set(float x, float y){
		this.x = x;
		this.y = y;
		return this;
	}
	public Vec2f set(Vec2f v){
		return set(v.x, v.y);
	}

	public float distance(float x, float y){
		return (float) Math.sqrt(Math.pow(x - this.x, 2) + Math.pow(y - this.y, 2));
	}
	public float distance(Vec2f v){
		return distance(v.x, v.y);
	}

	public float length(){
		return (float) Math.sqrt(lengthSqrDouble());
	}
	public float lengthSqr(){
		return (float) lengthSqrDouble();
	}
	private double lengthSqrDouble(){
		return (Math.pow(x, 2) + Math.pow(y, 2));
	}

	public Vec2f mul(float x, float y){
		return new Vec2f(this.x * x, this.y * y);
	}
	public Vec2f mul(float w){
		return mul(w, w);
	}
	public Vec2f mul(Vec2f v){
		return mul(v.x, v.y);
	}
	public Vec2f mulLocal(float x, float y){
		this.x *= x;
		this.y *= y;
		return this;
	}
	public Vec2f mulLocal(float w){
		return mulLocal(w, w);
	}
	public Vec2f mulLocal(Vec2f v){
		return mul(v.x, v.y);
	}

	public Vec2f div(float x, float y){
		return new Vec2f(this.x / x, this.y / y);
	}
	public Vec2f div(float w){
		return mul(w, w);
	}
	public Vec2f div(Vec2f v){
		return mul(v.x, v.y);
	}
	public Vec2f divLocal(float x, float y){
		this.x /= x;
		this.y /= y;
		return this;
	}
	public Vec2f divLocal(float w){
		return mulLocal(w, w);
	}
	public Vec2f divLocal(Vec2f v){
		return mul(v.x, v.y);
	}

}

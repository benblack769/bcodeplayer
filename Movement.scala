package benplayer

import battlecode.common._
import sun.security.util.Length

final class BulletPath(bul:BulletInfo){
	
}
class Vector(xc:Float,yc:Float){
	var x: Float = xc
	var y: Float = yc
	def this(direction: Direction,length:Float)
	= this(direction.getDeltaX(length),direction.getDeltaY(length))
	def this(m1: MapLocation,m2: MapLocation)
	 = this(m1.x - m2.x,m1.y - m2.y)
	def + (other:Vector):Vector = new Vector(x + other.x, y + other.y)
	def scale(dis:Float):Vector = new Vector(x * dis,y * dis)
	def direction() = new Direction(x,y)
	def length() = math.sqrt(x*x + y*y).toFloat
	def unit() = scale(length())
	def add(mloc:MapLocation) = new MapLocation(x + mloc.x,y + mloc.y)
}
final class Movement(incen:MapLocation,movespeed:Float){
	val cen:MapLocation = incen
	val speed:Float = movespeed
	
	var lin_vec:Vector = new Vector(0,0)
	
	var blist:List[BulletPath] = Nil
	
	def add_liniar_pull(mloc: MapLocation,invalue:Float): Unit = {
		val vec = new Vector(cen, mloc)
		val svec = vec.scale(invalue)
		lin_vec = lin_vec + svec
	}
	def add_bullet_path(bul:BulletInfo): Unit ={
		blist = new BulletPath(bul)::blist
	}
	def best_point():MapLocation = {
		lin_vec.unit().scale(speed).add(cen)
	}
}
object Movement{
	def add(one: MapLocation,other: MapLocation) =
		new MapLocation(one.x + other.x,one.y + other.y)
	def scale(a:MapLocation,dis:Float) =
		new MapLocation(a.x*dis,a.y*dis)
}
package benplayer

import battlecode.common._
import scala.collection.mutable.ArrayBuffer

final class Vector(xc:Float,yc:Float){
	val x: Float = xc
	val y: Float = yc
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
	def perpVec(direct:Boolean) = new Vector(-y,x).scale(if(direct) 1 else -1)
	def negate() = new Vector(-x,-y)
	def * (other:Vector) = x * other.x + y * other.y
}
final class Obstical(incen: MapLocation,inrad:Float){
	val cen:MapLocation  = incen
	val rad:Float = inrad
	def is_inside(p:MapLocation): Boolean =
		cen.distanceTo(p) < rad
	def doesCollide(src:MapLocation, path:Vector,propogation_dir:Direction):Boolean = {
		if(path.length() < rad + src.distanceTo(cen)){
			return false
		}
		val dirToRob = src.directionTo(cen)
		if(propogation_dir.radiansBetween(dirToRob) > Math.PI/2){
			return false
		}
		//TODO: make this estimate perfect by introducing line intersection mechanics
		
		//needed check for perfect estimate
		//val end = path.add(src)
		//if(is_inside(end)){
		//	return true
		//}
		return true
	}
}
object Obstical{
	def perpDis(loc:MapLocation,bul:BulletInfo): Float ={
		val dirToRob = bul.location.directionTo(loc)
		val theta = bul.dir.radiansBetween(dirToRob)
		if(Math.abs(theta) > Math.PI/2){
			return 10000000.0f
		}
		val distToRobot = bul.location.distanceTo(loc)
		Math.abs(distToRobot * Math.sin(theta)).toFloat
	}
}
final class PointVal(inp:MapLocation,inval:Float){
	val p:MapLocation = inp
	val value:Float = inval
}
final class Movement(incen:MapLocation,movespeed:Float,inbody_rad:Float,indamage_value:Float){
	val cen:MapLocation = incen
	val speed:Float = movespeed
	val body_rad:Float = inbody_rad
	val damage_value:Float = indamage_value
	
	var lin_vec:Vector = new Vector(0,0)
	
	var blist:List[BulletInfo] = Nil
	var obslist:List[Obstical] = Nil
	var specialPoints:List[PointVal] = Nil
	
	def addLiniarPull(mloc: MapLocation, invalue:Float): Unit = {
		val vec = new Vector(cen, mloc)
		val svec = vec.scale(invalue)
		lin_vec = lin_vec + svec
	}
	def addObstical(cen : MapLocation, rad:Float):Unit = {
		obslist = new Obstical(cen,rad)::obslist
	}
	def addBulletPath(bul:BulletInfo): Unit = {
		blist = bul :: blist
	}
	def addPointVal(point:MapLocation,value:Float): Unit ={
		specialPoints = new PointVal(point,value)::specialPoints
	}
	def bestPoint(randPs: Array[MapLocation]):MapLocation = {
		var checkPoints: ArrayBuffer[PointVal] = new ArrayBuffer[PointVal]
		//for(loc <- randPs)
	//		checkPoints += new PointVal(loc,calcVal(loc))
		
		val lin_point = lin_vec.unit().scale(speed).add(cen)
		return lin_point
		//checkPoints += new PointVal(lin_point,calcVal(lin_point))
		/*
		for(loc <- specialPoints)
			checkPoints += new PointVal(loc.p,calcVal(loc.p) + loc.value)
		*/
		//checkPoints.filter(pv => getBlockingObs(pv.p).isEmpty).maxBy(pv => pv.value).p
	}
	
	/*
	* helper methods for bestPoint.
	*
	* Do not call any of these anywhere else!!
	* */
	def calcLinVal(mloc:MapLocation):Float = {
		val pvec = new Vector(cen,mloc)
		pvec * lin_vec
	}
	def rangeBulletPath(bul:BulletInfo,mloc:MapLocation):Int = {
		val perp_dis = Obstical.perpDis(mloc,bul)
		val bul_dis = mloc.distanceTo(mloc)
		
		var fut_body_rad = body_rad
		var count = 0
		while(perp_dis < fut_body_rad &&
			bul_dis - fut_body_rad < bul.speed * (count + 1)){
			
			fut_body_rad -= (speed * Const.MOVE_EFFICIENCY)
			count += 1
		}
		return count
	}
	def calcVal(mloc:MapLocation):Float = {
		var endval = calcLinVal(mloc)
		for(bul <- blist){
			val bul_imp = rangeBulletPath(bul,mloc)
			if (bul_imp > 0) {
				endval -= bul.damage * damage_value
			}
		}
		endval
	}
	def getBlockingObs(loc:MapLocation):Option[Obstical]  ={
		for(obs <- obslist){
			if(obs.is_inside(loc)){
				return Some(obs)
			}
		}
		return None
	}
	/*
	def getAltPoints(obs:Obstical,mloc: MapLocation) = {
		val mid = Movement.scale(Movement.add(cen,mloc),0.5f)
		val vec = new Vector(cen,mloc)
		val dis = vec.length()
		//val R =
	}
	
	def placeLiniarPoint(): Unit ={
		val linval = lin_vec.length()
		val linp = lin_vec.unit().scale(speed).add(cen)
		val pointval = new PointVal(linp,linval)
		val blocking_obs = getBlockingObs(pointval.p)
		
		blocking_obs match {
			case Some(obs) =>{
				val myobs = new Obstical(cen,speed)
				val (p1,p2) = Movement.getIntersectPs(myobs,obs)
				checkPoints += new PointVal(p1,1)
				checkPoints += new PointVal(p2,1)
			}
			case None => checkPoints += pointval
		}
	}
	def placeSpecialPoints(): Unit ={
		for(sp <- specialPoints){
			val blocking_obs = getBlockingObs(sp)
			blocking_obs match {
				case Some(obs) => Unit
				case None => checkPoints += sp
			}
		}
	}
	def applyObstical(obstical: Obstical): Unit ={
		
	}*/
}
object Movement{
	def add(one: MapLocation,other: MapLocation) =
		new MapLocation(one.x + other.x,one.y + other.y)
	def scale(a:MapLocation,dis:Float) =
		new MapLocation(a.x*dis,a.y*dis)
	def getIntersectPs(o1: Obstical, o2:Obstical) = {
		val vec = new Vector(o1.cen, o2.cen)
		val dis = vec.length()
		val perp_dis = Movement.hieghtTriang(o1.rad,o2.rad,dis)
		val o1_dis = Math.sqrt(Movement.sqr(o1.rad) + Movement.sqr(perp_dis)).toFloat
		val o2_dis = dis - o1_dis
		val mid_vec = vec.scale(o1_dis/dis)
		val midp = mid_vec.add(o1.cen)
		val perp_vec1 = vec.perpVec(true).unit().scale(dis)
		val perp_vec2 = perp_vec1.negate()
		val p1 = perp_vec1.add(midp)
		val p2 = perp_vec2.add(midp)
		(p1,p2)
	}
	def hieghtTriang(a:Float, b:Float, base:Float):Float = {
		val s = (a + b + base)/2
		val area = Movement.sqr(s*(s-a)*(s-b)*(s-base))
		2 * area / base
	}
	//def doesIntersect(l1:MapLocation,l2:MapLocation,q1:MapLocation,q2:MapLocation):Boolean = {
	//	val inter_p = new MapLocation(
	//
	//	)
	//}
	def sqr(x:Float) = x*x
	//def bulVec(bul:BulletInfo) = new Vector(bul.dir,bul.speed)
}